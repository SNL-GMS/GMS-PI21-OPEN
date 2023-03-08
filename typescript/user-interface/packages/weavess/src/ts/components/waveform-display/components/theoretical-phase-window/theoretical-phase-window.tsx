/* eslint-disable react/destructuring-assignment */
import { UILogger } from '@gms/ui-util';
import React from 'react';

import type { TheoreticalPhaseWindowProps, TheoreticalPhaseWindowState } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

/**
 * Displays a window of time on a channel where a phase may theoretically exist.
 */
export class TheoreticalPhaseWindow extends React.PureComponent<
  TheoreticalPhaseWindowProps,
  TheoreticalPhaseWindowState
> {
  /**
   * Constructor
   *
   * @param Theoretical Phase Window Props
   * @param props
   */
  public constructor(props: TheoreticalPhaseWindowProps) {
    super(props);
    this.state = {};
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
    logger.error(`Weavess Theoretical Phase Window Error: ${error} : ${info}`);
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  public render(): JSX.Element {
    return (
      <div className="theoretical-phase-window">
        <div
          className="theoretical-phase-window-selection"
          style={{
            backgroundColor: this.props.color,
            left: `${this.props.left}%`,
            right: `${this.props.right}%`
          }}
        />
        <div
          className="theoretical-phase-window-label"
          style={{
            color: this.props.color,
            left: `${this.props.left}%`,
            right: `${this.props.right}%`
          }}
        >
          {this.props.label}
        </div>
      </div>
    );
  }
}
