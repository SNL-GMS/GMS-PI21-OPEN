package gms.shared.featureprediction.plugin.correction.ellipticity;

import com.google.common.primitives.ImmutableDoubleArray;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.filestore.FileDescriptor;
import gms.shared.utilities.filestore.FileStore;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DziewonskiGilbertLookupTableTest {

  @Mock
  FileStore fileStore;
  @Mock
  DziewonskiGilbertLookupTableConfiguration config;
  @InjectMocks
  DziewonskiGilbertLookupTable dziewonskiGilbertLookupTable;
  
  DziewonskiGilbertLookupTableView lookupTableView;

  @BeforeEach
  void init() {
    var minIoBucketName = "chumBucket";
    var dataDescriptor = "conchStreet";
    var fileDescriptor = FileDescriptor.create(minIoBucketName, dataDescriptor);
    var definition = DziewonskiGilbertLookupTableDefinition.create(Map.of("Ak135", dataDescriptor));
    lookupTableView = DziewonskiGilbertLookupTableView.builder()
      .setModel("Ak135")
      .setPhase(PhaseType.P)
      .setDepthUnits("kilometers")
      .setDistanceUnits("degrees")
      .setDepths(List.of(1D, 2D, 3D))
      .setDistances(List.of(4D, 5D, 6D))
      .setTau0(List.of(List.of(1D, 2D, 3D)))
      .setTau1(List.of(List.of(1D, 2D, 3D)))
      .setTau2(List.of(List.of(1D, 2D, 3D)))
      .build();

    when(config.minIoBucketName()).thenReturn(minIoBucketName);
    when(config.getCurrentDiezwonskiGilbertLookupTableDefinition())
      .thenReturn(definition);
    when(fileStore.findByKeyPrefix(fileDescriptor, DziewonskiGilbertLookupTableView.class))
      .thenReturn(Map.of(fileDescriptor, lookupTableView));
    
    dziewonskiGilbertLookupTable.initialize();
  }
  
  @Test
  void testGetUnits() {
    assertEquals(Triple.of(Units.SECONDS, Units.SECONDS, Units.SECONDS), dziewonskiGilbertLookupTable.getUnits());
  }
  
  @Test
  void testGetAvailablePhaseTypes() {
    assertEquals(Set.of(PhaseType.P), dziewonskiGilbertLookupTable.getAvailablePhaseTypes());
  }
  
  @Test
  void testGetDepthsKmForData() {
    var expectedDepths = ImmutableDoubleArray.copyOf(lookupTableView.getDepths());
    assertEquals(expectedDepths, dziewonskiGilbertLookupTable.getDepthsKmForData(PhaseType.P));
  }
  
  @Test
  void testGetDistancesDegForData() {
    var expectedDistances = ImmutableDoubleArray.copyOf(lookupTableView.getDistances());
    assertEquals(expectedDistances, dziewonskiGilbertLookupTable.getDistancesDegForData(PhaseType.P));
  }
  
  @Test
  void testGetValues() {
    var values = List.of(List.of(1D, 2D, 3D));
    var expectedValues = Triple.of(values, values, values);
    assertEquals(expectedValues, dziewonskiGilbertLookupTable.getValues(PhaseType.P));
  }
  
  @Test
  void testGetDepthsKmForStandardDeviations() {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      dziewonskiGilbertLookupTable.getDepthsKmForStandardDeviations(PhaseType.P);
    });
  }
  
  @Test
  void testGetDistancesDegForStandardDeviations() {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      dziewonskiGilbertLookupTable.getDistancesDegForStandardDeviations(PhaseType.P);
    });
  }
  
  @Test
  void testGetStandardDeviations() {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      dziewonskiGilbertLookupTable.getStandardDeviations(PhaseType.P);
    });
  }
  
  @Test
  void testGetName() {
    assertEquals("DziewonskiGilbertLookupTable", dziewonskiGilbertLookupTable.getName());
  }
}
