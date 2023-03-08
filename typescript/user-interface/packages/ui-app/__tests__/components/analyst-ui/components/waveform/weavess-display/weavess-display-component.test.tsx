/* eslint-disable react/jsx-no-constructed-context-values */
/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { ContextMenu } from '@blueprintjs/core';
import { CommonTypes, WorkflowTypes } from '@gms/common-model';
import { toEpochSeconds } from '@gms/common-util';
import {
  AnalystWorkspaceOperations,
  AnalystWorkspaceTypes,
  defaultTheme,
  getStore,
  setOpenInterval
} from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import type { WeavessTypes } from '@gms/weavess-core';
import { LineStyle } from '@gms/weavess-core/lib/types';
import { render } from '@testing-library/react';
import { mount } from 'enzyme';
import * as React from 'react';
import { Provider } from 'react-redux';
import * as toastify from 'react-toastify';

import { AmplitudeScalingOptions } from '../../../../../../src/ts/components/analyst-ui/components/waveform/components/waveform-controls/scaling-options';
import { WeavessContext } from '../../../../../../src/ts/components/analyst-ui/components/waveform/weavess-context';
import { WeavessDisplay } from '../../../../../../src/ts/components/analyst-ui/components/waveform/weavess-display';
import type { WeavessDisplayProps } from '../../../../../../src/ts/components/analyst-ui/components/waveform/weavess-display/types';
import {
  QcMaskType,
  systemConfig
} from '../../../../../../src/ts/components/analyst-ui/config/system-config';
import { BaseDisplayContext } from '../../../../../../src/ts/components/common-ui/components/base-display';
import { event } from '../../events/event-data-types';

jest.mock('worker-rpc', () => ({
  RpcProvider: jest.fn().mockImplementation(() => {
    const mockRpc = jest.fn(async () => {
      return new Promise(resolve => {
        resolve([]);
      });
    });
    return { rpc: mockRpc };
  })
}));

jest.mock('@gms/ui-state', () => {
  const actualImport = jest.requireActual('@gms/ui-state');
  return {
    ...actualImport,
    getBoundaries: jest.fn(() => ({
      topMax: 100,
      bottomMax: -100,
      channelAvg: 0,
      offset: 100,
      channelSegmentId: 'TEST',
      samplesCount: 100
    }))
  };
});

const contextMenuMock = {
  show: jest.fn()
};
Object.assign(ContextMenu, contextMenuMock);

const initialConfiguration: WeavessTypes.Configuration = {
  shouldRenderWaveforms: true,
  shouldRenderSpectrograms: false,
  hotKeys: {
    amplitudeScale: 'KeyY',
    amplitudeScaleSingleReset: 'Alt+KeyY',
    amplitudeScaleReset: 'Alt+Shift+KeyY',
    maskCreate: 'KeyM'
  },
  waveformDimPercent: 0.75,
  backgroundColor: '#182026',
  defaultChannel: {
    disableMeasureWindow: true,
    disableMaskModification: true
  },
  nonDefaultChannel: {
    disableMeasureWindow: true,
    disableMaskModification: false
  }
};
const startTimeSecs = toEpochSeconds('2010-05-20T22:00:00.000Z');
const endTimeSecs = toEpochSeconds('2010-05-20T23:59:59.000Z');
const viewableInterval = {
  startTimeSecs,
  endTimeSecs
};
const qcMaskVersion = {
  category: 'REJECTED',
  channelSegmentIds: [],
  startTime: 0,
  endTime: 1,
  rationale: 'just because',
  type: QcMaskType.SPIKE,
  version: 'version'
};

let updateMockSdIds;
const mockSetSelectedSdIds = jest.fn((selectedIds: string[]) => {
  updateMockSdIds(selectedIds);
});

// Add a mock function to set selectedStationIds for testing
let updateMockStationIds;
const mockSetSelectedStationIds = jest.fn((selectedIds: string[]) => {
  updateMockStationIds(selectedIds);
});

const timeRange: CommonTypes.TimeRange = {
  startTimeSecs,
  endTimeSecs
};

const mockCreateSignalDetection = jest.fn(async () => Promise.resolve());

