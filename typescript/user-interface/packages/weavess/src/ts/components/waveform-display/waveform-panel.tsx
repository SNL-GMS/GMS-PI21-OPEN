/* eslint-disable class-methods-use-this */
/* eslint-disable jsx-a11y/no-static-element-interactions */
/* eslint-disable react/destructuring-assignment */

import { MICROSECONDS_IN_SECOND } from '@gms/common-util';
import { HotkeyListener, UILogger } from '@gms/ui-util';
import type { WeavessTypes } from '@gms/weavess-core';
import { WeavessConstants, WeavessMessages } from '@gms/weavess-core';
import * as d3 from 'd3';
import elementResizeEvent from 'element-resize-event';
import debounce from 'lodash/debounce';
import defer from 'lodash/defer';
import flatMap from 'lodash/flatMap';
import range from 'lodash/range';
import memoizeOne from 'memoize-one';
import React from 'react';
import { toast } from 'react-toastify';
import ResizeObserver from 'resize-observer-polyfill';
import * as THREE from 'three';
import { RpcProvider } from 'worker-rpc';

import {
  computeTimeSecsForMouseXPositionFraction,
  convertPixelOffsetToFractionalPosition,
  convertPixelOffsetToTime
} from '../../util/position-util';
import { ViewportMaxSizer } from '../../util/viewport-max-sizer';
import { HotkeyHandler } from '../hotkey-handler';
import { Station, TimeAxis } from './components';
import { TimeRange } from './components/axes/time-range';
import {
  memoizedCreateMoveableMarkers,
  memoizedCreateSelectionWindowMarkers,
  memoizedCreateVerticalMarkers
} from './components/markers';
import { Ruler } from './components/ruler/ruler';
import type { Channel } from './components/station/components';
import { SingleDoubleClickEvent } from './events/single-double-click-event';
import type { WeavessContainerDimensions } from './shared-types';
import type { AnimationFrameOptions, WaveformPanelProps, WaveformPanelState } from './types';
import { BrushType } from './types';
import { clearThree, timeRangeDisplayString } from './utils';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

declare let require;
const WeavessWorker = require('worker-loader?inline&fallback=false!../../workers'); // eslint-disable-line

// create web workers responsible for creating line geometries
const defaultNumWorkers = 4;
const workerRpcs = range(window.navigator.hardwareConcurrency || defaultNumWorkers).map(() => {
  const worker = new WeavessWorker();
  const workerRpc = new RpcProvider((message, transfer) => worker.postMessage(message, transfer));
  worker.onmessage = e => workerRpc.dispatch(e.data);
  return workerRpc;
});

/** The number of decimal places to to set on zoom range */
const GL_DECIMAL_PRECISION = 15;

const DEFAULT_MAX_DIV_WIDTH_PX = 15252000;

const ONE_PIXEL = 1;

const ZOOM_INTERVAL_DEBOUNCE_MS = 200;

/** Percent tolerance within max div when checking */
const MAX_DIV_TOLERANCE_PERCENT = 0.99;

/** Percent tolerance within max resolution (100 microseconds) when checking */
const MAX_RESOLUTION_TOLERANCE_PERCENT = 1.01;

/**
 * Waveform Panel component. Contains a TimeAxis and Stations
 */
export class WaveformPanel extends React.PureComponent<WaveformPanelProps, WaveformPanelState> {
  /** Refs to each station component */
  public stationComponentRefs: Map<string, Station> | null;

  /** Ref to the root element of weavess */
  private weavessRootRef: HTMLDivElement | null;

  /** Ref to the viewport where waveforms are rendered */
  private waveformsViewportRef: HTMLDivElement | null;

  /** Ref to the container where waveforms are held, directly within the viewport */
  private waveformsContainerRef: HTMLDivElement | null;

  /** Ref to the translucent selection brush-effect region, which is updated manually for performance reasons */
  private selectionAreaRef: HTMLDivElement | null;

  /** Ref to the TimeAxis component, which is updated manually for performance reasons */
  private timeAxisRef: TimeAxis | null;

  /** Ref to the vertical crosshair indicator element */
  private crosshairRef: HTMLDivElement | null;

  /** Ref to the primary canvas element where the waveforms are drawn */
  private canvasRef: HTMLCanvasElement | null;

  /** THREE.js WebGLRenderer used to draw waveforms */
  private renderer: THREE.WebGLRenderer;

  /** A list of active web workers */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly workerRpcs: any[];

  /** If the brush has just started to be used */
  private startOfBrush = true;

  /** Flag to indicate whether or not the mouse button is pressed down */
  private isMouseDown: { clientX: number; clientY: number } | undefined = undefined;

  /** The start of the brush effect in [0,1] where 0 = zoomRange.left and 1 = zoomRange.right */
  private selectionStart: number | undefined;

  /** The type of brush used on the channel */
  private brushType: BrushType | undefined = undefined;

  /** The type of brush used on the channel */
  private needToDeselect = false;

  private globalHotkeyListenerId: string;

  /** An id of the previous requestAnimationFrame call, which allows
   * one to cancel it, so we can avoid enqueueing multiple animation frames */
  private prevRAF: number;

  /** An id of the previous requestAnimationFrame call to the onRenderWaveformLoopEnd function, which allows
   * one to cancel it, so we can avoid enqueueing multiple animation frames */
  private readonly prevRAFEnd: number;

  /**
   * A collection of the physical dimensions in the DOM. These are calculated in a batch
   * in order to reduce calculation time during critical points in time, such as
   * requestAnimationFrame calls.
   */
  private readonly dimensions: WeavessContainerDimensions;

  /**
   * A resize observer for the canvas element
   */
  private readonly canvasResizeObserver: ResizeObserver;

  /** The empty set of what is selected */
  private readonly emptySelection = {
    channels: undefined,
    signalDetections: undefined,
    predictedPhases: undefined
  };

  /** handler for handling single and double click events */
  private readonly handleSingleDoubleClick: SingleDoubleClickEvent = new SingleDoubleClickEvent();

  /**
   * A memoized function for creating all stations
   * The memoization function caches the results using
   * the most recent argument and returns the results.
   *
   * @param props the waveform panel props
   *
   * @returns an array JSX elements
   */
  private readonly memoizedCreateStationsJsx: (props: WaveformPanelProps) => JSX.Element[];

  /** Debounce call to update parent with latest zoomInterval */
  private readonly debounceUpdateZoomInterval;

  /** The max width the browser will allow us to render a div */
  private maxViewportSizePx = DEFAULT_MAX_DIV_WIDTH_PX;

