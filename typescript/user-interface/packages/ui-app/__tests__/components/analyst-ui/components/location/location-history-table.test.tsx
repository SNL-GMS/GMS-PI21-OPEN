import { render } from '@testing-library/react';
import React from 'react';

import { EventUtils } from '../../../../../src/ts/components/analyst-ui/common/utils';
import { LocationHistory } from '../../../../../src/ts/components/analyst-ui/components/location/components/location-history';
import * as LocationData from '../../../../__data__/location-data';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

it('Location History Table Renders a single location entry', () => {
  const { container } = render(
    <LocationHistory
      event={LocationData.event}
      location={{
        selectedLocationSolutionSetId: undefined,
        selectedLocationSolutionId: undefined,
        selectedPreferredLocationSolutionSetId: undefined,
        selectedPreferredLocationSolutionId: undefined
      }}
      setSelectedLocationSolution={() => {
        /* no-op */
      }}
      setSelectedPreferredLocationSolution={() => {
        /* no-o */
      }}
    />
  );
  expect(container).toMatchSnapshot();
});

// Unit test for checking that the correct, configured preferred restraints is picked
describe('When location is set to save, correct preferred location is set', () => {
  it('The osd event hypothesis should match expected result', () => {
    const preferredId = EventUtils.getPreferredLocationSolutionIdFromEventHypothesisLegacy(
      LocationData.eventHypothesisWithLocationSets
    );
    const hardCodedCorrectId = '3a06fac7-46ad-337e-a8da-090a1cc801a1';
    expect(preferredId).toEqual(hardCodedCorrectId);
  });
});
