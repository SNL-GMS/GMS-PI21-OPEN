import type { WeavessTypes } from '@gms/weavess-core';

/**
 * Sets up event listeners to handle mouseup and mousemove events. These listeners handle
 * drawing the measure window selection range on the waveform display.
 */
export const attachMeasureWindowSelectionListeners = (
  startClientX: number,
  displayInterval: WeavessTypes.TimeRange,
  offsetSecs: number,
  computeTimeSecsFromMouseXPixels: (timeSecs: number) => number,
  updateMeasureWindowSelectionTimeRange: (range: WeavessTypes.TimeRange) => void,
  updateMeasureWindow: (timeRange: WeavessTypes.TimeRange) => void
): { onMouseMove: (event: MouseEvent) => void; onMouseUp: (event: MouseEvent) => void } => {
  let isDragging = false;
  const onMouseMove = (event: MouseEvent) => {
    const diff = Math.abs(startClientX - event.clientX);
    // begin drag if moving more than 1 pixel
    if (diff > 1 && !isDragging) {
      isDragging = true;
    }
    const mouseXPosition = event.clientX;
    if (isDragging) {
      const left = Math.min(startClientX, mouseXPosition);
      const right = Math.max(startClientX, mouseXPosition);
      const startTimeSecs = computeTimeSecsFromMouseXPixels(left);
      const endTimeSecs = computeTimeSecsFromMouseXPixels(right);
      updateMeasureWindowSelectionTimeRange({
        startTimeSecs: startTimeSecs - offsetSecs,
        endTimeSecs: endTimeSecs - offsetSecs
      });
    }
  };

  const onMouseUp = (event: MouseEvent) => {
    const mouseXPosition = event.clientX;
    isDragging = false;
    const startTimeSecs = Math.max(
      computeTimeSecsFromMouseXPixels(Math.min(startClientX, mouseXPosition)),
      displayInterval.startTimeSecs
    );
    const endTimeSecs = Math.min(
      computeTimeSecsFromMouseXPixels(Math.max(startClientX, mouseXPosition)),
      displayInterval.endTimeSecs
    );
    updateMeasureWindow({
      startTimeSecs: startTimeSecs - offsetSecs,
      endTimeSecs: endTimeSecs - offsetSecs
    });
    document.body.removeEventListener('mousemove', onMouseMove);
    document.body.removeEventListener('mouseup', onMouseUp);
  };

  document.body.addEventListener('mousemove', onMouseMove);
  document.body.addEventListener('mouseup', onMouseUp);
  return { onMouseMove, onMouseUp };
};
