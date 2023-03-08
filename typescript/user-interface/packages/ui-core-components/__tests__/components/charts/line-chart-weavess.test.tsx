import { render } from '@testing-library/react';
import React from 'react';

import { LineChartWeavess } from '../../../src/ts/components/charts';
import {
  convertToWeavessData,
  defaultRange,
  getTicksAndRange,
  paddingPercent,
  YAxisLabel
} from '../../../src/ts/components/charts/line-chart-weavess';
import type { LineChartWeavessProps } from '../../../src/ts/components/charts/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('LineChartWeavess', () => {
  it('is LineChartWeavess exported', () => {
    expect(LineChartWeavess).toBeDefined();
  });

  it('is YAxisLabel exported', () => {
    expect(YAxisLabel).toBeDefined();
  });

  it('is convertToWeavessData exported', () => {
    expect(convertToWeavessData).toBeDefined();
  });

  it('renders LineChartWeavess undefined data', () => {
    const lineChartProps: LineChartWeavessProps = {
      heightPx: 100,
      widthPx: 100,
      startTimeMs: 0,
      endTimeMs: 100,
      lineDefs: undefined
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container: mockLineChart } = render(<LineChartWeavess {...lineChartProps} />);
    expect(mockLineChart).toMatchSnapshot();
  });

  it('renders LineChartWeavess empty data', () => {
    const lineChartProps: LineChartWeavessProps = {
      heightPx: 100,
      widthPx: 100,
      startTimeMs: 0,
      endTimeMs: 100,
      lineDefs: []
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container: mockLineChart } = render(<LineChartWeavess {...lineChartProps} />);
    expect(mockLineChart).toMatchSnapshot();
  });

  it('renders LineChartWeavess single line', () => {
    const lineChartProps: LineChartWeavessProps = {
      heightPx: 100,
      widthPx: 100,
      startTimeMs: 1,
      endTimeMs: 3,
      lineDefs: [
        {
          id: 1,
          color: 'red',
          values: new Float32Array([1, 1, 2, 2, 3, 3]),
          average: 2
        }
      ]
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container: mockLineChart } = render(<LineChartWeavess {...lineChartProps} />);
    expect(mockLineChart).toMatchSnapshot();
  });

  it('renders LineChartWeavess multiple lines', () => {
    const lineChartProps: LineChartWeavessProps = {
      heightPx: 100,
      widthPx: 100,
      startTimeMs: 1,
      endTimeMs: 3,
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
          // eslint-disable-next-line @typescript-eslint/no-magic-numbers
          values: new Float32Array([4, 4, 5, 5, 6, 6]),
          average: 5
        }
      ]
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container: mockLineChart } = render(<LineChartWeavess {...lineChartProps} />);
    expect(mockLineChart).toMatchSnapshot();
  });

  it('converts weavess data', () => {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(convertToWeavessData([], `my label`, 100, 0, 50)).toEqual([]);
  });

  it('gets custom ticks if the values provided are valid', () => {
    const numTicks = 10;
    const min = 0;
    const max = 100;
    const padding = (max - min) * paddingPercent;
    const result = getTicksAndRange(min, max, numTicks);
    expect(result.defaultRange.min).toEqual(min - padding); // handle the padding, so nothing gets cut off
    expect(result.defaultRange.max).toEqual(max + padding); // handle the padding, so nothing gets cut off
    expect(result.yAxisTicks).toHaveLength(numTicks);
    expect(result).toMatchSnapshot();
  });

  it('gets default ticks if the values provided are invalid', () => {
    const numTicks = 6;
    const min = undefined;
    const max = undefined;
    const padding = defaultRange * 2 * paddingPercent;
    const result = getTicksAndRange(min, max, numTicks);
    expect(result.defaultRange.min).toEqual(-1 * defaultRange - padding); // handle the padding, so nothing gets cut off
    expect(result.defaultRange.max).toEqual(defaultRange + padding); // handle the padding, so nothing gets cut off
    expect(result.yAxisTicks).toHaveLength(numTicks);
    expect(result).toMatchSnapshot();
  });

  it('gets default ticks if the values provided are equal', () => {
    const numTicks = 6;
    const min = 1;
    const max = 1;
    const result = getTicksAndRange(min, max, numTicks);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(result.defaultRange.min).toEqual(0.896);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(result.defaultRange.max).toEqual(1.104);
    expect(result.yAxisTicks).toHaveLength(3);
    expect(result).toMatchSnapshot();
  });

  it('throws error', () => {
    const numTicks = 6;
    const min = -1;
    const max = -1;
    expect(() => getTicksAndRange(min, max, numTicks)).toThrow();
  });

  it('renders custom line y-axis label', () => {
    const { container: mockYAxisLabel } = render(
      <YAxisLabel
        channel={{
          id: 'custom-label',
          name: 'My custom label'
        }}
        isDefaultChannel
        isExpandable={false}
        expanded={false}
        yAxisBounds={undefined}
        selections={undefined}
        showMaskIndicator={false}
        distance={0}
        distanceUnits={undefined}
        azimuth={0}
        channelName="custom-label"
      />
    );
    expect(mockYAxisLabel).toMatchSnapshot();
  });
});
