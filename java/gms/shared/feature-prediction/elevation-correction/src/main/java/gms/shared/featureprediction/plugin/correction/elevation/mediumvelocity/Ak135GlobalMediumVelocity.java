package gms.shared.featureprediction.plugin.correction.elevation.mediumvelocity;

import gms.shared.featureprediction.plugin.api.correction.elevation.mediumvelocity.MediumVelocityEarthModelPlugin;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.filestore.FileDescriptor;
import gms.shared.utilities.filestore.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class Ak135GlobalMediumVelocity implements MediumVelocityEarthModelPlugin {

  private static final Logger logger = LoggerFactory.getLogger(Ak135GlobalMediumVelocity.class);

  private final Ak135GlobalMediumVelocityConfiguration configuration;
  private final FileStore fileStore;
  private Map<PhaseType, Double> phaseTypeToMediumVelocity;

  @Autowired
  public Ak135GlobalMediumVelocity(
    Ak135GlobalMediumVelocityConfiguration configuration,
    FileStore fileStore) {
    this.configuration = requireNonNull(configuration);
    this.fileStore = requireNonNull(fileStore);
  }

  @Override
  public void initialize() {
    var bucketName = configuration.minIoBucketName();
    var dataDescriptor = configuration.ak135GlobalMediumVelocityDefinition().getDataDescriptor();
    var fileDescriptor = FileDescriptor.create(bucketName, dataDescriptor);
    phaseTypeToMediumVelocity = fileStore.findByFileDescriptor(fileDescriptor, PhaseTypeToMediumVelocity.class);
    logger.info("Initialized with medium velocities: {}", phaseTypeToMediumVelocity);
  }

  @Override
  public Units getUnits() {
    return Units.KILOMETERS_PER_SECOND;
  }

  @Override
  public Set<PhaseType> getAvailablePhaseTypes() {
    return phaseTypeToMediumVelocity.keySet();
  }

  @Override
  public Double getValue(PhaseType phaseType, Location location) {
    requireNonNull(phaseType);

    var mediumVelocity = phaseTypeToMediumVelocity.get(phaseType);

    if (Objects.isNull(mediumVelocity))
      throw new IllegalArgumentException("Ak135GlobalMediumVelocity only defined for final phases P and S, got " + phaseType);

    return mediumVelocity;
  }

  @Override
  public Double getStandardDeviation(PhaseType phaseType, Location location) {
    return null;
  }
}
