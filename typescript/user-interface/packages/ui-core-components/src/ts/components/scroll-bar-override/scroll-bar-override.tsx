/* eslint-disable jsx-a11y/click-events-have-key-events */
/* eslint-disable complexity */
/* eslint-disable jsx-a11y/no-static-element-interactions */
import {
  classList,
  useElementSize,
  useFollowMouse,
  useForceUpdate,
  usePrevious
} from '@gms/ui-util';
import * as d3 from 'd3';
import * as React from 'react';

import type { ScrollBarOverrideProps } from './types';

/**
 * Creates a function to handle the gutter click, which will move the scroll bar and scroll the parent.
 * This has side effects, manipulating the parent's scroll position.
 */
export const useHandleGutterClick = (
  scrollBarRef: React.MutableRefObject<HTMLElement>,
  scrollBarGutterRef: React.MutableRefObject<HTMLElement>,
  boundX: (value: number) => number,
  targetElement: Element,
  scaleToGutterDimensions: (
    value:
      | number
      | {
          valueOf(): number;
        }
  ) => number
): React.MouseEventHandler<HTMLDivElement> => {
  return React.useCallback(
    event => {
      const scrollBarRect = scrollBarRef.current.getBoundingClientRect();
      const scrollBarGutterRect = scrollBarGutterRef?.current?.getBoundingClientRect();
      const guardedClickX = boundX(
        // eslint-disable-next-line no-unsafe-optional-chaining
        event.clientX - scrollBarGutterRef?.current?.getBoundingClientRect()?.x ?? 0
      );
      const middleOfScrollBarX = guardedClickX - scrollBarRect.width / 2;
      let scrollToX = middleOfScrollBarX;
      if (guardedClickX + Math.round(scrollBarRect.width) >= scrollBarGutterRect.width) {
        scrollToX = guardedClickX;
      }
      if (guardedClickX - scrollBarRect.width <= 0) {
        scrollToX = 0;
      }
      scrollBarRef.current.style.transform = `translate(
      ${scrollToX}px, 0px)`;
      targetElement?.parentElement?.scrollTo(scaleToGutterDimensions(scrollToX), 0);
    },
    [
      boundX,
      scaleToGutterDimensions,
      scrollBarGutterRef,
      scrollBarRef,
      targetElement?.parentElement
    ]
  );
};

/**
 * Handles changes sent down from the parent component, causing the scroll bar to stay in sync with
 * changes from the parent.
 * This has side effects, manipulating the parent's scroll position.
 */
const useHandleUpdateFromParent = (
  isFollowing: boolean,
  scrollBar: { ref: React.MutableRefObject<HTMLElement>; width: number },
  scrollBarGutter: {
    ref: React.MutableRefObject<HTMLElement>;
    width: number;
  },
  scrollLeft: number,
  elementScrollWidth: number,
  targetElement: Element,
  overrideScrollBarWidth: number
) => {
  const previousScrollLeft = usePrevious(
    scrollBar.ref.current && scrollBarGutter.width ? scrollLeft : undefined,
    undefined
  );

  const previousScrollBarWidth = usePrevious(
    scrollBar.ref.current && scrollBarGutter.width ? scrollBar.width : undefined,
    undefined
  );

  // Used to map the scroll region of the provided element to the scroll bar override location
  // needed when the provided element scroll is manipulated at a higher level and the scroll bar override
  // needs to know how to adjust accordingly
  const inverseScale = d3
    .scaleLinear()
    .domain([0, elementScrollWidth])
    .range([0, scrollBarGutter.ref?.current?.getBoundingClientRect()?.width ?? 0])
    .clamp(true);

  const moveScrollBar = (scrollLeftValue: number) => {
    if (inverseScale(scrollLeftValue) > scrollBarGutter.width - overrideScrollBarWidth) {
      scrollBar.ref.current.style.transform = `translate(
        ${scrollBarGutter.width - overrideScrollBarWidth}px, 0px)`;
    } else if (inverseScale(scrollLeftValue) < 0) {
      scrollBar.ref.current.style.transform = `translate(
        ${0}px, 0px)`;
    } else {
      scrollBar.ref.current.style.transform = `translate(
      ${Math.round(inverseScale(scrollLeftValue))}px, 0px)`;
    }
  };

  /**
   * Updates the scroll bar position if the original scroll bar is manipulated at a higher level
   */
  const update = () => {
    if (
      previousScrollLeft !== scrollLeft &&
      !isFollowing &&
      scrollBar.ref.current &&
      scrollBar.ref.current.style
    ) {
      moveScrollBar(scrollLeft);
      targetElement?.parentElement.scrollTo(scrollLeft, 0);
    } else if (previousScrollBarWidth !== scrollBar.width) {
      // Updates and re calculates scroll bar on resizes
      moveScrollBar(previousScrollLeft);
    }
  };

  if (!isFollowing) {
    update();
  }
};

/**
 * @returns the x position of the scroll bar
 */
