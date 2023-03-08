/* eslint-disable react/jsx-props-no-spreading */
import { waitForComponentToPaint } from '@gms/ui-state/__tests__/test-util';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import type { StageExpansionButtonProps } from '../../../../../src/ts/components/analyst-ui/components/workflow/stage-expansion-button';
import { StageExpansionButton } from '../../../../../src/ts/components/analyst-ui/components/workflow/stage-expansion-button';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

describe('Stage Expansion Button', () => {
  it('is exported', () => {
    expect(StageExpansionButton).toBeDefined();
  });

  const stageExpansionButtonProps: StageExpansionButtonProps = {
    isExpanded: false,
    disabled: true,
    stageName: 'stage',
    toggle: jest.fn()
  };

  it('matches snapshot', () => {
    const component = Enzyme.mount(<StageExpansionButton {...stageExpansionButtonProps} />);
    expect(component).toMatchSnapshot();
    const workflowPanel: any = component.find('Memo()');
    expect(workflowPanel).toBeDefined();
  });

  it('shallow mounts', () => {
    const shallow = Enzyme.shallow(<StageExpansionButton {...stageExpansionButtonProps} />);
    expect(shallow).toMatchSnapshot();
    const workflowPanel: any = shallow.find('WorkflowPanel');
    expect(workflowPanel).toBeDefined();
  });

  it('Stage Expansion Button functions and clicks', async () => {
    const wrapper = Enzyme.mount(<StageExpansionButton {...stageExpansionButtonProps} />);

    wrapper.setProps({
      isExpanded: true,
      disabled: false
    });
    wrapper.update();
    await waitForComponentToPaint(wrapper);

    expect(wrapper.props().isExpanded).toBeTruthy();
    expect(wrapper.props().disabled).toBeFalsy();

    const spy = jest.spyOn(wrapper.props(), 'toggle');

    wrapper.props().toggle();
    const button = wrapper.find('button');
    button.simulate('click');

    expect(spy).toHaveBeenCalled();

    wrapper.setProps({ isExpanded: false });
    wrapper.setProps({ disabled: true });

    await waitForComponentToPaint(wrapper);
    expect(wrapper.props().isExpanded).toBeFalsy();
    expect(wrapper.props().disabled).toBeTruthy();
  });
});
