/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable react/jsx-props-no-spreading */
import { WeavessTypes } from '@gms/weavess-core';
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';
import type * as THREE from 'three';

import { Channel } from '../../../../../../../src/ts/components/waveform-display/components/station/components/channel/channel';
import type {
  ChannelProps,
  ChannelState
} from '../../../../../../../src/ts/components/waveform-display/components/station/components/channel/types';
import {
  getMinMaxAmplitudes,
  hasUserProvidedBoundaries
} from '../../../../../../../src/ts/components/waveform-display/components/station/utils';
import { defaultConfiguration } from '../../../../../../../src/ts/components/waveform-display/configuration';

const timeRange: WeavessTypes.TimeRange = {
  startTimeSecs: 0,
  endTimeSecs: 500
};
const domRect: DOMRect = {
  bottom: 100,
  top: 0,
  height: 100,
  left: 0,
  right: 200,
  toJSON: jest.fn(),
  width: 200,
  x: 0,
  y: 0
};
const canvasElement: Partial<HTMLCanvasElement> = {
  getBoundingClientRect: jest.fn(() => domRect)
};

const channelSegmentBoundaries: WeavessTypes.ChannelSegmentBoundaries = {
  topMax: 307.306593,
  bottomMax: 154.606635,
  channelAvg: 230.31431241288792,
  samplesCount: 179980,
  offset: 307.306593,
  channelSegmentId: 'unfiltered'
};
let startAmp = 1;
const channelSegment: WeavessTypes.ChannelSegment = {
  channelName: 'channel',
  wfFilterId: WeavessTypes.UNFILTERED,
  isSelected: false,
  dataSegments: [
    {
      color: 'dodgerblue',
      displayType: [WeavessTypes.DisplayType.SCATTER],
      pointSize: 2,
      data: {
        startTimeSecs: timeRange.startTimeSecs,
        endTimeSecs: timeRange.endTimeSecs,
        sampleRate: 1,
        values: Array.from({ length: 10 }, () => {
          startAmp += 1;
          return startAmp;
        })
      }
    }
  ],
  channelSegmentBoundaries
};
const channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
channelSegmentsRecord[WeavessTypes.UNFILTERED] = [channelSegment];

