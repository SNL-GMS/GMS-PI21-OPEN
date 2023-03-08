export interface YAxisProps {
  /** The height in percentage */
  heightInPercentage: number;

  /** Max amplitude as a number */
  maxAmplitude: number | undefined;

  /** Min amplitude as a number */
  minAmplitude: number | undefined;

  /** the y axis ticks */
  yAxisTicks?: number[];
}

// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface YAxisState {}
