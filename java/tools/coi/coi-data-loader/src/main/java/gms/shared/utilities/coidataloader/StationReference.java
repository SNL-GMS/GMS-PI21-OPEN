package gms.shared.utilities.coidataloader;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;

import java.util.Collection;
import java.util.List;

@AutoValue
public abstract class StationReference {

  public abstract Collection<ReferenceNetwork> getNetworks();

  public abstract Collection<ReferenceStation> getStations();

  public abstract Collection<ReferenceSite> getSites();

  public abstract Collection<ReferenceChannel> getChannels();

  public abstract Collection<ReferenceSensor> getSensors();

  public abstract Collection<ReferenceResponse> getResponses();

  public abstract Collection<ReferenceNetworkMembership> getNetworkMemberships();

  public abstract Collection<ReferenceStationMembership> getStationMemberships();

  public abstract Collection<ReferenceSiteMembership> getSiteMemberships();

  public static Builder builder() {
    return new AutoValue_StationReference.Builder()
      .setNetworks(List.of())
      .setStations(List.of())
      .setSites(List.of())
      .setChannels(List.of())
      .setSensors(List.of())
      .setResponses(List.of())
      .setNetworkMemberships(List.of())
      .setStationMemberships(List.of())
      .setSiteMemberships(List.of());
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setNetworks(Collection<ReferenceNetwork> networks);

    public abstract Builder setStations(Collection<ReferenceStation> c);

    public abstract Builder setSites(Collection<ReferenceSite> c);

    public abstract Builder setChannels(Collection<ReferenceChannel> c);

    public abstract Builder setSensors(Collection<ReferenceSensor> c);

    public abstract Builder setResponses(Collection<ReferenceResponse> c);

    public abstract Builder setNetworkMemberships(Collection<ReferenceNetworkMembership> c);

    public abstract Builder setStationMemberships(Collection<ReferenceStationMembership> c);

    public abstract Builder setSiteMemberships(Collection<ReferenceSiteMembership> c);

    public abstract StationReference build();
  }
}
