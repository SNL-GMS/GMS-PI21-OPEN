import { H1 } from '@blueprintjs/core';
import type { ReactWrapper } from 'enzyme';
import React from 'react';

import { withReduxProvider } from '../../src/ts/app/redux-provider';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

window.alert = jest.fn();
window.open = jest.fn();

// simple component we can wrap
class Welcome extends React.PureComponent {
  public render() {
    return <H1>Hello</H1>;
  }
}

describe('Redux wrapper', () => {
  const component: any = Welcome;
  const Wrapper = withReduxProvider(component);

  // make sure the function is defined
  test('should exist', () => {
    expect(withReduxProvider).toBeDefined();
  });

  // see what we got from the wrapper (should be a constructor function for a class)
  test('function should create a component class', () => {
    // returns a class function that we can call with the new keyword
    expect(typeof Wrapper).toBe('function');
  });

  // TODO Fix this tests nothing
  // lets render our wrapper and see what we get back
  test.skip('can create a rendered wrapper', () => {
    const mountedWrapper: ReactWrapper = Enzyme.mount(<Wrapper />);
    // make sure we can make a client if we call the client creation function
    expect(mountedWrapper).toMatchSnapshot();
  });
});
