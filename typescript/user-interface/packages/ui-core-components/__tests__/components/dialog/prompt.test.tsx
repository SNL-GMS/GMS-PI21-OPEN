/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { ModalPrompt } from '../../../src/ts/components/dialog/prompt';
import type * as ModalPromptTypes from '../../../src/ts/components/dialog/types';
import { waitForComponentToPaint } from '../../util/test-util';

const props: ModalPromptTypes.PromptProps = {
  title: 'string',
  actionText: 'string',
  actionTooltipText: 'string',
  cancelText: 'string',
  cancelTooltipText: 'string',
  isOpen: true,
  optionalButton: true,
  optionalText: 'string',
  optionalTooltipText: 'string',
  actionCallback: jest.fn(),
  cancelButtonCallback: jest.fn(),
  onCloseCallback: jest.fn(),
  optionalCallback: jest.fn()
};

describe('ModalPrompt', () => {
  it('to be defined', () => {
    expect(ModalPrompt).toBeDefined();
  });

  it('Modal Prompt renders', () => {
    const { container } = render(<ModalPrompt {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('Modal Prompt functions and clicks', async () => {
    const mockActionCallback = jest.fn();
    const mockCancelButtonCallback = jest.fn();
    const mockCloseCallback = jest.fn();
    const mockOptionalCallback = jest.fn();

    const wrapper3 = Enzyme.mount(
      <ModalPrompt
        title="string"
        actionText="string"
        actionTooltipText="string"
        cancelText="string"
        cancelTooltipText="string"
        isOpen={false}
        optionalButton={false}
        optionalText="string"
        optionalTooltipText="string"
        optionalCallback={mockOptionalCallback}
        actionCallback={mockActionCallback}
        cancelButtonCallback={mockCancelButtonCallback}
        onCloseCallback={mockCloseCallback}
      />
    );

    wrapper3.setProps({ isOpen: true });
    await waitForComponentToPaint(wrapper3);

    wrapper3.find('.dialog_parent').last().simulate('close');
    await waitForComponentToPaint(wrapper3);
    expect(mockCloseCallback).toHaveBeenCalledTimes(0);

    wrapper3.setProps({ isOpen: true });
    await waitForComponentToPaint(wrapper3);

    expect(wrapper3.find('Button')).toBeDefined();
    wrapper3.find('button').last().simulate('click');
    await waitForComponentToPaint(wrapper3);

    wrapper3.setProps({ isOpen: true });
    await waitForComponentToPaint(wrapper3);

    wrapper3.find('button').first().simulate('click');
    await waitForComponentToPaint(wrapper3);

    expect(mockActionCallback).toHaveBeenCalledTimes(0);
    expect(mockCancelButtonCallback).toHaveBeenCalled();
  });
});
