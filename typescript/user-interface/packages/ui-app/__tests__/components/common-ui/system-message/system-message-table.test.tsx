import { uuid } from '@gms/common-util';
import noop from 'lodash/noop';
import * as React from 'react';

import { SystemMessageTable } from '../../../../src/ts/components/common-ui/components/system-message/system-message-table';
import { buildDefaultSeverityFilterMap } from '../../../../src/ts/components/common-ui/components/system-message/toolbar/severity-filters';
import type { SystemMessageTableProps } from '../../../../src/ts/components/common-ui/components/system-message/types';
import { systemMessages } from './shared-data';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

let idCount = 0;
// eslint-disable-next-line no-plusplus
uuid.asString = jest.fn().mockImplementation(() => ++idCount);

describe('System Message Table', () => {
  it('should be defined', () => {
    expect(SystemMessageTable).toBeDefined();
  });

  const severityFilterMap = buildDefaultSeverityFilterMap();

  const getTimeColumnApiFunction = () => ({
    getColumnState: jest.fn(() => [
      {
        colId: 'time',
        sort: 'asc'
      }
    ])
  });

  const props: SystemMessageTableProps = {
    addSystemMessages: jest.fn(),
    clearAllSystemMessages: jest.fn(),
    isAutoScrollingEnabled: true,
    setIsAutoScrollingEnabled: jest.fn(),
    systemMessages,
    severityFilterMap
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  const table = Enzyme.shallow(<SystemMessageTable {...props} />);
  it('should match a snapshot', () => {
    expect(table).toMatchSnapshot();
  });

  const instance = table.instance();

  instance.state = { hasUnseenMessages: true };

  it('should not sort if props change if auto scrolling is enabled and sorted by time', () => {
    instance.forceUpdate = jest.fn();
    instance.table = {
      getColumnApi: getTimeColumnApiFunction,
      getTableApi: () => ({
        ensureIndexVisible: jest.fn(() => true),
        refreshInfiniteCache: jest.fn()
      })
    };

    instance.sortByTime = jest.fn();
    instance.componentDidUpdate(
      {
        isAutoScrollingEnabled: false
      },
      {
        systemMessages
      }
    );
    clearTimeout(instance.autoScrollTimer);
    expect(instance.sortByTime).not.toHaveBeenCalled();
  });

  it('should sort if props change if auto scrolling is enabled', () => {
    instance.table = {
      getColumnApi: () => ({
        getColumnState: jest.fn(() => [
          {
            colId: 'message',
            sort: 'asc'
          }
        ])
      }),
      getTableApi: () => ({
        ensureIndexVisible: jest.fn(() => true),
        refreshInfiniteCache: jest.fn()
      })
    };
    instance.sortByTime = jest.fn();
    instance.componentDidUpdate(
      {
        isAutoScrollingEnabled: false
      },
      {
        systemMessages
      }
    );
    expect(instance.sortByTime).toHaveBeenCalled();
  });

  it('can tell if it has scrolled to the end', () => {
    instance.getScrollContainer = () => ({
      scrollTop: 100, // how far from the top we have scrolled
      scrollHeight: 1000, // simulated height of whole container
      clientHeight: 900 // simulated amount visible
    });
    expect(instance.isScrolledToBottom()).toBeFalsy();
  });

  it('can tell if it has NOT scrolled to the end', () => {
    instance.getScrollContainer = () => ({
      scrollTop: 0, // how far from the top we have scrolled
      scrollHeight: 1000, // simulated height of whole container
      clientHeight: 900 // simulated amount visible
    });
    expect(instance.isScrolledToBottom()).toBeFalsy();
  });

  it('gets the right subcategory values', () => {
    const params = {
      data: {
        id: systemMessages[0].id
      }
    };
    const result = instance.subcategoryValueGetter(params);
    expect(result).toEqual(systemMessages[0].subCategory);
  });

  it('gets the right severity values', () => {
    const params = {
      data: {
        id: systemMessages[0].id
      }
    };
    const result = instance.severityValueGetter(params);
    expect(result).toEqual(systemMessages[0].severity);
  });

  it('gets the right message values', () => {
    const params = {
      data: {
        id: systemMessages[0].id
      }
    };
    const result = instance.messageValueGetter(params);
    expect(result).toEqual(systemMessages[0].message);
  });

  it('can checked if scrolled to start', () => {
    const result = instance.isScrolledToTop();
    expect(result).toBeTruthy();
  });
  it('can handle sort changed', () => {
    instance.onSortChanged();
    expect(instance.props.setIsAutoScrollingEnabled).toHaveBeenCalledTimes(1);
  });

  it('can tell scroll direction down', () => {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    instance.state.prevScrollPositions = [0, 10];
    expect(instance.getScrollDirection()).toEqual('down');
  });

  it('can tell scroll direction up', () => {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    instance.state.prevScrollPositions = [Number(instance.SCROLL_UP_THRESHOLD_PX) + 1, 0];
    expect(instance.getScrollDirection()).toEqual('up');
  });

  it('can tell scroll direction unchanged', () => {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    instance.state.prevScrollPositions = [10, 10];
    expect(instance.getScrollDirection()).toEqual('unchanged');
  });

  it('can use new messages indicator', () => {
    instance.table = {
      getColumnApi: getTimeColumnApiFunction,
      getTableApi: () => ({
        ensureIndexVisible: jest.fn(() => true)
      })
    };
    instance.onNewMessageIndicatorClick();
    expect(instance.props.setIsAutoScrollingEnabled).toHaveBeenCalledWith(true);
  });

  it('can use on body scroll', () => {
    jest.useFakeTimers();
    instance.props.setIsAutoScrollingEnabled(true);
    instance.getScrollDirection = jest.fn().mockImplementation(() => 'up');
    instance.onBodyScroll();
    jest.runAllTimers();
    expect(instance.getScrollDirection).toHaveBeenCalled();
    expect(instance.props.setIsAutoScrollingEnabled).toHaveBeenLastCalledWith(false);
  });

  it('can scroll to latest', () => {
    jest.useFakeTimers();
    jest.spyOn(global, 'setTimeout');
    instance.scrollToLatest();
    expect(setTimeout).toHaveBeenCalledTimes(1);
  });

  // TODO Unskip tests
  it.skip('can attempt to scroll if rows are temporarily unavailable', () => {
    jest.useFakeTimers();
    jest.spyOn(global, 'setTimeout');
    instance.timeout();
    expect(setTimeout).toHaveBeenCalledTimes(1);
  });

  // TODO Unskip tests
  it.skip('will not attempt to scroll if rows are temporarily unavailable if timeout retries is zero', () => {
    jest.useFakeTimers();
    jest.spyOn(global, 'setTimeout');
    instance.timeout();
    expect(setTimeout).toHaveBeenCalledTimes(1);
    instance.SET_TIMEOUT_RETRIES = 0;
    jest.runOnlyPendingTimers();
    expect(setTimeout).toHaveBeenCalledTimes(1);
  });

  it('will not scroll to latest if there are no system messages', () => {
    const propsNoSystemMessages: SystemMessageTableProps = {
      addSystemMessages: jest.fn(),
      clearAllSystemMessages: jest.fn(),
      isAutoScrollingEnabled: true,
      setIsAutoScrollingEnabled: jest.fn(),
      systemMessages: undefined,
      severityFilterMap
    };

    // eslint-disable-next-line react/jsx-props-no-spreading
    const tableNoSystemMessages = Enzyme.mount(<SystemMessageTable {...propsNoSystemMessages} />);
    const instanceNoSystemMessages = tableNoSystemMessages.instance();
    jest.clearAllMocks();
    jest.useFakeTimers();
    jest.spyOn(global, 'setTimeout');
    instanceNoSystemMessages.scrollToLatest();
    expect(setTimeout).not.toHaveBeenCalled();
  });

  it('will not attempt to scroll if auto scrolling is not enabled', () => {
    const propsAutoScrollingDisabled: SystemMessageTableProps = {
      addSystemMessages: jest.fn(),
      clearAllSystemMessages: jest.fn(),
      isAutoScrollingEnabled: false,
      setIsAutoScrollingEnabled: jest.fn(),
      systemMessages: undefined,
      severityFilterMap
    };

    const tableAutoScrollingDisabled = Enzyme.mount(
      // eslint-disable-next-line react/jsx-props-no-spreading
      <SystemMessageTable {...propsAutoScrollingDisabled} />
    );
    const instanceAutoScrollingDisabled = tableAutoScrollingDisabled.instance();
    jest.clearAllMocks();
    jest.useFakeTimers();
    jest.spyOn(global, 'setTimeout');
    instanceAutoScrollingDisabled.timeout();
    expect(setTimeout).not.toHaveBeenCalled();
  });

  it('can create an empty datasource', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const table1 = Enzyme.mount(<SystemMessageTable {...props} />);
    const instance1 = table1.instance();
    const emptyDataSource = instance1.table.getEmptyDataSource();
    expect(emptyDataSource).toBeDefined();
  });

  it('can be destroyed', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const table2 = Enzyme.mount(<SystemMessageTable {...props} />);
    const instance2 = table2.instance();
    instance2.table.coreTableRef.getTableApi = () => ({
      purgeInfiniteCache: jest.fn(),
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      setDatasource: jest.fn(dataSource => noop)
    });
    instance2.table.destroy();
    expect(instance2.table.coreTableRef).toBeUndefined();
  });
});
