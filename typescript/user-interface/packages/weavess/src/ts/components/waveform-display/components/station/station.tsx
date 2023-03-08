/* eslint-disable class-methods-use-this */
/* eslint-disable react/destructuring-assignment */
import { UILogger } from '@gms/ui-util';
import { WeavessConstants, WeavessTypes } from '@gms/weavess-core';
import memoizeOne from 'memoize-one';
import React from 'react';

import { Channel } from './components';
import type { StationProps, StationState } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

/**
 * Station Component. Contains channels, and optional events.
 */
export class Station extends React.PureComponent<StationProps, StationState> {
  /** The reference to the default channel. */
  public defaultChannelRef: Channel | null;

  /** The reference to the non-default channels. */
  public nonDefaultChannelRefs: { [id: string]: Channel | null } = {};

  /**
   * Constructor
   *
   * @param props Station props as StationProps
   */
  public constructor(props: StationProps) {
    super(props);

    // check to see if there are any masks on the default channel or any of its non-default channels
    const showMaskIndicator = Boolean(
      this.props.station.nonDefaultChannels &&
        this.props.station.nonDefaultChannels
          .map(
            channel =>
              channel.waveform &&
              channel.waveform.masks !== undefined &&
              channel.waveform.masks.length > 0
          )
          .reduce((c1, c2) => c1 || c2, false)
    );

    this.state = {
      showMaskIndicator
    };
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Catches exceptions generated in descendant components.
   * Unhandled exceptions will cause the entire component tree to unmount.
   *
   * @param error the error that was caught
   * @param info the information about the error
   */
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
  public componentDidCatch(error, info): void {
    logger.error(`Weavess Station Error: ${error} : ${info}`);
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  // eslint-disable-next-line react/sort-comp, complexity
  public render(): JSX.Element {
    // calculate and determine the individual row heights
    const rowHeights: number[] = [];
    rowHeights.push(
      this.props.station.defaultChannel.height ||
        this.props.initialConfiguration.defaultChannelHeightPx ||
        WeavessConstants.DEFAULT_CHANNEL_HEIGHT_PIXELS
    );

    const areChannelsShowing =
      !!this.props.station?.areChannelsShowing &&
      this.props.station.areChannelsShowing &&
      !!this.props.station?.nonDefaultChannels;

    if (areChannelsShowing && this.props.station.nonDefaultChannels) {
      this.props.station.nonDefaultChannels.forEach(channel => {
        rowHeights.push(
          channel.height ||
            this.props.initialConfiguration.defaultChannelHeightPx ||
            WeavessConstants.DEFAULT_CHANNEL_HEIGHT_PIXELS
        );
      });
    }

    const totalRowHeight = areChannelsShowing
      ? rowHeights.map(rowHeight => rowHeight + 1).reduce((a, b) => a + b, 0)
      : rowHeights[0] + 1;

    const defaultChannelTimeOffsetSeconds =
      this.props.station.defaultChannel.timeOffsetSeconds || 0;

    const distanceUnits: WeavessTypes.DistanceUnits = this.props.station.distanceUnits
      ? this.props.station.distanceUnits
      : WeavessTypes.DistanceUnits.degrees;

    let parentEvents;
    if (this.props.events?.defaultChannelEvents) {
      parentEvents = this.mapEventsToOffset(
        this.props.station.defaultChannel,
        this.props.events.defaultChannelEvents
      );
    }

    return (
      <div
        className="station"
        style={{
          height: totalRowHeight
        }}
      >
        <Channel // default channel
          key={`station-default-channel-${this.props.station.defaultChannel.id}`}
          ref={ref => {
            this.defaultChannelRef = ref;
          }}
          offsetSecs={defaultChannelTimeOffsetSeconds}
          index={0}
          height={rowHeights[0]}
          shouldRenderWaveforms={this.props.shouldRenderWaveforms}
          shouldRenderSpectrograms={this.props.shouldRenderSpectrograms}
          workerRpcs={this.props.workerRpcs}
          initialConfiguration={this.props.initialConfiguration}
          stationId={this.props.station.id}
          channel={this.mapChannelConfigToOffset(this.props.station.defaultChannel)}
          displayInterval={this.props.displayInterval}
          getZoomRatio={this.props.getZoomRatio}
          isDefaultChannel
          isExpandable={!!this.props.station.nonDefaultChannels}
          expanded={areChannelsShowing}
          selections={this.props.selections}
          showMaskIndicator={this.state.showMaskIndicator}
          distance={this.props.station.distance ? this.props.station.distance : 0}
          distanceUnits={distanceUnits}
          azimuth={this.props.station.azimuth ? this.props.station.azimuth : 0}
          customLabel={this.props.customLabel}
          events={parentEvents}
          canvasRef={this.props.canvasRef}
          getCanvasBoundingRect={this.props.getCanvasBoundingRect}
          getPositionBuffer={this.props.getPositionBuffer}
          getBoundaries={this.props.getBoundaries}
          renderWaveforms={this.props.renderWaveforms}
          glMin={this.props.glMin}
          glMax={this.props.glMax}
          converters={this.props.converters}
          onMouseMove={(e: React.MouseEvent<HTMLDivElement>, xPct: number, timeSecs: number) =>
            this.props.onMouseMove(e, xPct, timeSecs - defaultChannelTimeOffsetSeconds)
          }
          onMouseDown={this.props.onMouseDown}
          onMouseUp={this.props.onMouseUp}
          onContextMenu={this.props.onContextMenu}
          isMeasureWindow={this.props.isMeasureWindow}
          updateMeasureWindow={this.props.updateMeasureWindow}
          msrWindowWaveformAmplitudeScaleFactor={this.props.msrWindowWaveformAmplitudeScaleFactor}
          channelLabelTooltip={this.props.station.defaultChannel.channelLabelTooltip}
        />
        {areChannelsShowing && !!this.props.station?.nonDefaultChannels
          ? this.createNonDefaultChannelElements(
              this.props.station.nonDefaultChannels,
              rowHeights,
              distanceUnits
            )
          : []}
      </div>
    );
  }

  /**
   * Get a list of channels (used by waveform-panel to render the waveforms)
   *
   * @returns list of Channels
   */
  public getChannelList = (): Channel[] => {
    const channels: Channel[] = [];
    if (this.defaultChannelRef) {
      channels.push(this.defaultChannelRef);
    }

    // Add non-default channels if the the channel is expanded
    if (this.nonDefaultChannelRefs && this.props.station.areChannelsShowing) {
      Object.keys(this.nonDefaultChannelRefs).forEach(key => {
        const channel = this.nonDefaultChannelRefs[key];
        if (channel) {
          channels.push(channel);
        }
      });
    }
    return channels;
  };

  /**
   * Get the channel
   *
   * @param channelName
   * @returns channel found or undefined
   */
  public getChannel = (channelName: string): Channel | undefined => {
    const channels: Channel[] = this.getChannelList();
    return channels.find(channel => channel.getChannelId() === channelName);
  };

  /**
   * Sets the ref for a non default channel. Uses memoization to ensure referential stability
   * of this function for each non default channel
   */
  private readonly setNonDefaultChannelRef = memoizeOne((id: string) => ref => {
    this.nonDefaultChannelRefs[id] = ref;
  });

  /**
   * Resets the manual amplitude scaling on the parent and child channels
   */
  public resetAmplitude = (): void => {
    if (this.defaultChannelRef) {
      this.defaultChannelRef.resetAmplitude();
    }

    if (this.nonDefaultChannelRefs) {
      Object.keys(this.nonDefaultChannelRefs).forEach(key => {
        const channel = this.nonDefaultChannelRefs[key];
        if (channel) {
          channel.resetAmplitude();
        }
      });
    }
  };

  /**
   * Create the child channels JSX elements. This function helps break
   * up the render method's complexity and makes it more readable
   *
   * @param channels the child Weavess Channel list
   * @param rowHeights for each child Channel
   * @param distanceUnits which distanceUnits to use degrees or km
   */
  private readonly createNonDefaultChannelElements = (
    channels: WeavessTypes.Channel[],
    rowHeights: number[],
    distanceUnits: WeavessTypes.DistanceUnits
  ): JSX.Element[] => {
    return channels.map((channel, index: number) => {
      let childEvents;
      if (this.props.events?.nonDefaultChannelEvents) {
        childEvents = this.mapEventsToOffset(channel, this.props.events.nonDefaultChannelEvents);
      }

      const timeOffsetSeconds = channel.timeOffsetSeconds || 0;
      const updateMeasureWindow = this.props.updateMeasureWindow
        ? this.updateMeasureWindow
        : undefined;

      return (
        <Channel // Channel (for non-default channels)
          key={`station-nondefault-channel-${channel.id}`}
          ref={this.setNonDefaultChannelRef(channel.id)}
          offsetSecs={timeOffsetSeconds}
          index={(index + 1) * 2}
          height={rowHeights[index + 1]}
          shouldRenderWaveforms={this.props.shouldRenderWaveforms}
          shouldRenderSpectrograms={this.props.shouldRenderSpectrograms}
          workerRpcs={this.props.workerRpcs}
          initialConfiguration={this.props.initialConfiguration}
          stationId={this.props.station.id}
          channel={this.mapChannelConfigToOffset(channel)}
          displayInterval={this.props.displayInterval}
          getZoomRatio={this.props.getZoomRatio}
          isDefaultChannel={false}
          isExpandable={false}
          expanded={false}
          selections={this.props.selections}
          showMaskIndicator={false}
          distance={channel.distance || 0}
          distanceUnits={distanceUnits}
          azimuth={channel.azimuth || 0}
          customLabel={this.props.customLabel}
          events={childEvents}
          canvasRef={this.props.canvasRef}
          getCanvasBoundingRect={this.props.getCanvasBoundingRect}
          getPositionBuffer={this.props.getPositionBuffer}
          getBoundaries={this.props.getBoundaries}
          glMin={this.props.glMin}
          glMax={this.props.glMax}
          renderWaveforms={this.props.renderWaveforms}
          converters={this.props.converters}
          onMouseMove={this.props.onMouseMove}
          onMouseDown={this.props.onMouseDown}
          onMouseUp={this.props.onMouseUp}
          onContextMenu={this.props.onContextMenu}
          isMeasureWindow={this.props.isMeasureWindow}
          updateMeasureWindow={updateMeasureWindow}
          msrWindowWaveformAmplitudeScaleFactor={this.props.msrWindowWaveformAmplitudeScaleFactor}
          channelLabelTooltip={channel.channelLabelTooltip}
        />
      );
    });
  };

  /**
   * Updates the measure window
   *
   * @param stationId station id being updated
   * @param channel the channel being updated
   * @param startTimeSecs startTime as epoch seconds
   * @param endTimeSecs end time as epoch seconds
   * @param isDefaultChannel flag to know if default channel
   * @param removeSelection void function to remove the current selected channel
   */
  private readonly updateMeasureWindow = (
    stationId: string,
    channel: WeavessTypes.Channel,
    startTimeSecs: number,
    endTimeSecs: number,
    isDefaultChannel: boolean,
    waveformAmplitudeScaleFactor: number,
    removeSelection: () => void
  ) => {
    const defaultChannelTimeOffsetSeconds =
      this.props.station.defaultChannel.timeOffsetSeconds || 0;

    if (this.props.updateMeasureWindow) {
      this.props.updateMeasureWindow(
        stationId,
        channel,
        startTimeSecs - defaultChannelTimeOffsetSeconds,
        endTimeSecs - defaultChannelTimeOffsetSeconds,
        isDefaultChannel,
        waveformAmplitudeScaleFactor,
        removeSelection
      );
    }
  };

  /**
   * Maps the channel data to the provided time offset in seconds.
   *
   * @param channel
   */
  // eslint-disable-next-line complexity
  private readonly mapChannelConfigToOffset = (
    channel: WeavessTypes.Channel
  ): WeavessTypes.Channel => {
    if (!channel.timeOffsetSeconds) {
      return channel;
    }

    const { timeOffsetSeconds } = channel;
    // map the time seconds to the offset time seconds

    const waveformMasks: WeavessTypes.Mask[] | undefined =
      channel.waveform && channel.waveform.masks
        ? channel.waveform.masks.map(m => ({
            ...m,
            startTimeSecs: m.startTimeSecs + timeOffsetSeconds,
            endTimeSecs: m.endTimeSecs + timeOffsetSeconds
          }))
        : undefined;

    const waveformSignalDetections: WeavessTypes.PickMarker[] | undefined =
      channel.waveform && channel.waveform.signalDetections
        ? channel.waveform.signalDetections.map(s => ({
            ...s,
            timeSecs: s.timeSecs + timeOffsetSeconds
          }))
        : undefined;

    const waveformPredictedPhases: WeavessTypes.PickMarker[] | undefined =
      channel.waveform && channel.waveform.predictedPhases
        ? channel.waveform.predictedPhases.map(p => ({
            ...p,
            timeSecs: p.timeSecs + timeOffsetSeconds
          }))
        : undefined;

    const waveformTheoreticalPhaseWindows: WeavessTypes.TheoreticalPhaseWindow[] | undefined =
      channel.waveform && channel.waveform.theoreticalPhaseWindows
        ? channel.waveform.theoreticalPhaseWindows.map(t => ({
            ...t,
            startTimeSecs: t.startTimeSecs + timeOffsetSeconds,
            endTimeSecs: t.endTimeSecs + timeOffsetSeconds
          }))
        : undefined;

    const waveformMarkers: WeavessTypes.Markers | undefined =
      channel.waveform && channel.waveform.markers
        ? {
            verticalMarkers: channel.waveform.markers.verticalMarkers
              ? channel.waveform.markers.verticalMarkers.map(v => ({
                  ...v,
                  timeSecs: v.timeSecs + timeOffsetSeconds
                }))
              : undefined,

            moveableMarkers: channel.waveform.markers.moveableMarkers
              ? channel.waveform.markers.moveableMarkers.map(m => ({
                  ...m,
                  timeSecs: m.timeSecs + timeOffsetSeconds
                }))
              : undefined,

            selectionWindows: channel.waveform.markers.selectionWindows
              ? channel.waveform.markers.selectionWindows.map(
                  (s: WeavessTypes.SelectionWindow) => ({
                    ...s,
                    startMarker: {
                      ...s.startMarker,
                      timeSecs: s.startMarker.timeSecs + timeOffsetSeconds,
                      minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint
                        ? s.startMarker.minTimeSecsConstraint + timeOffsetSeconds
                        : s.startMarker.minTimeSecsConstraint,
                      maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint
                        ? s.startMarker.maxTimeSecsConstraint + timeOffsetSeconds
                        : s.startMarker.maxTimeSecsConstraint
                    },
                    endMarker: {
                      ...s.endMarker,
                      timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                      minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint
                        ? s.endMarker.minTimeSecsConstraint + timeOffsetSeconds
                        : s.endMarker.minTimeSecsConstraint,
                      maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint
                        ? s.endMarker.maxTimeSecsConstraint + timeOffsetSeconds
                        : s.endMarker.maxTimeSecsConstraint
                    }
                  })
                )
              : undefined
          }
        : undefined;

    const waveform: WeavessTypes.ChannelWaveformContent | undefined = channel.waveform
      ? {
          ...channel.waveform,
          channelSegmentsRecord: channel.waveform.channelSegmentsRecord,
          masks: waveformMasks,
          signalDetections: waveformSignalDetections,
          predictedPhases: waveformPredictedPhases,
          theoreticalPhaseWindows: waveformTheoreticalPhaseWindows,
          markers: waveformMarkers
        }
      : undefined;

    const spectrogramSignalDetections: WeavessTypes.PickMarker[] | undefined =
      channel.spectrogram && channel.spectrogram.signalDetections
        ? channel.spectrogram.signalDetections.map(s => ({
            ...s,
            timeSecs: s.timeSecs + timeOffsetSeconds
          }))
        : undefined;

    const spectrogramPredictedPhases: WeavessTypes.PickMarker[] | undefined =
      channel.spectrogram && channel.spectrogram.predictedPhases
        ? channel.spectrogram.predictedPhases.map(p => ({
            ...p,
            timeSecs: p.timeSecs + timeOffsetSeconds
          }))
        : undefined;

    const spectrogramTheoreticalPhaseWindows: WeavessTypes.TheoreticalPhaseWindow[] | undefined =
      channel.spectrogram && channel.spectrogram.theoreticalPhaseWindows
        ? channel.spectrogram.theoreticalPhaseWindows.map(t => ({
            ...t,
            startTimeSecs: t.startTimeSecs + timeOffsetSeconds,
            endTimeSecs: t.endTimeSecs + timeOffsetSeconds
          }))
        : undefined;

    const spectrogramMarkers: WeavessTypes.Markers | undefined =
      channel.spectrogram && channel.spectrogram.markers
        ? {
            verticalMarkers: channel.spectrogram.markers.verticalMarkers
              ? channel.spectrogram.markers.verticalMarkers.map((v: WeavessTypes.Marker) => ({
                  ...v,
                  timeSecs: v.timeSecs + timeOffsetSeconds
                }))
              : undefined,

            moveableMarkers: channel.spectrogram.markers.moveableMarkers
              ? channel.spectrogram.markers.moveableMarkers.map(m => ({
                  ...m,
                  timeSecs: m.timeSecs + timeOffsetSeconds
                }))
              : undefined,

            selectionWindows: channel.spectrogram.markers.selectionWindows
              ? channel.spectrogram.markers.selectionWindows.map(s => ({
                  ...s,
                  startMarker: {
                    ...s.startMarker,
                    timeSecs: s.startMarker.timeSecs + timeOffsetSeconds,
                    minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint
                      ? s.startMarker.minTimeSecsConstraint + timeOffsetSeconds
                      : s.startMarker.minTimeSecsConstraint,
                    maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint
                      ? s.startMarker.maxTimeSecsConstraint + timeOffsetSeconds
                      : s.startMarker.maxTimeSecsConstraint
                  },
                  endMarker: {
                    ...s.endMarker,
                    timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                    minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint
                      ? s.endMarker.minTimeSecsConstraint + timeOffsetSeconds
                      : s.endMarker.minTimeSecsConstraint,
                    maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint
                      ? s.endMarker.maxTimeSecsConstraint + timeOffsetSeconds
                      : s.endMarker.maxTimeSecsConstraint
                  }
                }))
              : undefined
          }
        : undefined;

    const spectrogram: WeavessTypes.ChannelSpectrogramContent | undefined = channel.spectrogram
      ? {
          ...channel.spectrogram,
          signalDetections: spectrogramSignalDetections,
          predictedPhases: spectrogramPredictedPhases,
          theoreticalPhaseWindows: spectrogramTheoreticalPhaseWindows,
          markers: spectrogramMarkers
        }
      : undefined;

    const markers: WeavessTypes.Markers | undefined =
      channel && channel.markers
        ? {
            verticalMarkers: channel.markers.verticalMarkers
              ? channel.markers.verticalMarkers.map(v => ({
                  ...v,
                  timeSecs: v.timeSecs + timeOffsetSeconds
                }))
              : undefined,

            moveableMarkers: channel.markers.moveableMarkers
              ? channel.markers.moveableMarkers.map(m => ({
                  ...m,
                  timeSecs: m.timeSecs + timeOffsetSeconds
                }))
              : undefined,

            selectionWindows: channel.markers.selectionWindows
              ? channel.markers.selectionWindows.map(s => ({
                  ...s,
                  startMarker: {
                    ...s.startMarker,
                    timeSecs: s.startMarker.timeSecs + timeOffsetSeconds,
                    minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint
                      ? s.startMarker.minTimeSecsConstraint + timeOffsetSeconds
                      : s.startMarker.minTimeSecsConstraint,
                    maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint
                      ? s.startMarker.maxTimeSecsConstraint + timeOffsetSeconds
                      : s.startMarker.maxTimeSecsConstraint
                  },
                  endMarker: {
                    ...s.endMarker,
                    timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                    minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint
                      ? s.endMarker.minTimeSecsConstraint + timeOffsetSeconds
                      : s.endMarker.minTimeSecsConstraint,
                    maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint
                      ? s.endMarker.maxTimeSecsConstraint + timeOffsetSeconds
                      : s.endMarker.maxTimeSecsConstraint
                  }
                }))
              : undefined
          }
        : undefined;

    return {
      ...channel,
      waveform,
      spectrogram,
      markers
    };
  };

  /**
   * Maps the events to the real time from offset in seconds.
   *
   * @param channel
   * @param channelEvents
   */
  // eslint-disable-next-line complexity
  private readonly mapEventsToOffset = (
    channel: WeavessTypes.Channel,
    channelEvents: WeavessTypes.ChannelEvents
  ): WeavessTypes.ChannelEvents => {
    if (!channel.timeOffsetSeconds) {
      return channelEvents;
    }

    const { timeOffsetSeconds } = channel;

    return {
      labelEvents: channelEvents.labelEvents ? channelEvents.labelEvents : undefined,
      events: channelEvents.events
        ? {
            ...channelEvents.events,
            // map the time seconds back to the original time seconds
            onChannelClick:
              channelEvents.events && channelEvents.events.onChannelClick
                ? (e: React.MouseEvent<HTMLDivElement>, channelId: string, timeSecs: number) => {
                    if (channelEvents.events && channelEvents.events.onChannelClick) {
                      channelEvents.events.onChannelClick(
                        e,
                        channelId,
                        timeSecs - timeOffsetSeconds
                      );
                    }
                  }
                : undefined,

            onSignalDetectionDragEnd:
              channelEvents.events && channelEvents.events.onSignalDetectionDragEnd
                ? (sdId: string, timeSecs: number) => {
                    if (channelEvents.events && channelEvents.events.onSignalDetectionDragEnd) {
                      channelEvents.events.onSignalDetectionDragEnd(
                        sdId,
                        timeSecs - timeOffsetSeconds
                      );
                    }
                  }
                : undefined,

            onPredictivePhaseDragEnd:
              channelEvents.events && channelEvents.events.onPredictivePhaseDragEnd
                ? (id: string, timeSecs: number) => {
                    if (channelEvents.events && channelEvents.events.onPredictivePhaseDragEnd) {
                      channelEvents.events.onPredictivePhaseDragEnd(
                        id,
                        timeSecs - timeOffsetSeconds
                      );
                    }
                  }
                : undefined,

            onMaskCreateDragEnd:
              channelEvents.events && channelEvents.events.onMaskCreateDragEnd
                ? (
                    e: React.MouseEvent<HTMLDivElement>,
                    startTimeSecs: number,
                    endTimeSecs: number,
                    needToDeselect: boolean
                  ) => {
                    if (channelEvents.events && channelEvents.events.onMaskCreateDragEnd) {
                      channelEvents.events.onMaskCreateDragEnd(
                        e,
                        startTimeSecs - timeOffsetSeconds,
                        endTimeSecs - timeOffsetSeconds,
                        needToDeselect
                      );
                    }
                  }
                : undefined,

            onMeasureWindowUpdated:
              channelEvents.events && channelEvents.events.onMeasureWindowUpdated
                ? // eslint-disable-next-line max-len
                  (
                    isVisible: boolean,
                    channelId?: string,
                    startTimeSecs?: number,
                    endTimeSecs?: number,
                    heightPx?: number
                  ) => {
                    if (channelEvents.events && channelEvents.events.onMeasureWindowUpdated) {
                      channelEvents.events.onMeasureWindowUpdated(
                        isVisible,
                        channelId,
                        startTimeSecs ? startTimeSecs - timeOffsetSeconds : undefined,
                        endTimeSecs ? endTimeSecs - timeOffsetSeconds : undefined,
                        heightPx
                      );
                    }
                  }
                : undefined,

            onUpdateMarker:
              channelEvents.events && channelEvents.events.onUpdateMarker
                ? // eslint-disable-next-line max-len
                  (channelId: string, marker: WeavessTypes.Marker) => {
                    if (channelEvents.events && channelEvents.events.onUpdateMarker) {
                      channelEvents.events.onUpdateMarker(channelId, {
                        ...marker,
                        timeSecs: marker.timeSecs - timeOffsetSeconds
                      });
                    }
                  }
                : undefined,

            onMoveSelectionWindow:
              channelEvents.events && channelEvents.events.onMoveSelectionWindow
                ? // eslint-disable-next-line max-len
                  (channelId: string, s: WeavessTypes.SelectionWindow) => {
                    if (channelEvents.events && channelEvents.events.onMoveSelectionWindow) {
                      channelEvents.events.onMoveSelectionWindow(channelId, {
                        ...s,
                        startMarker: {
                          ...s.startMarker,
                          timeSecs: s.startMarker.timeSecs - timeOffsetSeconds,
                          minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint
                            ? s.startMarker.minTimeSecsConstraint - timeOffsetSeconds
                            : s.startMarker.minTimeSecsConstraint,
                          maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint
                            ? s.startMarker.maxTimeSecsConstraint - timeOffsetSeconds
                            : s.startMarker.maxTimeSecsConstraint
                        },
                        endMarker: {
                          ...s.endMarker,
                          timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                          minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint
                            ? s.endMarker.minTimeSecsConstraint - timeOffsetSeconds
                            : s.endMarker.minTimeSecsConstraint,
                          maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint
                            ? s.endMarker.maxTimeSecsConstraint - timeOffsetSeconds
                            : s.endMarker.maxTimeSecsConstraint
                        }
                      });
                    }
                  }
                : undefined,

            onUpdateSelectionWindow:
              channelEvents.events && channelEvents.events.onUpdateSelectionWindow
                ? // eslint-disable-next-line max-len
                  (channelId: string, s: WeavessTypes.SelectionWindow) => {
                    if (channelEvents.events && channelEvents.events.onUpdateSelectionWindow) {
                      channelEvents.events.onUpdateSelectionWindow(channelId, {
                        ...s,
                        startMarker: {
                          ...s.startMarker,
                          timeSecs: s.startMarker.timeSecs - timeOffsetSeconds,
                          minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint
                            ? s.startMarker.minTimeSecsConstraint - timeOffsetSeconds
                            : s.startMarker.minTimeSecsConstraint,
                          maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint
                            ? s.startMarker.maxTimeSecsConstraint - timeOffsetSeconds
                            : s.startMarker.maxTimeSecsConstraint
                        },
                        endMarker: {
                          ...s.endMarker,
                          timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                          minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint
                            ? s.endMarker.minTimeSecsConstraint - timeOffsetSeconds
                            : s.endMarker.minTimeSecsConstraint,
                          maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint
                            ? s.endMarker.maxTimeSecsConstraint - timeOffsetSeconds
                            : s.endMarker.maxTimeSecsConstraint
                        }
                      });
                    }
                  }
                : undefined,

            onClickSelectionWindow:
              channelEvents.events && channelEvents.events.onClickSelectionWindow
                ? // eslint-disable-next-line max-len
                  (channelId: string, s: WeavessTypes.SelectionWindow, timeSecs: number) => {
                    if (channelEvents.events && channelEvents.events.onClickSelectionWindow) {
                      channelEvents.events.onClickSelectionWindow(
                        channelId,
                        {
                          ...s,
                          startMarker: {
                            ...s.startMarker,
                            timeSecs: s.startMarker.timeSecs - timeOffsetSeconds,
                            minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint
                              ? s.startMarker.minTimeSecsConstraint - timeOffsetSeconds
                              : s.startMarker.minTimeSecsConstraint,
                            maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint
                              ? s.startMarker.maxTimeSecsConstraint - timeOffsetSeconds
                              : s.startMarker.maxTimeSecsConstraint
                          },
                          endMarker: {
                            ...s.endMarker,
                            timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                            minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint
                              ? s.endMarker.minTimeSecsConstraint - timeOffsetSeconds
                              : s.endMarker.minTimeSecsConstraint,
                            maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint
                              ? s.endMarker.maxTimeSecsConstraint - timeOffsetSeconds
                              : s.endMarker.maxTimeSecsConstraint
                          }
                        },
                        timeSecs - timeOffsetSeconds
                      );
                    }
                  }
                : undefined
          }
        : undefined,
      /* eslint-disable @typescript-eslint/unbound-method */
      onKeyPress: channelEvents.onKeyPress
    };
  };

  /**
   * Update the mask labels based on the viewing area.
   */
  public readonly updateMaskLabels = (): void => {
    // update the mask labels (Red M) to be display only if the mask is within the viewing area
    if (this.defaultChannelRef) {
      // check to see if there are any masks on the default
      // channel or any of its non-default channels
      const showMaskIndicator = Boolean(
        this.props.station.nonDefaultChannels &&
          this.props.station.nonDefaultChannels
            .map(channel => {
              return (
                channel.waveform &&
                channel.waveform.masks !== undefined &&
                channel.waveform.masks.length > 0 &&
                // check to see if any of the masks are in the zoomed area
                channel.waveform.masks.some(mask =>
                  // TODO: Is this what we really want? Revisit when masks are back,
                  // TODO: since the old logic didn't make sense (it did the opposite of what was in the comments
                  this.props.isWithinTimeRange({
                    startTimeSecs: mask.startTimeSecs,
                    endTimeSecs: mask.endTimeSecs
                  })
                )
              );
            })
            .reduce((c1, c2) => c1 || c2, false)
      );

      if (showMaskIndicator !== this.state.showMaskIndicator) {
        this.setState({
          showMaskIndicator
        });
      }
    }
  };
}
