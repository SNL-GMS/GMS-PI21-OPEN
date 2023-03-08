package gms.shared.event.accessor.facet;

public enum FacetingTypes {
  STATION_TYPE(gms.shared.stationdefinition.facet.FacetingTypes.STATION_TYPE.toString()),
  CHANNEL_TYPE(gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_TYPE.toString()),
  RESPONSE_TYPE(gms.shared.stationdefinition.facet.FacetingTypes.RESPONSE_TYPE.toString()),
  CHANNEL_SEGMENT_TYPE(gms.shared.stationdefinition.facet.FacetingTypes.CHANNEL_SEGMENT_TYPE.toString()),
  SIGNAL_DETECTION_TYPE("SignalDetection"),
  SDH_TYPE("SignalDetectionHypothesis"),
  EVENT_TYPE("Event"),
  PREFERRED_EH_TYPE("PreferredEventHypothesis"),
  EVENT_HYPOTHESIS_TYPE("EventHypothesis"),
  LOCATION_SOLUTION_TYPE("LocationSolution"),
  FEATURE_PREDICTION_TYPE("FeaturePrediction"),
  FEATURE_MEASUREMENT_TYPE("FeatureMeasurement"),
  DEFAULT_FACETED_EVENT_HYPOTHESIS_TYPE("defaultFacetedEventHypothesis"),
  SD_HYPOTHESES_KEY("signalDetectionHypotheses"),
  EVENT_HYPOTHESIS_KEY("eventHypothesis"),
  PREFERRED_EH_KEY("preferredEventHypothesis"),
  OVERALL_PREFERRED_KEY("overallPreferred"),
  FINAL_EH_HISTORY_KEY("finalEventHypothesisHistory"),
  PARENT_EH_KEY("parentEventHypotheses"),
  ASSOCIATED_SDH_KEY("associatedSignalDetectionHypothesis"),
  PREFERRED_LOCATION_SOLUTION_KEY("preferredLocationSolution"),
  LOCATION_SOLUTION_KEY("locationSolution"),
  MEASURED_CHANNEL_SEGMENT_KEY("measuredChannelSegment"),
  FEATURE_PREDICTIONS_KEY("featurePredictions"),
  FEATURE_MEASUREMENTS_KEY("featureMeasurements"),
  STATION_KEY("station"),
  CHANNEL_KEY("channel"),
  CHANNELS_KEY(gms.shared.stationdefinition.facet.FacetingTypes.CHANNELS_KEY.toString()),
  CHANNEL_SEGMENT_KEY("channelSegment"),
  RESPONSES_KEY(gms.shared.stationdefinition.facet.FacetingTypes.RESPONSES_KEY.toString()),
  ID_CHANNEL_KEY(gms.shared.stationdefinition.facet.FacetingTypes.ID_CHANNEL_KEY.toString()),
  REJECTED_SD_KEY("rejectedSignalDetectionAssociations");

  private final String value;

  FacetingTypes(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return getValue();
  }
}