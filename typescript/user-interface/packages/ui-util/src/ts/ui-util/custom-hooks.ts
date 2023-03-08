import { Logger, LogLevel, sleep } from '@gms/common-util';
import * as Immutable from 'immutable';
import debounce from 'lodash/debounce';
import * as React from 'react';
import ResizeObserver from 'resize-observer-polyfill';

import { isDomElement } from './dom-util';

/**
 * React hook to call an action on a repeating schedule.
 * Cleans up after itself in useEffect's cleanup callback.
 *
 * @param action the function to call
 * @param periodMs the period of time to run.
 */
export const useActionEveryInterval = (action: () => void, periodMs: number): void => {
  const [lastTimeRun, setLastTimeRun] = React.useState(Date.now());
  const runAction = () => {
    const debouncedAction = debounce(() => {
      setLastTimeRun(Date.now());
      action();
    });
    const timeoutHandle = setTimeout(debouncedAction, periodMs);
    return () => {
      debouncedAction.cancel();
      clearTimeout(timeoutHandle);
    };
  };
  // !FIX ESLINT Validate and check REACT HOOK dependencies
  // eslint-disable-next-line react-hooks/exhaustive-deps
  React.useEffect(runAction, [lastTimeRun]);
};

export const useResizeObserver = (
  ref: React.MutableRefObject<HTMLElement>,
  callback: (ref: HTMLElement) => void
): void => {
  React.useEffect(() => {
    const resizeObserver = new ResizeObserver(() => {
      callback(ref.current);
    });
    resizeObserver.observe(ref.current);
    return () => {
      resizeObserver.disconnect();
    };
  }, [callback, ref]);
};

/**
 * @returns the ref which should target the element of interest,
 * and the height and width of that element:
 * [ref, height, width]
 */
export const useElementSize = (): [React.MutableRefObject<HTMLElement>, number, number] => {
  const ref = React.useRef<HTMLDivElement>(null);
  const [height, setHeight] = React.useState(ref.current?.clientHeight);
  const [width, setWidth] = React.useState(ref.current?.clientWidth);
  React.useEffect(() => {
    const resizeObserver = new ResizeObserver(() => {
      setHeight(ref.current?.clientHeight);
      setWidth(ref.current?.clientWidth);
    });
    resizeObserver.observe(ref.current);
    return () => {
      resizeObserver.disconnect();
    };
  }, [ref]);
  return [ref, height, width];
};

/**
 * A forceUpdate function for use within a function component.
 * Tracks an ever changing value to ensure that comparisons
 * of the component's state are always going to be unique.
 * Usage:
 * const demo: React.FunctionComponent<{}> = () => {
 *   const forceUpdate = useForceUpdate();
 *   return <button onClick={forceUpdate()} />;
 * }
 */
export const useForceUpdate = (): (() => void) => {
  const [, setValue] = React.useState({});
  return () => {
    setValue({}); // new empty object is different every time
  };
};

/**
 * A hook used to focus on the provided element when it mounts.
 *
 * @param ref A ref to the DOM element on which to focus
 * @param beforeFocus an optional function to call just before focusing.
 */
export const useFocusOnMount = (
  ref: React.MutableRefObject<HTMLElement>,
  beforeFocus?: () => void
): void => {
  React.useEffect(() => {
    if (beforeFocus) {
      beforeFocus();
    }
    ref.current.focus();
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ref]);
};

/**
 * Will scroll the element contained in the ref into view when the
 * provided condition is true.
 *
 * @param condition the condition to watch
 * @param options
 */
