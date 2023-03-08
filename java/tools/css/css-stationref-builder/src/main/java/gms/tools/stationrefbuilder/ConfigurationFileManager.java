package gms.tools.stationrefbuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Stream;

/**
 * This class holds all configuration lists and objects, such as the main config, the country list,
 * groups, allGroups, stations, etc.
 */
public class ConfigurationFileManager {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationFileManager.class);

  private Station currentStat;
  private URL scanDir;
  private StationReferenceBuilderConfiguration stationReferenceBuilderConfiguration;
  private StationGroupBuilderConfiguration stationGroupBuilderConfiguration;

  protected static final String HIGHDATE = "2286324";
  protected static final String HIGHVALUE = "9999999999.99900";

  private HashMap<String, List<String>> allGroups = new HashMap<>();


  protected ConfigurationFileManager(URL sd, StationReferenceBuilderConfiguration conf,
    StationGroupBuilderConfiguration grpConfig) {
    scanDir = sd;
    stationReferenceBuilderConfiguration = conf;
    stationGroupBuilderConfiguration = grpConfig;

    //Now set the empty station Group map for the "affected stations" to be added
    stationGroupBuilderConfiguration.getGroupNameList().forEach(v -> allGroups.put(v,
      new ArrayList<>()));

  }

  /**
   * Write the given ArrayList into the File Handle and flush.
   *
   * @param node The file Handle
   * @param currentFile The ArrayList of lines to write in this text file.
   * @param append Whether to replace or append the file in question
   */
  protected void writeFile(File node, ArrayList<String> currentFile, boolean append) {
    try (var tmp = new FileWriter(
      scanDir.getPath() + "/" + currentStat.getStationName() + "/" + node.getName(), append)) {
      for (String message : currentFile) {
        tmp.write(message);
        tmp.flush();
      }
    } catch (IOException ex) {
      logger.error("Station file not found", ex);
    }
  }

  /**
   * Loads the location from the site.dat file.
   * For convenience, this also reads in the 'startDate' to use for Affiliation.dat
   *
   * @param node - the site.dat file for the station in question.
   */
  protected void loadLocation(File node) {
    try (var scanner = new Scanner(node)) {
      ArrayList<String> types = new ArrayList<>();
      if (currentStat.getLocation() != null) {
        return;
      }
      var location = "";
      while (scanner.hasNextLine()) {
        var matchLine = scanner.nextLine();
        var st1 = new StringTokenizer(matchLine, " ");
        ArrayList<String> lineWords = new ArrayList<>();
        while (st1.hasMoreElements()) {
          lineWords.add(st1.nextToken());
        }
        //second column is date started... need for writing affiliation.dat
        var calCalc = Integer.parseInt(lineWords.get(1));
        var cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, calCalc / 1000);
        cal.set(Calendar.DAY_OF_YEAR, calCalc % 1000);
        currentStat.setDateStarted(Long.toString(cal.getTimeInMillis() / 1000));
        if (location.isEmpty()) {
          //seventh column is location...
          var st = new StringTokenizer(lineWords.get(6), "_,");
          var bld = new StringBuilder();
          while (st.hasMoreTokens()) {
            bld.append(st.nextToken());
            bld.append(" ");
          }
          location = bld.toString();
          location = Stream.of(location.split(" "))
            .map(w -> w.toUpperCase().charAt(0) + w.toLowerCase().substring(1))
            .reduce((s, s2) -> s + " " + s2).orElse("");
        }
        if (!types.contains(lineWords.get(7))) {
          types.add(lineWords.get(7));
        }
      }
      currentStat.setLocation(location);
      currentStat.setContinent(checkForContinent(location));
      setType(types);
    } catch (FileNotFoundException ex) {
      logger.error("File not found.");
    }

  }

  private String checkForContinent(String location) {
    for (String locWord : location.split(" ")) {
      for (String tmp : stationReferenceBuilderConfiguration.getCountriesByContinent().keySet()) {
        ArrayList<String> tmpList = Objects.requireNonNullElse(
          (ArrayList<String>)
            stationReferenceBuilderConfiguration.getCountriesByContinent().get(tmp), new ArrayList<>());
        if (tmpList.contains(locWord)) {
          return tmp;
        }
      }
    }
    return "NA";
  }

  /**
   * This method checks for extraneous white space, which can break the reference reader.
   *
   * @param node The file to be checked for white space violations.
   */
  protected void checkForWhiteSpace(File node) {
    var isWhiteSpace = false;
    try (var scanner = new Scanner(node)) {
      ArrayList<String> currentFile = new ArrayList<>();
      var line = "";
      while (scanner.hasNextLine()) {
        line = scanner.nextLine();
        if ("".equals(line.trim())) {
          //empty line... so call remove!
          isWhiteSpace = true;
          logger.info("Found empty line");
        } else {
          currentFile.add(line);
        }
      }
      if (isWhiteSpace) {
        //eradicate white space... by writing the new file without the spare line...
        writeFile(node, currentFile, false);
      }
    } catch (FileNotFoundException ex) {
      logger.error("checkForWhitespace: FileNotFound Exception occurred", ex);
    }
  }

  /**
   * Check for duplicates, which will break the reference reader. Removes the duplicate and rewrites
   * the file without it.
   *
   * @param node The file being tested.
   */
  protected void checkForDuplicates(File node) {
    var isDuplicates = false;
    if (!stationReferenceBuilderConfiguration.getCheckForDuplicates()) {
      return;
    }
    try (var scanner = new Scanner(node)) {
      ArrayList<String> currentFile = new ArrayList<>();
      var line = "";
      while (scanner.hasNextLine()) {
        line = scanner.nextLine();
        if (currentFile.contains(line)) {
          logger.info(
            "Found duplicate in file");
          isDuplicates = true;
        } else {
          currentFile.add(line + "\n");
        }
      }

      if (isDuplicates) {
        //eliminate duplicates... by writing the new file without the spare line...
        writeFile(node, currentFile, false);

      }
    } catch (FileNotFoundException ex) {
      logger.error("checkForDuplicates: FileNotFound Exception occurred.", ex);
    }
  }

  /**
   * This method checks for the existence of special replacement rules, like adding hardcoded lines from
   * a text file, or replacing one value for another by adding additional lines in a file.
   * This is for outlier cases that need to be addressed manually.
   *
   * @param repl The replacement rule
   * @return true if replacement was successfully generated.
   */
  protected boolean checkForSpecialReplacements(Replacement repl) {
    boolean result;

    switch (repl.getOperator()) {
      case "file":
        result = doFileReplacement(repl);
        break;
      case "add":
        Optional<String> val = repl.getStation();
        String stat = (val.orElse(null));

        if (stat == null) {
          logger.error("Need station name for this operation. Please check in configuration.json");
          result = false;
        } else {
          result = doAddition(repl, stat);
        }
        break;
      default:
        result = false;
    }

    return result;

  }

  private boolean doFileReplacement(Replacement replacement) {
    var isResult = false;

    var manipulateThisFile = new File(replacement.getFileName());
    var originalFile = new File(this.getClass().getClassLoader().getResource(replacement.getOriginalEntry()).getPath());
    var replacementFile = new File(this.getClass().getClassLoader().getResource(replacement.getReplacementEntry()).getPath());

    ArrayList<String> maniFile = new ArrayList<>();
    ArrayList<String> origFile = new ArrayList<>();
    ArrayList<String> replaceFile = new ArrayList<>();

    //read in all three files, so we can find the information.
    try (var scanner = new Scanner(manipulateThisFile); var scanner1 = new Scanner(originalFile);
         var scanner2 = new Scanner(replacementFile)) {
      while (scanner.hasNextLine()) {
        maniFile.add(scanner.nextLine());
      }
      while (scanner1.hasNextLine()) {
        origFile.add(scanner1.nextLine());
      }
      while (scanner2.hasNextLine()) {
        replaceFile.add(scanner2.nextLine());
      }
      //Compare...
      if (maniFile.containsAll(origFile)) {
        //we have a match!
        //where is the match?
        int indexStart = maniFile.indexOf(origFile.get(0));
        int indexEnd = maniFile.indexOf(origFile.get(origFile.size() - 1));

        //small sanity check to make sure indexes line up and there is nothing in-between...
        if ((indexEnd - indexStart) == origFile.size() - 1) {
          //this should be good...
          maniFile.removeAll(origFile);
          maniFile.addAll(indexStart, replaceFile);
          isResult = true;
        }
      }

      //write out the new file...
      writeFile(manipulateThisFile, maniFile, true);

    } catch (FileNotFoundException ex) {
      logger.error("doFileReplacement in replacement: FileNotFound Exception occurred. File: {}",
        manipulateThisFile.getName());
    }
    return isResult;
  }

  private boolean doAddition(Replacement replacement, String station) {
    var isResult = false;
    var manipulateThis = new File(String.format("%s/%s/%s", scanDir.getPath(),
      station, replacement.getFileName()));
    var original = replacement.getOriginalEntry();
    var replace = replacement.getReplacementEntry();

    try (var scanner = new Scanner(manipulateThis)) {
      ArrayList<String> currentFile = new ArrayList<>();
      var line = "";
      while (scanner.hasNextLine()) {
        line = scanner.nextLine();
        if (line.contains(original)) {
          //make a copy of it and place both in the ArrayList
          //need to make sure replacements are same length, if it is a .dat file, for example...
          while (original.length() < replace.length()) {
            original = String.format("%s ", original);
          }
          //or, if other way around...
          while (replace.length() < original.length()) {
            replace = String.format("%s ", replace);
          }
          var line2 = line.replace(original, replace);
          currentFile.add(line2);
        }
      }
      //Now that we gathered what to add, let's rewrite it...
      writeFile(manipulateThis, currentFile, true);

    } catch (FileNotFoundException ex) {
      logger.error("doAddition in replacement: FileNotFound Exception occurred", ex);
    }
    return isResult;
  }


  /**
   * Set the type of the station.
   */
  protected void setType(ArrayList<String> types) {
    if (types.contains("ar")) {
      currentStat.setType("ar");
    } else {
      var type = "";
      //types should never be more than 2, according to the data...
      if (types.size() == 2) {
        type += types.get(0) + " + " + types.get(1);
      } else if (types.size() == 1) {
        type = types.get(0);
      }
      currentStat.setType(type);
    }
  }

  protected void writeNetworkDatFile() {
    var dateTime = ZonedDateTime.now();
    var formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    var line = String.format("ww   %-22s -1 %s %s",
      stationReferenceBuilderConfiguration.getCurrentImplementor(), formatter.format(dateTime), "12:00:00\n");
    //now build the network.dat file info...
    Collection<String> groupNameList = stationGroupBuilderConfiguration.getGroupNameList();
    var nodeFile = new File("network.dat");
    try (var tmp = new FileWriter(scanDir.getPath() + "/" + nodeFile.getName(), false)) {
      for (String name : groupNameList) {
        tmp.write(String.format("%-8s %-80s %s", name, stationGroupBuilderConfiguration.getGroup(name).getDescription(), line));
      }
      tmp.flush();
    } catch (IOException ex) {
      logger.error("writeNetworkDatFile: IOException occurred. File: {}", nodeFile.getName());
    }
  }


  protected Station getCurrentStat() {
    return currentStat;
  }

  protected void setCurrentStat(Station s) {
    currentStat = s;
  }


  protected URL getScanDir() {
    return scanDir;
  }

  //Convenience Methods
  protected HashMap<String, List<String>> getAllGroups() {
    return allGroups;
  }

}
