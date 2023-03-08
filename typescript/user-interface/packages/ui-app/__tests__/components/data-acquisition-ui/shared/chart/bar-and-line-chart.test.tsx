/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import React from 'react';

import type { BarAndLineChartProps } from '../../../../../src/ts/components/data-acquisition-ui/shared/chart/bar-and-line-chart';
import { BarAndLineChart } from '../../../../../src/ts/components/data-acquisition-ui/shared/chart/bar-and-line-chart';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

Date.now = jest.fn().mockReturnValue(() => 1000);

describe('BarAndLineChart', () => {
  it('is BarAndLineChart exported', () => {
    expect(BarAndLineChart).toBeDefined();
  });

  it('renders BarAndLineChart undefined data 01', () => {
    const lineChartProps: BarAndLineChartProps = {
      id: `sample`,
      heightPx: 100,
      widthPx: 100
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarAndLineChart {...lineChartProps} />);
    expect(container).toMatchSnapshot();
  });

  it('renders BarAndLineChart undefined data 02', () => {
    const lineChartProps: BarAndLineChartProps = {
      id: `sample`,
      heightPx: 100,
      widthPx: 100,
      barChart: undefined,
      lineChart: undefined
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarAndLineChart {...lineChartProps} />);
    expect(container).toMatchSnapshot();
  });

  it('renders BarAndLineChart empty data', () => {
    const lineChartProps: BarAndLineChartProps = {
      id: `sample`,
      heightPx: 100,
      widthPx: 100,
      barChart: {
        heightPx: 100,
        widthPx: 100,
        maxBarWidth: 100,
        minBarWidth: 100,
        categories: { x: [], y: [] },
        barDefs: [],
        scrollBrushColor: undefined,
        thresholdsMarginal: undefined,
        thresholdsBad: undefined,
        disabled: undefined,
        dataComponent: undefined
      },
      lineChart: {
        heightPx: 100,
        widthPx: 100,
        startTimeMs: 0,
        endTimeMs: 100,
        xAxisLabel: undefined,
        yAxisLabel: undefined,
        lineDefs: []
      }
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarAndLineChart {...lineChartProps} />);
    expect(container).toMatchSnapshot();
  });

  it('renders BarAndLineChart multiple data', () => {
    const lineChartProps: BarAndLineChartProps = {
      id: `sample`,
      heightPx: 100,
      widthPx: 100,
      barChart: {
        heightPx: 100,
        widthPx: 100,
        maxBarWidth: 100,
        minBarWidth: 100,
        categories: { x: ['1', '2'], y: ['1', '2'] },
        barDefs: [
          {
            id: '1',
            color: '000000',
            value: {
              x: 1,
              y: 1
            }
          },
          {
            id: '2',
            color: '000000',
            value: {
              x: 2,
              y: 2
            }
          }
        ],
        scrollBrushColor: undefined,
        thresholdsMarginal: undefined,
        thresholdsBad: undefined,
        disabled: undefined,
        dataComponent: undefined
      },
      lineChart: {
        heightPx: 100,
        widthPx: 100,
        startTimeMs: 0,
        endTimeMs: 100,
        xAxisLabel: undefined,
        yAxisLabel: undefined,
        lineDefs: [
          {
            id: 1,
            color: 'red',
            values: new Float32Array([1, 1, 2, 2, 3, 3]),
            average: 2
          },
          {
            id: 2,
            color: 'blue',
            values: new Float32Array([4, 4, 5, 5, 6, 6]),
            average: 5
          }
        ]
      }
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarAndLineChart {...lineChartProps} />);
    expect(container).toMatchSnapshot();
  });
});
