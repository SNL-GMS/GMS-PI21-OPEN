import type React from 'react';

/**
 * An interface containing all public methods and variables of the
 * weavess WaveformPanel instance. We expose this here, explicitly,
 * to avoid circular dependencies.
 */
export interface WaveformPanelInstance {
  refresh(): void;
  resetAmplitudes(): void;
  computeTimeSecsForMouseXFractionalPosition(mouseXPositionFraction: number): number;
  computeTimeSecsFromMouseXPixels(mouseXPx: number): number;
  computeFractionOfCanvasFromXPositionPx(xPositionPx: number): number;
  getCanvasBoundingClientRect(): DOMRect | undefined;
  clearBrushStroke(): void;
  getOrderedVisibleChannelNames(): string[];
  getChannelWaveformYAxisBounds(channelName: string): YAxisBounds | undefined;
  getCurrentZoomInterval(): TimeRange;
  zoomToTimeWindow(zoomInterval: TimeRange): void;
}

/**
 * An interface containing all public methods and variables of the
 * weavess instance. We expose this here, explicitly,
 * to avoid circular dependencies.
 */
export interface WeavessInstance {
  readonly waveformPanelRef: WaveformPanelInstance | null;
  readonly measureWindowContainerRef: HTMLDivElement | null;
  readonly measureWindowPanelRef: WaveformPanelInstance | null;
  refresh(): void;
  convertTimeToGL(timeSec: number): number;
  isMeasureWindowVisible(): boolean;
  clearBrushStroke(): void;
  toggleMeasureWindowVisibility(): void;
  toggleRenderingContent(): void;
  toggleShouldRenderWaveforms(): void;
  toggleShouldRenderSpectrograms(): void;
  resetWaveformPanelAmplitudes(): void;
}
/**
 * Type of display
 */
export enum DisplayType {
  /** String representation of line type 'LINE' */
  LINE = 'LINE',

  /** String representation of line type 'SCATTER' */
  SCATTER = 'SCATTER'
}

/**
 * Type of line
 */
export enum LineStyle {
  /** String representation of solid line */
  SOLID = 'solid',

  /** String representation of dashed line */
  DASHED = 'dashed'
}

/**
 * Distance value's units degrees or kilometers
 */
export enum DistanceUnits {
  /** String representation of 'degrees' */
  degrees = 'degrees',

  /** String representation of 'km' */
  km = 'km'
}

// Enum to clarify pan direction
export enum PanType {
  Left,
  Right
}

/**
 * The display mode options for the waveform display.
 */
export enum WaveformDisplayMode {
  DEFAULT = 'Default',
  MEASUREMENT = 'Measurement'
}

export interface YAxisBounds {
  /** The height in percentage */
  heightInPercentage: number;

  /** Max amplitude as a number */
  maxAmplitude: number;

  /** Min amplitude as a number */
  minAmplitude: number;
}

/**
 * Time range of start time and end time
 */
export interface TimeRange {
  /** Start Time in seconds */
  startTimeSecs: number;

  /** End Time in seconds */
  endTimeSecs: number;
}

export interface MeasureWindowSelection {
  /** Station Id as a string */
  stationId: string;

  /** Channel config from the selection */
  channel: Channel;

  /** Epoch seconds of the start */
  startTimeSecs: number;

  /** Epoch seconds of the end */
  endTimeSecs: number;

  /** Indicates if default channel (used for specific event handling) */
  isDefaultChannel: boolean;

  /** Waveform amplitude scale factor  */
  waveformAmplitudeScaleFactor: number;

  /** Callback to remove the measure window selection div */
  removeSelection?(): void;
}

/**
 * Configuration object
 */
export interface Configuration {
  /** Default channel height in pixels */
  readonly defaultChannelHeightPx?: number;

  /** Label width in pixels */
  readonly labelWidthPx?: number;

  /** (optional) x-axis label */
  readonly xAxisLabel?: string;

  /** If true, then do not show the y-axis on the label */
  readonly suppressLabelYAxis?: boolean;

