/* eslint-disable jest/expect-expect */
import { EventTypes } from '@gms/common-model';
import type { SignalDetectionHypothesis } from '@gms/common-model/lib/signal-detection';
import produce from 'immer';

import type {
  EventStatus,
  EventStatusResponse,
  FindEventsByAssociatedSignalDetectionHypothesesProps,
  FindEventStatusInfoByStageIdAndEventIdsProps,
  PredictFeaturesForEventLocationProps,
  PredictFeaturesForLocationSolutionProps
} from '../../../../src/ts/app/api/event-manager';
import {
  eventManagerApiSlice,
  eventStatusTransform,
  updateEventStatus,
  useFindEventsByAssociatedSignalDetectionHypothesesQuery,
  usePredictFeaturesForEventLocationQuery,
  usePredictFeaturesForLocationSolutionQuery,
  useUpdateEventStatusMutation
} from '../../../../src/ts/app/api/event-manager';
import {
  expectQueryHookToMakeAxiosRequest,
  expectQueryHookToNotMakeAxiosRequest
} from '../query-test-util';
import { channels, locationSolution } from './event-data';

process.env.GMS_EVENT_QUERIES = 'true';

const sdHypothesis: SignalDetectionHypothesis = {
  id: {
    id: 'TEST1',
    signalDetectionId: 'testId'
  },
  featureMeasurements: [],
  monitoringOrganization: 'GMS',
  parentSignalDetectionHypothesis: null,
  rejected: false
};

