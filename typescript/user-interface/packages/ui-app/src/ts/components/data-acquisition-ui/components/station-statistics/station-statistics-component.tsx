/* eslint-disable react/destructuring-assignment */
import type { SohTypes } from '@gms/common-model';
import { Displays } from '@gms/common-model';
import { MILLISECONDS_IN_SECOND, millisToStringWithMaxPrecision } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import type { AppState, FilterableSOHTypes } from '@gms/ui-state';
import { dataAcquisitionActions, setSelectedStationIds, ssamControlApiSlice } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import isEqual from 'lodash/isEqual';
import memoizeOne from 'memoize-one';
import React from 'react';
import type { ConnectedProps } from 'react-redux';
import { connect } from 'react-redux';

import { BaseDisplay } from '~components/common-ui/components/base-display';
import type { WithAcknowledgeProps } from '~components/data-acquisition-ui/shared/acknowledge';

import type { StationStatisticsContextData } from './station-statistics-context';
import { StationStatisticsContext } from './station-statistics-context';
import { StationStatisticsPanel } from './station-statistics-panel';

const logger = UILogger.create(
  'GMS_LOG_SOH_STATION_STATISTICS',
  process.env.GMS_LOG_SOH_STATION_STATISTICS
);

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState) => ({
  selectedStationIds: state.app.common.selectedStationIds,
  sohStatus: state.app.dataAcquisition.data.sohStatus,
  sohConfigurationQuery: ssamControlApiSlice.endpoints.getSohConfiguration.select()(state),
  groupSelected: state.app.dataAcquisition.stationStatisticsGroup,
  statusesToDisplay:
    state.app.dataAcquisition.filtersToDisplay[Displays.SohDisplays.STATION_STATISTICS]
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 */
const mapDispatchToProps = {
  getSohConfiguration: ssamControlApiSlice.endpoints.getSohConfiguration.initiate,
  setSelectedStationIds,
  setStationStatisticsGroup: (group: string) => dispatch => {
    dispatch(dataAcquisitionActions.setStationStatisticsGroup(group));
  },
  setStatusesToDisplay: (filters: Record<FilterableSOHTypes, boolean>) => dispatch => {
    dispatch(
      dataAcquisitionActions.setFiltersToDisplay({
        list: Displays.SohDisplays.STATION_STATISTICS,
        filters
      })
    );
  }
};

export const stationStatisticsConnector = connect(mapStateToProps, mapDispatchToProps);

type ConnectedReduxProps = ConnectedProps<typeof stationStatisticsConnector>;

/**
 * Station Statistics component props
 */
export type StationStatisticsProps = ConnectedReduxProps &
  WithAcknowledgeProps & {
    glContainer?: GoldenLayout.Container;
  };

/**
 * Connected component that provides context and props to SOH Panel
 */
export class StationStatisticsComponent extends React.Component<StationStatisticsProps> {
  /*
   * Series of member variables to report lag statistics every 30 mins
   */

  /** Represents a half hour (30 minutes) in milliseconds */
  private readonly MS_HALF_HOUR: number = 1800000;

  /** the soh soh lag report period; how frequently the to report */
  private readonly sohLagReportPeriod: number = this.MS_HALF_HOUR;

  /** the last time the soh report was sent (reported) */
  private lastSohReport: number = Date.now();

  /** the min soh lag value */
  private minSohLag: number;

  /** the max soh lag value */
  private maxSohLag: number;

  /** the average soh lag value */
  private avgSohLag = 0;

  /** the soh station count - used to calculate the average */
  private sohStationsCount = 0;

  // Map to not check StationSOH already sent from API Gateway
  private readonly sohCheckDisplayCriteriaMap: Map<string, number>;

  private unsubscribe: null | (() => void) = null;

  private memoizedStationStatisticsContextData: () => StationStatisticsContextData;

  /**
   * constructor
   */
  public constructor(props: StationStatisticsProps) {
    super(props);
    this.sohCheckDisplayCriteriaMap = new Map<string, number>();
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  public componentDidMount(): void {
    const { getSohConfiguration } = this.props;
    // eslint-disable-next-line @typescript-eslint/unbound-method
    const { unsubscribe } = getSohConfiguration();
    this.unsubscribe = unsubscribe;
  }

  /**
   * React lifecycle `componentDidUpdate`.
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: StationStatisticsProps): void {
    if (!isEqual(prevProps.sohStatus, this.props)) {
      // Check SOH station entries to see if the lag exceeds the time limit
      this.checkSOHDisplayCriteria(this.props.sohStatus.stationAndStationGroupSoh.stationSoh);
    }
  }

  public componentWillUnmount(): void {
    this.unsubscribe?.();
  }

  /**
   * React lifecycle `render`.
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    this.memoizedStationStatisticsContextData = memoizeOne(() => {
      return {
        acknowledgeSohStatus: this.props.acknowledgeStationsByName,
        quietTimerMs: this.props.sohConfigurationQuery.data.acknowledgementQuietMs,
        sohStationStaleTimeMS: this.props.sohConfigurationQuery.data.sohStationStaleMs,
        updateIntervalSecs: this.props.sohConfigurationQuery.data.reprocessingPeriodSecs,
        selectedStationIds: this.props.selectedStationIds,
        setSelectedStationIds: this.props.setSelectedStationIds
      };
    }, isEqual);
    return (
      <StationStatisticsContext.Provider value={this.memoizedStationStatisticsContextData()}>
        <BaseDisplay
          glContainer={this.props.glContainer}
          className="soh-divider-container station-statistics-display drop-zone__wrapper"
          data-cy="station-statistics-display"
        >
          <StationStatisticsPanel
            statusesToDisplay={this.props.statusesToDisplay}
            setStatusesToDisplay={this.props.setStatusesToDisplay}
            stationGroups={this.props.sohStatus.stationAndStationGroupSoh.stationGroups}
            stationSohs={this.props.sohStatus.stationAndStationGroupSoh.stationSoh}
            groupSelected={this.props.groupSelected}
            setGroupSelected={this.props.setStationStatisticsGroup}
          />
        </BaseDisplay>
      </StationStatisticsContext.Provider>
    );
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Check if SOH exceeds 1 minute display to UI criteria
   */
  private readonly checkSOHDisplayCriteria = (stationStatus: SohTypes.UiStationSoh[]): void => {
    // If UI Configuration has not loaded yet bail
    if (!this.props.sohConfigurationQuery || !this.props.sohConfigurationQuery.data) {
      return;
    }

    stationStatus.forEach(soh => {
      if (!this.sohCheckDisplayCriteriaMap.has(soh.stationName)) {
        // Set the entry, the first time we don't check
        this.sohCheckDisplayCriteriaMap.set(soh.stationName, soh.time);
      } else if (this.sohCheckDisplayCriteriaMap.get(soh.stationName) !== soh.time) {
        // Set the entry in the map so we don't check the entry again
        this.sohCheckDisplayCriteriaMap.set(soh.stationName, soh.time);

        // Get time and scale to milliseconds
        const deltaSecs = Date.now() / MILLISECONDS_IN_SECOND - soh.time;

        // add to avg
        this.avgSohLag += deltaSecs;
        // eslint-disable-next-line no-plusplus
        this.sohStationsCount++;

        // Check min and max
        if (!this.minSohLag || deltaSecs < this.minSohLag) {
          this.minSohLag = deltaSecs;
        }

        if (!this.maxSohLag || deltaSecs > this.maxSohLag) {
          this.maxSohLag = deltaSecs;
        }
      }
    });

    // Check if need to send statistic report
    if (Date.now() > this.lastSohReport + this.sohLagReportPeriod) {
      logger.info(
        `SOH lag statistics for last ${millisToStringWithMaxPrecision(this.sohLagReportPeriod)}. ` +
          `Max lag: ${millisToStringWithMaxPrecision(this.maxSohLag * MILLISECONDS_IN_SECOND)}, ` +
          `Min lag ${millisToStringWithMaxPrecision(this.minSohLag * MILLISECONDS_IN_SECOND)}, ` +
          `Avg lag ${millisToStringWithMaxPrecision(
            (this.avgSohLag / this.sohStationsCount) * MILLISECONDS_IN_SECOND
          )}`
      );

      // Reset statistics
      this.lastSohReport = Date.now();
      this.sohStationsCount = 0;
      this.avgSohLag = 0;
      this.minSohLag = undefined;
      this.maxSohLag = undefined;
    }
  };
}