  /** true if waveforms should be rendered; false otherwise */
  readonly shouldRenderWaveforms: boolean;

  /** true if spectrograms should be rendered; false otherwise */
  readonly shouldRenderSpectrograms: boolean;

  /** Configuration of hot keys */
  readonly hotKeys: HotKeysConfiguration;

  /** Default channel */
  readonly defaultChannel: ChannelConfiguration;

  /** Non default channel */
  readonly nonDefaultChannel: ChannelConfiguration;

  readonly backgroundColor: string;

  readonly waveformDimPercent: number;

  /**
   * Custom color scale. Returns a color
   * as a string for the given value.
   */
  colorScale?(value: number): string;
}

/**
 * Hot Keys
 */
export interface HotKeysConfiguration {
  /** Hot key for scaling amplitude */
  amplitudeScale?: string;

  /** Hot key for resetting amplitude of single channel */
  amplitudeScaleSingleReset?: string;

  /** Hot key for resetting amplitude globally */
  amplitudeScaleReset?: string;

  /** Hot key for drawing the measure window (hold this key + click and drag) */
  drawMeasureWindow?: string;

  /** Hot key for creating mask */
  maskCreate?: string;
}

/**
 * Channel Configuration
 */
export interface ChannelConfiguration {
  /** Indicate whether measure window is on or not */
  disableMeasureWindow?: boolean;

  /** Indicate whether mask modification is available */
  disableMaskModification?: boolean;
}

export interface LabelProps {
  /** Channel configuration (holds the data) */
  channel: Channel;

  /** Boolean is default channel */
  isDefaultChannel: boolean;

  /** Does have sub channels */
  isExpandable: boolean;

  /** Displaying sub channels */
  expanded: boolean;

  /** The y-axis bounds */
  yAxisBounds: YAxisBounds[];

  /* The selections */
  selections: Selections;

  /** Toggles red M when mask(s) is in view */
  showMaskIndicator: boolean;

  /** Distance */
  distance: number;

  /** Distance units */
  distanceUnits: DistanceUnits;

  /** Azimuth */
  azimuth: number;

  /** Added Channel Name to help keep track
   * which channel the label belongs to */
  channelName: string;

  /** (optional) callback events Ex on label click */
  events?: LabelEvents;

  /** Defines a custom component for displaying a custom label */
  customLabel?: React.FunctionComponent<LabelProps>;

  suppressLabelYAxis?: boolean;

  /** Tooltip for the Channel Label */
  channelLabelTooltip?: string;
}

/**
 * Station configuration
 */
export interface Station {
  /** Id of station */
  id: string;

  /** Name of station */
  name: string;

  /** Default channel information for station */
  defaultChannel: Channel;

  /** Non-default channels for station */
  nonDefaultChannels?: Channel[];

  /** Indicates if the child weavess channels are displayed (label expanded) */
  areChannelsShowing?: boolean;

  /** Distance of station */
  distance?: number;

  /** Units for distance */
  distanceUnits?: DistanceUnits;

  /** Distance of station */
  azimuth?: number;
}

/** Specifies a range with a min and max */
export interface Range {
  /** the min */
  min?: number;
  /** the max */
  max?: number;
}

/**
 * Channel configuration
 */
export interface Channel {
  /** Id of channel */
  id: string;

  /** Name of channel */
  name: string | JSX.Element;

  /** Type of channel */
  channelType?: string;

  /** Height of the channel */
  height?: number;

  /** The number of seconds the data should be offset  */
  timeOffsetSeconds?: number;

  /** The arrival time of the station nearest the open event */
  baseStationTime?: number;

  /** Waveform content */
  waveform?: ChannelWaveformContent;

  /** Spectrogram content */
  spectrogram?: ChannelSpectrogramContent;

  /** default display range scale */
  defaultRange?: Range;

  /** the y axis ticks */
  yAxisTicks?: number[];

  /** Collection of markers to be rendered on the channel */
  markers?: Markers;

