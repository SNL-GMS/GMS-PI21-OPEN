package gms.shared.stationdefinition.converter.interfaces;

import gms.shared.stationdefinition.coi.channel.Calibration;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;

public interface CalibrationConverter {

  /**
   * Converts the SensorDao object to a
   * {@link Calibration} object.
   *
   * @param wfdiskDao The wfdisk object to read part of the calibration from.
   * @param sensorDao The sensor object to read the tshift from.
   * @return A Calibration object containing the information passed in through sensorDao
   */
  Calibration convert(WfdiscDao wfdiskDao, SensorDao sensorDao);


}
