import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';
import * as util from 'util';

// eslint-disable-next-line max-len
import { ReduxAzimuthSlowness } from '../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/azimuth-slowness-container';

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

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const store = getStore();

it('should render a ReduxAzimuthSlowness component correctly', () => {
  const { container } = render(
    <Provider store={store}>
      <ReduxAzimuthSlowness />
    </Provider>
  );
  // wrapper.dive();
  expect(container).toMatchSnapshot();
});
