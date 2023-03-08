import type { ToolbarTypes } from '@gms/ui-core-components';
import { NumericInputToolbarItem } from '@gms/ui-core-components';
import * as React from 'react';

const buildNumVisibleWaveforms = (
  analystNumberOfWaveforms: number,
  setAnalystNumberOfWaveforms: (value: number, valueAsString?: string) => void,
  widthPx: number,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <NumericInputToolbarItem
    key={key}
    tooltip="Sets the number of visible channels per screen"
    onChange={value => setAnalystNumberOfWaveforms(value)}
    numericValue={analystNumberOfWaveforms}
    cyData="num-waveform-input"
    minMax={{ min: 1, max: 100 }}
    step={1}
    menuLabel="Number of Channels"
    widthPx={widthPx}
  />
);

/**
 * Creates a toolbar control that lets the user choose the number of waveforms, or returns the previously created
 * toolbar if none of the parameters have changed. Requires referentially stable functions.
 *
 * @param analystNumberOfWaveforms the number of waveforms the analyst has chosen to display
 * @param setAnalystNumberOfWaveforms a setter function for setting the number of waveforms. Must be referentially stable.
 * @param key must be unique
 * @returns the toolbar item
 */
export const useNumWaveformControl = (
  analystNumberOfWaveforms: number,
  setAnalystNumberOfWaveforms: (value: number, valueAsString?: string) => void,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const widthPx = 100;
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () =>
      buildNumVisibleWaveforms(analystNumberOfWaveforms, setAnalystNumberOfWaveforms, widthPx, key),
    [analystNumberOfWaveforms, setAnalystNumberOfWaveforms, key]
  );
};
