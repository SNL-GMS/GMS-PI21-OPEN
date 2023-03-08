/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import type GoldenLayout from '@gms/golden-layout';
import type { ChartTypes } from '@gms/ui-core-components';
import { BarChart } from '@gms/ui-core-components';
import * as React from 'react';

import { dataAcquisitionUserPreferences } from '~components/data-acquisition-ui/config';

/** The Bar/line chart panel props */
export interface BarChartWrapperProps {
  id: string;
  widthPx: number;
  heightPx: number;
  barChartProps?: ChartTypes.BarChartProps;
  glContainer?: GoldenLayout.Container;
}

/**
 * Bar/line chart panel component - renders a bar chart and line chart
 */
// eslint-disable-next-line react/function-component-definition
export const BarChartWrapper: React.FunctionComponent<BarChartWrapperProps> = props => {
  // define the chart padding for the two charts
  const padding: ChartTypes.ChartPadding = { top: 16, right: 29, bottom: 120, left: 63 };

  const sharedProps = {
    classNames: 'table-display',
    padding,
    minHeightPx: dataAcquisitionUserPreferences.minChartHeightPx
  };

  return (
    <div className="legend-and-charts" data-cy="bar-chart">
      <BarChart
        id={`bar-chart-${props.id}`}
        /* eslint-disable-next-line react/jsx-props-no-spreading */
        {...sharedProps}
        /* eslint-disable-next-line react/jsx-props-no-spreading */
        {...props?.barChartProps}
        key={props.glContainer?.parent?.parent?.isMaximised ? props.widthPx : props.id}
      />
    </div>
  );
};
