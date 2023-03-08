/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable react/jsx-props-no-spreading */
import { WeavessTypes } from '@gms/weavess-core';
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { ContentRenderer } from '../../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/content-renderer/content-renderer';
import type { ContentRendererProps } from '../../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/content-renderer/types';

const initialConfiguration: WeavessTypes.Configuration = {
  shouldRenderWaveforms: true,
  shouldRenderSpectrograms: false,
  hotKeys: {
    amplitudeScale: 'KeyS',
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

const props: ContentRendererProps = {
  initialConfiguration,
  channelId: 'channel-id',
  stationId: 'station-id',
  displayInterval: {
    startTimeSecs: 300,
    endTimeSecs: 700
  },
  getZoomRatio: jest.fn().mockReturnValue(0.5),
  isDefaultChannel: true,
  predictedPhases: [],
  selections: {
    channels: [],
    predictedPhases: [],
    signalDetections: []
  },
  signalDetections: [],
  theoreticalPhaseWindows: [],
  workerRpcs: [],
  contentRenderers: [],
  description: 'description',
  descriptionLabelColor: '#ff000',
  events: {
    onChannelClick: jest.fn(),
    onClickSelectionWindow: jest.fn(),
    onContextMenu: jest.fn(),
    onMaskClick: jest.fn(),
    onMaskContextClick: jest.fn(),
    onMaskCreateDragEnd: jest.fn(),
    onMeasureWindowUpdated: jest.fn(),
    onMoveSelectionWindow: jest.fn(),
    onPredictivePhaseClick: jest.fn(),
    onPredictivePhaseContextMenu: jest.fn(),
    onPredictivePhaseDragEnd: jest.fn(),
    onSignalDetectionClick: jest.fn(),
    onSignalDetectionContextMenu: jest.fn(),
    onSignalDetectionDragEnd: jest.fn(),
    onUpdateMarker: jest.fn(),
    onUpdateSelectionWindow: jest.fn()
  },
  markers: {
    moveableMarkers: [],
    selectionWindows: [],
    verticalMarkers: []
  },
  canvasRef: jest.fn(),
  converters: {
    computeFractionOfCanvasFromMouseXPx: jest.fn(() => 88),
    computeTimeSecsForMouseXFractionalPosition: jest.fn(() => 88),
    computeTimeSecsFromMouseXPixels: jest.fn(() => 88)
  },
  onContextMenu: jest.fn(),
  onKeyDown: jest.fn(),
  onMouseDown: jest.fn(),
  onMouseMove: jest.fn(),
  onMouseUp: jest.fn(),
  renderWaveforms: jest.fn(),
  updateMeasureWindow: jest.fn()
};

const wrapper = Enzyme.mount(<ContentRenderer {...props} />);
const instance: any = wrapper.find(ContentRenderer).instance();

describe('Weavess Content Renderer', () => {
  it('to be defined', () => {
    expect(ContentRenderer).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<ContentRenderer {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('create all markers', () => {
    const p: ContentRendererProps = {
      ...props,
      markers: {
        moveableMarkers: [
          {
            id: 'moveable',
            timeSecs: 40,
            color: 'ff000',
            lineStyle: WeavessTypes.LineStyle.DASHED,
            minTimeSecsConstraint: 4,
            maxTimeSecsConstraint: 200
          }
        ],
        selectionWindows: [
          {
            id: 'selection',
            color: '00ff00',
            isMoveable: true,
            startMarker: {
              id: 'start',
              color: '#ff0000',
              lineStyle: WeavessTypes.LineStyle.SOLID,
              timeSecs: 10,
              minTimeSecsConstraint: 1,
              maxTimeSecsConstraint: 200
            },
            endMarker: {
              id: 'end',
              color: '#ff0000',
              lineStyle: WeavessTypes.LineStyle.SOLID,
              timeSecs: 80,
              minTimeSecsConstraint: 1,
              maxTimeSecsConstraint: 200
            }
          }
        ],
        verticalMarkers: [
          {
            id: 'vertical',
            color: '0000ff',
            lineStyle: WeavessTypes.LineStyle.DASHED,
            timeSecs: 120,
            minTimeSecsConstraint: 0,
            maxTimeSecsConstraint: 300
          }
        ]
      }
    };
    const { container } = render(<ContentRenderer {...p} />);
    expect(container).toMatchSnapshot();
  });

  it('getTimeSecsForClientX', () => {
    let value = instance.getTimeSecsForClientX(43);
    expect(value).toEqual(undefined);

    const computeTimeSecsForMouseXFractionalPosition = jest.fn(() => 88);
    const content = Enzyme.mount(
      <ContentRenderer
        {...props}
        converters={{
          computeFractionOfCanvasFromMouseXPx: jest.fn(() => 88),
          computeTimeSecsForMouseXFractionalPosition,
          computeTimeSecsFromMouseXPixels: jest.fn(() => 88)
        }}
        canvasRef={() =>
          ({
            getBoundingClientRect: () => ({
              right: 9,
              left: 11,
              width: 100
            })
          } as any)
        }
      />
    );
    const contentInstance: any = content.find(ContentRenderer).instance();
    value = contentInstance.getTimeSecsForClientX(10);
    expect(value).toEqual(undefined);

    value = contentInstance.getTimeSecsForClientX(18);
    expect(computeTimeSecsForMouseXFractionalPosition).toHaveBeenCalledTimes(1);
    expect(value).toEqual(88);
  });

  it('toggleDragIndicator', () => {
    instance.toggleDragIndicator(true, '000000');
    expect(instance.dragIndicatorRef.style.display).toEqual('initial');
    instance.toggleDragIndicator(false, '000000');
    expect(instance.dragIndicatorRef.style.display).toEqual('none');

    const content = Enzyme.mount(<ContentRenderer {...props} />);
    const contentInstance: any = content.find(ContentRenderer).instance();
    contentInstance.dragIndicatorRef = undefined;
    contentInstance.toggleDragIndicator(true, '000000');
  });

  it('positionDragIndicator', () => {
    const content = Enzyme.mount(<ContentRenderer {...props} />);

    const getBoundingClientRect = jest.fn(() => ({
      left: 44,
      right: 12,
      width: 100
    }));
    const contentInstance: any = content.find(ContentRenderer).instance();
    expect(contentInstance.dragIndicatorRef.style.display).toEqual('');

    contentInstance.containerRef = {
      getBoundingClientRect
    };
    contentInstance.positionDragIndicator(48);
    expect(contentInstance.dragIndicatorRef.style.left).toEqual('4%');
    expect(getBoundingClientRect).toHaveBeenCalledTimes(1);

    contentInstance.dragIndicatorRef = undefined;
    contentInstance.positionDragIndicator(48);
    expect(getBoundingClientRect).toHaveBeenCalledTimes(1);
  });
});
