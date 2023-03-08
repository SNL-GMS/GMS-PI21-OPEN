/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import React from 'react';

import { BarChart } from '../../../src/ts/components/charts';
import type { BarChartProps } from '../../../src/ts/components/charts/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('BarChart with defined props', () => {
  const barChartProps: BarChartProps = {
    heightPx: 100,
    widthPx: 100,
    minHeightPx: 5,
    maxBarWidth: 150,
    minBarWidth: 100,
    categories: {
      x: ['1', '2', '3'],
      y: ['a', 'b', 'c']
    },
    barDefs: [
      { id: 'first', color: 'tomato', value: { x: 1, y: 1 } },
      { id: 'second', color: 'bisque', value: { x: 2, y: 2 } },
      { id: 'third', color: 'salmon', value: { x: 3, y: 3 } }
    ],
    padding: {
      top: 10,
      bottom: 10,
      right: 20,
      left: 20
    },
    thresholdsBad: [25],
    thresholdsMarginal: [5, 10, 15],
    id: 'foobar',
    classNames: 'className'
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container } = render(<BarChart {...barChartProps} />);
  it('is exported', () => {
    expect(BarChart).toBeDefined();
  });
  it('Renders', () => {
    expect(container).toBeDefined();
  });
});

describe('BarChart with minimal props', () => {
  const barChartProps: BarChartProps = {
    heightPx: 100,
    widthPx: 5,
    maxBarWidth: 150,
    minBarWidth: 50,
    categories: {
      x: ['1', '2', '3'],
      y: ['a', 'b', 'c']
    },
    barDefs: [
      { id: 'first', color: 'tomato', value: { x: 1, y: 1 } },
      { id: 'second', color: 'bisque', value: { x: 2, y: 2 } },
      { id: 'third', color: 'salmon', value: { x: 3, y: 3 } }
    ],
    padding: {
      top: 10,
      bottom: 10,
      right: 20,
      left: 20
    }
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container } = render(<BarChart {...barChartProps} />);
  it('is exported', () => {
    expect(BarChart).toBeDefined();
  });
  it('Renders', () => {
    expect(container).toBeDefined();
  });
});
