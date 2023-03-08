package gms.shared.utilities.signalprocessing.validation;

import gms.shared.frameworks.osd.coi.waveforms.Waveform;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;

public class JitterPredicate implements Predicate<Waveform> {

  private final Instant jitterBaseStartTime;

  public JitterPredicate(Instant jitterBaseStartTime) {
    this.jitterBaseStartTime = jitterBaseStartTime;
  }

  @Override
  public boolean test(Waveform waveform) {
    if (jitterBaseStartTime.equals(waveform.getStartTime())) {
      return true;
    }

    Instant actualStart = waveform.getStartTime();
    Duration unfilledArea = Duration.between(jitterBaseStartTime, actualStart);
    Duration samplePeriod = waveform.getSamplePeriod();

    long unfilledSamples = unfilledArea.dividedBy(samplePeriod);
    Instant interpolatedStart = actualStart.minus(samplePeriod.multipliedBy(unfilledSamples));
    Duration allowableJitter = samplePeriod.dividedBy(2);

    Duration positiveJitter = Duration.between(jitterBaseStartTime, interpolatedStart);
    if (positiveJitter.compareTo(allowableJitter) < 0) {
      return true;
    } else {
      Instant reinterpolatedStart = interpolatedStart.minus(samplePeriod);
      Duration negativeJitter = Duration.between(reinterpolatedStart, jitterBaseStartTime);
      return negativeJitter.compareTo(allowableJitter) < 0;
    }
  }
}