  /** Tooltip for the Channel Label */
  channelLabelTooltip?: string;

  /** Distance */
  distance?: number;

  /** Distance units */
  distanceUnits?: DistanceUnits;

  /** Azimuth */
  azimuth?: number;
}
export interface ChannelWaveformContent {
  /** Id of channel segment */
  channelSegmentId: string;

  /** Collection of channel segments */
  channelSegmentsRecord: Record<string, ChannelSegment[]>;

  /** Collection of markers */
  markers?: Markers;

  /** Collection of masks */
  masks?: Mask[];

  /** Collection of signal detections */
  signalDetections?: PickMarker[];

  /** Collection of predictive phases */
  predictedPhases?: PickMarker[];

  /** Collection of theoretical phase windows */
  theoreticalPhaseWindows?: TheoreticalPhaseWindow[];
}

/** Channel Segment Boundaries contains:
 *  Amplitude min, max, avg and offset for each waveform content */
export interface ChannelSegmentBoundaries {
  /** Maximum value of top */
  topMax: number;

  /** Maximum value of bottom */
  bottomMax: number;

  /** Average of channel */
  channelAvg: number;

  /** The maximum magnitude of this channel */
  offset: number;

  /** Channel segment id */
  channelSegmentId: string;

  /** Samples count used to compute average helps
      when merging multiple boundaries */
  samplesCount?: number;

  /** topMax epoch seconds found */
  topMaxSecs?: number;

  /** bottomMax epoch seconds found */
  bottomMaxSecs?: number;
}

export interface ChannelSpectrogramContent {
  /** Spectrogram description */
  description?: string;

  /** Color of the label for the description */
  descriptionLabelColor?: string;

  /** Epoch start time in seconds */
  startTimeSecs: number;

  /** The time step of the spectrogram data (x-axis) */
  timeStep: number;

  /** The frequency step of the spectrogram data (y-axis) */
  frequencyStep: number;

  /**
   * The spectrogram data (time x frequency)
   * Provides the powers or intensity of the spectrogram
   */
  data: number[][];

  /** Collection of markers */
  markers?: Markers;

  /** Collection of signal detections */
  signalDetections?: PickMarker[];

  /** Collection of predictive phases */
  predictedPhases?: PickMarker[];

  /** Collection of theoretical phase windows */
  theoreticalPhaseWindows?: TheoreticalPhaseWindow[];
}

/** Channel Default Configuration */
export interface ChannelDefaultConfiguration {
  /** Display type */
  displayType: DisplayType[];

  /** Point size */
  pointSize: number;

  /** Color as a string */
  color: string;
}

export const UNFILTERED = 'unfiltered';

/** Channel Segment */
export interface ChannelSegment {
  /** Channel Name of the Waveform */
  channelName: string;

  /** Waveform filter of the Waveform the default (Raw) data is 'unfiltered' */
  wfFilterId: string;

  /** Flags this channel segment is selected
   * when corresponding SD is selected */
  isSelected: boolean;

  /** Collection of data segments */
  dataSegments: DataSegment[];

  /** Channel segment description */
  description?: string;

  /** Color of the label for the description */
  descriptionLabelColor?: string;

  /** processed Amplitude values during Typed Array conversion */
  channelSegmentBoundaries?: ChannelSegmentBoundaries;
}

/**
 * Represents data values by sample start time and sample rate
 */
export interface DataBySampleRate {
  /** Epoch start time in seconds */
  startTimeSecs: number;

  /** Epoch end time in seconds */
  endTimeSecs: number;

  /** Sample Rate */
  sampleRate: number;

  /** Collection representing data segment data */
  values: Float32Array | number[];
}

export interface DataClaimCheck extends Omit<DataBySampleRate, 'values'> {
  /** Collection representing data segment data */
  values: Float32Array | number[] | undefined;

  /** Claim check ID. Used to fetch and perform operations on this waveform sample data. */
  id: string;

  /** Used in conversion of waveform to set x (time) position */
  domainTimeRange: TimeRange;
}

/**
 * Represents a time value pair.
 */
