package gms.shared.stationdefinition.coi.channel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


@AutoValue
@JsonSerialize(as = gms.shared.stationdefinition.coi.channel.ChannelId.class)
@JsonDeserialize(builder = AutoValue_ChannelId.Builder.class)
@JsonPropertyOrder(alphabetic = true)
public abstract class ChannelId {

  public static gms.shared.stationdefinition.coi.channel.ChannelId createEntityReference(String name) {
    return gms.shared.stationdefinition.coi.channel.ChannelId.builder()
      .setName(name)
      .build();
  }

  public static gms.shared.stationdefinition.coi.channel.ChannelId createVersionReference(String name,
    Instant effectiveAt) {
    Objects.requireNonNull(effectiveAt);
    return gms.shared.stationdefinition.coi.channel.ChannelId.builder()
      .setName(name)
      .setEffectiveAt(effectiveAt)
      .build();
  }

  public Channel toChannelEntityReference() {
    return Channel
      .createEntityReference(this.getName());
  }

  public Channel toChannelVersionReference() {
    final Optional<Instant> effectiveAt = this.getEffectiveAt();
    Preconditions.checkArgument(effectiveAt.isPresent(),
      "EffectiveAt is missing from the channel's version reference.");
    return Channel
      .createVersionReference(this.getName(), effectiveAt.get());
  }

  public abstract String getName();

  public abstract Optional<Instant> getEffectiveAt();

  public static gms.shared.stationdefinition.coi.channel.ChannelId.Builder builder() {
    return new AutoValue_ChannelId.Builder();
  }

  public abstract gms.shared.stationdefinition.coi.channel.ChannelId.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    gms.shared.stationdefinition.coi.channel.ChannelId.Builder setName(String name);

    String getName();

    default gms.shared.stationdefinition.coi.channel.ChannelId.Builder setEffectiveAt(Instant effectiveAt) {
      return setEffectiveAt(Optional.ofNullable(effectiveAt));
    }

    gms.shared.stationdefinition.coi.channel.ChannelId.Builder setEffectiveAt(Optional<Instant> effectiveAt);

    gms.shared.stationdefinition.coi.channel.ChannelId autoBuild();

    default gms.shared.stationdefinition.coi.channel.ChannelId build() {
      gms.shared.stationdefinition.coi.channel.ChannelId channel = autoBuild();
      Validate.notEmpty(channel.getName(), "Channel must be provided a name");
      return channel;
    }
  }
}
