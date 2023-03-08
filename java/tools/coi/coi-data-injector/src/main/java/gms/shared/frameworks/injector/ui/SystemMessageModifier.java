package gms.shared.frameworks.injector.ui;

import gms.core.performancemonitoring.ssam.control.datapublisher.SystemEvent;
import gms.shared.frameworks.injector.Modifier;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * System message modifier class called by the data injector to randomly create System Messages to
 * be displayed in the UI.
 */
public class SystemMessageModifier implements Modifier<Iterable<SystemEvent<SystemMessage>>> {
  private ArrayList<SystemMessageGenerator> systemMessageGenerators = new ArrayList<>();
  private SecureRandom random;

  private static class Config {
    private static final String BASE_FILE_PATH = "gms/shared/frameworks/injector/";
    static final String STATION_LIST_FILE_PATH = BASE_FILE_PATH + "StationGroupMap.json";
  }

  /* Constructor initialize station list and SystemMessageGenerator for each station */
  public SystemMessageModifier() {
    this.random = new SecureRandom();
    List<StationGroup> stationGroups =
      UiDataInjectorUtility.loadStationGroupsFromFile(Config.STATION_LIST_FILE_PATH);
    Set<Station> stations = UiDataInjectorUtility.getStationSet(stationGroups);
    for (Station station : stations) {
      systemMessageGenerators.add(new SystemMessageGenerator(station));
    }
  }

  /**
   * Called by data injector to generate System Messages.
   * Publishes the system messages to a Kafka topic.
   *
   * @param systemMessageList the number of these is how many we will produce
   * @return a list of SystemEvent wrapped messages. (messages are produced as a side effect)
   */
  @Override
  public List<SystemEvent<SystemMessage>> apply(Iterable<SystemEvent<SystemMessage>> systemMessageList) {
    List<SystemEvent<SystemMessage>> newSystemMessageMaterializedViewList = new ArrayList<>();
    var generator = systemMessageGenerators.get((int) (
      systemMessageGenerators.size() * this.random.nextDouble()));
    newSystemMessageMaterializedViewList.add(generator.getSystemMessage());
    return newSystemMessageMaterializedViewList;
  }
}
