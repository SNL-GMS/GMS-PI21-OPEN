import { useResizeObserver } from '@gms/ui-util';
import React from 'react';

/**
 * The type of the props for the {@link ViewportMaxSizer} component
 */
export interface ViewportMaxSizerProps {
  setMaxViewportSizePx(maxPx: number): void;
}

// eslint-disable-next-line react/function-component-definition
const InternalViewportMaxSizer: React.FC<ViewportMaxSizerProps> = ({
  setMaxViewportSizePx
}: ViewportMaxSizerProps) => {
  const sizerRef = React.useRef(null);
  const updateSize = React.useCallback(() => {
    if (!sizerRef.current) return;
    setMaxViewportSizePx(sizerRef.current.clientWidth);
  }, [setMaxViewportSizePx]);
  useResizeObserver(sizerRef, updateSize);
  return (
    <aside
      ref={ref => {
        if (ref) {
          sizerRef.current = ref;
          updateSize();
        }
      }}
      style={{
        width: `${Number.MAX_SAFE_INTEGER}px`,
        height: '1px',
        position: 'absolute',
        pointerEvents: 'none',
        background: 'none'
      }}
    />
  );
};

/**
 * Creates a non-interactive, invisible div that is as wide as possible in order to determine the maximum width
 * of a div that the browser will allow. When it determines this value, calls the setMaxViewportSizePx function provided
 * in props.
 * Note, this is memoized to prevent it from calling the setter repeatedly. It performs best if the provided setter
 * function is referentially stable.
 */
export const ViewportMaxSizer = React.memo(InternalViewportMaxSizer);