describe('weavess display', () => {
  const weavessProps: WeavessDisplayProps = {
    weavessProps: {
      viewableInterval,
      minimumOffset: 0,
      maximumOffset: 0,
      showMeasureWindow: false,
      stations: [],
      events: undefined,
      measureWindowSelection: {
        channel: undefined,
        endTimeSecs: undefined,
        isDefaultChannel: undefined,
        startTimeSecs: undefined,
        stationId: undefined,
        waveformAmplitudeScaleFactor: undefined
      } as WeavessTypes.MeasureWindowSelection,
      selections: { signalDetections: [], channels: [] },
      initialConfiguration,
      customMeasureWindowLabel: undefined,
      flex: false
    },
    defaultWaveformFilters: [],
    defaultStations: [],
    events: [event],
    signalDetections: [],
    qcMasksByChannelName: [
      {
        currentVersion: qcMaskVersion,
        channelName: 'chanName',
        id: 'masky',
        qcMaskVersions: [qcMaskVersion]
      }
    ],
    measurementMode: {
      mode: AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT,
      entries: undefined
    } as AnalystWorkspaceTypes.MeasurementMode,
    defaultSignalDetectionPhase: CommonTypes.PhaseType.P,
    setMeasurementModeEntries: AnalystWorkspaceOperations.setMeasurementModeEntries,
    analysisMode: undefined,
    createEvent: undefined,
    createQcMask: undefined,
    createSignalDetection: mockCreateSignalDetection,
    currentOpenEventId: event.id,
    currentTimeInterval: timeRange,
    rejectQcMask: jest.fn(),
    rejectSignalDetection: jest.fn(),
    sdIdsToShowFk: [],
    selectedSdIds: [],
    selectedStationIds: [],
    setEventSignalDetectionAssociation: jest.fn(),
    updateQcMask: jest.fn(),
    updateSignalDetection: jest.fn(),
    glContainer: undefined,
    setSdIdsToShowFk: jest.fn(),
    setSelectedSdIds: mockSetSelectedSdIds,
    setSelectedStationIds: mockSetSelectedStationIds,
    amplitudeScaleOption: AmplitudeScalingOptions.FIXED,
    fixedScaleVal: 26,
    eventStatuses: {},
    uiTheme: defaultTheme
  };

  const store = getStore();
  store.dispatch(
    setOpenInterval(timeRange, undefined, undefined, [], WorkflowTypes.AnalysisMode.SCAN) as any
  );

  let weavessRef: any = {
    waveformPanelRef: {
      stationComponentRefs: {
        values: () => [
          {
            props: { station: { name: 'AAK.BHZ' }, nonDefaultChannels: ['AAK.AAK.BHZ'] },
            state: {}
          },
          {
            props: { station: { name: 'AFI.BHZ', nonDefaultChannels: ['AFI.AFI.BHZ'] } },
            state: { expanded: true }
          }
        ]
      },
      getOrderedVisibleChannelNames: jest.fn(() => {
        return ['AAK.BHZ', 'AAK.AAK.BHZ', 'AFI.BHZ', 'AFI.AFI.BHZ'];
      }),
      getCurrentZoomInterval: jest.fn(() => ({ startTimeSecs: 0, endTimeSecs: 1000 }))
    },
    toggleMeasureWindowVisibility: jest.fn(),
    zoomToTimeWindow: jest.fn(),
    refresh: jest.fn()
  };
  const waveform = (
    <Provider store={store}>
      <BaseDisplayContext.Provider
        value={{
          glContainer: undefined,
          widthPx: 1920,
          heightPx: 1080
        }}
      >
        <WeavessContext.Provider
          value={{
            weavessRef,
            setWeavessRef: ref => {
              weavessRef = ref;
            }
          }}
        >
          <WeavessDisplay {...weavessProps} />
        </WeavessContext.Provider>
      </BaseDisplayContext.Provider>
    </Provider>
  );

  const { container } = render(waveform);
  const wrapper = mount(waveform);
  const weavessDisplayComponent: any = wrapper.find('WeavessDisplayComponent').instance();

  const updateIds = (existingList: string[], ids: string[]): void => {
    // sync up the selected station id list in props
    if (existingList.length > 0) {
      existingList.splice(0, existingList.length);
    }
    ids.forEach(id => existingList.push(id));
  };

  // Replace now we have the weavessDisplayComponent ref
  updateMockStationIds = (ids: string[]): void => {
    updateIds(weavessDisplayComponent.props.selectedStationIds, ids);
  };
  updateMockSdIds = (ids: string[]): void => {
    updateIds(weavessDisplayComponent.props.selectedSdIds, ids);
  };

  test('can mount waveform panel', () => {
    expect(container).toMatchSnapshot();
  });

  test('onChannelClick', () => {
    let mockEvent = {
      preventDefault: jest.fn(),
      ctrlKey: true,
      metaKey: false,
      stopPropagation: jest.fn()
    };
    wrapper.setProps({
      defaultStations: [
        {
          name: 'AAK.BHZ',
          allRawChannels: [{ name: 'AAK.BHZ' }, { name: 'AAK.AAK.BHZ' }]
        }
      ]
    });
    weavessProps.defaultStations = [
      {
        name: 'AAK.BHZ',
        allRawChannels: [{ name: 'AAK.BHZ' }, { name: 'AAK.AAK.BHZ' }]
      } as any
    ];
    const waveformOnChannelClick = (
      <Provider store={store}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: undefined,
            widthPx: 1920,
            heightPx: 1080
          }}
        >
          <WeavessContext.Provider
            value={{
              weavessRef,
              setWeavessRef: ref => {
                weavessRef = ref;
              }
            }}
          >
            <WeavessDisplay {...weavessProps} />
          </WeavessContext.Provider>
        </BaseDisplayContext.Provider>
      </Provider>
    );
    const onChannelClickWrapper = mount(waveformOnChannelClick);
    const wrapperInstance: any = onChannelClickWrapper.find('WeavessDisplayComponent').instance();
    wrapperInstance.onChannelClick(mockEvent, 'AAK.BHZ', 100);
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(mockEvent.stopPropagation).toHaveBeenCalled();
    mockEvent.ctrlKey = false;
    mockEvent.metaKey = true;
    weavessDisplayComponent.props.defaultStations[0] = {
      name: 'AAK.BHZ',
      allRawChannels: [{ name: 'AAK.BHZ' }, { name: 'AAK.AAK.BHZ' }]
    };

    weavessDisplayComponent.onChannelClick(mockEvent, 'AAK.BHZ', 100);
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(mockCreateSignalDetection).toBeCalledTimes(2);

    // click on outside measurement selection area (produces toast warn)
    const mockToastWarn = jest.spyOn(toastify.toast, 'warn');
    mockEvent = {
      ...mockEvent,
      ctrlKey: false,
      metaKey: false
    };
    weavessDisplayComponent.onChannelClick(mockEvent, 'AAK.BHZ', 100);
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(mockToastWarn).toBeCalledTimes(1);
  });

  test('can exercise mask clicks', () => {
    // Lets try and exercise somethings
    weavessDisplayComponent.props.defaultStations[0] = {
      name: 'AAK.BHZ',
      allRawChannels: [{ name: 'AAK.BHZ' }, { name: 'AAK.AAK.BHZ' }]
    };
    weavessDisplayComponent.props.defaultStations[1] = {
      name: 'AFI.BHZ',
      allRawChannels: [{ name: 'AFI.BHZ' }, { name: 'AFI.AFI.BHZ' }]
    };
    weavessDisplayComponent.onUpdateChannelMarker();
    const mouseEvent: Partial<React.MouseEvent<HTMLDivElement>> = {
      preventDefault: jest.fn(),
      shiftKey: true
    };
    expect(() =>
      weavessDisplayComponent.onMaskClick(
        mouseEvent,
        'AAK.AAK.BHZ',
        [weavessProps.qcMasksByChannelName[0].id],
        false
      )
    ).not.toThrow();
    mouseEvent.shiftKey = false;
    expect(() =>
      weavessDisplayComponent.onMaskClick(
        mouseEvent,
        'AAK.AAK.BHZ',
        [weavessProps.qcMasksByChannelName[0].id],
        true
      )
    ).not.toThrow();
    weavessProps.qcMasksByChannelName.push(weavessProps.qcMasksByChannelName[0]);
    expect(() =>
      weavessDisplayComponent.onMaskClick(
        mouseEvent,
        'AAK.AAK.BHZ',
        [weavessProps.qcMasksByChannelName[0].id],
        true
      )
    ).not.toThrow();
    mouseEvent.shiftKey = true;
    expect(() =>
      weavessDisplayComponent.onMaskClick(
        mouseEvent,
        'AAK.AAK.BHZ',
        [weavessProps.qcMasksByChannelName[0].id],
        false
      )
    ).not.toThrow();

    expect(() => weavessDisplayComponent.clearSelectedChannels()).not.toThrow();
    expect(() => weavessDisplayComponent.refresh()).not.toThrow();
  });

  test('can exercise channel label clicks', () => {
    const mouseEvent: Partial<React.MouseEvent<HTMLDivElement>> = {
      preventDefault: jest.fn(),
      shiftKey: false
    };

    weavessDisplayComponent.onChannelLabelClick(mouseEvent, 'AAK.AAK.BHZ'); // Select
    expect(weavessDisplayComponent.props.selectedStationIds).toContain('AAK.AAK.BHZ');
    expect(weavessDisplayComponent.props.selectedStationIds).toHaveLength(1);
    weavessDisplayComponent.onChannelLabelClick(mouseEvent, 'AAK.AAK.BHZ'); // De-select
    expect(weavessDisplayComponent.props.selectedStationIds).toHaveLength(0);

    mouseEvent.shiftKey = true;
    weavessDisplayComponent.onChannelLabelClick(mouseEvent, 'AFI.BHZ'); // Range select
    expect(weavessDisplayComponent.props.selectedStationIds).toContain('AFI.BHZ');
    expect(weavessDisplayComponent.props.selectedStationIds).toHaveLength(3);
    // TODO this test is broken beyond this point with unexpected jest results and needs to be fixed
    // mouseEvent.shiftKey = false;
    // mouseEvent.ctrlKey = true;
    // mouseEvent.altKey = true;
    // weavessDisplayComponent.onChannelLabelClick(mouseEvent, 'AAK.BHZ');
    // expect(weavessDisplayComponent.props.selectedStationIds).toContain('AAK.BHZ');
    // expect(weavessDisplayComponent.props.selectedStationIds).toContain('AAK.AAK.BHZ');
    // expect(weavessDisplayComponent.props.selectedStationIds).toContain('AFI.BHZ');
    // expect(weavessDisplayComponent.props.selectedStationIds).toHaveLength(3);
  });
  test('click on signal detection for context menu', () => {
    const mouseEvent: Partial<React.MouseEvent<HTMLDivElement>> = {
      preventDefault: jest.fn(),
      stopPropagation: jest.fn(),
      altKey: true,
      clientX: 100,
      clientY: 100
    };
    const sds = signalDetectionsData;
    expect(weavessDisplayComponent.onSignalDetectionClick(mouseEvent, sds[0].id)).toBeUndefined();
    weavessDisplayComponent.props.signalDetections.push(sds[0]);
    expect(() =>
      weavessDisplayComponent.onSignalDetectionClick(mouseEvent, sds[0].id)
    ).not.toThrow();
  });

  test('click on signal detection for single select/deselect signal detections', () => {
    const mouseEvent: Partial<React.MouseEvent<HTMLDivElement>> = {
      preventDefault: jest.fn(),
      stopPropagation: jest.fn(),
      clientX: 100,
      clientY: 100
    };
    const sds = signalDetectionsData;
    expect(() =>
      weavessDisplayComponent.onSignalDetectionClick(mouseEvent, sds[0].id)
    ).not.toThrow();
    expect(mockSetSelectedSdIds).toBeCalledTimes(1);
    expect(weavessDisplayComponent.props.selectedSdIds).toEqual([sds[0].id]);

    expect(() =>
      weavessDisplayComponent.onSignalDetectionClick(mouseEvent, sds[0].id)
    ).not.toThrow();
    expect(mockSetSelectedSdIds).toBeCalledTimes(2);
    expect(weavessDisplayComponent.props.selectedSdIds).toHaveLength(0);
  });

  test('click on signal detection for single multi select signal detections', () => {
    // clear counts and selectedSdIds
    mockSetSelectedSdIds.mockClear();
    const mouseEvent: Partial<React.MouseEvent<HTMLDivElement>> = {
      preventDefault: jest.fn(),
      stopPropagation: jest.fn(),
      metaKey: true,
      clientX: 100,
      clientY: 100
    };
    const sds = signalDetectionsData;
    expect(() =>
      weavessDisplayComponent.onSignalDetectionClick(mouseEvent, sds[0].id)
    ).not.toThrow();
    expect(mockSetSelectedSdIds).toBeCalledTimes(1);
    expect(weavessDisplayComponent.props.selectedSdIds).toEqual([sds[0].id]);
    expect(() =>
      weavessDisplayComponent.onSignalDetectionClick(mouseEvent, sds[1].id)
    ).not.toThrow();
    expect(mockSetSelectedSdIds).toBeCalledTimes(2);
    expect(weavessDisplayComponent.props.selectedSdIds).toEqual([sds[0].id, sds[1].id]);

    // Remove sds[1].id from the list
    expect(() =>
      weavessDisplayComponent.onSignalDetectionClick(mouseEvent, sds[1].id)
    ).not.toThrow();
    expect(mockSetSelectedSdIds).toBeCalledTimes(3);
    expect(weavessDisplayComponent.props.selectedSdIds).toEqual([sds[0].id]);
  });

  test('bounds generator for fixed', async () => {
    let boundsGenerator: (
      id: string,
      channelSegment?: WeavessTypes.ChannelSegment
    ) => Promise<WeavessTypes.ChannelSegmentBoundaries>;
    boundsGenerator = weavessDisplayComponent.getBoundariesCalculator(
      AmplitudeScalingOptions.FIXED,
      weavessProps.fixedScaleVal,
      undefined,
      -1,
      -1
    );
    let bounds = await boundsGenerator('TEST');
    expect(bounds.channelSegmentId).toBe('TEST');
    expect(bounds.offset).toBe(weavessProps.fixedScaleVal);
    expect(bounds.bottomMax).toBe(-weavessProps.fixedScaleVal);
    expect(bounds.topMax).toBe(weavessProps.fixedScaleVal);
    expect(bounds.channelAvg).toBe(0);

    boundsGenerator = weavessDisplayComponent.getBoundariesCalculator(
      AmplitudeScalingOptions.FIXED,
      'this should trigger scale freezing',
      undefined,
      -1,
      1
    );
    bounds = await boundsGenerator('TEST');
    expect(bounds.channelSegmentId).toBe('TEST');
    expect(bounds.offset).toBe(weavessProps.fixedScaleVal);
    expect(bounds.bottomMax).toBe(-weavessProps.fixedScaleVal);
    expect(bounds.topMax).toBe(weavessProps.fixedScaleVal);
    expect(bounds.channelAvg).toBe(0);
  });

  test('bounds generator for scale all channels to this one', async () => {
    // Test bounds with scale amplitude for all channels set to use max set to 129
    let boundsGenerator = weavessDisplayComponent.getBoundariesCalculator(
      AmplitudeScalingOptions.FIXED,
      weavessProps.fixedScaleVal,
      'AAk',
      -110,
      129
    );
    let bounds = await boundsGenerator('TEST');
    expect(bounds.channelSegmentId).toBe('TEST');
    expect(bounds.offset).toBe(129);
    expect(bounds.bottomMax).toBe(-110);
    expect(bounds.topMax).toBe(129);
    expect(bounds.channelAvg).toBe(0);

    // Test bounds with scale amplitude for all channels set to use min set to -110
    boundsGenerator = weavessDisplayComponent.getBoundariesCalculator(
      AmplitudeScalingOptions.FIXED,
      weavessProps.fixedScaleVal,
      'AAk',
      -110,
      110
    );
    bounds = await boundsGenerator('TEST');
    expect(bounds.channelSegmentId).toBe('TEST');
    expect(bounds.offset).toBe(110);
    expect(bounds.bottomMax).toBe(-110);
    expect(bounds.topMax).toBe(110);
    expect(bounds.channelAvg).toBe(0);
  });

  test('bounds generator for auto', async () => {
    // Test bounds with scale amplitude for auto
    const boundsGenerator = weavessDisplayComponent.getBoundariesCalculator(
      AmplitudeScalingOptions.AUTO,
      weavessProps.fixedScaleVal,
      undefined,
      -1,
      1
    );
    const bounds = await boundsGenerator('TEST');
    expect(bounds.channelSegmentId).toBe('TEST');
    expect(bounds.offset).toBe(100);
    expect(bounds.bottomMax).toBe(-100);
    expect(bounds.topMax).toBe(100);
    expect(bounds.channelAvg).toBe(0);
  });

  test('getWindowedBoundaries gets bounds from the worker cache', async () => {
    const bounds = await weavessDisplayComponent.getWindowedBoundaries('TEST', {
      channelName: 'TEST',
      wfFilterId: 'unfiltered',
      isSelected: false,
      dataSegments: [1, 2, 3, 4]
    });
    expect(bounds).toMatchSnapshot();
    expect(bounds.topMax).toBe(100);
    expect(bounds.bottomMax).toBe(-100);
    expect(bounds.channelSegmentId).toBe('TEST');
  });

  test('getWindowedBoundaries gets bounds from the worker cache when given a time range', async () => {
    const bounds = await weavessDisplayComponent.getWindowedBoundaries(
      'TEST',
      {
        channelName: 'TEST',
        wfFilterId: 'unfiltered',
        isSelected: false,
        dataSegments: [1, 2, 3, 4]
      },
      { startTimeSecs: 0, endTimeSecs: 1000 }
    );
    expect(bounds).toMatchSnapshot();
    expect(bounds.topMax).toBe(100);
    expect(bounds.bottomMax).toBe(-100);
    expect(bounds.channelSegmentId).toBe('TEST');
  });

  test('onUpdateChannelSelectionWindow', () => {
    const startMarker = {
      id: systemConfig.measurementMode.peakTroughSelection.id,
      color: 'red',
      lineStyle: LineStyle.SOLID,
      timeSecs: 0
    };
    const endMarker = {
      ...startMarker,
      timeSecs: 100
    };
    const selectionWindow = {
      id: systemConfig.measurementMode.peakTroughSelection.id,
      startMarker,
      endMarker,
      isMoveable: true,
      color: 'red'
    };
    expect(() =>
      weavessDisplayComponent.onUpdateChannelSelectionWindow(1, selectionWindow)
    ).toThrow();
  });

  test('onClickChannelSelectionWindow', () => {
    const startMarker = {
      id: systemConfig.measurementMode.peakTroughSelection.id,
      color: 'red',
      lineStyle: LineStyle.SOLID,
      timeSecs: 0
    };
    const endMarker = {
      ...startMarker,
      timeSecs: 100
    };
    const selectionWindow = {
      id: systemConfig.measurementMode.peakTroughSelection.id,
      startMarker,
      endMarker,
      isMoveable: true,
      color: 'red'
    };
    expect(() =>
      weavessDisplayComponent.onClickChannelSelectionWindow(1, selectionWindow, 100)
    ).toThrow();
  });

  test('onKeyPress', () => {
    // call escape
    let nativeKeyboardEvent: Partial<React.KeyboardEvent<HTMLDivElement>> = {
      key: 'Escape',
      altKey: false,
      shiftKey: false,
      ctrlKey: false,
      preventDefault: jest.fn(),
      nativeEvent: {
        code: 'Escape'
      } as KeyboardEvent
    };
    const newState = {
      ...weavessDisplayComponent.state,
      selectedQcMask: 'foo'
    };
    weavessDisplayComponent.state = newState;
    expect(() => weavessDisplayComponent.onKeyPress(nativeKeyboardEvent, 1, 100)).not.toThrow();

    // call onAltKeyPress for all keys
    const keys = ['a', 'f', 'p', 'FOO'];
    keys.forEach(key => {
      nativeKeyboardEvent = {
        key,
        altKey: true,
        shiftKey: false,
        ctrlKey: true,
        preventDefault: jest.fn(),
        nativeEvent: {
          code: key
        } as KeyboardEvent
      };
      expect(() => weavessDisplayComponent.onKeyPress(nativeKeyboardEvent, 1, 100)).not.toThrow();
    });

    // Test branch logic when signal detections and event are set
    const newProps = {
      ...weavessProps,
      signalDetections: signalDetectionsData,
      currentOpenEventId: event.id,
      selectedSdIds: [signalDetectionsData[0].id],
      events: [event]
    };

    weavessDisplayComponent.props = newProps;
    keys.forEach(key => {
      nativeKeyboardEvent = {
        key,
        altKey: true,
        shiftKey: false,
        ctrlKey: true,
        preventDefault: jest.fn(),
        nativeEvent: {
          code: key
        } as KeyboardEvent
      };
      expect(() => weavessDisplayComponent.onKeyPress(nativeKeyboardEvent, 1, 100)).not.toThrow();
    });
  });

  test('can build mask selection windows', () => {
    const newState = {
      ...weavessDisplayComponent.state,
      qcMaskModifyInterval: timeRange
    };
    weavessDisplayComponent.state = newState;
    expect(() => weavessDisplayComponent.addMaskSelectionWindows()).not.toThrowError();
  });
});
