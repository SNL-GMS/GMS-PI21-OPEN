package gms.shared.stationdefinition.converter.interfaces;

import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;

import java.nio.file.Path;

public interface FrequencyAmplitudePhaseConverter {

  /**
   * Converts the file at the location of the passed filename string to a
   * {@link FrequencyAmplitudePhase} object.
   *
   * @param channelName String holding the two or three letter channel name.
   * @param path Path holding the filename path of the FAP Response file.
   * @return A FrequencyAmplitudePhase object containing the information passed in through the FAP File
   */

  FrequencyAmplitudePhase convert(String channelName, Path path);


  FrequencyAmplitudePhase convertToEntityReference(String fileName);

}
