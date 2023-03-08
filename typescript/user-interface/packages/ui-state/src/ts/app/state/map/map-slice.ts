import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type { MapLayers, MapState } from './types';

/**
 * The initial state for the map.
 */
export const mapInitialState: MapState = {
  isSyncedWithWaveformZoom: false,
  layerVisibility: {
    stations: true,
    sites: false,
    signalDetections: true,
    events: true,
    preferredLocationSolution: true,
    edgeEventsBeforeInterval: true,
    edgeEventsAfterInterval: true,
    nonPreferredLocationSolution: false,
    confidenceEllipse: true,
    coverageEllipse: true,

    edgeDetectionBefore: true,
    edgeDetectionAfter: true,

    unassociatedDetection: true,
    associatedOpenDetection: true,
    associatedOtherDetection: true,
    associatedCompleteDetection: true
  }
};

/**
 * The map state reducer slice
 */
export const mapSlice = createSlice({
  name: 'map',
  initialState: mapInitialState,
  reducers: {
    /**
     * Sets the boolean that determines if the map should sync visible signal detections to the waveform
     * panel's zoom interval
     */
    setIsMapSyncedWithWaveformZoom(state, action: PayloadAction<boolean>) {
      state.isSyncedWithWaveformZoom = action.payload;
    },
    updateLayerVisibility: (state, action: PayloadAction<Record<MapLayers, boolean>>) => {
      state.layerVisibility = action.payload;
    }
  }
});

export const mapActions = mapSlice.actions;
