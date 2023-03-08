import type { Point } from '@gms/ui-util';

export interface ResizerProps {
  className?: string;
  dataCy?: string;
  maxHeightPx?: number;
  minHeightPx?: number;
  forwardRef?: React.MutableRefObject<HTMLDivElement>;
  onResize?(newHeight?: number, newWidth?: number): void;
  onResizeEnd?(newHeight?: number, newWidth?: number): void;
}

export interface ResizeContainerProps {
  className?: string;
  dataCy?: string;
}

// TODO: Add top and left if/when needed
/**
 * The resizer orientations.
 */
export enum ResizeHandleOrientation {
  BOTTOM = 'BOTTOM',
  RIGHT = 'RIGHT',
  BOTTOM_RIGHT = 'BOTTOM_RIGHT'
}

export interface ResizeHandleProps {
  handleMouseMove(startPoint: Point): (e: MouseEvent) => void;
  onResizeStart?(): void;
  onResizeEnd?(): void;
}

export interface ResizeManager {
  getHeight(): number;
  getIsResizing(): boolean;
  handleMouseMove(startPoint: Point): (e2: MouseEvent) => void;
  handleResizeStart(): void;
  handleResizeEnd(): void;
}
