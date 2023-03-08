import { Units } from '../../src/ts/common/types';
import type { SignalDetection } from '../../src/ts/signal-detection';
import { FeatureMeasurementType } from '../../src/ts/signal-detection';
import { findArrivalTimeFeatureMeasurementUsingSignalDetection } from '../../src/ts/signal-detection/util';

const signalDetection: SignalDetection = {
  id: '012de1b9-8ae3-3fd4-800d-58665c3152cc',
  monitoringOrganization: 'GMS',
  signalDetectionHypotheses: [
    {
      id: {
        id: '20cc9505-efe3-3068-b7d5-59196f37992c',
        signalDetectionId: '012de1b9-8ae3-3fd4-800d-58665c3152cc'
      },
      parentSignalDetectionHypothesis: null,
      rejected: false,
      monitoringOrganization: 'GMS',
      featureMeasurements: [
        {
          channel: {
            name:
              'ASAR.beam.SHZ/beam,fk,coherent/steer,az_90.142deg,slow_7.122s_per_deg/06c0cb24-ab8f-3853-941d-bdf5e73a51b4',
            effectiveAt: 1636503404
          },
          measuredChannelSegment: {
            id: {
              channel: {
                name:
                  'ASAR.beam.SHZ/beam,fk,coherent/steer,az_90.142deg,slow_7.122s_per_deg/06c0cb24-ab8f-3853-941d-bdf5e73a51b4',
                effectiveAt: 1636503404
              },
              startTime: 1636503404,
              endTime: 1636503704,
              creationTime: 1636503404
            }
          },
          measurementValue: {
            arrivalTime: {
              value: 1636503404,
              standardDeviation: 1.162
            },
            travelTime: null
          },
          snr: {
            value: 8.9939442,
            standardDeviation: null,
            units: Units.DECIBELS
          },
          featureMeasurementType: FeatureMeasurementType.ARRIVAL_TIME
        }
      ]
    }
  ],
  station: {
    name: 'ASAR',
    effectiveAt: null
  }
};
describe('Common Model Signal Detection Utils Tests', () => {
  it('findArrivalTimeFeatureMeasurementUsingSignalDetection is defined', () => {
    expect(findArrivalTimeFeatureMeasurementUsingSignalDetection).toBeDefined();
  });

  it('findArrivalTimeFeatureMeasurementUsingSignalDetection return ArrivalTime FM', () => {
    expect(findArrivalTimeFeatureMeasurementUsingSignalDetection(signalDetection)).toBeDefined();
  });
});
