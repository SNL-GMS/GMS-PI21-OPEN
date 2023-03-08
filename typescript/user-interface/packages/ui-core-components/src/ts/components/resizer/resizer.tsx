/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import type { Point } from '@gms/ui-util';
import { classList } from '@gms/ui-util';
import * as React from 'react';

import { ResizeContext } from './resize-context';
import { BottomResizeHandle } from './resize-handle';
import type { ResizeManager, ResizerProps } from './types';

const DEFAULT_CLIENT_HEIGHT_PX = 360;

/**
 * Custom hook that encapsulates the logic and exposes functions to manage resizing.
 *
 * @param onResize callback that is called and passed the current size for each rendered size during resizing.
 * @param onResizeEnd callback that is called and passed the final size after resizing finishes.
 */
const useResizeManager = (
  resizeRef: React.MutableRefObject<HTMLDivElement>,
  onResize: (h: number) => void,
  onResizeEnd: (h: number) => void
): ResizeManager => {
  const context = React.useContext(ResizeContext);

  /**
   * isResizing is a flag used to indicate if the resizer is being resized.
   */
  const [isResizing, setResizing] = React.useState(false);

  /**
   * initialHeight is the height of the resizer when the user starts resizing it.
   * Combined with the amount the mouse has moved, this determines the user's
   * intended height.
   */
  const initialHeightRef = React.useRef(context.height);

  // Getters
  const getIsResizing = () => isResizing;

  /**
   * the actual height of the resizer in the DOM
   */
  const getCurrentHeight = React.useCallback(
    () =>
      Math.min(resizeRef.current?.clientHeight, context.containerHeight) ||
      resizeRef.current?.clientHeight ||
      context.height,
    [context.containerHeight, context.height, resizeRef]
  );

  /**
   * The height the resizer says we should currently use
   */
  const getHeight = () =>
    context.height && context.containerHeight
      ? Math.min(context.height, context.containerHeight) || context.height
      : DEFAULT_CLIENT_HEIGHT_PX;

  // Init
  React.useLayoutEffect(() => {
    const h = getCurrentHeight();
    context.setHeight(h);
    initialHeightRef.current = h;
  }, [context, getCurrentHeight]);

  // Setters
  const setIsResizing = (isIt: boolean) => {
    context.setIsResizing(isIt); // updates the context
    setResizing(isIt); // our locally managed isResizing state
  };

  // Event handlers
  /**
   * Handle mouse move while dragging
   */
  const handleMouseMove = (startPoint: Point) => (e2: MouseEvent) => {
    const mouseMoveFromStart = e2.pageY - (startPoint?.y ?? e2.pageY);
    const initialHeight = initialHeightRef.current;
    const newHeight = Math.min(initialHeight + mouseMoveFromStart, context.containerHeight);
    context.setHeight(newHeight);
    if (onResize) {
      onResize(newHeight);
    }
  };

  /**
   * Handle the end of resizing. Called by onMouseUp.
   */
  const handleResizeEnd = () => {
    setResizing(false);
    context.setIsResizing(false);
    context.setHeight(getCurrentHeight());
    onResizeEnd(context.height);
  };

  /**
   * handle the start of a resize event. Called by onMouseDown.
   */
  const handleResizeStart = () => {
    setIsResizing(true);
    initialHeightRef.current = getCurrentHeight();
  };

  return {
    getIsResizing,
    getHeight,
    handleMouseMove,
    handleResizeStart,
    handleResizeEnd
  };
};

// TODO: Create options for different orientations. Currently only supports BOTTOM, by default.
/**
 * A resizer component that creates a drag handle and sensor, and updates its size when that
 * handle is dragged. Synchs sizes with other resizer components within the same ResizeContainer.
 */
// eslint-disable-next-line react/function-component-definition
export const Resizer: React.FunctionComponent<React.PropsWithChildren<ResizerProps>> = props => {
  const context = React.useContext(ResizeContext);
  // !FIX ESLINT DO NOT USE REACT HOOK HOOKS IN CONDITIONAL
  // eslint-disable-next-line react-hooks/rules-of-hooks
  const resizeRef = props.forwardRef ? props.forwardRef : React.useRef<HTMLDivElement>(null);
  const resizeManager = useResizeManager(
    resizeRef,
    (h: number) => props.onResize(h),
    (h: number) => props.onResizeEnd(h)
  );
  return (
    <div
      className={classList(
        {
          resizer: true,
          'resizer--resizing': resizeManager.getIsResizing()
        },
        props.className ?? ''
      )}
      data-cy={props.dataCy}
      ref={resizeRef}
      style={{
        height: Math.max(props?.minHeightPx ?? 0, resizeManager.getHeight()),
        maxHeight: props.maxHeightPx ?? context.containerHeight ?? '100vh'
      }}
    >
      {props.children}
      <BottomResizeHandle
        handleMouseMove={e => resizeManager.handleMouseMove(e)}
        onResizeEnd={() => resizeManager.handleResizeEnd()}
        onResizeStart={() => resizeManager.handleResizeStart()}
      />
    </div>
  );
};
