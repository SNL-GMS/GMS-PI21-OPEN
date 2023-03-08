package gms.shared.event.coi.featureprediction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.FeaturePredictionValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Heterogeneous container for FeaturePredictions. The purpose is to hide the heterogeneous nature
 * of a collection of different FeaturePredictions, to avoid using wild cards in
 * public method return types.
 */
public class FeaturePredictionContainer {

  private final Multimap<FeaturePredictionType<?>, FeaturePrediction<?>> featureMeasurementTypeFeaturePredictionMap;

  private FeaturePredictionContainer(
    Multimap<FeaturePredictionType<?>, FeaturePrediction<?>> featureMeasurementTypeFeaturePredictionMap
  ) {
    this.featureMeasurementTypeFeaturePredictionMap = featureMeasurementTypeFeaturePredictionMap;
  }

  @JsonCreator
  public static FeaturePredictionContainer create(
    @JsonProperty("featurePredictions")
    Collection<FeaturePrediction<? extends FeaturePredictionValue<?, ?, ?>>> featurePredictions) {

    var mapBuilder
      = ImmutableMultimap.<FeaturePredictionType<?>, FeaturePrediction<?>>builder();

    featurePredictions.forEach(
      featurePrediction -> mapBuilder.put(featurePrediction.getPredictionType(), featurePrediction));

    return new FeaturePredictionContainer(mapBuilder.build());

  }

  public static FeaturePredictionContainer of(FeaturePrediction<?>... featurePredictions) {
    return create(Arrays.asList(featurePredictions));
  }

  /**
   * Get the FeaturePrediction from the container that has the given FeaturePredictionType.
   *
   * @param featurePredictionType Type of the FeaturePrediction to retrieve
   * @param <T> Class that extend FeaturePredictionValue, which is tightly matched to FeaturePredictionType/
   * @return The FeaturePrediction with the given type.
   */
  @JsonIgnore
  public <T extends FeaturePredictionValue<?, ?, ?>> Set<FeaturePrediction<T>> getFeaturePredictionsForType(
    FeaturePredictionType<T> featurePredictionType) {

    //
    // We need to cast element-wise in the returned collection, because
    // (Set<FeaturePrediction<T>>) featureMeasurementTypeFeaturePredictionMap.get(featurePredictionType)
    // will not work.
    //
    return featureMeasurementTypeFeaturePredictionMap.get(featurePredictionType).stream()
      //
      // Here, if we try casting via .map(fp -> (FeaturePrediction<T>) fp), SonarQube complains
      // that we are not using FeaturePrediction.class::cast method reference. Unfortunately, that
      // does not work, because type erasure removes the type T from the casted object. So, a separate
      // method is used to do the cast.
      //
      .map(FeaturePredictionContainer::<T>castBeforeErasure)
      .collect(Collectors.toSet());

  }

  /**
   * Check if this container contains the provided feature prediction.
   *
   * @param featurePrediction The FeaturePrediction to look for
   * @return True if the container has the prediciton, false otherwise.
   */
  public boolean contains(FeaturePrediction<?> featurePrediction) {
    return featureMeasurementTypeFeaturePredictionMap.containsValue(featurePrediction);
  }

  public boolean anyMatch(Predicate<FeaturePrediction<?>> predicate) {
    return featureMeasurementTypeFeaturePredictionMap.values().stream().anyMatch(predicate);
  }

  public <R> Stream<R> map(Function<? super FeaturePrediction<?>, ? extends R> mapper) {
    return featureMeasurementTypeFeaturePredictionMap.values().stream().map(mapper);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FeaturePredictionContainer)) {
      return false;
    }
    FeaturePredictionContainer that = (FeaturePredictionContainer) o;
    return Objects.equals(featureMeasurementTypeFeaturePredictionMap,
      that.featureMeasurementTypeFeaturePredictionMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(featureMeasurementTypeFeaturePredictionMap);
  }

  /**
   * Combines this container and otherContainer to a new FeaturePredictionContainer
   *
   * @param otherContainer to container to combine
   * @return the newly combined FeaturePredictionContainer
   */
  public FeaturePredictionContainer union(FeaturePredictionContainer otherContainer) {
    var copyOfFeaturePredictions = new HashSet<>(this.getFeaturePredictions());
    copyOfFeaturePredictions.addAll(otherContainer.getFeaturePredictions());
    return create(copyOfFeaturePredictions);
  }

  /**
   * Get the collection of all of our FeaturePredictions. This is used soley for Jackson to serialize,
   * and should remain private.
   *
   * @return The collections of feature predictions.
   */
  @JsonGetter
  private Collection<FeaturePrediction<?>> getFeaturePredictions() {
    return featureMeasurementTypeFeaturePredictionMap.values();
  }

  /**
   * This does casting before run-time erasure of the type T.
   *
   * @param featurePrediction FeaturePrediction to case
   * @param <T> Type of the casted FeaturePrediction
   * @return the FeaturePrediction, now casted to a FeaturePrediction&lt;T&gt;
   */
  // Because of how the map is constructed, and how FeaturePrediction and FeaturePredictionType are
  // tightly coupled, the casting that happens is safe.
  @SuppressWarnings("unchecked")
  private static <T extends FeaturePredictionValue<?, ?, ?>> FeaturePrediction<T> castBeforeErasure(
    FeaturePrediction<?> featurePrediction) {

    return (FeaturePrediction<T>) featurePrediction;
  }
}