describe('Event Manager API Slice', () => {
  it('provides', () => {
    expect(usePredictFeaturesForLocationSolutionQuery).toBeDefined();
    expect(useUpdateEventStatusMutation).toBeDefined();
    expect(updateEventStatus).toBeDefined();
    expect(useFindEventsByAssociatedSignalDetectionHypothesesQuery).toBeDefined();
    expect(eventManagerApiSlice).toBeDefined();
  });

  it('can updated event status', () => {
    const eventStatus: EventStatus = {
      stageId: { name: 'sample' },
      eventId: '123',
      eventStatusInfo: {
        eventStatus: EventTypes.EventStatus.COMPLETE,
        activeAnalystIds: ['user1', 'user2']
      }
    };
    const eventStatuses: Record<string, EventStatus> = {
      '123': {
        stageId: { name: 'sample' },
        eventId: '123',
        eventStatusInfo: {
          eventStatus: EventTypes.EventStatus.IN_PROGRESS,
          activeAnalystIds: ['user1', 'user2']
        }
      }
    };
    const result = produce(eventStatuses, updateEventStatus([eventStatus]));
    expect(Object.keys(result)).toHaveLength(1);
    expect(result['123'].eventStatusInfo.eventStatus).toEqual(EventTypes.EventStatus.COMPLETE);
  });
  describe('usePredictFeaturesForLocationSolutionQuery', () => {
    it('hook queries for feature prediction', async () => {
      const params: PredictFeaturesForLocationSolutionProps = {
        locationSolution,
        channels,
        phases: ['P']
      };
      const useTestHook = () => usePredictFeaturesForLocationSolutionQuery(params);

      await expectQueryHookToMakeAxiosRequest(useTestHook);
    });

    it('hook does not query with no location solution', async () => {
      const params: PredictFeaturesForLocationSolutionProps = {
        locationSolution: null,
        channels,
        phases: ['P']
      };
      const useTestHook = () => usePredictFeaturesForLocationSolutionQuery(params);
      await expectQueryHookToNotMakeAxiosRequest(useTestHook);
    });

    it('hook does not query with no channels', async () => {
      const params: PredictFeaturesForLocationSolutionProps = {
        locationSolution,
        channels: [],
        phases: ['P']
      };
      const useTestHook = () => usePredictFeaturesForLocationSolutionQuery(params);
      await expectQueryHookToNotMakeAxiosRequest(useTestHook);
    });
  });

  describe('usePredictFeaturesForEventLocationQuery', () => {
    const receivers = [
      {
        receiverBandType: 'B',
        receiverDataType: null,
        receiverLocationsByName: {
          'Canonical Name One': {
            depthKm: 100,
            elevationKm: 5500,
            latitudeDegrees: 35.45,
            longitudeDegrees: -125.2345
          },
          'Canonical Name Two': {
            depthKm: 100,
            elevationKm: 5500,
            latitudeDegrees: 35,
            longitudeDegrees: -125
          }
        }
      },
      {
        receiverBandType: null,
        receiverDataType: null,
        receiverLocationsByName: {
          STA: {
            depthKm: 50,
            elevationKm: 10,
            latitudeDegrees: 35.647,
            longitudeDegrees: 100
          }
        }
      }
    ];
    it('hook queries for feature prediction', async () => {
      const params: PredictFeaturesForEventLocationProps = {
        receivers,
        sourceLocation: locationSolution.location,
        phases: ['P']
      };
      const useTestHook = () => usePredictFeaturesForEventLocationQuery(params);

      await expectQueryHookToMakeAxiosRequest(useTestHook);
    });

    it('hook does not query with no source location', async () => {
      const params: PredictFeaturesForEventLocationProps = {
        receivers,
        sourceLocation: null,
        phases: ['P']
      };
      const useTestHook = () => usePredictFeaturesForEventLocationQuery(params);
      await expectQueryHookToNotMakeAxiosRequest(useTestHook);
    });

    it('hook does not query with no channels', async () => {
      const params: PredictFeaturesForEventLocationProps = {
        receivers: [],
        sourceLocation: locationSolution.location,
        phases: ['P']
      };
      const useTestHook = () => usePredictFeaturesForEventLocationQuery(params);
      await expectQueryHookToNotMakeAxiosRequest(useTestHook);
    });
  });

  it('hook queries for event statuses', async () => {
    const params: FindEventStatusInfoByStageIdAndEventIdsProps = {
      stageId: { name: 'TestStage' },
      eventIds: ['test ']
    };
    const useTestHook = () =>
      eventManagerApiSlice.useFindEventStatusInfoByStageIdAndEventIdsQuery(params);
    await expectQueryHookToMakeAxiosRequest(useTestHook);
  });
  it('transforms the event status initial response', () => {
    const mockResponse: EventStatusResponse = {
      eventStatusInfoMap: {
        testEvent: {
          eventStatus: EventTypes.EventStatus.NOT_STARTED,
          activeAnalystIds: ['larry', 'moe', 'curly']
        }
      },
      stageId: {
        name: 'testStage'
      }
    };
    const expectedResult: Record<string, EventStatus> = {
      testEvent: {
        stageId: { name: 'testStage' },
        eventId: 'testEvent',
        eventStatusInfo: {
          eventStatus: EventTypes.EventStatus.NOT_STARTED,
          activeAnalystIds: ['larry', 'moe', 'curly']
        }
      }
    };

    expect(eventStatusTransform(mockResponse)).toEqual(expectedResult);
  });

  it('useFindEventsByASDHQuery queries for events by assoc sd hypotheses', async () => {
    const params: FindEventsByAssociatedSignalDetectionHypothesesProps = {
      signalDetectionHypotheses: [sdHypothesis],
      stageId: { name: 'AL1' }
    };
    const useTestHook = () => useFindEventsByAssociatedSignalDetectionHypothesesQuery(params);
    await expectQueryHookToMakeAxiosRequest(useTestHook);
  });

  it('useFindEventsByASDHQuery does not query without hypotheses', async () => {
    const params: FindEventsByAssociatedSignalDetectionHypothesesProps = {
      signalDetectionHypotheses: [],
      stageId: { name: 'AL1' }
    };
    const useTestHook = () => useFindEventsByAssociatedSignalDetectionHypothesesQuery(params);
    await expectQueryHookToNotMakeAxiosRequest(useTestHook);
  });

  it('useFindEventsByASDHQuery does not query without valid stageId', async () => {
    const params: FindEventsByAssociatedSignalDetectionHypothesesProps = {
      signalDetectionHypotheses: [],
      stageId: null
    };
    const useTestHook = () => useFindEventsByAssociatedSignalDetectionHypothesesQuery(params);
    await expectQueryHookToNotMakeAxiosRequest(useTestHook);
  });
});
