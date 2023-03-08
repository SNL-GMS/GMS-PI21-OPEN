package gms.shared.stationdefinition.converter.interfaces;

import gms.shared.stationdefinition.coi.channel.Calibration;
import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;

public interface ResponseConverter {

  /**
   * Converts the WfdiscDao objects, to a
   * {@link Response} object.
   *
   * @param wfdiscDao object
   * @param calibration object
   * @param frequencyAmplitudePhase object
   * @return A Response object containing the information passed in through wfDiscDAO and FAP.
   */
  Response convert(WfdiscDao wfdiscDao, SensorDao sensorDao, Calibration calibration,
    FrequencyAmplitudePhase frequencyAmplitudePhase);

  /**
   * Converts the WfdiscDao object to an entity reference {@link Response}.
   *
   * @param wfdiscDao wfdisc dao object
   * @return A Response object containing info passed in through wfDiscDAO
   */
  Response convertToEntity(WfdiscDao wfdiscDao);

}
