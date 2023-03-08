/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { PieChart } from '../../../../src/ts/components/charts/pie-chart/pie-chart';
import type * as PieChartTypes from '../../../../src/ts/components/charts/pie-chart/types';
import { waitForComponentToPaint } from '../../../util/test-util';

const pieChartStyle: PieChartTypes.PieChartStyle = {
  diameterPx: 100,
  borderPx: 5
};

const props: PieChartTypes.PieChartProps = {
  style: pieChartStyle,
  percent: 40,
  className: 'string',
  pieSliceClass: 'string',
  status: 'string'
};

describe('Pie Chart', () => {
  it('to be defined', () => {
    expect(PieChart).toBeDefined();
  });

  it('Pie Chart renders', () => {
    const { container } = render(<PieChart {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('History List functions and clicks', async () => {
    const wrapper3 = Enzyme.mount(<PieChart {...props} />);

    wrapper3.setProps({ percent: 50 });
    await waitForComponentToPaint(wrapper3);
    expect(wrapper3.find('PieSlice')).toBeDefined();
  });
});
