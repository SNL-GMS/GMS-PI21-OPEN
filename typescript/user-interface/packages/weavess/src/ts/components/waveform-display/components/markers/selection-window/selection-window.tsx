/* eslint-disable react/destructuring-assignment */
import { UILogger } from '@gms/ui-util';
import debounce from 'lodash/debounce';
import isEqual from 'lodash/isEqual';
import React from 'react';

import { SingleDoubleClickEvent } from '../../../events/single-double-click-event';
import { calculateLeftPercent } from '../../../utils';
import { MoveableMarker } from '../moveable-marker';
import { VerticalMarker } from '../vertical-marker';
import type { SelectionWindowProps, SelectionWindowState } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

/**
 * SelectionWindow Component. Contains two moveable markers.
 */
export class SelectionWindow extends React.PureComponent<
  SelectionWindowProps,
  SelectionWindowState
> {
  /** Ref to the time window selection */
  private timeWindowSelectionRef: HTMLDivElement | null;

  /** Ref to the lead marker */
  private leadBorderRef: VerticalMarker | MoveableMarker | null;

  /** Ref to the lag marker */
  private endBorderRef: VerticalMarker | MoveableMarker | null;

  /** indicates if the mouse is down */
  private mouseDown = false;

  /** indicates if the mouse is dragging */
  private isDragging = false;

  /** handler for handling single and double click events */
  private readonly handleSingleDoubleClick: SingleDoubleClickEvent = new SingleDoubleClickEvent();

  /** The number of milliseconds to delay calls to onMoveSelectionClick  */
  private readonly debouncedOnMoveSelectionClickMS: number = 500;

  /** The debounced function of onMoveSelectionClick event handler */
  private debouncedOnMoveSelectionClick:
    | ((() => void) & { cancel(): void; flush(): void })
    | undefined;

  /**
   * Constructor
   *
   * @param props Selection Window props as SelectionWindowProps
   */
  public constructor(props: SelectionWindowProps) {
    super(props);
    this.state = {};
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: SelectionWindowProps): void {
    // eslint-disable-next-line @typescript-eslint/unbound-method
    if (!isEqual(this.props.onMoveSelectionWindow, prevProps.onMoveSelectionWindow)) {
      if (this.debouncedOnMoveSelectionClick) {
        this.debouncedOnMoveSelectionClick.cancel();
      }
      this.debouncedOnMoveSelectionClick = this.props.onMoveSelectionWindow
        ? debounce(
            () => {
              if (this.props.onMoveSelectionWindow) {
                this.props.onMoveSelectionWindow(this.props.selectionWindow);
              }
            },
            this.debouncedOnMoveSelectionClickMS,
            { maxWait: this.debouncedOnMoveSelectionClickMS }
          )
        : undefined;
    }
  }

  /**
   * Catches exceptions generated in descendant components.
   * Unhandled exceptions will cause the entire component tree to unmount.
   *
   * @param error the error that was caught
   * @param info the information about the error
   */
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
  public componentDidCatch(error, info): void {
    logger.error(`Weavess Selection Window Error: ${error} : ${info}`);
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const percent100 = 100;

    const leftPercent = calculateLeftPercent(
      this.props.selectionWindow.startMarker.timeSecs,
      this.props.timeRange().startTimeSecs,
      this.props.timeRange().endTimeSecs
    );
    const rightPercent =
      percent100 -
      calculateLeftPercent(
        this.props.selectionWindow.endMarker.timeSecs,
        this.props.timeRange().startTimeSecs,
        this.props.timeRange().endTimeSecs
      );
    return (
      // eslint-disable-next-line jsx-a11y/no-static-element-interactions
      <div
        className="selection-window"
        onMouseDown={this.onMouseDown}
        onMouseMove={this.onMouseMove}
        onMouseUp={this.onMouseUp}
      >
        {/* eslint-disable-next-line jsx-a11y/no-static-element-interactions */}
        <div
          ref={ref => {
            this.timeWindowSelectionRef = ref;
          }}
          className="selection-window-selection"
          style={{
            backgroundColor: `${this.props.selectionWindow.color}`,
            left: `${leftPercent}%`,
            right: `${rightPercent}%`,
            cursor: this.props.selectionWindow.isMoveable ? 'move' : 'auto'
          }}
          onMouseDown={this.onSelectionWindowClick}
          onDoubleClick={this.handleSingleDoubleClick.onDoubleClick}
        />
        {this.createMarkers()}
      </div>
    );
  }

  /**
   * Selection window on click logic, creates mouse move and mouse down
   * Listeners to determine where to move the window and the markers.
   *
   * @param event
   */
  private readonly onSelectionWindowClick = (event: React.MouseEvent<HTMLDivElement>) => {
    if (event.button === 2 || event.altKey || event.ctrlKey || event.metaKey) return;

    event.stopPropagation();

    if (!this.mouseDown) {
      this.mouseDown = true;

      // TODO accurately calculate the move distance to ensure that the selection window stays with the mouse position
      // TODO see createSelectionWindowMarkers in the waveform-panel: this.waveformsViewportRef
      const htmlEle: HTMLDivElement = event.target as HTMLDivElement;
      const timeRange = this.props.timeRange().endTimeSecs - this.props.timeRange().startTimeSecs;
      const mouseXOffset = event.clientX - htmlEle.offsetLeft;
      const viewPortWidth = this.props.viewportClientWidth();
      const percentFrac = 100;
      let startXPercent = (event.clientX - mouseXOffset) / viewPortWidth;

      // calculate initial start time
      const timeSecs = this.getTimeSecsForClientX(event.clientX);

      // eslint-disable-next-line complexity
      const onMouseMove = (mouseMoveEvent: MouseEvent) => {
        if (this.mouseDown) {
          const currentXPercent = (mouseMoveEvent.clientX - mouseXOffset) / viewPortWidth;

          let diffPct = startXPercent - currentXPercent;

          const diffTimeSecs = timeRange * diffPct;

          // the mouse is considered to be dragging if the user has moved greater than 50ms
          const mouseMoveConstraint = 0.05; // represents 50 ms

          if (!this.isDragging && Math.abs(diffTimeSecs) > mouseMoveConstraint) {
            this.isDragging = true;
          }

          diffPct *= percentFrac;

          if (this.isDragging) {
            if (htmlEle && htmlEle.style.left && htmlEle.style.right) {
              let divLeftPercent = htmlEle.style.left
                ? parseFloat(htmlEle.style.left) - diffPct
                : 0;
              let divRightPercent = htmlEle.style.right
                ? parseFloat(htmlEle.style.right) + diffPct
                : 0;

              if (
                this.leadBorderRef &&
                this.leadBorderRef.containerRef &&
                this.leadBorderRef.containerRef.style.left &&
                this.endBorderRef &&
                this.endBorderRef.containerRef &&
                this.endBorderRef.containerRef.style.left
              ) {
                const leadPosition = parseFloat(this.leadBorderRef.containerRef.style.left);
                const lagPosition = parseFloat(this.endBorderRef.containerRef.style.left);
                let leadPositionPercent = leadPosition - diffPct;
                let lagPositionPercent = lagPosition - diffPct;

                // Guard to ensure stays on waveform
                // Guard to ensure stays with min and max constraints
                if (
                  leadPositionPercent <
                    (this.leadBorderRef as MoveableMarker).getMinConstraintPercentage() ||
                  lagPositionPercent >
                    (this.endBorderRef as MoveableMarker).getMaxConstraintPercentage()
                ) {
                  leadPositionPercent = leadPosition;
                  lagPositionPercent = lagPosition;
                  divLeftPercent = parseFloat(htmlEle.style.left);
                  divRightPercent = parseFloat(htmlEle.style.right);
                } else {
                  // !FIX DO NOT SET THE PROPS DIRECTLY
                  this.props.selectionWindow.startMarker.timeSecs -= diffTimeSecs;
                  this.props.selectionWindow.endMarker.timeSecs -= diffTimeSecs;
                  startXPercent = currentXPercent;
                }

                htmlEle.style.left = `${divLeftPercent}%`;
                htmlEle.style.right = `${divRightPercent}%`;
                this.leadBorderRef.containerRef.style.left = `${leadPositionPercent}%`;
                this.endBorderRef.containerRef.style.left = `${lagPositionPercent}%`;
              }

              if (this.debouncedOnMoveSelectionClick) {
                this.debouncedOnMoveSelectionClick();
              }
            }
          }
        }
      };

      const onMouseUp = (mouseUpEvent: MouseEvent) => {
        document.body.removeEventListener('mousemove', onMouseMove);
        document.body.removeEventListener('mouseup', onMouseUp);

        if (this.debouncedOnMoveSelectionClick) {
          this.debouncedOnMoveSelectionClick.cancel();
        }

        if (this.isDragging) {
          // only update if the selection window is moveable; no false updates
          if (this.props.selectionWindow.isMoveable && this.props.onUpdateSelectionWindow) {
            this.props.onUpdateSelectionWindow(this.props.selectionWindow);
          }
        } else {
          // handle a single click event
          this.handleSingleDoubleClick.onSingleClickEvent(mouseUpEvent, () => {
            if (this.props.onClickSelectionWindow && timeSecs) {
              this.props.onClickSelectionWindow(this.props.selectionWindow, timeSecs);
            }
          });
        }

        this.isDragging = false;
        this.mouseDown = false;
      };

      document.body.addEventListener('mousemove', onMouseMove);
      document.body.addEventListener('mouseup', onMouseUp);
    }
  };

  /**
   * Create boarder markers
   */
  private readonly createMarkers = (): JSX.Element[] => {
    if (!this.props.selectionWindow) return [];
    const borderMarkers: JSX.Element[] = [];
    const borderMarkersKey = 'moveable-marker-start';
    const verticalMarkerKey = 'vertical-marker-start';
    borderMarkers.push(
      this.props.selectionWindow.isMoveable ? (
        <MoveableMarker
          ref={ref => {
            this.leadBorderRef = ref;
          }}
          name={borderMarkersKey}
          marker={this.props.selectionWindow.startMarker}
          associatedEndMarker={this.props.selectionWindow.endMarker}
          labelWidthPx={this.props.labelWidthPx}
          percentageLocation={calculateLeftPercent(
            this.props.selectionWindow.startMarker.timeSecs,
            this.props.timeRange().startTimeSecs,
            this.props.timeRange().endTimeSecs
          )}
          containerClientWidth={this.props.containerClientWidth}
          viewportClientWidth={this.props.viewportClientWidth}
          updateTimeWindowSelection={this.updateTimeWindowSelection}
          timeRange={this.props.timeRange}
          getZoomRatio={this.props.getZoomRatio}
          onUpdateMarker={this.onUpdateMarker}
        />
      ) : (
        <VerticalMarker
          name={verticalMarkerKey}
          color={this.props.selectionWindow.startMarker.color}
          lineStyle={this.props.selectionWindow.startMarker.lineStyle}
          percentageLocation={calculateLeftPercent(
            this.props.selectionWindow.startMarker.timeSecs,
            this.props.timeRange().startTimeSecs,
            this.props.timeRange().endTimeSecs
          )}
        />
      )
    );
    const moveableMarkerKey = 'moveable-marker-end';
    const verticalMarkerEndKey = 'vertical-marker-end';
    borderMarkers.push(
      this.props.selectionWindow.isMoveable ? (
        <MoveableMarker
          ref={ref => {
            this.endBorderRef = ref;
          }}
          name={moveableMarkerKey}
          labelWidthPx={this.props.labelWidthPx}
          marker={this.props.selectionWindow.endMarker}
          associatedStartMarker={this.props.selectionWindow.startMarker}
          percentageLocation={calculateLeftPercent(
            this.props.selectionWindow.endMarker.timeSecs,
            this.props.timeRange().startTimeSecs,
            this.props.timeRange().endTimeSecs
          )}
          containerClientWidth={this.props.containerClientWidth}
          viewportClientWidth={this.props.viewportClientWidth}
          updateTimeWindowSelection={this.updateTimeWindowSelection}
          getZoomRatio={this.props.getZoomRatio}
          timeRange={this.props.timeRange}
          onUpdateMarker={this.onUpdateMarker}
        />
      ) : (
        <VerticalMarker
          name={verticalMarkerEndKey}
          color={this.props.selectionWindow.startMarker.color}
          lineStyle={this.props.selectionWindow.startMarker.lineStyle}
          percentageLocation={calculateLeftPercent(
            this.props.selectionWindow.endMarker.timeSecs,
            this.props.timeRange().startTimeSecs,
            this.props.timeRange().endTimeSecs
          )}
        />
      )
    );
    return borderMarkers;
  };

  /**
   * Handles the on update marker event and updates the selection
   */
  private readonly onUpdateMarker = () => {
    // only update if the selection window is moveable; no false updates
    if (this.props.selectionWindow.isMoveable && this.props.onUpdateSelectionWindow) {
      this.props.onUpdateSelectionWindow(this.props.selectionWindow);
    }
  };

  /**
   * update time window div based on vertical markers moving
   */
  private readonly updateTimeWindowSelection = () => {
    if (
      !this.timeWindowSelectionRef ||
      !this.endBorderRef ||
      !this.leadBorderRef ||
      !this.leadBorderRef.containerRef ||
      !this.endBorderRef.containerRef
    ) {
      return;
    }

    const percent100 = 100;
    if (this.leadBorderRef.containerRef.style.left && this.endBorderRef.containerRef.style.left) {
      this.timeWindowSelectionRef.style.left = `${parseFloat(
        this.leadBorderRef.containerRef.style.left
      )}%`;
      this.timeWindowSelectionRef.style.right = `${
        percent100 - parseFloat(this.endBorderRef.containerRef.style.left)
      }%`;
    }
  };

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

    if (!canvasRef) return undefined;

    const offset = canvasRef.getBoundingClientRect();
    // eslint-disable-next-line consistent-return
    if (clientX < offset.left && clientX > offset.right) return undefined;

    // position in [0,1] in the current channel bounds.
    const position = (clientX - offset.left) / offset.width;
    const time = this.props.computeTimeSecsForMouseXPosition(position);
    // eslint-disable-next-line consistent-return
    return time;
  };

  /**
   * onMouseDown event handler.
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onMouseDown = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!this.timeWindowSelectionRef) return;
    this.props.onMouseDown(e);
  };

  /**
   * onMouseMove event handler.
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!this.timeWindowSelectionRef) return;
    this.props.onMouseMove(e);
  };

  /**
   * onMouseUp event handler.
   *
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onMouseUp = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!this.timeWindowSelectionRef) return;
    this.props.onMouseUp(e);
  };
}
