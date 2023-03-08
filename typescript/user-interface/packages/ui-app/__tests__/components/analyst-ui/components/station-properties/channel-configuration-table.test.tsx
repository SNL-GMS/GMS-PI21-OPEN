import type { ResponseTypes } from '@gms/common-model';
import { ChannelTypes, CommonTypes, ProcessingStationTypes, StationTypes } from '@gms/common-model';
import { toEpochSeconds } from '@gms/common-util';
import { stationPropertiesConfigurationInitialState } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';

import { ChannelConfigurationTable } from '../../../../../src/ts/components/analyst-ui/components/station-properties/channel-configuration-table';
import { ChannelColumnDefs } from '../../../../../src/ts/components/analyst-ui/components/station-properties/column-definitions';
import { channelColumnsToDisplay } from '../../../../../src/ts/components/analyst-ui/components/station-properties/station-properties-utils';

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

describe('Channel Configuration Table', () => {
  const station: StationTypes.Station = {
    name: 'STA',
    effectiveAt: toEpochSeconds('2021-04-20T16:11:31.118870Z'),
    effectiveUntil: toEpochSeconds('2021-04-20T16:11:31.118870Z'),
    relativePositionsByChannel: {
      'Real Channel Name One': {
        northDisplacementKm: 50,
        eastDisplacementKm: 5,
        verticalDisplacementKm: 10
      },
      'Real Channel Name Two': {
        northDisplacementKm: 50,
        eastDisplacementKm: 5,
        verticalDisplacementKm: 10
      }
    },
    channelGroups: [{} as any],
    allRawChannels: [{} as any],
    description: 'This is a test station',
    location: {
      latitudeDegrees: 35.647,
      longitudeDegrees: 100.0,
      depthKm: 50.0,
      elevationKm: 10.0
    },
    type: StationTypes.StationType.SEISMIC_1_COMPONENT
  };
  const channels: ChannelTypes.Channel[] = [
    {
      station: { name: 'STA' },
      response: {
        fapResponse: ({
          // TODO no id currently in FAP response
          id: '966deee1-545e-44ed-a346-b029ab111151'
        } as unknown) as ResponseTypes.FrequencyAmplitudePhase,
        calibration: {
          calibrationPeriodSec: 14.5,
          calibrationTimeShift: 'PT2S',
          calibrationFactor: {
            value: 1.2,
            standardDeviation: 0.112,
            units: CommonTypes.Units.SECONDS
          }
        },
        effectiveAt: toEpochSeconds('2021-04-20T16:11:30.674083Z'),
        effectiveUntil: toEpochSeconds('2021-04-20T16:11:30.674083Z'),
        id: '5dc3dce4-ae3c-3fb0-84ba-2f83fb5a89d2'
      },
      processingMetadata: new Map<ChannelTypes.ChannelProcessingMetadataType, any>([
        [ChannelTypes.ChannelProcessingMetadataType.CHANNEL_GROUP, 'CHANNEL_GROUP']
      ]),
      channelBandType: ChannelTypes.ChannelBandType.BROADBAND, // 'BROADBAND',
      channelInstrumentType: ChannelTypes.ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
      channelOrientationType: ChannelTypes.ChannelOrientationType.VERTICAL,
      channelOrientationCode: 'Z',
      channelDataType: ProcessingStationTypes.ChannelDataType.DIAGNOSTIC_SOH,
      nominalSampleRateHz: 66.0,
      orientationAngles: {
        horizontalAngleDeg: 65.0,
        verticalAngleDeg: 135.0
      },
      configuredInputs: [],
      processingDefinition: new Map<string, any>(),
      description: 'Example description',
      canonicalName: 'Canonical Name One',
      location: {
        latitudeDegrees: 35.0,
        longitudeDegrees: -125.0,
        depthKm: 100.0,
        elevationKm: 5500.0
      },
      units: CommonTypes.Units.NANOMETERS,
      effectiveAt: toEpochSeconds('1970-01-01T00:00:00Z'),
      effectiveUntil: toEpochSeconds('1970-01-01T00:00:00Z'),
      name: 'Real Channel Name One'
    }
  ];
  const { container } = render(
    <ChannelConfigurationTable
      channels={channels}
      stationData={station}
      columnsToDisplay={channelColumnsToDisplay}
    />
  );
  test('can mount', () => {
    expect(ChannelConfigurationTable).toBeDefined();
  });
  test('matches snapshot', () => {
    expect(container).toMatchSnapshot();
  });
  test('table useEffect', () => {
    jest.useFakeTimers();
    const setTimeoutSpy = jest.spyOn(global, 'setTimeout');
    const clearTimeoutSpy = jest.spyOn(global, 'clearTimeout');
    const tableWrapper = render(
      <ChannelConfigurationTable
        channels={channels}
        stationData={station}
        columnsToDisplay={channelColumnsToDisplay}
      />
    );
    jest.runOnlyPendingTimers();
    expect(tableWrapper).toBeDefined();
    expect(setTimeoutSpy).toHaveBeenCalled();
    tableWrapper.unmount();
    expect(clearTimeoutSpy).toHaveBeenCalled();
    jest.useRealTimers();
  });
  test('channel configuration visible column order matches channel column definitions order', () => {
    const channelColumnDefFieldNamesList = ChannelColumnDefs.map(c => c.field);
    const channelConfigurationColumnKeys = [
      ...Object.keys(stationPropertiesConfigurationInitialState.channelConfigurationColumns)
    ];
    expect(channelColumnDefFieldNamesList).toEqual(channelConfigurationColumnKeys);
  });
});
