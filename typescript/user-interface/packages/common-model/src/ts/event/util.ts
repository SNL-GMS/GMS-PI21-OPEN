import type { Event, EventHypothesis, LocationSolution, PreferredEventHypothesis } from './types';

/**
 * Finds preferred event hypothesis for a given event and stage
 * If no preferred hypothesis exists for stage, will return most recent preferred hypothesis by stage
 *
 * @param event
 * @param openIntervalName
 * @param stageNames
 */
export const findPreferredEventHypothesis = (
  event: Event,
  openIntervalName: string,
  stageNames: string[]
): EventHypothesis => {
  const { preferredEventHypothesisByStage, eventHypotheses } = event;
  let preferredEventHypothesis: PreferredEventHypothesis;
  // We want display the latest preferredEventHypothesis need to use the stageNames order
  // from the workflowQuery so that if the preferredEventHypothesisByStage is not present, will
  // try and grab the previous stage in the list e.x open 'AL1' but preferredEventHypothesisByStage only has
  // 'Auto Network' so will end up using 'Auto Network'
  const index = stageNames.indexOf(openIntervalName);
  for (let i = index; i > -1; i -= 1) {
    preferredEventHypothesis = preferredEventHypothesisByStage.find(
      hypothesis => hypothesis.stage.name === stageNames[i]
    );

    if (preferredEventHypothesis) {
      break;
    }
  }

  if (!preferredEventHypothesis) {
    return undefined;
  }

  return eventHypotheses.find(
    hypothesis => hypothesis.id.hypothesisId === preferredEventHypothesis.preferred.id.hypothesisId
  );
};

/**
 * Finds the last non rejected parent event hypothesis for a given event and hypothesis
 * if no valid hypothesis is found it returns the first hypothesis
 *
 * @param event
 * @param rejectedHypothesis
 */
export const findEventHypothesisParent = (
  event: Event,
  rejectedHypothesis: EventHypothesis
): EventHypothesis => {
  // Loop backwards until we find a non-rejected hypothesis
  for (let i = rejectedHypothesis.parentEventHypotheses.length - 1; i >= 0; i -= 1) {
    const parentEventHypothesis = event.eventHypotheses.find(
      hypo => hypo.id.hypothesisId === rejectedHypothesis.parentEventHypotheses[i].id.hypothesisId
    );
    if (parentEventHypothesis && !parentEventHypothesis.rejected) {
      return parentEventHypothesis;
    }
  }
  return rejectedHypothesis[0];
};

/**
 * Finds the preferred location solution for a hypothesis falling back to the parents if the hypothesis is rejected
 *
 * @param eventHypothesisId hypothesis id to find the solution for
 * @param eventHypotheses list of hypotheses
 * @returns a location solution
 */
export const findPreferredLocationSolution = (
  eventHypothesisId: string,
  eventHypotheses: EventHypothesis[]
): LocationSolution => {
  const eventHypothesis = eventHypotheses.find(
    hypothesis => hypothesis.id.hypothesisId === eventHypothesisId
  );
  if (eventHypothesis.preferredLocationSolution) {
    return eventHypothesis.locationSolutions.find(
      ls => ls.id === eventHypothesis.preferredLocationSolution.id
    );
  }

  if (eventHypothesis.parentEventHypotheses) {
    const parentHypothesisId =
      eventHypothesis.parentEventHypotheses[eventHypothesis.parentEventHypotheses.length - 1].id
        .hypothesisId;

    const parentEventHypothesis = eventHypotheses.find(
      hypothesis => hypothesis.id.hypothesisId === parentHypothesisId
    );

    return parentEventHypothesis.locationSolutions.find(
      ls => ls.id === parentEventHypothesis.preferredLocationSolution.id
    );
  }

  return undefined;
};
