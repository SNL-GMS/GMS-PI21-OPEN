/* eslint-disable react/jsx-no-constructed-context-values */
import { uuid } from '@gms/common-util';
import { dataAcquisitionActions, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';
import type { SohOverviewContextData } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-context';
import { SohOverviewContext } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-context';
import { SohOverviewPanel } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-panel';
import { stationAndStationGroupSohStatus } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetSohConfigurationQuery: jest.fn(() => ({
      data: sohConfiguration,
      isLoading: false
    }))
  };
});
let idCount = 0;
// eslint-disable-next-line no-plusplus
uuid.asString = jest.fn().mockImplementation(() => ++idCount);

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

const contextValues: SohOverviewContextData = {
  stationSoh: stationAndStationGroupSohStatus.stationSoh,
  stationGroupSoh: stationAndStationGroupSohStatus.stationGroups,
  acknowledgeSohStatus: jest.fn(),
  glContainer: undefined,
  quietTimerMs: 5000,
  updateIntervalSecs: 5,
  selectedStationIds: [],
  setSelectedStationIds: jest.fn(),
  sohStationStaleTimeMS: 30000
};

describe('Soh Panel', () => {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  Date.now = jest.fn().mockReturnValue(1573244087715);
  const setState = jest.fn();
  const useStateSpy = jest.spyOn(React, 'useState');
  const mock: any = init => [init, setState];
  useStateSpy.mockImplementation(mock);

  const store = getStore();
  store.dispatch(
    dataAcquisitionActions.setSohStatus({
      isStale: false,
      lastUpdated: 0,
      loading: false,
      stationAndStationGroupSoh: undefined
    })
  );

  const { container } = render(
    <Provider store={store}>
      <SohOverviewContext.Provider value={contextValues}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: { width: 150, height: 150 } as any,
            widthPx: 150,
            heightPx: 150
          }}
        >
          <SohOverviewPanel />
        </BaseDisplayContext.Provider>
      </SohOverviewContext.Provider>
    </Provider>
  );

  it('should be defined', () => {
    expect(SohOverviewPanel).toBeDefined();
  });

  it('should match snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
