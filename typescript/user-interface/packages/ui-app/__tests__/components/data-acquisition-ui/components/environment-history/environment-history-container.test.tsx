/* eslint-disable @typescript-eslint/no-magic-numbers */
import { getStore, withReduxProvider } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import { EnvironmentContainer } from '../../../../../src/ts/components/data-acquisition-ui/components/environment-history/environment-history-container';

const MOCK_TIME = 1611153271425;
Date.now = jest.fn(() => MOCK_TIME);
Date.constructor = jest.fn(() => new Date(MOCK_TIME));

describe('Environment history panel', () => {
  it('should be defined', () => {
    expect(Date.now()).toEqual(MOCK_TIME);
    expect(EnvironmentContainer).toBeDefined();
  });

  it('render container', () => {
    const Component = withReduxProvider(EnvironmentContainer);

    const { container } = render(
      <Provider store={getStore()}>
        <Component />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