export interface TimeValuePair {
  timeSecs: number;
  value: number;
}

/**
 * Represents data values by time.
 */
export interface DataByTime {
  values: TimeValuePair[] | Float32Array;
}

/** Data Segment */
export interface DataSegment {
  /** Color */
  color?: string;

  /** Display type */
  displayType?: DisplayType[];

  /** Point size */
  pointSize?: number;

  /** Collection representing data segment data */
  data: DataBySampleRate | DataByTime | DataClaimCheck;
}

/** Pick Marker Configuration */
export interface PickMarker {
  /** unique id of the pick marker */
  id: string;

  /** Time in seconds of the pick marker */
  timeSecs: number;

  /** Uncertainty in seconds of the time */
  uncertaintySecs: number;

  /** Show uncertainty bars on the channel */
  showUncertaintyBars: boolean;

  /** Label of pick marker */
  label: string;

  /** Color of pick marker */
  color: string;

  /** Is this pick in conflict */
  isConflicted: boolean;

  /** Is the marker selected i.e. SignalDetection pick markers */
  isSelected: boolean;

  /** Is this pick disabled for some other reason */
  isDisabled?: boolean;

  /**
   * A filter provided for the pick marker
   *
   * style.filter = "none | blur() | brightness() | contrast() | drop-shadow() |
   *                 grayscale() | hue-rotate() | invert() | opacity() | saturate() | sepia()"
   */
  filter?: string;
}

/** Theoretical Phase Window */
export interface TheoreticalPhaseWindow {
  /** Id of theoretical phase window */
  id: string;

  /** Epoch start time in seconds */
  startTimeSecs: number;

  /** Epoch end time in seconds */
  endTimeSecs: number;

  /** Label */
  label: string;

  /** Color */
  color: string;
}

/** Mask */
export interface Mask {
  /** Id of mask */
  id: string;

  /** Epoch start time of mask in seconds */
  startTimeSecs: number;

  /** Epoch end time of mask in seconds */
  endTimeSecs: number;

  /** Color of mask */
  color: string;
}

/** Event Label */
export interface LabelEvents {
  /**
   * Event handler for channel expansion
   *
   * @param channelId a Channel Id as a string
   */
  onChannelExpanded?(channelId: string): void;
  /**
   * Event handler for channel collapse
   *
   * @param channelId a Channel Id as a string
   */
  onChannelCollapsed?(channelId: string): void;
  /**
   * Event handler for when a channel label is clicked
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param channelId a Channel Id as a string
   */
  onChannelLabelClick?(e: React.MouseEvent<HTMLDivElement>, channelId: string): void;

  /**
   * Event handler for when a channel label is right-clicked
   *
   * @param e mouse event
   * @param channelId name of the channel that was clicked as a string
   * @param isDefaultChannel boolean that specifies if the channel is top level or sub channel
   */
  onContextMenu?(
    e: React.MouseEvent<HTMLDivElement>,
    channelId: string,
    amplitidueMinValue: number,
    amplitudeMaxValue: number,
    isDefaultChannel: boolean
  ): void;
}

/** Channel content events */
export interface ChannelContentEvents {
  /**
   * Event handler for when context menu is displayed
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param channelId a Channel Id as a string
   */
  onContextMenu?(e: React.MouseEvent<HTMLDivElement>, channelId: string): void;

  /**
   * Event handler for when channel is clicked
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param channelId a Channel Id as a string
   * @param timeSecs epoch seconds of where clicked in respect to the data
   */
  onChannelClick?(e: React.MouseEvent<HTMLDivElement>, channelId: string, timeSecs: number): void;

  /**
   * Event handler for when signal detection is clicked
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param sdId a Signal Detection Id as a string
   */
  onSignalDetectionClick?(e: React.MouseEvent<HTMLDivElement>, sdId: string): void;

  /**
   * Event handler for when context menu is displayed
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param channelId a Channel Id as a string
   * @param sdId a Signal Detection Id as a string
   */
  onSignalDetectionContextMenu?(
    e: React.MouseEvent<HTMLDivElement>,
    channelId: string,
    sdId: string
  ): void;

