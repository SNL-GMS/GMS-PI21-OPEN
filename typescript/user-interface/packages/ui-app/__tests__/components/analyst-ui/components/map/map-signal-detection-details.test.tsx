import React from 'react';
import renderer from 'react-test-renderer';

import { messageConfig } from '~analyst-ui/config/message-config';

import { EdgeTypes } from '../../../../../src/ts/components/analyst-ui/components/events/types';
import {
  getSdDetailDisplayValues,
  getSdDetailFormItems,
  getSwatchTooltipText,
  MapSignalDetectionDetails
} from '../../../../../src/ts/components/analyst-ui/components/map/map-signal-detection-details';
import type { MapSDFormValues } from '../../../../../src/ts/components/analyst-ui/components/map/types';
import { mockEmptySd, mockSd } from './map-sd-mock-data';

describe('MapSignalDetectionDetails', () => {
  test('functions are defined', () => {
    expect(MapSignalDetectionDetails).toBeDefined();
    expect(getSwatchTooltipText).toBeDefined();
    expect(getSdDetailFormItems).toBeDefined();
    expect(getSdDetailDisplayValues).toBeDefined();
  });

  it('matches snapshot', () => {
    const component = renderer.create(<MapSignalDetectionDetails sd={mockSd} />).toJSON();
    expect(component).toMatchSnapshot();
  });

  it('renders with undefined values', () => {
    const component = renderer.create(<MapSignalDetectionDetails sd={mockEmptySd} />).toJSON();
    expect(component).toMatchSnapshot();
  });

  test('getSwatchTooltipText handles complete and before', () => {
    const result = getSwatchTooltipText('Associated to completed event', EdgeTypes.BEFORE);
    expect(result).toEqual('Completed event, before interval');
  });

  test('getSwatchTooltipText handles open and after', () => {
    const result = getSwatchTooltipText('Associated to open event', EdgeTypes.AFTER);
    expect(result).toEqual('Open event, after interval');
  });

  test('getSwatchTooltipText handles other and within', () => {
    const result = getSwatchTooltipText('Associated to other event', EdgeTypes.INTERVAL);
    expect(result).toEqual('Other event, within interval');
  });

  test('getSwatchTooltipText handles unassociated and within', () => {
    const result = getSwatchTooltipText('Unassociated', EdgeTypes.INTERVAL);
    expect(result).toEqual('Unassociated to event, within interval');
  });

  test('getSdDetailDisplayValues handles undefined values', () => {
    const sd: any = {
      detectionTime: {
        detectionTimeValue: undefined,
        detectionTimeUncertainty: undefined
      },
      azimuth: {
        azimuthValue: undefined,
        azimuthUncertainty: undefined
      },
      slowness: {
        slownessValue: undefined,
        slownessUncertainty: undefined
      },
      phaseValue: {
        confidence: undefined,
        referenceTime: undefined,
        value: undefined
      },
      associatedEventTimeValue: '123',
      signalDetectionColor: undefined,
      status: undefined,
      edgeSDType: undefined,
      stationName: undefined
    };
    const expected: MapSDFormValues = {
      phaseValue: messageConfig.invalidCellText,
      stationName: messageConfig.invalidCellText,
      detectionTimeValue: messageConfig.invalidCellText,
      detectionTimeUncertainty: messageConfig.invalidCellText,
      azimuthValue: messageConfig.invalidCellText,
      azimuthUncertainty: messageConfig.invalidCellText,
      slownessValue: messageConfig.invalidCellText,
      slownessUncertainty: messageConfig.invalidCellText,
      status: '123'
    };
    expect(getSdDetailDisplayValues(sd)).toEqual(expected);
  });
});
