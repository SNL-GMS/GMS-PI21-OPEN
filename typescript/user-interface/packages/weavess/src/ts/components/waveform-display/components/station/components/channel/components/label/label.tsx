/* eslint-disable react/destructuring-assignment */
import { Tooltip2 } from '@blueprintjs/popover2';
import { classList, UILogger } from '@gms/ui-util';
import type { WeavessTypes } from '@gms/weavess-core';
import React from 'react';

import { YAxis } from '../../../../../axes';
import { DistanceAzimuth } from './distance-azimuth';
import type { LabelState } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

/**
 * Label component. Describes a waveform (or other graphic component) and has optional events
 */
export class Label extends React.PureComponent<WeavessTypes.LabelProps, LabelState> {
  /** The y-axis references. */
  public yAxisRefs: { [id: string]: YAxis | null } = {};

  /**
   * Constructor
   *
   * @param props Label props as LabelProps
   */
  public constructor(props: WeavessTypes.LabelProps) {
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
  public componentDidUpdate(prevProps: WeavessTypes.LabelProps): void {
    // should we force re-draw the yAxis?
    // Note that the y-axis will redraw based on its changed props,
    // so we don't need to check for changes to the bounds
    if (
      this.props.channel.id !== prevProps.channel.id ||
      this.props.channel?.height !== prevProps.channel?.height ||
      this.props.channel.yAxisTicks !== prevProps.channel.yAxisTicks
    ) {
      this.refreshYAxis();
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
    logger.error(`Weavess Label Error: ${error} : ${info}`);
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  // eslint-disable-next-line react/sort-comp, complexity
  public render(): JSX.Element {
    const isSelected =
      this.props.selections.channels &&
      this.props.selections.channels.indexOf(this.props.channel.id) > -1;
    return (
      <div
        onContextMenu={this.onContextMenu}
        // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex
        tabIndex={0}
        className={`label${this.props?.events?.onChannelLabelClick ? ' label--actionable' : ''}`}
      >
        <div className={`label-container${isSelected ? ' is-selected' : ''}`}>
          {this.props.isDefaultChannel ? (
            /* render parent label with expansion button */
            <div className="label-container-left-parent">
              {
                /* render expansion button if there are additional channels */
                this.props.isExpandable ? (
                  // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
                  <div
                    className="label-container-left-parent-expansion-button"
                    data-cy="weavess-expand-parent"
                    data-cy-channel-name={this.props.channel.id}
                    onClick={this.labelContainerOnClick}
                  >
                    {this.props.expanded ? '-' : '+'}
                  </div>
                ) : null
              }
            </div>
          ) : (
            /* render child label without expansion button */
            <div className="label-container-left-child" />
          )}
          <div className="label-container-content">
            {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
            <div
              className={`label-container-content-label${isSelected ? ' is-selected' : ''}`}
              onClick={this.labelContainerContentOnClick}
              data-cy={`channel-label-${this.props.channel.id}`}
            >
              {this.props.customLabel ? (
                // eslint-disable-next-line react/jsx-props-no-spreading
                <this.props.customLabel {...this.props} />
              ) : (
                <div>
                  <span className="weavess-tooltip__target label-container-content">
                    <Tooltip2
                      disabled={!this.props.channelLabelTooltip}
                      className={classList({
                        'weavess-tooltip': true,
                        'weavess-tooltip--help': !!this.props.channelLabelTooltip
                      })}
                      content={this.props.channelLabelTooltip}
                      placement="right"
                      hoverOpenDelay={250} // ms
                    >
                      {this.props.channel.name}
                    </Tooltip2>
                    <div className="distance-azimuth">
                      <p>
                        <DistanceAzimuth
                          azimuth={this.props.azimuth}
                          distance={this.props.distance}
                          distanceUnits={this.props.distanceUnits}
                        />
                        <span className="label-container-content-mask-indicator">
                          {this.props.showMaskIndicator ? 'M' : null}
                        </span>
                      </p>
                    </div>
                  </span>
                </div>
              )}
            </div>
            <div
              style={{
                height: '100%'
              }}
              data-cy-contains-amplitude-markers={this.props.yAxisBounds.length > 0}
            >
              {this.props.suppressLabelYAxis
                ? undefined
                : this.props.yAxisBounds.map((yAxisBounds, index) => (
                    <YAxis
                      // eslint-disable-next-line react/no-array-index-key
                      key={`${this.props.channel.id}_yaxis_${index}`}
                      ref={ref => {
                        this.yAxisRefs[index] = ref;
                      }}
                      maxAmplitude={yAxisBounds.maxAmplitude}
                      minAmplitude={yAxisBounds.minAmplitude}
                      heightInPercentage={yAxisBounds.heightInPercentage}
                      yAxisTicks={this.props.channel.yAxisTicks}
                    />
                  ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  /**
   * Refreshes the y-axis for the label
   */
  public readonly refreshYAxis = (): void => {
    if (this.yAxisRefs) {
      Object.keys(this.yAxisRefs).forEach(key => {
        const yAxis = this.yAxisRefs[key];
        if (yAxis) {
          yAxis.display();
        }
      });
    }
  };

  /**
   * The on context menu event handler
   *
   * @param e the mouse event
   */
  private readonly onContextMenu = (e: React.MouseEvent<HTMLDivElement, MouseEvent>): void => {
    if (this.props.events?.onContextMenu && (e.button === 2 || (e.button === 0 && e.ctrlKey))) {
      e.preventDefault();
      this.props.events.onContextMenu(
        e,
        this.props.channelName,
        this.props.yAxisBounds.length > 0 ? this.props.yAxisBounds[0].minAmplitude : -1,
        this.props.yAxisBounds.length > 0 ? this.props.yAxisBounds[0].maxAmplitude : -1,
        this.props.isDefaultChannel
      );
    }

    // Prevents chrome context menu from appearing if
    // Left mouse + control is used to summon context menu
    if (this.props.events && this.props.events.onChannelLabelClick && e.button === 0 && e.ctrlKey) {
      e.preventDefault();
      e.stopPropagation();
      this.props.events.onChannelLabelClick(e, this.props.channel.id);
    }
  };

  /**
   * The label container on click event handler
   *
   * @param e the mouse event
   */
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  private readonly labelContainerOnClick = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    const isExpanded = !this.props.expanded;
    if (isExpanded) {
      if (this.props.events && this.props.events.onChannelExpanded) {
        this.props.events.onChannelExpanded(this.props.channel.id);
      }
    } else if (this.props.events && this.props.events.onChannelCollapsed) {
      this.props.events.onChannelCollapsed(this.props.channel.id);
    }
  };

  /**
   * The label container content on click event handler
   *
   * @param e the mouse event
   */
  private readonly labelContainerContentOnClick = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>
  ) => {
    if (this.props.events && this.props.events.onChannelLabelClick) {
      this.props.events.onChannelLabelClick(e, this.props.channel.id);
    }
  };
}
