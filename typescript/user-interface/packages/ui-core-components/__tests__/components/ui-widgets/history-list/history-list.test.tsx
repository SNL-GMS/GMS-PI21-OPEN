/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { HistoryList } from '../../../../src/ts/components/ui-widgets/history-list';
import type * as HistoryListTypes from '../../../../src/ts/components/ui-widgets/history-list/types';

const historyListItem: HistoryListTypes.HistoryListItem = {
  index: 0,
  label: 'item',
  id: '1'
};

const props: HistoryListTypes.HistoryListProps = {
  items: [historyListItem],
  preferredItems: [historyListItem],
  listLength: 1,
  onSelect: jest.fn()
};

const props2: HistoryListTypes.HistoryListProps = {
  items: [historyListItem],
  preferredItems: [historyListItem],
  listLength: 1,
  onSelect: jest.fn()
};

describe('History List', () => {
  it('to be defined', () => {
    expect(HistoryList).toBeDefined();
  });

  it('History List renders', () => {
    const { container } = render(<HistoryList {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('History List renders with other props', () => {
    const { container } = render(<HistoryList {...props2} />);
    expect(container).toMatchSnapshot();
  });

  it('History List functions and clicks', () => {
    const wrapper3 = Enzyme.mount(<HistoryList {...props} />);
    const instance: HistoryList = wrapper3.find(HistoryList).instance() as HistoryList;

    const spy = jest.spyOn(instance, 'componentDidMount');
    instance.componentDidMount();
    expect(spy).toHaveBeenCalled();

    const input = wrapper3.find('HistoryList');
    const spy3 = jest.spyOn(instance, 'render');
    input.simulate('click');
    instance.render();
    expect(spy3).toHaveBeenCalled();
  });
});