  /**
   * Constructor.
   *
   * @param props Waveform Panel props as WaveformPanelProps
   */
  public constructor(props: WaveformPanelProps) {
    super(props);
    this.state = {
      rulerIsActive: false,
      rulerInitialPoint: undefined,
      zoomTimeInterval: props.viewableInterval
    };
    this.canvasResizeObserver = new ResizeObserver(() => this.updateSize());
    this.workerRpcs = workerRpcs;
    this.stationComponentRefs = new Map();
    this.memoizedCreateStationsJsx = memoizeOne(this.createStationsJsx);
    this.dimensions = {
      viewport: {
        clientHeight: 0,
        clientWidth: 0,
        scrollHeight: 0,
        scrollLeft: 0,
        scrollWidth: 0,
        scrollTop: 0
      },
      viewportContentContainer: {
        clientWidth: 0
      },
      canvas: {
        rect: new DOMRect(),
        offsetHeight: 0,
        offsetWidth: 0,
        clientWidth: 0
      }
    };
    // Create debounce call to update zoom interval in parent component
    this.debounceUpdateZoomInterval = debounce(
      this.updateZoomIntervalInControlledComponent,
      ZOOM_INTERVAL_DEBOUNCE_MS,
      {
        leading: false
      }
    );
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Called immediately after a component is mounted.
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount(): void {
    // prevent firefox ctrl + wheel zoom, which fights with weavess
    document.addEventListener('wheel', this.documentPreventCtrlMouseWheel, { passive: false });
    this.globalHotkeyListenerId = HotkeyListener.subscribeToGlobalHotkeyListener();

    if (!this.canvasRef) {
      logger.error('Weavess error - canvas not present at mount time'); // eslint-disable-line
      return;
    }

    this.renderer = new THREE.WebGLRenderer({
      alpha: true,
      antialias: true,
      canvas: this.canvasRef
    });
    this.canvasResizeObserver.observe(this.canvasRef);

    elementResizeEvent(this.waveformsViewportRef, () => {
      this.renderWaveforms();
      if (this.timeAxisRef) this.timeAxisRef.update();
    });
    this.postZoomUpdate();
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: WaveformPanelProps, prevState: WaveformPanelState): void {
    // check if the zoom interval has changed
    if (
      !this.areIntervalsEqual(prevState.zoomTimeInterval, this.state.zoomTimeInterval) ||
      !this.areIntervalsEqual(prevProps.viewableInterval, this.props.viewableInterval)
    ) {
      this.zoom(this.state.zoomTimeInterval);
    } else {
      this.pruneStationComponentRefs();
      this.postZoomUpdate();
    }
  }

  /**
   * Catches exceptions generated in descendant components.
   * Unhandled exceptions will cause the entire component tree to unmount.
   *
   * @param error the error that was caught
   * @param info the information about the error
   */
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
  public componentDidCatch(error, info): void {
    logger.error(`Waveform Panel Error: ${error} : ${info}`);
  }

  /**
   * clean up when the component is unmounted
   */
  public componentWillUnmount(): void {
    document.removeEventListener('wheel', this.documentPreventCtrlMouseWheel);
    HotkeyListener.unsubscribeFromGlobalHotkeyListener(this.globalHotkeyListenerId);
    if (this.canvasRef) this.canvasResizeObserver.unobserve(this.canvasRef);
    clearThree(this.renderer);
    if (this.renderer.forceContextLoss) {
      this.renderer.forceContextLoss();
    }
    if (this.renderer) {
      this.renderer.canvas = null;
      this.renderer.context = null;
      this.renderer.domElement = null;
      this.renderer = null;
    }
    this.crosshairRef = null;
    this.timeAxisRef = null;
    this.selectionAreaRef = null;
    this.waveformsContainerRef = null;
    this.waveformsViewportRef = null;
    this.weavessRootRef = null;
    this.renderer = null;
    this.stationComponentRefs = null;
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Exposed primarily for non-react users.
   * Force a redraw of the waveforms.
   */
  // eslint-disable-next-line react/sort-comp
  public refresh = (): void => {
    this.renderWaveforms();
  };

  /**
   * Reset amplitudes of all waveforms in this panel.
   */
  public resetAmplitudes = (): void => {
    // Reset manual scale for each station
    if (this.stationComponentRefs) {
      this.stationComponentRefs.forEach(station => station.resetAmplitude());
    }

    // call reset amplitude selection
    if (this.props.events.onResetAmplitude) {
      this.props.events.onResetAmplitude();
    }
  };

  /**
   * Computes the time in seconds for the mouse x position, represented as a fraction of the canvas.
   *
   * @param mouseXPositionFraction the mouse x position from 0 to 1, where 0 is the far left and 1 is the far right of the canvas
   * @returns The computed time in seconds
   */
  public readonly computeTimeSecsForMouseXFractionalPosition = (
    mouseXPositionFraction: number
  ): number => {
    return computeTimeSecsForMouseXPositionFraction(
      mouseXPositionFraction,
      this.props.viewableInterval,
      this.getCurrentZoomIntervalRange()
    );
  };

  /**
   * Computes the time in epoch seconds when given an x pixel position on the screen.
   *
   * @param mouseXPx the x position in pixels in question
   * @returns the time represented by that location
   */
  public readonly computeTimeSecsFromMouseXPixels = (mouseXPx: number): number => {
    return convertPixelOffsetToTime(
      mouseXPx,
      this.getCanvasRect(),
      this.props.viewableInterval,
      this.getCurrentZoomIntervalRange()
    );
  };

  /**
   * Computes a fraction representing where on the canvas an x pixel value is found.
   * 0 means the left side of the canvas, 1 means the right. Value can be out of these bounds.
   *
   * @param xPositionPx the input
   * @returns the fractional x position on the canvas
   */
  public readonly computeFractionOfCanvasFromXPositionPx = (xPositionPx: number): number => {
    return convertPixelOffsetToFractionalPosition(this.getCanvasRect(), xPositionPx);
  };

  /**
   * Gets the bounding client rectangle of the waveform panel's canvas element in the DOM.
   *
   * @returns the canvas' bounding rectangle, or undefined if no canvas is found.
   */
  public readonly getCanvasBoundingClientRect = (): DOMRect | undefined =>
    this.dimensions.canvas?.rect ?? this.canvasRef?.getBoundingClientRect();

  /**
   * Removes the brush div, is public so it be hit with weavess reference
   */
  public clearBrushStroke = (): void => {
    if (!this.selectionAreaRef) {
      return;
    }
    this.selectionAreaRef.style.display = 'none';
    this.selectionStart = undefined;
    this.brushType = undefined;
    this.startOfBrush = true;
  };

  /**
   * Gets a list of the channel names in the order they are displayed in the WaveformPanel.
   *
   * @returns a list of the channel names in the order in which they are displayed (from top to bottom)
   */
  public readonly getOrderedVisibleChannelNames = (): string[] => {
    const chanNames: string[] = [];
    this.props.stations.forEach(sta => {
      chanNames.push(sta.defaultChannel.id);
      sta.nonDefaultChannels?.forEach(chan => {
        chanNames.push(chan.id);
      });
    });
    return chanNames;
  };

  /**
   * Finds the Channel's Waveform YAxisBounds
   *
   * @param channelId
   * @returns WeavessTypes.YAxisBounds for the channel name
   */
  public readonly getChannelWaveformYAxisBounds = (
    channelName: string
  ): WeavessTypes.YAxisBounds | undefined => {
    const channel = this.findChannel(channelName);
    if (channel) {
      return channel.getWaveformYAxisBound();
    }
    return undefined;
  };

  /**
   * get the currently displayed zoomTimeInterval
   * (the startTime and endTime of the currently displayed view of the waveforms).
   *
   * @returns the state's zoomTimeInterval
   */
  public getCurrentZoomInterval = (): WeavessTypes.TimeRange => {
    const zoomTime = this.state.zoomTimeInterval;
    if (!zoomTime || Number.isNaN(zoomTime.startTimeSecs) || Number.isNaN(zoomTime.endTimeSecs)) {
      return this.props.viewableInterval;
    }
    return zoomTime;
  };

  /**
   * Will zoom to the provided time range even if current zoom interval is the same
   *
   * @param zoomInterval
   */
  public readonly zoomToTimeWindow = (zoomInterval: WeavessTypes.TimeRange): void => {
    this.setZoomIntervalInState(this.checkMaxZoomInterval(zoomInterval));
  };

  /** ********************
   * * End Public Methods
   ********************** */

  private readonly documentPreventCtrlMouseWheel = e => {
    if (e.ctrlKey) {
      e.preventDefault();
    }
  };

  /**
   * Compare the updated zoom interval with current zoom interval in state.
   *
   * @param zoomInterval interval to compare against current zoom interval in state
   * @returns true if the zoom interval is the same (or within 1 pixel); false otherwise
   */
  private readonly isCurrentZoomIntervalEqual = (
    zoomInterval: WeavessTypes.TimeRange | undefined
  ): boolean => {
    return this.areIntervalsEqual(this.state.zoomTimeInterval, zoomInterval);
  };

  /**
   * Compare the two intervals to see if they are equal within 1 pixel
   *
   * @param interval1
   * @param interval2
   * @returns true if the interval is the same (or within 1 pixel); false otherwise
   */
  private readonly areIntervalsEqual = (
    interval1: WeavessTypes.TimeRange | undefined,
    interval2: WeavessTypes.TimeRange | undefined
  ): boolean => {
    if (!interval1 || !interval2) {
      return false;
    }
    const pixelsPerSecond =
      this.dimensions.canvas.clientWidth / (interval1.endTimeSecs - interval1.startTimeSecs);

    // compare the two intervals; they are considered different if the delta is >= than 1 pixel
    // !ignore extremely small deltas when comparing the zoom intervals; this can cause an infinite loop when zooming
    return (
      Math.abs(pixelsPerSecond * (interval1.startTimeSecs - interval2.startTimeSecs)) < ONE_PIXEL &&
      Math.abs(pixelsPerSecond * (interval1.endTimeSecs - interval2.endTimeSecs)) < ONE_PIXEL
    );
  };

  /**
   * Zoom interval is checked for limits
   *
   * @param zoomInterval to check
   * @return returns clamped zoom interval if necessary. Will return undefined if already at max zoom
   */
  private readonly checkMaxZoomInterval = (
    zoomInterval: WeavessTypes.TimeRange
  ): WeavessTypes.TimeRange => {
    // Only call setState if the time range has actually changed.
    if (
      !zoomInterval ||
      Number.isNaN(zoomInterval.startTimeSecs) ||
      Number.isNaN(zoomInterval.endTimeSecs)
    ) {
      return zoomInterval;
    }

    // If already maxed out and still trying to zoom in notify and return undefined zoom interval
    if (
      this.hasCurrentZoomIntervalReachedMax() &&
      this.isZoomIntervalSmallerThanCurrent(zoomInterval)
    ) {
      this.notifyMaxZoomHasBeenReached();
      return undefined;
    }

    // if the interval changed, (strict equality check) that means we didn't zoom to the range requested,
    // so we should inform the user.
    const clampedZoomInterval = this.clampToMaxZoomInterval(zoomInterval);
    if (clampedZoomInterval !== zoomInterval && !this.hasCurrentZoomIntervalReachedMax()) {
      this.notifyMaxZoomHasBeenReached();
    }
    return clampedZoomInterval;
  };

  /**
   * Set the zoom interval in state.
   */
  private readonly setZoomIntervalInState = (zoomInterval: WeavessTypes.TimeRange) => {
    if (
      !zoomInterval ||
      Number.isNaN(zoomInterval.startTimeSecs) ||
      Number.isNaN(zoomInterval.endTimeSecs)
    ) {
      return;
    }

    if (!this.isCurrentZoomIntervalEqual(zoomInterval)) {
      this.setState({
        zoomTimeInterval: zoomInterval
      });
    }
  };

  /**
   * If waveform panel is a controlled component update parent with
   * zoomInterval
   */
  private readonly updateZoomIntervalInControlledComponent = (): void => {
    // Update zoom interval with parent component after zooming
    if (this.props.isControlledComponent && this.props.events.onZoomChange) {
      this.props.events.onZoomChange(this.getCurrentZoomInterval());
    }
  };

  /**
   * Call to update amplitude after a zoom call. Also remove any manual scaling.
   */
  private readonly updateAmplitudes = () => {
    this.getStationsChannels().forEach(async chan => {
      if (chan && chan.updateAmplitude) await chan?.updateAmplitude(this.getCurrentZoomInterval());
    });
  };

  /**
   * Deletes any station component refs that are not in the current stations (from props)
   */
  private readonly pruneStationComponentRefs = () => {
    if (!this.stationComponentRefs) {
      return;
    }
    const currentStationNames = this.props.stations.map(sta => sta.id).sort();
    Array.from(this.stationComponentRefs.keys()).forEach(name => {
      if (!currentStationNames.includes(name)) {
        this.stationComponentRefs?.delete(name);
      }
    });
  };

  /**
   * Updates the position and size dimensions of elements that the waveform display cares about. This
   * prevents one from having to ask the browser to calculate layout and styles during critical points
   * in the execution of the code.
   */
  private readonly updateTrackedDimensions = () => {
    if (this.waveformsViewportRef) {
      this.dimensions.viewport.clientHeight = this.waveformsViewportRef.clientHeight;
      this.dimensions.viewport.clientWidth = this.waveformsViewportRef.clientWidth;
      this.dimensions.viewport.scrollHeight = this.waveformsViewportRef.scrollHeight;
      this.dimensions.viewport.scrollWidth = this.waveformsViewportRef.scrollWidth;
      this.dimensions.viewport.scrollLeft = this.waveformsViewportRef.scrollLeft;
      // we don't update the viewport scrollTop in here because we want a reference to the previous
      // scrollTop value so that we can pin the scroll position when holding control and scrolling.
    }
    if (this.waveformsContainerRef) {
      this.dimensions.viewportContentContainer.clientWidth = this.waveformsContainerRef.clientWidth;
    }
    if (this.canvasRef) {
      this.dimensions.canvas.rect = this.canvasRef.getBoundingClientRect();
      this.dimensions.canvas.clientWidth = this.canvasRef.clientWidth;
    }
  };

  /**
   * calculate the zoom range in [0,1] from the current zoom interval
   * where 0 = this.props.startTimeSecs
   * and 1 = this.props.endTimeSecs
   */
  private readonly getCurrentZoomIntervalRange = () => {
    return this.getZoomRangeFromInterval(this.getCurrentZoomInterval());
  };

  /**
   * calculate the zoom range in [0,1] for the time range provided
   * where 0 = this.props.startTimeSecs
   * and 1 = this.props.endTimeSecs
   */
  private readonly getZoomRangeFromInterval = (zoomInterval: WeavessTypes.TimeRange) => {
    const waveformDataRange =
      this.props.viewableInterval.endTimeSecs - this.props.viewableInterval.startTimeSecs;
    const startZoomRange =
      (zoomInterval.startTimeSecs - this.props.viewableInterval.startTimeSecs) / waveformDataRange;
    const endZoomRange =
      (zoomInterval.endTimeSecs - this.props.viewableInterval.startTimeSecs) / waveformDataRange;
    return this.setTimeRangePrecision([startZoomRange, endZoomRange]);
  };

  /**
   * Convert giving range to time interval using props start and end times
   *
   * @param zoomRange range 0 - 1 used in conversion
   * @returns time interval
   */
  private readonly convertRangeToTimeInterval = (
    zoomRange: [number, number]
  ): WeavessTypes.TimeRange => {
    const interval =
      this.props.viewableInterval.endTimeSecs - this.props.viewableInterval.startTimeSecs;
    const adjustedRange = this.setTimeRangePrecision(zoomRange);
    const startTimeSecs = adjustedRange[0] * interval + this.props.viewableInterval.startTimeSecs;
    const endTimeSecs = adjustedRange[1] * interval + this.props.viewableInterval.startTimeSecs;
    return { startTimeSecs, endTimeSecs };
  };

  /**
   * Calculates the current viewport range in fractional units
   *
   * @returns fractional viewport range in the format [0, 1]
   */
  private readonly getRangeFromCurrentViewport = (): [number, number] => {
    const labelWidthPx = this.labelWidthPx();
    const timeRangeLeft =
      this.dimensions.viewport.scrollLeft /
      (this.dimensions.viewportContentContainer.clientWidth - labelWidthPx);

    const timeRangeRight =
      (this.dimensions.viewport.scrollLeft + this.dimensions.canvas.clientWidth) /
      (this.dimensions.viewportContentContainer.clientWidth - labelWidthPx);

    // if calculations result in no zoom value then display not open
    if (timeRangeLeft === 0 && timeRangeRight === 0) {
      return undefined;
    }
    return this.setTimeRangePrecision([timeRangeLeft, timeRangeRight]);
  };

  /**
   * Calculate number of pixels to which the viewport should be scrolled, given the current zoom range
   * and viewport range.
   *
   * @param viewportRange a fractional range in the form [0, 1] showing what region of the whole loaded
   * range is in view.
   * @returns number of pixels from the left of the container
   */
  private readonly getNumberOfPixelsFromLeft = (viewportRange: [number, number]): number => {
    const zoomRange = this.getCurrentZoomIntervalRange();
    const theRange = zoomRange[1] - zoomRange[0];
    const labelWidthPx = this.labelWidthPx();
    const pixels = this.dimensions.viewport.clientWidth / theRange + labelWidthPx;
    return zoomRange[0] * (pixels - labelWidthPx) - viewportRange[0] * (pixels - labelWidthPx);
  };

  /**
   * Calculates a time interval corresponding to the scroll position of the viewport.
   * To function correctly, this.dimensions must have been set using updateTrackedDimensions.
   *
   * @returns a time interval for scroll bar range
   */
  private readonly getTimeIntervalFromViewport = (): WeavessTypes.TimeRange => {
    const viewportRange = this.getRangeFromCurrentViewport();
    // if viewport is not set then no time interval not possible to calculate
    if (!viewportRange) {
      return undefined;
    }
    return this.convertRangeToTimeInterval(viewportRange);
  };

  private readonly setTimeRangePrecision = ([left, right]: [number, number]): [number, number] => {
    return [
      Number.parseFloat(left.toFixed(GL_DECIMAL_PRECISION)),
      Number.parseFloat(right.toFixed(GL_DECIMAL_PRECISION))
    ];
  };

  /**
   * onScroll handler for viewport scroll events.
   */
  private readonly onScroll = () => {
    if (!this.waveformsViewportRef || !this.stationComponentRefs) {
      return;
    }

    // check if zooming; if so enure the vertical scrollbar does not scroll
    if (HotkeyListener.isKeyDown(HotkeyListener.ModifierHotKeys.CONTROL)) {
      this.waveformsViewportRef.scroll({ top: this.dimensions.viewport.scrollTop }); // keep the position fixed at this location
    }
    // set this separately from the other dimensions because we use the old value to keep the position fixed.
    // if we were to set it in updateTrackedDimensions, then we would not have the previous scrollTop value
    // for use here.
    this.dimensions.viewport.scrollTop = this.waveformsViewportRef.scrollTop;

    // update zoom interval for use within
    this.updateTrackedDimensions();
    // If the scroll position has moved more than 1 pixel then
    const viewportRange = this.getRangeFromCurrentViewport();
    // if viewport is not set then no scrolling possible
    if (!viewportRange) {
      return;
    }
    const deltaPixels = this.getNumberOfPixelsFromLeft(viewportRange);
    if (Math.abs(deltaPixels) > 1) {
      this.setZoomIntervalInState(this.getTimeIntervalFromViewport());
    }
    this.postZoomUpdate();
  };

  /**
   * Zoom in on mouse wheel
   *
   * @param e
   */
  private readonly onWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    const modPercent = 0.4;
    if (e.ctrlKey || e.metaKey) {
      // compute current x position in [0,1] and zoom to that point
      const xFrac =
        (e.clientX - this.dimensions.canvas.rect.left) / this.dimensions.canvas.rect.width;

      if (e.deltaY > 0) {
        // zoom out
        this.zoomByPercentageToPoint(modPercent, xFrac);
      } else {
        // zoom in
        this.zoomByPercentageToPoint(-modPercent, xFrac);
      }
    }
  };

  /**
   * onKeyDown event handler
   *
   * @param e
   */
  private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
    // Hot key definitions
    const amplitudeScaleResetHotKey = this.props.initialConfiguration.hotKeys.amplitudeScaleReset;
    const maskCreateHotKey = this.props.initialConfiguration.hotKeys.maskCreate;
    // check for amplitude reset for all channels
    if (
      amplitudeScaleResetHotKey &&
      HotkeyListener.isHotKeyCommandSatisfied(e.nativeEvent, amplitudeScaleResetHotKey)
    ) {
      this.props.resetWaveformPanelAmplitudes();

      // Starting a new zoom reset amplitudes?
      this.resetAmplitudes();
    }
    // check for mask create hot key
    else if (
      maskCreateHotKey &&
      HotkeyListener.isHotKeyCommandSatisfied(e.nativeEvent, maskCreateHotKey)
    ) {
      this.brushType = BrushType.CreateMask;
    }
  };

