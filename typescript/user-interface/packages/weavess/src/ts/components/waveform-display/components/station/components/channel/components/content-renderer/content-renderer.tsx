/* eslint-disable react/default-props-match-prop-types */
/* eslint-disable react/destructuring-assignment */
import { WeavessConstants, WeavessTypes } from '@gms/weavess-core';
import React from 'react';

import {
  memoizedCreateMoveableMarkers,
  memoizedCreateSelectionWindowMarkers,
  memoizedCreateVerticalMarkers
} from '../../../../../markers';
import { PredictedPhases, SignalDetections, TheoreticalPhases } from './components';
import type { ContentRendererProps, ContentRendererState } from './types';

/**
 * Content renderer component responsible for rendering the main content of a channel.
 */
export class ContentRenderer extends React.PureComponent<
  React.PropsWithChildren<ContentRendererProps>,
  ContentRendererState
> {
  /** Default channel props, if not provided */
  // eslint-disable-next-line react/static-property-placement
  public static readonly defaultProps: WeavessTypes.ChannelDefaultConfiguration = {
    displayType: [WeavessTypes.DisplayType.LINE],
    pointSize: 2,
    color: '#4580E6'
  };

  /** Ref to the element where this channel will be rendered */
  public containerRef: HTMLElement | null;

  /** Ref to the element where this description label will be rendered */
  // eslint-disable-next-line
  public descriptionLabelRef: HTMLElement | null;

  /** Ref to drag indicator element */
  private dragIndicatorRef: HTMLDivElement | null;

  private readonly resizeObserver: ResizeObserver;

  /**
   * Constructor
   *
   * @param props props as ContentRendererProps
   */
  public constructor(props: ContentRendererProps) {
    super(props);
    this.resizeObserver = new ResizeObserver(() => this.forceUpdate());
    this.state = {};
  }

  public componentDidMount(): void {
    const canvasRef = this.props.canvasRef();
    if (canvasRef) {
      this.resizeObserver.observe(canvasRef);
    }
  }

  public componentWillUnmount(): void {
    const canvasRef = this.props.canvasRef();
    if (this.resizeObserver && canvasRef) {
      this.resizeObserver.unobserve(canvasRef);
    }
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const isSelected =
      this.props.selections.channels &&
      this.props.selections.channels.indexOf(this.props.channelId) > -1;
    const labelWidthPx =
      this.props.initialConfiguration?.labelWidthPx ?? WeavessConstants.DEFAULT_LABEL_WIDTH_PIXELS;
    const anySignalDetectionsSelected = this.props.signalDetections
      ? this.props.signalDetections.reduce((anySelected, sd) => anySelected || sd.isSelected, false)
      : false;
    return (
      // eslint-disable-next-line jsx-a11y/mouse-events-have-key-events, jsx-a11y/no-static-element-interactions
      <div
        className={`contentrenderer${
          anySignalDetectionsSelected ? ' contentrenderer--selected' : ''
        }`}
        style={{
          backgroundColor: isSelected ? 'rgba(150,150,150,0.2)' : 'initial'
        }}
        ref={ref => {
          if (ref) {
            this.containerRef = ref;
          }
        }}
        // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex
        tabIndex={0}
        onKeyDown={this.props.onKeyDown}
        onContextMenu={this.props.onContextMenu}
        onMouseMove={this.props.onMouseMove}
        onMouseDown={this.props.onMouseDown}
        onMouseUp={this.props.onMouseUp}
      >
        {this.props.children}
        <div
          className="contentrenderer-content contentrenderer-content--sticky"
          style={{
            width: this.props.canvasRef()?.clientWidth ?? `calc(100% - ${labelWidthPx})`
          }}
        >
          {this.props.description ? (
            <div
              ref={ref => {
                this.descriptionLabelRef = ref;
              }}
              className="contentrenderer-content-description-label"
              data-cy="filtered-channel-label"
              data-cy-color={this.props.descriptionLabelColor}
              style={{
                color: this.props.descriptionLabelColor
              }}
            >
              <span className="contentrenderer-content__description">{this.props.description}</span>
            </div>
          ) : undefined}
        </div>
        <div
          className="contentrenderer-content"
          style={{
            left: labelWidthPx,
            width: `calc(100% - ${labelWidthPx}px)`
          }}
          data-cy-station-waveform={this.props.channelId}
        >
          <div
            ref={ref => {
              this.dragIndicatorRef = ref;
            }}
            className="contentrenderer-content-drag-indicator"
          />
          {...this.props.contentRenderers}
          <div className="contentrenderer-content-markers">{this.createAllMarkers(this.props)}</div>
          <SignalDetections
            stationId={this.props.stationId}
            channelId={this.props.channelId}
            signalDetections={this.props.signalDetections}
            isDefaultChannel={this.props.isDefaultChannel}
            displayInterval={this.props.displayInterval}
            events={this.props.events}
            getTimeSecsForClientX={this.getTimeSecsForClientX}
            toggleDragIndicator={this.toggleDragIndicator}
            positionDragIndicator={this.positionDragIndicator}
          />
          <PredictedPhases
            stationId={this.props.stationId}
            channelId={this.props.channelId}
            predictedPhases={this.props.predictedPhases}
            isDefaultChannel={this.props.isDefaultChannel}
            displayInterval={this.props.displayInterval}
            selectedPredictedPhases={this.props.selections.predictedPhases}
            events={this.props.events}
            getTimeSecsForClientX={this.getTimeSecsForClientX}
            toggleDragIndicator={this.toggleDragIndicator}
            positionDragIndicator={this.positionDragIndicator}
          />
          <TheoreticalPhases
            stationId={this.props.stationId}
            theoreticalPhaseWindows={this.props.theoreticalPhaseWindows}
            isDefaultChannel={this.props.isDefaultChannel}
            displayInterval={this.props.displayInterval}
            events={this.props.events}
            getTimeSecsForClientX={this.getTimeSecsForClientX}
            toggleDragIndicator={this.toggleDragIndicator}
            positionDragIndicator={this.positionDragIndicator}
          />
        </div>
      </div>
    );
  }

  /**
   * Creates all of the markers.
   *
   * @param props the content renderer props
   *
   * @returns an array JSX elements
   */
  private readonly createAllMarkers = (props: ContentRendererProps): JSX.Element[] => [
    ...memoizedCreateVerticalMarkers(
      props.displayInterval.startTimeSecs,
      props.displayInterval.endTimeSecs,
      props.markers ? props.markers.verticalMarkers : undefined
    ),
    ...memoizedCreateMoveableMarkers(
      props.displayInterval.startTimeSecs,
      props.displayInterval.endTimeSecs,
      props.markers ? props.markers.moveableMarkers : undefined,
      props.getZoomRatio,
      () => (this.containerRef ? this.containerRef.clientWidth : 0),
      () => (this.containerRef ? this.containerRef.clientWidth : 0),
      props.events
        ? (marker: WeavessTypes.Marker) => {
            if (props.events && props.events.onUpdateMarker) {
              props.events.onUpdateMarker(props.channelId, marker);
            }
          }
        : undefined,
      0
    ),
    ...memoizedCreateSelectionWindowMarkers(
      props.displayInterval.startTimeSecs,
      props.displayInterval.endTimeSecs,
      props.markers ? props.markers.selectionWindows : undefined,
      props.getZoomRatio,
      () => props.canvasRef(),
      () => (this.containerRef ? this.containerRef.clientWidth : 0),
      () => (this.containerRef ? this.containerRef.clientWidth : 0),
      // eslint-disable-next-line @typescript-eslint/unbound-method
      props.converters.computeTimeSecsForMouseXFractionalPosition,
      // eslint-disable-next-line @typescript-eslint/unbound-method
      props.onMouseMove,
      // eslint-disable-next-line @typescript-eslint/unbound-method
      props.onMouseDown,
      // eslint-disable-next-line @typescript-eslint/unbound-method
      props.onMouseUp,
      props.events
        ? (selection: WeavessTypes.SelectionWindow) => {
            if (props.events && props.events.onMoveSelectionWindow) {
              props.events.onMoveSelectionWindow(props.channelId, selection);
            }
          }
        : undefined,
      props.events
        ? (selection: WeavessTypes.SelectionWindow) => {
            if (props.events && props.events.onUpdateSelectionWindow) {
              props.events.onUpdateSelectionWindow(props.channelId, selection);
            }
          }
        : undefined,
      props.events
        ? (selection: WeavessTypes.SelectionWindow, timeSecs: number) => {
            if (props.events && props.events.onClickSelectionWindow) {
              props.events.onClickSelectionWindow(props.channelId, selection, timeSecs);
            }
          }
        : undefined,
      0
    )
  ];

  /**
   * Returns the time in seconds for the given clientX.
   *
   * @param clientX The clientX
   *
   * @returns The time in seconds; undefined if clientX is
   * out of the channel's bounds on screen.
   */
  private readonly getTimeSecsForClientX = (clientX: number): number | undefined => {
    const canvasRef = this.props.canvasRef();

    if (!this.containerRef || !canvasRef) return undefined;

    const offset = canvasRef.getBoundingClientRect();
    // eslint-disable-next-line consistent-return
    if (clientX < offset.left && clientX > offset.right) return undefined;

    // position in [0,1] in the current channel bounds.
    const position = (clientX - offset.left) / offset.width;
    // eslint-disable-next-line consistent-return
    return this.props.converters.computeTimeSecsForMouseXFractionalPosition(position);
  };

  /**
   * Toggle display of the drag indicator for this channel
   *
   * @param show True to show drag indicator
   * @param color The color of the drag indicator
   */
  private readonly toggleDragIndicator = (show: boolean, color: string): void => {
    if (!this.dragIndicatorRef) return;

    this.dragIndicatorRef.style.borderColor = color;
    this.dragIndicatorRef.style.display = show ? 'initial' : 'none';
  };

  /**
   * Set the position for the drag indicator
   *
   * @param clientX The clientX
   */
  private readonly positionDragIndicator = (clientX: number): void => {
    if (!this.containerRef || !this.dragIndicatorRef) return;

    const fracToPct = 100;
    const boundingRect = this.containerRef.getBoundingClientRect();
    // position in [0,1] in the current channel bounds.
    const position = (clientX - boundingRect.left) / boundingRect.width;
    this.dragIndicatorRef.style.left = `${position * fracToPct}%`;
  };
}
