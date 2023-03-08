import type { ChannelContentEvents, Events, LabelEvents } from './types';

/** Constant for converting from decimal to percent. */
export const PERCENT_100 = 100;

/** Constant for the scrollbar width in pixels; */
export const SCROLLBAR_WIDTH_PIXELS = 10;

/** Constant for the default label width in pixels; */
export const MIN_LABEL_WIDTH_PIXELS = 100;

/** Constant for the default label width in pixels; */
export const DEFAULT_LABEL_WIDTH_PIXELS = 184;

/** Constant for the default channel height in pixels; */
export const DEFAULT_CHANNEL_HEIGHT_PIXELS = 50;

/** Constant for the xaxis height in pixels; */
export const DEFAULT_XAXIS_HEIGHT_PIXELS = 35;

/** Constant the number or milliseconds in a second */
export const MILLISECONDS_IN_SECOND = 1000;

/** The duration of one frame at 60FPS */
export const ONE_FRAME_MS = 16;

/** The default height of the horizontal divider top container */
export const DEFAULT_DIVIDER_TOP_HEIGHT_PX = 200;

/** The min height of the horizontal divider top container */
export const DEFAULT_DIVIDER_TOP_MIN_HEIGHT_PX = 100;

/** The max height of the horizontal divider top container */
export const DEFAULT_DIVIDER_TOP_MAX_HEIGHT_PX = 500;

/**
 * The number of pixels of padding to add at the top and bottom of each waveform.
 * This prevents them from touching the top and bottom of the channel, and also adjusts
 * the position of the y axis accordingly.
 */
export const WAVEFORM_PADDING_PX = 2;

/**
 * A constant that defines the weavess label events
 * to all be `undefined`.
 */
export const DEFAULT_UNDEFINED_LABEL_EVENTS: LabelEvents = {
  onChannelCollapsed: undefined,
  onChannelExpanded: undefined,
  onChannelLabelClick: undefined
};

/**
 * A constant that defines the weavess channel content events
 * to all be `undefined`.
 */
export const DEFAULT_UNDEFINED_CHANNEL_CONTENT_EVENTS: ChannelContentEvents = {
  onContextMenu: undefined,
  onChannelClick: undefined,
  onSignalDetectionContextMenu: undefined,
  onSignalDetectionClick: undefined,
  onSignalDetectionDragEnd: undefined,
  onPredictivePhaseContextMenu: undefined,
  onPredictivePhaseClick: undefined,
  onPredictivePhaseDragEnd: undefined,
  onMeasureWindowUpdated: undefined,
  onUpdateMarker: undefined,
  onMoveSelectionWindow: undefined,
  onUpdateSelectionWindow: undefined,
  onClickSelectionWindow: undefined
};

/**
 * A constant that defines the weavess events
 * to all be `undefined`.
 */
export const DEFAULT_UNDEFINED_EVENTS: Events = {
  stationEvents: {
    defaultChannelEvents: {
      labelEvents: DEFAULT_UNDEFINED_LABEL_EVENTS,
      events: DEFAULT_UNDEFINED_CHANNEL_CONTENT_EVENTS,
      onKeyPress: undefined
    },
    nonDefaultChannelEvents: {
      labelEvents: DEFAULT_UNDEFINED_LABEL_EVENTS,
      events: DEFAULT_UNDEFINED_CHANNEL_CONTENT_EVENTS,
      onKeyPress: undefined
    }
  },
  onUpdateMarker: undefined,
  onMoveSelectionWindow: undefined,
  onUpdateSelectionWindow: undefined,
  onClickSelectionWindow: undefined,
  onZoomChange: undefined
};
