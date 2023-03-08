import { WeavessConstants } from '@gms/weavess-core';
import * as React from 'react';
import ResizeObserver from 'resize-observer-polyfill';

import type { HorizontalDividerProps } from './types';

/**
 * A hook that attaches a resize observer and returns the height of the referenced element
 * Note that it tracks state internally, and so will issue a rerender to children when
 * the component resizes and the ResizeObserver event fires.
 *
 * @param ref A ref to the element we want the size of
 * @returns the height and width of the element, or undefined
 */
const useElementSize = (
  ref: React.MutableRefObject<HTMLDivElement | null>
): { heightPx: number | undefined; widthPx: number | undefined } => {
  const [heightPx, setHeight] = React.useState(ref.current?.clientHeight);
  const [widthPx, setWidth] = React.useState(ref.current?.clientWidth);
  React.useEffect(() => {
    const resizeObserver = new ResizeObserver(() => {
      if (ref.current) {
        setHeight(ref.current.clientHeight);
        setWidth(ref.current.clientWidth);
      }
    });
    if (ref.current) {
      resizeObserver.observe(ref.current);
    }
    return () => {
      resizeObserver.disconnect();
    };
  }, [ref]);
  return { heightPx, widthPx };
};

/**
 * A hook that creates a drag listener. Note that the drag listener is wrapped in useCallback
 * to ensure that it is referentially stable for components that are passed it as a prop.
 */
const useDividerDragListener = (
  minTopHeight: number | undefined,
  maxTopHeight: number | undefined,
  topHeightPx: number,
  bottomHeightPx: number,
  setTopHeightPx: React.Dispatch<React.SetStateAction<number>>,
  setIsResizing: (val: boolean) => void,
  onResizeEnd: ((topHeightPx: number, bottomHeightPx: number) => void) | undefined
) =>
  React.useCallback(
    (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
      const startPosition = event.clientY;
      const minHeightPx = minTopHeight ?? WeavessConstants.DEFAULT_DIVIDER_TOP_MIN_HEIGHT_PX;
      const maxHeightPx = maxTopHeight ?? WeavessConstants.DEFAULT_DIVIDER_TOP_MAX_HEIGHT_PX;
      setIsResizing(true);

      const onDividerMouseMove = (e2: MouseEvent) => {
        const currentPos = e2.clientY;
        const diff = currentPos - startPosition;
        const heightPx = topHeightPx + diff;
        setTopHeightPx(Math.min(Math.max(heightPx, minHeightPx), maxHeightPx));
      };

      // eslint-disable-next-line complexity
      const onDividerMouseUp = (): void => {
        document.body.removeEventListener('mousemove', onDividerMouseMove);
        document.body.removeEventListener('mouseup', onDividerMouseUp);
        setIsResizing(false);
        if (onResizeEnd) onResizeEnd(topHeightPx, bottomHeightPx);
      };

      document.body.addEventListener('mousemove', onDividerMouseMove);
      document.body.addEventListener('mouseup', onDividerMouseUp);
    },
    [
      bottomHeightPx,
      maxTopHeight,
      minTopHeight,
      onResizeEnd,
      setIsResizing,
      setTopHeightPx,
      topHeightPx
    ]
  );

/**
 * Renders a top and bottom container with a resizable horizontal divider between them.
 * Will fill the space available to it.
 * Top and bottom containers can be ReactElements or functions that return a React Element.
 */
// eslint-disable-next-line react/function-component-definition
export const HorizontalDivider: React.FC<HorizontalDividerProps> = ({
  topComponent,
  bottomComponent,
  topClassName,
  bottomClassName,
  showTop,
  showBottom,
  minTopHeight,
  maxTopHeight,
  onResizeEnd
}: HorizontalDividerProps) => {
  const topContainerRef = React.useRef<HTMLDivElement | null>(null);
  const bottomContainerRef = React.useRef<HTMLDivElement | null>(null);
  const bottomHeightPx = useElementSize(bottomContainerRef).heightPx ?? -1;
  const [topHeightPx, setTopHeightPx] = React.useState(
    WeavessConstants.DEFAULT_DIVIDER_TOP_HEIGHT_PX
  );
  const [isResizing, setIsResizing] = React.useState(false);

  const onDividerDrag = useDividerDragListener(
    minTopHeight,
    maxTopHeight,
    topHeightPx,
    bottomHeightPx,
    setTopHeightPx,
    setIsResizing,
    onResizeEnd
  );
  const shouldShowTop = showTop === undefined || showTop;
  const shouldShowBottom = showBottom === undefined || showBottom;
  return (
    <>
      {shouldShowTop && (
        <div
          ref={ref => {
            topContainerRef.current = ref;
          }}
          className={`horizontal-divider__top ${topClassName}`}
          data-cy={`${topClassName}`}
          style={{
            height: `${topHeightPx}px`
          }}
        >
          {typeof topComponent === 'function'
            ? topComponent(topHeightPx, bottomHeightPx, isResizing)
            : topComponent}
        </div>
      )}
      {shouldShowTop && shouldShowBottom && (
        <div className="horizontal-divider__handle">
          {/* eslint-disable-next-line jsx-a11y/no-static-element-interactions */}
          <div
            className="horizontal-divider__target"
            data-cy="waveform-divider"
            onMouseDown={onDividerDrag}
          />
        </div>
      )}
      {shouldShowBottom && (
        <div
          ref={ref => {
            bottomContainerRef.current = ref;
          }}
          className={`horizontal-divider__bottom ${bottomClassName}`}
          data-cy={`${bottomClassName}`}
          style={{
            height: showTop ? `calc(100% - ${topHeightPx}px)` : '100%'
          }}
        >
          {typeof bottomComponent === 'function'
            ? bottomComponent(topHeightPx, bottomHeightPx, isResizing)
            : bottomComponent}
        </div>
      )}
    </>
  );
};
