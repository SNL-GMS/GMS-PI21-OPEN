import { combineReducers } from '@reduxjs/toolkit';

import {
  eventManagerApiSlice,
  processingConfigurationApiSlice,
  processingStationApiSlice,
  signalEnhancementConfigurationApiSlice,
  sohAceiApiSlice,
  ssamControlApiSlice,
  stationDefinitionSlice,
  systemEventGatewayApiSlice,
  systemMessageDefinitionApiSlice,
  userManagerApiSlice,
  workflowApiSlice
} from './api';
import { dataSlice } from './api/data/data-slice';
import { reducer as appReducer } from './state/reducer';

export const reducer = combineReducers({
  // application api (queries)
  [systemEventGatewayApiSlice.reducerPath]: systemEventGatewayApiSlice.reducer,
  [eventManagerApiSlice.reducerPath]: eventManagerApiSlice.reducer,
  [processingConfigurationApiSlice.reducerPath]: processingConfigurationApiSlice.reducer,
  [processingStationApiSlice.reducerPath]: processingStationApiSlice.reducer,
  [signalEnhancementConfigurationApiSlice.reducerPath]:
    signalEnhancementConfigurationApiSlice.reducer,
  [ssamControlApiSlice.reducerPath]: ssamControlApiSlice.reducer,
  [sohAceiApiSlice.reducerPath]: sohAceiApiSlice.reducer,
  [stationDefinitionSlice.reducerPath]: stationDefinitionSlice.reducer,
  [systemMessageDefinitionApiSlice.reducerPath]: systemMessageDefinitionApiSlice.reducer,
  [userManagerApiSlice.reducerPath]: userManagerApiSlice.reducer,
  [workflowApiSlice.reducerPath]: workflowApiSlice.reducer,
  // data state
  [dataSlice.name]: dataSlice.reducer,
  // application state
  app: appReducer
});
