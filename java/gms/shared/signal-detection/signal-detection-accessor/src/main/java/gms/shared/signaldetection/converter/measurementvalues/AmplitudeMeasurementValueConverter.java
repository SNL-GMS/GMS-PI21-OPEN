package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.values.AmplitudeMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.dao.css.enums.AmplitudeUnits;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.bridge.database.enums.ClipFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AmplitudeMeasurementValueConverter implements
  MeasurementValueConverter<AmplitudeMeasurementValue> {

  private static EnumMap<AmplitudeUnits, Units> ampUnitsToUnitsMap = new EnumMap<>(AmplitudeUnits.class);
  private static final long NANO_SECOND_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
  private static final Logger logger = LoggerFactory.getLogger(AmplitudeMeasurementValueConverter.class);

  static {
    ampUnitsToUnitsMap.put(AmplitudeUnits.NM, Units.NANOMETERS);
    ampUnitsToUnitsMap.put(AmplitudeUnits.PASCALS, Units.PASCALS);
    ampUnitsToUnitsMap.put(AmplitudeUnits.HERTZ, Units.HERTZ);
    ampUnitsToUnitsMap.put(AmplitudeUnits.LOG_NM, Units.LOG_NM);
    ampUnitsToUnitsMap.put(AmplitudeUnits.NA, Units.UNITLESS);
  }

  private AmplitudeMeasurementValueConverter() {
  }

  public static AmplitudeMeasurementValueConverter create() {
    return new AmplitudeMeasurementValueConverter();
  }

  @Override
  public Optional<AmplitudeMeasurementValue> convert(MeasurementValueSpec<AmplitudeMeasurementValue> spec) {

    var amplitudeDaoOptional = spec.getAmplitudeDao();

    if (amplitudeDaoOptional.isEmpty()) {
      logger.warn("No amplitude dao found to create AmplitudeMeasurementValue from.");
      return Optional.empty();
    }

    var amplitudeDao = amplitudeDaoOptional.orElseThrow();
    var units = ampUnitsToUnitsMap.get(amplitudeDao.getUnits());
    if (units == null) {
      logger.warn("No appropriate units found for amplitude.");
      return Optional.empty();
    }
    var doubleValue = DoubleValue.from(amplitudeDao.getAmplitude(), Optional.empty(), units);

    long periodNanoSeconds = (long) (amplitudeDao.getPeriod() * NANO_SECOND_PER_SECOND);
    var periodDuration = Duration.ofNanos(periodNanoSeconds);
    if (periodDuration.isZero()) {
      logger.warn("Period duration of amplitude dao is zero.");
      return Optional.empty();
    }

    var measurementTime = getValueOrEmptyOptional(amplitudeDao::getAmplitudeTime);
    var measurementWindowStart = getValueOrEmptyOptional(amplitudeDao::getTime);
    var measurementWindowDuration = getValueOrEmptyOptional(amplitudeDao::getDuration);

    Optional<Boolean> clipped = Optional.empty();
    var ampClip = amplitudeDao.getClip();
    if (ampClip == ClipFlag.CLIPPED) {
      clipped = Optional.of(true);
    } else if (ampClip == ClipFlag.NOT_CLIPPED) {
      clipped = Optional.of(false);
    }

    return Optional.of(AmplitudeMeasurementValue.from(doubleValue, periodDuration, measurementTime, measurementWindowStart,
      measurementWindowDuration, clipped));
  }

  private <T> Optional<T> getValueOrEmptyOptional(Supplier<T> source) {

    var val = source.get();

    if (val == null) {
      return Optional.empty();
    }
    return Optional.of(val);
  }
}
