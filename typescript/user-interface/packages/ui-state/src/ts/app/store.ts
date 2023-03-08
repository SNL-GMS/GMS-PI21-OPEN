import { IS_NODE_ENV_DEVELOPMENT, IS_NODE_ENV_PRODUCTION, NODE_ENV } from '@gms/common-util';
import { getElectron, getElectronEnhancer, UILogger } from '@gms/ui-util';
import { configureStore } from '@reduxjs/toolkit';
import type { ThunkMiddleware } from '@reduxjs/toolkit/node_modules/redux-thunk';
import thunk from '@reduxjs/toolkit/node_modules/redux-thunk';
import { createLogger } from 'redux-logger';
import {
  createStateSyncMiddleware,
  initStateWithPrevTab,
  withReduxStateSync
} from 'redux-state-sync';

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
import { reducer } from './reducer';
import { userSessionActions } from './state';
import { waveformSlice } from './state/waveform';
import {
  establishWsConnection,
  registerConnectionStatusCallback
} from './subscription/subscription';

const DISABLE_REDUX_STATE_SYNC = process.env.GMS_DISABLE_REDUX_STATE_SYNC === 'true';

const logger = UILogger.create('GMS_LOG_REDUX_STORE', process.env.GMS_LOG_REDUX_STORE || 'info');

const electron = getElectron();
const electronEnhancer = electron ? getElectronEnhancer() : undefined;

if (electron && electronEnhancer) {
  logger.info('Configuring Redux store for Electron');
} else {
  logger.info('Configuring Redux store for browser');
}

logger.info(
  `Configuring Redux store with the following properties: ` +
    `NODE_ENV:${NODE_ENV} ` +
    `GMS_ENABLE_REDUX_LOGGER:${process.env.GMS_ENABLE_REDUX_LOGGER} ` +
    `GMS_DISABLE_REDUX_IMMUTABLE_CHECK:${process.env.GMS_DISABLE_REDUX_IMMUTABLE_CHECK} ` +
    `GMS_DISABLE_REDUX_SERIALIZABLE_CHECK:${process.env.GMS_DISABLE_REDUX_SERIALIZABLE_CHECK} ` +
    `DISABLE_REDUX_STATE_SYNC:${DISABLE_REDUX_STATE_SYNC} `
);

const thunkMiddleware: ThunkMiddleware = thunk;

const buildStore = () => {
  const store = configureStore({
    reducer: !DISABLE_REDUX_STATE_SYNC ? withReduxStateSync(reducer) : reducer,
    devTools:
      !IS_NODE_ENV_PRODUCTION &&
      process.env.GMS_DISABLE_REDUX_DEV_TOOLS?.toLocaleLowerCase() !== 'true'
        ? {
            trace: true,
            actionsBlacklist: [
              waveformSlice.actions.incrementLoadingTotal.name,
              waveformSlice.actions.incrementLoadingCompleted.name
            ]
          }
        : false,
    middleware: getDefaultMiddleware => {
      const middlewares = getDefaultMiddleware({
        immutableCheck:
          IS_NODE_ENV_DEVELOPMENT &&
          process.env.GMS_DISABLE_REDUX_IMMUTABLE_CHECK?.toLocaleLowerCase() !== 'true',
        serializableCheck:
          IS_NODE_ENV_DEVELOPMENT &&
          process.env.GMS_DISABLE_REDUX_SERIALIZABLE_CHECK?.toLocaleLowerCase() !== 'true'
      })
        .concat(thunkMiddleware)
        .concat(systemEventGatewayApiSlice.middleware)
        .concat(eventManagerApiSlice.middleware)
        .concat(processingConfigurationApiSlice.middleware)
        .concat(processingStationApiSlice.middleware)
        .concat(signalEnhancementConfigurationApiSlice.middleware)
        .concat(sohAceiApiSlice.middleware)
        .concat(ssamControlApiSlice.middleware)
        .concat(stationDefinitionSlice.middleware)
        .concat(systemMessageDefinitionApiSlice.middleware)
        .concat(userManagerApiSlice.middleware)
        .concat(workflowApiSlice.middleware);

      if (!DISABLE_REDUX_STATE_SYNC) {
        middlewares.push(createStateSyncMiddleware());
      }

      // ! the logger should always be the last middleware added
      // enable the Redux logger only if `GMS_ENABLE_REDUX_LOGGER` is set to true
      if (process.env.GMS_ENABLE_REDUX_LOGGER?.toLocaleLowerCase() === 'true') {
        const reduxLogger = createLogger({
          collapsed: true,
          duration: true,
          timestamp: false,
          level: 'info',
          logger: console,
          logErrors: true,
          diff: false
        });
        middlewares.push(reduxLogger);
      }

      return middlewares;
    },
    enhancers:
      electron && electronEnhancer
        ? [
            // must be placed after the enhancers which dispatch
            // their own actions such as redux-thunk or redux-saga
            electronEnhancer({
              dispatchProxy: a => store.dispatch(a)
            })
          ]
        : []
  });

  // initialize state using any previous state from other tabs
  if (!DISABLE_REDUX_STATE_SYNC) {
    initStateWithPrevTab(store);
  }

  return store;
};

export interface GMSWindow extends Window {
  ReduxStore: ReturnType<typeof buildStore> | undefined;
}

const gmsWindow = (window as unknown) as GMSWindow;

// ! ensure that only one Redux store instance is created
if (gmsWindow.ReduxStore === undefined) {
  // !ensure a single instance of the Redux store by attaching it to the window object
  // ?this also exposes the Redux store for Cypress testing
  gmsWindow.ReduxStore = buildStore();

  // store is created connect to System Event gateway and set callback to set connection status
  establishWsConnection();
  const updateConnectionStatus = (connected: boolean): void => {
    if (gmsWindow.ReduxStore) {
      gmsWindow.ReduxStore.dispatch(userSessionActions.setConnected(connected));
    }
  };

  registerConnectionStatusCallback(
    updateConnectionStatus,
    gmsWindow.ReduxStore.getState().app.userSession
  );
} else {
  // !Throw an error if the Redux store is created more than once
  const error = new Error('Critical Error: Redux store has already been created');
  logger.error('Redux store should only be created once', error);
  throw error;
}

/**
 * Creates the Redux application store, which is stored on the window object in order to avoid any chance for
 * multiple stores to be created (if multiple copies of the module are included in the bundle, for example).
 *
 * @returns the Redux store for the application
 */
export const getStore = (): typeof gmsWindow.ReduxStore => gmsWindow.ReduxStore;

/**
 * The application state.
 * Infer the `AppState` types from the store itself
 */
export type AppState = ReturnType<typeof reducer>;

/**
 * The application dispatched (typed).
 * Infer the `AppDispatch` types from the store itself
 */
export type AppDispatch = typeof gmsWindow.ReduxStore.dispatch;
