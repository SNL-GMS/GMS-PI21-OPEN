import type { WeavessTypes } from '@gms/weavess-core';

export interface VerticalMarkerProps {
  /** Color as a string */
  color: string;

  /** Line style as a LineStyle */
  lineStyle: WeavessTypes.LineStyle;

  /** Percentage Location 0-100 as a number */
  percentageLocation: number;

  /** react key but called name since key was causing fortify issues */
  name?: string;
}

// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface VerticalMarkerState {}
