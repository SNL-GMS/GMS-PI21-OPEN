package gms.shared.frameworks.osd.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@AutoValue
public abstract class ProcessingResult<S, T> {

  public abstract Optional<T> getResult();

  public abstract ImmutableList<RejectedInput<S>> getRejectedInputs();

  @JsonIgnore
  public Stream<RejectedInput<S>> getRejectedInputsStream() {
    return getRejectedInputs().stream();
  }

  public static <S, T> ProcessingResult.Builder<S, T> builder() {
    return new AutoValue_ProcessingResult.Builder<>();
  }

  @JsonCreator
  public static <S, T> ProcessingResult<S, T> from(
    @JsonProperty("result") Optional<T> result,
    @JsonProperty("rejectedInputs") List<RejectedInput<S>> rejectedInputs) {
    return ProcessingResult.<S, T>builder()
      .setRejectedInputs(rejectedInputs)
      .setResult(result)
      .build();
  }

  @AutoValue.Builder
  public abstract static class Builder<S, T> {

    public abstract Builder<S, T> setResult(T result);

    public abstract Builder<S, T> setResult(Optional<T> result);

    abstract Builder<S, T> setRejectedInputs(ImmutableList<RejectedInput<S>> rejectedInput);

    public Builder<S, T> setRejectedInputs(List<RejectedInput<S>> rejectedInput) {
      return setRejectedInputs(ImmutableList.copyOf(rejectedInput));
    }

    abstract ImmutableList.Builder<RejectedInput<S>> rejectedInputsBuilder();

    public Builder<S, T> addRejectedInput(RejectedInput<S> rejectedInput) {
      rejectedInputsBuilder().add(rejectedInput);
      return this;
    }

    public Builder<S, T> addRejectedInput(S input, String rationale) {
      return addRejectedInput(RejectedInput.create(input, rationale));
    }

    public abstract ProcessingResult<S, T> build();

  }

}