  /**
   * Event handler for when a signal detection drag ends
   *
   * @param sdId a Signal Detection Id as a string
   * @param timeSecs epoch seconds of where drag ended in respect to the data
   */
  onSignalDetectionDragEnd?(sdId: string, timeSecs: number): void;

  /**
   * Event handler for when predictive phase is clicked
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param id a predictive phase Id as a string
   */
  onPredictivePhaseClick?(e: React.MouseEvent<HTMLDivElement>, id: string): void;

  /**
   * Event handler for when context menu is displayed
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param channelId a Channel Id as a string
   * @param id a Predictive Phase Id as a string
   */
  onPredictivePhaseContextMenu?(
    e: React.MouseEvent<HTMLDivElement>,
    channelId: string,
    id: string
  ): void;

  /**
   * Event handler for when a predictive phase drag ends
   *
   * @param id a predictive phase Id as a string
   * @param timeSecs epoch seconds of where drag ended in respect to the data
   */
  onPredictivePhaseDragEnd?(id: string, timeSecs: number): void;

  /**
   * Event handler for clicking on mask
   *
   * @param event mouse event as React.MouseEvent<HTMLDivElement>
   * @param channelId a Channel Id as a string
   * @param maskId mask Ids as a string array
   * @param maskCreateHotKey (optional) indicates a hotkey is pressed
   */
  onMaskClick?(
    event: React.MouseEvent<HTMLDivElement>,
    channelId: string,
    maskId: string[],
    maskCreateHotKey?: boolean
  ): void;

  /**
   * Event handler for context clicking on a mask
   *
   * @param event mouse event as React.MouseEvent<HTMLDivElement>
   * @param channelId a Channel Id as a string
   * @param masks mask Ids as a string array
   */
  onMaskContextClick?(event: React.MouseEvent<HTMLDivElement>, channelId: string, masks: string[]);

  /**
   * Event handler for when a create mask drag ends
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param startTimeSecs epoch seconds of where clicked started
   * @param endTimeSecs epoch seconds of where clicked ended
   * @param needToDeselect boolean that indicates to deselect the channel
   */
  onMaskCreateDragEnd?(
    e: React.MouseEvent<HTMLDivElement>,
    startTimeSecs: number,
    endTimeSecs: number,
    needToDeselect: boolean
  ): void;

  /**
   * Event handler that is invoked and handled when the Measure Window is updated.
   *
   * @param isVisible true if the measure window is updated
   * @param channelId the unique channel id of the channel that the measure window on;
   * channel id is undefined if the measure window is not visible
   * @param startTimeSecs the start time in seconds of the measure window;
   * start time seconds is undefined if the measure window is not visible
   * @param endTimeSecs the end time in seconds of the measure window;
   * end time seconds is undefined if the measure window is not visible
   * @param heightPx the height in pixels of the measure window;
   * height pixels is undefined if the measure window is not visible
   */
  onMeasureWindowUpdated?(
    isVisible: boolean,
    channelId?: string,
    startTimeSecs?: number,
    endTimeSecs?: number,
    heightPx?: number
  ): void;

  /**
   * Event handler for updating markers value
   *
   * @param channelId the unique channel id of the channel
   * @param marker the marker
   */
  onUpdateMarker?(id: string, marker: Marker): void;

  /**
   * Event handler for invoked while the selection is moving
   *
   * @param channelId the unique channel id of the channel
   * @param selection the selection
   *
   */
  onMoveSelectionWindow?(channelId: string, selection: SelectionWindow): void;

  /**
   * Event handler for updating selections value
   *
   * @param channelId the unique channel id of the channel
   * @param selection the selection
   *
   */
  onUpdateSelectionWindow?(channelId: string, selection: SelectionWindow): void;

