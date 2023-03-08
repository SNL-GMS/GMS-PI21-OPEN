import { HotkeyListener } from '@gms/ui-util';
import type { WeavessTypes } from '@gms/weavess-core';
import { WeavessMessages } from '@gms/weavess-core';
import uniqueId from 'lodash/uniqueId';
import * as React from 'react';

import { getMeasureWindowSelectionAreaFraction } from '../../../../../util/position-util';
import { attachMeasureWindowSelectionListeners } from './measure-window-mouse-handlers';
import { MeasureWindowSelectionArea } from './measure-window-selection-area';
import type { MeasureWindowSelectionListenerProps } from './types';

export const InternalMeasureWindowSelectionListener: React.FC<MeasureWindowSelectionListenerProps> = ({
  displayInterval,
  offsetSecs,
  isMeasureWindowEnabled,
  children,
  toast,
  updateMeasureWindowPanel,
  computeTimeSecsFromMouseXPixels,
  hotKeys
}: MeasureWindowSelectionListenerProps) => {
  HotkeyListener.useGlobalHotkeyListener();
  const { amplitudeScale, drawMeasureWindow } = hotKeys;
  const [selectionTimeRange, setSelectionTimeRange] = React.useState<
    WeavessTypes.TimeRange | undefined
  >(undefined);

  const { current: cleanupCallbacks } = React.useRef<(() => void)[]>([]);
  React.useEffect(() => {
    return () => {
      cleanupCallbacks.forEach(callback => callback());
    };
  }, [cleanupCallbacks]);

  const removeMeasureWindowSelection = React.useCallback(() => {
    setSelectionTimeRange(undefined);
  }, [setSelectionTimeRange]);

  const onMouseDown = React.useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (HotkeyListener.isGlobalHotKeyCommandSatisfied(drawMeasureWindow)) {
        if (!isMeasureWindowEnabled()) {
          toast(WeavessMessages.measureWindowDisabled);
        } else {
          const startClientX = e.clientX;
          const { onMouseMove, onMouseUp } = attachMeasureWindowSelectionListeners(
            startClientX,
            displayInterval,
            offsetSecs,
            computeTimeSecsFromMouseXPixels,
            setSelectionTimeRange,
            timeRange => updateMeasureWindowPanel(timeRange, removeMeasureWindowSelection)
          );
          cleanupCallbacks.push(() => {
            document.body.removeEventListener('mousemove', onMouseMove);
            document.body.removeEventListener('mouseup', onMouseUp);
          });
        }
      }
    },
    [
      cleanupCallbacks,
      computeTimeSecsFromMouseXPixels,
      displayInterval,
      offsetSecs,
      drawMeasureWindow,
      isMeasureWindowEnabled,
      removeMeasureWindowSelection,
      toast,
      updateMeasureWindowPanel
    ]
  );

  /**
   * onMeasureWindowClick event handler
   *
   * @param e The mouse event
   */
  const onMeasureWindowClick = React.useCallback(
    (e: React.MouseEvent<HTMLDivElement>): void => {
      if (
        e.button === 2 ||
        e.altKey ||
        e.ctrlKey ||
        e.metaKey ||
        HotkeyListener.isGlobalHotKeyCommandSatisfied(amplitudeScale) ||
        !selectionTimeRange
      ) {
        return;
      }
      const startClientX = e.clientX;
      const measureWindowAreaDuration =
        selectionTimeRange.endTimeSecs - selectionTimeRange.startTimeSecs;

      let isDragging = false;

      const calculateTimeRange = (clientX: number) => {
        const currentMouseTimeSecs = computeTimeSecsFromMouseXPixels(clientX);

        const startMouseTimeSecs = computeTimeSecsFromMouseXPixels(startClientX);

        const timeDiffSecs = currentMouseTimeSecs - startMouseTimeSecs;
        let startTimeSecs = selectionTimeRange.startTimeSecs + timeDiffSecs;
        let endTimeSecs = selectionTimeRange.endTimeSecs + timeDiffSecs;

        // Clamp the time range to be bounded by the max and min start and end times
        if (startTimeSecs < displayInterval.startTimeSecs) {
          startTimeSecs = displayInterval.startTimeSecs;
          endTimeSecs = startTimeSecs + measureWindowAreaDuration;
        }
        if (endTimeSecs > displayInterval.endTimeSecs) {
          endTimeSecs = displayInterval.endTimeSecs;
          startTimeSecs = endTimeSecs - measureWindowAreaDuration;
        }
        return { startTimeSecs, endTimeSecs };
      };

      const onMouseMove = (event: MouseEvent) => {
        const diff = Math.abs(startClientX - event.clientX);
        // begin drag if moving more than 1 pixel
        if (diff > 1 && !isDragging) {
          isDragging = true;
        }
        const newTimeRange = calculateTimeRange(event.clientX);
        if (isDragging) {
          setSelectionTimeRange(newTimeRange);
        }
      };

      const onMouseUp = (event: MouseEvent) => {
        isDragging = false;
        const newTimeRange = calculateTimeRange(event.clientX);
        updateMeasureWindowPanel(newTimeRange, removeMeasureWindowSelection);
        document.body.removeEventListener('mousemove', onMouseMove);
        document.body.removeEventListener('mouseup', onMouseUp);
      };

      document.body.addEventListener('mousemove', onMouseMove);
      document.body.addEventListener('mouseup', onMouseUp);
    },
    [
      amplitudeScale,
      computeTimeSecsFromMouseXPixels,
      displayInterval.endTimeSecs,
      displayInterval.startTimeSecs,
      removeMeasureWindowSelection,
      selectionTimeRange,
      updateMeasureWindowPanel
    ]
  );

  const MeasureWindowSelection = React.useMemo(
    () => (
      <MeasureWindowSelectionArea
        key={uniqueId()}
        position={getMeasureWindowSelectionAreaFraction(
          selectionTimeRange,
          displayInterval.startTimeSecs,
          displayInterval.endTimeSecs,
          offsetSecs
        )}
        onClick={onMeasureWindowClick}
      />
    ),
    [
      displayInterval.endTimeSecs,
      displayInterval.startTimeSecs,
      offsetSecs,
      onMeasureWindowClick,
      selectionTimeRange
    ]
  );

  return children({
    contentRenderer: MeasureWindowSelection,
    onMouseDown
  });
};

export const MeasureWindowSelectionListener = React.memo(InternalMeasureWindowSelectionListener);
