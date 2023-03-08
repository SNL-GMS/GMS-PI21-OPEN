package gms.shared.frameworks.osd.coi.fk;

import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.waveforms.FkAttributes;
import gms.shared.frameworks.osd.coi.waveforms.FkSpectra;
import gms.shared.frameworks.osd.coi.waveforms.FkSpectrum;
import gms.shared.frameworks.osd.coi.waveforms.WaveformTestFixtures;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.CHANNEL;

public class FkTestFixtures {

  public static final PhaseType phaseType = PhaseType.P;
  public static final double SLOW_START_X = 5;
  public static final double SLOW_DELTA_X = 10;
  public static final int SLOW_COUNT_X = 25;
  public static final double SLOW_START_Y = 5;
  public static final double SLOW_DELTA_Y = 10;
  public static final int SLOW_COUNT_Y = 25;
  public static final Duration sampleTimeStep = Duration.ofMinutes(1);
  public static final double SAMPLE_PERIOD = sampleTimeStep.toNanos() / 1.0e9;
  public static final double SAMPLE_RATE = (1.0 / SAMPLE_PERIOD);
  public static final FkSpectrum fkSpectrum = buildFkSpectrum(10, 1, 1);
  //collection of fkspectra sequential in time based on the sampleTimeStep
  public static final FkSpectra fkSpectra = buildFkSpectra(WaveformTestFixtures.SEGMENT_START,
    fkSpectrum);
  public static final FkSpectrum fkSpectrum2 = buildFkSpectrum(11, 2, 2);
  public static final FkSpectra fkSpectra2 = buildFkSpectra(
    WaveformTestFixtures.SEGMENT_START.plus(sampleTimeStep), fkSpectrum2);
  public static final FkSpectrum fkSpectrum3 = buildFkSpectrum(12, 3, 3);
  public static final FkSpectra fkSpectra3 = buildFkSpectra(
    WaveformTestFixtures.SEGMENT_START.plus(sampleTimeStep.multipliedBy(2)),
    fkSpectrum3);
  public static final FkSpectrum fkSpectrum4 = buildFkSpectrum(13, 4, 4);
  public static final FkSpectra fkSpectra4 = buildFkSpectra(
    WaveformTestFixtures.SEGMENT_START.plus(sampleTimeStep.multipliedBy(3)),
    fkSpectrum4);
  public static final FkSpectrum fkSpectrum5 = buildFkSpectrum(14, 5, 1);
  public static final FkSpectra fkSpectra5 = buildFkSpectra(
    WaveformTestFixtures.SEGMENT_START.plus(sampleTimeStep.multipliedBy(4)),
    fkSpectrum5);

  public static FkSpectra buildFkSpectra(Instant start, FkSpectrum... spectrums) {
    return FkSpectra.builder()
      .setStartTime(start)
      .setSampleRate(SAMPLE_RATE)
      .withValues(List.of(spectrums))
      .setMetadata(fkMetadata())
      .build();
  }

  //TODO: fstat
  public static FkSpectrum buildFkSpectrum(int icoeff, int jcoeff, int quality) {
    return FkSpectrum.from(fkPower(icoeff, jcoeff), fkPower(icoeff + 1d, jcoeff + 1d), quality,
      List.of(fkAttributes()));
  }

  public static FkAttributes fkAttributes() {
    return FkAttributes.from(10, 10, 0.1, 0.2,
      0.5);
  }

  /**
   * Generates fk power data based off of the input coefficients. Power = (i * icoeff) + (j +
   * jcoeff)
   */
  public static double[][] fkPower(double icoeff, double jcoeff) {
    double[][] fkData = new double[SLOW_COUNT_Y][SLOW_COUNT_X];

    for (int i = 0; i < SLOW_COUNT_Y; i++) {
      for (int j = 0; j < SLOW_COUNT_X; j++) {
        if (j == 0) {
          fkData[i][j] = 0.0;
        } else {
          fkData[i][j] = (i * icoeff) + (j + jcoeff);
        }
      }
    }

    return fkData;
  }

  public static FkSpectra.Metadata fkMetadata() {
    return FkSpectra.Metadata.builder()
      .setPhaseType(phaseType)
      .setSlowStartX(SLOW_START_X)
      .setSlowDeltaX(SLOW_DELTA_X)
      .setSlowStartY(SLOW_START_Y)
      .setSlowDeltaY(SLOW_DELTA_Y)
      .build();
  }

  public static List<ChannelSegment<FkSpectra>> createFkChannelSegments(
    FkSpectra... fks) {
    return Arrays.stream(fks).map(fk ->
      ChannelSegment.create(
        CHANNEL,
        "Test",
        ChannelSegment.Type.FK_BEAM,
        List.of(fk))).collect(Collectors.toList());
  }

  private FkTestFixtures() {
    //private constructor for static test fixtures factory
  }
}