const useScrollBarX = (
  isFollowing: boolean,
  scrollBarRef: React.MutableRefObject<HTMLElement>,
  scrollBarGutterRef: React.MutableRefObject<HTMLElement>,
  initialMouseX: number,
  mouseX: number
) => {
  const scrollBarMouseOffsetLeft = React.useRef<number | undefined>(undefined);
  React.useLayoutEffect(() => {
    if (isFollowing) {
      scrollBarMouseOffsetLeft.current =
        initialMouseX - (scrollBarRef?.current?.getBoundingClientRect()?.left ?? 0);
    } else {
      scrollBarMouseOffsetLeft.current = undefined;
    }
  }, [initialMouseX, scrollBarRef, isFollowing]);

  return (
    // eslint-disable-next-line no-unsafe-optional-chaining
    (mouseX - scrollBarGutterRef?.current?.getBoundingClientRect()?.x ?? 0) -
    scrollBarMouseOffsetLeft.current
  );
};

/**
 * @returns the width of the actual override scroll bar div
 */
const getOverrideScrollBarWidth = (targetElement: Element, scrollBarGutterWidth: number) => {
  const innerContainerWidth = targetElement?.getBoundingClientRect()?.width;

  if (!innerContainerWidth || innerContainerWidth === 0 || scrollBarGutterWidth === 0) {
    return 0;
  }
  let overrideScrollBarWidth = Math.max(
    scrollBarGutterWidth / (innerContainerWidth / scrollBarGutterWidth),
    5
  );

  // Hides the scroll bar if width is big enough to show all the data
  if (innerContainerWidth <= scrollBarGutterWidth) {
    overrideScrollBarWidth = 0;
  }
  return overrideScrollBarWidth;
};

/**
 * A scroll bar override component that allows for a elements scroll bar to be hidden, but provides
 * A custom scroll bar instead. Useful when wanting to have a scroll bar stick for small heights and the
 * original element cannot support that.
 *
 * @param props for the scroll bar override
 * @returns a scrollBar able to control the element provided and hides that elements scroll bar
 */
// eslint-disable-next-line react/function-component-definition
export const ScrollBarOverride: React.FunctionComponent<ScrollBarOverrideProps> = (
  props: ScrollBarOverrideProps
) => {
  const forceUpdate = useForceUpdate();
  const { onMouseDown, mouseX, initialMouseX, isFollowing } = useFollowMouse(forceUpdate);
  const [scrollBarGutterRef, , scrollBarGutterWidth] = useElementSize();
  const [scrollBarRef, , scrollBarWidth] = useElementSize();
  const { scrollLeft, targetElement, orientation, className } = props;

  const scrollBarX = useScrollBarX(
    isFollowing,
    scrollBarRef,
    scrollBarGutterRef,
    initialMouseX,
    mouseX
  );

  const elementScrollWidth = targetElement?.parentElement?.scrollWidth ?? 0;

  // Used to map the scroll bar override location to the scroll region of the provided element
  const scaleToGutterDimensions = d3
    .scaleLinear()
    .domain([0, scrollBarGutterRef?.current?.getBoundingClientRect()?.width ?? 0])
    .range([0, elementScrollWidth])
    .clamp(true);

  /**
   * Binds value to the correct region making sure scroll bar doesn't go out of bounds
   *
   * @param value to bound
   * @returns boundValueX
   */
  const boundX = (value: number): number => {
    let boundValueX = value;
    if (value > scrollBarGutterWidth - scrollBarWidth) {
      boundValueX = scrollBarGutterWidth - scrollBarWidth;
    } else if (value < 0) {
      boundValueX = 0;
    }
    return boundValueX;
  };

  const boundMouseX = boundX(scrollBarX);

  /**
   * For internal actions, update the parent's scroll position based on our internal actions
   */
  const keepParentScrollInSync = () => {
    if (!Number.isNaN(scrollBarX) && isFollowing) {
      targetElement?.parentElement?.scrollTo(scaleToGutterDimensions(scrollBarX), 0);
    }
  };
  keepParentScrollInSync();

  const overrideScrollBarWidth = getOverrideScrollBarWidth(targetElement, scrollBarGutterWidth);

  useHandleUpdateFromParent(
    isFollowing,
    { ref: scrollBarRef, width: scrollBarWidth },
    { ref: scrollBarGutterRef, width: scrollBarGutterWidth },
    scrollLeft,
    elementScrollWidth,
    targetElement,
    overrideScrollBarWidth
  );

  const handleGutterClick = useHandleGutterClick(
    scrollBarRef,
    scrollBarGutterRef,
    boundX,
    targetElement,
    scaleToGutterDimensions
  );

  return (
    <div
      data-cy={`${targetElement?.parentElement?.className ?? 'unknown'}-gutter-scrollbar-override`}
      ref={ref => {
        scrollBarGutterRef.current = ref;
      }}
      onClick={handleGutterClick}
      className={classList(
        {
          'scroll-bar': true,
          'scroll-box--x': orientation === 'x',
          'scroll-box--y': orientation === 'y'
        },
        className
      )}
    >
      <div
        data-cy={`${targetElement?.parentElement?.className ?? 'unknown'}-scrollbar-override`}
        ref={ref => {
          scrollBarRef.current = ref;
        }}
        style={{
          width: Number.isNaN(overrideScrollBarWidth) ? 0 : overrideScrollBarWidth,
          transform: `translate(${boundMouseX}px, 0px)`
        }}
        onMouseDown={onMouseDown}
        onClick={event => event.stopPropagation()}
        className={classList(
          {
            'scroll-bar__bar': true,
            'scroll-box--x': orientation === 'x',
            'scroll-box--y': orientation === 'y'
          },
          className
        )}
      />
    </div>
  );
};
