/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { CollapseButton } from '../../../src/ts/components/collapse/collapse-button';
import type * as CollapseButtonTypes from '../../../src/ts/components/collapse/types';
import { waitForComponentToPaint } from '../../util/test-util';

const props: CollapseButtonTypes.CollapseButtonProps = {
  buttonText: 'collapse-button',
  isLoading: false,
  isCollapsed: false,
  onClick: jest.fn()
};

describe('CollapseButton', () => {
  it('to be defined', () => {
    expect(CollapseButton).toBeDefined();
  });

  it('CollapseButton renders', () => {
    const { container } = render(<CollapseButton {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('CollapseButton functions and clicks', async () => {
    const mockOnClick = jest.fn();

    const wrapper3 = Enzyme.mount(
      <CollapseButton
        buttonText="collapse-button"
        isLoading={false}
        isCollapsed
        onClick={mockOnClick}
      />
    );

    const cProps = wrapper3.props() as CollapseButtonTypes.CollapseButtonProps;

    cProps.onClick(false);
    await waitForComponentToPaint(wrapper3);

    wrapper3.simulate('click');
    await waitForComponentToPaint(wrapper3);
    expect(wrapper3.prop('isCollapsed')).toEqual(true);

    expect(mockOnClick).toHaveBeenCalledTimes(2);
  });

  it('CollapseButton item clicks', async () => {
    const mockOnClick = jest.fn();

    const wrapper4 = Enzyme.mount(
      <CollapseButton
        // buttonText={'my button'}
        buttonText={viz => (viz ? 'my button' : 'my hidden button')}
        isLoading={false}
        isCollapsed={false}
        onClick={mockOnClick}
      />
    );

    await waitForComponentToPaint(wrapper4);

    const lastCB = wrapper4.find('.collapse-button--open');
    const lastCollapse = lastCB.find('.collapse-button__target--open');
    expect(lastCB).toBeDefined();
    expect(lastCollapse).toBeDefined();
    wrapper4.simulate('click');
    await waitForComponentToPaint(wrapper4);
    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });
});
