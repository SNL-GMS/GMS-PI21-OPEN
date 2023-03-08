/* eslint-disable react/jsx-no-constructed-context-values */
/* eslint-disable react/jsx-props-no-spreading */
import { H1 } from '@blueprintjs/core';
import { getStore } from '@gms/ui-state';
import { processingStations } from '@gms/ui-state/__tests__/__data__/processing-station-data';
import { sohStatus } from '@gms/ui-state/__tests__/__data__/soh-status-data';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';
import { SohMapPanel } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-map/soh-map-panel';
import type { SohMapPanelProps } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-map/types';

jest.mock('../../../../../src/ts/components/common-ui/components/map', () => {
  function MockMap() {
    return <H1>Map</H1>;
  }
  return { Map: () => MockMap() };
});

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useProcessingStations: jest.fn(() => processingStations)
  };
});

const mockSetSelectedStationIds = jest.fn();
const sohMapProps: SohMapPanelProps = {
  minHeightPx: 100,
  selectedStationIds: ['ABC'],
  sohStatus,
  setSelectedStationIds: mockSetSelectedStationIds
};

describe('soh map panel', () => {
  it('is defined', () => {
    expect(SohMapPanel).toBeDefined();
  });
  it('can mount map', () => {
    const { container } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{ glContainer: {} as any, widthPx: 200, heightPx: 200 }}
        >
          <SohMapPanel {...sohMapProps} />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
