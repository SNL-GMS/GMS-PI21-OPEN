import { Position } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { render } from '@testing-library/react';
import React from 'react';

import type { SimpleCheckboxListProps } from '../../../../src/ts/components/ui-widgets/checkbox-list';
import { SimpleCheckboxList } from '../../../../src/ts/components/ui-widgets/checkbox-list';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('SimpleCheckboxList', () => {
  it('is exported and renders with empty checkBoxListEntries', () => {
    const simpleCheckboxList: SimpleCheckboxListProps = {
      checkBoxListEntries: []
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<SimpleCheckboxList {...simpleCheckboxList} />);
    expect(SimpleCheckboxList).toBeDefined();
    expect(container).toMatchSnapshot();
  });
  it('renders with checkBoxListEntries', () => {
    const simpleCheckboxList: SimpleCheckboxListProps = {
      checkBoxListEntries: [
        { name: 'Station 0', isChecked: true, headerTitle: 'Entry Title', divider: true },
        {
          name: 'Station 1',
          isChecked: false,
          iconName: IconNames.CIRCLE,
          iconColor: 'black',
          headerTitle: 'Icon Title',
          divider: true
        },
        {
          name: 'Station 2',
          isChecked: true,
          divider: true,
          headerTitle: 'Element Title',
          element: React.createElement('div')
        },
        {
          name: 'Station 3',
          isChecked: true,
          divider: true,
          headerTitle: 'Button Title',
          iconButton: {
            iconName: IconNames.COG,
            popover: {
              content: React.createElement('div'),
              position: Position.RIGHT_BOTTOM,
              usePortal: true,
              minimal: true
            },
            onClick: jest.fn(() => 'test')
          }
        },
        { name: 'Station 0', isChecked: true, headerTitle: 'Entry Title', divider: true },
        {
          name: 'Station 1',
          isChecked: false,
          iconName: IconNames.CIRCLE,
          iconColor: 'black'
        },
        {
          name: 'Station 2',
          isChecked: true,
          element: React.createElement('div')
        },
        {
          name: 'Station 3',
          isChecked: true,
          iconButton: {
            iconName: IconNames.COG
          }
        }
      ]
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<SimpleCheckboxList {...simpleCheckboxList} />);
    expect(container).toMatchSnapshot();
  });
});
