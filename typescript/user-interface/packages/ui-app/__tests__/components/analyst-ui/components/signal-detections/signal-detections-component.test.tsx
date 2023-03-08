import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import { SignalDetectionsComponent } from '../../../../../src/ts/components/analyst-ui/components';

jest.mock(
  '../../../../../src/ts/components/analyst-ui/components/signal-detections/signal-detections-panel',
  () => {
    function MockSignalDetections() {
      return <div className="ian-signal-detections-wrapper" />;
    }
    return { SignalDetectionsPanel: () => MockSignalDetections() };
  }
);

const getAllStationsQueryFn = jest.fn(() => {
  return {};
});

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetAllStationsQuery: jest.fn(() => getAllStationsQueryFn()),
    useGetCurrentStationsQuery: jest.fn(() => ({
      data: []
    }))
  };
});

const { container } = render(
  <Provider store={getStore()}>
    <SignalDetectionsComponent glContainer={{} as any} />
  </Provider>
);

describe('ui ian signal detections', () => {
  test('is defined', () => {
    expect(SignalDetectionsComponent).toBeDefined();
  });

  test('can mount signal detections', () => {
    expect(container).toMatchSnapshot();
  });
});
