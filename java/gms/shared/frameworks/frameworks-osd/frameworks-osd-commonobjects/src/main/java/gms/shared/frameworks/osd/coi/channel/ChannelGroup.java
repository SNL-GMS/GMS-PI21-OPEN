package gms.shared.frameworks.osd.coi.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;

@AutoValue
public abstract class ChannelGroup {

  public enum Type {
    PROCESSING_GROUP,
    SITE_GROUP
  }

  public abstract String getName();

  public abstract String getDescription();

  public abstract Optional<Location> getLocation();

  public abstract ChannelGroup.Type getType();

  public abstract NavigableSet<Channel> getChannels();

  @JsonCreator
  public static ChannelGroup from(
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("location") Location location,
    @JsonProperty("type") ChannelGroup.Type type,
    @JsonProperty("channels") List<Channel> channels) {
    Validate.notEmpty(name, "Channel Group name must not be null");
    Validate.notEmpty(channels, "ChannelGroup must have a non-zero number of channels");

    NavigableSet<Channel> orderedChannels = new TreeSet<>(Comparator.comparing(Channel::getName));
    orderedChannels.addAll(channels);

    return new AutoValue_ChannelGroup(
      name, description, Optional.ofNullable(location), type, Collections.unmodifiableNavigableSet(orderedChannels));
  }
}
