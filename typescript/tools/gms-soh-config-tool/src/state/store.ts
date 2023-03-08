import { combineReducers, configureStore } from '@reduxjs/toolkit';

import { setupListeners } from '@reduxjs/toolkit/query/react';
import { configApi } from './api-slice';
import { appSettingsSlice } from './app-settings-slice';
import { stationControlsSlice } from './station-controls-slice';

const reducer = combineReducers({
  [appSettingsSlice.name]: appSettingsSlice.reducer,
  [configApi.reducerPath]: configApi.reducer,
  [stationControlsSlice.name]: stationControlsSlice.reducer,
});

export const store = configureStore({
  reducer,
  devTools: {
    trace: true,
  },
  // Adding the api middleware enables caching, invalidation, polling,
  // and other useful features of `rtk-query`.
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(configApi.middleware),
});

// optional, but required for refetchOnFocus/refetchOnReconnect behaviors
// see `setupListeners` docs - takes an optional callback as the 2nd arg for customization
setupListeners(store.dispatch);

/**
 * The application state.
 * Infer the `AppState` types from the store itself
 */
export type AppState = ReturnType<typeof reducer>;

/**
 * The application dispatched (typed).
 * Infer the `AppDispatch` types from the store itself
 */
export type AppDispatch = typeof store.dispatch;

export type AppStore = typeof store;
