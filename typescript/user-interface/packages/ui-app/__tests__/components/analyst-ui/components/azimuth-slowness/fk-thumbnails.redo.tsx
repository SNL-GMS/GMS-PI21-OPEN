import type { LegacyEventTypes } from '@gms/common-model';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import type { ReactWrapper } from 'enzyme';
import Immutable from 'immutable';
import React from 'react';

import type { FkThumbnailListProps } from '../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/components/fk-thumbnail-list/fk-thumbnail-list';
import { FkThumbnailList } from '../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/components/fk-thumbnail-list/fk-thumbnail-list';
import type { FkUnits } from '../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/types';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');
// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
/* eslint-disable import/first */
/* eslint-disable import/no-extraneous-dependencies */
import * as util from 'util';

Object.defineProperty(window, 'TextEncoder', {
  writable: true,
  value: util.TextEncoder
});
Object.defineProperty(window, 'TextDecoder', {
  writable: true,
  value: util.TextDecoder
});
Object.defineProperty(global, 'TextEncoder', {
  writable: true,
  value: util.TextEncoder
});
Object.defineProperty(global, 'TextDecoder', {
  writable: true,
  value: util.TextDecoder
});

import Adapter from '@cfaester/enzyme-adapter-react-18';

const mockProps: Partial<FkThumbnailListProps> = {
  sortedSignalDetections: signalDetectionsData,
  signalDetectionIdsToFeaturePrediction: Immutable.Map<
    string,
    LegacyEventTypes.FeaturePrediction[]
  >(),
  thumbnailSizePx: 300,
  selectedSdIds: [],
  unassociatedSdIds: [],
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>(),
  clearSelectedUnassociatedFks: () => {
    /** empty */
  },
  markFksForSdIdsAsReviewed: () => {
    /** empty */
  },
  showFkThumbnailContextMenu: () => {
    /** empty */
  },
  setSelectedSdIds: () => {
    /** empty */
  }
};

// TODO the file name .redo. makes the tests skip everything in here, the tests need to be redone
describe('FK thumbnails tests', () => {
  // enzyme needs a new adapter for each configuration
  beforeEach(() => {
    Enzyme.configure({ adapter: new Adapter() });
  });

  // eslint-disable-next-line jest/no-done-callback
  it('renders a snapshot', (done: jest.DoneCallback) => {
    // Mounting enzyme into the DOM
    // Using a testing DOM not real DOM
    // So a few things will be missing window.fetch, or alert etc...
    const wrapper: ReactWrapper = Enzyme.mount(
      // eslint-disable-next-line react/jsx-props-no-spreading
      <FkThumbnailList {...(mockProps as any)} />
    );

    setTimeout(() => {
      wrapper.update();

      expect(wrapper.find(FkThumbnailList)).toMatchSnapshot();

      done();
    }, 0);
  });
});
