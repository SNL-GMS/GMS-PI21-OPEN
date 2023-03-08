export type HorizontalDividerComponentRenderer =
  | React.ReactElement
  | ((topHeightPx: number, bottomHeightPx: number, isResizing: boolean) => JSX.Element);

export interface HorizontalDividerProps {
  topComponent: HorizontalDividerComponentRenderer;
  bottomComponent: HorizontalDividerComponentRenderer;
  /** Append this to the end of the existing class names for the top component */
  topClassName?: string;
  /** Append this to the end of the existing class names for the bottom component */
  bottomClassName?: string;
  /** Whether to display the top element. Divider will only appear if both are visible */
  showTop?: boolean;
  /** Whether to display the bottom element. Divider will only appear if both are visible */
  showBottom?: boolean;
  minTopHeight?: number;
  maxTopHeight?: number;
  onResizeEnd?(topHeightPx: number, bottomHeightPx: number): void;
}
