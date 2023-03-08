package gms.core.performancemonitoring.ssam.control.processor;

import gms.core.performancemonitoring.ssam.control.SohPackage;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.core.performancemonitoring.uimaterializedview.SohQuietAndUnacknowledgedCacheManager;
import gms.core.performancemonitoring.uimaterializedview.UIStationAndStationGroupsChanges;
import gms.core.performancemonitoring.uimaterializedview.UiStationAndStationGroupGenerator;
import gms.core.performancemonitoring.uimaterializedview.UiStationAndStationGroups;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.function.Function;

public interface MaterializedViewProcessor
  extends Function<SohPackage, List<UiStationAndStationGroups>> {

  static MaterializedViewProcessor create(
    SohQuietAndUnacknowledgedCacheManager quietAndUnackListsManager,
    StationSohMonitoringUiClientParameters stationSohConfig,
    List<StationGroup> stationGroups,
    Sinks.Many<SystemMessage> systemMessageFluxSink) {

    return sohPackage -> {

      var unacknowledgedStatusChanges = quietAndUnackListsManager
        .getUnacknowledgedList();

      var quietedSohStatusChanges = quietAndUnackListsManager
        .getQuietedSohStatusChanges();

      var uiStationAndStationGroupsChangesData = UIStationAndStationGroupsChanges.builder()
        .setUnacknowledgedStatusChanges(unacknowledgedStatusChanges)
        .setQuietedSohStatusChanges(quietedSohStatusChanges)
        .build();

      // TRUE indicates the UI should immediately redraw the SOH UI.
      // FALSE indicates the UI will batch the List of UiStationAndStationGroups before redraw.
      final var IS_UPDATE = false;

      //update the unacknowledgedList
      quietAndUnackListsManager.updateUnacknowledgedSet(sohPackage.getStationSohs());

      return UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
        sohPackage.getStationSohs(),
        uiStationAndStationGroupsChangesData,
        sohPackage.getCapabilitySohRollups(),
        stationSohConfig,
        stationGroups,
        IS_UPDATE,
        systemMessageFluxSink
      );
    };

  }
}
