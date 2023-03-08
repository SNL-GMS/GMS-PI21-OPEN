/* eslint-disable react/destructuring-assignment */
import { UILogger } from '@gms/ui-util';
import React from 'react';

import type { UncertaintyMarkerProps } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

/**
 * An interactive marker, that is configurable, and can have specific events.
 */
export class UncertaintyMarker extends React.PureComponent<UncertaintyMarkerProps> {
  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

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
    const leftPos = this.props.isLeftUncertaintyBar
      ? this.props.position
      : this.props.pickMarkerPosition;
    const width = Math.abs(this.props.position - this.props.pickMarkerPosition);
    return (
      // eslint-disable-next-line jsx-a11y/no-static-element-interactions
      <div className="uncertainty-marker">
        <div
          className="uncertainty-marker--vertical"
          style={{
            borderLeft: `1.5px solid ${this.props.color}`,
            left: `${this.props.position}%`,
            boxShadow: 'initial'
          }}
        />
        <div
          className="uncertainty-marker--horizontal"
          style={{
            borderTop: `1.5px solid ${this.props.color}`,
            left: `${leftPos}%`,
            width: `${width}%`,
            boxShadow: 'initial'
          }}
        />
      </div>
    );
  }
}
