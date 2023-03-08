import { getStore } from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import { IANMap } from '../../../../../src/ts/components/analyst-ui/components/map';

jest.mock('../../../../../src/ts/components/analyst-ui/components/map/ian-map-panel', () => {
  function MockMap() {
    return (
      <div className="cesium-viewer-toolbar">
        <span className="cesium-sceneModePicker-wrapper cesium-toolbar-button" />
      </div>
    );
  }
  return { IANMapPanel: () => MockMap() };
});

const getAllStationsQueryFn = jest.fn(() => {
  return {};
});

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useSignalDetectionForMap: jest.fn(() => ({
      data: signalDetectionsData
    })),
    useGetAllStationsQuery: jest.fn(() => getAllStationsQueryFn()),
    useGetCurrentStationsQuery: jest.fn(() => ({
      data: []
    }))
  };
});

describe('ui map', () => {
  test('is defined', () => {
    expect(IANMap).toBeDefined();
  });

  test('can mount map', () => {
    const { container } = render(
      <Provider store={getStore()}>
        <IANMap />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  test('calls station groups', () => {
    const { container } = render(
      <Provider store={getStore()}>
        <IANMap />
      </Provider>
    );
    expect(container).toBeDefined();
    expect(getAllStationsQueryFn).toHaveBeenCalled();
  });
});