  /**
   * Event handler for click events within a selection
   *
   * @param channelId the unique channel id of the channel
   * @param selection the selection
   * @param timeSecs epoch seconds of where drag ended in respect to the data
   */
  onClickSelectionWindow?(channelId: string, selection: SelectionWindow, timeSecs: number): void;
}

/** Channel Events */
export interface ChannelEvents {
  /** Events of label */
  labelEvents?: LabelEvents;

  /** events on the channel content */
  events?: ChannelContentEvents;

  /**
   * Event handler for when a key is pressed
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param clientX x location of where the key was pressed
   * @param clientY y location of where the key was pressed
   * @param channelId a Channel Id as a string
   * @param timeSecs epoch seconds of where the key was pressed in respect to the data
   */
  onKeyPress?(
    e: React.KeyboardEvent<HTMLDivElement>,
    clientX: number,
    clientY: number,
    channelId: string,
    timeSecs: number
  ): void;
}

/** Station Events */
export interface StationEvents {
  /** Default channel events */
  defaultChannelEvents?: ChannelEvents;

  /** Non-default channel events */
  nonDefaultChannelEvents?: ChannelEvents;
}

/** Events */
export interface Events {
  /** station events */
  stationEvents?: StationEvents;

  /**
   * Event handler for updating markers value
   *
   * @param channelId the unique channel id of the channel
   * @param marker the marker
   */
  onUpdateMarker?(marker: Marker): void;

  /**
   * Event handler for updating selections value
   *
   * @param selection the selection
   */
  onUpdateSelectionWindow?(selection: SelectionWindow): void;

  /**
   * Event handler for invoked while the selection is moving
   *
   * @param channelId the unique channel id of the channel
   * @param selection the selection
   *
   */
  onMoveSelectionWindow?(selection: SelectionWindow): void;

  /**
   * Event handler for click events within a selection
   *
   * @param selection the selection
   * @param timeSecs epoch seconds of where drag ended in respect to the data
   */
  onClickSelectionWindow?(SelectionWindow, timeSecs: number): void;

  /**
   * Event handler for on zoom change - fired when zoom changes
   *
   * @param timeRange the updated zoom time range
   */
  onZoomChange?(timeRange: TimeRange): void;

  /**
   * Callback for when the measure window is resized. Note that this can
   * occur even if the measure window has no selection.
   *
   * @param heightPx the new height of the measure window in pixels
   */
  onMeasureWindowResize?(heightPx: number): void;

  /**
   * Callback when the user presses the "reset all amplitudes" hotkeys
   */
  onResetAmplitude?(): void;

  /**
   * Callback for when the {@link WaveformDisplay} component mounts
   */
  onMount?(weavessInstance: WeavessInstance): void;
}

/** Selections */
export interface Selections {
  /** String array of channels */
  channels?: string[];

  /** String array of signal detections */
  signalDetections?: string[];

  /** String array of predictive phases */
  predictedPhases?: string[];
}

/** Mouse Position */
export interface MousePosition {
  /** x value of mouse position */
  clientX: number;

  /** y value of mouse position */
  clientY: number;
}

/** Marker Configuration */
export interface Markers {
  /** Vertical markers */
  verticalMarkers?: Marker[];

  /** Moveable markers */
  moveableMarkers?: Marker[];

  /** Selection windows */
  selectionWindows?: SelectionWindow[];
}

/** Marker Configuration */
export interface Marker {
  /** The id of the marker */
  id: string;

  /** The style */
  color: string;

  /** Style of line */
  lineStyle: LineStyle;

  /** Epoch time in seconds */
  timeSecs: number;

  /** The min time (in seconds) constraint on the marker */
  minTimeSecsConstraint?: number;

  /** The max time (in seconds) constraint on the marker */
  maxTimeSecsConstraint?: number;
}

/** Selection Window Configuration */
export interface SelectionWindow {
  /** The id of the selection */
  id: string;

  /** Start marker for selection window */
  startMarker: Marker;

  /** End marker for selection window */
  endMarker: Marker;

  /** Indicates if the selection is moveable */
  isMoveable: boolean;

  /** Color */
  color: string;
}
