import Axios from 'axios';
import * as React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import { getStore } from '../../../src/ts/app/store';
import { waitForComponentToPaint } from '../../test-util';

/**
 * Creates and mounts a test component that calls the hook provided.
 * The test component is wrapped by the react query provider, which
 * has default values set.
 * The axios call returns a promise with the
 *
 * @param useHook the hook to call
 * @param resolve an optional resolve object for the Axios promise.
 * Defaults to 'Successful Query'
 */
export const expectQueryHookToMakeAxiosRequest = async (useHook: () => any): Promise<void> => {
  const axiosSchema = {
    data: {},
    status: 200,
    statusText: 'OK',
    headers: {},
    config: {},
    request: {}
  };
  Axios.request = jest.fn(async () => Promise.resolve(axiosSchema)) as any;
  const numCalls = (Axios.request as jest.Mock).mock.calls.length;

  function TestComponent() {
    const query = useHook();
    return <div>{JSON.stringify(query.data)}</div>;
  }

  function TestComponentWithProvider() {
    return (
      <Provider store={getStore()}>
        <TestComponent />
      </Provider>
    );
  }

  // Mounting may call the request, if React decides to run it soon.
  const wrapper = create(<TestComponentWithProvider />);

  // This ensures that the axios request will have been called.
  await waitForComponentToPaint(wrapper);

  // eslint-disable-next-line @typescript-eslint/unbound-method
  expect((Axios.request as jest.Mock).mock.calls.length).toBeGreaterThan(numCalls);
};

/**
 * Creates and mounts a test component that calls the hook provided.
 * The test component is wrapped by the react query provider, which
 * has default values set.
 * Checks to make sure the axios query was not called
 *
 * @param useHook the hook to call
 * @param resolve an optional resolve object for the Axios promise.
 * Defaults to 'Successful Query'
 */
export const expectQueryHookToNotMakeAxiosRequest = async (useHook: () => any): Promise<void> => {
  const axiosSchema = {
    data: {},
    status: 200,
    statusText: 'OK',
    headers: {},
    config: {},
    request: {}
  };
  Axios.request = jest.fn(async () => Promise.resolve(axiosSchema)) as any;

  function TestComponent() {
    const query = useHook();
    return <div>{JSON.stringify(query.data)}</div>;
  }

  function TestComponentWithProvider() {
    return (
      <Provider store={getStore()}>
        <TestComponent />
      </Provider>
    );
  }

  // Mounting may call the request, if React decides to run it soon.
  const wrapper = create(<TestComponentWithProvider />);

  // This ensures that the axios request will have been called.
  await waitForComponentToPaint(wrapper);

  // eslint-disable-next-line @typescript-eslint/unbound-method
  expect((Axios.request as jest.Mock).mock.calls).toHaveLength(0);
};
