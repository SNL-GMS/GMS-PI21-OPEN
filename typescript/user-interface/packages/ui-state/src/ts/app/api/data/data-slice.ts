import type { EventTypes, SignalDetectionTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';
import produce from 'immer';

import type { UiChannelSegment } from '../../../types';
import { clearWaveforms } from '../../../workers/api/clear-waveforms';
import { addGetChannelSegmentsByChannelReducers } from './channel-segment/get-channel-segments-by-channel';
import { createRecipeToMutateUiChannelSegmentsRecord } from './channel-segment/mutate-channel-segment-record';
import { addGetEventsWithDetectionsAndSegmentsByTimeReducers } from './event/get-events-detections-segments-by-time';
import { addGetSignalDetectionsWithSegmentsByStationAndTimeReducers } from './signal-detection/get-signal-detections-segments-by-station-time';
import type { DataState } from './types';

const logger = UILogger.create('DATA_SLICE', process.env.DATA_SLICE);

/**
 * The initial state for the data state.
 * This is the starting state for the {@link dataSlice}
 */
export const dataInitialState: DataState = {
  uiChannelSegments: {},
  signalDetections: {},
  events: {},
  queries: {
    getSignalDetectionWithSegmentsByStationAndTime: {},
    getChannelSegmentsByChannel: {},
    getEventsWithDetectionsAndSegmentsByTime: {}
  }
};

/**
 * Defines a Redux slice that contains various data that is fetched using async thunk requests.
 */
export const dataSlice = createSlice({
  name: 'data',
  initialState: dataInitialState,
  reducers: {
    /**
     * Add channel segments to the state.
     */
    addChannelSegments(
      state,
      action: PayloadAction<
        {
          name: string;
          startTimeSecs: number;
          endTimeSecs: number;
          channelSegments: UiChannelSegment[];
        }[]
      >
    ) {
      action.payload.forEach(entry => {
        entry.channelSegments.forEach(cs => {
          state.uiChannelSegments = produce(
            state.uiChannelSegments,
            createRecipeToMutateUiChannelSegmentsRecord(entry.name, [cs])
          );
        });
      });
    },

    /**
     * Clears the channel segments and channel segment request history from the state.
     */
    clearChannelSegmentsAndHistory(state) {
      state.queries.getChannelSegmentsByChannel = {};
      state.uiChannelSegments = {};
      clearWaveforms().catch(e => {
        logger.error(`Failed to clear out waveform cache`, e);
      });
    },

    /**
     * Add events to the state.
     */
    addEvents(state, action: PayloadAction<EventTypes.Event[]>) {
      action.payload.forEach(event => {
        state.events[event.id] = event;
      });
    },

    /**
     * Clears the events and event request history from the state.
     */
    clearEventsAndHistory(state) {
      state.queries.getEventsWithDetectionsAndSegmentsByTime = {};
      state.events = {};
    },

    /**
     * Add signal detections to the state.
     */
    addSignalDetections(state, action: PayloadAction<SignalDetectionTypes.SignalDetection[]>) {
      action.payload.forEach(sd => {
        state.signalDetections[sd.id] = sd;
      });
    },

    /**
     * Clears the signal detections and signal detection request history from the state.
     */
    clearSignalDetectionsAndHistory(state) {
      state.queries.getSignalDetectionWithSegmentsByStationAndTime = {};
      state.signalDetections = {};
    },
    /**
     * clears all data and history from the state
     */
    clearAll(state) {
      state.queries.getSignalDetectionWithSegmentsByStationAndTime = {};
      state.signalDetections = {};
      state.queries.getEventsWithDetectionsAndSegmentsByTime = {};
      state.events = {};
      state.queries.getChannelSegmentsByChannel = {};
      state.uiChannelSegments = {};
      clearWaveforms().catch(e => {
        logger.error(`Failed to clear out waveform cache`, e);
      });
    }
  },

  extraReducers: builder => {
    // add any extra reducers at the data slice level
    addGetChannelSegmentsByChannelReducers(builder);
    addGetEventsWithDetectionsAndSegmentsByTimeReducers(builder);
    addGetSignalDetectionsWithSegmentsByStationAndTimeReducers(builder);
  }
});
