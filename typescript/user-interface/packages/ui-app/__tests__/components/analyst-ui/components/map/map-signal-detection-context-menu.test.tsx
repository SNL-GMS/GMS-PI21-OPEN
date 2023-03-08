import React from 'react';
import renderer from 'react-test-renderer';

import {
  MapSignalDetectionContextMenu,
  showMapSdDetailsPopover
} from '../../../../../src/ts/components/analyst-ui/components/map/map-signal-detection-context-menu';
import { mockEmptySd, mockSd } from './map-sd-mock-data';

describe('MapSignalDetectionContextMenu', () => {
  const mockLeft = 800;
  const mockTop = 800;
  test('functions are defined', () => {
    expect(MapSignalDetectionContextMenu).toBeDefined();
  });

  it('matches snapshot', () => {
    const component = renderer
      .create(<MapSignalDetectionContextMenu sd={mockSd} left={mockLeft} top={mockTop} />)
      .toJSON();
    expect(component).toMatchSnapshot();
  });

  test('showMapSdDetailsPopover matches snapshot', () => {
    expect(showMapSdDetailsPopover(mockSd, mockLeft, mockTop)).toMatchSnapshot();
  });

  it('renders with undefined values', () => {
    const component = renderer
      .create(<MapSignalDetectionContextMenu sd={mockEmptySd} left={mockLeft} top={mockTop} />)
      .toJSON();
    expect(component).toMatchSnapshot();
  });
});
