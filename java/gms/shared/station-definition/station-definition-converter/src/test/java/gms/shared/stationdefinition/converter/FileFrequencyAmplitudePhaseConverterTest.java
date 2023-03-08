package gms.shared.stationdefinition.converter;

import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;
import gms.shared.stationdefinition.coi.utils.Units;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileFrequencyAmplitudePhaseConverterTest {

  FileFrequencyAmplitudePhaseConverter fileFrequencyAmplitudePhaseConverter = new FileFrequencyAmplitudePhaseConverter();

  private static final File RESOURCE_DIR = new File(FileFrequencyAmplitudePhaseConverterTest.class.
    getClassLoader().getResource("FAP").getFile());

  private static final String FAPFILE1_LOC = RESOURCE_DIR.toString() + "/AAK_BHE_AF_2014255.fap";
  private static FrequencyAmplitudePhase fapOrig;

  @BeforeAll
  static void loadObjects() {
    double[] frequencies = {2.0E-4, 2.04668E-4, 2.09445E-4, 2.143335E-4, 2.193361E-4, 2.244555E-4, 2.296943E-4, 2.350554E-4, 2.405417E-4, 2.46156E-4, 2.519013E-4, 2.577807E-4, 2.637974E-4, 2.699545E-4, 2.762553E-4, 2.827031E-4, 2.893015E-4, 2.960538E-4, 3.029638E-4, 3.10035E-4};
    double[] amplitudes = {2.775677E-5, 2.974503E-5, 3.187562E-5, 3.415871E-5, 3.660521E-5, 3.922679E-5, 4.203595E-5, 4.504608E-5, 4.827154E-5, 5.172769E-5, 5.543098E-5, 5.939903E-5, 6.36507E-5, 6.82062E-5, 7.308716E-5, 7.831672E-5, 8.391966E-5, 8.992252E-5, 9.635367E-5, 1.032435E-4};
    double[] ampStdDev = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    double[] phase = {-1.786542, -1.791653, -1.796888, -1.802251, -1.807744, -1.813373, -1.81914, -1.825049, -1.831103, -1.837307, -1.843665, -1.850181, -1.856859, -1.863703, -1.870719, -1.877911, -1.885284, -1.892843, -1.900594, -1.908541};
    double[] phaseStdDev = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    fapOrig = FrequencyAmplitudePhase.builder()
      .setData(FrequencyAmplitudePhase.Data.builder()
        .setAmplitudeResponseUnits(Units.COUNTS_PER_NANOMETER)
        .setAmplitudeResponse(amplitudes)
        .setAmplitudeResponseStdDev(ampStdDev)
        .setPhaseResponse(phase)
        .setPhaseResponseStdDev(phaseStdDev)
        .setPhaseResponseUnits(Units.DEGREES)
        .setFrequencies(frequencies)
        .build())
      .setId(UUID.nameUUIDFromBytes(FAPFILE1_LOC.getBytes()))
      .build();
  }

  @Test
  void frequencyAmplitudePhaseConverterPass() {
    FrequencyAmplitudePhase fap = fileFrequencyAmplitudePhaseConverter.convert(
      "BHE", Path.of(FAPFILE1_LOC));
    assertEquals(fap, fapOrig);
  }

  @ParameterizedTest
  @MethodSource("createFAPConverterNullCheck")
  void testFAPConverter_nullChecks(Class<? extends Exception> exception,
    String fileName,
    String channelName) {
    assertThrows(exception,
      () -> fileFrequencyAmplitudePhaseConverter.convert(channelName, Path.of(fileName)));
  }

  private static Stream<Arguments> createFAPConverterNullCheck() {
    return Stream.of(
      Arguments.of(NullPointerException.class, null, "BHE"),
      Arguments.of(NullPointerException.class, FAPFILE1_LOC, null),
      Arguments.of(IllegalStateException.class, "", "BHE"),
      Arguments.of(IllegalStateException.class, FAPFILE1_LOC, ""),
      Arguments.of(IllegalStateException.class, "nonExistentPath", "BHE"),
      Arguments.of(IllegalStateException.class, FAPFILE1_LOC, "XXXX")
    );
  }
}
