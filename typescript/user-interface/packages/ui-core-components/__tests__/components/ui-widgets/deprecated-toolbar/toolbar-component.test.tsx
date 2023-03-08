import { render } from '@testing-library/react';
import React from 'react';

import {
  DeprecatedToolbar,
  DeprecatedToolbarTypes
} from '../../../../src/ts/components/ui-widgets/deprecated-toolbar';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('Toolbar component', () => {
  const toolbarItemsRight: DeprecatedToolbarTypes.ToolbarItem[] = [];
  const toolbarItemsLeft: DeprecatedToolbarTypes.ToolbarItem[] = [];
  const item1: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    rank: 1,
    tooltip: 'test',
    label: 'test',
    value: ''
  };
  const item2: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    rank: 2,
    tooltip: 'test',
    label: 'test',
    value: ''
  };
  const item3: DeprecatedToolbarTypes.LabelValueItem = {
    type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
    rank: 3,
    tooltip: 'test',
    label: 'test',
    value: ''
  };
  toolbarItemsLeft.push(item1);
  toolbarItemsLeft.push(item2);
  toolbarItemsRight.push(item3);
  test('has defined export values', () => {
    expect(DeprecatedToolbar).toBeDefined();
  });
  test('Toolbar builds with snap', () => {
    const { container } = render(
      <DeprecatedToolbar
        toolbarWidthPx={10}
        itemsRight={toolbarItemsRight}
        itemsLeft={toolbarItemsLeft}
      />
    );
    expect(container).toMatchSnapshot();
  });
  test('Toolbar builds with snap and lots of pixels', () => {
    const { container } = render(
      <DeprecatedToolbar
        toolbarWidthPx={1000}
        itemsRight={toolbarItemsRight}
        itemsLeft={toolbarItemsLeft}
      />
    );
    expect(container).toMatchSnapshot();
  });
});
