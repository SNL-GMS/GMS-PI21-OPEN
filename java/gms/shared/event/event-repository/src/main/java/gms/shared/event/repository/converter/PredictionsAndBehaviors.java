package gms.shared.event.repository.converter;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;
import gms.shared.event.coi.LocationBehavior;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Defines the structure of a PredictionsAndBehaviors object - aggregates {@link gms.shared.event.coi.featureprediction.FeaturePrediction}s
 * and {@link LocationBehavior}s
 */
@AutoValue
public abstract class PredictionsAndBehaviors {
  private static final PredictionsAndBehaviors EMPTY = PredictionsAndBehaviors.create(FeaturePredictionContainer.of(), emptySet());

  public abstract FeaturePredictionContainer getFeaturePredictions();

  public abstract Set<LocationBehavior> getLocationBehaviors();

  public static PredictionsAndBehaviors empty() {
    return EMPTY;
  }

  public static PredictionsAndBehaviors create(FeaturePredictionContainer featurePredictions,
    Set<LocationBehavior> locationBehaviors) {
    return new AutoValue_PredictionsAndBehaviors(featurePredictions, locationBehaviors);
  }

  public static PredictionsAndBehaviors union(PredictionsAndBehaviors first, PredictionsAndBehaviors second) {
    return PredictionsAndBehaviors.create(
      first.getFeaturePredictions().union(second.getFeaturePredictions()),
      Sets.union(first.getLocationBehaviors(), second.getLocationBehaviors()));
  }

}
