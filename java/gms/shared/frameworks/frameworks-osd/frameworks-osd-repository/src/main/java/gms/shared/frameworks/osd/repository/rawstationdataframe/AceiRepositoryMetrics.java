package gms.shared.frameworks.osd.repository.rawstationdataframe;

import gms.shared.metrics.CustomMetric;

/**
 * Metrics for ACEI timing analysis of {@link AcquiredChannelEnvironmentIssueRepositoryJpa} methods
 */
public class AceiRepositoryMetrics {
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohSyncACEI =
    CustomMetric
      .create(CustomMetric::incrementer, "soh_sync_acei_hits:type=Counter", 0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveAnalog =
    CustomMetric.create(CustomMetric::incrementer, "soh_retrieve_analog_hits:type=Counter", 0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveBoolean =
    CustomMetric.create(CustomMetric::incrementer, "soh_retrieve_boolean_hits:type=Counter", 0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveACEIAnalogId =
    CustomMetric
      .create(CustomMetric::incrementer, "soh_retrieve_acei_analog_id_hits:type=Counter", 0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveACEIBooleanId =
    CustomMetric
      .create(CustomMetric::incrementer, "soh_retrieve_acei_boolean_id_hits:type=Counter", 0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveACEIAnalogTimeType =
    CustomMetric
      .create(CustomMetric::incrementer, "soh_retrieve_acei_analog_time_type_hits:type=Counter",
        0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveACEIBooleanTimeType =
    CustomMetric.create(CustomMetric::incrementer,
      "soh_retrieve_acei_boolean_time_type_hits:type=Counter", 0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveACEIBooleanTime =
    CustomMetric
      .create(CustomMetric::incrementer, "soh_retrieve_acei_boolean_time_hits:type=Counter",
        0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveACEIAnalogTime =
    CustomMetric
      .create(CustomMetric::incrementer, "soh_retrieve_acei_analog_time_hits:type=Counter", 0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveLatestACEIBoolean =
    CustomMetric
      .create(CustomMetric::incrementer, "soh_retrieve_latest_acei_boolean_hits:type=Counter",
        0L);
  static final CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> sohRetrieveLatestACEIAnalog =
    CustomMetric
      .create(CustomMetric::incrementer, "soh_retrieve_latest_acei_analog_hits:type=Counter",
        0L);
  static final CustomMetric<Long, Long> sohSyncACEIDuration =
    CustomMetric
      .create(CustomMetric::updateTimingData, "soh_ync_acei_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveAnalogDuration =
    CustomMetric
      .create(CustomMetric::updateTimingData, "soh_retrieve_analog_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveBooleanDuration =
    CustomMetric
      .create(CustomMetric::updateTimingData, "soh_retrieve_boolean_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveACEIAnalogIdDuration =
    CustomMetric
      .create(CustomMetric::updateTimingData, "soh_retrieve_acei_analog_id_duration:type=Value",
        0L);
  static final CustomMetric<Long, Long> sohRetrieveACEIBooleanIdDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "soh_retrieve_acei_boolean_id_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveACEIAnalogTimeTypeDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "soh_retrieve_acei_analog_time_type_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveACEIBooleanTimeTypeDuration =
    CustomMetric
      .create(CustomMetric::updateTimingData,
        "soh_retrieve_acei_boolean_time_type_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveACEIBooleanTimeDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "soh_retrieve_acei_boolean_time_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveACEIAnalogTimeDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "soh_retrieve_acei_analog_time_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveLatestACEIBooleanDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "soh_retrieve_latest_acei_boolean_duration:type=Value", 0L);
  static final CustomMetric<Long, Long> sohRetrieveLatestACEIAnalogDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "soh_retrieve_latest_acei_analog_duration:type=Value", 0L);

  private AceiRepositoryMetrics() {
  }
}
