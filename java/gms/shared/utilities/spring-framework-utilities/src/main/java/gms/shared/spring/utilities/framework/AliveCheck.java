package gms.shared.spring.utilities.framework;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

/**
 * Constructs a AliveCheck object
 */
@AutoValue
@JsonSerialize(as = AliveCheck.class)
@JsonDeserialize(builder = AutoValue_AliveCheck.Builder.class)
public abstract class AliveCheck {

  public abstract String getAliveAt();

  public static AliveCheck.Builder builder() {
    return new AutoValue_AliveCheck.Builder();
  }

  public abstract AliveCheck.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    AliveCheck.Builder setAliveAt(String timeInstant);

    AliveCheck autobuild();

    default AliveCheck build() {
      var aliveCheck = autobuild();

      Preconditions.checkNotNull(aliveCheck.getAliveAt());
      Preconditions.checkState(!aliveCheck.getAliveAt().isEmpty());
      Preconditions.checkState(!aliveCheck.getAliveAt().isBlank());

      return aliveCheck;
    }

  }

}