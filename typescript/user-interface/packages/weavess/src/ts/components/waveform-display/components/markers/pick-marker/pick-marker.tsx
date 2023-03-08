/* eslint-disable react/destructuring-assignment */
import { Icon, Intent } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { UILogger } from '@gms/ui-util';
import { WeavessMessages } from '@gms/weavess-core';
import classNames from 'classnames';
import React from 'react';
import { toast } from 'react-toastify';

import { calculateLeftPercent } from '../../../utils';
import type { PickMarkerProps, PickMarkerState } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

const ICON_SIZE_PX = 14;

/**
 * An interactive marker, that is configurable, and can have specific events.
 */
export class PickMarker extends React.PureComponent<PickMarkerProps, PickMarkerState> {
  /** container reference */
  private containerRef: HTMLDivElement | null;

  /** line reference */
  private lineRef: HTMLDivElement | null;

  /** label reference */
  private labelRef: HTMLDivElement | null;

  /**
   * Constructor
   *
   * @param props props as PickMarkerProps
   */
  public constructor(props: PickMarkerProps) {
    super(props);
    this.state = {
      position: this.props.position
    };
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
  public componentDidUpdate(prevProps: PickMarkerProps): void {
    if (!this.lineRef) return;

    if (prevProps.position !== this.props.position) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ position: this.props.position });
    } else {
      // if the color changes, flash animation
      // eslint-disable-next-line no-lonely-if
      if (prevProps.color !== this.props.color) {
        this.lineRef.style.borderColor = this.props.color;
        setTimeout(() => {
          if (!this.lineRef) return;

          this.lineRef.style.borderColor = this.props.color;
          this.lineRef.style.transition = 'border-color 0.5s ease-in';
          setTimeout(() => {
            if (!this.lineRef) return;
            this.lineRef.style.transition = '';
            // eslint-disable-next-line @typescript-eslint/no-magic-numbers, @typescript-eslint/indent
          }, 500);
          // eslint-disable-next-line @typescript-eslint/no-magic-numbers, @typescript-eslint/indent
        }, 500);
      }
    }
  }

  /**
   * Catches exceptions generated in descendant components.
   * Unhandled exceptions will cause the entire component tree to un-mount.
   *
   * @param error the error that was caught
   * @param info the information about the error
   */
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
  public componentDidCatch(error, info): void {
    logger.error(`Weavess Pick Marker Error: ${error} : ${info}`);
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  // eslint-disable-next-line react/sort-comp, complexity
  public render(): JSX.Element {
    // show the pointer cursor if the click or drag events are defined
    const cursor: React.CSSProperties =
      this.props.onClick || this.props.onDragEnd ? { cursor: 'pointer' } : { cursor: 'auto' };

    return (
      // eslint-disable-next-line jsx-a11y/no-static-element-interactions
      <div
        className={classNames([
          `pick-marker`,
          {
            'pick-marker--selected': this.props.isSelected
          },
          {
            'pick-marker--selectable': this.props.isSelectable
          }
        ])}
        ref={ref => {
          this.containerRef = ref;
        }}
        style={{ '--pick-marker-color': this.props.color } as React.CSSProperties}
        onMouseDown={this.onMouseDown}
      >
        <div
          className="pick-marker__vertical"
          data-cy-predicted={`pick-marker-pick-predicted-${this.props.predicted}`}
          ref={ref => {
            this.lineRef = ref;
          }}
          style={{
            left: `${this.state.position}%`,
            filter: this.props.filter
          }}
        />
        {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
        <div
          className={classNames([
            `pick-marker__label`,
            { 'pick-marker__label--top': !this.props.predicted },
            { 'pick-marker__label--bottom': this.props.predicted }
          ])}
          data-cy={`pick-marker-${this.props.id}`}
          data-cy-color={`pick-marker-${this.props.color}`}
          data-cy-is-predicted-phase={this.props.predicted}
          data-cy-phase={this.props.label}
          data-cy-style-left={this.state.position}
          onContextMenu={this.props.onContextMenu ? this.onContextMenu : undefined}
          style={{
            left: `calc(4px + ${this.state.position}%)`,
            filter: this.props.filter,
            ...cursor
          }}
          ref={ref => {
            this.labelRef = ref;
          }}
        >
          {this.props.label}
          {this.props.isConflicted ? (
            <Icon
              icon={IconNames.ISSUE}
              iconSize={ICON_SIZE_PX}
              intent={Intent.DANGER}
              className="pick-marker__conflict"
            />
          ) : null}
        </div>
      </div>
    );
  }

  /**
   * referentially stable handler for pick markers
   *
   * @param e
   */
  private readonly onClick = (e: React.MouseEvent<HTMLDivElement>): void => {
    if (this.props.onClick) {
      this.props.onClick(e, this.props.id);
    }
  };

  /**
   * onContextMenu menu event handler for pick markers
   *
   * @param e
   */
  private readonly onContextMenu = (e: React.MouseEvent<HTMLDivElement>): void => {
    e.stopPropagation();
    if (this.props.onContextMenu) {
      this.props.onContextMenu(e, this.props.channelId, this.props.id);
    }
  };

  /**
   * onMouseDown event handler for signal detections
   *
   * @param e
   */
  private readonly onMouseDown = (e: React.MouseEvent<HTMLDivElement>): void => {
    this.onClick(e);
    // if holding any meta key down then not picking on a Signal Detection
    if (e.altKey || e.shiftKey || e.ctrlKey || e.metaKey) {
      return;
    }

    // prevent propagation of these events so that the underlying channel click doesn't register
    e.stopPropagation();

    // if context-menu, don't trigger
    if (e.button === 2) return;

    if (this.props.isConflicted) {
      toast.info(WeavessMessages.signalDetectionInConflict);
    } else if (this.props.onDragEnd) {
      this.addDragListenerForMove(e);
    }
  };

  /**
   * Add a drag listener for pick marker move modification. This
   * listener will be removed on mouse up event
   *
   * @param e MouseEvent from mouseDown
   */
  // TODO to ensure that we are cleaning up after ourselves,
  // and maybe to clean this function up a bit into something more generalized that we know is safe.
  private readonly addDragListenerForMove = (e: React.MouseEvent<HTMLDivElement>): void => {
    const start = e.clientX;
    let currentPos = e.clientX;
    let isDragging = false;
    let diff = 0;

    const onMouseMove = (event: MouseEvent) => {
      if (!this.containerRef) return;

      currentPos = event.clientX;
      diff = Math.abs(currentPos - start);
      // begin drag if moving more than 1 pixel
      if (diff > 1 && !isDragging) {
        isDragging = true;
        if (this.labelRef) {
          this.labelRef.style.filter = 'brightness(0.5)';
        }
        if (this.lineRef) {
          this.lineRef.style.filter = 'brightness(0.5)';
        }
        this.props.toggleDragIndicator(true, this.props.color);
      }
      if (isDragging) {
        this.props.positionDragIndicator(currentPos);
      }
    };

    const onMouseUp = (event: MouseEvent) => {
      try {
        event.stopPropagation();
        if (!this.containerRef || !isDragging) return;
        this.props.toggleDragIndicator(false, this.props.color);
        if (this.labelRef) {
          this.labelRef.style.filter = this.props.filter ? this.props.filter : 'initial';
        }
        if (this.lineRef) {
          this.lineRef.style.filter = this.props.filter ? this.props.filter : 'initial';
        }
        const time = this.props.getTimeSecsForClientX(currentPos);
        if (time != null) {
          this.setState(
            {
              position: calculateLeftPercent(time, this.props.startTimeSecs, this.props.endTimeSecs)
            },
            () => {
              if (this.props.onDragEnd) {
                this.props.onDragEnd(this.props.id, time);
              }
            }
          );
        }
      } finally {
        document.body.removeEventListener('mousemove', onMouseMove);
        document.body.removeEventListener('mouseup', onMouseUp);
      }
    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  };
}
