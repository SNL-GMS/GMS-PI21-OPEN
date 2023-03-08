import type { FilterListTypes } from '@gms/common-model';
import React from 'react';
import { batch } from 'react-redux';

import { useGetFilterListsDefinitionQuery } from '../api';
import {
  analystActions,
  selectHotkeyCycle,
  selectSelectedFilterIndex,
  selectSelectedFilterList
} from '../state';
import type { HotkeyCycleList } from '../state/analyst/types';
import { useAppDispatch, useAppSelector } from './react-redux-hooks';

/**
 * @returns a setter function that dispatches an update to the redux store, updating the filter list.
 */
export const useSetFilterList = (): ((fl: FilterListTypes.FilterList | string) => void) => {
  const dispatch = useAppDispatch();
  const filterQuery = useGetFilterListsDefinitionQuery();
  const filterLists = filterQuery.data?.filterLists;
  return React.useCallback(
    (fl: FilterListTypes.FilterList | string) => {
      batch(() => {
        let filterList;
        if (typeof fl === 'string') {
          filterList = filterLists.find(f => f.name === fl);
          if (!filterList) {
            throw new Error(`Filter list ${fl} not found`);
          }
        } else {
          filterList = fl;
        }
        dispatch(analystActions.setSelectedFilterList(filterList.name));
        dispatch(analystActions.setSelectedFilterIndex(filterList.defaultFilterIndex));
      });
    },
    [dispatch, filterLists]
  );
};

/**
 * @returns the name of the preferred filter list for the currently open activity (interval)
 */
export const usePreferredFilterListForActivity = (): string => {
  const filterListQuery = useGetFilterListsDefinitionQuery();
  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
  const preferredFilterList = filterListQuery.data?.preferredFilterListByActivity.find(
    pf => pf.workflowDefinitionId.name === openIntervalName
  );
  return preferredFilterList?.name;
};

/**
 * @returns the selected filter list, derived from the selected filter name and the filter lists from the signal-enhancement query
 * If no filter list is selected, will update the redux store to select the default filter list, and return that.
 */
export const useSelectedFilterList = (): FilterListTypes.FilterList => {
  const filterListQuery = useGetFilterListsDefinitionQuery();
  const result = useAppSelector(selectSelectedFilterList);
  const dispatch = useAppDispatch();
  const preferred = usePreferredFilterListForActivity();
  React.useEffect(() => {
    // select the preferred filter list if none was already selected
    if (!result) {
      dispatch(analystActions.setSelectedFilterList(preferred));
    }
  }, [dispatch, preferred, result]);
  if (!result && filterListQuery.data) {
    return filterListQuery.data.filterLists.find(fl => fl.name === preferred);
  }
  return result;
};

/**
 * @example
 * const { selectedFilter, setSelectedFilter } = useSelectedFilter();
 *
 * @returns an object containing the selected filer, and a setter function. The setter
 * function takes either a string (the filter name) or a filter, or null to unset the selection.
 *
 * All elements returned should be referentially stable, so they may be checked for
 * shallow equality in dependency arrays and memoization functions.
 */
export const useSelectedFilter = (): {
  selectedFilter: FilterListTypes.Filter;
  setSelectedFilter: (selectedFilter: FilterListTypes.Filter | null) => void;
} => {
  // initiate the subscription to the query data. selectSelectedFilterList will get the data that this stores.
  useGetFilterListsDefinitionQuery();
  const dispatch = useAppDispatch();
  const selectedFilterList = useAppSelector(selectSelectedFilterList);
  const selectedFilterIndex = useAppSelector(selectSelectedFilterIndex);
  return {
    selectedFilter: selectedFilterList.filters[selectedFilterIndex],
    setSelectedFilter: React.useCallback(
      (selected: FilterListTypes.Filter | null) => {
        const indexOfFilter = selectedFilterList.filters.findIndex(fl => fl === selected);
        dispatch(analystActions.setSelectedFilterIndex(indexOfFilter));
      },
      [dispatch, selectedFilterList.filters]
    )
  };
};

/**
 * @returns an object containing the HotkeyCycleList (which maps indices to whether a filter
 * is in the hotkey cycle), and a setter to set whether a filter at a given index is within that list.
 */
export const useHotkeyCycle = (): {
  hotkeyCycle: HotkeyCycleList;
  setIsFilterWithinHotkeyCycle: (index: number, isWithinCycle: boolean) => void;
} => {
  const hotkeyCycle = useAppSelector(selectHotkeyCycle);
  const dispatch = useAppDispatch();
  return {
    hotkeyCycle,
    setIsFilterWithinHotkeyCycle: (index, isWithinCycle) =>
      dispatch(analystActions.setIsFilterWithinHotkeyCycle({ index, isWithinCycle }))
  };
};

/**
 * @returns two functions, one to select the next filter, and one to select the previous filter.
 */
export const useFilterCycle = (): {
  selectNextFilter: () => void;
  selectPreviousFilter: () => void;
  selectUnfiltered: () => void;
} => {
  const selectedFilterIndex = useAppSelector(state => state.app.analyst.selectedFilterIndex);
  const { hotkeyCycle } = useHotkeyCycle();
  const dispatch = useAppDispatch();
  const filterList = useSelectedFilterList();
  const selectNextFilter = React.useCallback(() => {
    if (selectedFilterIndex == null || !hotkeyCycle?.length) {
      return;
    }
    let i = selectedFilterIndex + 1 < hotkeyCycle.length ? selectedFilterIndex + 1 : 0;
    while (!hotkeyCycle[i] && i !== selectedFilterIndex) {
      i += 1;
      if (i >= hotkeyCycle.length) {
        i = 0;
      }
    }
    dispatch(analystActions.setSelectedFilterIndex(i));
  }, [dispatch, hotkeyCycle, selectedFilterIndex]);
  const selectPreviousFilter = React.useCallback(() => {
    if (selectedFilterIndex == null || !hotkeyCycle?.length) {
      return;
    }
    let i = selectedFilterIndex - 1 >= 0 ? selectedFilterIndex - 1 : hotkeyCycle.length - 1;
    while (!hotkeyCycle[i] && i !== selectedFilterIndex) {
      i -= 1;
      if (i < 0) {
        i = hotkeyCycle.length - 1;
      }
    }
    dispatch(analystActions.setSelectedFilterIndex(i));
  }, [dispatch, hotkeyCycle, selectedFilterIndex]);
  const selectUnfiltered = React.useCallback(() => {
    if (filterList?.filters == null) {
      return;
    }
    const unfilteredIndex = filterList.filters.findIndex(f => f.unfiltered);
    dispatch(analystActions.setSelectedFilterIndex(unfilteredIndex));
  }, [dispatch, filterList?.filters]);
  return {
    selectNextFilter,
    selectPreviousFilter,
    selectUnfiltered
  };
};
