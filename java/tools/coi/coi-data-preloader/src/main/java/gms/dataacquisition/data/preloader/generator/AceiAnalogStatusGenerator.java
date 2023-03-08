package gms.dataacquisition.data.preloader.generator;


import org.apache.commons.lang3.Validate;

import java.security.SecureRandom;

/**
 * This class randomly generates a series of analogue ACEI ({@link gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog})
 * status values.  Values are drawn from a first order auto-regressive time series model (AR1) that
 * are then clipped to a [min, max] range:
 * <p>
 * nextValue = beta0 + beta1*prevValue + gaussianNoise,
 * </p>
 * and then clipped to [min, max].
 */
public class AceiAnalogStatusGenerator {

  private final double min;
  private final double max;
  private final double beta0;
  private final double beta1;
  private final double stderr;
  private final SecureRandom random = new SecureRandom();
  private double prevStatus;

  /**
   * Constructor for analogue ACEI status generator.
   *
   * @param min minimum allowed status
   * @param max maximum allowed status
   * @param beta0 constant parameter of AR1 time series model
   * @param beta1 1st order parameter of AR1 time series model
   * @param stderr standard deviation of additive Gaussian noise for AR1 time series model
   * @param initialValue initial status of AR1 time series model
   */
  public AceiAnalogStatusGenerator(double min, double max, double beta0, double beta1,
    double stderr, double initialValue) {
    Validate.isTrue(min <= max);
    Validate.isTrue(0.0 <= stderr);

    this.min = min;
    this.max = max;
    this.beta0 = beta0;
    this.beta1 = beta1;
    this.stderr = stderr;
    this.prevStatus = initialValue;
  }

  /**
   * Return the next value of the AR1 time series
   *
   * @return next value of the AR1 time series
   */
  public double next() {
    double nextStatus = beta0 + beta1 * prevStatus + nextError();
    nextStatus = Math.max(Math.min(nextStatus, max), min);
    prevStatus = nextStatus;
    return nextStatus;
  }

  /* Return a Gaussian sample with zero mean and variance=stderr^2 */
  private double nextError() {
    return stderr * random.nextGaussian();
  }

}
