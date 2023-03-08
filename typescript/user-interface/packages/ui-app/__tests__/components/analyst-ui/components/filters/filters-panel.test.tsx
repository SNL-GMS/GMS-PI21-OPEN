/* eslint-disable @typescript-eslint/no-magic-numbers */
import { AnalysisMode } from '@gms/common-model/lib/workflow/types';
import { getStore, setOpenInterval } from '@gms/ui-state';
import { testFilterList } from '@gms/ui-state/__tests__/filter-list-data';
import React from 'react';
import { Provider } from 'react-redux';
import renderer from 'react-test-renderer';

import {
  FilterListNonIdealState,
  FiltersPanel
} from '../../../../../src/ts/components/analyst-ui/components/filters/filters-panel';

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');

  return {
    ...actual,
    useCurrentIntervalWithBuffer: jest.fn(() => ({
      startTimeSecs: 0,
      endTimeSecs: 1000
    })),
    useGetFilterListsDefinitionQuery: jest.fn(() => ({
      data: {
        filterLists: [testFilterList]
      }
    })),
    selectSelectedFilterList: jest.fn(() => testFilterList)
  };
});

describe('FiltersPanel', () => {
  describe('FilterListNonIdealState', () => {
    it('matches a snapshot if given an error', () => {
      const tree = renderer
        .create(
          <Provider store={getStore()}>
            <FilterListNonIdealState error="Error" filterLists={[testFilterList]}>
              <div>Here goes the filter list</div>
            </FilterListNonIdealState>
          </Provider>
        )
        .toJSON();
      expect(tree).toMatchSnapshot();
    });
    it('matches a snapshot if not given filter lists', () => {
      const tree = renderer
        .create(
          <Provider store={getStore()}>
            <FilterListNonIdealState error={undefined} filterLists={undefined}>
              <div>Here goes the filter list</div>
            </FilterListNonIdealState>
          </Provider>
        )
        .toJSON();
      expect(tree).toMatchSnapshot();
    });
    it('matches a snapshot if given an empty list of filter lists', () => {
      const tree = renderer
        .create(
          <Provider store={getStore()}>
            <FilterListNonIdealState error={undefined} filterLists={[]}>
              <div>Here goes the filter list</div>
            </FilterListNonIdealState>
          </Provider>
        )
        .toJSON();
      expect(tree).toMatchSnapshot();
    });
    it('matches a snapshot if given everything it needs', () => {
      const tree = renderer
        .create(
          <Provider store={getStore()}>
            <FilterListNonIdealState error={undefined} filterLists={[testFilterList]}>
              <div>Here goes the filter list</div>
            </FilterListNonIdealState>
          </Provider>
        )
        .toJSON();
      expect(tree).toMatchSnapshot();
    });
  });
  it('matches a snapshot', () => {
    const store = getStore();
    store.dispatch(
      setOpenInterval(
        { startTimeSecs: 0, endTimeSecs: 1000 },
        {
          effectiveAt: 0,
          name: 'ALL_1',
          description: 'testy'
        },
        'AL1 Event Review',
        ['AL1 Event Review'],
        AnalysisMode.EVENT_REVIEW
      )
    );
    const tree = renderer
      .create(
        <Provider store={store}>
          <FiltersPanel />
        </Provider>
      )
      .toJSON();
    expect(tree).toMatchSnapshot();
  });
});
