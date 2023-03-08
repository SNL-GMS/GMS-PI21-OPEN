import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import { EffectiveNowTimeInitializer } from '../../../../src/ts/app/initializers/effective-time-now-initializer';

const store = getStore();

describe('EffectiveNowTimeInitializer', () => {
  test('is defined', () => {
    expect(EffectiveNowTimeInitializer).toBeDefined();
  });

  test('matches snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        <EffectiveNowTimeInitializer />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
