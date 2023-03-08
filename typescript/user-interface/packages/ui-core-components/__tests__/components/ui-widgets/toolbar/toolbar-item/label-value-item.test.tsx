import { render } from '@testing-library/react';
import React from 'react';

import { LabelValueToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/label-value-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('LabelValueToolbarItem', () => {
  test('LabelValueToolbarItem renders directly', () => {
    const { container } = render(
      <LabelValueToolbarItem
        key="labelvalue"
        labelValue=""
        tooltip="Hello label"
        label="label"
        menuLabel="menu label"
      />
    );

    expect(container).toMatchSnapshot();
  });

  test('LabelValueToolbarItem renders with issue and tooltip', () => {
    const { container } = render(
      <LabelValueToolbarItem
        key="labelvalue"
        labelValue=""
        tooltip="Hello label"
        label="label"
        menuLabel="menu label"
        hasIssue
        tooltipForIssue="Issue tooltip"
      />
    );

    expect(container).toMatchSnapshot();
  });
});
