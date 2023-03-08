import { IconNames } from '@blueprintjs/icons';
import type { CommonTypes } from '@gms/common-model';
import { WaveformTypes } from '@gms/common-model';
import type { ToolbarTypes } from '@gms/ui-core-components';
import { ButtonGroupToolbarItem } from '@gms/ui-core-components';
import { useGetProcessingAnalystConfigurationQuery, useZoomInterval } from '@gms/ui-state';
import * as React from 'react';

const buildPanGroup = (
  pan: (panDirection: WaveformTypes.PanType, shouldLoadAdditionalData: boolean) => void,
  canPanLeft: boolean,
  canPanRight: boolean,
  key: string | number
): ToolbarTypes.ToolbarItemElement => (
  <ButtonGroupToolbarItem
    key={key}
    buttons={[
      {
        key: `${key}panleft`,
        cyData: 'btn-pan-waveform-left',
        disabled: !canPanLeft,
        label: 'Pan Left',
        tooltip: 'Pan waveforms to the left',
        icon: IconNames.ARROW_LEFT,
        onlyShowIcon: true,
        onButtonClick: () => pan(WaveformTypes.PanType.Left, true)
      },
      {
        key: `${key}panright`,
        cyData: 'btn-pan-waveform-right',
        disabled: !canPanRight,
        label: 'Pan Right',
        tooltip: 'Pan waveforms to the Right',
        icon: IconNames.ARROW_RIGHT,
        onlyShowIcon: true,
        onButtonClick: () => pan(WaveformTypes.PanType.Right, true)
      }
    ]}
    label="pan"
  />
);

/**
 * Creates a group of two buttons that pan the display, or returns the previously created
 * buttons if none of the parameters have changed since last called.
 *
 * @param pan a function that pans the waveform display. Must be referentially stable.
 * @param key must be unique
 * @returns a group of two buttons that pan the display left or right.
 */
export const usePanGroupControl = (
  pan: (panDirection: WaveformTypes.PanType, shouldLoadAdditionalData: boolean) => void,
  currentTimeInterval: CommonTypes.TimeRange,
  viewableTimeInterval: CommonTypes.TimeRange,
  key: string | number
): ToolbarTypes.ToolbarItemElement => {
  const [zoomTimeInterval] = useZoomInterval();
  const processingAnalystConfiguration = useGetProcessingAnalystConfigurationQuery();
  const earliestAllowedTime =
    currentTimeInterval.startTimeSecs -
    Number(processingAnalystConfiguration.data?.waveformPanningBoundaryDuration ?? -Infinity);
  const latestAllowedTime =
    currentTimeInterval.endTimeSecs +
    Number(processingAnalystConfiguration.data?.waveformPanningBoundaryDuration ?? Infinity);
  const canPanLeft = zoomTimeInterval
    ? Math.floor(zoomTimeInterval.startTimeSecs) > Math.ceil(earliestAllowedTime) ||
      zoomTimeInterval.startTimeSecs > viewableTimeInterval.startTimeSecs
    : false;
  const canPanRight = zoomTimeInterval
    ? Math.ceil(zoomTimeInterval.endTimeSecs) < Math.floor(latestAllowedTime) ||
      zoomTimeInterval.endTimeSecs < viewableTimeInterval.endTimeSecs
    : false;
  return React.useMemo<ToolbarTypes.ToolbarItemElement>(
    () => buildPanGroup(pan, canPanLeft, canPanRight, key),
    [canPanLeft, canPanRight, pan, key]
  );
};
