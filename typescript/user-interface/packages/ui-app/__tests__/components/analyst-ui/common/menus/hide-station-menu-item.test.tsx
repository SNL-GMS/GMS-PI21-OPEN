import { render } from '@testing-library/react';
import * as React from 'react';

import { HideStationMenuItem } from '../../../../../src/ts/components/analyst-ui/common/menus/hide-station-menu-item';
import type { HideStationMenuItemProps } from '../../../../../src/ts/components/analyst-ui/common/menus/types';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const hideStationCallback = jest.fn();
const props: HideStationMenuItemProps = {
  hideStationCallback,
  stationName: 'AAK',
  showHideText: undefined
};
/**
 * Tests the qc mask menu component
 */
describe('hide-station-menu-item', () => {
  it('exported menu should be defined', () => {
    expect(HideStationMenuItem).toBeDefined();
  });

  it('should create menu item to hide AAK Channel', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<HideStationMenuItem {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('should create menu item to show AAK Channel', () => {
    props.showHideText = `Show ${props.stationName}`;

    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<HideStationMenuItem {...props} />);
    expect(container).toMatchSnapshot();
  });
});
