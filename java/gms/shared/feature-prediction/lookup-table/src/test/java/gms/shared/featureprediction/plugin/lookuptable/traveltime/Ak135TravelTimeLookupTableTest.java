package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import com.google.common.primitives.ImmutableDoubleArray;
import gms.shared.featureprediction.utilities.data.EarthModelType;
import gms.shared.featureprediction.utilities.view.Immutable2dArray;
import gms.shared.featureprediction.utilities.view.TravelTimeLookupView;
import gms.shared.featureprediction.utilities.view.TravelTimeLookupViewTransformer;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.filestore.FileDescriptor;
import gms.shared.utilities.filestore.FileStore;
import java.time.Duration;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class Ak135TravelTimeLookupTableTest {

  @Mock
  static FileStore mockedFileStore;
  @Mock
  static Ak135TravelTimeLookupTableConfiguration mockedConfiguration;

  static private final TravelTimeLookupTableDefinition definition = TravelTimeLookupTableDefinition.builder()
    .setFileDescriptor(FileDescriptor.create("fake-bucket-name", "fake-key-name"))
    .build();
  static private final HashMap<FileDescriptor, TravelTimeLookupView> viewMap = new HashMap<>(2);

  private Ak135TravelTimeLookupTable lookupTable;

  @BeforeAll
  static void initialize() {

    TravelTimeLookupView viewP = TravelTimeLookupView.builder()
      .setModel(EarthModelType.AK135)
      .setPhase(PhaseType.P)
      .setRawPhaseString("P")
      .setDepthUnits(Units.KILOMETERS)
      .setDistanceUnits(Units.DEGREES)
      .setTravelTimeUnits(Units.SECONDS)
      .setDepths(new double[]{0.0, 700.0})
      .setDistances(new double[]{0.0, 180.0})
      .setTravelTimes(new Duration[][]
        {
          {
            Duration.parse("PT0.0S"),
            Duration.parse("PT1212.5273S")
          },
          {
            Duration.parse("PT79.6958S"),
            Duration.parse("PT1132.8315S")
          }
        })
      .setModelingErrorDepths(new double[]{0.0, 200.0})
      .setModelingErrorDistances(new double[]{0.0, 180.0})
      .setModelingErrors(new Duration[][]
        {
          {
            Duration.parse("PT0.1S"),
            Duration.parse("PT1.8S")
          },
          {
            Duration.parse("PT0.7S"),
            Duration.parse("PT1.5S")
          }
        }
      )
      .build();

    TravelTimeLookupView viewS = TravelTimeLookupView.builder()
      .setModel(EarthModelType.AK135)
      .setPhase(PhaseType.S)
      .setRawPhaseString("S")
      .setDepthUnits(Units.KILOMETERS)
      .setDistanceUnits(Units.DEGREES)
      .setTravelTimeUnits(Units.SECONDS)
      .setDepths(new double[]{0.0, 700.0})
      .setDistances(new double[]{0.0, 180.0})
      .setTravelTimes(new Duration[][]
        {
          {
            Duration.parse("PT0.0S"),
            Duration.parse("PT1636.6226S")
          },
          {
            Duration.parse("PT144.6422S"),
            Duration.parse("PT1491.9803S")
          }
        })
      .setModelingErrorDepths(new double[]{})
      .setModelingErrorDistances(new double[]{0.0, 180.0})
      .setModelingErrors(new Duration[][]
        {
          {
            Duration.parse("PT0.100S")
          },
          {
            Duration.parse("PT1.300S")
          }
        }
      )
      .build();

    viewMap.put(FileDescriptor.create("bucketName", "keyP"), viewP);
    viewMap.put(FileDescriptor.create("bucketName", "keyS"), viewS);
  }

  void setup() {
    Mockito
      .when(mockedFileStore.findByKeyPrefix(
        Mockito.any(FileDescriptor.class),
        Mockito.any(TravelTimeLookupViewTransformer.class)
      ))
      .thenReturn(viewMap);
    Mockito
      .when(mockedConfiguration.getTravelTimeLookupTableDefinition())
      .thenReturn(definition);

    lookupTable = new Ak135TravelTimeLookupTable(mockedFileStore, mockedConfiguration);
  }

  @Test
  void testInitialize() {
    setup();
    Assertions.assertNotNull(lookupTable);
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());
  }

  @Test
  void testGetName() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());
    Assertions.assertEquals("Ak135TravelTimeLookupTable", lookupTable.getName());
  }

  @Test
  void testGetUnits() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    AtomicReference<Units> units = new AtomicReference<>();

    Assertions.assertDoesNotThrow(() -> units.set(lookupTable.getUnits()));
    Assertions.assertEquals(Units.SECONDS, units.get());
  }

  @Test
  void testGetAvailablePhaseTypes() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    AtomicReference<Set<PhaseType>> set = new AtomicReference<>();

    Assertions.assertDoesNotThrow(() -> set.set(lookupTable.getAvailablePhaseTypes()));
    Assertions.assertEquals(2, set.get().size());
    Assertions.assertTrue(set.get().contains(PhaseType.P));
    Assertions.assertTrue(set.get().contains(PhaseType.S));
  }

  @Test
  void testGetDepthsKmForData() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    AtomicReference<ImmutableDoubleArray> doubleArray = new AtomicReference<>();

    Assertions.assertDoesNotThrow(
      () -> doubleArray.set(lookupTable.getDepthsKmForData(PhaseType.P)));
    Assertions.assertArrayEquals(new double[]{0.0, 700.0}, doubleArray.get().toArray());

    Assertions.assertDoesNotThrow(
      () -> doubleArray.set(lookupTable.getDepthsKmForData(PhaseType.S)));
    Assertions.assertArrayEquals(new double[]{0.0, 700.0}, doubleArray.get().toArray());
  }

  @Test
  void testGetDistancesDegForData() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    AtomicReference<ImmutableDoubleArray> doubleArray = new AtomicReference<>();

    Assertions.assertDoesNotThrow(
      () -> doubleArray.set(lookupTable.getDistancesDegForData((PhaseType.P))));
    Assertions.assertArrayEquals(new double[]{0.0, 180.0}, doubleArray.get().toArray());

    Assertions.assertDoesNotThrow(
      () -> doubleArray.set(lookupTable.getDistancesDegForData((PhaseType.S))));
    Assertions.assertArrayEquals(new double[]{0.0, 180.0}, doubleArray.get().toArray());
  }

  @Test
  void testGetValues() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    AtomicReference<Immutable2dArray<Duration>> durationArray = new AtomicReference<>();

    Assertions.assertDoesNotThrow(() -> durationArray.set(lookupTable.getValues(PhaseType.P)));
    Assertions.assertEquals(Duration.parse("PT0.0S"), durationArray.get().getValue(0, 0));
    Assertions.assertEquals(Duration.parse("PT1212.5273S"), durationArray.get().getValue(0, 1));
    Assertions.assertEquals(Duration.parse("PT79.6958S"), durationArray.get().getValue(1, 0));
    Assertions.assertEquals(Duration.parse("PT1132.8315S"), durationArray.get().getValue(1, 1));

    Assertions.assertDoesNotThrow(() -> durationArray.set(lookupTable.getValues(PhaseType.S)));
    Assertions.assertEquals(Duration.parse("PT0.0S"), durationArray.get().getValue(0, 0));
    Assertions.assertEquals(Duration.parse("PT1636.6226S"), durationArray.get().getValue(0, 1));
    Assertions.assertEquals(Duration.parse("PT144.6422S"), durationArray.get().getValue(1, 0));
    Assertions.assertEquals(Duration.parse("PT1491.9803S"), durationArray.get().getValue(1, 1));
  }

  @Test
  void testGetDepthsKmForStandardDeviations() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    AtomicReference<ImmutableDoubleArray> doubleArray = new AtomicReference<>();

    Assertions.assertDoesNotThrow(
      () -> doubleArray.set(lookupTable.getDepthsKmForStandardDeviations(PhaseType.P)));
    Assertions.assertArrayEquals(new double[]{0.0, 200.0}, doubleArray.get().toArray());

    Assertions.assertDoesNotThrow(
      () -> doubleArray.set(lookupTable.getDepthsKmForStandardDeviations(PhaseType.S)));
    Assertions.assertEquals(0, doubleArray.get().length());
  }

  @Test
  void testGetDistancesDegForStandardDeviations() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    AtomicReference<ImmutableDoubleArray> doubleArray = new AtomicReference<>();

    Assertions.assertDoesNotThrow(
      () -> doubleArray.set(lookupTable.getDistancesDegForStandardDeviations(PhaseType.P)));
    Assertions.assertArrayEquals(new double[]{0.0, 180.0}, doubleArray.get().toArray());

    Assertions.assertDoesNotThrow(
      () -> doubleArray.set(lookupTable.getDistancesDegForStandardDeviations(PhaseType.S)));
    Assertions.assertArrayEquals(new double[]{0.0, 180.0}, doubleArray.get().toArray());
  }

  @Test
  void testGetStandardDeviations() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    AtomicReference<Immutable2dArray<Duration>> durationArray = new AtomicReference<>();

    Assertions.assertDoesNotThrow(
      () -> durationArray.set(lookupTable.getStandardDeviations(PhaseType.P)));
    Assertions.assertEquals(Duration.parse("PT0.1S"), durationArray.get().getValue(0, 0));
    Assertions.assertEquals(Duration.parse("PT1.8S"), durationArray.get().getValue(0, 1));
    Assertions.assertEquals(Duration.parse("PT0.7S"), durationArray.get().getValue(1, 0));
    Assertions.assertEquals(Duration.parse("PT1.5S"), durationArray.get().getValue(1, 1));

    Assertions.assertDoesNotThrow(
      () -> durationArray.set(lookupTable.getStandardDeviations(PhaseType.S)));
    Assertions.assertEquals(Duration.parse("PT0.100S"), durationArray.get().getValue(0, 0));
    Assertions.assertEquals(Duration.parse("PT1.300S"), durationArray.get().getValue(1, 0));
  }

  @Test
  void testUninitializedGetName() {
    lookupTable = new Ak135TravelTimeLookupTable(mockedFileStore, mockedConfiguration);
    Assertions.assertEquals("Ak135TravelTimeLookupTable", lookupTable.getName());
  }

  @Test
  void testUninitializedGetters() {
    lookupTable = new Ak135TravelTimeLookupTable(mockedFileStore, mockedConfiguration);

    Assertions.assertThrows(IllegalArgumentException.class, lookupTable::getUnits);
    Assertions.assertThrows(IllegalArgumentException.class, lookupTable::getAvailablePhaseTypes);
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getDepthsKmForData(PhaseType.P));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getDistancesDegForData((PhaseType.P)));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getValues(PhaseType.P));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getDepthsKmForStandardDeviations(PhaseType.P));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getDistancesDegForStandardDeviations(PhaseType.P));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getStandardDeviations(PhaseType.P));
  }

  @Test
  void testUnavailablePhaseType() {
    setup();
    Assertions.assertDoesNotThrow(() -> lookupTable.initialize());

    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getDepthsKmForData(PhaseType.I));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getDistancesDegForData((PhaseType.I)));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getValues(PhaseType.I));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getDepthsKmForStandardDeviations(PhaseType.I));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getDistancesDegForStandardDeviations(PhaseType.I));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> lookupTable.getStandardDeviations(PhaseType.I));
  }

}
