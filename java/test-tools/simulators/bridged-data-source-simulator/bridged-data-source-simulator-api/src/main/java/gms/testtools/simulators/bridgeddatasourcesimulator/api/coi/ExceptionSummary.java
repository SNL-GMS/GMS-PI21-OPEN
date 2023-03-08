package gms.testtools.simulators.bridgeddatasourcesimulator.api.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

@AutoValue
public abstract class ExceptionSummary {

  public abstract Instant getExceptionTime();

  public abstract String getExceptionType();

  public abstract String getMessage();

  @JsonCreator
  public static ExceptionSummary create(@JsonProperty("exceptionTime") Instant exceptionTime,
    @JsonProperty("exceptionType") String exceptionType,
    @JsonProperty("message") String message) {

    Preconditions.checkArgument(StringUtils.isNotBlank(exceptionType), "Exception type cannot be blank");
    Preconditions.checkArgument(StringUtils.isNotBlank(message), "Message cannot be blank");
    return new AutoValue_ExceptionSummary(exceptionTime, exceptionType, message);
  }
}


