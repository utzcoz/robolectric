package org.robolectric.annotation.processing;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Sets.newTreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import org.robolectric.annotation.Implements;

/**
 * Model describing the Robolectric source file.
 */
public class RobolectricModel {

  private final TreeSet<String> imports;
  /**
   * Key: name of shadow class
   */
  private final TreeMap<String, ShadowInfo> shadowTypes;
  private final TreeMap<String, String> extraShadowTypes;
  /**
   * Key: name of shadow class
   */
  private final TreeMap<String, ResetterInfo> resetterMap;

  private final TreeMap<String, DocumentedPackage> documentedPackages;

  public Collection<DocumentedPackage> getDocumentedPackages() {
    return documentedPackages.values();
  }

  public RobolectricModel(TreeSet<String> imports,
      TreeMap<String, ShadowInfo> shadowTypes,
      TreeMap<String, String> extraShadowTypes,
      TreeMap<String, ResetterInfo> resetterMap,
      Map<String, DocumentedPackage> documentedPackages) {
    this.imports = new TreeSet<>(imports);
    this.shadowTypes = new TreeMap<>(shadowTypes);
    this.extraShadowTypes = new TreeMap<>(extraShadowTypes);
    this.resetterMap = new TreeMap<>(resetterMap);
    this.documentedPackages = new TreeMap<>(documentedPackages);
  }

  private static ElementVisitor<TypeElement, Void> typeElementVisitor = new SimpleElementVisitor6<TypeElement, Void>() {

    @Override
    public TypeElement visitType(TypeElement e, Void p) {
      return e;
    }
  };

  public static class Builder {

    private final ProcessingEnvironment environment;

    private final TreeSet<String> imports = newTreeSet();

    private final TreeMap<String, ShadowInfo> shadowTypes = newTreeMap();
    private final TreeMap<String, String> extraShadowTypes = newTreeMap();
    private final TreeMap<String, ResetterInfo> resetterMap = newTreeMap();
    private final Map<String, DocumentedPackage> documentedPackages = new TreeMap<>();

    private final Map<TypeElement, String> referentMap = newHashMap();
    private HashMultimap<String, TypeElement> typeMap = HashMultimap.create();
    private final Map<TypeElement, TypeElement> importMap = newHashMap();

    public Builder(ProcessingEnvironment environment) {
      this.environment = environment;
    }

    public void addShadowType(TypeElement shadowType, TypeElement actualType) {
      shadowTypes
          .put(shadowType.getQualifiedName().toString(), new ShadowInfo(shadowType, actualType));
    }

    public void addExtraShadow(String sdkClassName, String shadowClassName) {
      extraShadowTypes.put(shadowClassName, sdkClassName);
    }

    public void addResetter(TypeElement shadowTypeElement, ExecutableElement elem) {
      resetterMap.put(shadowTypeElement.getQualifiedName().toString(),
          new ResetterInfo(shadowTypeElement, elem));
    }

    public void documentPackage(String name, String documentation) {
      getDocumentedPackage(name).setDocumentation(documentation);
    }

    public void documentType(TypeElement type, String documentation, List<String> imports) {
      DocumentedType documentedType = getDocumentedType(type);
      documentedType.setDocumentation(documentation);
      documentedType.imports = imports;
    }

    public void documentMethod(TypeElement shadowClass, DocumentedMethod documentedMethod) {
      DocumentedType documentedType = getDocumentedType(shadowClass);
      documentedType.methods.put(documentedMethod.getName(), documentedMethod);
    }

    private DocumentedPackage getDocumentedPackage(String name) {
      DocumentedPackage documentedPackage = documentedPackages.get(name);
      if (documentedPackage == null) {
        documentedPackage = new DocumentedPackage(name);
        documentedPackages.put(name, documentedPackage);
      }
      return documentedPackage;
    }

    private DocumentedPackage getDocumentedPackage(TypeElement type) {
      Element pkgElement = type.getEnclosingElement();
      return getDocumentedPackage(pkgElement.toString());
    }

    private DocumentedType getDocumentedType(TypeElement type) {
      DocumentedPackage documentedPackage = getDocumentedPackage(type);
      return documentedPackage.getDocumentedType(type.getQualifiedName().toString());
    }

    public RobolectricModel build() {
      prepare();

      return new RobolectricModel(imports, shadowTypes, extraShadowTypes, resetterMap,
          documentedPackages);
    }

