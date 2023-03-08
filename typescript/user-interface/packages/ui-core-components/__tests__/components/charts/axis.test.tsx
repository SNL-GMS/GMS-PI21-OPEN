import { render } from '@testing-library/react';
import React from 'react';

import { Axis } from '../../../src/ts/components/charts/axis';
import type { AxisProps } from '../../../src/ts/components/charts/types';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Axis', () => {
  const axisProps: AxisProps = {
    barDefs: [
      {
        color: 'hsl(337.4922359499622deg, 36.84210526315789%, 52.49999999999999%)',
        id: 'AAK.AAK.BHE',
        value: { x: 'AAK.AAK.BHE', y: 2571 }
      },
      {
        color: 'hsl(199.98447189992442deg, 36.84210526315789%, 52.49999999999999%)',
        id: 'AAK.AAK.BHN',
        value: { x: 'AAK.AAK.BHN', y: 2239 }
      },
      {
        color: 'hsl(62.47670784988662deg, 36.84210526315789%, 52.49999999999999%)',
        id: 'AAK.AAK.BHZ',
        value: { x: 'AAK.AAK.BHZ', y: 2450 }
      }
    ],
    rotateAxis: true,
    xAxisLabel: 'Channel Name',
    xTickFormat: undefined,
    xTickTooltips: ['2571', '2239', '2450'],
    yAxisLabel: 'Average Lag (s)',
    yTickFormat: undefined
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container } = render(<Axis {...axisProps} />);
  it('is exported', () => {
    expect(Axis).toBeDefined();
  });
  it('Renders', () => {
    expect(container).toMatchSnapshot();
  });
});
