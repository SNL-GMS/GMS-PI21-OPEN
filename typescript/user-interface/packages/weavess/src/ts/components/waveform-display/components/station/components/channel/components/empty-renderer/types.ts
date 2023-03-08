import type { WeavessTypes } from '@gms/weavess-core';

export interface EmptyRendererProps {
  /** waveform interval loaded and available to display */
  displayInterval: WeavessTypes.TimeRange;
}

// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface EmptyRendererState {}