// Grabbed the waveform definition from WeavessExample
// !Should consolidate the definition for re-use
const props: ChannelProps = {
  stationId: 'station-id',
  channel: {
    id: 'channel',
    name: 'channel',
    waveform: {
      channelSegmentId: WeavessTypes.UNFILTERED,
      channelSegmentsRecord,
      masks: [
        {
          id: 'mask',
          color: 'red',
          startTimeSecs: 5,
          endTimeSecs: 10
        }
      ],
      signalDetections: [
        {
          id: `sd`,
          timeSecs: timeRange.startTimeSecs + 500,
          color: 'red',
          label: 'P',
          filter: 'brightness(1)',
          isConflicted: false,
          uncertaintySecs: 1.5,
          showUncertaintyBars: true,
          isSelected: true
        }
      ]
    },
    spectrogram: {
      description: 'test spectogram data',
      descriptionLabelColor: 'black',
      startTimeSecs: timeRange.startTimeSecs,
      timeStep: 0.5,
      frequencyStep: 1,
      data: [[0, 0.5, 1.0, 1.5, 2.0, 2.5]],
      signalDetections: [
        {
          id: `sd`,
          timeSecs: timeRange.startTimeSecs + 500,
          color: 'red',
          label: 'P',
          filter: 'brightness(1)',
          isConflicted: false,
          uncertaintySecs: 1.5,
          showUncertaintyBars: false,
          isSelected: false
        }
      ],
      predictedPhases: [
        {
          id: `predictive`,
          timeSecs: timeRange.startTimeSecs + 515,
          color: 'red',
          label: 'P',
          filter: 'opacity(.6)',
          isConflicted: false,
          uncertaintySecs: 1.5,
          showUncertaintyBars: false,
          isSelected: true
        }
      ],
      theoreticalPhaseWindows: [
        {
          id: 'theoretical-phase',
          startTimeSecs: timeRange.startTimeSecs + 60,
          endTimeSecs: timeRange.startTimeSecs + 120,
          color: 'red',
          label: 'TP'
        }
      ],
      markers: {
        verticalMarkers: [
          {
            id: 'marker',
            color: 'lime',
            lineStyle: WeavessTypes.LineStyle.DASHED,
            timeSecs: timeRange.startTimeSecs + 5
          }
        ],
        moveableMarkers: [
          {
            id: 'marker',
            color: 'RED',
            lineStyle: WeavessTypes.LineStyle.DASHED,
            timeSecs: timeRange.startTimeSecs + 50
          }
        ],
        selectionWindows: [
          {
            id: 'selection',
            startMarker: {
              id: 'marker',
              color: 'purple',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: timeRange.startTimeSecs + 200
            },
            endMarker: {
              id: 'marker',
              color: 'purple',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: timeRange.startTimeSecs + 400
            },
            isMoveable: true,
            color: 'rgba(200,0,0,0.2)'
          }
        ]
      }
    }
  },
  glMin: 0,
  glMax: 100,
  displayInterval: {
    startTimeSecs: timeRange.startTimeSecs,
    endTimeSecs: timeRange.endTimeSecs
  },
  initialConfiguration: defaultConfiguration,
  distance: 4,
  azimuth: 9,
  expanded: true,
  distanceUnits: WeavessTypes.DistanceUnits.km,
  height: 800,
  index: 1,
  isDefaultChannel: true,
  isExpandable: true,
  workerRpcs: [],
  getCanvasBoundingRect: jest.fn(() => domRect),
  selections: {
    channels: [],
    predictedPhases: [],
    signalDetections: [`sd`]
  },
  shouldRenderSpectrograms: true,
  shouldRenderWaveforms: true,
  showMaskIndicator: true,
  defaultRange: {
    min: 4,
    max: 11
  },
  offsetSecs: 0,
  events: {
    labelEvents: {
      onChannelCollapsed: jest.fn(),
      onChannelExpanded: jest.fn(),
      onChannelLabelClick: jest.fn()
    },
    events: {
      onContextMenu: jest.fn(),
      onChannelClick: jest.fn(),
      onSignalDetectionContextMenu: jest.fn(),
      onSignalDetectionClick: jest.fn(),
      onSignalDetectionDragEnd: jest.fn(),
      onPredictivePhaseContextMenu: jest.fn(),
      onPredictivePhaseClick: jest.fn(),
      onPredictivePhaseDragEnd: jest.fn(),
      onMeasureWindowUpdated: jest.fn(),
      onUpdateMarker: jest.fn(),
      onMoveSelectionWindow: jest.fn(),
      onUpdateSelectionWindow: jest.fn(),
      onClickSelectionWindow: jest.fn(),
      onMaskContextClick: jest.fn(),
      onMaskCreateDragEnd: jest.fn(),
      onMaskClick: jest.fn()
    },
    onKeyPress: jest.fn()
  },
  canvasRef: jest.fn(() => canvasElement as HTMLCanvasElement),
  converters: {
    computeFractionOfCanvasFromMouseXPx: jest.fn(),
    computeTimeSecsForMouseXFractionalPosition: jest.fn(),
    computeTimeSecsFromMouseXPixels: jest.fn(
      (clientx: number) => (1 + clientx / 100) * timeRange.startTimeSecs
    )
  },
  getZoomRatio: jest.fn().mockReturnValue(0.5),
  onMouseDown: jest.fn(),
  onMouseMove: jest.fn(),
  onMouseUp: jest.fn(),
  renderWaveforms: jest.fn(),
  onContextMenu: jest.fn(),
  updateMeasureWindow: jest.fn(),
  isMeasureWindow: true,
  getBoundaries: jest.fn(
    async () =>
      new Promise<WeavessTypes.ChannelSegmentBoundaries>(resolve =>
        // eslint-disable-next-line no-promise-executor-return
        resolve(channelSegmentBoundaries)
      )
  )
};

const emptyChannelProps: ChannelProps = {
  ...props,
  stationId: 'empty-station-id',
  channel: {
    id: 'channel',
    name: 'channel',
    waveform: undefined,
    spectrogram: undefined
  },
  shouldRenderSpectrograms: false,
  shouldRenderWaveforms: false,
  showMaskIndicator: false,
  events: undefined,
  isMeasureWindow: false
};

const wrapper = Enzyme.mount(<Channel {...props} />);
const channelInstance: Channel = wrapper.find(Channel).instance() as Channel;

