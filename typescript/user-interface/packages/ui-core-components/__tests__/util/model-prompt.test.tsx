import * as React from 'react';

import { ModalPrompt } from '../../src/ts/components';
import type { PromptProps } from '../../src/ts/components/dialog/types';
// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');
// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
/* eslint-disable import/first */
/* eslint-disable import/no-extraneous-dependencies */
import * as util from 'util';

Object.defineProperty(window, 'TextEncoder', {
  writable: true,
  value: util.TextEncoder
});
Object.defineProperty(window, 'TextDecoder', {
  writable: true,
  value: util.TextDecoder
});
Object.defineProperty(global, 'TextEncoder', {
  writable: true,
  value: util.TextEncoder
});
Object.defineProperty(global, 'TextDecoder', {
  writable: true,
  value: util.TextDecoder
});

import Adapter from '@cfaester/enzyme-adapter-react-18';
import { render } from '@testing-library/react';

const props: PromptProps = {
  title: 'Example Title',
  actionText: 'Accept',
  actionTooltipText: 'Accept the prompt',
  cancelText: 'Reject',
  cancelTooltipText: 'Reject the prompt',
  isOpen: true,
  actionCallback: jest.fn(),
  cancelButtonCallback: jest.fn(),
  onCloseCallback: jest.fn()
};

describe('modal prompt tests', () => {
  beforeEach(() => {
    Enzyme.configure({ adapter: new Adapter() });
  });

  // eslint-disable-next-line react/jsx-props-no-spreading
  const wrapper: any = Enzyme.mount(<ModalPrompt {...props} />);
  it('we pass in basic props', () => {
    const passedInProps = wrapper.props() as PromptProps;
    expect(passedInProps).toMatchSnapshot();
  });
  it('renders', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<ModalPrompt {...props} />);
    expect(container).toMatchSnapshot();
  });
  it('renders children', () => {
    const { container } = render(
      // eslint-disable-next-line react/jsx-props-no-spreading
      <ModalPrompt {...props}>
        <div>Sample Children</div>
      </ModalPrompt>
    );
    expect(container).toMatchSnapshot();
  });
});
