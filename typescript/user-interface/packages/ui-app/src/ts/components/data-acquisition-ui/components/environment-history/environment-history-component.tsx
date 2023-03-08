/* eslint-disable react/destructuring-assignment */
import type { SohTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import { nonIdealStateWithNoSpinner, nonIdealStateWithSpinner } from '@gms/ui-core-components';
import type { AppState } from '@gms/ui-state';
import { dataAcquisitionActions, setSelectedStationIds, ssamControlApiSlice } from '@gms/ui-state';
import isEqual from 'lodash/isEqual';
import memoizeOne from 'memoize-one';
import React from 'react';
import type { ConnectedProps } from 'react-redux';
import { connect } from 'react-redux';

import { BaseDisplay } from '~components/common-ui/components/base-display';
import { isAnalogAceiMonitorType } from '~components/data-acquisition-ui/shared/utils';

import type { AceiContextData } from './acei-context';
import { AceiContext } from './acei-context';
import { EnvironmentHistoryPanel } from './environment-history-panel';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState) => ({
  selectedStationIds: state.app.common.selectedStationIds,
  sohStatus: state.app.dataAcquisition.data.sohStatus,
  selectedAceiType: state.app.dataAcquisition.selectedAceiType,
  sohConfigurationQuery: ssamControlApiSlice.endpoints.getSohConfiguration.select()(state)
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 */
const mapDispatchToProps = {
  setSelectedAceiType: dataAcquisitionActions.setSelectedAceiType,
  getSohConfiguration: ssamControlApiSlice.endpoints.getSohConfiguration.initiate,
  setSelectedStationIds
};

export const environmentHistoryConnector = connect(mapStateToProps, mapDispatchToProps);

type ConnectedReduxProps = ConnectedProps<typeof environmentHistoryConnector>;

export type EnvironmentHistoryProps = ConnectedReduxProps & {
  glContainer?: GoldenLayout.Container;
};

/**
 * Parent SOH Environment using query to get soh status
 */
export class EnvironmentHistoryComponent extends React.PureComponent<EnvironmentHistoryProps> {
  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************
  private unsubscribe: null | (() => void) = null;

  private memoizedGetSelectedAceiTypePropertyAndSetterObject: () => AceiContextData;

  public componentDidMount(): void {
    const { getSohConfiguration } = this.props;
    // eslint-disable-next-line @typescript-eslint/unbound-method
    const { unsubscribe } = getSohConfiguration();
    this.unsubscribe = unsubscribe;
  }

  public componentWillUnmount(): void {
    this.unsubscribe?.();
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    // Determine if non ideal state
    const isNonIdealState = this.isNonIdealState();
    if (isNonIdealState) {
      return isNonIdealState;
    }

    const station = this.getStation();
    const { channelSohs } = station;
    this.memoizedGetSelectedAceiTypePropertyAndSetterObject = memoizeOne(() => {
      return {
        selectedAceiType: this.props.selectedAceiType,
        setSelectedAceiType: this.props.setSelectedAceiType
      };
    }, isEqual);

    return (
      <BaseDisplay
        glContainer={this.props.glContainer}
        className="environment-history-display top-level-container scroll-box scroll-box--y full-width-height soh-env-component"
        onContextMenu={e => {
          e.preventDefault();
        }}
      >
        <AceiContext.Provider value={this.memoizedGetSelectedAceiTypePropertyAndSetterObject()}>
          <EnvironmentHistoryPanel
            station={station}
            channelSohs={channelSohs}
            sohHistoricalDurations={this.props.sohConfigurationQuery.data.sohHistoricalTimesMs}
          />
        </AceiContext.Provider>
      </BaseDisplay>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /** Returns the selected station */
  private readonly getStation = (): SohTypes.UiStationSoh => {
    if (this.props.sohStatus?.stationAndStationGroupSoh?.stationSoh) {
      return this.props.sohStatus.stationAndStationGroupSoh.stationSoh.find(
        s => s.stationName === this.props.selectedStationIds[0]
      );
    }
    return undefined;
  };

  /**
   * Checks the props and determines if we should go into a non ideal state for the component
   */
  // eslint-disable-next-line complexity
  private readonly isNonIdealState = () => {
    // If the golden-layout container is not visible, do not attempt to render
    // the component, this is to prevent JS errors that may occur when trying to
    // render the component while the golden-layout container is hidden

    const { glContainer, sohStatus, selectedStationIds, selectedAceiType } = this.props;

    const station = this.getStation();

    const channelSohs = station ? station.channelSohs : [];

    let returnValue;

    if (glContainer?.isHidden) {
      returnValue = nonIdealStateWithNoSpinner();
    } else if (!sohStatus) {
      returnValue = nonIdealStateWithSpinner('No SOH Data', 'Station SOH');
    } else if (sohStatus?.loading) {
      returnValue = nonIdealStateWithSpinner('Loading:', 'Station SOH');
    } else if (!sohStatus?.stationAndStationGroupSoh) {
      returnValue = nonIdealStateWithSpinner('No Station Group Data:', 'For SOH');
    } else if (!selectedStationIds || selectedStationIds.length === 0) {
      returnValue = nonIdealStateWithNoSpinner(
        'No Station Selected',
        'Select a station in SOH Overview or Station Statistics to view Environment'
      );
    } else if (!station) {
      returnValue = nonIdealStateWithSpinner('Loading:', 'Station SOH');
    } else if (selectedStationIds.length > 1) {
      returnValue = nonIdealStateWithNoSpinner(
        'Multiple Stations Selected',
        'Select one station to see Environment'
      );
    } else if (channelSohs === undefined) {
      returnValue = nonIdealStateWithSpinner('Loading', 'Channel SOH');
    } else if (channelSohs.length === 0) {
      returnValue = nonIdealStateWithNoSpinner(
        'No Channel Data',
        "Check this station's configuration"
      );
    } else if (isAnalogAceiMonitorType(selectedAceiType)) {
      returnValue = nonIdealStateWithNoSpinner(
        'Unsupported monitor type',
        'Analog environmental monitor types not supported at this time. Select a boolean monitor type to see historical trends.'
      );
    }
    return returnValue;
  };
}
