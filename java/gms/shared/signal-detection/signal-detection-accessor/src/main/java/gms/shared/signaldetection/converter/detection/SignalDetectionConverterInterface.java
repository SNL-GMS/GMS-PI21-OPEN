package gms.shared.signaldetection.converter.detection;

import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.repository.utils.SignalDetectionComponents;

import java.util.Optional;

public interface SignalDetectionConverterInterface {

  /**
   * Convert method definition for converting legacy DB to COI objects for SignalDetection.
   *
   * @param signalDetectionComponents the {@link SignalDetectionComponents}
   * object that contains necessary information for creation of {@link SignalDetection}s
   * @return a SignalDetection object
   */
  Optional<SignalDetection> convert(SignalDetectionComponents signalDetectionComponents);
}
