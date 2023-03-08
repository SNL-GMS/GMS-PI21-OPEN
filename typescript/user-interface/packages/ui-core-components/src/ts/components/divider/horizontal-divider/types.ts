/**
 * HorizontalDivider State
 */
export interface HorizontalDividerState {
  topComponentHeightPx: number;
}

export interface HorizontalDividerSizeRange {
  minimumTopHeightPx?: number;
  minimumBottomHeightPx?: number;
}

/**
 * HorizontalDivider Props
 */
export interface HorizontalDividerProps {
  topHeightPx?: number;
  sizeRange?: HorizontalDividerSizeRange;
  minimumBottomHeightPx?: number;
  top: JSX.Element;
  bottom: JSX.Element;
  // Callback that is called each time the divider size changes
  // Passes the current height as a parameter
  onResize?(heightPx: number): void;
  // called when the divider height change action completes
  // Passes the new height as a parameter
  onResizeEnd?(heightPx: number): void;
}

export interface DragHandleDividerProps {
  handleHeight: number;
  onDrag(e: React.MouseEvent<HTMLDivElement>): void;
}
