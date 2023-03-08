/* eslint-disable react/function-component-definition */
/* eslint-disable @typescript-eslint/no-floating-promises */
/* eslint-disable react/display-name */
import { renderHook } from '@testing-library/react-hooks';
import React from 'react';
import { Provider } from 'react-redux';
import renderer from 'react-test-renderer';

import { workflowActions } from '../../../src/ts/app';
import {
  useFilterCycle,
  usePreferredFilterListForActivity,
  useSelectedFilterList,
  useSetFilterList
} from '../../../src/ts/app/hooks/filter-hooks';
import { analystActions } from '../../../src/ts/app/state';
import { getStore } from '../../../src/ts/app/store';
import { testFilterList } from '../../filter-list-data';

const mockDispatch = jest.fn();
jest.mock('../../../src/ts/app/hooks/react-redux-hooks', () => {
  const actual = jest.requireActual('../../../src/ts/app/hooks/react-redux-hooks');
  return {
    ...actual,
    useAppDispatch: () => mockDispatch
  };
});

jest.mock('../../../src/ts/app/api/signal-enhancement-configuration/selectors', () => {
  return {
    selectFilterLists: jest.fn().mockReturnValue([
      {
        defaultFilterIndex: 0,
        name: 'test',
        filters: [
          {
            withinHotKeyCycle: true,
            unfiltered: true,
            namedFilter: null,
            filterDefinition: null
          },
          {
            withinHotKeyCycle: false,
            unfiltered: null,
            namedFilter: 'Test Filter should appear in snapshot',
            filterDefinition: null
          }
        ],
        description: 'foo'
      }
    ])
  };
});

jest.mock(
  '../../../src/ts/app/api/signal-enhancement-configuration/signal-enhancement-api-slice',
  () => {
    const actual = jest.requireActual(
      '../../../src/ts/app/api/signal-enhancement-configuration/signal-enhancement-api-slice'
    );
    return {
      ...actual,
      useGetFilterListsDefinitionQuery: jest.fn().mockReturnValue({
        data: {
          preferredFilterListByActivity: [
            { name: 'test', workflowDefinitionId: { name: 'test-open-interval' } }
          ],
          filterLists: [
            {
              defaultFilterIndex: 0,
              name: 'test',
              filters: [
                {
                  withinHotKeyCycle: true,
                  unfiltered: true,
                  namedFilter: null,
                  filterDefinition: null
                },
                {
                  withinHotKeyCycle: false,
                  unfiltered: null,
                  namedFilter: 'Test Filter should appear in snapshot',
                  filterDefinition: null
                },
                {
                  withinHotKeyCycle: true,
                  unfiltered: null,
                  namedFilter: 'Test Filter should appear in snapshot',
                  filterDefinition: null
                }
              ],
              description: 'foo'
            }
          ]
        }
      })
    };
  }
);