    /**
     * Prepares the various derived parts of the model based on the class mappings that have been
     * registered to date.
     */
    void prepare() {
      for (ShadowInfo shadowInfo : getVisibleShadowTypes()) {
        registerType(shadowInfo.shadowType);

        registerType(shadowInfo.actualType);
      }

      for (ShadowInfo shadowInfo : getResetterShadowTypes()) {
        registerType(shadowInfo.shadowType);
      }

      while (!typeMap.isEmpty()) {
        final HashMultimap<String, TypeElement> nextRound = HashMultimap.create();
        for (Map.Entry<String, Set<TypeElement>> referents : Multimaps.asMap(typeMap).entrySet()) {
          final Set<TypeElement> c = referents.getValue();
          // If there is only one type left with the given simple
          // name, then
          if (c.size() == 1) {
            final TypeElement type = c.iterator().next();
            referentMap.put(type, referents.getKey());
          } else {
            for (TypeElement type : c) {
              SimpleElementVisitor6<Void, TypeElement> visitor = new SimpleElementVisitor6<Void, TypeElement>() {
                @Override
                public Void visitType(TypeElement parent, TypeElement type) {
                  nextRound.put(parent.getSimpleName() + "." + type.getSimpleName(), type);
                  importMap.put(type, parent);
                  return null;
                }

                @Override
                public Void visitPackage(PackageElement parent, TypeElement type) {
                  referentMap.put(type, type.getQualifiedName().toString());
                  importMap.remove(type);
                  return null;
                }
              };
              visitor.visit(importMap.get(type).getEnclosingElement(), type);
            }
          }
        }
        typeMap = nextRound;
      }

      // FIXME: check this type lookup for NPEs (and also the ones in the validators)
      Element javaLang = environment.getElementUtils().getPackageElement("java.lang");

      for (TypeElement imp : importMap.values()) {
        if (imp.getModifiers().contains(Modifier.PUBLIC)
            && !javaLang.equals(imp.getEnclosingElement())) {
          imports.add(imp.getQualifiedName().toString());
        }
      }

      // Other imports that the generated class needs
      imports.add("java.util.Map");
      imports.add("java.util.HashMap");
      imports.add("javax.annotation.Generated");
      imports.add("org.robolectric.internal.ShadowProvider");
      imports.add("org.robolectric.shadow.api.Shadow");

      Helpers helpers = new Helpers(environment.getElementUtils(), environment.getTypeUtils());
      ReferentResolver referentResolver = new ReferentResolver() {
        @Override
        public String getReferentFor(TypeMirror typeMirror) {
          return findReferent.visit(typeMirror);
        }

        @Override
        public String getReferentFor(TypeElement type) {
          return referentMap.get(type);
        }
      };
      shadowTypes.values().forEach(shadowInfo -> shadowInfo.prepare(referentResolver, helpers));
      resetterMap.values().forEach(resetterInfo -> resetterInfo.prepare(referentResolver));
    }

    private Iterable<ShadowInfo> getResetterShadowTypes() {
      return Iterables.filter(shadowTypes.values(),
          shadowInfo -> resetterMap.containsKey(shadowInfo.getShadowName()));
    }

    public Iterable<ShadowInfo> getVisibleShadowTypes() {
      return Iterables.filter(shadowTypes.values(),
          ShadowInfo::isInAndroidSdk);
    }

    private void registerType(TypeElement type) {
      if (!importMap.containsKey(type)) {
        typeMap.put(type.getSimpleName().toString(), type);
        importMap.put(type, type);
        for (TypeParameterElement typeParam : type.getTypeParameters()) {
          for (TypeMirror bound : typeParam.getBounds()) {
            // FIXME: get rid of cast using a visitor
            TypeElement boundElement = typeElementVisitor
                .visit(environment.getTypeUtils().asElement(bound));
            registerType(boundElement);
          }
        }
      }
    }

    private TypeVisitor<String, Void> findReferent = new SimpleTypeVisitor6<String, Void>() {
      @Override
      public String visitDeclared(DeclaredType t, Void p) {
        return referentMap.get(t.asElement());
      }
    };
  }

  public Collection<ResetterInfo> getResetters() {
    return resetterMap.values();
  }

  public Set<String> getImports() {
    return imports;
  }

  public Collection<ShadowInfo> getAllShadowTypes() {
    return shadowTypes.values();
  }

  public Map<String, String> getExtraShadowTypes() {
    return extraShadowTypes;
  }

