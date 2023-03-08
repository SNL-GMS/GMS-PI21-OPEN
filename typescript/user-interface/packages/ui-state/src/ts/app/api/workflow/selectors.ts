import type { AppState } from '../../store';

/**
 * A redux selector for returning the open interval name.
 *
 * @example const name = useAppState(selectOpenIntervalName);
 *
 * @param state the redux app state
 * @returns the open interval name
 */
export const selectOpenIntervalName = (state: AppState): string => {
  return state.app.workflow.openIntervalName;
};

/**
 * A redux selector for returning the open activity names.
 *
 * @example const names = useAppState(selectOpenActivityNames);
 *
 * @param state the redux app state
 * @returns the list of open activity names
 */
export const selectOpenActivityNames = (state: AppState): string[] => {
  return state.app.workflow.openActivityNames;
};