  /**
   * onKeyUp event handler
   *
   * @param e
   */
  private readonly onKeyUp = (e: React.KeyboardEvent<HTMLDivElement>) => {
    const maskCreateHotKey = this.props.initialConfiguration.hotKeys.maskCreate;
    if (
      maskCreateHotKey &&
      maskCreateHotKey.indexOf(e.nativeEvent.code) > -1 &&
      !this.selectionStart
    ) {
      this.brushType = undefined;
    }
  };

  /**
   * Creates all of the markers.
   *
   * @param props the waveform panel props
   *
   * @returns an array JSX elements
   */
  private readonly createAllMarkers = (props: WaveformPanelProps): JSX.Element[] => [
    ...memoizedCreateVerticalMarkers(
      props.viewableInterval.startTimeSecs,
      props.viewableInterval.endTimeSecs,
      props.markers ? props.markers.verticalMarkers : undefined
    ),
    ...memoizedCreateMoveableMarkers(
      props.viewableInterval.startTimeSecs,
      props.viewableInterval.endTimeSecs,
      props.markers ? props.markers.moveableMarkers : undefined,
      this.getZoomRatio,
      () => this.dimensions.viewportContentContainer.clientWidth,
      () => this.dimensions.viewportContentContainer.clientWidth,
      props.events
        ? (marker: WeavessTypes.Marker) => {
            if (props.events.onUpdateMarker) {
              props.events.onUpdateMarker(marker);
            }
          }
        : undefined,
      this.labelWidthPx()
    ),
    ...memoizedCreateSelectionWindowMarkers(
      props.viewableInterval.startTimeSecs,
      props.viewableInterval.endTimeSecs,
      props.markers ? props.markers.selectionWindows : undefined,
      this.getZoomRatio,
      () => this.canvasRef,
      () => this.dimensions.viewportContentContainer.clientWidth,
      // TODO verify that this is correct for the panel level selection windows: this.waveformsViewportRef
      () => this.dimensions.viewportContentContainer.clientWidth,
      this.computeTimeSecsForMouseXFractionalPosition,
      this.onMouseMove,
      this.onMouseDown,
      this.onMouseUp,
      props.events && props.events.onMoveSelectionWindow
        ? (selection: WeavessTypes.SelectionWindow) => {
            props.events.onMoveSelectionWindow(selection);
          }
        : undefined,
      props.events && props.events.onUpdateSelectionWindow
        ? (selection: WeavessTypes.SelectionWindow) => {
            props.events.onUpdateSelectionWindow(selection);
          }
        : undefined,
      props.events && props.events.onClickSelectionWindow
        ? (selection: WeavessTypes.SelectionWindow, timeSecs: number) => {
            props.events.onClickSelectionWindow(selection, timeSecs);
          }
        : undefined,
      this.labelWidthPx()
    )
  ];

