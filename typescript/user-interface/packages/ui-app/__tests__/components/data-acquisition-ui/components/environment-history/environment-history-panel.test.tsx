/* eslint-disable react/jsx-no-constructed-context-values */
import { SohTypes } from '@gms/common-model';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import React from 'react';

import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display/base-display-context';
import { AceiContext } from '../../../../../src/ts/components/data-acquisition-ui/components/environment-history/acei-context';
import { EnvironmentHistoryPanel } from '../../../../../src/ts/components/data-acquisition-ui/components/environment-history/environment-history-panel';
import { testStationSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

const MOCK_TIME = 1611153271425;
const MOCK_TIME_STR = '2021-01-20 02:34:31';

const mockDate: any = new Date(MOCK_TIME);
mockDate.now = () => MOCK_TIME;
Date.constructor = jest.fn(() => new Date(MOCK_TIME));
jest.spyOn(global, 'Date').mockImplementation(() => mockDate);
Date.now = jest.fn(() => MOCK_TIME);
Date.UTC = jest.fn(() => MOCK_TIME);

jest.mock('moment-precise-range-plugin', () => {
  return {};
});

jest.mock('moment', () => {
  // mock chain builder pattern
  const mMoment = {
    utc: jest.fn(() => mMoment),
    format: jest.fn(() => MOCK_TIME_STR)
  };

  // mock the constructor and to modify instance methods
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const fn: any = jest.fn(() => {
    mMoment.format = jest.fn(() => MOCK_TIME_STR);
    return mMoment;
  });

  // mock moment methods that depend on moment not on a moment instance
  fn.unix = () => ({ utc: () => mMoment });
  return fn;
});

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetSohConfigurationQuery: jest.fn(() => ({
      data: sohConfiguration,
      isLoading: false
    })),
    useGetHistoricalAceiDataQuery: jest.fn(() => ({
      data: [],
      isLoading: false
    }))
  };
});
describe('Environment history panel', () => {
  it('should be defined', () => {
    expect(Date.now()).toEqual(MOCK_TIME);
    expect(EnvironmentHistoryPanel).toBeDefined();
  });
  it('matches the snapshot empty channel soh data', () => {
    const { container } = render(
      <BaseDisplayContext.Provider
        value={{
          glContainer: undefined,
          heightPx: 100,
          widthPx: 100
        }}
      >
        <AceiContext.Provider
          value={{
            selectedAceiType: SohTypes.AceiType.AMPLIFIER_SATURATION_DETECTED,
            setSelectedAceiType: jest.fn()
          }}
        >
          <EnvironmentHistoryPanel
            channelSohs={[]}
            // eslint-disable-next-line @typescript-eslint/no-magic-numbers
            sohHistoricalDurations={[1000, 5000, 8000]}
            station={testStationSoh}
          />
        </AceiContext.Provider>
      </BaseDisplayContext.Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
