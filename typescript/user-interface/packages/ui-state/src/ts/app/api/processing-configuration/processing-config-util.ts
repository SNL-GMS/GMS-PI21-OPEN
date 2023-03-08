import type { ConfigurationTypes } from '@gms/common-model';
import type { ProcessingAnalystConfiguration } from '@gms/common-model/lib/ui-configuration/types';

import type { AppState } from '../../store';
import { userManagerApiSlice } from '../user-manager/user-manager-api-slice';
import { defaultTheme } from './default-theme';
import { processingConfigurationApiSlice } from './processing-configuration-api-slice';

/**
 * @param getState A function that returns the current state
 * @returns the processing analyst configuration from the query results, or undefined if the
 * query doesn't have data (such as in the case of an error)
 */
export const getProcessingAnalystConfiguration = (
  getState: () => AppState
): ProcessingAnalystConfiguration | undefined =>
  processingConfigurationApiSlice.endpoints.getProcessingAnalystConfiguration.select()(getState())
    .data;

/**
 * Uses the processing configuration to get the list of themes, and the current theme from the user
 * profile, in order to return the selected user profile.
 *
 * @default @see defaultTheme
 * @param getState a function to return the current redux state
 * @returns the currently selected UI theme, or the default theme if the current theme is not found.
 */
export const getUiTheme = (getState: () => AppState): ConfigurationTypes.UITheme => {
  const state = getState();
  const uiThemes = processingConfigurationApiSlice.endpoints.getProcessingAnalystConfiguration.select()(
    state
  )?.data?.uiThemes;
  const currentTheme = userManagerApiSlice.endpoints.getUserProfile.select()(state)?.data
    ?.currentTheme;
  return uiThemes?.find(t => t.name === currentTheme) ?? defaultTheme;
};
