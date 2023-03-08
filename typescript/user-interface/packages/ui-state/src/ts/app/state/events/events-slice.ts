import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type { EventFilters, EventsColumn, EventsState } from './types';

/**
 * The initial state for the signal detections panel.
 */
export const eventsInitialState: EventsState = {
  eventsColumns: {
    conflict: true,
    time: true,
    latitudeDegrees: true,
    longitudeDegrees: true,
    depthKm: true,
    region: true,
    confidenceSemiMajorAxis: false,
    confidenceSemiMinorAxis: false,
    coverageSemiMajorAxis: false,
    coverageSemiMinorAxis: false,
    magnitudeMb: true,
    magnitudeMs: false,
    magnitudeMl: false,
    activeAnalysts: true,
    preferred: true,
    status: true,
    rejected: true
  },
  edgeEvents: {
    'Edge events before interval': true,
    'Edge events after interval': true
  },
  stationsAssociatedWithCurrentOpenEvent: []
};

/**
 * The signal detections panel state reducer slice
 */
export const eventsSlice = createSlice({
  name: 'events',
  initialState: eventsInitialState,
  reducers: {
    /**
     * Sets the boolean that determines if an events column should be displayed
     */
    updateEventsColumns: (state, action: PayloadAction<Record<EventsColumn, boolean>>) => {
      state.eventsColumns = action.payload;
    },
    /**
     * Sets the boolean that determines if a category of edge events should be displayed
     */
    updateEdgeEvents: (state, action: PayloadAction<Record<EventFilters, boolean>>) => {
      state.edgeEvents = action.payload;
    },
    /**
     * Sets the stations associated to the open event
     */
    setStationsAssociatedWithCurrentOpenEvent(state, action: PayloadAction<string[]>) {
      state.stationsAssociatedWithCurrentOpenEvent = action.payload;
    }
  }
});
export const eventsActions = eventsSlice.actions;
