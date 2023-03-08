import { IconNames } from '@blueprintjs/icons';
import { render } from '@testing-library/react';
import React from 'react';

import { ButtonGroupToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/button-group-item';
import type { ButtonToolbarItemProps } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/button-item';
import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { ButtonGroupOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/button-group-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const buttonProps: ButtonToolbarItemProps = {
  key: 'button-item-1',
  disabled: false,
  label: 'Pan Left',
  tooltip: 'Pan waveforms to the left',
  icon: IconNames.ARROW_LEFT,
  onlyShowIcon: true,
  onButtonClick: () => jest.fn()
};

const buttonGroupToolbarItem = (
  <ButtonGroupToolbarItem
    key="buttongroup"
    buttons={[buttonProps]}
    tooltip="Button675 Group Hello"
    hasIssue={false}
    widthPx={50}
  />
);

const loadingSpinnerToolbarItem = (
  <LoadingSpinnerToolbarItem
    key="loadingspinner"
    itemsToLoad={1}
    tooltip="Hello LoadingSpinner"
    hasIssue={false}
    widthPx={50}
  />
);

describe('ButtonGroupOverflowMenuToolbarItem', () => {
  test('ButtonGroupOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <ButtonGroupOverflowMenuToolbarItem
        item={buttonGroupToolbarItem.props}
        menuKey={buttonGroupToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('ButtonGroupOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <ButtonGroupOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
