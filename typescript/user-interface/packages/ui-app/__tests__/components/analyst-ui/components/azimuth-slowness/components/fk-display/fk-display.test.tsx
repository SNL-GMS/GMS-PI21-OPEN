import React from 'react';
import renderer from 'react-test-renderer';

import type { FkDisplayProps } from '../../../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/components/fk-display/fk-display';
import { FkDisplay } from '../../../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/components/fk-display/fk-display';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const fkDisplayProps: Partial<FkDisplayProps> = {
  defaultStations: undefined,
  defaultWaveformFilters: undefined,
  eventsInTimeRange: undefined,
  currentOpenEvent: undefined,
  signalDetectionsByStation: undefined,
  signalDetection: undefined,
  signalDetectionFeaturePredictions: undefined,
  widthPx: 100,
  heightPx: 90,
  multipleSelected: true,
  anySelected: true,
  userInputFkWindowParameters: undefined,
  userInputFkFrequency: undefined,
  numberOfOutstandingComputeFkMutations: 5,
  fkUnit: undefined,
  fkFrequencyThumbnails: undefined,
  currentMovieSpectrumIndex: 4,
  arrivalTimeMovieSpectrumIndex: 6,
  channelFilters: undefined,
  defaultSignalDetectionPhase: undefined
};

it('FkDisplay renders & matches snapshot', () => {
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
        <FkDisplay {...(fkDisplayProps as any)} />
      </div>
    )
    .toJSON();

  expect(tree).toMatchSnapshot();
});
