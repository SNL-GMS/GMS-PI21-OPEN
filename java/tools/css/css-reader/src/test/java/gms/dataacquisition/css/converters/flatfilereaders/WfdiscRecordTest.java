package gms.dataacquisition.css.converters.flatfilereaders;

import gms.dataacquisition.cssreader.data.WfdiscRecord;
import gms.dataacquisition.cssreader.data.WfdiscRecord32;
import gms.dataacquisition.cssreader.flatfilereaders.FlatFileWfdiscReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests WfdiscRecord, which is a class that both represents and parses CSS 3.0 WfdiscRecord files.
 */
class WfdiscRecordTest {

  private static WfdiscRecord32 wfdisc;
  private final double COMPARISON_DELTA = 0.000001;
  private static final String STS2 = "STS-2";
  private static final String NAN = "not a number";
  private static final String PATH = "src/test/resources/css/WFS4/";
  private static final String TIME = "20150408 12:00:00";
  private static final String WFID = "12345";

  @BeforeEach
  void resetWfdisc() {
    wfdisc = new WfdiscRecord32();
    wfdisc.setSta("DAVOX");
    wfdisc.setChan("HHN");
    wfdisc.setTime("1274313600");
    wfdisc.setWfid("64583333");
    wfdisc.setChan("4247");
    wfdisc.setJdate("2010140");
    wfdisc.setEndtime("1274313732.99167");
    wfdisc.setNsamp("15960");
    wfdisc.setSamprate("120");
    wfdisc.setCalib("0.253");
    wfdisc.setCalper("1");
    wfdisc.setInstype(STS2);
    wfdisc.setSegtype("o");
    wfdisc.setDatatype("s4");
    wfdisc.setClip("-");
    wfdisc.setDir(PATH);
    wfdisc.setDfile("DAVOX0.w");
    wfdisc.setFoff("3897184");
    wfdisc.setCommid("-1");
    wfdisc.setLddate(TIME);
  }

  // tests on 'sta'
  @Test
  void testSetNullSta() {
    wfdisc.setSta(null);
    assertThrows(NullPointerException.class, () -> wfdisc.validate());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'chan'
  @Test
  void testSetNullChan() {
    wfdisc.setChan(null);
    assertThrows(NullPointerException.class, () -> wfdisc.validate());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'time'
  @Test
  void testSetNullTime() {
    //SetTime is throwing an exception on setting it to null, so validate is never called, so removed.
    assertThrows(NullPointerException.class, () -> wfdisc.setTime(null));
  }

  @Test
  void testSetTimeWithBadString() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setTime(NAN));
  }

