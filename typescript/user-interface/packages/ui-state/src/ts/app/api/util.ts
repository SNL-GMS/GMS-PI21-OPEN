import type { AppDispatch } from '../store';
import { dataSlice } from './data';
import { eventManagerApiSlice } from './event-manager/event-manager-api-slice';
import { processingConfigurationApiSlice } from './processing-configuration';
import { ssamControlApiSlice } from './ssam-control';
import { systemMessageDefinitionApiSlice } from './system-message-definition';
import { userManagerApiSlice } from './user-manager';
import { workflowApiSlice } from './workflow';

/**
 * Clears out the entire api state.
 *
 * @param dispatch
 */
export const resetApiState = (dispatch: AppDispatch): void => {
  dispatch(eventManagerApiSlice.util.resetApiState());
  dispatch(workflowApiSlice.util.resetApiState());
  dispatch(ssamControlApiSlice.util.resetApiState());
  dispatch(userManagerApiSlice.util.resetApiState());
  dispatch(processingConfigurationApiSlice.util.resetApiState());
  dispatch(systemMessageDefinitionApiSlice.util.resetApiState());
  dispatch(dataSlice.actions.clearAll());
};
