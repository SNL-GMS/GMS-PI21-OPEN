import { render } from '@testing-library/react';
import React from 'react';

import { CustomToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/custom-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('CustomToolbarItem', () => {
  test('CustomToolbarItem renders', () => {
    const { container } = render(
      <CustomToolbarItem key="customitem" element={<div>Custom Item Test</div>} />
    );

    expect(container).toMatchSnapshot();
  });
});
