/* eslint-disable react/destructuring-assignment */
import type { SohTypes } from '@gms/common-model';
import type { ValueType } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import type { AppState } from '@gms/ui-state';
import { setSelectedStationIds, ssamControlApiSlice } from '@gms/ui-state';
import * as React from 'react';
import type { ConnectedProps } from 'react-redux';
import { connect } from 'react-redux';

import { BaseDisplay } from '~components/common-ui/components/base-display';
import { dataAcquisitionUIConfig } from '~components/data-acquisition-ui/config';

import type { Type } from './bar-chart/types';
import { SohBarChartPanel } from './soh-bar-chart-panel';

const MIN_CHART_HEIGHT_PX = dataAcquisitionUIConfig.dataAcquisitionUserPreferences.minChartHeightPx;

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (type: Type, valueType: ValueType) => (state: AppState) => ({
  type,
  valueType,
  selectedStationIds: state.app.common.selectedStationIds,
  sohStatus: state.app.dataAcquisition.data.sohStatus,
  sohConfigurationQuery: ssamControlApiSlice.endpoints.getSohConfiguration.select()(state)
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = {
  setSelectedStationIds,
  getSohConfiguration: ssamControlApiSlice.endpoints.getSohConfiguration.initiate
};

export const sohBarChartConnector = (type: Type, valueType: ValueType) =>
  connect(mapStateToProps(type, valueType), mapDispatchToProps);

type ConnectedReduxProps = ConnectedProps<ReturnType<typeof sohBarChartConnector>>;

/**
 * SohBarChartProps props
 */
export type SohBarChartProps = ConnectedReduxProps & {
  glContainer?: GoldenLayout.Container;
  type: Type;
  valueType: ValueType;
};

export class SohBarChart extends React.Component<SohBarChartProps> {
  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************
  private unsubscribe: null | (() => void) = null;

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
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <BaseDisplay
        glContainer={this.props.glContainer}
        className={`${this.props.type.toLocaleLowerCase()}-display top-level-container scroll-box scroll-box--y`}
      >
        <SohBarChartPanel
          minHeightPx={MIN_CHART_HEIGHT_PX}
          glContainer={this.props.glContainer}
          type={this.props.type}
          station={this.getStation()}
          sohStatus={this.props.sohStatus}
          sohConfiguration={this.props.sohConfigurationQuery.data}
          valueType={this.props.valueType}
        />
      </BaseDisplay>
    );
  }
  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /** Returns the selected station */
  private readonly getStation = (): SohTypes.UiStationSoh =>
    this.props.sohStatus.stationAndStationGroupSoh.stationSoh.find(
      s => s.stationName === this.props.selectedStationIds[0]
    );
}