  @Test
  void testSetTime() {
    wfdisc.setTime("1274317219.75");  // epoch seconds with millis, just as in flatfilereaders files
    assertNotNull(wfdisc.getTime());
    Instant expected = Instant.ofEpochMilli((long) (1274317219.75 * 1000L));
    assertEquals(expected, wfdisc.getTime());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'wfid'
  @Test
  void testSetWfIdNull() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setWfid(null));
  }

  @Test
  void testSetWfIdBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setWfid(NAN));
  }

  @Test
  void testSetWfId() {
    wfdisc.setWfid(WFID);
    assertEquals(12345, wfdisc.getWfid());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'chanid'
  @Test
  void testSetChanIdNull() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setChanid(null));
  }

  @Test
  void testSetChanIdBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setChanid(NAN));
  }

  @Test
  void testSetChanId() {
    wfdisc.setChanid(WFID);
    assertEquals(12345, wfdisc.getChanid());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'jdate'
  @Test
  void testSetJdateNull() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setJdate(null));
  }

  @Test
  void testSetJdateBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setJdate(NAN));
  }

  @Test
  void testSetJdateTooLow() {
    wfdisc.setJdate("1500001"); // ah, the first day of 1500 AD.  What a glorious time to be alive.
    assertThrows(IllegalArgumentException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetJdateTooHigh() {
    wfdisc.setJdate("2900001"); // If this code is still running in 2900 AD, that's impressive.
    assertThrows(IllegalArgumentException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetJdate() {
    wfdisc.setJdate("2012123");
    assertEquals(2012123, wfdisc.getJdate());
  }
  ////////////////////////////////////////////////////////////////////////
  // tests on 'endtime'

  @Test
  void testSetNullEndtime() {
    assertThrows(NullPointerException.class, () -> wfdisc.setEndtime(null));
  }

  @Test
  void testSetEndtimeWithBadString() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setEndtime(NAN));
  }

  @Test
  void testSetEndtime() {
    wfdisc.setEndtime("1274317219.75");  // epoch seconds with millis, just as in wfdiscreaders files
    assertNotNull(wfdisc.getEndtime());
    Instant expected = Instant.ofEpochMilli((long) (1274317219.75 * 1000L));
    assertEquals(expected, wfdisc.getEndtime());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'nsamp'
  @Test
  void testSetNsampNull() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setNsamp(null));
  }

  @Test
  void testSetNsampBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setNsamp(NAN));
  }

  @Test
  void testSetNsampNegative() {
    wfdisc.setNsamp("-123");
    assertThrows(IllegalArgumentException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetNsamp() {
    wfdisc.setNsamp("5000");
    assertEquals(5000, wfdisc.getNsamp());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'samprate'
  @Test
  void testSetSamprateNull() {
    assertThrows(NullPointerException.class, () -> wfdisc.setSamprate(null));
  }

  @Test
  void testSetSamprateBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setSamprate(NAN));
  }

  @Test
  void testSetSamprateNegative() {
    wfdisc.setSamprate("-123");
    assertThrows(IllegalArgumentException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetSamprate() {
    wfdisc.setSamprate("40.0");
    assertEquals(40.0, wfdisc.getSamprate());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'calib'
  @Test
  void testSetCalibNull() {
    assertThrows(NullPointerException.class, () -> wfdisc.setCalib(null));
  }

  @Test
  void testSetCalibBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setCalib(NAN));
  }

  @Test
  void testSetCalibNegative() {
    wfdisc.setCalib("-123");
    assertThrows(IllegalArgumentException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetCalib() {
    wfdisc.setCalib("1.0");
    assertEquals(1.0, wfdisc.getCalib());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'calper'
  @Test
  void testSetCalperNull() {
    assertThrows(NullPointerException.class, () -> wfdisc.setCalper(null));
  }

  @Test
  void testSetCalperNegative() {
    wfdisc.setCalper("-123");
    assertThrows(IllegalArgumentException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetCalperBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setCalper(NAN));
  }

  @Test
  void testSetCalper() {
    wfdisc.setCalper("1.0");
    assertEquals(1.0, wfdisc.getCalper());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'instype'
  @Test
  void testSetNullInstype() {
    wfdisc.setInstype(null);
    assertThrows(NullPointerException.class, () -> wfdisc.validate());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'segtype'
  @Test
  void testSetSegtypeNull() {
    wfdisc.setSegtype(null);
    assertThrows(NullPointerException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetSegtype() {
    String seg = "o";
    wfdisc.setSegtype(seg);
    assertEquals(seg, wfdisc.getSegtype());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'datatype'
  @Test
  void testSetDatatypeNull() {
    wfdisc.setDatatype(null);
    assertThrows(NullPointerException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetDatatype() {
    String dt = "s4";
    wfdisc.setDatatype(dt);  // format code from CSS 3.0
    assertEquals(dt, wfdisc.getDatatype());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'clip'
  @Test
  void testSetClip() {
    wfdisc.setClip("-");
    assertFalse(wfdisc.getClip());

    wfdisc.setClip("some random string that isn't just 'c'");
    assertFalse(wfdisc.getClip());

    wfdisc.setClip("c");
    assertTrue(wfdisc.getClip());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'dir'
  @Test
  void testSetNullDir() {
    wfdisc.setDir(null);
    assertThrows(NullPointerException.class, () -> wfdisc.validate());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'dfile'
  @Test
  void testSetNullDfile() {
    wfdisc.setDfile(null);
    assertThrows(NullPointerException.class, () -> wfdisc.validate());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'foff'
  @Test
  void testSetFoffNull() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setFoff(null));
  }

  @Test
  void testSetFoffBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setFoff(NAN));
  }

  @Test
  void testSetFoffNegative() {
    wfdisc.setFoff("-123");
    assertThrows(IllegalArgumentException.class, () -> wfdisc.validate());
  }

  @Test
  void testSetFoff() {
    wfdisc.setFoff(WFID);
    assertEquals(12345, wfdisc.getFoff());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'commid'
  @Test
  void testSetCommidNull() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setCommid(null));
  }

  @Test
  void testSetCommidBad() {
    assertThrows(NumberFormatException.class, () -> wfdisc.setCommid(NAN));
  }

  @Test
  void testSetCommid() {
    wfdisc.setCommid(WFID);
    assertEquals(12345, wfdisc.getCommid());
  }

  ////////////////////////////////////////////////////////////////////////
  // tests on 'lddate'
  @Test
  void testSetNullLddate() {
    wfdisc.setLddate(null);
    assertThrows(NullPointerException.class, () -> wfdisc.validate());
  }

  // TODO: if lddate gets read into a Date or something in the future,
  // add a test verifying this works.
  ////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////
  // tests reading flatfilereaders against known values
  @Test
  void testWfdiscReadAll() throws IOException {
    FlatFileWfdiscReader reader = new FlatFileWfdiscReader();
    List<WfdiscRecord> wfdiscs = reader.read("src/test/resources/css/WFS4/wfdisc_gms_s4.txt");
    assertEquals(76, wfdiscs.size());

    WfdiscRecord wfdisc = wfdiscs.get(1);
    assertEquals("DAVOX", wfdisc.getSta());
    assertEquals("HHE", wfdisc.getChan());
    assertEquals(Instant.ofEpochMilli(1274317199108L), wfdisc.getTime());
    assertEquals(64583325, wfdisc.getWfid());
    assertEquals(4248, wfdisc.getChanid());
    assertEquals(2010140, wfdisc.getJdate());
    assertEquals(Instant.ofEpochMilli(1274317201991L), wfdisc.getEndtime());
    assertEquals(347, wfdisc.getNsamp());
    assertEquals(119.98617, wfdisc.getSamprate(), this.COMPARISON_DELTA);
    assertEquals(0.253, wfdisc.getCalib(), this.COMPARISON_DELTA);
    assertEquals(1, wfdisc.getCalper(), this.COMPARISON_DELTA);
    assertEquals(STS2, wfdisc.getInstype());
    assertEquals("o", wfdisc.getSegtype());
    assertEquals("s4", wfdisc.getDatatype());
    assertFalse(wfdisc.getClip());
    assertEquals(PATH, wfdisc.getDir());
    assertEquals("DAVOX0.w", wfdisc.getDfile());
    assertEquals(63840, wfdisc.getFoff());
    assertEquals(-1, wfdisc.getCommid());
    assertEquals(TIME, wfdisc.getLddate());
  }

  @Test
  void testWfdiscReadFilter() throws Exception {
    ArrayList<String> stations = new ArrayList<>(1);
    stations.add("MLR");

    ArrayList<String> channels = new ArrayList<>(1);
    channels.add("BHZ");

    FlatFileWfdiscReader reader = new FlatFileWfdiscReader(stations, channels, null, null);
    List<WfdiscRecord> wfdiscs = reader.read("src/test/resources/css/WFS4/wfdisc_gms_s4.txt");
    assertEquals(3, wfdiscs.size());

    WfdiscRecord wfdisc = wfdiscs.get(1);
    assertEquals("MLR", wfdisc.getSta());
    assertEquals("BHZ", wfdisc.getChan());
    assertEquals(Instant.ofEpochMilli(1274317190019L), wfdisc.getTime());
    assertEquals(64587196, wfdisc.getWfid());
    assertEquals(4162, wfdisc.getChanid());
    assertEquals(2010140, wfdisc.getJdate());
    assertEquals(Instant.ofEpochMilli(1274317209994L), wfdisc.getEndtime());
    assertEquals(800, wfdisc.getNsamp());
    assertEquals(40, wfdisc.getSamprate(), this.COMPARISON_DELTA);
    assertEquals(0.0633, wfdisc.getCalib(), this.COMPARISON_DELTA);
    assertEquals(1, wfdisc.getCalper(), this.COMPARISON_DELTA);
    assertEquals(STS2, wfdisc.getInstype());
    assertEquals("o", wfdisc.getSegtype());
    assertEquals("s4", wfdisc.getDatatype());
    assertFalse(wfdisc.getClip());
    assertEquals(PATH, wfdisc.getDir());
    assertEquals("MLR0.w", wfdisc.getDfile());
    assertEquals(2724800, wfdisc.getFoff());
    assertEquals(-1, wfdisc.getCommid());
    assertEquals(TIME, wfdisc.getLddate());
  }
}