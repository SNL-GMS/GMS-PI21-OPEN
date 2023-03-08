package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AutoValue
@JsonSerialize(as = ReferenceChannelRequest.class)
@JsonDeserialize(builder = AutoValue_ReferenceChannelRequest.Builder.class)
public abstract class ReferenceChannelRequest {

  public abstract Optional<List<String>> getChannelNames();

  public abstract Optional<List<UUID>> getEntityIds();

  public abstract Optional<List<UUID>> getVersionIds();

  public static ReferenceChannelRequest.Builder builder() {
    return new AutoValue_ReferenceChannelRequest.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    abstract ReferenceChannelRequest.Builder setChannelNames(ImmutableList<String> channelNames);

    public ReferenceChannelRequest.Builder setChannelNames(Collection<String> channelNames) {
      return setChannelNames(ImmutableList.copyOf(channelNames));
    }

    public ReferenceChannelRequest.Builder setChannelNames(String... channelNames) {
      return setChannelNames(ImmutableList.copyOf(channelNames));
    }

    abstract ReferenceChannelRequest.Builder setEntityIds(ImmutableList<UUID> entityIds);

    public ReferenceChannelRequest.Builder setEntityIds(Collection<UUID> entityIds) {
      return setEntityIds(ImmutableList.copyOf(entityIds));
    }

    public ReferenceChannelRequest.Builder setEntityIds(UUID... entityIds) {
      return setEntityIds(ImmutableList.copyOf(entityIds));
    }

    abstract ReferenceChannelRequest.Builder setVersionIds(ImmutableList<UUID> versionIds);

    public ReferenceChannelRequest.Builder setVersionIds(Collection<UUID> versionIds) {
      return setVersionIds(ImmutableList.copyOf(versionIds));
    }

    public ReferenceChannelRequest.Builder setVersionIds(UUID... versionIds) {
      return setVersionIds(ImmutableList.copyOf(versionIds));
    }

    public abstract ReferenceChannelRequest build();
  }
}
