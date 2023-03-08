package gms.dataacquisition.data.preloader;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

@AutoValue
@JsonSerialize(as = GenerationSpec.class)
@JsonDeserialize(builder = AutoValue_GenerationSpec.Builder.class)
public abstract class GenerationSpec {

  public abstract GenerationType getType();

  public abstract Instant getStartTime();

  public Instant getReceptionTime() {
    final var receptionCondition = getInitialConditions().get(InitialCondition.RECEPTION_DELAY);
    final var receptionDelay = Duration.parse(receptionCondition);
    return getStartTime().plus(receptionDelay);
  }

  public abstract Duration getSampleDuration();

  public abstract Duration getDuration();

  public abstract ImmutableMap<InitialCondition, String> getInitialConditions();

  public Stream<Entry<InitialCondition, String>> initialConditions() {
    return getInitialConditions().entrySet().stream();
  }

  public Optional<String> getInitialCondition(InitialCondition condition) {
    return Optional.ofNullable(getInitialConditions().get(condition));
  }

  public abstract ImmutableMap<InitialCondition, Double> getDurationStatusGeneratorParameters();

  public Stream<Entry<InitialCondition, Double>> durationGeneratorParameters() {
    return getDurationStatusGeneratorParameters().entrySet().stream();
  }

  public Optional<Double> getDurationStatusGeneratorParameter(InitialCondition condition) {
    return Optional.ofNullable(getDurationStatusGeneratorParameters().get(condition));
  }

  public abstract ImmutableMap<InitialCondition, Double> getPercentStatusGeneratorParameters();

  public Stream<Entry<InitialCondition, Double>> percentGeneratorParameters() {
    return getPercentStatusGeneratorParameters().entrySet().stream();
  }

  public Optional<Double> getPercentStatusGeneratorParameter(InitialCondition condition) {
    return Optional.ofNullable(getPercentStatusGeneratorParameters().get(condition));
  }

  public abstract ImmutableMap<InitialCondition, Object> getBooleanStatusGeneratorParameters();

  public Stream<Entry<InitialCondition, Object>> booleanGeneratorParameters() {
    return getBooleanStatusGeneratorParameters().entrySet().stream();
  }

  public Optional<Object> getBooleanStatusGeneratorParameter(InitialCondition condition) {
    return Optional.ofNullable(getBooleanStatusGeneratorParameters().get(condition));
  }

  public Duration getBatchDuration() {
    return Duration.ofNanos((long) getBatchSize() * getSampleDuration().toNanos());
  }

  public abstract int getBatchSize();

  public abstract Optional<AcquiredChannelEnvironmentIssueType> getAcquiredChannelEnvironmentIssueType();

  public abstract boolean getIsCd11Station();

  public abstract boolean getUseCuratedDataGeneration();

  public static Builder builder() {
    return new AutoValue_GenerationSpec.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    abstract Optional<Boolean> getUseCuratedDataGeneration();

    abstract Optional<Boolean> getIsCd11Station();

    public abstract Builder setBatchSize(int batchSize);

    public abstract Builder setType(GenerationType type);

    public abstract Builder setStartTime(Instant startTime);

    public abstract Builder setSampleDuration(Duration sampleDuration);

    public abstract Builder setDuration(Duration duration);

    abstract Builder setInitialConditions(ImmutableMap<InitialCondition, String> initialConditions);

    public Builder setInitialConditions(Map<InitialCondition, String> initialConditions) {
      return setInitialConditions(ImmutableMap.copyOf(initialConditions));
    }

    abstract ImmutableMap.Builder<InitialCondition, String> initialConditionsBuilder();

    public Builder addInitialCondition(InitialCondition condition, String value) {
      initialConditionsBuilder().put(condition, value);
      return this;
    }

    abstract Builder setDurationStatusGeneratorParameters(
      ImmutableMap<InitialCondition, Double> durationStatusGeneratorParameters);

    public Builder setDurationStatusGeneratorParameters(
      Map<InitialCondition, Double> durationStatusGeneratorParameters) {
      return setDurationStatusGeneratorParameters(
        ImmutableMap.copyOf(durationStatusGeneratorParameters));
    }

    abstract ImmutableMap.Builder<InitialCondition, Double> durationStatusGeneratorParametersBuilder();

    public Builder addDurationStatusGeneratorParameter(InitialCondition condition, Double value) {
      durationStatusGeneratorParametersBuilder().put(condition, value);
      return this;
    }

    abstract Builder setPercentStatusGeneratorParameters(
      ImmutableMap<InitialCondition, Double> percentStatusGeneratorParameters);

    public Builder setPercentStatusGeneratorParameters(
      Map<InitialCondition, Double> percentStatusGeneratorParameters) {
      return setPercentStatusGeneratorParameters(
        ImmutableMap.copyOf(percentStatusGeneratorParameters));
    }

    abstract ImmutableMap.Builder<InitialCondition, Double> percentStatusGeneratorParametersBuilder();

    public Builder addPercentStatusGeneratorParameter(InitialCondition condition, Double value) {
      percentStatusGeneratorParametersBuilder().put(condition, value);
      return this;
    }

    abstract Builder setBooleanStatusGeneratorParameters(
      ImmutableMap<InitialCondition, Object> booleanStatusGeneratorParameters);

    public Builder setBooleanStatusGeneratorParameters(
      Map<InitialCondition, Object> booleanStatusGeneratorParameters) {
      return setBooleanStatusGeneratorParameters(
        ImmutableMap.copyOf(booleanStatusGeneratorParameters));
    }

    abstract ImmutableMap.Builder<InitialCondition, Object> booleanStatusGeneratorParametersBuilder();

    public Builder addBooleanStatusGeneratorParameter(InitialCondition condition, Object value) {
      booleanStatusGeneratorParametersBuilder().put(condition, value);
      return this;
    }

    abstract Builder setAcquiredChannelEnvironmentIssueType(
      Optional<AcquiredChannelEnvironmentIssueType> acquiredChannelEnvironmentIssueType);

    public Builder setAcquiredChannelEnvironmentIssueType(
      AcquiredChannelEnvironmentIssueType acquiredChannelEnvironmentIssueType) {
      return setAcquiredChannelEnvironmentIssueType(
        Optional.of(acquiredChannelEnvironmentIssueType));
    }

    public abstract Builder setIsCd11Station(boolean isCd11Station);

    public abstract Builder setUseCuratedDataGeneration(boolean useCuratedDataGeneration);

    abstract GenerationSpec autoBuild();

    public GenerationSpec build() {
      if (getUseCuratedDataGeneration().isEmpty()) {
        setUseCuratedDataGeneration(false);
      }
      if (getIsCd11Station().isEmpty()) {
        setIsCd11Station(false);
      }

      return autoBuild();
    }
  }
}
