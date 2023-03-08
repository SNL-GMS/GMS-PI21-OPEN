import { HotkeyListener } from '@gms/ui-util';
import React from 'react';

/** Zoom in on the center of the zoom interval */
export const ZOOM_TARGET_FRACTION = 0.5;

/**
 * Zooming in and then zooming out should return you to the same zoom interval.
 * These ratios have this effect in the zoomByPercentageToPoint function in the WaveformPanel.
 */
export const ZOOM_IN_RATIO = -0.5;
export const ZOOM_OUT_RATIO = 0.6666666667;

/**
 * The fallback percent of the screen to pan on keypress
 */
export const PAN_RATIO = 0.25;

/**
 * The type of the props for the {@link HotkeyHandler} component
 */
export interface HotkeyHandlerProps {
  children: React.ReactNode;
  pan: (pct: number) => void;
  panRatio?: number;
  zoomInRatio?: number;
  zoomOutRatio?: number;
  zoomByPercentageToPoint: (modPercent: number, x: number) => void;
}

/**
 * Listens for zoom hotkey changes and updates zoom via the provided function in props if zoom hotkeys are pressed.
 */
export function HotkeyHandler({
  children,
  pan,
  panRatio,
  zoomInRatio,
  zoomOutRatio,
  zoomByPercentageToPoint
}: HotkeyHandlerProps) {
  return (
    <div
      className="weavess-zoom-handler"
      onKeyDown={(ev: React.KeyboardEvent<HTMLDivElement>) => {
        if (HotkeyListener.isHotKeyCommandSatisfied(ev.nativeEvent, 'KeyW')) {
          zoomByPercentageToPoint(zoomInRatio ?? ZOOM_IN_RATIO, ZOOM_TARGET_FRACTION);
        }
        if (HotkeyListener.isHotKeyCommandSatisfied(ev.nativeEvent, 'KeyS')) {
          zoomByPercentageToPoint(zoomOutRatio ?? ZOOM_OUT_RATIO, ZOOM_TARGET_FRACTION);
        }
        if (HotkeyListener.isHotKeyCommandSatisfied(ev.nativeEvent, 'KeyA')) {
          ev.stopPropagation();
          pan(panRatio != null ? -1 * panRatio : -1 * PAN_RATIO);
        }
        if (HotkeyListener.isHotKeyCommandSatisfied(ev.nativeEvent, 'KeyD')) {
          ev.stopPropagation();
          pan(panRatio ?? PAN_RATIO);
        }
      }}
      role="tab"
      tabIndex={-1}
    >
      {children}
    </div>
  );
}
