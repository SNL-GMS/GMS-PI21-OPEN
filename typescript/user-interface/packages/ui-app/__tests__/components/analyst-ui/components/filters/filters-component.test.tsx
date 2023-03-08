import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import { FiltersComponent } from '../../../../../src/ts/components/analyst-ui/components/filters/filters-component';

const { container } = render(
  <Provider store={getStore()}>
    <FiltersComponent glContainer={{} as any} />
  </Provider>
);

describe('ui ian signal detections', () => {
  test('is defined', () => {
    expect(FiltersComponent).toBeDefined();
  });

  test('can mount signal detections', () => {
    expect(container).toMatchSnapshot();
  });
});
