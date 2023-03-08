/* eslint-disable react/destructuring-assignment */
import type GoldenLayout from '@gms/golden-layout';
import type { AppState } from '@gms/ui-state';
import { setSelectedStationIds, ssamControlApiSlice } from '@gms/ui-state';
import isEqual from 'lodash/isEqual';
import memoizeOne from 'memoize-one';
import React from 'react';
import type { ConnectedProps } from 'react-redux';
import { connect } from 'react-redux';

import { BaseDisplay } from '~components/common-ui/components/base-display';
import type { WithAcknowledgeProps } from '~components/data-acquisition-ui/shared/acknowledge';

import type { SohOverviewContextData } from './soh-overview-context';
import { SohOverviewContext } from './soh-overview-context';
import { SohOverviewPanel } from './soh-overview-panel';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState) => ({
  selectedStationIds: state.app.common.selectedStationIds,
  sohStatus: state.app.dataAcquisition.data.sohStatus,
  sohConfigurationQuery: ssamControlApiSlice.endpoints.getSohConfiguration.select()(state)
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 */
const mapDispatchToProps = {
  setSelectedStationIds,
  getSohConfiguration: ssamControlApiSlice.endpoints.getSohConfiguration.initiate
};

export const sohOverviewConnector = connect(mapStateToProps, mapDispatchToProps);

type ConnectedReduxProps = ConnectedProps<typeof sohOverviewConnector>;

/**
 * SohSummary props
 */
export type SohOverviewProps = ConnectedReduxProps &
  WithAcknowledgeProps & {
    glContainer?: GoldenLayout.Container;
  };

/**
 * Parent soh component using query to get soh status and pass down to Soh Overview
 */
export class SohOverviewComponent extends React.PureComponent<SohOverviewProps> {
  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************
  private unsubscribe: null | (() => void) = null;

  private memoizedSohOverviewContextData: () => SohOverviewContextData;

  public componentDidMount(): void {
    const { getSohConfiguration } = this.props;
    // eslint-disable-next-line @typescript-eslint/unbound-method
    const { unsubscribe } = getSohConfiguration();
    this.unsubscribe = unsubscribe;
  }

  public componentWillUnmount(): void {
    this.unsubscribe?.();
  }

  /**
   * Renders the component.
   */
  public render(): JSX.Element {
    this.memoizedSohOverviewContextData = memoizeOne(() => {
      return {
        sohStationStaleTimeMS: this.props.sohConfigurationQuery.data.sohStationStaleMs,
        acknowledgeSohStatus: this.props.acknowledgeStationsByName,
        glContainer: this.props.glContainer,
        selectedStationIds: this.props.selectedStationIds ? this.props.selectedStationIds : [],
        setSelectedStationIds: this.props.setSelectedStationIds,
        stationSoh: this.props.sohStatus.stationAndStationGroupSoh.stationSoh,
        stationGroupSoh: this.props.sohStatus.stationAndStationGroupSoh.stationGroups,
        quietTimerMs: this.props.sohConfigurationQuery.data.acknowledgementQuietMs,
        updateIntervalSecs: this.props.sohConfigurationQuery.data.reprocessingPeriodSecs
      };
    }, isEqual);
    return (
      <SohOverviewContext.Provider value={this.memoizedSohOverviewContextData()}>
        <BaseDisplay
          glContainer={this.props.glContainer}
          className="soh-overview soh-overview-display"
          data-cy="soh-overview-display"
        >
          <SohOverviewPanel />
        </BaseDisplay>
      </SohOverviewContext.Provider>
    );
  }
}
