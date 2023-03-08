export interface PieChartProps {
  style: PieChartStyle;
  percent: number;
  className?: string;
  pieSliceClass?: string;
  status?: string;
}

export interface PieChartStyle {
  diameterPx: number;
  borderPx?: number;
}
