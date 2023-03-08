import type { ToolbarTypes } from '@gms/ui-core-components';
import { ButtonGroupToolbarItem } from '@gms/ui-core-components';
import { useKeyboardShortcutConfig } from '@gms/ui-state';
import * as React from 'react';
import { useHotkeys } from 'react-hotkeys-hook';

const buildZASControl = (
  zoomAlignSort: () => void,
  canZAS: boolean,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <ButtonGroupToolbarItem
    key={key}
    buttons={[
      {
        key: `${key}zas`,
        cyData: 'btn-waveform-zas',
        disabled: !canZAS,
        label: 'ZAS',
        tooltip: 'Zoom-align-sort (ZAS) when an event is open (hot key: z)',
        widthPx: 47,
        onButtonClick: () => zoomAlignSort()
      }
    ]}
    label="ZAS"
  />
);

/**
 * Creates a group of one button that zooms, aligns, and sorts the display,
 * or returns the previously created buttons if none of the parameters have
 * changed since last called.
 *
 * @param zoomAlignSort a function that zooms, aligns, sorts the waveform display. Must be referentially stable.
 * @param key must be unique
 * @returns a group of one button that zooms, aligns, sorts the display
 */
export const useZASControl = (
  zoomAlignSort: () => void,
  currentOpenEventId: string,
  featurePredictionQueryDataUnavailable: boolean,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const canZAS = !(
    currentOpenEventId === null ||
    currentOpenEventId === undefined ||
    featurePredictionQueryDataUnavailable
  );
  const keyboardShortcutConfig = useKeyboardShortcutConfig();
  useHotkeys(
    keyboardShortcutConfig?.zas?.hotkeys ?? 'z',
    () => {
      if (canZAS) {
        zoomAlignSort();
      }
    },
    {},
    [canZAS, zoomAlignSort, keyboardShortcutConfig?.zas?.hotkeys]
  );
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () => buildZASControl(zoomAlignSort, canZAS, key),
    [zoomAlignSort, canZAS, key]
  );
};
