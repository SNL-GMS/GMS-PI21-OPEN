import { render } from '@testing-library/react';
import React from 'react';

import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { NumericInputToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/numeric-input-item';
import { NumericOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/numeric-input-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const numericInputToolbarItem = (
  <NumericInputToolbarItem
    key="numericinput"
    numericValue={42}
    minMax={{
      max: 43,
      min: 41
    }}
    onChange={jest.fn()}
    tooltip="Hello Numeric"
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

describe('NumericOverflowMenuToolbarItem', () => {
  test('NumericOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <NumericOverflowMenuToolbarItem
        item={numericInputToolbarItem.props}
        menuKey={numericInputToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('NumericOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <NumericOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
