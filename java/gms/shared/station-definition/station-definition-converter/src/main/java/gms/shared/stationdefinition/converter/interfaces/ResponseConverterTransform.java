package gms.shared.stationdefinition.converter.interfaces;

import gms.shared.stationdefinition.coi.channel.Calibration;
import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;

@FunctionalInterface
public interface ResponseConverterTransform {
  Response getResponse(WfdiscDao wfdiscDao, SensorDao sensorDao, Calibration calibration,
    FrequencyAmplitudePhase frequencyAmplitudePhase);
}
