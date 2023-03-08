/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable react/jsx-props-no-spreading */
import { WeavessTypes } from '@gms/weavess-core';
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';
import { act } from 'react-dom/test-utils';

import type { WaveformRendererProps } from '../../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/waveform-renderer/types';
import { WaveformRenderer } from '../../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/waveform-renderer/waveform-renderer';

const maxAmplitudeValue = 10;
const channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
channelSegmentsRecord[WeavessTypes.UNFILTERED] = [
  {
    channelName: 'WaveformRendererChannel',
    wfFilterId: WeavessTypes.UNFILTERED,
    isSelected: false,
    dataSegments: [
      {
        color: 'dodgerblue',
        displayType: [WeavessTypes.DisplayType.SCATTER],
        pointSize: 2,
        data: {
          id: 'blah',
          startTimeSecs: 450,
          endTimeSecs: 500,
          sampleRate: 1,
          values: [1, 2, 3, 4, 5, 6, 7, 8, 9, maxAmplitudeValue],
          domainTimeRange: {
            startTimeSecs: 460,
            endTimeSecs: 490
          }
        }
      }
    ],
    channelSegmentBoundaries: {
      channelSegmentId: WeavessTypes.UNFILTERED,
      topMax: maxAmplitudeValue,
      bottomMax: -maxAmplitudeValue,
      channelAvg: 0,
      offset: 0
    }
  }
];
const props: WaveformRendererProps = {
  displayInterval: {
    startTimeSecs: 400,
    endTimeSecs: 700
  },
  workerRpcs: [],
  defaultRange: {
    min: 4,
    max: 9
  },
  channelName: 'AAK.AAK.BHZ',
  channelSegmentId: WeavessTypes.UNFILTERED,
  channelSegmentsRecord,
  masks: [
    {
      id: `mask_1`,
      startTimeSecs: 420,
      endTimeSecs: 440,
      color: 'green'
    }
  ],
  glMin: 0,
  glMax: 100,
  msrWindowWaveformAmplitudeScaleFactor: 2.0,
  isMeasureWindow: false,
  renderWaveforms: jest.fn(),
  setYAxisBounds: jest.fn(),
  // eslint-disable-next-line no-promise-executor-return
  getPositionBuffer: jest.fn(async () => new Promise(resolve => resolve(new Float32Array(0))))
};

const wrapper = Enzyme.mount(<WaveformRenderer {...props} />);
const instance: WaveformRenderer = wrapper.find(WaveformRenderer).instance() as any;

describe('Weavess Waveform Renderer', () => {
  it('to be defined', () => {
    expect(WaveformRenderer).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<WaveformRenderer {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('calls render waveforms from updateAmplitude', async () => {
    // update this if it changes. Used to simply verify that the number of calls has increased
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(props.renderWaveforms).toHaveBeenCalledTimes(2);
    await instance.updateAmplitude({ startTimeSecs: 400, endTimeSecs: 700 });
    await act(async () => {
      // eslint-disable-next-line no-promise-executor-return
      await new Promise(resolve => setTimeout(resolve, 200));
      wrapper.update();
    });
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(props.renderWaveforms).toHaveBeenCalledTimes(3);
  });

  it('test setting camera amplitude factor', () => {
    expect(instance.getCameraManualScaleAmplitude()).toEqual(
      props.msrWindowWaveformAmplitudeScaleFactor
    );
    (instance as any).manualAmplitudeScaledValue = 1.2345;
    expect(instance.getCameraManualScaleAmplitude()).toMatchInlineSnapshot(`1.2345`);
  });

  it('call componentDidUpdate with various props', async () => {
    const previousPropsDisplayTimeChanges = {
      ...props,
      displayInterval: {
        startTimeSecs: 300,
        endTimeSecs: 600
      }
    };
    expect(await instance.componentDidUpdate(previousPropsDisplayTimeChanges)).toBeUndefined();
    const previousPropsDisplayDiffChanSegId = {
      ...props,
      channelSegmentId: 'foobar'
    };
    expect(await instance.componentDidUpdate(previousPropsDisplayDiffChanSegId)).toBeUndefined();
  });

  // eslint-disable-next-line @typescript-eslint/require-await
  it('test measure window camera amplitude adjustment', async () => {
    const instanceAny = instance as any;
    const previousPropsDisplayTimeChanges = {
      ...props,
      msrWindowWaveformAmplitudeScaleFactor: 2.1
    };
    expect(async () => instance.componentDidUpdate(previousPropsDisplayTimeChanges)).not.toThrow();
    expect(instanceAny.manualAmplitudeScaledValue).toEqual(
      props.msrWindowWaveformAmplitudeScaleFactor
    );
  });

  it('can componentWillUnmount clean up draw images', () => {
    const instanceAny = instance as any;
    expect(instanceAny.shuttingDown).toBeFalsy();
    expect(() => instance.componentWillUnmount()).not.toThrow();
    expect(instanceAny.shuttingDown).toBeTruthy();
  });

  describe('can manually scale using mouse', () => {
    const buildHTMLDivMouseEvent = (altKey: boolean) => {
      const keyboardEvent = {
        preventDefault: jest.fn(),
        shiftKey: false,
        clientX: 50,
        clientY: 50,
        altKey,
        stopPropagation: jest.fn(() => true)
      };
      return keyboardEvent;
    };
    it('drag mouse to manually scale amplitude', () => {
      const mouseProps = {
        ...props,
        msrWindowWaveformAmplitudeScaleFactor: 0
      };
      const mouseWrapper = Enzyme.mount(<WaveformRenderer {...mouseProps} />);
      const mouseInstance: WaveformRenderer = mouseWrapper.find(WaveformRenderer).instance() as any;
      // Expect not amplitude not set
      expect(mouseInstance.getCameraManualScaleAmplitude()).toEqual(0);
      const mouseEvent: any = buildHTMLDivMouseEvent(false);
      // Call mouse down when scaling
      expect(() => mouseInstance.beginScaleAmplitudeDrag(mouseEvent)).not.toThrow();
      expect(mouseInstance.getCameraManualScaleAmplitude()).toMatchInlineSnapshot(`1`);
      // Call mouse mouse when scaling up
      mouseEvent.clientY = 60;
      let mouse = new MouseEvent('mousemove', mouseEvent);
      expect(() => document.body.dispatchEvent(mouse)).not.toThrow();
      expect(mouseInstance.getCameraManualScaleAmplitude()).toMatchInlineSnapshot(`0.9`);

      // Call mouse mouse when scaling down
      mouseEvent.clientY = 40;
      mouse = new MouseEvent('mousemove', mouseEvent);
      expect(() => document.body.dispatchEvent(mouse)).not.toThrow();
      expect(mouseInstance.getCameraManualScaleAmplitude()).toMatchInlineSnapshot(`0.99`);

      // Reset (disable) manual amplitde scale override
      expect(() => mouseInstance.resetAmplitude()).not.toThrow();
      expect(mouseInstance.getCameraManualScaleAmplitude()).toEqual(0);

      // Remove mouse move and mouse up
      expect(() => document.body.dispatchEvent(new MouseEvent('mouseup'))).not.toThrow();
    });
  });
});
