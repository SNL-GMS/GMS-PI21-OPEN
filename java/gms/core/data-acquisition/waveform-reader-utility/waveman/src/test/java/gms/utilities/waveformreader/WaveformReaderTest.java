package gms.utilities.waveformreader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaveformReaderTest {

  @ParameterizedTest
  @MethodSource("readSamplesArguments")
  void readSamples_take_skip(FormatCode formatCode, String file) {
    try (InputStream is = this.getClass().getResourceAsStream(file)) {
      final double[] doubles = assertDoesNotThrow(
        () -> WaveformReader.readSamples(is, formatCode.getCode(), 10, 0));
      assertTrue(doubles.length > 0);
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  @ParameterizedTest
  @MethodSource("readSamplesArguments")
  void readSamples_take_offset_skip(FormatCode formatCode, String file) {
    try (InputStream is = this.getClass().getResourceAsStream(file)) {
      final double[] doubles = assertDoesNotThrow(
        () -> WaveformReader.readSamples(is, formatCode.getCode(), 10, 0, 0));
      assertTrue(doubles.length > 0);
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  @ParameterizedTest
  @MethodSource("readSamplesArguments")
  void readSamples(FormatCode formatCode, String file) {
    try (InputStream is = this.getClass().getResourceAsStream(file)) {
      final double[] doubles = assertDoesNotThrow(
        () -> WaveformReader.readSamples(is, formatCode.getCode()));
      assertTrue(doubles.length > 0);
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  @ParameterizedTest
  @MethodSource("readSamplesArguments_mismatchedFormat")
  void readSamples_take_skip_mismatchedFormat(FormatCode formatCode, String file) {
    try (InputStream is = this.getClass().getResourceAsStream(file)) {
      assertThrows(Exception.class, () -> WaveformReader.readSamples(is, formatCode.getCode()));
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  @ParameterizedTest
  @MethodSource("readSamplesArguments_mismatchedFormat")
  void readSamples_take_offset_skip_mismatchedFormat(FormatCode formatCode, String file) {
    try (InputStream is = this.getClass().getResourceAsStream(file)) {
      assertThrows(Exception.class, () -> WaveformReader.readSamples(is, formatCode.getCode()));
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  @ParameterizedTest
  @MethodSource("readSamplesArguments_mismatchedFormat")
  void readSamples_mismatchedFormat(FormatCode formatCode, String file) {
    try (InputStream is = this.getClass().getResourceAsStream(file)) {
      assertThrows(Exception.class, () -> WaveformReader.readSamples(is, formatCode.getCode()));
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  @ParameterizedTest
  @MethodSource("readerForArguments")
  <T extends WaveformReaderInterface> void readerFor(FormatCode formatCode, T reader) {
    WaveformReaderInterface result = WaveformReader.readerFor(formatCode.getCode());

    assertNotNull(result);
    assertTrue(reader.getClass().isInstance(result), String
      .format("Expected an instance of %s, but got and instance of %s", reader.getClass(),
        result.getClass()));
  }

  @ParameterizedTest
  @MethodSource("readerForArguments_errors")
  <T extends WaveformReaderInterface> void readerFor_errors(Optional<String> formatCode) {
    final String fc = formatCode.orElse(null);
    assertThrows(IllegalArgumentException.class, () -> WaveformReader.readerFor(fc));
  }

  private static Stream<Arguments> readerForArguments_errors() {
    return Stream.of(
      Arguments.arguments(Optional.<String>empty()),
      Arguments.arguments(Optional.of("")),
      Arguments.arguments(Optional.of("code"))
    );
  }

  private static Stream<Arguments> readerForArguments() {
    return Stream.of(
      Arguments.arguments(FormatCode.F4, new Float4FormatWaveformReader()),
      Arguments.arguments(FormatCode.S3, new Sun3FormatWaveformReader()),
      Arguments.arguments(FormatCode.S4, new Sun4FormatWaveformReader()),
      Arguments.arguments(FormatCode.I4, new I4FormatWaveformReader()),
      Arguments.arguments(FormatCode.CD, new CanadianCompressedWaveformReader()),
      Arguments.arguments(FormatCode.CC, new CanadianCompressedWaveformReader()),
      Arguments.arguments(FormatCode.E1, new E1FormatWaveformReader()),
      Arguments.arguments(FormatCode.CM6, new Cm6WaveformReader()),
      Arguments.arguments(FormatCode.T4, new SunSinglePrecisionReal()),
      Arguments.arguments(FormatCode.S2, new Sun2FormatWaveformReader())
    );
  }


  private static Stream<Arguments> readSamplesArguments() {
    return Stream.of(
      Arguments.arguments(FormatCode.S4, "/css/WFS4/I22FR.s4.w"),
      Arguments.arguments(FormatCode.S3, "/css/WFS4/I22FR.s3.w"),
      Arguments.arguments(FormatCode.S2, "/css/WFS4/S2Test.w"),
      Arguments.arguments(FormatCode.F4, "/css/WFS4/F4Test.w"),
      Arguments.arguments(FormatCode.E1, "/css/WFS4/I22FR.e1.w"),
      Arguments.arguments(FormatCode.T4, "/css/WFS4/t4.w"),
      Arguments.arguments(FormatCode.CC, "/css/WFS4/cc.w"),
      Arguments.arguments(FormatCode.CD, "/css/WFS4/cc.w"),
      Arguments.arguments(FormatCode.I4, "/css/WFS4/i4.w")
    );
  }

  private static Stream<Arguments> readSamplesArguments_mismatchedFormat() {
    return Stream.of(
      Arguments.arguments(FormatCode.F4, "/css/WFS4/I22FR.s3.w")
    );
  }

}