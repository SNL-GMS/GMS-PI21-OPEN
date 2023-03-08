/* eslint-disable @typescript-eslint/no-magic-numbers */
import { SignalDetectionTypes } from '@gms/common-model';

export const minimalSignalDetectionsWithArrivalTimes = [
  {
    id: '4444',
    signalDetectionHypotheses: [
      {
        featureMeasurements: [
          {
            featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME,
            measurementValue: {
              arrivalTime: {
                value: '4444',
                standardDeviation: 1.162
              },
              travelTime: null
            }
          }
        ]
      }
    ]
  },
  {
    id: '55556',
    signalDetectionHypotheses: [
      {
        featureMeasurements: [
          {
            featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME,
            measurementValue: {
              arrivalTime: {
                value: '55556',
                standardDeviation: 1.162
              },
              travelTime: null
            }
          }
        ]
      }
    ]
  },
  {
    id: '7777777',
    signalDetectionHypotheses: [
      {
        featureMeasurements: [
          {
            featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME,
            measurementValue: {
              arrivalTime: {
                value: '7777777',
                standardDeviation: 1.162
              },
              travelTime: null
            }
          }
        ]
      }
    ]
  }
];
