import type { Event, EventHypothesis, LocationSolution } from '../../../src/ts/event';
import {
  findEventHypothesisParent,
  findPreferredEventHypothesis,
  findPreferredLocationSolution
} from '../../../src/ts/event';

const locationSolution: LocationSolution = {
  id: 'locationSolutionId',
  networkMagnitudeSolutions: [],
  featurePredictions: { featurePredictions: [] },
  locationBehaviors: [],
  location: undefined,
  locationRestraint: undefined,
  name: 'location solution'
};

const eventHypothesis1: EventHypothesis = {
  id: { eventId: 'event id', hypothesisId: 'Hypo1' },
  rejected: false,
  parentEventHypotheses: [],
  associatedSignalDetectionHypotheses: [],
  locationSolutions: [locationSolution],
  preferredLocationSolution: { id: 'locationSolutionId' },
  name: 'event hypo 1'
};

const eventHypothesis2: EventHypothesis = {
  id: { eventId: 'event id', hypothesisId: 'Hypo2' },
  rejected: true,
  parentEventHypotheses: [eventHypothesis1],
  associatedSignalDetectionHypotheses: [],
  locationSolutions: [],
  name: ''
};
const event: Event = {
  id: 'event id',
  rejectedSignalDetectionAssociations: [],
  monitoringOrganization: '',
  eventHypotheses: [eventHypothesis1, eventHypothesis2],
  preferredEventHypothesisByStage: [
    {
      preferred: eventHypothesis2,
      preferredBy: 'test user',
      stage: { name: 'AL1', effectiveTime: 0 }
    }
  ],
  finalEventHypothesisHistory: [],
  name: ''
};

describe('Event Util', () => {
  it('findPreferredLocationSolution finds the preferred location solution if it exists', () => {
    expect(findPreferredLocationSolution('Hypo1', [eventHypothesis1, eventHypothesis2])).toEqual(
      locationSolution
    );
  });

  it('findPreferredLocationSolution finds the parents preferred location solution if the hypothesis is rejected', () => {
    expect(findPreferredLocationSolution('Hypo2', [eventHypothesis1, eventHypothesis2])).toEqual(
      locationSolution
    );
  });

  it('findPreferredEventHypothesis finds the preferred event hypothesis solution if it exists', () => {
    expect(findPreferredEventHypothesis(event, 'AL1', ['AL1'])).toEqual(eventHypothesis2);
  });

  it('findEventHypothesisParent finds the non rejected parent', () => {
    expect(findEventHypothesisParent(event, eventHypothesis2)).toEqual(eventHypothesis1);
  });
  it('findEventHypothesisParent returns undefined if no valid parent exists', () => {
    expect(findEventHypothesisParent(event, eventHypothesis1)).toEqual(undefined);
  });
});
