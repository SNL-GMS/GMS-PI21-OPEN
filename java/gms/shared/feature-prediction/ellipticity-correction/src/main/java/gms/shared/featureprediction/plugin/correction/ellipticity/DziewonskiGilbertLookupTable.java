package gms.shared.featureprediction.plugin.correction.ellipticity;

import com.google.common.primitives.ImmutableDoubleArray;
import gms.shared.featureprediction.plugin.api.lookuptable.DziewonskiGilbertEllipticityCorrectionLookupTablePlugin;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.filestore.FileDescriptor;
import gms.shared.utilities.filestore.FileStore;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class DziewonskiGilbertLookupTable implements DziewonskiGilbertEllipticityCorrectionLookupTablePlugin {
  
  private static final String NOT_SUPPORTED_YET = "Not supported yet.";

  private static final Logger logger = LoggerFactory.getLogger(DziewonskiGilbertLookupTable.class);

  private final FileStore fileStore;
  private final DziewonskiGilbertLookupTableConfiguration lookupTableConfiguration;
  private Map<PhaseType, DziewonskiGilbertLookupTableView> phaseToLookupTable;

  @Autowired
  public DziewonskiGilbertLookupTable(FileStore fileStore, DziewonskiGilbertLookupTableConfiguration lookupTableConfiguration) {
    this.fileStore = fileStore;
    this.lookupTableConfiguration = lookupTableConfiguration;
  }

  @Override
  public Triple<Units, Units, Units> getUnits() {
    return Triple.of(Units.SECONDS, Units.SECONDS, Units.SECONDS);
  }

  @Override
  public Set<PhaseType> getAvailablePhaseTypes() {
    return phaseToLookupTable.keySet();
  }

  @Override
  public ImmutableDoubleArray getDepthsKmForData(PhaseType phaseType) {
    return ImmutableDoubleArray.copyOf(requireNonNull(phaseToLookupTable.get(phaseType)).getDepths());
  }

  @Override
  public ImmutableDoubleArray getDistancesDegForData(PhaseType phaseType) {
    return ImmutableDoubleArray.copyOf(requireNonNull(phaseToLookupTable.get(phaseType)).getDistances());
  }

  @Override
  public Triple<List<List<Double>>, List<List<Double>>, List<List<Double>>> getValues(PhaseType phaseType) {
    var tau0 = requireNonNull(phaseToLookupTable.get(phaseType)).getTau0();
    var tau1 = requireNonNull(phaseToLookupTable.get(phaseType)).getTau1();
    var tau2 = requireNonNull(phaseToLookupTable.get(phaseType)).getTau2();
    return Triple.of(tau0, tau1, tau2);
  }

  @Override
  public ImmutableDoubleArray getDepthsKmForStandardDeviations(PhaseType phaseType) {
    throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
  }

  @Override
  public ImmutableDoubleArray getDistancesDegForStandardDeviations(PhaseType phaseType) {
    throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
  }

  @Override
  public Triple<List<List<Double>>, List<List<Double>>, List<List<Double>>> getStandardDeviations(PhaseType phaseType) {
    throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
  }

  @Override
  public void initialize() {
    phaseToLookupTable = new EnumMap<>(PhaseType.class);
    var minIoBucketName = lookupTableConfiguration.minIoBucketName();
    var keyPrefix = lookupTableConfiguration.getCurrentDiezwonskiGilbertLookupTableDefinition().getEarthModelToDataDescriptor().get("Ak135");
    var fileDescriptorToLookupTableView = fileStore.findByKeyPrefix(FileDescriptor.create(minIoBucketName, keyPrefix), DziewonskiGilbertLookupTableView.class);
    fileDescriptorToLookupTableView.values().forEach(lookupTableView -> phaseToLookupTable.put(lookupTableView.getPhase(), lookupTableView));
    logger.info("Loaded Dziewonski-Gilbert lookup table information for the following phases: {}", phaseToLookupTable.keySet());
  }

}
