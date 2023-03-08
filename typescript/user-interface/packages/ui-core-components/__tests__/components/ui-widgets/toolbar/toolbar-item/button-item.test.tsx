import { render } from '@testing-library/react';
import React from 'react';

import { ButtonToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/button-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const mockOnClick = jest.fn();
const mockOnMouseEnter = jest.fn();
const mockOnMouseOut = jest.fn();

describe('ButtonToolbarItem', () => {
  test('ButtonToolbarItem renders', () => {
    const { container } = render(
      <ButtonToolbarItem
        key="button"
        onButtonClick={mockOnClick}
        onMouseEnter={mockOnMouseEnter}
        onMouseOut={mockOnMouseOut}
        tooltip="Button Hello"
        hasIssue={false}
      />
    );

    expect(container).toMatchSnapshot();
  });

  test('ButtonToolbarItem renders with marginRight and labelRight', () => {
    const { container } = render(
      <ButtonToolbarItem
        key="button"
        onButtonClick={mockOnClick}
        onMouseEnter={mockOnMouseEnter}
        onMouseOut={mockOnMouseOut}
        tooltip="Button Hello"
        hasIssue={false}
        marginRight={5}
        labelRight="Button Right Label"
      />
    );

    expect(container).toMatchSnapshot();
  });
});
