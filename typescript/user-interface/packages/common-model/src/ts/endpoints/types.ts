/**
 * Event Manager Service
 */
export const EventManagerUrls = {
  baseUrl: '/event-manager-service/event' as const,
  getEventsWithDetectionsAndSegmentsByTime: '/detections-and-segments/time' as const,
  findEventsByAssociatedSignalDetectionHypotheses: `/associated-signal-detection-hypotheses` as const,
  predict: `/predict` as const,
  predictEvent: `/predict-for-event-location` as const,
  status: `/status` as const,
  update: `/update` as const
};

/**
 * Frameworks OSD Service
 */
export const FrameworksOsdSUrls = {
  baseUrl: '/frameworks-osd-service/osd' as const,
  getProcessingStationGroups: `/station-groups` as const
};

/**
 * Processing Configuration
 */
export const ProcessingConfigUrls = {
  baseUrl: '/ui-processing-configuration-service' as const,
  getProcessingConfiguration: `/resolve` as const
};

/**
 * Signal Detections Manager Service
 */
export const SignalDetectionManagerUrls = {
  baseUrl: '/signal-detection-manager-service/signal-detection' as const,
  getDetectionsWithSegmentsByStationsAndTime: '/signal-detections-with-channel-segments/query/stations-timerange' as const
};

/**
 * Signal Enhancement Configuration
 */
export const SignalEnhancementConfigurationUrls = {
  baseUrl: '/signal-enhancement-configuration-manager-service/signal-enhancement-configuration' as const,
  getSignalEnhancementConfiguration: `/filter-lists-definition` as const
};

/**
 * Station Definition Service
 */
export const StationDefinitionUrls = {
  baseUrl: '/station-definition-service/station-definition' as const,
  getStationGroupsByNames: `/station-groups/query/names` as const,
  getStations: `/stations/query/names` as const,
  getStationsEffectiveAtTimes: `/stations/query/change-times` as const,
  getChannelsByNames: `/channels/query/names` as const
};

/**
 * System Event Gateway
 */
export const SystemEventGatewayUrls = {
  baseUrl: '/interactive-analysis-api-gateway' as const,
  sendClientLogs: `/client-log` as const,
  acknowledgeSohStatus: `/acknowledge-soh-status` as const,
  quietSohStatus: `/quiet-soh-status` as const
};

/**
 * System Messages
 */
export const SystemMessageUrls = {
  baseUrl: '/smds-service' as const,
  getSystemMessageDefinitions: `/retrieve-system-message-definitions` as const
};

/**
 * User Manager Service
 */
export const UserManagerServiceUrls = {
  baseUrl: '/user-manager-service' as const,
  getUserProfile: `/user-preferences` as const,
  setUserProfile: `/user-preferences/store` as const
};

/**
 * Waveform Manager Service
 */
export const WaveformManagerServiceUrls = {
  baseUrl: '/waveform-manager-service/waveform/channel-segment/query' as const,
  getChannelSegment: '/channel-timerange' as const
};

/**
 * Workflow Manager Service
 */
export const WorkflowManagerServiceUrls = {
  baseUrl: '/workflow-manager-service/workflow-manager' as const,
  workflow: `/workflow-definition` as const,
  stageIntervalsByIdAndTime: `/interval/stage/query/ids-timerange` as const,
  updateActivityIntervalStatus: `/interval/activity/update` as const,
  updateStageIntervalStatus: `/interval/stage/interactive-analysis/update` as const
};
