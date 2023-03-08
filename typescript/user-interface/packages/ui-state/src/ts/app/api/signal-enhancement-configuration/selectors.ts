import type { FilterListTypes } from '@gms/common-model';
import { Logger } from '@gms/common-util';
import { createSelector } from '@reduxjs/toolkit';

import type { AppState } from '../../store';
import { selectOpenActivityNames } from '../workflow';
import { signalEnhancementConfigurationApiSlice } from './signal-enhancement-api-slice';

export const logger = Logger.create(
  'GMS_LOG_SIGNAL_ENHANCEMENT_SELECTORS',
  process.env.GMS_LOG_SIGNAL_ENHANCEMENT_SELECTORS
);

/**
 * A selector for the filter lists from the getFilterListsDefinition query.
 *
 * @example const fls = useAppState(selectFilterLists);
 *
 * @param state the redux app state
 * @returns the list of all filter lists returned by the getFilterListsDefinition query in @see signalEnhancementConfigurationApiSlice
 */
export const selectFilterLists = (state: AppState): FilterListTypes.FilterList[] =>
  signalEnhancementConfigurationApiSlice.endpoints.getFilterListsDefinition.select()(state).data
    ?.filterLists;

/**
 * A selector for the preferred filter lists by activity from the getFilterListsDefinition query
 *
 * @example const fls = useAppState(selectPreferredFilterListsByActivity);
 * @param state the redux app state
 * @returns the list of preferred filter lists by activity, as returned by the getFilterListsDefinition query in @see signalEnhancementConfigurationApiSlice
 */
export const selectPreferredFilterListsByActivity = (
  state: AppState
): FilterListTypes.FilterListActivity[] =>
  signalEnhancementConfigurationApiSlice.endpoints.getFilterListsDefinition.select()(state).data
    ?.preferredFilterListByActivity;

/**
 * Creates a referentially stable, derived result from the two selectors: the query results from @see selectPreferredFilterListsByActivity
 * and the currently open interval's name. Note that the result of this function is memoized. @see https://redux-toolkit.js.org/api/createSelector
 * ! We have to type the returned function. Type inference gets confused due to complex CombinedState types inferred from the app state.
 *
 * @returns the preferred filter list based on the currently open activity, or activities.
 * In the case of multiple activities, prefers those with lower indices.
 * */
export const selectPreferredFilterListForActivity: (
  state: AppState
) => string | undefined = createSelector(
  [selectPreferredFilterListsByActivity, selectOpenActivityNames],
  (preferredFilterListByActivity, openActivityNames) => {
    const activity = openActivityNames?.find(aName =>
      preferredFilterListByActivity?.find(pf => pf.workflowDefinitionId.name === aName)
    );
    if (!activity) {
      logger.error(
        `No preferred filter list found for activities ${JSON.stringify(openActivityNames)}`
      );
    }
    return activity
      ? preferredFilterListByActivity?.find(pf => pf.workflowDefinitionId.name === activity)?.name
      : undefined;
  }
);
