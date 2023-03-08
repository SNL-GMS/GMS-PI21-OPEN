package gms.tools.stationrefbuilder;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * The main class for the Station MetaData Auto-Manipulator. Expecting no inputs arguments this
 * program assumes:
 * 1. The directory where the meta data is kept will be "meta/"
 * 2. The file where the configuration is kept will be called "configuration.json"
 * 3. The file where the station group rules are kept will be called "stationgrouprules.json"
 */
public class StationMetaDataManipulator {

  public static void main(String[] args) {
    args = loadArgs(args);

    String scanDirName = args[0];
    String generalConfFileName = args[1];
    String groupConfFileName = args[2];
    String outputDir = args[3];

    var scanFile = new File(scanDirName);
    var confFile = new File(generalConfFileName);
    var groupConfFile = new File(groupConfFileName);
    var outPutDirLoc = new File(outputDir);

    if (!scanFile.exists()) {
      JOptionPane.showMessageDialog(null, "Could not find the \"data\" directory, cannot continue");
      return;
    }
    if (!confFile.exists()) {
      JOptionPane.showMessageDialog(null, "Could not find the configuration.json file, cannot continue");
      return;
    }
    if (!groupConfFile.exists()) {
      JOptionPane.showMessageDialog(null, "Could not find the stationgrouprules.json file, cannot continue");
      return;
    }
    if (!outPutDirLoc.exists()) {
      JOptionPane.showMessageDialog(null, "Could not find the output directory, cannot continue");
    }

    var stations = new StationManipulator();
    try {
      stations.init(
        scanFile.toURI().toURL(),
        confFile.toURI().toURL(),
        groupConfFile.toURI().toURL(),
        outPutDirLoc.toURI().toURL());
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, "Error Processing: " + e);
    }
  }

  private static String[] loadArgs(String[] args) {
    List<String> argList = Arrays.asList(args);
    if (argList.isEmpty()) {
      argList.add(
        getValueFromDialog(
          "Please enter a Scan Directory Name (give the full path)"));
    }
    if (argList.size() == 1) {
      argList.add(
        getValueFromDialog(
          "Please enter the location of the configuration.json file, ending with /"));
      if (!argList.get(1).endsWith("json")) {
        argList.set(1, argList.get(1) + "configuration.json");
      }
    }
    if (argList.size() == 2) {
      argList.add(
        getValueFromDialog(
          "Please enter the stationgrouprules.json File Location, ending with /"));
      if (!argList.get(2).endsWith("json")) {
        argList.set(2, argList.get(1) + "stationgrouprules.json");
      }
    }
    if (argList.size() == 3) {
      argList.add(
        getValueFromDialog(
          "Please enter the directory you would like to save the output files into."));
    }
    return (argList.toArray(String[]::new));
  }

  static String getValueFromDialog(String message) {
    return JOptionPane.showInputDialog(null, message);
  }
}
