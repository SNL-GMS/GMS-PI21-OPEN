import { render } from '@testing-library/react';
import React from 'react';

import { SwitchToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/switch-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('SwitchToolbarItem', () => {
  test('SwitchToolbarItem renders directly', () => {
    const { container } = render(
      <SwitchToolbarItem
        key="switch"
        switchValue
        onChange={jest.fn()}
        tooltip="Switch tooltip"
        label="Switch Label"
      />
    );

    expect(container).toMatchSnapshot();
  });
});
