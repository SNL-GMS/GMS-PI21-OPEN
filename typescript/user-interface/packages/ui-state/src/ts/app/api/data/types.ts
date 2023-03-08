import type { EventTypes, SignalDetectionTypes } from '@gms/common-model';

import type { UiChannelSegment } from '../../../types';
import type { GetChannelSegmentsByChannelHistory } from './channel-segment/types';
import type { GetEventsWithDetectionsAndSegmentsByTimeHistory } from './event/types';
import type { GetSignalDetectionsWithSegmentsByStationAndTimeHistory } from './signal-detection/types';

/**
 * Defines the Data slice state.
 */
export interface DataState {
  /** the channel segments - by unique channel name - populated by multiple queries */
  uiChannelSegments: Record<string, Record<string, UiChannelSegment[]>>;
  /** the signal detections - by unique station name - populated by multiple queries */
  signalDetections: Record<string, SignalDetectionTypes.SignalDetection>;
  /** the events - by time- populated by multiple queries */
  events: Record<string, EventTypes.Event>;
  queries: {
    /** the history record of the getChannelSegmentsByChannel query */
    getChannelSegmentsByChannel: GetChannelSegmentsByChannelHistory;
    /** the history record of the getSignalDetectionWithSegmentsByStationAndTime query */
    getSignalDetectionWithSegmentsByStationAndTime: GetSignalDetectionsWithSegmentsByStationAndTimeHistory;
    /** the history record of the getEventsWithDetectionsAndSegmentsByTime query */
    getEventsWithDetectionsAndSegmentsByTime: GetEventsWithDetectionsAndSegmentsByTimeHistory;
  };
}
