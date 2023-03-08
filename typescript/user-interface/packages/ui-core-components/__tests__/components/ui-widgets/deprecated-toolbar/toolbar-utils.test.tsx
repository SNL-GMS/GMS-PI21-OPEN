import {
  getOverflowMenuItems,
  getSizeOfAllRenderedItems,
  getSizeOfItems,
  renderItem,
  renderMenuItem
} from '../../../../src/ts/components/ui-widgets/deprecated-toolbar/toolbar-utils';
import * as DeprecatedToolbarTypes from '../../../../src/ts/components/ui-widgets/deprecated-toolbar/types';
import type { PopoverButton } from '../../../../src/ts/components/ui-widgets/popover-button';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('Toolbar utils', () => {
  test('Toolbar utils has defined exported functions', () => {
    expect(renderMenuItem).toBeDefined();
    expect(renderItem).toBeDefined();
    expect(getSizeOfItems).toBeDefined();
    expect(getSizeOfAllRenderedItems).toBeDefined();
    expect(getOverflowMenuItems).toBeDefined();
  });
  test('renderMenuItem renders item', () => {
    const item: DeprecatedToolbarTypes.LabelValueItem = {
      type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
      ianApp: true,
      label: 'Description',
      tooltip: 'Station Description',
      widthPx: 400,
      rank: 1,
      value: 'value'
    };
    const redneredItem = renderMenuItem(item, false, 'menuKey');
    expect(redneredItem).toMatchSnapshot();
  });

  test('renderItem renders item', () => {
    const popoverButtonMap: Map<number, PopoverButton> = new Map<number, PopoverButton>();
    const item: DeprecatedToolbarTypes.ToolbarItem = {
      type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
      ianApp: true,
      label: 'Description',
      tooltip: 'Station Description',
      widthPx: 400,
      rank: 1,
      value: 'value'
    };
    const redneredItem = renderItem(item, popoverButtonMap, false);
    expect(redneredItem).toMatchSnapshot();
  });

  test('getSizeOfItems returns the item size', () => {
    const toolbarItemRefs: any[] = [
      {
        getBoundingClientRect: jest.fn(() => {
          return {
            width: 10
          };
        })
      },
      {
        getBoundingClientRect: jest.fn(() => {
          return {
            width: 10
          };
        })
      }
    ];
    const resultWidth = 20;
    expect(getSizeOfItems(toolbarItemRefs)).toEqual(resultWidth);
    expect(toolbarItemRefs[0].getBoundingClientRect).toHaveBeenCalledTimes(1);
  });

  test('getSizeOfItems returns 0 if no refs', () => {
    const toolbarItemRefs: any[] = [];
    const resultWidth = 0;
    expect(getSizeOfItems(toolbarItemRefs)).toEqual(resultWidth);
  });

  test('getSizeOfAllRenderedItems returns the items total size', () => {
    const toolbarItemRightRefs: any[] = [
      {
        getBoundingClientRect: jest.fn(() => {
          return {
            width: 10
          };
        })
      },
      {
        getBoundingClientRect: jest.fn(() => {
          return {
            width: 10
          };
        })
      }
    ];
    const toolbarItemLeftRefs: any[] = [
      {
        getBoundingClientRect: jest.fn(() => {
          return {
            width: 10
          };
        })
      },
      {
        getBoundingClientRect: jest.fn(() => {
          return {
            width: 10
          };
        })
      }
    ];
    const whiteSpace = 10;
    const resultWidth = 50;
    expect(
      getSizeOfAllRenderedItems(toolbarItemLeftRefs, toolbarItemRightRefs, whiteSpace)
    ).toEqual(resultWidth);
  });

  test('getOverflowMenuItems return items', () => {
    const itemsRight: DeprecatedToolbarTypes.ToolbarItem[] = [
      {
        type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
        ianApp: true,
        tooltip: 'Station Description',
        widthPx: 400,
        rank: 1,
        value: 'value'
      },
      {
        type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
        ianApp: true,
        label: 'Description',
        tooltip: 'Station Description',
        widthPx: 400,
        rank: 2,
        value: 'value'
      }
    ];
    const itemsLeftight: DeprecatedToolbarTypes.ToolbarItem[] = [
      {
        type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
        ianApp: true,
        tooltip: 'Station Description',
        widthPx: 400,
        rank: 3,
        value: 'value'
      },
      {
        type: DeprecatedToolbarTypes.ToolbarItemType.LabelValue,
        ianApp: true,
        label: 'Description',
        tooltip: 'Station Description',
        widthPx: 400,
        rank: 4,
        value: 'value'
      }
    ];
    const rightIndicesToOverflow: number[] = [0, 1];
    const leftIndicesToOverflow: number[] = [0, 1];
    const redneredItem = getOverflowMenuItems(
      itemsRight,
      itemsLeftight,
      rightIndicesToOverflow,
      leftIndicesToOverflow
    );
    expect(redneredItem).toMatchSnapshot();
  });
});