describe('Weavess Channel', () => {
  it('channel wrapper to match snapshot', () => {
    const { container } = render(<Channel {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('channel to be defined', () => {
    expect(Channel).toBeDefined();
  });

  it('has a channel name set', () => {
    expect(channelInstance.getChannelId()).toEqual('channel');
  });

  it('call componentDidUpdate', () => {
    const state: ChannelState = {
      waveformYAxisBounds: {
        minAmplitude: -1,
        maxAmplitude: 1,
        heightInPercentage: 20
      },
      spectrogramYAxisBounds: {
        minAmplitude: -1,
        maxAmplitude: 1,
        heightInPercentage: 20
      }
    };
    // Set default bounds and measure window
    channelInstance.setState(state);
    expect(() => channelInstance.componentDidUpdate(props, state)).not.toThrow();
  });

  it('componentDidCatch', () => {
    const spy = jest.spyOn(channelInstance, 'componentDidCatch');
    channelInstance.componentDidCatch(new Error('error'), { componentStack: undefined });
    expect(spy).toBeCalled();
  });

  it('call render to matchSnapshot', () => {
    expect(() => channelInstance.render()).not.toThrow();
  });

  it('call resetAmplitude', () => {
    const mockResetAmplitudes = jest.fn();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const instance: any = channelInstance;
    if (instance.waveformRendererRef) {
      instance.waveformRendererRef.resetAmplitude = mockResetAmplitudes;
    }

    expect(() => channelInstance.resetAmplitude()).not.toThrow();
    expect(mockResetAmplitudes).toBeCalledTimes(1);
  });

  it('call updateAmplitude', () => {
    expect(async () =>
      channelInstance.updateAmplitude({ startTimeSecs: 400, endTimeSecs: 700 })
    ).not.toThrow();
  });

  it('call getTimeSecs', () => {
    expect(channelInstance.getTimeSecs()).toMatchInlineSnapshot(`undefined`);
  });

  it('call getMousePosition', () => {
    expect(channelInstance.getMousePosition()).toMatchSnapshot();
  });

  it('renders the THREE scene when given reasonable DOM rect', () => {
    const mockRenderer: THREE.WebGLRenderer = {
      setViewport: jest.fn(),
      setScissor: jest.fn(),
      render: jest.fn()
    };
    channelInstance.renderScene(mockRenderer, domRect);
    expect(mockRenderer.setViewport).toHaveBeenCalled();
    expect(mockRenderer.setScissor).toHaveBeenCalled();
    expect(mockRenderer.render).toHaveBeenCalled();
  });
  it('does not render the THREE scene if DOM rect is out of bounds', () => {
    const mockRenderer: THREE.WebGLRenderer = {
      setViewport: jest.fn(),
      setScissor: jest.fn(),
      render: jest.fn()
    };
    const mockDomRect: DOMRect = {
      bottom: -100,
      top: -200,
      height: 100,
      left: 0,
      right: 200,
      toJSON: jest.fn(),
      width: 200,
      x: 0,
      y: 0
    };
    channelInstance.renderScene(mockRenderer, mockDomRect);
    expect(mockRenderer.setViewport).not.toHaveBeenCalled();
    expect(mockRenderer.setScissor).not.toHaveBeenCalled();
    expect(mockRenderer.render).not.toHaveBeenCalled();
  });

  it('create an empty Channel, mount it and call componentDidUpdate', () => {
    const emptyChannelWrapper = Enzyme.mount(<Channel {...emptyChannelProps} />);
    expect(emptyChannelWrapper).toBeDefined();
    const emptyChannelInstance: Channel = wrapper.find(Channel).instance() as Channel;

    const state: ChannelState = {
      waveformYAxisBounds: {
        minAmplitude: -1,
        maxAmplitude: 1,
        heightInPercentage: 20
      },
      spectrogramYAxisBounds: {
        minAmplitude: -1,
        maxAmplitude: 1,
        heightInPercentage: 20
      }
    };
    // Set default bounds and measure window
    expect(() => emptyChannelInstance.componentDidUpdate(emptyChannelProps, state)).not.toThrow();
    expect(emptyChannelInstance.render()).toBeDefined();

    const mockRenderer: THREE.WebGLRenderer = {
      setViewport: jest.fn(),
      setScissor: jest.fn(),
      render: jest.fn()
    };
    emptyChannelInstance.renderScene(mockRenderer, domRect);
    expect(mockRenderer.setViewport).toHaveBeenCalled();
    expect(mockRenderer.setScissor).toHaveBeenCalled();
    expect(mockRenderer.render).toHaveBeenCalled();
  });

  it('can get min/max amplitudes', () => {
    expect(getMinMaxAmplitudes([channelSegment])).toMatchSnapshot();
    const channelSegmentNoBoundaries = {
      ...channelSegment,
      channelSegmentBoundaries: undefined
    };
    expect(getMinMaxAmplitudes([channelSegmentNoBoundaries])).toMatchSnapshot();
  });

  it('can determine has boundaries', () => {
    expect(hasUserProvidedBoundaries([channelSegment])).toBeTruthy();
    expect(hasUserProvidedBoundaries([])).toBeFalsy();
  });
});

describe('Weavess Channel private method', () => {
  // These are private methods so cast instance to an any to call them
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const instance: any = channelInstance;

  const buildHTMLDivMouseEvent = (altKey: boolean): Partial<React.MouseEvent<HTMLDivElement>> => {
    const keyboardEvent: Partial<React.MouseEvent<HTMLDivElement>> = {
      preventDefault: jest.fn(),
      shiftKey: false,
      clientX: 50,
      clientY: 50,
      altKey,
      stopPropagation: jest.fn(() => true)
    };
    return keyboardEvent;
  };

  const buildKeyboardEvent = (code: string, altKey: boolean): Partial<KeyboardEvent> => {
    const nativeKeyboardEvent: Partial<KeyboardEvent> = {
      code,
      altKey
    };
    return nativeKeyboardEvent;
  };

  const buildHTMLDivKeyboardEvent = (
    nativeKeyboardEvent: Partial<KeyboardEvent>
  ): Partial<React.KeyboardEvent<HTMLDivElement>> => {
    const keyboardEvent: Partial<React.KeyboardEvent<HTMLDivElement>> = {
      preventDefault: jest.fn(),
      shiftKey: false,
      repeat: false,
      nativeEvent: nativeKeyboardEvent as KeyboardEvent,
      stopPropagation: jest.fn(() => true)
    };
    return keyboardEvent;
  };
  describe('test keyboard events', () => {
    it('call onWaveformKeyDown amplitude scaling', () => {
      expect(() =>
        instance.onWaveformKeyDown(buildHTMLDivKeyboardEvent(buildKeyboardEvent('KeyY', true)))
      ).not.toThrow();

      // Set the amplitude scale keyDown followed by the keyup
      expect(() =>
        instance.onWaveformKeyDown(buildHTMLDivKeyboardEvent(buildKeyboardEvent('KeyY', false)))
      ).not.toThrow();

      // Call mouse down when scaling
      expect(() => instance.onMouseDown(buildHTMLDivMouseEvent(false))).not.toThrow();

      const eventInit: KeyboardEventInit = {
        code: defaultConfiguration.hotKeys.amplitudeScale,
        repeat: false,
        shiftKey: false
      };
      const keyboardEvent = new KeyboardEvent('keyup', eventInit);
      expect(document.body.dispatchEvent(keyboardEvent)).toBeTruthy();
    });

    it('call single channel reset amplitude scaling', () => {
      const mockResetAmplitudes = jest.fn();
      if (instance.waveformRendererRef) {
        instance.waveformRendererRef.resetAmplitude = mockResetAmplitudes;
      }

      const resetKey = buildHTMLDivKeyboardEvent(buildKeyboardEvent('KeyY', true));
      expect(() => instance.onKeyDown(resetKey)).not.toThrow();
      expect(mockResetAmplitudes).toBeCalledTimes(1);
    });

    it('call onWaveformKeyDown create mask', () => {
      // Call mask create followed by keyup
      expect(() =>
        instance.onWaveformKeyDown(buildHTMLDivKeyboardEvent(buildKeyboardEvent('KeyM', false)))
      ).not.toThrow();
      const eventInit: KeyboardEventInit = {
        code: defaultConfiguration.hotKeys.maskCreate,
        repeat: false,
        shiftKey: false
      };
      const keyboardEvent = new KeyboardEvent('keyup', eventInit);
      expect(document.body.dispatchEvent(keyboardEvent)).toBeTruthy();
    });

    it('call onKeyDown', () => {
      const anyKey = buildHTMLDivKeyboardEvent(buildKeyboardEvent('anyKey', false));
      // Call mask create followed by keyup
      expect(() => instance.onKeyDown(anyKey)).not.toThrow();

      //  Test key down if waveformContentRef is undefined
      const { waveformContentRef } = instance;
      instance.waveformContentRef = undefined;
      expect(() => instance.onKeyDown(anyKey)).not.toThrow();

      // Restore ref
      instance.waveformContentRef = waveformContentRef;
    });
  });

  describe('test mouse events', () => {
    const mouseProps = {
      ...props,
      isMeasureWindow: false
    };
    const mouseWrapper = Enzyme.mount(<Channel {...mouseProps} />);
    const mouseInstance: any = mouseWrapper.find(Channel).instance();
    it('mouse instance call set state and call componentDidUpdate', () => {
      const state: ChannelState = {
        waveformYAxisBounds: {
          minAmplitude: -1,
          maxAmplitude: 1,
          heightInPercentage: 20
        },
        spectrogramYAxisBounds: {
          minAmplitude: -1,
          maxAmplitude: 1,
          heightInPercentage: 20
        }
      };
      // Set default bounds and measure window
      mouseInstance.setState(state);
      expect(() => mouseInstance.componentDidUpdate(props, state)).not.toThrow();
    });
    it('call onSpectrogramMouseUp', () => {
      expect(() => mouseInstance.onSpectrogramMouseUp(buildHTMLDivMouseEvent(false))).not.toThrow();
    });
    it('call onWaveformContextMenu', () => {
      expect(() =>
        mouseInstance.onWaveformContextMenu(buildHTMLDivMouseEvent(false))
      ).not.toThrow();
    });

    it('call onWaveformMouseUp', () => {
      expect(() => mouseInstance.onWaveformMouseUp(buildHTMLDivMouseEvent(false))).not.toThrow();
    });
    it('call onMouseMove', () => {
      expect(() => mouseInstance.onMouseMove(buildHTMLDivMouseEvent(false))).not.toThrow();
    });

    it('call onMouseDown', () => {
      expect(() => mouseInstance.onMouseDown(buildHTMLDivMouseEvent(true))).not.toThrow();
    });

    it('call onSpectonWaveformContextMenurogramContextMenu', () => {
      expect(() => instance.onWaveformContextMenu(buildHTMLDivMouseEvent(false))).not.toThrow();
    });

    it('call onSpectrogramContextMenu', () => {
      expect(() => instance.onSpectrogramContextMenu(buildHTMLDivMouseEvent(false))).not.toThrow();
    });
  });
  it('call setSpectrogramYAxisBounds', () => {
    // Set it then change max but keep min the same
    expect(() => instance.setSpectrogramYAxisBounds(-100.0, 100)).not.toThrow();
    expect(() => instance.setSpectrogramYAxisBounds(-100.0, 200)).not.toThrow();
  });

  // TODO Unskip tests and fix
  it.skip('call setWaveformYAxisBounds', () => {
    // Set it then change max but keep min the same
    expect(() => instance.setWaveformYAxisBounds(-100.0, 100)).not.toThrow();
    expect(() => instance.setWaveformYAxisBounds(-100.0, 200)).not.toThrow();

    const yAxisBounds = {
      ...instance.state.waveformYAxisBounds,
      maxAmplitude: 200.0,
      minAmplitude: -100.0
    };
    expect(instance.getWaveformYAxisBound()).toEqual(yAxisBounds);
  });

  it('call updateMeasureWindowPanel', () => {
    expect(() => instance.updateMeasureWindowPanel(timeRange, jest.fn())).not.toThrow();
  });
});

describe('Weavess Channel with no waveform', () => {
  it('channel wrapper to match snapshot undefined waveform', () => {
    const propsNoWaveform = {
      ...props,
      channel: {
        ...props.channel,
        waveform: undefined
      }
    };
    propsNoWaveform.channel.waveform = undefined;
    const { container } = render(<Channel {...propsNoWaveform} />);
    expect(container).toMatchSnapshot();
  });

  it('can mount channel where the props.events are undefined', () => {
    const myProps: ChannelProps = {
      ...props,
      events: {
        labelEvents: props.events?.labelEvents,
        events: undefined
      }
    };
    const { container } = render(<Channel {...myProps} />);
    expect(container).toMatchSnapshot();
  });
});
