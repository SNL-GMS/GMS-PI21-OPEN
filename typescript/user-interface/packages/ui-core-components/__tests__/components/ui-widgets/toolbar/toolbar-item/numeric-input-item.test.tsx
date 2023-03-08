import { render } from '@testing-library/react';
import React from 'react';

import { NumericInputToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/numeric-input-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('NumericInputToolbarItem', () => {
  test('NumericInputToolbarItem renders directly', () => {
    const { container } = render(
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
        label="Numeric label"
      />
    );

    expect(container).toMatchSnapshot();
  });

  test('NumericInputToolbarItem renders with labelRight', () => {
    const { container } = render(
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
        labelRight="Numeric label right"
      />
    );

    expect(container).toMatchSnapshot();
  });
});
