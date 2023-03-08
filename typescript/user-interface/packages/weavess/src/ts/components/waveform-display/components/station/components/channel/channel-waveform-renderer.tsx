import type { WeavessTypes } from '@gms/weavess-core';
import * as React from 'react';

import type { PositionConverters } from '../../../../../../util/types';
import type {
  AnimationFrameOptions,
  updateMeasureWindow as updateMeasureWindowFunction
} from '../../../../types';
import { MeasureWindowSelectionListener } from '../../../measure-window/measure-window-selection';
import { ContentRenderer } from './components/content-renderer/content-renderer';
import { WaveformRenderer } from './components/waveform-renderer/waveform-renderer';

/**
 * The type of the props for the {@link ChannelWaveformRenderer} component
 */
export interface ChannelWaveformRendererProps {
  canvasRef(): HTMLCanvasElement;
  channel: WeavessTypes.Channel;
  channelSegments: WeavessTypes.ChannelSegment[];
  contentRenderMouseDown;
  converters: PositionConverters;
  displayInterval: WeavessTypes.TimeRange;
  events: WeavessTypes.ChannelContentEvents;
  getBoundaries(
    channelName: string,
    channelSegment?: WeavessTypes.ChannelSegment,
    timeRange?: WeavessTypes.TimeRange
  ): Promise<WeavessTypes.ChannelSegmentBoundaries>;
  getContentRenderer: (this: any, contentRenderer: any) => any[];
  getPositionBuffer(
    id: string,
    startTime: number,
    endTime: number,
    domainTimeRange: WeavessTypes.TimeRange
  ): Promise<Float32Array>;
  getSignalDetections: (signalDetections: WeavessTypes.PickMarker[]) => WeavessTypes.PickMarker[];
  getZoomRatio: () => number;
  glMax: number;
  glMin: number;
  height: number;
  initialConfiguration: WeavessTypes.Configuration;
  isDefaultChannel: boolean;
  isMeasureWindow: boolean;
  isMeasureWindowEnabled(): boolean;
  labelWidthPx: number;
  msrWindowWaveformAmplitudeScaleFactor?: number;
  numberOfRenderers: number;
  onMouseMove: (e: React.MouseEvent<HTMLDivElement>) => void;
  onWaveformContextMenu: (e: React.MouseEvent<HTMLDivElement>) => void;
  onWaveformKeyDown: (e: React.KeyboardEvent<HTMLDivElement>) => void;
  onWaveformMouseUp: (e: React.MouseEvent<HTMLDivElement>) => void;
  renderWaveforms(options?: AnimationFrameOptions): void;
  selections: WeavessTypes.Selections;
  setWaveformContainerRef: (ref: HTMLDivElement) => void;
  setWaveformContentRendererRef: (ref: ContentRenderer) => void;
  setWaveformRendererRef: (ref: WaveformRenderer) => void;
  setWaveformYAxisBounds: (min: number, max: number) => void;
  stationId: string;
  toast: (message: string) => void;
  updateMeasureWindow: updateMeasureWindowFunction;
  updateMeasureWindowPanel: (
    timeRange: WeavessTypes.TimeRange,
    removeMeasureWindowSelection: () => void
  ) => void;
  waveform: WeavessTypes.ChannelWaveformContent;
  workerRpcs: any[];
}

/**
 * Renders the waveform content for the channel, including the measure window
 * selection listener, the ContentRenderer, and the waveform renderer
 */
