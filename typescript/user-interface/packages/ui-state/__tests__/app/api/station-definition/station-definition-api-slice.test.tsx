/* eslint-disable jest/expect-expect */
import React from 'react';

import {
  convertRelativePositions,
  stationDefinitionSlice,
  useGetChannelsByNamesQuery,
  useGetStationGroupsByNamesQuery,
  useGetStationsEffectiveAtTimesQuery,
  useGetStationsQuery,
  useGetStationsWithChannelsQuery
} from '../../../../src/ts/app/api/station-definition';
import { expectQueryHookToMakeAxiosRequest } from '../query-test-util';

describe('Station Definition API Slice', () => {
  it('provides', () => {
    expect(useGetStationGroupsByNamesQuery).toBeDefined();
    expect(useGetStationsQuery).toBeDefined();
    expect(useGetStationsWithChannelsQuery).toBeDefined();
    expect(convertRelativePositions).toBeDefined();
    expect(useGetStationsEffectiveAtTimesQuery).toBeDefined();
    expect(stationDefinitionSlice).toBeDefined();
  });

  it('hook queries for station groups by names', async () => {
    const useQuery = () => {
      const result = useGetStationGroupsByNamesQuery({
        effectiveTime: 100,
        stationGroupNames: ['test']
      });
      return <div>{JSON.stringify(result)}</div>;
    };

    await expectQueryHookToMakeAxiosRequest(useQuery);
  });

  it('hook queries stations', async () => {
    const useQuery = () => {
      const result = useGetStationsQuery({
        effectiveTime: 100,
        stationNames: ['test']
      });
      return <div>{JSON.stringify(result)}</div>;
    };

    await expectQueryHookToMakeAxiosRequest(useQuery);
  });

  it('hook queries for stations with channels', async () => {
    const useQuery = () => {
      const result = useGetStationsWithChannelsQuery({
        effectiveTime: 100,
        stationNames: ['test']
      });
      return <div>{JSON.stringify(result)}</div>;
    };

    await expectQueryHookToMakeAxiosRequest(useQuery);
  });

  it('hook queries for station effective at times', async () => {
    const useQuery = () => {
      const result = useGetStationsEffectiveAtTimesQuery({
        stationName: 'test',
        startTime: 100,
        endTime: 200
      });
      return <div>{JSON.stringify(result)}</div>;
    };

    await expectQueryHookToMakeAxiosRequest(useQuery);
  });

  it('hook queries for channels by names', async () => {
    const useQuery = () => {
      const result = useGetChannelsByNamesQuery({
        effectiveTime: 100,
        channelNames: ['test']
      });
      return <div>{JSON.stringify(result)}</div>;
    };

    await expectQueryHookToMakeAxiosRequest(useQuery);
  });

  it('has expected results for convertRelativePositions', () => {
    const response: any = [
      {
        relativePositionChannelPairs: [
          {
            relativePosition: {
              northDisplacementKm: 0.0,
              eastDisplacementKm: 0.0,
              verticalDisplacementKm: 0.0
            },
            channel: {
              name: 'ASAR.ASAR.SHZ',
              effectiveAt: null
            }
          },
          {
            relativePosition: {
              northDisplacementKm: -3.3641,
              eastDisplacementKm: 1.0124,
              verticalDisplacementKm: 0.0
            },
            channel: {
              name: 'ASAR.AS10.SHZ',
              effectiveAt: null
            }
          }
        ]
      }
    ];
    expect(convertRelativePositions(response)).toMatchSnapshot();
  });
});
