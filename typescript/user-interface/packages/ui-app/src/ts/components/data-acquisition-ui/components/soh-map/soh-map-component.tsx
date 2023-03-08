/* eslint-disable react/destructuring-assignment */
import React from 'react';

import { SohMapPanel } from './soh-map-panel';
import type { SohMapProps } from './types';

const MIN_CHART_HEIGHT_PX = 300;

/**
 * State of health map component.
 */
// eslint-disable-next-line react/prefer-stateless-function
export class SohMapComponent extends React.Component<SohMapProps> {
  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Renders the component.
   */
  public render(): JSX.Element {
    return (
      <SohMapPanel
        minHeightPx={MIN_CHART_HEIGHT_PX}
        selectedStationIds={this.props.selectedStationIds ? this.props.selectedStationIds : []}
        sohStatus={this.props.sohStatus}
        setSelectedStationIds={this.props.setSelectedStationIds}
      />
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************
}
