import type { WeavessTypes } from '@gms/weavess-core';
import isEqual from 'lodash/isEqual';
import memoizeOne from 'memoize-one';
import React from 'react';

import { calculateLeftPercent } from '../../utils';
import { MoveableMarker } from './moveable-marker';
import { SelectionWindow } from './selection-window';
import { VerticalMarker } from './vertical-marker';

/**
 * Utility for creating vertical marker elements
 *
 * @param startTimeSecs the start time in seconds for the data
 * @param endTimeSecs the end time in seconds for the data
 * @param verticalMarkers the vertical markers
 */
export const createVerticalMarkers = (
  startTimeSecs: number,
  endTimeSecs: number,
  verticalMarkers: WeavessTypes.Marker[] | undefined
): JSX.Element[] => {
  if (!verticalMarkers || verticalMarkers.length === 0) return [];
  return (
    verticalMarkers
      // marker is out of the visible time range, no need to render
      .filter(v => v.timeSecs >= startTimeSecs && v.timeSecs <= endTimeSecs)
      .map(verticalMarker => {
        const leftPct = calculateLeftPercent(verticalMarker.timeSecs, startTimeSecs, endTimeSecs);
        return (
          <VerticalMarker
            key={`vertical_marker_${verticalMarker.id}`}
            color={verticalMarker.color}
            lineStyle={verticalMarker.lineStyle}
            percentageLocation={leftPct}
          />
        );
      })
  );
};

/**
 * Utility for creating moveable marker elements.
 *
 * @param startTimeSecs the start time in seconds for the data
 * @param endTimeSecs the end time in seconds for the data
 * @param moveableMarkers the moveable markers
 * @param zoomInterval current waveform interval displayed
 * @param containerClientWidth provides the container client width
 * @param viewportClientWidth provides the viewport client width
 * @param updateMarkers event to be invoked on update
 * @param onUpdateMarker
 * @param labelWidthPx offset provided for calculating location
 */
export const createMoveableMarkers = (
  startTimeSecs: number,
  endTimeSecs: number,
  moveableMarkers: WeavessTypes.Marker[] | undefined,
  getZoomRatio: () => number,
  containerClientWidth: () => number,
  viewportClientWidth: () => number,
  onUpdateMarker?: (marker: WeavessTypes.Marker) => void,
  labelWidthPx = 0
): JSX.Element[] => {
  if (!moveableMarkers || moveableMarkers.length === 0) return [];
  return (
    moveableMarkers
      // marker is out of the visible time range, no need to render
      .filter(m => m.timeSecs >= startTimeSecs && m.timeSecs <= endTimeSecs)
      .map(moveableMarker => {
        const leftPct = calculateLeftPercent(moveableMarker.timeSecs, startTimeSecs, endTimeSecs);
        return (
          <MoveableMarker
            key={`moveable_marker_${moveableMarker.id}`}
            labelWidthPx={labelWidthPx}
            marker={moveableMarker}
            percentageLocation={leftPct}
            timeRange={(): WeavessTypes.TimeRange => ({
              startTimeSecs,
              endTimeSecs
            })}
            getZoomRatio={getZoomRatio}
            containerClientWidth={containerClientWidth}
            viewportClientWidth={viewportClientWidth}
            // these methods shouldn't be bound as they are events passed in by a 3rd party user
            // and have already been bound to the appropriate context.
            onUpdateMarker={onUpdateMarker}
          />
        );
      })
  );
};

/**
 * Utility for creating selection window elements.
 *
 * @param startTimeSecs the start time in seconds for the data
 * @param endTimeSecs the end time in seconds for the data
 * @param selectionWindows the selection windows
 * @param zoomInterval current waveform interval displayed
 * @param canvasRef provides the canvas reference
 * @param containerClientWidth provides the container client width
 * @param viewportClientWidth provides the viewport client width
 * @param computeTimeSecsForMouseXPosition computes the time in seconds for the mouse x position.
 * @param onMouseMove event to be invoked on mouse move
 * @param onMouseDown event to be invoked on mouse move
 * @param onMouseUp event to be invoked on mouse move
 * @param onMoveSelectionWindow event handler for invoked while the selection is moving
 * @param onUpdateSelectionWindow event handler for updating selections value
 * @param onClickSelectionWindow event handler for click events within a selection
 * @param labelWidthPx offset provided for calculating location
 */
