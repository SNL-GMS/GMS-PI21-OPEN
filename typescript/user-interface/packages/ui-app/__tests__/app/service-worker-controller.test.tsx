import { sleep } from '@gms/common-util';
import { getStore } from '@gms/ui-state';
import { ServiceWorkerMessages } from '@gms/ui-workers';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';
import { act } from 'react-test-renderer';

import { ServiceWorkerController } from '../../src/ts/app/service-worker-controller';

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  process.env.GMS_SW = 'true';

  // do this in here so that it is defined early enough
  Object.defineProperty((global as any).navigator, 'serviceWorker', {
    value: {
      register: jest.fn(),
      addEventListener: jest.fn()
    }
  });
  // this is a no-op. We just use this mock to do some super hoisting so we can mock things early enough
  return actual;
});

describe('ServiceWorkerWrapper', () => {
  const store = getStore();
  it('renders a non ideal state while service worker is not yet initialized', async () => {
    const { container } = render(
      <Provider store={store}>
        <ServiceWorkerController>
          <div>The rest of the app would go here...</div>
        </ServiceWorkerController>
      </Provider>
    );
    await act(async () => {
      const waitDurationMs = 200;
      await sleep(waitDurationMs);
    });
    expect(container).toMatchSnapshot();
  });
  it('renders the element if no non ideal state happens', async () => {
    const mockAddEventListener = (
      _eventName: string,
      callback: (message: { data: string }) => unknown
    ) => {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      act(() => {
        callback({ data: ServiceWorkerMessages.listenersActiveMessage });
      });
    };
    jest
      .spyOn((global as any).navigator.serviceWorker, 'addEventListener')
      .mockImplementation(mockAddEventListener as any);
    const { container } = render(
      <Provider store={store}>
        <ServiceWorkerController>
          <div>The rest of the app would go here...</div>
        </ServiceWorkerController>
      </Provider>
    );
    await act(async () => {
      const waitDurationMs = 200;
      await sleep(waitDurationMs);
    });
    expect(container).toMatchSnapshot();
  });
  it('renders the error non ideal state if it gets an unknown message', async () => {
    const mockAddEventListener = (
      _eventName: string,
      callback: (message: { data: string }) => unknown
    ) => {
      try {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        act(() => {
          callback({ data: 'UNKNOWN_MESSAGE' });
        });
      } catch (e) {
        // just consume the error
      }
    };
    jest
      .spyOn((global as any).navigator.serviceWorker, 'addEventListener')
      .mockImplementation(mockAddEventListener as any);
    const { container } = render(
      <Provider store={store}>
        <ServiceWorkerController>
          <div>The rest of the app would go here...</div>
        </ServiceWorkerController>
      </Provider>
    );
    await act(async () => {
      const waitDurationMs = 200;
      await sleep(waitDurationMs);
    });
    expect(container).toMatchSnapshot();
  });
});