  /**
   * handler for the ref callback for stations. As a side effect, adds the station's
   * default channel to the visible list of channels.
   *
   * @param stationRef: the ref to the station provided by the React ref callback.
   * If null, will be a no-op.
   */
  private readonly setStationComponentRef = (stationRef: Station | null) => {
    if (this.stationComponentRefs && stationRef) {
      // id should be set when station is created.
      this.stationComponentRefs.set(stationRef.props.station.id, stationRef);
    }
  };

  /**
   * @returns a ref to the canvas element on which all waveforms are drawn.
   */
  private readonly getCanvasRef = () => this.canvasRef;

  /**
   * @returns the bounding client rectangle for the canvas.
   */
  private readonly getCanvasRect = () => this.dimensions.canvas.rect;

  /**
   * A set of converter functions that are passed to stations for converting position to
   * and from screen and time units.
   * */
  // eslint-disable-next-line react/sort-comp
  private readonly converters = {
    computeTimeSecsForMouseXFractionalPosition: this.computeTimeSecsForMouseXFractionalPosition,
    computeTimeSecsFromMouseXPixels: this.computeTimeSecsFromMouseXPixels,
    computeFractionOfCanvasFromMouseXPx: this.computeFractionOfCanvasFromXPositionPx
  };

  /**
   * @returns the ratio of the zoom duration divided by the total viewable duration.
   * This can be used to see how far zoomed in we are.
   */
  private readonly getZoomRatio = () =>
    (this.getCurrentZoomInterval().endTimeSecs - this.getCurrentZoomInterval().startTimeSecs) /
    (this.props.viewableInterval.endTimeSecs - this.props.viewableInterval.startTimeSecs);

