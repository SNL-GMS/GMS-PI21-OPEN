import * as Enzyme from 'enzyme';
import React from 'react';
import { Provider } from 'react-redux';
import type { Store } from 'redux';

/**
 * Gotta make a fn component in order to test hook
 * https://kentcdodds.com/blog/how-to-test-custom-react-hooks
 */
// eslint-disable-next-line @typescript-eslint/ban-types
export const renderHook = (hook: () => {}): any[] => {
  const returnVal = [];

  function TestFunctionComponent() {
    // eslint-disable-next-line
    Object.assign(returnVal, hook());
    return null;
  }

  Enzyme.shallow(<TestFunctionComponent />);
  return returnVal;
};

/**
 * Gotta make a fn component in order to test hook
 * https://kentcdodds.com/blog/how-to-test-custom-react-hooks
 */
// eslint-disable-next-line @typescript-eslint/ban-types
export const renderReduxHook = (store: Store, hook: () => {}): any => {
  let returnVal;

  function TestFunctionComponent() {
    returnVal = hook();
    return null;
  }

  Enzyme.mount(
    <Provider store={store}>
      <TestFunctionComponent />
    </Provider>
  );
  return returnVal;
};
