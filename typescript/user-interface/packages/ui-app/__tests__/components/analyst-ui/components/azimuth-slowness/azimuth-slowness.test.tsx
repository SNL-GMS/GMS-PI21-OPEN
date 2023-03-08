import { WorkflowTypes } from '@gms/common-model';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import renderer from 'react-test-renderer';

import { AzimuthSlowness } from '../../../../../src/ts/components/analyst-ui/components/azimuth-slowness';
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
  setSdIdsToShowFk: () => {
    /* no-op */
  },
  computeFks: undefined,
  setWindowLead: undefined,
  markFksReviewed: undefined
};

it('AzimuthSlowness renders & matches snapshot', () => {
  const tree = renderer
    .create(
      <div
        style={{
          border: `1px solid #111`,
          resize: 'both',
          overflow: 'auto',
          height: '700px',
          width: '1000px'
        }}
      >
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <AzimuthSlowness {...(azSlowReduxProps as any)} />
      </div>
    )
    .toJSON();

  expect(tree).toMatchSnapshot();
});
