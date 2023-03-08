import { render } from '@testing-library/react';
import React from 'react';

import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('LoadingSpinnerToolbarItem', () => {
  test('LoadingSpinnerToolbarItem renders directly', () => {
    const { container } = render(
      <LoadingSpinnerToolbarItem
        key="loadingspinner"
        itemsToLoad={1}
        tooltip="Hello LoadingSpinner"
        hasIssue={false}
      />
    );

    expect(container).toMatchSnapshot();
  });

  test('LoadingSpinnerToolbarItem renders with icon only', () => {
    const { container } = render(
      <LoadingSpinnerToolbarItem
        key="loadingspinner"
        itemsToLoad={1}
        tooltip="Hello LoadingSpinner"
        hasIssue={false}
        onlyShowIcon
      />
    );

    expect(container).toMatchSnapshot();
  });
});
