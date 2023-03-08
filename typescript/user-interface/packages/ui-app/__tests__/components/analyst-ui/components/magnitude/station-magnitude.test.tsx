import { CommonTypes, LegacyEventTypes } from '@gms/common-model';
import { render } from '@testing-library/react';
import React from 'react';

import {
  StationMagnitude
  // eslint-disable-next-line max-len
} from '../../../../../src/ts/components/analyst-ui/components/magnitude/components/station-magnitude/station-magnitude-component';
import type {
  StationMagnitudeProps,
  StationMagnitudeSdData,
  StationMagnitudeState
} from '../../../../../src/ts/components/analyst-ui/components/magnitude/components/station-magnitude/types';
import { testMagTypes } from '../../../../__data__/test-util-data';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

const locationSolution: LegacyEventTypes.LocationSolution = {
  id: '123',
  locationType: 'type',
  locationToStationDistances: [
    {
      azimuth: 1,
      distance: {
        degrees: 1,
        km: 2
      },
      stationId: '1'
    }
  ],
  location: {
    latitudeDegrees: 1,
    longitudeDegrees: 1,
    depthKm: 1,
    time: 1
  },
  featurePredictions: undefined,
  locationRestraint: undefined,
  locationBehaviors: undefined,
  networkMagnitudeSolutions: [
    {
      uncertainty: 1,
      magnitudeType: LegacyEventTypes.MagnitudeType.MB,
      magnitude: 1,
      networkMagnitudeBehaviors: []
    },
    {
      uncertainty: 1,
      magnitudeType: LegacyEventTypes.MagnitudeType.MS,
      magnitude: 1,
      networkMagnitudeBehaviors: []
    },
    {
      uncertainty: 1,
      magnitudeType: LegacyEventTypes.MagnitudeType.MBMLE,
      magnitude: 1,
      networkMagnitudeBehaviors: []
    },
    {
      uncertainty: 1,
      magnitudeType: LegacyEventTypes.MagnitudeType.MSMLE,
      magnitude: 1,
      networkMagnitudeBehaviors: []
    }
  ],
  snapshots: undefined
};

function flushPromises(): any {
  return new Promise(resolve => {
    setTimeout(resolve, 0);
  });
}

describe('when loading station Magnitude', () => {
  const validSignalDetectionForMagnitude = new Map<any, boolean>([
    [LegacyEventTypes.MagnitudeType.MB, true]
  ]);
  const magTypeToAmplitudeMap = new Map<any, StationMagnitudeSdData>([
    [
      LegacyEventTypes.MagnitudeType.MB,
      {
        channel: 'bar',
        phase: CommonTypes.PhaseType.P,
        amplitudeValue: 1,
        amplitudePeriod: 1,
        signalDetectionId: '1',
        time: 1,
        stationName: '1',
        flagForReview: false
      }
    ]
  ]);

  const lazyloadStub: StationMagnitudeProps = {
    checkBoxCallback: jest.fn(),
    setSelectedSdIds: jest.fn(),
    selectedSdIds: ['123'],
    historicalMode: false,
    locationSolution,
    computeNetworkMagnitudeSolution: undefined,
    displayedMagnitudeTypes: testMagTypes,
    amplitudesByStation: [
      {
        stationName: 'foo',
        validSignalDetectionForMagnitude,
        magTypeToAmplitudeMap
      }
    ],
    openEventId: ''
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const wrapper = Enzyme.mount(<StationMagnitude {...lazyloadStub} />);

  it.skip('Network station magnitude table renders', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<StationMagnitude {...lazyloadStub} />);
    expect(container).toMatchSnapshot();
  });

  it.skip('we populate props', () => {
    const props = wrapper.props() as StationMagnitudeProps;
    expect(props).toMatchSnapshot();
  });

  it.skip('we have null initial state', () => {
    const state = wrapper.state() as StationMagnitudeState;
    expect(state).toMatchSnapshot();
  });

  it.skip('we have a onCellClicked function click handler for selecting mb and ms rows', () => {
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'onCellClicked');
    instance.mainTable = {};
    instance.mainTable.getSelectedNodes = jest.fn();
    instance.mainTable.getRenderedNodes = jest.fn(() => [
      { data: { mbSignalDetectionId: '123' }, rowIndex: 1 }
    ]);
    // null call
    instance.onCellClicked(null);
    flushPromises();
    expect(spy).toHaveBeenCalledWith(null);

    // empty call
    instance.onCellClicked({});
    flushPromises();
    expect(spy).toHaveBeenCalledWith({});

    // call with good args
    const defaultClickSpy = jest.spyOn(instance, 'onDefaultClick');
    instance.onCellClicked({
      node: undefined,
      event: {},
      data: { mbSignalDetectionIdevent: '123' },
      column: { colId: 'mbChannel' }
    });
    flushPromises();
    expect(defaultClickSpy).toHaveBeenCalled();
    expect(spy).toHaveBeenCalledWith({
      node: undefined,
      event: {},
      data: { mbSignalDetectionIdevent: '123' },
      column: { colId: 'mbChannel' }
    });

    // called with shiftkey
    const shiftClickSpy = jest.spyOn(instance, 'onShiftClick');
    instance.onCellClicked({
      node: { rowIndex: 1 },
      event: { shiftKey: true },
      data: { mbSignalDetectionIdevent: '123' },
      column: { colId: 'mbChannel' }
    });
    flushPromises();
    expect(shiftClickSpy).toHaveBeenCalled();
    expect(spy).toHaveBeenCalledWith({
      node: { rowIndex: 1 },
      event: { shiftKey: true },
      data: { mbSignalDetectionIdevent: '123' },
      column: { colId: 'mbChannel' }
    });

    // called with ctrlKey/metaKey
    const onControlClickSpy = jest.spyOn(instance, 'onControlClick');
    instance.onCellClicked({
      event: { ctrlKey: true },
      data: { mbSignalDetectionIdevent: '123' },
      column: { colId: 'mbChannel' }
    });
    flushPromises();
    expect(onControlClickSpy).toHaveBeenCalled();
    expect(spy).toHaveBeenCalledWith({
      event: { ctrlKey: true },
      data: { mbSignalDetectionIdevent: '123' },
      column: { colId: 'mbChannel' }
    });
  });

  it.skip('we can getSelectedMbRowIndices', () => {
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'getSelectedMbRowIndices');
    instance.mainTable = {};
    const node = { data: { mbSignalDetectionId: '123' }, rowIndex: 1 };
    instance.mainTable.getRenderedNodes = jest.fn(() => [node]);
    const result = instance.getSelectedMbRowIndices();
    flushPromises();
    expect(result).toEqual([1]);
    expect(spy).toHaveBeenCalled();
  });

  it.skip('we can getSelectedMsRowIndices', () => {
    const instance = wrapper.instance();
    const spy = jest.spyOn(instance, 'getSelectedMsRowIndices');
    instance.mainTable = {};
    const node = { data: { msSignalDetectionId: '123' }, rowIndex: 1 };
    instance.mainTable.getRenderedNodes = jest.fn(() => [node]);
    const result = instance.getSelectedMsRowIndices();
    flushPromises();
    expect(result).toEqual([1]);
    expect(spy).toHaveBeenCalled();
  });
});
