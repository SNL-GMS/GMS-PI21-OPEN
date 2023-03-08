/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { SaveOpenDialog } from '../../../src/ts/components/dialog/save-open-dialog';
import type * as SaveOpenDialogTypes from '../../../src/ts/components/dialog/types';
import { waitForComponentToPaint } from '../../util/test-util';

const saveableItem1: SaveOpenDialogTypes.SaveableItem = {
  title: 'title',
  id: '1'
};

const saveableItem2: SaveOpenDialogTypes.SaveableItem = {
  title: 'title',
  id: '2'
};

const props: SaveOpenDialogTypes.SaveOpenDialogProps = {
  title: 'string',
  actionText: 'string',
  itemList: [saveableItem1, saveableItem2],
  actionTooltipText: 'string',
  isDialogOpen: false,
  titleOfItemList: 'string',
  cancelText: 'string',
  cancelTooltipText: 'string',
  selectedId: 'string',
  openedItemId: 'string',
  defaultId: '1',
  defaultSaveName: 'string',
  actionCallback: jest.fn(),
  cancelCallback: jest.fn(),
  selectEntryCallback: jest.fn()
};

describe('SaveOpenDialog', () => {
  it('to be defined', () => {
    expect(SaveOpenDialog).toBeDefined();
  });

  it('Save Open Dialog renders', () => {
    const { container } = render(<SaveOpenDialog {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('Save Open Dialog functions and clicks', async () => {
    const mockActionCallback = jest.fn();
    const mockCancelCallback = jest.fn();
    const mockSelectEntryCallback = jest.fn();

    const wrapper3 = Enzyme.mount(
      <SaveOpenDialog
        title="string"
        actionText="string"
        itemList={[saveableItem1, saveableItem2]}
        actionTooltipText="string"
        isDialogOpen={false}
        titleOfItemList="string"
        cancelText="string"
        cancelTooltipText="string"
        selectedId="string"
        openedItemId="string"
        defaultId="string"
        defaultSaveName="string"
        actionCallback={mockActionCallback}
        cancelCallback={mockCancelCallback}
        selectEntryCallback={mockSelectEntryCallback}
      />
    );

    wrapper3.setProps({ isDialogOpen: true });
    await waitForComponentToPaint(wrapper3);

    wrapper3.setProps({ isDialogOpen: true });
    await waitForComponentToPaint(wrapper3);

    expect(wrapper3.find('Button')).toBeDefined();
    wrapper3.find('button').last().simulate('click');

    wrapper3.setProps({ isDialogOpen: true });
    await waitForComponentToPaint(wrapper3);
    expect(wrapper3.find('button')).toBeDefined();

    wrapper3.find('button').first().simulate('click');
    await waitForComponentToPaint(wrapper3);

    wrapper3.setProps({ isDialogOpen: true });
    await waitForComponentToPaint(wrapper3);

    wrapper3.find('.dialog__entry').last().simulate('click');
    await waitForComponentToPaint(wrapper3);

    expect(mockActionCallback).toHaveBeenCalledTimes(0);
    expect(mockCancelCallback).toHaveBeenCalled();
    expect(mockSelectEntryCallback).toHaveBeenCalled();
  });
});