  /**
   * @param timeRangeToCheck A time range to check to see if it is entirely in view
   * @returns Whether the time range provided is within the zoom interval (inclusive)
   */
  private readonly isWithinTimeRange = (timeRangeToCheck: WeavessTypes.TimeRange) =>
    timeRangeToCheck.startTimeSecs <= this.getCurrentZoomInterval().endTimeSecs &&
    timeRangeToCheck.endTimeSecs >= this.getCurrentZoomInterval().startTimeSecs;

  /**
   * Creates all of the stations. Parameters should be referentially stable for
   * optimal rendering.
   *
   * @param props the waveform panel props
   *
   * @returns an array JSX elements
   */
  private readonly createStationsJsx = ({
    stations,
    initialConfiguration,
    events,
    viewableInterval,
    shouldRenderWaveforms,
    shouldRenderSpectrograms,
    selections,
    updateMeasureWindow,
    getPositionBuffer,
    getBoundaries
  }: WaveformPanelProps): JSX.Element[] => {
    const stationElements: JSX.Element[] = [];
    // eslint-disable-next-line no-restricted-syntax
    for (const station of stations) {
      stationElements.push(
        <Station
          // data props
          key={station.id}
          ref={this.setStationComponentRef}
          initialConfiguration={initialConfiguration}
          displayInterval={viewableInterval}
          getZoomRatio={this.getZoomRatio}
          isWithinTimeRange={this.isWithinTimeRange}
          shouldRenderWaveforms={shouldRenderWaveforms}
          shouldRenderSpectrograms={shouldRenderSpectrograms}
          workerRpcs={this.workerRpcs}
          selections={selections ?? this.emptySelection}
          station={station}
          customLabel={this.props.customLabel}
          glMin={this.props.convertTimeToGL(viewableInterval.startTimeSecs)}
          glMax={this.props.convertTimeToGL(viewableInterval.endTimeSecs)}
          isMeasureWindow={!this.props.isControlledComponent} // measure window is uncontrolled
          canvasRef={this.getCanvasRef}
          getCanvasBoundingRect={this.getCanvasRect}
          getPositionBuffer={getPositionBuffer}
          getBoundaries={getBoundaries}
          renderWaveforms={this.renderWaveforms}
          converters={this.converters}
          events={events?.stationEvents}
          onMouseMove={this.onMouseMove}
          onMouseDown={this.onMouseDown}
          onMouseUp={this.onMouseUp}
          updateMeasureWindow={updateMeasureWindow || undefined}
          msrWindowWaveformAmplitudeScaleFactor={this.props.msrWindowWaveformAmplitudeScaleFactor}
        />
      );
    }
    return stationElements;
  };

  /**
   * If WEAVESS is contained inside of a div with flex layout, sizing it with height=100% doesn't work.
   */
  private createRootStyle(): React.CSSProperties & { '--weavess-scrollbar-track-margin': string } {
    if (this.props.flex) {
      return {
        flex: '1 1 0',
        position: 'relative',
        // this custom property is used in the css to set the webkit scrollbar margin (position the left side of the scrollbar).
        '--weavess-scrollbar-track-margin': `${this.labelWidthPx()}px`
      };
    }

    return {
      height: '100%',
      position: 'relative',
      width: '100%',
      boxSizing: 'content-box',
      // this custom property is used in the css to set the webkit scrollbar margin (position the left side of the scrollbar).
      '--weavess-scrollbar-track-margin': `${this.labelWidthPx()}px`
    };
  }

  /**
   * This function is scheduled as an animation frame during @function renderWaveforms. It is
   * cleared by subsequent @function renderWaveform calls, and then scheduled again in the resulting
   * animation frames.
   */
  private readonly onRenderWaveformsLoopEnd = () => {
    window.cancelAnimationFrame(this.prevRAFEnd);
    this.updateAmplitudes();
    this.debounceUpdateZoomInterval();
  };

  /**
   * Create a list of all the channels for all stations
   *
   * @returns list of channels
   */
  private readonly getStationsChannels = (): Channel[] => {
    let channels: Channel[] = [];
    if (this.stationComponentRefs) {
      this.stationComponentRefs.forEach(station => {
        channels = channels.concat(station.getChannelList());
      });
    }
    return channels;
  };

  /**
   * Find channel
   *
   * @params channelName
   * @returns Channel or undefined
   */
  private readonly findChannel = (channelName: string): Channel | undefined => {
    if (this.stationComponentRefs) {
      // using for-of syntax to allow us to bail as soon as we find the result.
      // eslint-disable-next-line no-restricted-syntax
      for (const station of this.stationComponentRefs.values()) {
        const channel = station.getChannelList().find(chan => chan.getChannelId() === channelName);
        if (channel) {
          return channel;
        }
      }
    }
    return undefined;
  };

  /**
   * resize the renderer to fit the new canvas size
   */
  private updateSize() {
    if (!this.canvasRef) return;
    if (this.props.isResizing) {
      // clear the display while we are resizing so we don't draw distorted images
      this.renderer.clear(true, true, true);
      defer(() => this.updateSize(), WeavessConstants.ONE_FRAME_MS);
      return;
    }

    this.updateTrackedDimensions();
    const width = this.canvasRef.offsetWidth;
    const height = this.canvasRef.offsetHeight;
    if (
      this.dimensions.canvas.offsetWidth !== width ||
      this.dimensions.canvas.offsetHeight !== height
    ) {
      this.dimensions.canvas.offsetWidth = width;
      this.dimensions.canvas.offsetHeight = height;
      this.renderer.setSize(width, height, false);
      this.zoom(this.getCurrentZoomInterval());
    }
  }

  /**
   * Handles a double click event.
   *
   * @param event
   */
  private readonly onDoubleClick = (event: React.MouseEvent<HTMLDivElement>) => {
    this.handleSingleDoubleClick.onDoubleClick(event, () => {
      // double click registered, clear mouse down state
      this.isMouseDown = undefined;
      this.setZoomIntervalInState(this.props.viewableInterval);
    });
  };

