/* eslint-disable react/destructuring-assignment */
import type { SohTypes } from '@gms/common-model';
import { Displays } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import type { AppState } from '@gms/ui-state';
import { dataAcquisitionActions, setSelectedStationIds, ssamControlApiSlice } from '@gms/ui-state';
import Immutable from 'immutable';
import React from 'react';
import type { ConnectedProps } from 'react-redux';
import { connect } from 'react-redux';

import { BaseDisplay } from '~components/common-ui/components/base-display';
import { messageConfig } from '~components/data-acquisition-ui/config/message-config';
import { dataAcquisitionUserPreferences } from '~components/data-acquisition-ui/config/user-preferences';
import { DrillDownTitle } from '~components/data-acquisition-ui/shared/drill-down-components';
import type { SohContextData } from '~components/data-acquisition-ui/shared/soh-context';
import { SohContext } from '~components/data-acquisition-ui/shared/soh-context';
import {
  FilterableSOHTypes,
  FilterableSohTypesDisplayStrings
} from '~components/data-acquisition-ui/shared/toolbars/soh-toolbar';

import { EnvironmentPanel } from './soh-environment-panel';
import { EnvironmentToolbar } from './soh-environment-toolbar';

/** the filter item width in pixels */
const filterItemWidthPx = 240;

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState) => ({
  selectedStationIds: state.app.common.selectedStationIds,
  sohStatus: state.app.dataAcquisition.data.sohStatus,
  selectedAceiType: state.app.dataAcquisition.selectedAceiType,
  sohConfigurationQuery: ssamControlApiSlice.endpoints.getSohConfiguration.select()(state),
  channelStatusesToDisplay:
    state.app.dataAcquisition.filtersToDisplay[
      `${Displays.SohDisplays.SOH_ENVIRONMENT}-channel-statuses`
    ],
  monitorStatusesToDisplay:
    state.app.dataAcquisition.filtersToDisplay[
      `${Displays.SohDisplays.SOH_ENVIRONMENT}-monitor-statuses`
    ]
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = {
  setSelectedStationIds,
  setSelectedAceiType: dataAcquisitionActions.setSelectedAceiType,
  getSohConfiguration: ssamControlApiSlice.endpoints.getSohConfiguration.initiate,
  setMonitorStatusesToDisplay: (filters: Record<any, boolean>) => dispatch => {
    dispatch(
      dataAcquisitionActions.setFiltersToDisplay({
        list: `${Displays.SohDisplays.SOH_ENVIRONMENT}-monitor-statuses`,
        filters
      })
    );
  },
  setChannelStatusesToDisplay: (filters: Record<any, boolean>) => dispatch => {
    dispatch(
      dataAcquisitionActions.setFiltersToDisplay({
        list: `${Displays.SohDisplays.SOH_ENVIRONMENT}-channel-statuses`,
        filters
      })
    );
  }
};

export const sohEnvironmentConnector = connect(mapStateToProps, mapDispatchToProps);

type ConnectedReduxProps = ConnectedProps<typeof sohEnvironmentConnector>;

/**
 * SohEnvironment props
 */
export type EnvironmentProps = ConnectedReduxProps & {
  glContainer?: GoldenLayout.Container;
};

/**
 * Parent SOH Environment using query to get soh status
 */
export class EnvironmentComponent extends React.PureComponent<EnvironmentProps> {
  private unsubscribe: null | (() => void) = null;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

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
    const station = this.getStation();
    return (
      <SohContext.Provider value={this.getContextDefaults()}>
        <BaseDisplay
          glContainer={this.props.glContainer}
          className="environment-display top-level-container  scroll-box scroll-box--y full-width-height soh-env-component"
          onContextMenu={e => {
            e.preventDefault();
          }}
        >
          <this.EnvironmentChartHeader />
          <EnvironmentPanel
            channelSohs={station.channelSohs}
            channelStatusesToDisplay={this.props.channelStatusesToDisplay}
            monitorStatusesToDisplay={this.props.monitorStatusesToDisplay}
            isStale={this.props.sohStatus.isStale}
            defaultQuietDurationMs={this.props.sohConfigurationQuery.data.acknowledgementQuietMs}
            quietingDurationSelections={this.props.sohConfigurationQuery.data.availableQuietTimesMs}
            stationName={station.stationName}
          />
        </BaseDisplay>
      </SohContext.Provider>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /** Renders the title */
  // eslint-disable-next-line class-methods-use-this
  private readonly Title: React.FunctionComponent<{ stationName: string }> = ({ stationName }) => (
    <DrillDownTitle title={stationName} subtitle={messageConfig.labels.environmentalSubtitle} />
  );

  /** Renders the environment toolbar */
  private readonly EnvironmentToolbar: React.FunctionComponent = () => (
    <EnvironmentToolbar
      setMonitorStatusesToDisplay={this.props.setMonitorStatusesToDisplay}
      filterDropdown={[this.makeFilterDropDown()]}
      monitorStatusesToDisplay={this.props.monitorStatusesToDisplay}
    />
  );

  private readonly EnvironmentChartHeader: React.FunctionComponent = () => (
    <div>
      <this.EnvironmentToolbar />
      <this.Title stationName={this.getStation().stationName} />
    </div>
  );

  /**
   * Returns the default values for the Context
   */
  private readonly getContextDefaults = (): SohContextData => ({
    glContainer: this.props.glContainer,
    selectedAceiType: this.props.selectedAceiType,
    setSelectedAceiType: this.props.setSelectedAceiType
  });

  /** Returns the selected station */
  private readonly getStation = (): SohTypes.UiStationSoh =>
    this.props.sohStatus?.stationAndStationGroupSoh?.stationSoh?.find(
      s => s.stationName === this.props.selectedStationIds[0]
    );

  /**
   * Creates the filter drop down
   */
  private readonly makeFilterDropDown = (): DeprecatedToolbarTypes.CheckboxDropdownItem => ({
    enumOfKeys: FilterableSOHTypes,
    label: 'Filter Channels by Status',
    menuLabel: 'Filter Channels by Status',
    rank: 2,
    widthPx: filterItemWidthPx,
    type: DeprecatedToolbarTypes.ToolbarItemType.CheckboxList,
    tooltip: 'Filter Channels by Status',
    values: Immutable.Map(this.props.channelStatusesToDisplay),
    enumKeysToDisplayStrings: FilterableSohTypesDisplayStrings,
    onChange: (statuses: Immutable.Map<any, boolean>) =>
      this.props.setChannelStatusesToDisplay(statuses.toObject()),
    cyData: 'filter-soh-channels',
    colors: Immutable.Map([
      [FilterableSOHTypes.GOOD, dataAcquisitionUserPreferences.colors.ok],
      [FilterableSOHTypes.MARGINAL, dataAcquisitionUserPreferences.colors.warning],
      [FilterableSOHTypes.BAD, dataAcquisitionUserPreferences.colors.strongWarning],
      [FilterableSOHTypes.NONE, 'NULL_CHECKBOX_COLOR_SWATCH']
    ])
  });
}
