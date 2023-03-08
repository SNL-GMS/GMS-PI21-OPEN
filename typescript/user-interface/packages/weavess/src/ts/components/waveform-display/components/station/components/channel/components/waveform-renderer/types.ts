import type { WeavessTypes } from '@gms/weavess-core';

import type { AnimationFrameOptions } from '../../../../../../types';

export interface WaveformRendererProps {
  /** waveform interval loaded and available to display */
  displayInterval: WeavessTypes.TimeRange;

  /** Id of channel segment */
  channelSegmentId: string;

  /** Collection of channel segments */
  channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]>;

  /** the min boundary (x value) in gl units */
  glMin: number;

  /** the max boundary (x value) in gl units */
  glMax: number;

  /* Is this part of the Measure Window */
  isMeasureWindow: boolean;

  /** waveform display configuration */
  initialConfiguration?: Partial<WeavessTypes.Configuration>;

  /** Collection of masks */
  masks?: WeavessTypes.Mask[];

  /** Web Workers */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  workerRpcs: any[];

  /** helps identify the channel associated to this renderer */
  channelName: string;

  /** default min/max range scale for the y-axis */
  defaultRange?: WeavessTypes.Range;

  /** Amplitude scale factor to apply to measure window's waveform renderer */
  msrWindowWaveformAmplitudeScaleFactor?: number;
  /** channel offset for scaling during alignment */
  channelOffset?: number;

  // Callbacks
  /**
   * Sets the Y axis bounds
   *
   * @param min minimum bound as a number
   * @param max Maximum bound as a number
   */
  setYAxisBounds(min: number | undefined, max: number | undefined);

  /** Issues a re render */
  renderWaveforms(options?: AnimationFrameOptions): void;

  /**
   * Used to look up the position buffer data (a Float32Array formatted like so: x y x y x y ...).
   * Takes the position buffer's id.
   */
  getPositionBuffer?(
    id: string,
    startTime: number,
    endTime: number,
    domainTimeRange: WeavessTypes.TimeRange
  ): Promise<Float32Array>;

  /**
   * Used to look up the Channel Segment Boundaries for a given channel segment by name
   */
  getBoundaries?(
    channelName: string,
    channelSegment: WeavessTypes.ChannelSegment,
    timeRange?: WeavessTypes.TimeRange
  ): Promise<WeavessTypes.ChannelSegmentBoundaries>;
}

// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface WaveformRendererState {}

export interface Float32ArrayData {
  /** is this channel segment selected */
  isSelected: boolean;

  /** Color */
  color?: string;

  /** Display type */
  displayType?: WeavessTypes.DisplayType[];

  /** Point size */
  pointSize?: number;

  /** Waveform data */
  float32Array: Float32Array;
}