  /**
   * mouse down event handler
   *
   * @param e
   * @param xPct
   * @param channelId
   * @param timeSecs
   * @param isDefaultChannel
   */
  // eslint-disable-next-line complexity
  private readonly onMouseDown = (
    e: React.MouseEvent<HTMLDivElement>,
    xPct: number | undefined = undefined,
    channelId: string | undefined = undefined,
    timeSecs: number | undefined = undefined,
    isDefaultChannel: boolean | undefined = undefined
  ) => {
    // keep track of the mouse down state
    if (timeSecs) {
      // markers do not have time seconds, only track when on the waveform
      this.isMouseDown = { clientX: e.clientX, clientY: e.clientY };
    }
    // if the amplitude scaling hotkey is in use - do not brush
    this.setState({ rulerIsActive: true, rulerInitialPoint: { x: e.clientX, y: e.clientY } });

    // check if any keys are pressed on mouse down
    // zoom mode
    if (e.ctrlKey || e.metaKey) {
      this.brushType = BrushType.Zoom;
    }

    // set the zoom start point if a brush is being used
    if (this.brushType === BrushType.Zoom) {
      this.selectionStart = xPct;
    } else if (this.brushType === BrushType.CreateMask) {
      const disableMaskModification = isDefaultChannel
        ? this.props.initialConfiguration.defaultChannel.disableMaskModification
        : this.props.initialConfiguration.nonDefaultChannel.disableMaskModification;
      if (!disableMaskModification) {
        this.selectionStart = xPct;
      } else {
        this.selectionStart = undefined;
        this.brushType = undefined;
        toast.info(WeavessMessages.maskModificationDisabled);
      }
    }

    // Select channel if no channels selected and using CreateMask brush
    if (this.brushType === BrushType.CreateMask) {
      if (
        !this.props.selections ||
        !this.props.selections.channels ||
        this.props.selections.channels.length < 1
      ) {
        if (this.props.selectChannel && channelId) {
          this.props.selectChannel(channelId);
          this.needToDeselect = true;
        }
      }
    }
  };

  /**
   * mouse move event handler
   *
   * @param e
   * @param xPct
   * @param timeSecs
   */
  private readonly onMouseMove = (
    e: React.MouseEvent<HTMLDivElement>,
    xPct: number | undefined = undefined
  ) => {
    if (!this.selectionAreaRef) return;

    const width = this.dimensions.canvas.rect.width ?? 0;
    if (!xPct) {
      const leftOffset = this.dimensions.canvas.rect.left ?? 0;
      // eslint-disable-next-line no-param-reassign
      xPct = (e.clientX - leftOffset) / width;
    }

    // move the crosshair to the current pointer location
    if (this.crosshairRef) {
      this.crosshairRef.style.transform = `translateX(${xPct * width}px)`;
    }

    // if the user has moved more than 1% of the viewport, consider it an operation
    // Paint !
    if (this.selectionStart) {
      const fracToPct = 100;
      // minimum amount the mouse must move until it begins a brush effect
      // 0.01 = 1% of the current zoom range
      const minMovementDeltaFrac = 0.01;
      if (
        Math.abs(this.selectionStart - xPct) > minMovementDeltaFrac ||
        Math.abs(xPct - this.selectionStart) > minMovementDeltaFrac
      ) {
        if (this.startOfBrush) {
          this.selectionAreaRef.style.display = 'initial';
          this.startOfBrush = false;
        }
        const start = Math.min(this.selectionStart, xPct);
        const end = Math.max(this.selectionStart, xPct);
        const left = `${start * fracToPct}%`;
        const right = `${(1 - end) * fracToPct}%`;
        this.selectionAreaRef.style.left = left;
        this.selectionAreaRef.style.right = right;
        // eslint-disable-next-line
        if (this.brushType === BrushType.CreateMask) {
          // eslint-disable-next-line max-len
          this.selectionAreaRef.style.backgroundColor = 'rgba(145, 228, 151, .3)'; // ! should be set from user preferences
        } else {
          this.selectionAreaRef.style.backgroundColor = 'rgba(150,150,150,0.3)';
        }
      }
    }
  };

  private readonly onRulerMouseUp = () => {
    this.setState({ rulerIsActive: false, rulerInitialPoint: undefined });
  };

  /**
   * mouse up event handler
   *
   * @param event
   * @param xPct
   * @param channelId
   * @param timeSecs
   * @param isDefaultChannel
   */
  // eslint-disable-next-line complexity
  private readonly onMouseUp = (
    event: React.MouseEvent<HTMLDivElement>,
    xPct: number | undefined = undefined,
    channelId: string | undefined = undefined,
    timeSecs: number | undefined = undefined,
    isDefaultChannel: boolean | undefined = undefined
  ) => {
    // ignore any mouse up events if the mouse down flag is not set
    if (!this.isMouseDown) {
      return;
    }

    const mouseDown = this.isMouseDown;

    // track the mouse down state
    this.isMouseDown = undefined;

    // If the mouse is released *before* a brush stroke has been made
    // Cancel the stroke so as to not interfere with other mouse events
    if (this.startOfBrush) {
      if (this.selectionAreaRef) {
        this.selectionAreaRef.style.display = 'none';
      }
      this.selectionStart = undefined;
      this.brushType = undefined;
      this.startOfBrush = true;
    }
    if (!this.selectionAreaRef) return;

    if (!xPct) {
      const leftOffset = this.dimensions.canvas.rect.left;
      const { width } = this.dimensions.canvas.rect;
      // eslint-disable-next-line no-param-reassign
      xPct = (event.clientX - leftOffset) / width;
    }

    // eslint-disable-next-line no-nested-ternary
    const events = isDefaultChannel
      ? this.props.events?.stationEvents?.defaultChannelEvents?.events
        ? this.props.events?.stationEvents.defaultChannelEvents.events
        : undefined
      : this.props.events?.stationEvents?.nonDefaultChannelEvents?.events
      ? this.props.events?.stationEvents.nonDefaultChannelEvents.events
      : undefined;

    // if the user is zooming, perform the zoom
    if (this.brushType && !this.startOfBrush && this.selectionStart) {
      const scale = d3.scaleLinear().domain([0, 1]).range(this.getCurrentZoomIntervalRange());
      const start = Math.min(this.selectionStart, xPct);
      const end = Math.max(this.selectionStart, xPct);
      if (this.brushType === BrushType.Zoom) {
        const zoomInterval = this.convertRangeToTimeInterval([scale(start), scale(end)]);
        this.setZoomIntervalInState(this.checkMaxZoomInterval(zoomInterval));
      } else if (this.brushType === BrushType.CreateMask) {
        const scaleTime = d3
          .scaleLinear()
          .domain([0, 1])
          .range([
            this.props.viewableInterval.startTimeSecs,
            this.props.viewableInterval.endTimeSecs
          ]);

        if (events) {
          if (events.onMaskCreateDragEnd) {
            const channels = flatMap(
              this.props.stations.map<WeavessTypes.Channel[]>((s: WeavessTypes.Station) =>
                s.nonDefaultChannels
                  ? [s.defaultChannel, ...s.nonDefaultChannels]
                  : [s.defaultChannel]
              )
            );
            const channel = channels.find(c => c.id === channelId);
            if (channel) {
              // determine if there is an offset applied to the channel data
              const timeOffsetSeconds = channel.timeOffsetSeconds ? channel.timeOffsetSeconds : 0;
              events.onMaskCreateDragEnd(
                event,
                scaleTime(scale(start)) - timeOffsetSeconds,
                scaleTime(scale(end)) - timeOffsetSeconds,
                this.needToDeselect
              );
            }
          }
        }
        this.needToDeselect = false;
      }
    } else {
      // handle a single click event, only if the user has not moved the mouse
      // eslint-disable-next-line no-lonely-if
      if (event.clientX === mouseDown.clientX && event.clientY === mouseDown.clientY) {
        this.handleSingleDoubleClick.onSingleClickEvent(
          event,
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (e: React.MouseEvent<HTMLDivElement> | any) => {
            // handle onChannelClick event if not zooming or modifying a mask
            if (events) {
              if (events.onChannelClick && channelId && timeSecs) {
                events.onChannelClick(e, channelId, timeSecs);
              }
            }
          }
        );
      }
    }

    if (this.brushType !== BrushType.CreateMask) {
      this.selectionAreaRef.style.display = 'none';
      this.selectionStart = undefined;
      this.brushType = undefined;
      this.startOfBrush = true;
    }
  };

