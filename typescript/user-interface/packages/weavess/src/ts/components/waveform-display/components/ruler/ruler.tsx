import { HotkeyListener } from '@gms/ui-util';
import moment from 'moment';
import * as React from 'react';

import { useFollowMouse } from '../../../../util/custom-hooks';
import { LabelValue } from './label-value';
import type { RulerProps } from './types';
/**
 * A ruler component that draws lines to the display to illustrate duration and provide info
 *
 * @param props RulerProps
 * @returns A ruler component
 */

// TODO pull out some of this logic into custom hooks to lower it's complexity
// eslint-disable-next-line complexity, react/function-component-definition
export const Ruler: React.FunctionComponent<RulerProps> = (props: RulerProps) => {
  const {
    initialPoint,
    isActive,
    containerDimensions,
    computeTimeSecsForMouseXFractionalPosition,
    onRulerMouseUp
  } = props;

  const { onMouseMove, setMouseX, mouseX, mouseY } = useFollowMouse();
  HotkeyListener.useGlobalHotkeyListener();
  React.useEffect(() => {
    if (isActive) {
      document.addEventListener('mousemove', onMouseMove);
      document.addEventListener('mouseup', onRulerMouseUp);
    } else {
      document.removeEventListener('mouseup', onRulerMouseUp);
      document.removeEventListener('mousemove', onMouseMove);
    }

    return () => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onRulerMouseUp);
    };
  }, [isActive, onMouseMove, onRulerMouseUp, setMouseX]);

  if (!isActive || !initialPoint || mouseX === 0) return null;
  const canvasLeftPos = containerDimensions.canvas.rect?.left ?? 0;
  const canvasWidth = containerDimensions.canvas.rect?.width ?? 0;
  const canvasTop = containerDimensions.canvas.rect?.top ?? 0;
  const canvasHeight = containerDimensions.canvas.rect?.height ?? 0;

  const startX = initialPoint.x - canvasLeftPos;
  const currentMousePositionX = mouseX - canvasLeftPos;
  const currentMousePositionY = mouseY - canvasTop;
  const leftOffset = canvasLeftPos;

  const boundMousePositionX = Math.min(Math.max(0, currentMousePositionX), canvasWidth);
  const timeMouseX = (mouseX - leftOffset) / canvasWidth;
  const boundTimeX = Math.min(Math.max(0, timeMouseX), 1);
  const initialTime = moment
    .unix(computeTimeSecsForMouseXFractionalPosition((initialPoint.x - leftOffset) / canvasWidth))
    .utc();
  const currentTime = moment.unix(computeTimeSecsForMouseXFractionalPosition(boundTimeX)).utc();
  const duration = initialTime.isBefore(currentTime)
    ? currentTime.diff(initialTime)
    : initialTime.diff(currentTime);
  const labelOffset = 115;
  const labelHeight = 120;
  const tooltipBoundLimit = 100;
  const boundMousePositionY = Math.min(Math.max(0, currentMousePositionY), canvasHeight);

  let tooltipPositionX = boundMousePositionX;
  let tooltipPositionY = boundMousePositionY;

  if (tooltipPositionX < tooltipBoundLimit) {
    tooltipPositionX += labelOffset;
  }

  if (tooltipPositionX > canvasWidth - labelOffset) {
    tooltipPositionX -= labelOffset;
  }

  if (tooltipPositionY < tooltipBoundLimit) {
    tooltipPositionY += labelOffset;
  }

  const isModifierKeyActive =
    HotkeyListener.isKeyDown(HotkeyListener.ModifierHotKeys.CONTROL) ||
    HotkeyListener.isKeyDown(HotkeyListener.ModifierHotKeys.ALT) ||
    HotkeyListener.isKeyDown(HotkeyListener.ModifierHotKeys.META) ||
    HotkeyListener.isKeyDown(HotkeyListener.ModifierHotKeys.SHIFT);

  return (
    <>
      {!isModifierKeyActive ? (
        <>
          <div
            className="ruler__start-marker"
            style={{
              transform: `translateX(${startX}px)`
            }}
          />
          <div
            className="ruler__current-marker"
            style={{
              width: `${Math.abs(startX - boundMousePositionX)}px`,
              transform: `translateX(${Math.min(
                startX,
                boundMousePositionX
              )}px) translateY(${boundMousePositionY}px)`
            }}
          />
        </>
      ) : undefined}
      <div
        className="ruler__info"
        style={{
          transform: `translateX(${tooltipPositionX - labelOffset}px) translateY(${
            tooltipPositionY - labelHeight
          }px)`
        }}
      >
        <div className="ruler-tooltip" data-cy="ruler-tooltip">
          <LabelValue
            label="Date"
            value={`${currentTime.format('YYYY-MM-DD')}`}
            tooltip="Date"
            containerClass="ruler-tooltip-container"
          />
          <LabelValue
            label="Time"
            value={`${currentTime.format('HH:mm:ss.SSS')}`}
            tooltip="Time"
            containerClass="ruler-tooltip-container"
          />
          <LabelValue
            label="Duration"
            value={`${moment.utc(duration).format('HH:mm:ss.SSS')}`}
            tooltip="Duration"
            containerClass="ruler-tooltip-container"
          />
        </div>
      </div>
    </>
  );
};
