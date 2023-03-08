package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DataPoint {

  public abstract DoubleOrInteger getStatus();

  public abstract long getTimeStamp();

  public static DataPoint.Builder builder() {
    return new AutoValue_DataPoint.Builder();
  }

  public abstract DataPoint.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract DataPoint.Builder setStatus(DoubleOrInteger status);

    public abstract DataPoint.Builder setTimeStamp(long timeStamp);

    abstract DataPoint autoBuild();

    public DataPoint build() {
      return autoBuild();
    }
  }

}
