package gms.tools.stationrefbuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CSVManager {

  private static final Logger logger = LoggerFactory.getLogger(CSVManager.class);

  /**
   * Logs the header for the .csv file for all station details.
   *
   * @param f - the filewriter containing the name of the file to write
   */
  protected void logDetailsHead(FileWriter f) throws IOException {
    f.write("Station Name\tIMS #\tLocation\tContinent\tStation Type\t# Channels\tOriginal Format\tComments\n");
    f.flush();
  }


  /**
   * Write the details of the station.
   *
   * @param f The Filewriter object containing the name of the file.
   * @param station The station object containing all pertinent information for the station.
   */
  protected void logDetails(FileWriter f, Station station) throws IOException {
    f.write(station.toString());
    f.flush();
  }

  /**
   * Write the Station Group List from a horizontal hash map to a vertical .csv file.
   *
   * @param f The Filewriter object with the name of the file to be written.
   * @param stationGroupsWithStationsMap The station groups and all stations they contain.
   */
  protected void writeStationGroupList(FileWriter f, HashMap<String, List<String>> stationGroupsWithStationsMap) {
    try {

      //first things first - SORT IT! For testability and readability
      //in order to make this testable, need to have a specific order...
      ArrayList<String> keys = new ArrayList<>(stationGroupsWithStationsMap.keySet());
      Collections.sort(keys);
      ArrayList<String>[] values = new ArrayList[stationGroupsWithStationsMap.size()];
      var index = 0;
      var largest = 0;
      for (String key : keys) {
        values[index] = (ArrayList<String>) stationGroupsWithStationsMap.get(key);
        if (values[index].size() > largest) {
          largest = values[index].size();
        }
        f.write(key + "\t");
        index++;
      }
      f.write("\n");
      for (var i = 0; i < largest; i++) {
        for (var j = 0; j < values.length; j++) {

          if (values[j] != null && values[j].size() > i) {
            f.write(values[j].get(i));
          }
          f.write("\t");
        }
        f.write("\n");
      }
      f.flush();
    } catch (IOException ex) {
      logger.error("IOException while writing Station Group List.");
    }
    logger.info("Station Groups:");
    stationGroupsWithStationsMap.forEach((s, l) -> logger.info("{}: {}", s, l));
  }


}
