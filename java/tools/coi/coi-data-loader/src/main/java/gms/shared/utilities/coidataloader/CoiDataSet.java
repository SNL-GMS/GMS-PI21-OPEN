package gms.shared.utilities.coidataloader;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.event.Event;
import gms.shared.frameworks.osd.coi.signaldetection.QcMask;
import gms.shared.frameworks.osd.coi.signaldetection.Response;
import gms.shared.frameworks.osd.coi.signaldetection.SignalDetection;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.waveforms.FkSpectra;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@AutoValue
public abstract class CoiDataSet {

  public abstract StationReference getStationReference();

  public abstract Collection<StationGroup> getStationGroups();

  public abstract Collection<Response> getResponses();

  public abstract Collection<Event> getEvents();

  public abstract Collection<SignalDetection> getSignalDetections();

  public abstract Collection<QcMask> getMasks();

  public abstract Stream<ChannelSegment<Waveform>> getWaveforms();

  public abstract Stream<ChannelSegment<FkSpectra>> getFks();

  public static Builder builder() {
    return new AutoValue_CoiDataSet.Builder()
      .setStationReference(StationReference.builder().build())
      .setStationGroups(List.of())
      .setResponses(List.of())
      .setEvents(List.of())
      .setSignalDetections(List.of())
      .setMasks(List.of())
      .setWaveforms(Stream.of())
      .setFks(Stream.of());
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setStationReference(StationReference stationRef);

    public abstract Builder setStationGroups(Collection<StationGroup> stationGroups);

    public abstract Builder setResponses(Collection<Response> responses);

    public abstract Builder setEvents(Collection<Event> events);

    public abstract Builder setSignalDetections(Collection<SignalDetection> dets);

    public abstract Builder setMasks(Collection<QcMask> masks);

    public abstract Builder setWaveforms(Stream<ChannelSegment<Waveform>> waveforms);

    public abstract Builder setFks(Stream<ChannelSegment<FkSpectra>> fks);

    public abstract CoiDataSet build();
  }

}
