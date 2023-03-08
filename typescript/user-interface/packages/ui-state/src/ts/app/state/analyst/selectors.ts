import type { FilterListTypes } from '@gms/common-model';
import { createSelector } from '@reduxjs/toolkit';

import { selectFilterLists } from '../../api/signal-enhancement-configuration/selectors';
import type { AppState } from '../../store';
import type { HotkeyCycleList } from './types';

/**
 * @returns the selected filter list from the provided redux state
 */
export const selectSelectedFilterListName = (state: AppState): string =>
  state.app.analyst.selectedFilterList;

/**
 * A selector that derives the selected filter list from the signalEnhancementConfiguration query (filters)
 * and the currently selected filter list.
 *
 * @returns the selected filter list. Note that this is memoized by createSelector.
 * @see https://redux.js.org/usage/deriving-data-selectors
 */
export const selectSelectedFilterList: (
  state: AppState
) => FilterListTypes.FilterList = createSelector(
  [selectSelectedFilterListName, selectFilterLists],
  (selectedFLName, filterLists) =>
    filterLists != null ? filterLists.find(fl => fl.name === selectedFLName) : undefined
);

/**
 * a selector to get the selected filter out of the redux state.
 *
 * @example
 * const selectedFilterIndex = useAppSelector(selectSelectedFilterIndex);
 *
 * @param state the AppState
 * @returns the index of the selected filter, or null if there is no selection
 */
export const selectSelectedFilterIndex: (state: AppState) => number | null = state =>
  state.app.analyst.selectedFilterIndex;

/**
 * a selector to get the selected filter out of the redux state.
 *
 * @example
 * const selectedFilter = useAppSelector(selectSelectedFilter);
 *
 * @param state the AppState
 * @returns the index of the selected filter, or null if there is no selection
 */
export const selectSelectedFilter: (state: AppState) => FilterListTypes.Filter = createSelector(
  [selectSelectedFilterIndex, selectSelectedFilterList],
  (index: number, selectedFilterList: FilterListTypes.FilterList) => {
    return selectedFilterList[index];
  }
);

/**
 * @returns the record of records of user supplied overrides to the hotkey cycle, such that each key
 * is the index of a record of hotkeyCycle overrides
 * @see selectHotkeyCycle for an array of boolean values indicating which filters are in the cycle
 * @see selectHotkeyCycleOverrides for this FilterList's record of overrides
 */
export const selectAllHotkeyCycleOverrides: (
  state: AppState
) => Record<string, Record<number, boolean>> = state => state.app.analyst.hotkeyCycleOverrides;

/**
 * @returns the record of user supplied overrides to the hotkey cycle, such that each key
 * is the index of a filter, and it's boolean value represents the user-set overridden state.
 * This is intended primarily for internal use, and components that care about whether a filter
 * is within the hotkey cycle should use the @function selectHotkeyCycle.
 * @see selectHotkeyCycle for an array of boolean values indicating which filters are in the cycle
 * @see selectAllHotkeyCycleOverrides for a record of all overrides
 */
export const selectHotkeyCycleOverrides: (
  state: AppState
) => Record<number, boolean> = createSelector(
  selectSelectedFilterListName,
  selectAllHotkeyCycleOverrides,
  (filterListName, allHotkeyCycles) => allHotkeyCycles[filterListName]
);

/**
 * @returns a HotkeyCycleList that contains a boolean at the corresponding index of each filter within
 * the FilterList.filter ordered list.
 */
export const selectHotkeyCycle: (state: AppState) => HotkeyCycleList = createSelector(
  [selectSelectedFilterList, selectHotkeyCycleOverrides],
  (filterList: FilterListTypes.FilterList, hotkeyCycleOverrides: Record<number, boolean>) => {
    if (!filterList?.filters) {
      return [];
    }
    return filterList.filters.map((fl, index) =>
      hotkeyCycleOverrides != null && hotkeyCycleOverrides[index] != null
        ? hotkeyCycleOverrides[index]
        : fl.withinHotKeyCycle
    );
  }
);
