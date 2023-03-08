import type { LegacyEventTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';

import { selectPreferredFilterListForActivity } from '../../api/signal-enhancement-configuration/selectors';
import { signalEnhancementConfigurationApiSlice } from '../../api/signal-enhancement-configuration/signal-enhancement-api-slice';
import type { AppDispatch, AppState } from '../../store';
import { analystActions } from './analyst-slice';
import { selectSelectedFilterList, selectSelectedFilterListName } from './selectors';
import { WaveformDisplayMode, WaveformSortType } from './types';

const logger = UILogger.create('[UI State Manager - Analyst]', process.env.GMS_LOG_UI_STATE_STORE);

/**
 * Redux operation for setting the mode.
 *
 * @param mode the mode to set
 */
export const setMode = (mode: WaveformDisplayMode) => (dispatch: AppDispatch): void => {
  dispatch(analystActions.setMode(mode));
};

/**
 * Redux operation for setting the measurement mode entries.
 *
 * @param entries the measurement mode entries to set
 */
export const setMeasurementModeEntries = (entries: Record<string, boolean>) => (
  dispatch: AppDispatch
): void => {
  dispatch(analystActions.setMeasurementModeEntries(entries));
};

/**
 * Redux operation for setting the selected location solution.
 *
 * @param locationSolutionSetId the location solution set id
 * @param locationSolutionId the location solution id
 */
export const setSelectedLocationSolution = (
  locationSolutionSetId: string,
  locationSolutionId: string
) => (dispatch: AppDispatch, getState: () => AppState): void => {
  if (getState().app.analyst.location.selectedLocationSolutionSetId !== locationSolutionSetId) {
    dispatch(analystActions.setSelectedLocationSolutionSetId(locationSolutionSetId));
  }

  if (getState().app.analyst.location.selectedLocationSolutionId !== locationSolutionId) {
    dispatch(analystActions.setSelectedLocationSolutionId(locationSolutionId));
  }
};

/**
 * Redux operation for setting the selected preferred location solution.
 *
 * @param preferredLocationSolutionSetId the preferred location solution set id
 * @param preferredLocationSolutionId the preferred location solution id
 */
export const setSelectedPreferredLocationSolution = (
  preferredLocationSolutionSetId: string,
  preferredLocationSolutionId: string
) => (dispatch: AppDispatch, getState: () => AppState): void => {
  if (
    getState().app.analyst.location.selectedPreferredLocationSolutionSetId !==
    preferredLocationSolutionSetId
  ) {
    dispatch(
      analystActions.setSelectedPreferredLocationSolutionSetId(preferredLocationSolutionSetId)
    );
  }

  if (
    getState().app.analyst.location.selectedPreferredLocationSolutionId !==
    preferredLocationSolutionId
  ) {
    dispatch(analystActions.setSelectedPreferredLocationSolutionId(preferredLocationSolutionId));
  }
};

/**
 * Redux operation for setting the current open event id.
 *
 * @param event the event to set
 * @param latestLocationSolutionSet
 * @param preferredLocationSolutionId
 */
export const setOpenEventId = (
  event: LegacyEventTypes.Event | undefined,
  latestLocationSolutionSet: LegacyEventTypes.LocationSolutionSet | undefined,
  preferredLocationSolutionId: string | undefined
) => (dispatch: AppDispatch, getState: () => AppState): void => {
  if (getState().app.workflow.timeRange && event) {
    if (getState().app.analyst.openEventId !== event.id) {
      dispatch(analystActions.setOpenEventId(event.id));
      dispatch(analystActions.setSelectedEventIds([event.id]));
      dispatch(analystActions.setSelectedSortType(WaveformSortType.distance));

      // set the default (latest) location solution
      setSelectedLocationSolution(
        latestLocationSolutionSet ? latestLocationSolutionSet.id : undefined,
        latestLocationSolutionSet ? latestLocationSolutionSet.locationSolutions[0].id : undefined
      )(dispatch, getState);

      // set the default (latest) preferred location solution
      setSelectedPreferredLocationSolution(
        latestLocationSolutionSet.id,
        preferredLocationSolutionId
      )(dispatch, getState);
    }
  } else {
    dispatch(analystActions.setOpenEventId(undefined));
    dispatch(analystActions.setSelectedEventIds([]));
    dispatch(analystActions.setSelectedSortType(WaveformSortType.stationNameAZ));
    dispatch(analystActions.setMeasurementModeEntries({}));
    // update the selected location and preferred location solutions
    setSelectedLocationSolution(undefined, undefined)(dispatch, getState);
    setSelectedPreferredLocationSolution(undefined, undefined)(dispatch, getState);
  }

  // TODO: confirm should we be clearing????
  dispatch(analystActions.setSelectedSdIds([]));
  dispatch(analystActions.setSdIdsToShowFk([]));
  setMode(WaveformDisplayMode.DEFAULT)(dispatch);
};

/**
 * creates an action that sets the selected filter list to be the preferred filter list from the list of
 * preferred filter lists returned by the filter definition query.
 * Logs warnings if it is being misused, for example, if the preferred filter list for the activity is nullish,
 * or if the selectedFilterList was already set.
 *
 * @see signalEnhancementConfigurationApiSlice
 */
export const setPreferredFilterList = () => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  const state = getState();
  const preferredFilterListForActivity = selectPreferredFilterListForActivity(state);
  const selectedFilterList = selectSelectedFilterListName(state);
  if (selectedFilterList == null && preferredFilterListForActivity != null) {
    dispatch(analystActions.setSelectedFilterList(preferredFilterListForActivity));
  } else if (selectedFilterList != null && preferredFilterListForActivity == null) {
    logger.warn(`Cannot set preferred filter list because it is not defined.`);
  } else if (selectedFilterList == null && preferredFilterListForActivity == null) {
    logger.warn(
      `Cannot set preferred filter list because neither it nor the selected filter list is defined.`
    );
  } else {
    logger.warn(
      `Cannot set preferred filter list ${preferredFilterListForActivity} because ${selectedFilterList} was already selected.`
    );
  }
};

/**
 * Sets the default filter from the currently selected filter list.
 * Logs a warning if no selected filter is found.
 * This will subscribe the component to the results of the filter list query.
 * TODO: We need to unsubscribe? How? Where?
 */
export const setDefaultFilter = () => async (
  dispatch: AppDispatch,
  getState: () => AppState
): Promise<void> => {
  const state = getState();
  // subscribe to this state
  await dispatch(
    signalEnhancementConfigurationApiSlice.endpoints.getFilterListsDefinition.initiate()
  );
  const fl = selectSelectedFilterList(state);
  if (fl == null) {
    logger.warn('cannot set default filter list');
    return;
  }
  dispatch(analystActions.setSelectedFilterIndex(fl.defaultFilterIndex));
};
