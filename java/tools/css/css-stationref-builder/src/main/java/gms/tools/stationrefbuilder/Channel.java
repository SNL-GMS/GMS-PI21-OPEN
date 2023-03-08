package gms.tools.stationrefbuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.util.Optional;

@AutoValue
@JsonSerialize(as = Channel.class)
@JsonDeserialize(builder = AutoValue_Channel.Builder.class)
public abstract class Channel {

  public abstract Optional<Integer> getSampleRate();

  public abstract String getChannelName();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_Channel.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    protected abstract Channel autoBuild();

    public abstract Builder setSampleRate(int sampleRate);

    public abstract Builder setChannelName(String channelName);

    public Channel build() {
      return autoBuild();
    }
  }
}
