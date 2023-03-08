/* eslint-disable react/destructuring-assignment */
import { IconNames } from '@blueprintjs/icons';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '@gms/ui-util';
import React from 'react';

import {
  DataType,
  TableDataState,
  TableInvalidState
} from '~analyst-ui/common/utils/table-invalid-state';
import { systemConfig, userPreferences } from '~analyst-ui/config';

import { MagnitudePanel } from './magnitude-panel';
import type { MagnitudeComponentState, MagnitudeProps } from './types';

// Fix in the future when converted to event, SD and station use hooks
const dummyEventsInTimeRangeQuery = {
  isLoading: false,
  data: []
};
const dummySignalDetectionsByStationQuery = {
  isLoading: false,
  data: []
};
const dummyStationQuery = {
  isLoading: false,
  data: []
};

/**
 * Magnitude display, displays various data for location solutions. It is composed of two tables
 * Network Magnitude and Station Magnitude.
 */
export class Magnitude extends React.Component<MagnitudeProps, MagnitudeComponentState> {
  /**
   * constructor
   */
  public constructor(props: MagnitudeProps) {
    super(props);

    this.state = {
      displayedMagnitudeTypes: userPreferences.initialMagType
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount(): void {
    addGlForceUpdateOnShow(this.props.glContainer, this);
    addGlForceUpdateOnResize(this.props.glContainer, this);
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Setting the state for magnitude types
   *
   * @param displayedMagnitudeTypes the Magnitude Types to be displayed
   */
  private readonly setDisplayedMagnitudeTypes = (
    displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes
  ) => {
    this.setState({ displayedMagnitudeTypes });
  };

  /**
   * Renders the component.
   */
  public render(): JSX.Element {
    let dataState: TableDataState = TableDataState.READY;
    if (!dummyEventsInTimeRangeQuery.data || !dummySignalDetectionsByStationQuery.data) {
      dataState = TableDataState.NO_INTERVAL;
      // eslint-disable-next-line no-dupe-else-if
    } else if (!dummyEventsInTimeRangeQuery.data && dummyEventsInTimeRangeQuery.isLoading) {
      dataState = TableDataState.NO_EVENTS;
    } else if (
      dummySignalDetectionsByStationQuery.isLoading ||
      !dummySignalDetectionsByStationQuery.data
    ) {
      dataState = TableDataState.NO_SDS;
    } else if (!this.props.openEventId) {
      dataState = TableDataState.NO_EVENT_OPEN;
    }

    if (dataState !== TableDataState.READY) {
      return (
        <TableInvalidState
          visual={IconNames.CHANGES}
          message={dataState}
          dataType={dataState === TableDataState.NO_SDS ? DataType.SD : DataType.EVENT}
          noEventMessage="Select an event to adjust magnitude"
        />
      );
    }
    const currentlyOpenEvent = dummyEventsInTimeRangeQuery.data.find(
      event => event.id === this.props.openEventId
    );

    const associatedSds = undefined;

    return (
      <MagnitudePanel
        stations={dummyStationQuery.data}
        eventsInTimeRange={dummyEventsInTimeRangeQuery.data}
        associatedSignalDetections={associatedSds}
        widthPx={this.props.glContainer ? this.props.glContainer.width : 0}
        displayedMagnitudeTypes={this.state.displayedMagnitudeTypes}
        location={this.props.location}
        currentlyOpenEvent={currentlyOpenEvent}
        magnitudeTypesForPhase={systemConfig.magnitudeTypesForPhase}
        setDisplayedMagnitudeTypes={this.setDisplayedMagnitudeTypes}
        selectedSdIds={this.props.selectedSdIds}
        setSelectedSdIds={this.props.setSelectedSdIds}
        setSelectedLocationSolution={this.props.setSelectedLocationSolution}
        computeNetworkMagnitudeSolution={this.props.computeNetworkMagnitudeSolution}
        openEventId={this.props.openEventId}
      />
    );
  }
}
