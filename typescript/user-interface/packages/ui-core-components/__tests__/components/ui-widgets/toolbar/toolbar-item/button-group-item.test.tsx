import { IconNames } from '@blueprintjs/icons';
import { render } from '@testing-library/react';
import React from 'react';

import { ButtonGroupToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/button-group-item';
import type { ButtonToolbarItemProps } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/button-item';
// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const buttonProps: ButtonToolbarItemProps = {
  key: 'buttonitem1',
  disabled: false,
  label: 'Pan Left',
  tooltip: 'Pan waveforms to the left',
  icon: IconNames.ARROW_LEFT,
  onlyShowIcon: true,
  onButtonClick: () => jest.fn()
};

describe('ButtonGroupToolbarItem', () => {
  test('ButtonGroupToolbarItem renders directly', () => {
    const { container } = render(
      <ButtonGroupToolbarItem
        key="buttongroup"
        buttons={[buttonProps]}
        tooltip="Button Group Hello"
        hasIssue={false}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
