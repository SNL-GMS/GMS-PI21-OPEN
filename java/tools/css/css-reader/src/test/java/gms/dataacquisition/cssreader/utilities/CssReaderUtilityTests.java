package gms.dataacquisition.cssreader.utilities;

import gms.shared.frameworks.osd.coi.event.MagnitudeModel;
import gms.shared.frameworks.osd.coi.event.MagnitudeType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CssReaderUtilityTests {


  @Test
  void testToInstant() {
    assertThrows(NullPointerException.class, () -> CssReaderUtility.toInstant(null));
    assertThrows(IllegalStateException.class, () -> CssReaderUtility.toInstant(""));
    assertThrows(IllegalStateException.class, () -> CssReaderUtility.toInstant("   "));
    assertEquals(Optional.of(Instant.ofEpochMilli(-1000L)), CssReaderUtility.toInstant("-1"));
    assertEquals(Optional.of(Instant.ofEpochSecond(0)), CssReaderUtility.toInstant("0"));
    assertEquals(Optional.empty(), CssReaderUtility.toInstant("9999999999.99900"));
  }

  @Test
  void testParseDate() {
    assertThrows(NullPointerException.class, () -> CssReaderUtility.parseDate(null));
    assertThrows(IllegalStateException.class, () -> CssReaderUtility.parseDate(""));
    assertThrows(IllegalStateException.class, () -> CssReaderUtility.parseDate("   "));
    assertEquals(Optional.empty(), CssReaderUtility.parseDate("text"));
    assertEquals(Optional.empty(), CssReaderUtility.parseDate("19991231T23:59:59"));
    assertEquals(Optional.of(ZonedDateTime.ofInstant(Instant.ofEpochSecond(946684799),
      ZoneId.of("UTC")).toInstant()), CssReaderUtility.parseDate("19991231 23:59:59"));
  }

  @Test
  void testJdToInstant() {
    assertThrows(NullPointerException.class, () -> CssReaderUtility.jdToInstant(null));
    assertThrows(IllegalStateException.class, () -> CssReaderUtility.jdToInstant(""));
    assertThrows(IllegalStateException.class, () -> CssReaderUtility.jdToInstant("       "));
    assertThrows(IllegalArgumentException.class, () -> CssReaderUtility.jdToInstant("-1"));
    assertEquals(Optional.empty(), CssReaderUtility.jdToInstant("2286324"));
    assertEquals(Optional.of(Instant.ofEpochMilli(946684800000L)), CssReaderUtility.jdToInstant("2000001"));
  }

  @Test
  void testMagnitudeType() {
    assertEquals(MagnitudeType.UNKNOWN, CssReaderUtility.getMagnitudeType(null));
    assertEquals(MagnitudeType.UNKNOWN, CssReaderUtility.getMagnitudeType(""));
    assertEquals(MagnitudeType.UNKNOWN, CssReaderUtility.getMagnitudeType("   "));
    assertEquals(MagnitudeType.UNKNOWN, CssReaderUtility.getMagnitudeType("NA"));
    assertEquals(MagnitudeType.MB, CssReaderUtility.getMagnitudeType("mb_ave"));
    assertEquals(MagnitudeType.MBMLE, CssReaderUtility.getMagnitudeType("mb_mle"));
    assertEquals(MagnitudeType.MB1, CssReaderUtility.getMagnitudeType("mb1"));
    assertEquals(MagnitudeType.MB1MLE, CssReaderUtility.getMagnitudeType("mb1mle"));
    assertEquals(MagnitudeType.MB, CssReaderUtility.getMagnitudeType("mb_tmp"));
    assertEquals(MagnitudeType.ML, CssReaderUtility.getMagnitudeType("mlppn"));
    assertEquals(MagnitudeType.MS, CssReaderUtility.getMagnitudeType("ms_ave"));
    assertEquals(MagnitudeType.MSMLE, CssReaderUtility.getMagnitudeType("ms_mle"));
    assertEquals(MagnitudeType.MS1, CssReaderUtility.getMagnitudeType("ms1"));
    assertEquals(MagnitudeType.MS1MLE, CssReaderUtility.getMagnitudeType("ms1mle"));
  }

  @Test
  void testMagnitudeModel() {
    assertEquals(MagnitudeModel.UNKNOWN, CssReaderUtility.getMagnitudeModel(null));
    assertEquals(MagnitudeModel.UNKNOWN, CssReaderUtility.getMagnitudeModel(""));
    assertEquals(MagnitudeModel.UNKNOWN, CssReaderUtility.getMagnitudeModel("   "));
    assertEquals(MagnitudeModel.UNKNOWN, CssReaderUtility.getMagnitudeModel("NA"));
    assertEquals(MagnitudeModel.VEITH_CLAWSON, CssReaderUtility.getMagnitudeModel("qfvc1"));
    assertEquals(MagnitudeModel.REZAPOUR_PEARCE, CssReaderUtility.getMagnitudeModel("rez_pearce"));

  }


}
