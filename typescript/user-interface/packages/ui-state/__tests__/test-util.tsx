/* eslint-disable react/function-component-definition */
/* eslint-disable no-promise-executor-return */
import { configureStore } from '@reduxjs/toolkit';
import cloneDeep from 'lodash/cloneDeep';
import * as React from 'react';
import { act } from 'react-dom/test-utils';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';
import type { Action, Store } from 'redux';

import {
  dataInitialState,
  dataSlice,
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
} from '../src/ts/app/api';
import { initialState, reducer } from '../src/ts/app/state/reducer';
import type { AppState } from '../src/ts/app/store';

const TIME_TO_WAIT_MS = 2000;

/**
 * Fixes React warning that "An update to Component inside a test was not wrapped in act(...)."
 */
export const waitForComponentToPaint = async (wrapper: any): Promise<void> => {
  // eslint-disable-next-line @typescript-eslint/await-thenable
  await act(async () => {
    await new Promise(resolve => setTimeout(resolve, TIME_TO_WAIT_MS));
    wrapper.update();
  });
};

export const expectHookToCallWorker = async (
  useHook: () => any,
  reduxStore: Store<any, Action>,
  expectedValue?: any
): Promise<void> => {
  const TestComponent: React.FC = () => {
    const query = useHook();
    return <div>{JSON.stringify(query.data)}</div>;
  };

  // Mounting may call the request, if React decides to run it soon.
  const wrapper = create(
    <Provider store={reduxStore}>
      <TestComponent />
    </Provider>
  );

  // This ensures that the axios request will have been called.
  await waitForComponentToPaint(wrapper);

  expect(wrapper.toJSON()).toMatchSnapshot();

  if (expectedValue) {
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(JSON.stringify(wrapper.toJSON)).toContain(expectedValue);
  }
};

/**
 * the props for the @component HookChecker component.
 */
export interface HookProps<HookReturnType> {
  /**
   * A hook to be called within the HookChecker component
   */
  useHook: () => HookReturnType;
  children: (result: HookReturnType) => void;
}

/**
 * When passed a hook and given a `checker` function as a render prop, this will call the hook, then call
 * the checker function with the return value of the hook. This allows us to perform assertions on the
 * result of the hook. Be sure to use `act` on any assertions that will require a render.
 *
 * @param props expects a useHook prop and a children prop. The children prop should contain a function
 * that will be called with the return value of useHook.
 * @returns null, should not render anything.
 */
export function HookChecker<HookReturnType>({
  useHook,
  children
}: HookProps<HookReturnType>): null {
  const result = useHook();
  if (children && typeof children === 'function') {
    const check: any = children;
    check(result);
  } else {
    throw new Error(
      'Invalid children in HookChecker. Pass in a function that expects the hook results as a parameter, and run your assertions in there.'
    );
  }
  return null;
}

/**
 * Initial App state
 */
export const appState: AppState = {
  eventManagerApi: undefined,
  processingConfigurationApi: undefined,
  processingStationApi: undefined,
  signalEnhancementConfigurationApi: undefined,
  ssamControlApi: undefined,
  systemMessageDefinitionApi: undefined,
  userManagerApi: undefined,
  workflowApi: undefined,
  sohAceiApi: undefined,
  stationDefinitionApi: undefined,
  systemEventGatewayApi: undefined,
  data: dataInitialState,
  app: cloneDeep(initialState)
};

/**
 * signalEnhancementConfigurationApiSlice
 *
 * @returns a new Redux store (without our custom middleware) that contains the initial state.
 */
export const configureNewStore = (): Store<AppState> =>
  configureStore({
    reducer: {
      // application api (queries)
      [systemEventGatewayApiSlice.reducerPath]: systemEventGatewayApiSlice.reducer,
      [eventManagerApiSlice.reducerPath]: eventManagerApiSlice.reducer,
      [processingConfigurationApiSlice.reducerPath]: processingConfigurationApiSlice.reducer,
      [processingStationApiSlice.reducerPath]: processingStationApiSlice.reducer,
      [signalEnhancementConfigurationApiSlice.reducerPath]:
        signalEnhancementConfigurationApiSlice.reducer,
      [sohAceiApiSlice.reducerPath]: sohAceiApiSlice.reducer,
      [ssamControlApiSlice.reducerPath]: ssamControlApiSlice.reducer,
      [stationDefinitionSlice.reducerPath]: stationDefinitionSlice.reducer,
      [systemMessageDefinitionApiSlice.reducerPath]: systemMessageDefinitionApiSlice.reducer,
      [userManagerApiSlice.reducerPath]: userManagerApiSlice.reducer,
      [workflowApiSlice.reducerPath]: workflowApiSlice.reducer,
      // data state for Signal Detection, Channel Segments...
      [dataSlice.name]: dataSlice.reducer,
      // application state
      app: reducer
    }
  });
