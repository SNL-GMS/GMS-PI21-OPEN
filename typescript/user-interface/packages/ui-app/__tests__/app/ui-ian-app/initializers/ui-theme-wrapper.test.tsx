import { ConfigurationTypes } from '@gms/common-model';
import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import {
  getCssVarsFromTheme,
  UIThemeWrapper
} from '../../../../src/ts/app/initializers/ui-theme-wrapper';

const store = getStore();

describe('UIThemeWrapper', () => {
  test('is defined', () => {
    expect(UIThemeWrapper).toBeDefined();
  });

  test('matches snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        <UIThemeWrapper>arbitrary children for wrapper</UIThemeWrapper>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
  test('convert color theme to css', () => {
    expect(getCssVarsFromTheme(ConfigurationTypes.defaultColorTheme)).toMatchSnapshot();
  });
});
