package gms.tools.stationrefbuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class handles the creation and manipulation of the required JSON files, cd11.json and
 * default.json that list all stations and attributes.
 */
public class JSONManager {

  private static final Logger logger = LoggerFactory.getLogger(JSONManager.class);

  private static final String PH1 = "PLACEHOLDER1";
  private static final String PH2 = "PLACEHOLDER2";

  private final StationReferenceBuilderConfiguration stationReferenceBuilderConfiguration;

  public JSONManager(StationReferenceBuilderConfiguration conf) {
    stationReferenceBuilderConfiguration = conf;
  }

  /**
   * Write the JSON file for the station information... Each station entry replaces two
   * placeholders, the first for the station name and the second for the port offset, which starts
   * at 0 and is incremented for each entry.
   */
  protected void writeJSON(ArrayList<Station> stations, String outDir) {
    var jsonFile = new File(outDir, stationReferenceBuilderConfiguration.getDefaultJsonFilename());
    try (var fw = new FileWriter(jsonFile)) {
      var counter = 0;
      var stringBuilder = new StringBuilder();
      stringBuilder.append("{\n");
      stringBuilder.append("    \"name\" : \"station-acquisition-config\",\n");
      stringBuilder.append("    \"constraints\" : [ {\n");
      stringBuilder.append("        \"constraintType\" : \"DEFAULT\"\n");
      stringBuilder.append("    } ],\n");
      stringBuilder.append("    \"parameters\" : {\n");
      stringBuilder.append("        \"stations\" : [ {\n");

      for (Station station : stations) {
        station.setPortOffset(counter++);
        stringBuilder.append(String.format(
          "            \"acquired\" : %s,\n" +
            "            \"portOffset\" : %d,\n" +
            "            \"frameProcessingDisabled\" : %s,\n" +
            "            \"stationName\" : \"%s\"\n" +
            "        }, {\n", station.isAcquired(), station.getPortOffset(),
          station.isFrameProcessingDisable(), station.getStationName()));

      }
      stringBuilder.delete(stringBuilder.length() - 4, stringBuilder.length());
      stringBuilder.append(" ]\n    }\n}");
      fw.write(stringBuilder.toString());
      fw.flush();

    } catch (IOException e) {
      logger.error(
        "Could not generate JSON File: {}.",
        stationReferenceBuilderConfiguration.getDefaultJsonFilename());
    }
  }

  /**
   * Write the Channel Reference Data JSON File. Each entry replaces one "Placeholder" tag.
   * Currently, the JSON file echoes key:value as the same, so the same placeholder replaces both.
   * The exception are the stations that need site id, like 00, those have a second placeholder.
   */
  protected void writeChannelRefJSON(ArrayList<Station> stations, String outDir) {
    //write the JSON file for the station offset... make this a JsonNode, once in gms-common...
    var jsonFile = new File(outDir, stationReferenceBuilderConfiguration.getCd11JsonFilename());


    try (var fw = new FileWriter(
      jsonFile)) {
      var buildString = new StringBuilder();
      buildString.append("{\n");
      buildString.append("    \"name\": \"cd-1.1\",\n");
      buildString.append("    \"constraints\": [\n");
      buildString.append("        {\n");
      buildString.append("            \"constraintType\": \"STRING\",\n");
      buildString.append("            \"criterion\": \"protocol\",\n");
      buildString.append("            \"operator\": {\n");
      buildString.append("                \"type\": \"EQ\",\n");
      buildString.append("                \"negated\": false\n");
      buildString.append("            },\n");
      buildString.append("            \"value\": [\n");
      buildString.append("                \"CD11\"\n");
      buildString.append("            ]\n");
      buildString.append("        }\n");
      buildString.append("    ],\n");
      buildString.append("    \"parameters\": {\n");
      buildString.append("        \"channelIdsByPacketName\": {\n");
      var jsonString2 = "            \"" + PH1 + "\": \"" + PH1 + "\"";
      var jsonString4 = "            \"" + PH2 + "\": \"" + PH1 + "\"";
      var jsonString3 = "\n        }\n    }\n}";
      fw.write(buildString.toString());
      for (Iterator<Station> iterator = stations.iterator(); iterator.hasNext(); ) {
        Station temp = iterator.next();
        if (stationReferenceBuilderConfiguration.getReplaceZeroes()
          && ("MiniSEED".equals(temp.getFormat()) || "".equals(temp.getFormat()))) {
          fw.write(replaceJSONString(jsonString4, temp, true));
        } else {
          fw.write(replaceJSONString(jsonString2, temp, false));
        }
        if (iterator.hasNext()) {
          fw.write(",\n");
        }
        fw.flush();
      }
      fw.write(jsonString3);
      fw.flush();
    } catch (IOException e) {
      logger.error(
        "Could not generate JSON File: {}.",
        stationReferenceBuilderConfiguration.getCd11JsonFilename());
    }
  }