  public Iterable<ShadowInfo> getVisibleShadowTypes() {
    return Iterables.filter(shadowTypes.values(),
        ShadowInfo::isInAndroidSdk);
  }

  public Collection<String> getShadowedPackages() {
    Set<String> packages = new TreeSet<>();
    for (ShadowInfo shadowInfo : shadowTypes.values()) {
      String packageName = shadowInfo.getActualPackage();

      // org.robolectric.* should never be instrumented
      if (packageName.matches("org.robolectric(\\..*)?")) {
        continue;
      }

      packages.add("\"" + packageName + "\"");
    }
    return packages;
  }

  interface ReferentResolver {

    String getReferentFor(TypeMirror typeMirror);

    /**
     * Returns a plain string to be used in the generated source to identify the given type. The
     * returned string will have sufficient level of qualification in order to make the referent
     * unique for the source file.
     */
    String getReferentFor(TypeElement type);
  }

  public static class ShadowInfo {

    private final TypeElement shadowType;
    private final TypeElement actualType;

    private String paramDefStr;
    private String paramUseStr;
    private String actualTypeReferent;
    private String shadowTypeReferent;
    private String actualTypePackage;
    private String shadowBinaryName;

    ShadowInfo(TypeElement shadowType, TypeElement actualType) {
      this.shadowType = shadowType;
      this.actualType = actualType;
    }

    void prepare(ReferentResolver referentResolver, Helpers helpers) {
      int paramCount = 0;
      StringBuilder paramDef = new StringBuilder("<");
      StringBuilder paramUse = new StringBuilder("<");
      for (TypeParameterElement typeParam : actualType.getTypeParameters()) {
        if (paramCount > 0) {
          paramDef.append(',');
          paramUse.append(',');
        }
        boolean first = true;
        paramDef.append(typeParam);
        paramUse.append(typeParam);
        for (TypeMirror bound : helpers.getExplicitBounds(typeParam)) {
          if (first) {
            paramDef.append(" extends ");
            first = false;
          } else {
            paramDef.append(" & ");
          }
          paramDef.append(referentResolver.getReferentFor(bound));
        }
        paramCount++;
      }

      this.paramDefStr = "";
      this.paramUseStr = "";
      if (paramCount > 0) {
        paramDefStr = paramDef.append('>').toString();
        paramUseStr = paramUse.append('>').toString();
      }

      actualTypeReferent = referentResolver.getReferentFor(actualType);
      actualTypePackage = helpers.getPackageOf(actualType);
      shadowTypeReferent = referentResolver.getReferentFor(shadowType);
      shadowBinaryName = helpers.getBinaryName(shadowType);
    }

    public String getShadowName() {
      return shadowType.getQualifiedName().toString();
    }

    public String getShadowBinaryName() {
      return shadowBinaryName;
    }

    public String getActualName() {
      return actualType.getQualifiedName().toString();
    }

    public boolean isInAndroidSdk() {
      return shadowType.getAnnotation(Implements.class).isInAndroidSdk();
    }

    public String getParamDefStr() {
      return paramDefStr;
    }

    public boolean shadowIsDeprecated() {
      return shadowType.getAnnotation(Deprecated.class) != null;
    }

    public boolean actualIsPublic() {
      return actualType.getModifiers().contains(Modifier.PUBLIC);
    }

    public String getActualTypeWithParams() {
      return actualTypeReferent + paramUseStr;
    }

    public String getShadowTypeWithParams() {
      return shadowTypeReferent + paramUseStr;
    }

    public String getActualPackage() {
      return actualTypePackage;
    }
  }

  public static class ResetterInfo {

    private final TypeElement shadowType;
    private final ExecutableElement executableElement;
    private String shadowTypeReferent;

    public ResetterInfo(TypeElement shadowType, ExecutableElement executableElement) {
      this.shadowType = shadowType;
      this.executableElement = executableElement;
    }

    void prepare(ReferentResolver referentResolver) {
      shadowTypeReferent = referentResolver.getReferentFor(shadowType);
    }

    private Implements getImplementsAnnotation() {
      return shadowType.getAnnotation(Implements.class);
    }

    public String getMethodCall() {
      return shadowTypeReferent + "." + executableElement.getSimpleName() + "();";
    }

    public int getMinSdk() {
      return getImplementsAnnotation().minSdk();
    }

    public int getMaxSdk() {
      return getImplementsAnnotation().maxSdk();
    }
  }

}