  /**
   * zoomPct in [0,1], x in [0,1]
   *
   * @param zoomPct positive zooms out, negative zooms in
   * @param x
   */
  private readonly zoomByPercentageToPoint = (zoomPct: number, x: number) => {
    const zoomRange = this.getCurrentZoomIntervalRange();
    const theRange = zoomRange[1] - zoomRange[0];
    const zoomIncrement = (theRange * zoomPct) / 2.0;
    const left = Math.max(zoomRange[0] - zoomIncrement * x, 0);
    const right = Math.min(zoomRange[1] + zoomIncrement * (1 - x), 1);
    const zoomInterval = this.convertRangeToTimeInterval([left, right]);
    this.setZoomIntervalInState(this.checkMaxZoomInterval(zoomInterval));
  };

  private readonly pan = (panPct: number) => {
    const deltaPx = this.dimensions.canvas.clientWidth * panPct;
    this.waveformsViewportRef.scrollLeft += deltaPx;
  };

  /**
   * left/right are numbers between [0,1] denoting the left/right percentages of [start,end]
   *
   * @param requestedStart Number between 0 and 1 inclusive. guarded against undefined
   * @param requestedEnd Number between 0 and 1 inclusive. guarded against undefined
   */
  private readonly zoom = (zoomInterval: WeavessTypes.TimeRange) => {
    if (
      !this.waveformsContainerRef ||
      !this.canvasRef ||
      !this.waveformsViewportRef ||
      !zoomInterval ||
      zoomInterval.startTimeSecs === undefined ||
      zoomInterval.endTimeSecs === undefined
    ) {
      return;
    }

    // If the zoom interval doesn't change but the viewableInterval does
    // the zoom interval may need to be clamped
    const clampedZoomInterval = this.clampToMaxZoomInterval(zoomInterval);
    const scale = d3
      .scaleLinear()
      .domain([this.props.viewableInterval.startTimeSecs, this.props.viewableInterval.endTimeSecs])
      .range([0, 1])
      .clamp(false); // do not clamp so that we can guarantee the correct requestedRange is respected.
    let start = scale(clampedZoomInterval.startTimeSecs);
    let end = scale(clampedZoomInterval.endTimeSecs);
    const requestedRange = Math.min(end - start, 1);
    if (start < 0) {
      start = 0;
      end = start + requestedRange;
    }
    if (end > 1) {
      end = 1;
      start = end - requestedRange;
    }
    if (end <= start) {
      const minDelta = 0.001;
      end = start + minDelta;
    }

    const labelWidthPx = this.labelWidthPx();
    const pixels = Math.round(this.dimensions.canvas.clientWidth / (end - start));
    this.waveformsContainerRef.style.width = `${pixels + labelWidthPx}px`;
    this.waveformsViewportRef.scrollLeft = Math.round(start * pixels);

    // update since changes made to browser div before determining viewport time interval
    this.updateTrackedDimensions();
  };

  /**
   * Has current state zoom interval reached either max element or max resolution limit
   *
   * @returns whether the current zoom interval provided is at max
   */
  private readonly hasCurrentZoomIntervalReachedMax = () => {
    return (
      this.isZoomIntervalAtMaxElementWidth(this.state.zoomTimeInterval) ||
      this.isZoomIntervalAtMaxResolution(this.state.zoomTimeInterval)
    );
  };

  /**
   * Notify user max zoom has been reached
   */
  private readonly notifyMaxZoomHasBeenReached = (): void => {
    toast.info(`Max zoom limit reached.`, { toastId: 'Max zoom limit reached.' });
  };

  /**
   * Returns true if the zoom interval at or smaller (zooming in) on current zoom interval.
   */
  private readonly isZoomIntervalSmallerThanCurrent = (
    zoomInterval: WeavessTypes.TimeRange
  ): boolean => {
    const currentZoomInterval = this.getCurrentZoomInterval();
    if (
      zoomInterval.startTimeSecs >= currentZoomInterval.startTimeSecs &&
      zoomInterval.endTimeSecs <= currentZoomInterval.endTimeSecs
    ) {
      return true;
    }
    return false;
  };

  /**
   * @param zoomInterval A zoom interval to check
   * @returns true if the provided interval would cause us to zoom so much that we would be creating DOM elements
   * that are larger than the browser supports.
   */
  private readonly isZoomIntervalAtMaxElementWidth = (zoomInterval: WeavessTypes.TimeRange) => {
    const zoomRange = this.getZoomRangeFromInterval(zoomInterval);
    const widthPx = Math.ceil(this.dimensions.canvas.clientWidth / (zoomRange[1] - zoomRange[0]));

    // On initial load shortly after mounting canvas client width can be 0 resulting in false positive
    return (
      widthPx > 0 &&
      widthPx >= (this.maxViewportSizePx - this.labelWidthPx()) * MAX_DIV_TOLERANCE_PERCENT
    );
  };

  /**
   * @param zoomInterval A zoom interval to check
   * @returns true if the provided interval would cause us to zoom so much that we pass the maximum time resolution
   * allowed, which is 100 microseconds.
   */
  private readonly isZoomIntervalAtMaxResolution = (zoomInterval: WeavessTypes.TimeRange) => {
    const minRange = (1 / MICROSECONDS_IN_SECOND) * 100; // One hundred microseconds is the min range we allow
    return (
      zoomInterval.endTimeSecs - zoomInterval.startTimeSecs <=
      minRange * MAX_RESOLUTION_TOLERANCE_PERCENT
    );
  };

  /**
   * @param zoomInterval the interval to check
   * @returns a zoom interval that is clamped to the maximum element width, if it would
   * exceed that width.
   */
  private readonly calculateMaxElementZoomInterval = (
    zoomInterval: WeavessTypes.TimeRange
  ): WeavessTypes.TimeRange => {
    const zoomRange = this.getZoomRangeFromInterval(zoomInterval);
    const targetWidthPx = this.maxViewportSizePx - this.labelWidthPx();
    const newRange = this.dimensions.canvas.clientWidth / targetWidthPx;
    const addRange = (newRange - (zoomRange[1] - zoomRange[0])) / 2;
    zoomRange[0] -= addRange;
    zoomRange[1] += addRange;
    return this.convertRangeToTimeInterval([zoomRange[0], zoomRange[1]]);
  };

  /**
   * @param zoomInterval the interval to check
   * @returns a zoom interval that is clamped to the minimum time interval,
   * if it would exceed that size
   */
  private readonly calculateMaxTimeResolutionZoomInterval = (
    zoomInterval: WeavessTypes.TimeRange
  ): WeavessTypes.TimeRange => {
    const minRange = (1 / MICROSECONDS_IN_SECOND) * 100; // One hundred microseconds is the min range we allow
    const midpoint =
      (zoomInterval.endTimeSecs - zoomInterval.startTimeSecs) / 2 + zoomInterval.startTimeSecs;
    return {
      startTimeSecs: midpoint - minRange / 2,
      endTimeSecs: midpoint + minRange / 2
    };
  };

  /**
   * Checks zoom interval for two limits. First if reached max browser width in pixels
   * second if interval is less than 1 microsecond
   *
   * @param zoomInterval
   * @returns limited zoom interval if limit is reached else checked zoom interval
   */
  private readonly clampToMaxZoomInterval = (
    zoomInterval: WeavessTypes.TimeRange
  ): WeavessTypes.TimeRange => {
    // If zoom has reached max browser width in pixels set limit and warn
    // If the display is behind another window max width is 0 and clamping breaks
    if (this.maxViewportSizePx !== 0 && this.isZoomIntervalAtMaxElementWidth(zoomInterval)) {
      return this.calculateMaxElementZoomInterval(zoomInterval);
    }

    // If zoom interval is sub millisecond, set zoom interval to limit and warn
    if (this.isZoomIntervalAtMaxResolution(zoomInterval)) {
      return this.calculateMaxTimeResolutionZoomInterval(zoomInterval);
    }
    return zoomInterval;
  };

