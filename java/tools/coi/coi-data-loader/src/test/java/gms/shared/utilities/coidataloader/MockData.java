package gms.shared.utilities.coidataloader;

import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.event.Event;
import gms.shared.frameworks.osd.coi.signaldetection.QcMask;
import gms.shared.frameworks.osd.coi.signaldetection.Response;
import gms.shared.frameworks.osd.coi.signaldetection.SignalDetection;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.coi.waveforms.FkSpectra;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
class MockData {

  private MockData() {
  }

  // have to set these up separately because you can't collect the same Stream multiple times
  private static final ChannelSegment<Waveform> SEGMENT_1 = mock(ChannelSegment.class);
  private static final ChannelSegment<Waveform> SEGMENT_2 = mock(ChannelSegment.class);
  static final Collection<ChannelSegment<Waveform>> SEGMENTS = List.of(SEGMENT_1, SEGMENT_2);
  static final ChannelSegment<FkSpectra> FK_SEGMENT_1 = mock(ChannelSegment.class);
  static final ChannelSegment<FkSpectra> FK_SEGMENT_2 = mock(ChannelSegment.class);
  static final Collection<ChannelSegment<FkSpectra>> FK_SEGMENTS = List.of(FK_SEGMENT_1, FK_SEGMENT_2);

  // setup responses separately because their mocking is more involved since they are found in a directory
  static final ReferenceResponse RESPONSE_1 = mock(ReferenceResponse.class);
  static final ReferenceResponse RESPONSE_2 = mock(ReferenceResponse.class);

  static CoiDataSet createDataSet() {
    return CoiDataSet.builder()
      .setStationReference(StationReference.builder()
        .setNetworks(mocks(ReferenceNetwork.class))
        .setStations(mocks(ReferenceStation.class))
        .setSites(mocks(ReferenceSite.class))
        .setChannels(mocks(ReferenceChannel.class))
        .setSensors(mocks(ReferenceSensor.class))
        .setResponses(List.of(RESPONSE_1, RESPONSE_2))
        .setNetworkMemberships(mocks(ReferenceNetworkMembership.class))
        .setStationMemberships(mocks(ReferenceStationMembership.class))
        .setSiteMemberships(mocks(ReferenceSiteMembership.class))
        .build())
      .setStationGroups(mocks(StationGroup.class))
      .setResponses(mocks(Response.class))
      .setEvents(mocks(Event.class))
      .setSignalDetections(mocks(SignalDetection.class))
      .setMasks(mocks(QcMask.class))
      .setWaveforms(Stream.of(SEGMENT_1, SEGMENT_2))
      .setFks(Stream.of(FK_SEGMENT_1, FK_SEGMENT_2))
      .build();
  }

  private static <T> Collection<T> mocks(Class<T> c) {
    return List.of(mock(c), mock(c), mock(c));
  }
}