export const createSelectionWindowMarkers = (
  startTimeSecs: number,
  endTimeSecs: number,
  selectionWindows: WeavessTypes.SelectionWindow[] | undefined,
  getZoomRatio: () => number,
  canvasRef: () => HTMLCanvasElement | null,
  containerClientWidth: () => number,
  viewportClientWidth: () => number,
  computeTimeSecsForMouseXPosition: (mouseXPosition: number) => number,
  onMouseMove: (e: React.MouseEvent<HTMLDivElement>) => void,
  onMouseDown: (e: React.MouseEvent<HTMLDivElement>) => void,
  onMouseUp: (e: React.MouseEvent<HTMLDivElement>) => void,
  onMoveSelectionWindow?: (selection: WeavessTypes.SelectionWindow) => void,
  onUpdateSelectionWindow?: (selection: WeavessTypes.SelectionWindow) => void,
  onClickSelectionWindow?: (selection: WeavessTypes.SelectionWindow, timeSecs: number) => void,
  labelWidthPx = 0
): JSX.Element[] => {
  if (!selectionWindows || selectionWindows.length === 0) return [];
  return (
    selectionWindows
      // marker is out of the visible time range, no need to render
      .filter(
        s =>
          s.startMarker.timeSecs >= startTimeSecs &&
          s.startMarker.timeSecs <= endTimeSecs &&
          s.endMarker.timeSecs >= startTimeSecs &&
          s.endMarker.timeSecs <= endTimeSecs
      )
      .map(selectionWindow => (
        <SelectionWindow
          key={`selection_window_${selectionWindow.id}`}
          timeRange={(): WeavessTypes.TimeRange => ({
            startTimeSecs,
            endTimeSecs
          })}
          labelWidthPx={labelWidthPx}
          getZoomRatio={getZoomRatio}
          canvasRef={canvasRef}
          selectionWindow={selectionWindow}
          containerClientWidth={containerClientWidth}
          viewportClientWidth={viewportClientWidth}
          computeTimeSecsForMouseXPosition={computeTimeSecsForMouseXPosition}
          /* eslint-disable @typescript-eslint/unbound-method */
          // these methods shouldn't be bound as they are events passed in by a 3rd party user
          // and have already been bound to the appropriate context.
          onMoveSelectionWindow={onMoveSelectionWindow}
          onUpdateSelectionWindow={onUpdateSelectionWindow}
          onClickSelectionWindow={onClickSelectionWindow}
          onMouseDown={onMouseDown}
          onMouseMove={onMouseMove}
          onMouseUp={onMouseUp}
        />
      ))
  );
};

/**
 * A memoized function for creating the vertical markers.
 * The memoization function caches the results using
 * the most recent argument and returns the results.
 *
 * @returns an array JSX elements
 */
export const memoizedCreateVerticalMarkers = memoizeOne(
  createVerticalMarkers,
  /* tell memoize to use a deep comparison for complex objects */
  isEqual
);

/**
 * A memoized function for creating the moveable markers.
 * The memoization function caches the results using
 * the most recent argument and returns the results.
 *
 * @returns an array JSX elements
 */
export const memoizedCreateMoveableMarkers = memoizeOne(
  createMoveableMarkers,
  /* tell memoize to use a deep comparison for complex objects */
  isEqual
);

/**
 * A memoized function for creating the selection window markers.
 * The memoization function caches the results using
 * the most recent argument and returns the results.
 *
 * @returns an array JSX elements
 */
export const memoizedCreateSelectionWindowMarkers = memoizeOne(
  createSelectionWindowMarkers,
  /* tell memoize to use a deep comparison for complex objects */
  isEqual
);