  /**
   * After zoom update update waveform panel
   */
  private readonly postZoomUpdate = () => {
    // Sets station to show `mask indicator` on station's label
    // if any masks are within the zoom range
    // TODO: does this have to be here or could it move down to the station componentDidUpdate method?
    if (this.waveformsViewportRef && this.stationComponentRefs) {
      this.stationComponentRefs.forEach(station => {
        station.updateMaskLabels();
      });
    }

    this.renderWaveforms();
    if (this.timeAxisRef) {
      this.timeAxisRef.update();
    }
  };

  /**
   * Render currently visible waveforms to the canvas.
   * Note: This is not a React render, this is drawing to the canvas. It uses
   * `requestAnimationFrame` in order to queue up work to draw the waveforms
   * to the canvas at the most opportune time. This will happen asynchronously,
   * and not necessarily because of a React render.
   *
   * @param options shouldCallAnimationLoopEnd sets whether {@see onRenderWaveformsLoopEnd} should be triggered (true by default)
   * which can be used to prevent infinite loops.
   * shouldUpdateDimensions sets whether the animation frame should recalculate the dimensions of the DOM (expensive). True by default.
   */
  private readonly renderWaveforms = (options?: AnimationFrameOptions): void => {
    const { shouldCallAnimationLoopEnd = true } = options ?? {
      shouldCallAnimationLoopEnd: true
    };
    // don't render yet if we are resizing
    if (this.props.isResizing !== undefined && this.props.isResizing) {
      defer(this.renderWaveforms, WeavessConstants.ONE_FRAME_MS);
      return;
    }

    window.cancelAnimationFrame(this.prevRAF);
    window.cancelAnimationFrame(this.prevRAFEnd);
    this.prevRAF = window.requestAnimationFrame(
      this.renderWaveformsAnimationFrame(shouldCallAnimationLoopEnd)
    );
  };

  /**
   * The function called by the requestAnimationFrame scheduled in @function renderWaveforms.
   * This actually results in drawing the waveforms.
   */
  private readonly renderWaveformsAnimationFrame = (shouldCallAnimationLoopEnd = true) => () => {
    // if we don't have a set size to display, abort
    if (
      !this.weavessRootRef ||
      !this.stationComponentRefs ||
      !this.waveformsViewportRef ||
      !this.canvasRef ||
      this.dimensions.viewport.clientHeight === 0 ||
      this.dimensions.viewport.clientWidth === 0
    ) {
      return;
    }
    /**
     * Schedule a renderWaveformsLoopEnd call. This can be cleared out by a subsequent @function renderWaveforms call.
     * This will only end up firing if we don't schedule another animation frame using @function renderWaveforms.
     *
     */
    if (shouldCallAnimationLoopEnd) {
      window.cancelAnimationFrame(this.prevRAFEnd);
      this.prevRAF = window.requestAnimationFrame(this.onRenderWaveformsLoopEnd);
    }

    this.renderer.setScissorTest(true);

    this.getStationsChannels().forEach(channel => {
      if (channel) {
        channel.renderScene(this.renderer, this.dimensions.canvas.rect);
      }
    });

    this.renderer.setScissorTest(false);
  };

  /**
   * The amount of pixels allocated for the label
   *
   * @returns number of pixels
   */
  private readonly labelWidthPx = (): number => {
    return (
      this.props.initialConfiguration.labelWidthPx || WeavessConstants.DEFAULT_LABEL_WIDTH_PIXELS
    );
  };

  /**
   * The amount of pixels allocated for the label and scrollbar widths
   *
   * @returns number of pixels
   */
  private readonly labelWithScrollbarWidthPx = (): number => {
    return this.labelWidthPx() + WeavessConstants.SCROLLBAR_WIDTH_PIXELS;
  };

  private readonly setMaxViewportSizePx = (maxPx: number) => {
    this.maxViewportSizePx = maxPx;
  };

  /**
   * React component lifecycle
   */
  public render(): JSX.Element {
    const waveformComponents = this.memoizedCreateStationsJsx(this.props);
    const markers = this.createAllMarkers(this.props);
    const weavessRootStyle = this.createRootStyle();
    return (
      <HotkeyHandler
        zoomInRatio={this.props.hotkeyZoomInRatio}
        zoomOutRatio={this.props.hotkeyZoomOutRatio}
        zoomByPercentageToPoint={this.zoomByPercentageToPoint}
        pan={this.pan}
        panRatio={this.props.panRatio}
      >
        <div
          className="weavess-wp"
          ref={ref => {
            this.weavessRootRef = ref;
          }}
          style={weavessRootStyle}
          onDoubleClick={this.onDoubleClick}
        >
          <canvas
            className="weavess-wp-canvas"
            ref={canvas => {
              if (canvas) {
                this.canvasRef = canvas;
                this.dimensions.canvas.rect = canvas?.getBoundingClientRect();
                this.dimensions.canvas.clientWidth = canvas.clientWidth;
              }
            }}
            style={{
              width: `calc(100% - ${this.labelWithScrollbarWidthPx()}px)`,
              height: `calc(100% - ${WeavessConstants.DEFAULT_XAXIS_HEIGHT_PIXELS}px)`,
              left: `${this.labelWidthPx()}px`
            }}
          />
          <div className="weavess-wp-container">
            <div className="weavess-wp-container-1">
              <div className="weavess-wp-container-2">
                <div className="weavess-wp-container-3">
                  <ViewportMaxSizer setMaxViewportSizePx={this.setMaxViewportSizePx} />
                  <div
                    className="weavess-wp-container-viewport"
                    ref={ref => {
                      this.waveformsViewportRef = ref;
                    }}
                    // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex
                    tabIndex={0}
                    onWheel={this.onWheel}
                    onScroll={this.onScroll}
                    onKeyDown={this.onKeyDown}
                    onKeyUp={this.onKeyUp}
                  >
                    <div
                      className="weavess-wp-container-viewport-content"
                      ref={waveformsContainer => {
                        this.waveformsContainerRef = waveformsContainer;
                      }}
                    >
                      {waveformComponents}
                      <div
                        className="weavess-wp-container-viewport-content-markers"
                        style={{
                          width: `calc(100% - ${this.labelWithScrollbarWidthPx()}px)`,
                          left: `${this.labelWidthPx()}px`
                        }}
                      >
                        {markers}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div
                className="weavess-wp-container-overlay"
                style={{
                  width: `calc(100% - ${this.labelWithScrollbarWidthPx()}px)`
                }}
              >
                <div
                  className="weavess-wp-container-overlay-cross-hair"
                  ref={ref => {
                    this.crosshairRef = ref;
                  }}
                />
                <Ruler
                  isActive={this.state.rulerIsActive}
                  initialPoint={this.state.rulerInitialPoint}
                  containerDimensions={this.dimensions}
                  computeTimeSecsForMouseXFractionalPosition={
                    this.computeTimeSecsForMouseXFractionalPosition
                  }
                  onRulerMouseUp={this.onRulerMouseUp}
                />
                <div
                  className="weavess-wp-container-overlay-selection-area"
                  ref={ref => {
                    this.selectionAreaRef = ref;
                  }}
                />
              </div>
            </div>
            {this.props.stations.length > 0 ? (
              <TimeAxis
                ref={ref => {
                  this.timeAxisRef = ref;
                }}
                displayInterval={this.getCurrentZoomInterval()}
                borderTop
                labelWidthPx={this.labelWidthPx()}
                scrollbarWidthPx={WeavessConstants.SCROLLBAR_WIDTH_PIXELS}
                label={this.props.initialConfiguration.xAxisLabel}
              />
            ) : (
              []
            )}
          </div>
          <TimeRange labelWidthPx={this.labelWidthPx()}>
            {timeRangeDisplayString(this.getCurrentZoomInterval())}
          </TimeRange>
        </div>
      </HotkeyHandler>
    );
  }
}
// eslint-disable-next-line max-lines