describe('Filter Hooks', () => {
  describe('useSetFilterList', () => {
    it('gives us a set filter list function that can handle an object', () => {
      const store = getStore();
      const TestComponent: React.FC = () => {
        const setFilterList = useSetFilterList();
        expect(typeof setFilterList).toBe('function');
        setFilterList(testFilterList);
        return null;
      };
      renderer.create(
        <Provider store={store}>
          <TestComponent />
        </Provider>
      );

      expect(mockDispatch.mock.calls[0][0]).toMatchObject(
        analystActions.setSelectedFilterList(testFilterList.name)
      );
    });
    it('gives us a set filter list function that can handle a string', () => {
      const TestComponent: React.FC = () => {
        const setFilterList = useSetFilterList();
        expect(typeof setFilterList).toBe('function');
        setFilterList(testFilterList.name);
        return null;
      };
      renderer.create(
        <Provider store={getStore()}>
          <TestComponent />
        </Provider>
      );

      expect(mockDispatch.mock.calls[0][0]).toMatchObject(
        analystActions.setSelectedFilterList(testFilterList.name)
      );
    });
  });

  describe('usePreferredFilterListForActivity', () => {
    const TestComponent: React.FC = () => {
      const preferredFilterList = usePreferredFilterListForActivity();
      // eslint-disable-next-line react/jsx-no-useless-fragment
      return <>{preferredFilterList}</>;
    };
    it('calls the query and gets a result', () => {
      const store = getStore();
      const tree = renderer.create(
        <Provider store={store}>
          <TestComponent />
        </Provider>
      );
      expect(tree.toJSON()).toMatchSnapshot();
    });

    it('returns the preferred list with the correct data', () => {
      const store = getStore();
      store.dispatch(workflowActions.setOpenIntervalName('test-open-interval'));
      const tree = renderer.create(
        <Provider store={store}>
          <TestComponent />
        </Provider>
      );
      expect(tree.toJSON()).toMatchSnapshot();
    });
  });

  describe('useSelectedFilterList', () => {
    const TestComponent: React.FC = () => {
      const selectedFilterList = useSelectedFilterList();
      return <>{JSON.stringify(selectedFilterList)}</>;
    };

    it('returns null with initial state', () => {
      const store = getStore();
      const tree = renderer.create(
        <Provider store={store}>
          <TestComponent />
        </Provider>
      );
      expect(tree.toJSON()).toMatchSnapshot();
    });

    it('returns filter list from state', () => {
      const store = getStore();
      store.dispatch(analystActions.setSelectedFilterList('test'));
      const tree = renderer.create(
        <Provider store={store}>
          <TestComponent />
        </Provider>
      );
      expect(tree.toJSON()).toMatchSnapshot();
    });
  });
  describe('useFilterCycle', () => {
    const store = getStore();
    const { result } = renderHook(() => useFilterCycle(), {
      wrapper: (props: React.PropsWithChildren<unknown>) => (
        // eslint-disable-next-line react/destructuring-assignment
        <Provider store={store}>{props.children}</Provider>
      )
    });
    it('creates two functions', () => {
      expect(result.current?.selectNextFilter).toBeDefined();
      expect(result.current?.selectPreviousFilter).toBeDefined();
    });
    describe('selectNextFilter', () => {
      result.current.selectNextFilter();
      it('does not dispatch if no selectedFilterIndex is set', () => {
        mockDispatch.mockClear();
        expect(mockDispatch).not.toHaveBeenCalled();
      });
      it('does not dispatch if no hotkeyCycle is set', () => {
        // set this to make sure it hits the hotkey cycle condition
        mockDispatch.mockClear();
        store.dispatch(analystActions.setSelectedFilterIndex(0));
        renderHook(() => useFilterCycle(), {
          wrapper: (props: React.PropsWithChildren<unknown>) => (
            // eslint-disable-next-line react/destructuring-assignment
            <Provider store={store}>{props.children}</Provider>
          )
        });
        expect(mockDispatch).not.toHaveBeenCalled();
      });
      store.dispatch(analystActions.setSelectedFilterIndex(0));
      store.dispatch(analystActions.setSelectedFilterList('test'));
      store.dispatch(
        analystActions.setFilterHotkeyCycleOverridesForCurrentFilterList({
          0: false,
          1: true,
          2: true
        })
      );
      let renderedHook = renderHook(() => useFilterCycle(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          // eslint-disable-next-line react/destructuring-assignment
          <Provider store={store}>{props.children}</Provider>
        )
      });
      it('calls dispatch if hotkeyCycle and selectedFilterIndex are set', () => {
        store.dispatch(analystActions.setSelectedFilterIndex(0));
        store.dispatch(
          analystActions.setFilterHotkeyCycleOverridesForCurrentFilterList({
            0: false,
            1: true,
            2: true
          })
        );
        renderedHook = renderHook(() => useFilterCycle(), {
          wrapper: (props: React.PropsWithChildren<unknown>) => (
            // eslint-disable-next-line react/destructuring-assignment
            <Provider store={store}>{props.children}</Provider>
          )
        });
        renderedHook.result.current.selectNextFilter();
        expect(mockDispatch).toHaveBeenCalledWith({
          payload: 1,
          type: 'analyst/setSelectedFilterIndex'
        });
      });
    });
    describe('selectPreviousFilter', () => {
      store.dispatch(analystActions.setSelectedFilterIndex(2));
      store.dispatch(analystActions.setSelectedFilterList('test'));
      store.dispatch(
        analystActions.setFilterHotkeyCycleOverridesForCurrentFilterList({
          0: true,
          1: false,
          2: true
        })
      );
      mockDispatch.mockClear();
      const renderedHook = renderHook(() => useFilterCycle(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          // eslint-disable-next-line react/destructuring-assignment
          <Provider store={store}>{props.children}</Provider>
        )
      });

      it('calls dispatch if hotkeyCycle and selectedFilterIndex are set', () => {
        renderedHook.result.current.selectPreviousFilter();
        expect(mockDispatch).toHaveBeenCalledWith({
          payload: 0,
          type: 'analyst/setSelectedFilterIndex'
        });
      });
    });
    describe('selectUnfiltered', () => {
      store.dispatch(analystActions.setSelectedFilterIndex(2));
      store.dispatch(analystActions.setSelectedFilterList('test'));
      store.dispatch(
        analystActions.setFilterHotkeyCycleOverridesForCurrentFilterList({
          0: true,
          1: false,
          2: true
        })
      );
      mockDispatch.mockClear();
      const renderedHook = renderHook(() => useFilterCycle(), {
        wrapper: (props: React.PropsWithChildren<unknown>) => (
          // eslint-disable-next-line react/destructuring-assignment
          <Provider store={store}>{props.children}</Provider>
        )
      });

      it('calls dispatch to select the unfiltered option if hotkeyCycle and selectedFilterIndex are set', () => {
        renderedHook.result.current.selectUnfiltered();
        expect(mockDispatch).toHaveBeenCalledWith({
          payload: 0, // the index of the unfiltered option
          type: 'analyst/setSelectedFilterIndex'
        });
      });
    });
  });
});
