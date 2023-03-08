/* eslint-disable jest/expect-expect */
import type { NonIdealStateDefinition } from '@gms/ui-core-components';
import { WithNonIdealStates } from '@gms/ui-core-components';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { AnalystNonIdealStates } from '../../../../../src/ts/components/analyst-ui/common/non-ideal-states';

const testForLoading = (def: NonIdealStateDefinition<any>, key: string) => {
  function TestComponent() {
    return <div>Test</div>;
  }
  const WrappedComponent = WithNonIdealStates<any>([def], TestComponent);
  const props = {};

  props[key] = { isLoading: true };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const loadingWrapper = Enzyme.mount(<WrappedComponent {...props} />);
  expect(loadingWrapper.containsMatchingElement(<TestComponent />)).toEqual(false);
  // index 0 is the loading case
  expect(loadingWrapper.containsMatchingElement(def.element)).toEqual(true);
};

const testForParallelLoadingWait = (def: NonIdealStateDefinition<any>, key: string) => {
  function TestComponent() {
    return <div>Test</div>;
  }
  const WrappedComponent = WithNonIdealStates<any>([def], TestComponent);
  const props = {};

  props[key] = { pending: 1, fulfilled: 0 };
  // eslint-disable-next-line react/jsx-props-no-spreading
  let loadingWrapper = Enzyme.mount(<WrappedComponent {...props} />);
  expect(loadingWrapper.containsMatchingElement(<TestComponent />)).toEqual(false);
  // index 0 is the loading case
  expect(loadingWrapper.containsMatchingElement(def.element)).toEqual(true);

  props[key] = { pending: 0, fulfilled: 0 };
  // eslint-disable-next-line react/jsx-props-no-spreading
  loadingWrapper = Enzyme.mount(<WrappedComponent {...props} />);
  expect(loadingWrapper.containsMatchingElement(<TestComponent />)).toEqual(false);
  // index 0 is the loading case
  expect(loadingWrapper.containsMatchingElement(def.element)).toEqual(true);
};

const testForError = (def: NonIdealStateDefinition<any>, key: string) => {
  function TestComponent() {
    return <div>Test</div>;
  }
  const WrappedComponent = WithNonIdealStates<any>([def], TestComponent);
  const props = {};

  props[key] = { isError: true };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const errorWrapper = Enzyme.mount(<WrappedComponent {...props} />);
  expect(errorWrapper.containsMatchingElement(<TestComponent />)).toEqual(false);
  // index 1 is the error case
  expect(errorWrapper.containsMatchingElement(def.element)).toEqual(true);
};

describe('Non ideal state definitions', () => {
  it('renders non ideal states for loading and error processing analyst configuration', () => {
    testForLoading(
      AnalystNonIdealStates.processingAnalystConfigNonIdealStateDefinitions[0],
      'processingAnalystConfigurationQuery'
    );
    testForError(
      AnalystNonIdealStates.processingAnalystConfigNonIdealStateDefinitions[1],
      'processingAnalystConfigurationQuery'
    );
  });

  it('renders non ideal states for loading and error events', () => {
    testForLoading(AnalystNonIdealStates.eventNonIdealStateDefinitions[0], 'eventResults');
    testForParallelLoadingWait(
      AnalystNonIdealStates.eventNonIdealStateDefinitions[0],
      'eventResults'
    );
    testForError(AnalystNonIdealStates.eventNonIdealStateDefinitions[1], 'eventResults');
  });

  it('renders non ideal states for loading and error signal detections', () => {
    testForError(
      AnalystNonIdealStates.signalDetectionsNonIdealStateDefinitions[0],
      'signalDetectionResults'
    );
  });

  it('renders non ideal states for loading and error station definitions', () => {
    testForLoading(
      AnalystNonIdealStates.stationDefinitionNonIdealStateDefinitions[0],
      'stationsQuery'
    );
    testForError(
      AnalystNonIdealStates.stationDefinitionNonIdealStateDefinitions[1],
      'stationsQuery'
    );
  });
});
