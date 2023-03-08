import * as Enzyme from 'enzyme';
import React from 'react';

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

  Enzyme.mount(<TestFunctionComponent />);
  return returnVal;
};
