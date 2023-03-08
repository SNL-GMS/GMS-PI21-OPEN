import { WorkflowTypes } from '@gms/common-model';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';

import { AzimuthSlowness } from '../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/azimuth-slowness-component';
import type { AzimuthSlownessProps } from '../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/types';
import {
  eventId,
  signalDetectionsIds,
  timeInterval,
  useQueryStateResult
} from '../../../../__data__/test-util-data';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const processingAnalystConfigurationQuery = cloneDeep(useQueryStateResult);
processingAnalystConfigurationQuery.data = {
  defaultNetwork: 'demo',
  defaultInteractiveAnalysisStationGroup: 'ALL_1',
  defaultFilters: []
};
const azSlowReduxProps: Partial<AzimuthSlownessProps> = {
  location: undefined,
  processingAnalystConfigurationQuery,
  currentTimeInterval: timeInterval,
  selectedSdIds: signalDetectionsIds,
  openEventId: eventId,
  sdIdsToShowFk: [],
  analysisMode: WorkflowTypes.AnalysisMode.EVENT_REVIEW,
  computeFkFrequencyThumbnails: undefined,
  setSelectedSdIds: () => {
    /* no-op */
  },
  computeFks: undefined,
  setWindowLead: undefined,
  setSdIdsToShowFk: () => {
    /* no-op */
  },
  markFksReviewed: undefined
};

describe('AzimuthSlowness Direct', () => {
  test('AzimuthSlowness renders directly with data correctly', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<AzimuthSlowness {...(azSlowReduxProps as any)} />);
    expect(container).toMatchSnapshot();
  });
});
