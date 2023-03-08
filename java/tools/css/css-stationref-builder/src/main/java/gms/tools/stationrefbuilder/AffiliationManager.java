package gms.tools.stationrefbuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * AffiliationManager handles creation and editing of affiliation.dat files, which are utilized to
 * assign station groups to a station. The file structure is like this:
 * data/Station_Name/affiliation.dat. It is dependent on the location of the data files and the
 * configuration.json and stationgrouprules.json files. Interaction with the StationGroupBuilder to
 * figure out which groups the current station should be in.
 */
public class AffiliationManager {

  private static final Logger logger = LoggerFactory.getLogger(AffiliationManager.class);

  private URL scanDir;

  private StationReferenceBuilderConfiguration stationReferenceBuilderConfiguration;
  private StationGroupBuilderConfiguration stationGroupBuilderConfiguration;
  private ConfigurationFileManager configurationFileManager;


  public AffiliationManager(URL meta, StationReferenceBuilderConfiguration conf,
    ConfigurationFileManager cm, StationGroupBuilderConfiguration grp) {
    scanDir = meta;
    stationReferenceBuilderConfiguration = conf;
    configurationFileManager = cm;
    stationGroupBuilderConfiguration = grp;
  }


  /**
   * Handles the creation of the affiliation.dat files for all stations.
   *
   * @param node - the file currently under review - affiliation.dat
   * @throws IOException if it cannot access the file.
   */
  protected void handleAffiliation(File node) throws IOException {
    if (!stationReferenceBuilderConfiguration.getWriteAffiliations()) {
      return;
    }
    var path = new File(configurationFileManager.getCurrentStat().getStationName(), node.getName());
    final var finalPath = new File(
      scanDir.getPath(), path.toPath().toString());
    if (!finalPath.getAbsolutePath().startsWith(Paths.get(this.scanDir.getPath()).normalize().toAbsolutePath().toString())) {
      logger.warn("Path manipulation detected! Affiliation will not be written to disk");
      return;
    }

    try (var affiliation = new FileWriter(finalPath, false)) {
      var dateTime = ZonedDateTime.now();
      var formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
      var line = String.format("%-7s %-17s %-16s %s %s",
        configurationFileManager.getCurrentStat().getStationName(),
        configurationFileManager.getCurrentStat().getDateStarted(),
        "9999999999.99900", formatter.format(dateTime), "12:00:00\n");

      //There is some house keeping that we have to do before we can start comparing...
      //like loading the CSV files and the locations...

      configurationFileManager.loadLocation(new File(node.getParent() + "/site.dat"));

      //
      var stationGroupBuilder = new StationGroupBuilder(
        stationGroupBuilderConfiguration, scanDir);
      configurationFileManager.getCurrentStat()
        .setInGroups(stationGroupBuilder.checkAgainstAllRules(
          configurationFileManager.getCurrentStat()));

      ArrayList<String> affiliationLinesForFile = new ArrayList<>();

      for (Map.Entry<String, Boolean> pair : configurationFileManager.getCurrentStat()
        .getInGroups().entrySet()) {
        if (Boolean.TRUE.equals(pair.getValue())) {
          affiliationLinesForFile.add(String.format("%-9s", pair.getKey()) + line);
          configurationFileManager.getAllGroups().get(pair.getKey()).add(
            configurationFileManager.getCurrentStat().getStationName());
        }
      }

      //in order to make this testable, need to have a specific order...
      Collections.sort(affiliationLinesForFile);

      writeAffiliation(affiliation, affiliationLinesForFile);
    } catch (FileNotFoundException ex) {
      logger.error("Could not find Affiliation for {}.",
        configurationFileManager.getCurrentStat().getStationName());
    }
  }


  /**
   * Write the affiliation.dat file based on the information generated in this file.
   *
   * @param f The Filewriter object with the name of the file to be written
   */
  private void writeAffiliation(FileWriter f, ArrayList<String> affiliationFileLines)
    throws IOException {
    for (String message : affiliationFileLines) {
      f.append(message);
      f.flush();
    }
  }


}
