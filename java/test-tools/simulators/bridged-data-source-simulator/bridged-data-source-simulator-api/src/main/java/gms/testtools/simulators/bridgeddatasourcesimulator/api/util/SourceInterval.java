package gms.testtools.simulators.bridgeddatasourcesimulator.api.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Instant;

@AutoValue
@JsonSerialize(as = AutoValue_SourceInterval.class)
@JsonDeserialize(builder = AutoValue_SourceInterval.Builder.class)
public abstract class SourceInterval {

  public abstract long getIntervalIdentifier();

  public abstract String getType();

  public abstract String getName();

  public abstract double getTime();

  public abstract double getEndTime();

  public abstract String getState();

  public abstract String getAuthor();

  public abstract double getPercentAvailable();

  public abstract Instant getProcessStartDate();

  public abstract Instant getProcessEndDate();

  public abstract Instant getLastModificationDate();

  public abstract Instant getLoadDate();

  public static SourceInterval.Builder getBuilder() {
    return new AutoValue_SourceInterval.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    SourceInterval.Builder setIntervalIdentifier(long identifier);

    SourceInterval.Builder setType(String type);

    SourceInterval.Builder setName(String name);

    SourceInterval.Builder setTime(double time);

    SourceInterval.Builder setEndTime(double endTime);

    SourceInterval.Builder setState(String state);

    SourceInterval.Builder setAuthor(String author);

    SourceInterval.Builder setPercentAvailable(double percentAvailable);

    SourceInterval.Builder setProcessStartDate(Instant processingDate);

    SourceInterval.Builder setProcessEndDate(Instant endDate);

    SourceInterval.Builder setLastModificationDate(Instant lastModificationDate);

    SourceInterval.Builder setLoadDate(Instant loadDate);

    SourceInterval autoBuild();

    default SourceInterval build() {
      return autoBuild();
    }
  }
}
