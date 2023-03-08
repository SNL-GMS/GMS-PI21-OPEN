/* eslint-disable react/destructuring-assignment */
import { Drawer, Position } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { SohTypes } from '@gms/common-model';
import type { ValueType } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import type { CheckboxListEntry, DeprecatedToolbarTypes } from '@gms/ui-core-components';
import { SimpleCheckboxList } from '@gms/ui-core-components';
import type { SohStatus, UiHistoricalSohAsTypedArray } from '@gms/ui-state';
import { useRetrieveDecimatedHistoricalStationSohQuery } from '@gms/ui-state';
import { DistinctColorPalette } from '@gms/ui-util';
import Immutable from 'immutable';
import * as React from 'react';

import { useBaseDisplaySize } from '~components/common-ui/components/base-display/base-display-hooks';
import { messageConfig } from '~components/data-acquisition-ui/config/message-config';
import { DrillDownTitle } from '~components/data-acquisition-ui/shared/drill-down-components';
import { BaseToolbar } from '~components/data-acquisition-ui/shared/toolbars/base-toolbar';
import { useTrendTimeIntervalSelector } from '~components/data-acquisition-ui/shared/toolbars/trend-time-interval-hook';
import { gmsLayout } from '~scss-config/layout-preferences';

import { TITLE_HEIGHT_PX, TOOLBAR_HEIGHT_PX } from '../../shared/chart/constants';
import { useShowLegend } from '../../shared/show-legend-hook';
import { BarLineChartPanel } from './bar-line-chart-panel';
import { validateNonIdealState } from './non-ideal-states';
import { getChartHeight } from './utils';

/** The Historical Trends panel props */
export interface HistoricalTrendsPanelProps {
  monitorType: SohTypes.SohMonitorType;
  station: SohTypes.UiStationSoh;
  sohStatus: SohStatus;
  sohHistoricalDurations: number[];
  valueType: ValueType;
  displaySubtitle: string;
  glContainer?: GoldenLayout.Container;
}

export interface HistoricalTrendsInnerPanelProps {
  uiHistoricalSoh: UiHistoricalSohAsTypedArray;
  timeIntervalSelector: DeprecatedToolbarTypes.DateRangePickerItem;
  widthPx: number;
  heightPx: number;
  startTimeMs: number;
  endTimeMs: number;
  glContainer?: GoldenLayout.Container;
}

/**
 * Map used for keeping track of what channels are visible in the charts
 *
 * @param names string array of channel names
 */
export const useChannelVisibilityMap = (
  names: string[]
): [
  Immutable.Map<string, boolean>,
  React.Dispatch<React.SetStateAction<Immutable.Map<string, boolean>>>
] => {
  let initialChannelVisibilityMap = Immutable.Map<string, boolean>();
  names.forEach(name => {
    initialChannelVisibilityMap = initialChannelVisibilityMap.set(name, true);
  });
  const [channelVisibilityMap, setChannelVisibilityMap] = React.useState<
    Immutable.Map<string, boolean>
  >(initialChannelVisibilityMap);
  return [channelVisibilityMap, setChannelVisibilityMap];
};

/**
 * Renders the various history charts.
 * Depending on the props passed into this component it will
 * render historical data from soh monitor types.
 *
 * @param props the props
 */
