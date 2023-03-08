package gms.shared.frameworks.osd.api;

import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.osd.api.channel.ChannelRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.CapabilitySohRollupRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.PerformanceMonitoringRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.SohStatusChangeRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AcquiredChannelEnvironmentIssueRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AcquiredChannelEnvironmentIssueRepositoryQueryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.RawStationDataFrameRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.RawStationDataFrameRepositoryQueryInterface;
import gms.shared.frameworks.osd.api.station.StationGroupRepositoryInterface;
import gms.shared.frameworks.osd.api.station.StationRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceChannelRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceNetworkRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceResponseRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceSensorRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceSiteRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceStationRepositoryInterface;
import gms.shared.frameworks.osd.api.systemmessage.SystemMessageRepositoryInterface;

import javax.ws.rs.Path;

@Component("osd")
@Path("/frameworks-osd-service/osd")
public interface OsdRepositoryInterface extends
  CapabilitySohRollupRepositoryInterface,
  ChannelRepositoryInterface,
  PerformanceMonitoringRepositoryInterface,
  RawStationDataFrameRepositoryInterface,
  RawStationDataFrameRepositoryQueryInterface,
  ReferenceChannelRepositoryInterface,
  ReferenceNetworkRepositoryInterface,
  ReferenceResponseRepositoryInterface,
  ReferenceSensorRepositoryInterface,
  ReferenceSiteRepositoryInterface,
  ReferenceStationRepositoryInterface,
  SohStatusChangeRepositoryInterface,
  StationGroupRepositoryInterface,
  StationRepositoryInterface,
  AcquiredChannelEnvironmentIssueRepositoryInterface,
  AcquiredChannelEnvironmentIssueRepositoryQueryInterface,
  SystemMessageRepositoryInterface {

}
