package gms.shared.frameworks.injector;

import gms.core.performancemonitoring.ssam.control.datapublisher.SystemEvent;
import gms.shared.frameworks.injector.ui.StationSohModifier;
import gms.shared.frameworks.injector.ui.SystemMessageModifier;
import gms.shared.frameworks.injector.ui.UiStationAndStationGroupsModifier;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;

public enum InjectableType {

  ACEI_ID(AcquiredChannelEnvironmentIssue.class, AceiIdModifier.class),
  RAW_STATION_DATA_FRAME(RawStationDataFrame.class, DefaultModifier.class),
  RAW_STATION_DATA_FRAME_ID(RawStationDataFrame.class, RsdfIdModifier.class),
  STATION_SOH(StationSoh.class, StationSohModifier.class),
  SYSTEM_MESSAGE(SystemEvent.class, SystemMessageModifier.class),
  STATION_SOH_ID(StationSoh.class, StationSohIdModifier.class),
  UI_STATION_AND_STATION_GROUPS(SystemEvent.class, UiStationAndStationGroupsModifier.class),
  SOH_STATUS_CHANGE_EVENT(UnacknowledgedSohStatusChange.class, DefaultModifier.class),
  SOH_QUIETING_LIST(QuietedSohStatusChange.class, DefaultModifier.class),
  CAPABILITY_SOH_ROLLUP(CapabilitySohRollup.class, DefaultModifier.class);

  private final Class baseClass;
  private final Class<? extends Modifier> modifier;

  InjectableType(Class baseClass, Class<? extends Modifier> modifier) {
    this.baseClass = baseClass;
    this.modifier = modifier;
  }

  public Class getBaseClass() {
    return baseClass;
  }

  public Class<? extends Modifier> getModifier() {
    return modifier;
  }
}
