package org.robolectric.shadows;

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_AUDIO_OPUS;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AV1;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_VP9;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowMediaCodecList}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class ShadowMediaCodecListTest {

  private static final String AAC_DECODER_NAME = "shadow.test.decoder.aac";
  private static final String OPUS_DECODER_NAME = "shadow.test.decoder.opus";
  private static final String AVC_DECODER_NAME = "shadow.test.decoder.avc";
  private static final String VP9_DECODER_NAME = "shadow.test.decoder.vp9";
  private static final String AAC_ENCODER_NAME = "shadow.test.encoder.aac";
  private static final String OPUS_ENCODER_NAME = "shadow.test.encoder.opus";
  private static final String AVC_ENCODER_NAME = "shadow.test.encoder.avc";
  private static final String VP9_ENCODER_NAME = "shadow.test.encoder.vp9";

  @Before
  public void setUp() throws Exception {
    ShadowMediaCodecList.addDecoder(AAC_DECODER_NAME, MIMETYPE_AUDIO_AAC);
    ShadowMediaCodecList.addDecoder(OPUS_DECODER_NAME, MIMETYPE_AUDIO_OPUS);
    ShadowMediaCodecList.addDecoder(AVC_DECODER_NAME, MIMETYPE_VIDEO_AVC);
    ShadowMediaCodecList.addDecoder(VP9_DECODER_NAME, MIMETYPE_VIDEO_VP9);
    ShadowMediaCodecList.addEncoder(AAC_ENCODER_NAME, MIMETYPE_AUDIO_AAC);
    ShadowMediaCodecList.addEncoder(OPUS_ENCODER_NAME, MIMETYPE_AUDIO_OPUS);
    ShadowMediaCodecList.addEncoder(AVC_ENCODER_NAME, MIMETYPE_VIDEO_AVC);
    ShadowMediaCodecList.addEncoder(VP9_ENCODER_NAME, MIMETYPE_VIDEO_VP9);
  }

  @Test
  public void getCodecInfosLength() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);

    assertThat(mediaCodecList.getCodecInfos()).hasLength(8);
  }

  @Test
  public void aacDecoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo aacCodecInfo = mediaCodecList.getCodecInfos()[0];
    CodecCapabilities aacCapabilities = aacCodecInfo.getCapabilitiesForType(MIMETYPE_AUDIO_AAC);

    assertThat(aacCodecInfo.getName()).isEqualTo(AAC_DECODER_NAME);
    assertThat(aacCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_AUDIO_AAC);
    assertThat(aacCodecInfo.isEncoder()).isFalse();
    assertThat(aacCapabilities.getAudioCapabilities()).isNotNull();
    assertThat(aacCapabilities.profileLevels).hasLength(7);
  }

  @Test
  public void opusDecoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo opusCodecInfo = mediaCodecList.getCodecInfos()[1];
    CodecCapabilities opusCapabilities = opusCodecInfo.getCapabilitiesForType(MIMETYPE_AUDIO_OPUS);

    assertThat(opusCodecInfo.getName()).isEqualTo(OPUS_DECODER_NAME);
    assertThat(opusCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_AUDIO_OPUS);
    assertThat(opusCodecInfo.isEncoder()).isFalse();
    assertThat(opusCapabilities.getAudioCapabilities()).isNotNull();
    assertThat(opusCapabilities.profileLevels).hasLength(0);
  }

  @Test
  public void avcDecoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo avcCodecInfo = mediaCodecList.getCodecInfos()[2];
    CodecCapabilities avcCapabilities = avcCodecInfo.getCapabilitiesForType(MIMETYPE_VIDEO_AVC);

    assertThat(avcCodecInfo.getName()).isEqualTo(AVC_DECODER_NAME);
    assertThat(avcCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_VIDEO_AVC);
    assertThat(avcCodecInfo.isEncoder()).isFalse();
    assertThat(avcCapabilities.getVideoCapabilities()).isNotNull();
    assertThat(avcCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_AdaptivePlayback))
        .isTrue();
    assertThat(avcCapabilities.profileLevels).hasLength(5);
    assertThat(avcCapabilities.colorFormats).hasLength(2);
  }

  @Test
  public void vp9DecoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo vp9CodecInfo = mediaCodecList.getCodecInfos()[3];
    CodecCapabilities vp9Capabilities = vp9CodecInfo.getCapabilitiesForType(MIMETYPE_VIDEO_VP9);

    assertThat(vp9CodecInfo.getName()).isEqualTo(VP9_DECODER_NAME);
    assertThat(vp9CodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_VIDEO_VP9);
    assertThat(vp9CodecInfo.isEncoder()).isFalse();
    assertThat(vp9Capabilities.getVideoCapabilities()).isNotNull();
    assertThat(vp9Capabilities.isFeatureSupported(CodecCapabilities.FEATURE_AdaptivePlayback))
        .isTrue();
    assertThat(vp9Capabilities.profileLevels).hasLength(3);
    assertThat(vp9Capabilities.colorFormats).hasLength(2);
  }

  @Test
  public void aacEncoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo aacCodecInfo = mediaCodecList.getCodecInfos()[4];
    CodecCapabilities aacCapabilities = aacCodecInfo.getCapabilitiesForType(MIMETYPE_AUDIO_AAC);

    assertThat(aacCodecInfo.getName()).isEqualTo(AAC_ENCODER_NAME);
    assertThat(aacCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_AUDIO_AAC);
    assertThat(aacCodecInfo.isEncoder()).isTrue();
    assertThat(aacCapabilities.getAudioCapabilities()).isNotNull();
    assertThat(aacCapabilities.profileLevels).hasLength(7);
  }

  @Test
  public void opusEncoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo opusCodecInfo = mediaCodecList.getCodecInfos()[5];
    CodecCapabilities opusCapabilities = opusCodecInfo.getCapabilitiesForType(MIMETYPE_AUDIO_OPUS);

    assertThat(opusCodecInfo.getName()).isEqualTo(OPUS_ENCODER_NAME);
    assertThat(opusCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_AUDIO_OPUS);
    assertThat(opusCodecInfo.isEncoder()).isTrue();
    assertThat(opusCapabilities.getAudioCapabilities()).isNotNull();
    assertThat(opusCapabilities.profileLevels).hasLength(0);
  }

  @Test
  public void avcEncoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo avcCodecInfo = mediaCodecList.getCodecInfos()[6];
    CodecCapabilities avcCapabilities = avcCodecInfo.getCapabilitiesForType(MIMETYPE_VIDEO_AVC);

    assertThat(avcCodecInfo.getName()).isEqualTo(AVC_ENCODER_NAME);
    assertThat(avcCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_VIDEO_AVC);
    assertThat(avcCodecInfo.isEncoder()).isTrue();
    assertThat(avcCapabilities.getVideoCapabilities()).isNotNull();
    assertThat(avcCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_AdaptivePlayback))
        .isTrue();
    assertThat(avcCapabilities.profileLevels).hasLength(5);
    assertThat(avcCapabilities.colorFormats).hasLength(2);
  }

  @Test
  public void vp9EncoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo vp9CodecInfo = mediaCodecList.getCodecInfos()[7];
    CodecCapabilities vp9Capabilities = vp9CodecInfo.getCapabilitiesForType(MIMETYPE_VIDEO_VP9);

    assertThat(vp9CodecInfo.getName()).isEqualTo(VP9_ENCODER_NAME);
    assertThat(vp9CodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_VIDEO_VP9);
    assertThat(vp9CodecInfo.isEncoder()).isTrue();
    assertThat(vp9Capabilities.getVideoCapabilities()).isNotNull();
    assertThat(vp9Capabilities.isFeatureSupported(CodecCapabilities.FEATURE_AdaptivePlayback))
        .isTrue();
    assertThat(vp9Capabilities.profileLevels).hasLength(3);
    assertThat(vp9Capabilities.colorFormats).hasLength(2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void addNonSupportedMimeType() {
    ShadowMediaCodecList.addDecoder(AAC_DECODER_NAME, MIMETYPE_VIDEO_AV1);
  }
}
