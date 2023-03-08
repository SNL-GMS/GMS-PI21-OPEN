import type { EventTypes } from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import { PhaseType, Units } from '@gms/common-model/lib/common/types';
import type { ArrivalTimeMeasurementValue } from '@gms/common-model/lib/signal-detection';
import type { ReceiverLocationResponse } from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import { random, seed } from 'faker';
import cloneDeep from 'lodash/cloneDeep';

import {
  calculateOffsetsObservedPhase,
  calculateOffsetsPredictedPhase
} from '../../../../../src/ts/components/analyst-ui/components/waveform/utils';
import { eventData } from '../../../../__data__/event-data';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const numberOfFeaturePredictions = random.number({ min: 10, max: 12 });
const fakerWaveformDisplaySeed = 123;
seed(fakerWaveformDisplaySeed);

function* MockFeaturePrediction() {
  while (true) {
    const predictedValue: ArrivalTimeMeasurementValue = {
      arrivalTime: {
        value: random.number({ min: 1, max: 20 }),
        standardDeviation: 0
      },
      travelTime: { value: 0, standardDeviation: 0, units: Units.SECONDS }
    };
    const predictionValue = {
      featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME,
      predictedValue,
      featurePredictionComponentSet: []
    };
    const predictionType = SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME;
    const phase = PhaseType.P;
    const featurePrediction: EventTypes.FeaturePrediction = {
      predictionValue,
      predictionType,
      phase,
      sourceLocation: undefined,
      receiverLocation: undefined,
      predictionChannelSegment: undefined
    };
    yield featurePrediction;
  }
}

const mockFeaturePrediction = MockFeaturePrediction();

const mockFeaturePredictionGenerator: () => EventTypes.FeaturePrediction = (() => () =>
  mockFeaturePrediction.next().value as EventTypes.FeaturePrediction)();

const mockFeaturePredictions: Record<string, ReceiverLocationResponse> = {};

for (let i = 0; i < numberOfFeaturePredictions; i += 1) {
  mockFeaturePredictions[i] = { featurePredictions: [mockFeaturePredictionGenerator()] };
}

describe('Waveform Display Utility Test', () => {
  describe('Calculate Offsets', () => {
    test('calculateOffsetsPredictedPhase should return a list of offsets', () => {
      const offsets = calculateOffsetsPredictedPhase(mockFeaturePredictions, '0', 'P');
      expect(offsets).toHaveLength(numberOfFeaturePredictions);
    });

    test('calculateOffsetsObservedPhase should return a list of offsets', () => {
      const associatedEvent = cloneDeep(eventData);

      // Associate the detection to the event
      associatedEvent.eventHypotheses[0].associatedSignalDetectionHypotheses.push(
        signalDetectionsData[0].signalDetectionHypotheses[0]
      );

      const offsets = calculateOffsetsObservedPhase(
        signalDetectionsData,
        mockFeaturePredictions,
        'ASAR',
        [associatedEvent],
        associatedEvent.id,
        'P'
      );
      expect(offsets).toHaveLength(numberOfFeaturePredictions + 1);
    });
  });
});