export const useScrollIntoView = <T extends Element>(
  condition: (ref: T) => boolean,
  options?: ScrollIntoViewOptions | boolean
): React.MutableRefObject<T> => {
  const ref = React.useRef<T>(null);
  React.useEffect(() => {
    if (condition(ref.current)) {
      if (ref.current && ref.current.scrollIntoView) {
        ref.current.scrollIntoView(options);
      }
    }
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [condition(ref.current)]);
  return ref;
};

/**
 * Used to remember what element was focused when the condition is set to true,
 * and to restore focus to that element when that condition flips to false.
 *
 * @param condition when the condition flips to true, it will store the focus. When the condition
 * changes to false, it will restore the focus.
 * @returns storeFocus and restoreFocus functions to manually force these actions, if
 * controlling them via the boolean condition flag is not desired
 */
export const useRestoreFocus = (
  condition?: boolean
): { storeFocus(): void; restoreFocus(): void } => {
  const [storedElement, setStoredElement] = React.useState(null);
  const storeFocus = () => {
    setStoredElement(document.activeElement);
  };
  const restoreFocus = () => {
    if (storedElement) {
      storedElement.focus();
    }
    setStoredElement(null);
  };
  React.useLayoutEffect(() => {
    if (condition) {
      storeFocus();
    } else {
      restoreFocus();
    }
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [condition]);
  return { storeFocus, restoreFocus };
};

/**
 * The visual states a highlighted element can be in
 */
export enum HighlightVisualState {
  HIDDEN = 'HIDDEN',
  REVEALED = 'REVEALED',
  HIGHLIGHTED = 'HIGHLIGHTED'
}

export interface HighlightManager {
  getVisualState(): HighlightVisualState;
  onMouseOver(): void;
  onMouseUp(): void;
  onMouseOut(): void;
  onMouseDown(): void;
}

export const useHighlightManager = (): HighlightManager => {
  const [visualState, setVisualState] = React.useState(HighlightVisualState.HIDDEN);

  const onMouseOver = () =>
    visualState !== HighlightVisualState.HIGHLIGHTED &&
    setVisualState(HighlightVisualState.REVEALED);

  const onMouseUp = () => setVisualState(HighlightVisualState.HIDDEN);

  const onMouseOut = () =>
    visualState !== HighlightVisualState.HIGHLIGHTED && setVisualState(HighlightVisualState.HIDDEN);

  const onMouseDown = () => {
    setVisualState(HighlightVisualState.HIGHLIGHTED);
  };

  return {
    getVisualState: () => visualState,
    onMouseOver,
    onMouseUp,
    onMouseOut,
    onMouseDown
  };
};

/**
 * Creates, manages and exposes an interval, consisting of a start and end time,
 * and a setter function.
 *
 * @param initialStartTimeMs The starting time (farther in the past)
 * @param initialEndTimeMs The ending time (farther in the future)
 * @returns an array of three objects:
 * * startTimeMS (earlier)
 * * endTimeMS (later)
 * * setInterval
 * Example of use:
 * const [startTimeMs, endTimeMs, setInterval] = useInterval(Date.now()-1000, Date.now())
 */
export const useInterval = (
  initialStartTimeMs: number,
  initialEndTimeMs: number
): [number, number, (startTimeMs: number, endTimeMs: number) => void] => {
  const [interval, setInternalInterval] = React.useState({
    startTimeMs: initialStartTimeMs,
    endTimeMs: initialEndTimeMs
  });

  const setInterval = (s: number, e: number) => {
    setInternalInterval({ startTimeMs: s, endTimeMs: e });
  };

  return [interval.startTimeMs, interval.endTimeMs, setInterval];
};

/**
 * Adds mouseup listeners to any and all elements that match the css query selector string passed in.
 * Removes the events on removal of the component.
 *
 * @param querySelector the CSS query string to select
 * @param callback the callback function to call for mouseup events on any and all matching elements
 */
export const useMouseUpListenerBySelector = (
  querySelector: string,
  callback: EventListener
): (() => void) => {
  let elements: NodeListOf<Element>;
  React.useEffect(() => {
    // eslint-disable-next-line react-hooks/exhaustive-deps
    elements = document.querySelectorAll(querySelector);
    elements.forEach(element => {
      if (isDomElement(element)) {
        element.addEventListener('mouseup', callback);
      }
    });
  }, []);
  return () => {
    elements.forEach(element => element.removeEventListener('mouseup', callback));
  };
};

/**
 * Creates, manages, and exposes an immutable map state.
 *
 * @param keys string array map keys
 * @param value the default value for each map entry
 */
export const useImmutableMap = <T>(
  keys: string[],
  value: T
): [Immutable.Map<string, T>, React.Dispatch<React.SetStateAction<Immutable.Map<string, T>>>] => {
  let initialState = Immutable.Map<string, T>();
  keys.forEach(name => {
    initialState = initialState.set(name, value);
  });
  return React.useState<Immutable.Map<string, T>>(initialState);
};

/**
 * Returns the previous value that was passed in, and stores the current
 * value for future reference. On the next run, returns the previous value,
 * and then stores the passed in value for future reference. Etc...
 * On the first run, it returns initialValue
 *
 * @param value a value to assign for future retrieval.
 * @param initialValue a starting value
 * @returns the previous value, or the initial value on the first run
 */
export function usePrevious<T = unknown>(value: T, initialValue: T): T {
  const ref = React.useRef(initialValue);
  React.useEffect(() => {
    ref.current = value;
  }, [value]);
  return ref.current;
}

/**
 * a higher order function that builds a reducer for determining if dependencies have changed
 *
 * @returns a reducer function set up with the dependencies provided
 */
const buildDepReducer = (previousDeps, dependencyNames) => (
  accum: { [key: string]: unknown },
  dependency,
  index
) => {
  if (dependency !== previousDeps[index]) {
    const keyName = dependencyNames[index] || index;
    return {
      ...accum,
      [keyName]: {
        before: previousDeps[index],
        after: dependency
      }
    };
  }
  return accum;
};

/**
 * Logs out which dependencies from @param dependencies have changed, using a shallow `===` comparison.
 *
 * @param dependencies a list of dependencies to check for changes.
 * @param dependencyNames optional list of the names of the dependencies. If not provided, will simply log indices.
 */
export const useDependencyDebugger = (
  dependencies: unknown[],
  dependencyNames: string[] = []
): void => {
  const previousDeps = usePrevious(dependencies, []);
  const changedDeps: any = dependencies.reduce(buildDepReducer(previousDeps, dependencyNames), {});

  if (Object.keys(changedDeps).length) {
    // This is solely for debugging purposes, so simply use a console statement so that developers do not
    // have to fiddle with environment vars to turn on a logger.
    // eslint-disable-next-line no-console
    console.debug(changedDeps);
  }
};

/**
 * Prints out which of the dependencies have changed, helping to debug useEffect code
 *
 * @param effectHook a function that should be executed when useEffect is called
 * @param dependencies an array of dependencies, checked for referential equality
 * @param dependencyNames an optional list of names corresponding to the dependencies with the same indices
 */
export const useEffectDebugger = (
  effectHook: () => void,
  dependencies: unknown[],
  dependencyNames: string[] = []
): void => {
  const logger = Logger.create('GMS_LOG_USE_EFFECT_DEBUGGER', LogLevel.DEBUG);
  const previousDeps = usePrevious(dependencies, []);
  const { current: effectHookCallback } = React.useRef(effectHook);
  const changedDeps: any = dependencies.reduce(buildDepReducer(previousDeps, dependencyNames), {});

  if (Object.keys(changedDeps).length) {
    logger.debug(changedDeps);
  }

  React.useEffect(effectHookCallback, [effectHookCallback, ...dependencies]);
};

/**
 * Hook that uses event listeners added to the body when mouseDown listener is added to an element
 * To provide mouse info for initial click and mouse up release
 *
 * @param onFollowingEnd optional callback which will be called after this hook has finished all the actions
 * that occur when the mouseup is called. This means that the event listeners will already be removed and onFollowingRef
 * will reflect the new state (false).
 *
 * @returns isFollowingRef, initialMouseX, onMouseDown, onMouseMove, onMouseUp, mouseX
 */
export const useFollowMouse = (
  onFollowingEnd?: () => void
): {
  isFollowing: boolean;
  initialMouseX: number;
  onMouseDown: (event: React.MouseEvent) => void;
  onMouseUp: () => void;
  onMouseMove: (event: MouseEvent) => void;
  mouseX: number;
} => {
  const isFollowingRef = React.useRef(false);
  const initialMouseX = React.useRef<number | undefined>(undefined);
  const [mouseX, setMouseX] = React.useState(0);

  const onMouseMove = React.useCallback(
    (event: MouseEvent) => {
      if (isFollowingRef.current) {
        setMouseX(event.clientX);
      }
    },
    [isFollowingRef]
  );

  const onMouseUp = React.useCallback(() => {
    isFollowingRef.current = false;
    initialMouseX.current = undefined;
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    if (onFollowingEnd) {
      onFollowingEnd();
    }
  }, [onFollowingEnd, onMouseMove]);

  const onMouseDown = React.useCallback(
    (event: React.MouseEvent) => {
      isFollowingRef.current = true;
      initialMouseX.current = event.clientX;

      document.addEventListener('mousemove', onMouseMove);
      document.addEventListener('mouseup', onMouseUp);
    },
    [isFollowingRef, onMouseUp, onMouseMove]
  );

  return {
    isFollowing: isFollowingRef.current,
    initialMouseX: initialMouseX.current,
    onMouseDown,
    onMouseUp,
    onMouseMove,
    mouseX
  };
};

/**
 * Whenever @param updatesFromStore changes, this will start a timer equal to @param timeToWaitMs,
 * and then will call @param setState with the latest value of @param updatesFromStore after that
 * time has passed. When the update occurs, it will call @param callback. If @param updatesFromStore
 * changes, it will reset the timer.
 *
 * This is essentially a debounced function.
 *
 * @param updatesFromStore
 * @param setState
 * @param timeToWaitMs
 * @param callback
 */
export const useDebouncedUpdates = (
  updatesFromStore: unknown,
  setState: (val: any) => void,
  // eslint-disable-next-line default-param-last
  timeToWaitMs = 100,
  callback?: () => void
): void => {
  const debouncedUpdates = React.useRef(undefined);

  React.useEffect(() => {
    clearTimeout(debouncedUpdates.current);
    debouncedUpdates.current = undefined;

    // prevent from thrashing the UI by updating too frequently
    debouncedUpdates.current = setTimeout(async () => {
      await sleep(0); // await sleep to allow this call to be async
      if (callback) callback();
      setState(updatesFromStore);
    }, timeToWaitMs);
    return () => {
      clearTimeout(debouncedUpdates.current);
    };
  }, [setState, updatesFromStore, callback, timeToWaitMs]);
};

/**
 * Whenever @param upstreamUpdates changes, this will start a timer equal to @param timeToWaitMs,
 * and then will call @param setState with the latest value of @param upstreamUpdates after that
 * time has passed. When the update occurs, it will call @param callback. If @param upstreamUpdates
 * changes, it will reset the timer.
 *
 * This is essentially a debounced function.
 *
 * @param upstreamUpdates
 * @param setState
 * @param timeToWaitMs
 * @param callback
 */
export const useThrottledUpdates = (
  upstreamUpdates: unknown,
  setState: (val: any) => void,
  // eslint-disable-next-line default-param-last
  timeToWaitMs = 100,
  callback?: () => void
): void => {
  const lastRan = React.useRef(Date.now());

  React.useEffect(() => {
    const handler = setTimeout(async () => {
      await sleep(0); // await sleep to allow this call to be async
      if (Date.now() - lastRan.current >= timeToWaitMs) {
        if (callback) callback();
        setState(upstreamUpdates);
      }
    }, timeToWaitMs);

    return () => {
      clearTimeout(handler);
    };
  }, [setState, upstreamUpdates, callback, timeToWaitMs]);
};

/**
 * @example
 * const MyComponent: React.FC = () => {
 *   const addDataAttrScroll = useDataAttrScroll();
 *   return <div ref={addDataAttrScroll} />;
 * }
 *
 * @returns a function that may be provided as a ref callback, which will edit the html element,
 * adding a data attribute called `scroll` that will contain the number of pixels scrolled.
 */
export const useDataAttrScroll = (): ((ref: HTMLElement | null) => void) => {
  const cleanup = React.useRef(null);
  React.useEffect(() => {
    return cleanup.current;
  });
  return React.useCallback(ref => {
    if (ref != null) {
      // Reads out the scroll position and stores it in the data attribute
      // so we can use it in our stylesheets
      const storeScroll = debounce(() => {
        // we are actually modifying the DOM here, not just some random param
        // eslint-disable-next-line no-param-reassign
        ref.dataset.scroll = ref.scrollTop.toString();
      });

      ref.addEventListener('scroll', storeScroll);

      // Update scroll position for first time
      storeScroll();

      // create the cleanup function
      cleanup.current = () => ref.removeEventListener('scroll', storeScroll);
    }
  }, []);
};