  /**
   * Replace placeholder in the JSON String with information passed in - station name
   *
   * @param js - the JSON String
   * @param stat - The Station
   * @return the corrected JSON String
   */
  private String replaceJSONString(String js, Station stat, boolean replace) {
    var bld = new StringBuilder();
    for (Iterator<Channel> iterator = stat.getChannels().iterator(); iterator.hasNext(); ) {
      String temp = iterator.next().getChannelName();
      String built = js.replace(PH1, temp);
      if (replace) {
        String temp2 = temp.replace("00.", ".");
        built = built.replace(PH2, temp2);
      }
      bld.append(built);
      if (iterator.hasNext()) {
        bld.append(",\n");
      }
    }
    return bld.toString();
  }

  protected void writeUIStationGroupFile(StationGroupBuilderConfiguration stationGroupBuilderConfiguration,
    String outDir) {
    var jsonFile = new File(outDir, stationReferenceBuilderConfiguration.getUiStationGroupFilename());
    try (var fw = new FileWriter(jsonFile)) {
      var jsonString =
        "{\n"
          + "  \"name\": \"station-group-names-default\",\n"
          + "  \"constraints\": [\n"
          + "    {\n"
          + "      \"constraintType\": \"DEFAULT\"\n"
          + "    }\n"
          + "  ],\n"
          + "  \"parameters\": {\n"
          + "    \"stationGroupNames\": [\n";

      var jsonEndString = "    ]\n" + "  }\n" + "}\n";

      fw.write(jsonString);
      var buildString = new StringBuilder();

      ArrayList<String> stationGroups = new ArrayList<>(stationGroupBuilderConfiguration.getGroupNameList());

      for (var stationGroup : stationGroups) {
        buildString.append(String.format("      \"%s\",%n", stationGroup));
      }
      buildString.deleteCharAt(buildString.lastIndexOf(","));
      fw.write(buildString.toString());
      fw.write(jsonEndString);
      fw.flush();
    } catch (IOException ex) {
      logger.error("Error printing UI Station Group File.");
    }
  }

  protected void writeGlobalMonitoringOrganizationFile(String outDir) {
    var jsonFile = new File(outDir, stationReferenceBuilderConfiguration.getMonitoringOrgFilename());
    try (var fw = new FileWriter(jsonFile)) {
      var jsonString =
        "{\n"
          + "  \"name\": \"monitoring-org-default\",\n"
          + "  \"constraints\": [\n"
          + "    {\n"
          + "      \"constraintType\": \"DEFAULT\"\n"
          + "    }\n"
          + "  ],\n"
          + "  \"parameters\": {\n"
          + "    \"monitoringOrganization\": \""
          + stationReferenceBuilderConfiguration.getMonitoringOrganization()
          + "\"\n"
          + "  }\n"
          + "}";
      fw.write(jsonString);
      fw.flush();
    } catch (IOException ex) {
      logger.error("Error printing Global MonitoringOrganization File.");
    }
  }

  /**
   * Gets an ObjectMapper for use in JSON serialization. This ObjectMapper can serialize/deserialize
   * any COI object, and has common modules registered such as for Java 8 Instant.
   *
   * @return an ObjectMapper for use with JSON
   */
  public static ObjectMapper getJsonObjectMapper() {
    return configureObjectMapper(new ObjectMapper());
  }

  private static ObjectMapper configureObjectMapper(ObjectMapper objMapper) {
    return objMapper.findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
      .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

}