function HistoricalTrendsHistoryInnerPanel(
  props: HistoricalTrendsPanelProps & HistoricalTrendsInnerPanelProps
) {
  const drawerSizePx = 240;
  const channelNames = props.uiHistoricalSoh.monitorValues.map(mv => mv.channelName).sort();
  const colorPalette = new DistinctColorPalette(channelNames, props.station.stationName);
  const [channelVisibilityMap, setChannelVisibilityMap] = useChannelVisibilityMap(channelNames);

  const [legend, isLegendVisible, setShowLegend] = useShowLegend(
    'Shows legend for bar and line graph',
    IconNames.SERIES_FILTERED
  );

  const onChange = (channelName: string) => {
    setChannelVisibilityMap(
      channelVisibilityMap.set(channelName, !channelVisibilityMap.get(channelName))
    );
  };

  const legendValueToColor = channelNames?.map(name => {
    const checkboxListEntry: CheckboxListEntry = {
      name,
      color: colorPalette.getColorString(name),
      isChecked: channelVisibilityMap.get(name)
    };
    return checkboxListEntry;
  });

  return (
    <>
      <BaseToolbar
        widthPx={props.widthPx}
        itemsLeft={[legend]}
        itemsRight={[props.timeIntervalSelector]}
      />
      <DrillDownTitle
        title={props.station.stationName}
        subtitle={props.displaySubtitle}
        description={messageConfig.labels.decimationDescription(
          props.uiHistoricalSoh.percentageSent
        )}
      />
      <BarLineChartPanel
        legendTitle={props.station.stationName}
        startTimeMs={props.startTimeMs}
        endTimeMs={props.endTimeMs}
        heightPx={props.heightPx}
        widthPx={props.widthPx - gmsLayout.displayPaddingPx * 2}
        entryVisibilityMap={channelVisibilityMap}
        colorPalette={colorPalette}
        monitorType={props.monitorType}
        station={props.station}
        valueType={props.valueType}
        uiHistoricalSoh={{
          ...props.uiHistoricalSoh,
          monitorValues: props.uiHistoricalSoh.monitorValues.filter(mv =>
            channelVisibilityMap.get(mv.channelName)
          )
        }}
        glContainer={props.glContainer}
      />
      <Drawer
        className="soh-legend"
        title={`${props.station.stationName} Channels`}
        isOpen={isLegendVisible}
        autoFocus
        canEscapeKeyClose
        canOutsideClickClose
        enforceFocus={false}
        hasBackdrop={false}
        position={Position.LEFT}
        size={drawerSizePx}
        onClose={() => setShowLegend(!isLegendVisible)}
        usePortal={false}
      >
        <SimpleCheckboxList checkBoxListEntries={legendValueToColor} onChange={onChange} />
      </Drawer>
    </>
  );
}

/**
 * Panel wrapper used for maintaining the react hooks and executing the query.
 * Handles non ideal states for trends displays but provides access to the toolbar
 *
 * @param props the props
 */
export function HistoricalTrendsHistoryPanel(props: HistoricalTrendsPanelProps) {
  const [widthPx, baseHeightPx] = useBaseDisplaySize();
  const heightPx = getChartHeight(baseHeightPx - TOOLBAR_HEIGHT_PX - TITLE_HEIGHT_PX);
  const [startTimeMs, endTimeMs, timeIntervalSelector] = useTrendTimeIntervalSelector(
    props.monitorType,
    props.sohHistoricalDurations
  );

  // request historical SOH data request needs to be in epoch seconds not milliseconds
  const historicalSohByStation = useRetrieveDecimatedHistoricalStationSohQuery({
    stationName: props.station.stationName,
    startTime: startTimeMs / 1000,
    endTime: endTimeMs / 1000,
    sohMonitorType: props.monitorType
  });

  const nonIdealState = validateNonIdealState(
    props.monitorType,
    historicalSohByStation.isFetching || historicalSohByStation.isLoading,
    historicalSohByStation.isError,
    historicalSohByStation.data,
    startTimeMs,
    endTimeMs
  );

  if (nonIdealState) {
    return (
      <>
        <BaseToolbar widthPx={widthPx} itemsRight={[timeIntervalSelector]} />
        <DrillDownTitle title={props.station.stationName} subtitle={props.displaySubtitle} />
        {nonIdealState}
      </>
    );
  }

  return (
    <HistoricalTrendsHistoryInnerPanel
      // eslint-disable-next-line react/jsx-props-no-spreading
      {...props}
      key={props.station.stationName}
      uiHistoricalSoh={historicalSohByStation.data}
      timeIntervalSelector={timeIntervalSelector}
      startTimeMs={startTimeMs}
      endTimeMs={endTimeMs}
      glContainer={props.glContainer}
      widthPx={widthPx}
      heightPx={heightPx}
    />
  );
}