// eslint-disable-next-line react/function-component-definition
export const InternalChannelWaveformRenderer: React.FC<ChannelWaveformRendererProps> = ({
  canvasRef,
  channel,
  channelSegments,
  contentRenderMouseDown,
  converters,
  displayInterval,
  events,
  getBoundaries,
  getContentRenderer,
  getPositionBuffer,
  getSignalDetections,
  getZoomRatio,
  glMax,
  glMin,
  height,
  initialConfiguration,
  isDefaultChannel,
  isMeasureWindow,
  isMeasureWindowEnabled,
  labelWidthPx,
  msrWindowWaveformAmplitudeScaleFactor,
  numberOfRenderers,
  onMouseMove,
  onWaveformContextMenu,
  onWaveformKeyDown,
  onWaveformMouseUp,
  renderWaveforms,
  selections,
  setWaveformContainerRef,
  setWaveformContentRendererRef,
  setWaveformRendererRef,
  setWaveformYAxisBounds,
  stationId,
  toast,
  updateMeasureWindow,
  updateMeasureWindowPanel,
  waveform,
  workerRpcs
}: ChannelWaveformRendererProps) => {
  const renderMeasureWindowSelectionChildren = React.useCallback(
    ({ contentRenderer, onMouseDown }) => {
      return (
        <div
          className="channel-content-container"
          key={`channel-segment-${waveform.channelSegmentId}`}
          ref={setWaveformContainerRef}
          style={{
            height: `${height / numberOfRenderers}px`,
            width: `calc(100% - ${labelWidthPx}px)`,
            left: `${labelWidthPx}px`
          }}
        >
          <ContentRenderer
            ref={setWaveformContentRendererRef}
            canvasRef={canvasRef}
            converters={converters}
            displayInterval={displayInterval}
            initialConfiguration={initialConfiguration}
            isDefaultChannel={isDefaultChannel}
            renderWaveforms={renderWaveforms}
            selections={selections}
            stationId={stationId}
            workerRpcs={workerRpcs}
            getZoomRatio={getZoomRatio}
            updateMeasureWindow={updateMeasureWindow}
            contentRenderers={getContentRenderer(contentRenderer)}
            channelId={channel.id}
            // TODO: Should we walk thru all descriptions and label color and return undefined if not consistent?
            description={channelSegments[0]?.description}
            descriptionLabelColor={channelSegments[0]?.descriptionLabelColor}
            signalDetections={getSignalDetections(waveform.signalDetections)}
            predictedPhases={waveform?.predictedPhases}
            theoreticalPhaseWindows={waveform?.theoreticalPhaseWindows}
            markers={waveform?.markers}
            events={events}
            onContextMenu={onWaveformContextMenu}
            onMouseMove={onMouseMove}
            onMouseDown={contentRenderMouseDown(onMouseDown)}
            onMouseUp={onWaveformMouseUp}
            onKeyDown={onWaveformKeyDown}
          />
          <WaveformRenderer
            ref={setWaveformRendererRef}
            displayInterval={displayInterval}
            glMax={glMax}
            glMin={glMin}
            renderWaveforms={renderWaveforms}
            workerRpcs={workerRpcs}
            initialConfiguration={initialConfiguration}
            getPositionBuffer={getPositionBuffer}
            getBoundaries={getBoundaries}
            channelName={channel.id}
            defaultRange={channel.defaultRange}
            channelSegmentId={waveform.channelSegmentId ?? ''}
            channelSegmentsRecord={waveform.channelSegmentsRecord ?? {}}
            masks={waveform?.masks}
            setYAxisBounds={setWaveformYAxisBounds}
            msrWindowWaveformAmplitudeScaleFactor={msrWindowWaveformAmplitudeScaleFactor}
            isMeasureWindow={isMeasureWindow}
            channelOffset={channel.timeOffsetSeconds}
          />
        </div>
      );
    },
    [
      canvasRef,
      channel.defaultRange,
      channel.id,
      channel.timeOffsetSeconds,
      channelSegments,
      contentRenderMouseDown,
      converters,
      displayInterval,
      events,
      getBoundaries,
      getContentRenderer,
      getPositionBuffer,
      getSignalDetections,
      getZoomRatio,
      glMax,
      glMin,
      height,
      initialConfiguration,
      isDefaultChannel,
      isMeasureWindow,
      labelWidthPx,
      msrWindowWaveformAmplitudeScaleFactor,
      numberOfRenderers,
      onMouseMove,
      onWaveformContextMenu,
      onWaveformKeyDown,
      onWaveformMouseUp,
      renderWaveforms,
      selections,
      setWaveformContainerRef,
      setWaveformContentRendererRef,
      setWaveformRendererRef,
      setWaveformYAxisBounds,
      stationId,
      updateMeasureWindow,
      waveform,
      workerRpcs
    ]
  );

  if (!waveform) {
    return null;
  }

  return (
    <MeasureWindowSelectionListener
      displayInterval={displayInterval}
      offsetSecs={channel.timeOffsetSeconds}
      hotKeys={initialConfiguration.hotKeys}
      isMeasureWindowEnabled={isMeasureWindowEnabled}
      // eslint-disable-next-line @typescript-eslint/unbound-method
      computeTimeSecsFromMouseXPixels={converters.computeTimeSecsFromMouseXPixels}
      toast={toast}
      updateMeasureWindowPanel={updateMeasureWindowPanel}
    >
      {renderMeasureWindowSelectionChildren}
    </MeasureWindowSelectionListener>
  );
};

export const ChannelWaveformRenderer = React.memo(InternalChannelWaveformRenderer);
