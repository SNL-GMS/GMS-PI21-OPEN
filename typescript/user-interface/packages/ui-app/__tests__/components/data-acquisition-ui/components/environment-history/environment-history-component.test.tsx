import { SohTypes } from '@gms/common-model';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import React from 'react';

import { EnvironmentHistoryComponent } from '../../../../../src/ts/components/data-acquisition-ui/components/environment-history/environment-history-component';
import { testStationSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';
import { useQueryStateResult } from '../../../../__data__/test-util-data';

const MOCK_TIME = 1611153271425;
Date.now = jest.fn(() => MOCK_TIME);
Date.constructor = jest.fn(() => new Date(MOCK_TIME));
window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

const glContainerHidden: any = { isHidden: true };
const glContainer: any = { isHidden: false };

const sohConfigurationQuery: any = useQueryStateResult;
sohConfigurationQuery.data = sohConfiguration;

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    // useGetSohConfigurationQuery: jest.fn(() => ({
    //   data: sohConfiguration,
    //   isLoading: false
    // })),
    useGetHistoricalAceiDataQuery: jest.fn(() => ({
      data: [],
      isLoading: false
    }))
  };
});

describe('Environment history panel', () => {
  it('should be defined', () => {
    expect(Date.now()).toEqual(MOCK_TIME);
    expect(EnvironmentHistoryComponent).toBeDefined();
  });
  it('no soh data', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={undefined}
        selectedStationIds={undefined}
        selectedAceiType={undefined}
        sohStatus={undefined}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={undefined}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('hidden', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainerHidden}
        selectedStationIds={undefined}
        selectedAceiType={undefined}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: undefined
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={undefined}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('No station Selected', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[]}
        selectedAceiType={undefined}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: undefined
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={undefined}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('No Station Selected', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={['1', '2', '3']}
        selectedAceiType={undefined}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [testStationSoh]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('Multiple Stations Selected', () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[testStationSoh.stationName, '1', '2', '3']}
        selectedAceiType={undefined}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [{ ...testStationSoh, channelSohs: [] }]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });
  it('matches the snapshot with data no selected station', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[]}
        selectedAceiType={undefined}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [testStationSoh]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });
  it('No Monitor Selected', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[testStationSoh.stationName]}
        selectedAceiType={undefined}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [testStationSoh]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('Unsupported monitor type', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[testStationSoh.stationName]}
        selectedAceiType={SohTypes.AceiType.BEGINNING_DATE_OUTAGE}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [testStationSoh]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('with data for CLIPPED', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[testStationSoh.stationName]}
        selectedAceiType={SohTypes.AceiType.CLIPPED}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [testStationSoh]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('Loading Station SOH', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[testStationSoh.stationName]}
        selectedAceiType={SohTypes.AceiType.CLIPPED}
        sohStatus={{
          lastUpdated: 0,
          loading: true,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [testStationSoh]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('No Station Group Data', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[testStationSoh.stationName]}
        selectedAceiType={SohTypes.AceiType.CLIPPED}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: undefined
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('No Channel Data - Check this stations configuration', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[testStationSoh.stationName]}
        selectedAceiType={SohTypes.AceiType.CLIPPED}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: true,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [{ ...testStationSoh, channelSohs: [] }]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });

  it('Loading - Channel SOH', () => {
    const { container } = render(
      <EnvironmentHistoryComponent
        glContainer={glContainer}
        selectedStationIds={[testStationSoh.stationName]}
        selectedAceiType={SohTypes.AceiType.CLIPPED}
        sohStatus={{
          lastUpdated: 0,
          loading: false,
          isStale: false,
          stationAndStationGroupSoh: {
            isUpdateResponse: false,
            stationGroups: [],
            stationSoh: [{ ...testStationSoh, channelSohs: undefined }]
          }
        }}
        setSelectedStationIds={jest.fn()}
        setSelectedAceiType={undefined}
        getSohConfiguration={jest.fn(() => ({ unsubscribe: jest.fn() })) as any}
        sohConfigurationQuery={sohConfigurationQuery}
      />
    );
    expect(container).toMatchSnapshot();
  });
});
