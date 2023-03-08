import type { Point } from '@gms/ui-util';

/* eslint-disable @typescript-eslint/no-magic-numbers */
export const EVERY_20_SECONDS_FOR_A_MINUTE = 3;
export const EVERY_20_SECONDS_FOR_AN_HOUR = EVERY_20_SECONDS_FOR_A_MINUTE * 60;
export const EVERY_20_SECONDS_FOR_A_DAY = EVERY_20_SECONDS_FOR_AN_HOUR * 24;
export const EVERY_20_SECONDS_FOR_A_WEEK = EVERY_20_SECONDS_FOR_A_DAY * 7;
export const EVERY_20_SECONDS_FOR_A_MONTH = EVERY_20_SECONDS_FOR_A_DAY * 30;
/* eslint-enable @typescript-eslint/no-magic-numbers */

export interface WeavessLineDefinition {
  id: string | number;
  color: string;
  values: Float32Array;
  average: number;
}

export interface BarValue extends Point {
  quietUntilMs?: number;
  quietDurationMs?: number;
}

export interface BarDefinition {
  id: string | number;
  color: string;
  value: BarValue;
}

/** defines a domain tuple */
export type DomainTuple = [number, number];

/** defines a domain */
export interface Domain {
  x: DomainTuple;
  y: DomainTuple;
}

/** defines the chart padding */
export interface ChartPadding {
  top: number;
  left: number;
  bottom: number;
  right: number;
}

/** The base chart props */
export interface BaseChartProps {
  id?: string | number;
  classNames?: string;
  widthPx: number;
  heightPx: number;
  minHeightPx?: number;
  padding?: ChartPadding;
  domain?: Domain;
}

/** The axis props */
export interface AxisProps {
  rotateAxis?: boolean;
  suppressYAxis?: boolean;
  suppressXAxis?: boolean;
  yAxisLabel?: string;
  xAxisLabel?: string;
  yTickCount?: number;
  xTickCount?: number;
  xTickValues?: string[] | number[];
  yTickValues?: string[] | number[];
  xTickTooltips?: string[];
  yTickTooltips?: string[];
  barDefs?: BarDefinition[];
  disabled?: Disabled;
  onContextMenuBar?(e: any, datum: any): void;
  onContextMenuBarLabel?(e: any, index: number): void;
  yTickFormat?(value: string | number): string | number;
  xTickFormat?(value: string | number): string | number;
}

interface Disabled {
  xTicks: {
    disabledColor: string;
    disabledCondition(tick: { value: number; tooltip: string }): boolean;
  };
}

/** The bar chart props */
export interface BarChartProps extends BaseChartProps, AxisProps {
  maxBarWidth: number;
  minBarWidth: number;
  categories: { x: string[]; y: string[] };
  barDefs: BarDefinition[];
  scrollBrushColor?: string;
  thresholdsMarginal?: number[];
  thresholdsBad?: number[];
  disabled?: Disabled;
  dataComponent?: React.Component;
}

/** The line chart WEAVESS props */
export interface LineChartWeavessProps {
  id?: string | number;
  classNames?: string;
  heightPx: number;
  widthPx: number;
  startTimeMs: number;
  endTimeMs: number;
  yAxisLabel?: string;
  xAxisLabel?: string;
  lineDefs?: WeavessLineDefinition[];
  minAndMax?: {
    readonly xMin: number;
    readonly xMax: number;
    readonly yMin: number;
    readonly yMax: number;
  };
}
