/* eslint-disable react/function-component-definition */
/* eslint-disable jest/expect-expect */
import type { NonIdealStateDefinition } from '@gms/ui-core-components';
import { WithNonIdealStates } from '@gms/ui-core-components';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import {
  channelSohNonIdealStateDefinitions,
  stationSelectedSohNonIdealStateDefinitions
} from '../../../../../src/ts/components/data-acquisition-ui/shared/non-ideal-states/non-ideal-state-defs';
import { testStationSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

const sohStatus = {
  lastUpdated: 1,
  loading: false,
  error: undefined,
  stationAndStationGroupSoh: {
    isUpdateResponse: false,
    stationGroups: [],
    stationSoh: [testStationSoh]
  }
};

const props = {
  sohStatus,
  selectedStationIds: ['Test', 'H06N']
};

const testForLoading = (defs: NonIdealStateDefinition<unknown>[], key: string) => {
  const TestComponent: React.FC = () => <div>Test</div>;
  const WrappedComponent = WithNonIdealStates<any>(defs, TestComponent);

  props[key] = { isLoading: true, isError: false };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const loadingWrapper = Enzyme.mount(<WrappedComponent {...props} />);
  expect(loadingWrapper.containsMatchingElement(<TestComponent />)).toBeDefined();
};

describe('Non ideal state definitions', () => {
  it('selected station non ideal to be defined', () => {
    expect(stationSelectedSohNonIdealStateDefinitions).toBeDefined();
  });
  it('renders non ideal states for selected station', () => {
    testForLoading(
      stationSelectedSohNonIdealStateDefinitions,
      'stationSelectedSohNonIdealStateDefinitions'
    );
  });
  it('renders non ideal states for channel soh', () => {
    testForLoading(channelSohNonIdealStateDefinitions, 'channelSohNonIdealStateDefinitions');
  });
});
