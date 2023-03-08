/* eslint-disable react/destructuring-assignment */
import type { ConfigurationTypes, SohTypes } from '@gms/common-model';
import type { ValueType } from '@gms/common-util';
import { determinePrecisionByType } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import { nonIdealStateWithNoSpinner } from '@gms/ui-core-components';
import type { SohStatus } from '@gms/ui-state';
import { useAppSelector } from '@gms/ui-state';
import * as React from 'react';

import { useBaseDisplaySize } from '~components/common-ui/components/base-display/base-display-hooks';
import {
  barChartXAxisLabel,
  barChartYAxisTicFormat
} from '~components/data-acquisition-ui/components/historical-trends/utils';
import { dataAcquisitionUserPreferences } from '~components/data-acquisition-ui/config';
import { gmsColors } from '~scss-config/color-preferences';
import { gmsLayout } from '~scss-config/layout-preferences';

import { MAX_BAR_CHART_WIDTH, MIN_BAR_CHART_WIDTH } from '../../../shared/chart/constants';
import {
  buildData,
  getOnContextMenus,
  toolbarBarChartXAxisTicFormat,
  toolbarBarChartYAxisLabel
} from './bar-chart-utils';
import { BarChartWrapper } from './bar-chart-wrapper';
import { QuietIndicatorWrapper } from './quiet-indicator-wrapper';
import type { ChannelSohForMonitorType } from './types';

const PADDING_PX = gmsLayout.displayPaddingPx * 2;
const unknownValueToolTip = 'Unknown';
const isXTickDisabled = (tick: { value: number; tooltip: string }): boolean =>
  tick.value === null && tick.tooltip === unknownValueToolTip;

export interface BarChartPanelProps {
  minHeightPx: number;
  chartHeaderHeight: number;
  type: SohTypes.SohMonitorType;
  station: SohTypes.UiStationSoh;
  sohStatus: SohStatus;
  channelSoh: ChannelSohForMonitorType[];
  sohConfiguration: ConfigurationTypes.UiSohConfiguration;
  valueType: ValueType;
  glContainer?: GoldenLayout.Container;
}

export function BarChartPanel(props: BarChartPanelProps) {
  const [widthPx, heightPx] = useBaseDisplaySize();

  const isStale = useAppSelector(state => state.app.dataAcquisition?.data?.sohStatus?.isStale);

  // get our 2 context menus for the bar and the label
  const onContextMenus = getOnContextMenus(
    props.type,
    props.sohConfiguration.availableQuietTimesMs,
    props.station.stationName,
    isStale,
    props.channelSoh
  );
  const widthWithPaddingPx = widthPx - gmsLayout.displayPaddingPx * 2;
  const barChartHeight = heightPx - props.chartHeaderHeight - PADDING_PX;

  // format the data for passing to the bar chart props
  const chartData = buildData(props.channelSoh, onContextMenus, props.valueType);

  return chartData?.barData.length > 0 ? (
    <BarChartWrapper
      widthPx={widthWithPaddingPx || dataAcquisitionUserPreferences.minChartWidthPx}
      heightPx={barChartHeight || dataAcquisitionUserPreferences.minChartHeightPx}
      glContainer={props.glContainer}
      id={props.type}
      barChartProps={{
        // eslint-disable-next-line @typescript-eslint/unbound-method
        onContextMenuBar: onContextMenus.onContextMenuBar,
        // eslint-disable-next-line @typescript-eslint/unbound-method
        onContextMenuBarLabel: onContextMenus.onContextMenuBarLabel,
        // victory does not take a functional component so we have
        // to cast our quiet component to an any to suppress the linter
        // this can supposedly be fixed by updating to the latest types
        // in our package.json
        dataComponent: QuietIndicatorWrapper as any,
        maxBarWidth: MAX_BAR_CHART_WIDTH,
        minBarWidth: MIN_BAR_CHART_WIDTH,
        scrollBrushColor: gmsColors.gmsMain,
        categories: chartData?.barCategories ? chartData.barCategories : { x: [], y: [] },
        widthPx: widthWithPaddingPx || dataAcquisitionUserPreferences.minChartWidthPx,
        heightPx: barChartHeight || dataAcquisitionUserPreferences.minChartHeightPx,
        barDefs: chartData?.barData,
        disabled: {
          xTicks: {
            disabledColor: gmsColors.gmsChartTickLabelDisabled,
            disabledCondition: isXTickDisabled
          }
        },
        thresholdsBad: chartData?.thresholdsBad,
        thresholdsMarginal: chartData?.thresholdsMarginal,
        yTickFormat: barChartYAxisTicFormat,
        xTickFormat: toolbarBarChartXAxisTicFormat(props.station.stationName, props.channelSoh),
        // !update toolbarBarChartYAxisLabel when adding in new types
        yAxisLabel: toolbarBarChartYAxisLabel(props.type),
        xAxisLabel: barChartXAxisLabel(),
        xTickTooltips: chartData?.barData.map(bar =>
          bar.value.y === null
            ? unknownValueToolTip
            : `${determinePrecisionByType(bar.value.y, props.valueType, true)}`
        )
      }}
    />
  ) : (
    nonIdealStateWithNoSpinner('No data to display', 'Data is filtered out')
  );
}
