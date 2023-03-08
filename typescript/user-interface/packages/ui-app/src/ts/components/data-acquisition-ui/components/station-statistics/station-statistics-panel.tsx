/* eslint-disable class-methods-use-this */
/* eslint-disable react/destructuring-assignment */
import { IconNames } from '@blueprintjs/icons';
import { SohTypes } from '@gms/common-model';
import { CenterIcon, DeprecatedToolbarTypes, HorizontalDivider } from '@gms/ui-core-components';
import type { FilterableSOHTypes } from '@gms/ui-state';
import Immutable from 'immutable';
// eslint-disable-next-line no-restricted-imports
import type { Cancelable } from 'lodash';
import cloneDeep from 'lodash/cloneDeep';
import debounce from 'lodash/debounce';
import flatMap from 'lodash/flatMap';
import isEqual from 'lodash/isEqual';
import max from 'lodash/max';
import orderBy from 'lodash/orderBy';
import memoizeOne from 'memoize-one';
import * as React from 'react';

import { dataAcquisitionUserPreferences } from '~components/data-acquisition-ui/config';
import { messageConfig } from '~components/data-acquisition-ui/config/message-config';
import { StationDeselectHandler } from '~components/data-acquisition-ui/shared/table/station-deselect-handler';
import {
  acknowledgeContextMenu,
  getWorseStatus,
  sharedSohTableClasses
} from '~components/data-acquisition-ui/shared/table/utils';
import { SohToolbar } from '~components/data-acquisition-ui/shared/toolbars/soh-toolbar';

import { DropZone } from '../../shared/table/drop-zone';
import type { StationStatisticsRowClickedEvent } from './column-definitions';
import { Columns } from './column-definitions';
import type { StationStatisticsContextData } from './station-statistics-context';
import { StationStatisticsContext } from './station-statistics-context';
import { StationStatisticsTable } from './station-statistics-table';
import type { StationStatisticsRow } from './station-statistics-table-context';

/**
 * Station Statistics panel component props
 */
export interface StationStatisticsPanelProps {
  stationGroups: SohTypes.StationGroupSohStatus[];
  stationSohs: SohTypes.UiStationSoh[];
  statusesToDisplay: Record<FilterableSOHTypes, boolean>;
  setStatusesToDisplay: (statuses: Record<FilterableSOHTypes, boolean>) => void;
  groupSelected: string;
  setGroupSelected: (group: string) => void;
}

/**
 * Station Statistics panel component state
 */
export interface StationStatisticsPanelState {
  isHighlighted: boolean;
  /** defines the map that determines which columns should be displayed or hidden */
  columnsToDisplay: Immutable.OrderedMap<Columns, boolean>;
}

/**
 * Renders the toolbar, divider, and station statistics display.
 */
export class StationStatisticsPanel extends React.Component<
  StationStatisticsPanelProps,
  StationStatisticsPanelState
> {
  /** The context type */
  // eslint-disable-next-line react/static-property-placement
  public static contextType: React.Context<StationStatisticsContextData> = StationStatisticsContext;

  /** The context wrapped around the panel, used for mutation and access to the GL Container */
  // eslint-disable-next-line react/static-property-placement
  public declare context: React.ContextType<typeof StationStatisticsContext>;

  /**
   * A memoized function for generating the table rows.
   *
   * @param stationSohs station soh data
   * @param groupSelected the selected group
   * @returns an array of station statistics rows
   */
  private readonly memoizedGenerateTableRows: (
    stationSohs: SohTypes.UiStationSoh[],
    groupSelected: string
  ) => StationStatisticsRow[];

  /** Default group selector text for the dropdown filter */
  private readonly defaultGroupSelectorText: string = 'All Groups';

  /** A ref to the bottom that toggles checkbox dropdown in the toolbar */
  private highlightButtonRef: HTMLDivElement = null;

  /**
   * How frequently to attempt to set the highlight
   */
  private readonly setStateTryMs: number = 200;

  /**
   * Set the highlight state on a debounced function so that many
   * event calls do not clog up the event stack.
   */
  private readonly debouncedSetStateHighlight: (() => void) & Cancelable = debounce(
    () => {
      this.setState({ isHighlighted: true });
    },
    this.setStateTryMs,
    { leading: true }
  );

  /** The reference to the acknowledged table */
  private tableAcknowledged: StationStatisticsTable;

  /** The reference to the unacknowledged table */
  private tableUnacknowledged: StationStatisticsTable;

  /**
   * constructor
   */
  public constructor(props: StationStatisticsPanelProps) {
    super(props);
    this.memoizedGenerateTableRows =
      typeof memoizeOne === 'function'
        ? memoizeOne(
            this.generateTableData,
            /* tell memoize to use a deep comparison for complex objects */
            isEqual
          )
        : this.generateTableData;
    this.state = {
      isHighlighted: false,
      columnsToDisplay: Immutable.Map(
        Object.values(Columns)
          // all columns are visible by default
          .map(v => [v, true])
      )
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    /** Default top row height in pixels */
    const defaultTopRowHeightPx = 425;
    /** Icon size */
    const iconSize = 50;
    /** The selected group to filter on */
    const groupSelector = this.generateGroupSelector(
      this.props.stationGroups,
      this.props.groupSelected
    );

    const columnsToDisplayCheckBoxDropdown = this.generateColumnDisplaySelector();

    /** Will filter if a group is selected otherwise use the props passed in */
    const dataForBothTables = this.props.groupSelected
      ? this.memoizedGenerateTableRows(
          this.props.stationSohs,
          this.props.groupSelected
        ).filter(data =>
          data.stationGroups.find(group => group.groupName === this.props.groupSelected)
        )
      : this.memoizedGenerateTableRows(this.props.stationSohs, this.props.groupSelected);

    /** Filters data based on needing acknowledgement, this data is used for the top table */
    const needsAttentionSohData = dataForBothTables.filter(station => station.needsAttention);

    /** Filters the soh data that does not need attention, this data is used for the bottom table */
    const doesNotNeedAttentionSohData = dataForBothTables
      .filter(station => !station.needsAttention)
      .filter(row => this.props.statusesToDisplay[row.stationData.stationCapabilityStatus]);

    return (
      <>
        <div className="soh-toolbar-container">
          <SohToolbar
            statusesToDisplay={this.props.statusesToDisplay}
            setStatusesToDisplay={this.props.setStatusesToDisplay}
            leftItems={[groupSelector, columnsToDisplayCheckBoxDropdown]}
            rightItems={[]}
            statusFilterText={messageConfig.labels.sohToolbar.filterStatuses}
            statusFilterTooltip={messageConfig.tooltipMessages.sohToolbar.selectStatuses}
            toggleHighlight={this.toggleHighlight}
            isDrillDown={false}
          />
        </div>
        <HorizontalDivider
          topHeightPx={defaultTopRowHeightPx}
          sizeRange={dataAcquisitionUserPreferences.stationStatisticsMinContainerRange}
          top={
            <StationDeselectHandler
              setSelectedStationIds={(ids: string[]) => this.context.setSelectedStationIds(ids)}
              className={`station-statistics-table-container station-statistics-table-container--needs-attention ${sharedSohTableClasses}`}
              dataCy="soh-unacknowledged"
            >
              <div className="soh-table-label">Needs Attention</div>
              {needsAttentionSohData.length !== 0 ? (
                <StationStatisticsTable
                  ref={ref => {
                    this.tableUnacknowledged = ref;
                  }}
                  id="soh-unacknowledged"
                  tableData={needsAttentionSohData}
                  selectedIds={this.context.selectedStationIds}
                  onRowClicked={this.onRowClicked}
                  acknowledgeContextMenu={this.acknowledgeContextMenu}
                />
              ) : (
                <CenterIcon
                  iconName={IconNames.TICK_CIRCLE}
                  description="Nothing to acknowledge"
                  iconSize={iconSize}
                  className="table-background-icon"
                />
              )}
            </StationDeselectHandler>
          }
          bottom={
            <StationDeselectHandler
              setSelectedStationIds={(ids: string[]) => this.context.setSelectedStationIds(ids)}
              className={`station-statistics-table-container station-statistics-table-container--acknowledged gms-drop-zone
                  ${sharedSohTableClasses}
                  ${
                    this.state.isHighlighted
                      ? 'station-statistics-table-container--highlighted'
                      : ''
                  }`}
              dataCy="soh-acknowledged"
            >
              <DropZone onDrop={(payload: string[]) => this.cellDrop(payload, this.context)}>
                <StationStatisticsTable
                  ref={ref => {
                    this.tableAcknowledged = ref;
                  }}
                  id="soh-acknowledged"
                  tableData={doesNotNeedAttentionSohData}
                  selectedIds={this.context.selectedStationIds}
                  onRowClicked={this.onRowClicked}
                  acknowledgeContextMenu={(stationNames: string[]) =>
                    this.acknowledgeContextMenu(stationNames)
                  }
                  suppressContextMenu
                  highlightDropZone={this.debouncedSetStateHighlight}
                />
              </DropZone>
            </StationDeselectHandler>
          }
        />
      </>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Handles row selection and updated the redux state with station ID for sync selection
   *
   * @param event StationStatisticsRowClickedEvent
   */
  private readonly onRowClicked = (event: StationStatisticsRowClickedEvent): void => {
    const stationNames: string[] = event.api.getSelectedRows().map(row => row.id);
    this.context.setSelectedStationIds(stationNames);
  };

  private readonly acknowledgeContextMenu = (stationNames: string[]): JSX.Element =>
    acknowledgeContextMenu(
      stationNames,
      this.props.stationSohs,
      this.context.sohStationStaleTimeMS,
      // eslint-disable-next-line @typescript-eslint/unbound-method
      this.context.acknowledgeSohStatus
    );

  /**
   * Cell drag logic, gets data from transfer object and calls acknowledgeSohStatus
   *
   * @param event React.DragEvent<HTMLDivElement>
   * @param context context data for an soh panel
   */
  private readonly cellDrop = (
    stationNames: string[],
    context: StationStatisticsContextData
  ): void => {
    this.debouncedSetStateHighlight.cancel();
    this.setState({ isHighlighted: false });
    context.acknowledgeSohStatus(stationNames);
    context.setSelectedStationIds(stationNames);
  };

  /**
   * Generates the drop down for the groups
   *
   * @param groups station groups list
   * @param groupSelected current group selected
   * @returns a toolbar item of type DeprecatedToolbarTypes.ToolbarItem
   */
  private readonly generateGroupSelector = (
    groups: SohTypes.StationGroupSohStatus[],
    groupSelected: string
  ): DeprecatedToolbarTypes.ToolbarItem => {
    const groupOptions = { default: this.defaultGroupSelectorText };
    orderBy(groups, 'priority', 'asc').forEach(group => {
      groupOptions[group.id] = group.stationGroupName;
    });

    return {
      rank: 2,
      label: 'Select a Group',
      tooltip: 'Show only a single group',
      type: DeprecatedToolbarTypes.ToolbarItemType.Dropdown,
      onChange: e => {
        this.props.setGroupSelected(e !== this.defaultGroupSelectorText ? e : undefined);
      },
      dropdownOptions: groupOptions,
      value: groupSelected || this.defaultGroupSelectorText,
      cyData: 'station-statistics-group-selector',
      widthPx: 150
    };
  };

  /**
   * Generates the drop down for selecting which columns are visible.
   *
   * @returns a toolbar item of type DeprecatedToolbarTypes.ToolbarItem
   */
  private readonly generateColumnDisplaySelector = (): DeprecatedToolbarTypes.ToolbarItem => {
    const columnsToDisplayCheckBoxDropdown: DeprecatedToolbarTypes.CheckboxDropdownItem = {
      enumOfKeys: Object.values(Columns)
        // do not allow the station column to be hidden
        .filter(v => v !== Columns.Station),
      label: 'Show columns',
      menuLabel: 'Show columns',
      rank: 3,
      widthPx: 150,
      type: DeprecatedToolbarTypes.ToolbarItemType.CheckboxList,
      tooltip: 'Set which columns are visible',
      values: this.state.columnsToDisplay,
      onChange: this.setColumnsToDisplay,
      cyData: 'filter-column'
    };
    return columnsToDisplayCheckBoxDropdown;
  };

  /**
   * @param stationSoh the soh data for the station belonging to a row
   * @param monitorType either a monitor type, or 'ENVIRONMENT' in the case of the environment cell.
   * Note that 'ENVIRONMENT' string is used instead of an enum because there is no single enum corresponding
   * to the channel issues cell. Instead, that cell is a rollup of all environmental issues.
   */
  private readonly generateChannelCellData = (
    stationSoh: SohTypes.UiStationSoh,
    monitorType: SohTypes.SohMonitorType | 'ENVIRONMENT'
  ) => {
    const contributorData =
      monitorType === 'ENVIRONMENT'
        ? this.getEnvRollup(stationSoh)
        : this.getContributorForSohMonitorType(stationSoh, monitorType);
    return {
      value: contributorData && contributorData.valuePresent ? contributorData.value : undefined,
      status: contributorData ? contributorData.statusSummary : undefined,
      isContributing: contributorData ? contributorData.contributing : false
    };
  };

  /**
   * Generates table data
   *
   * @param props the props
   * @returns an array of table data rows
   */
  private readonly generateTableData = (
    stationSohs: SohTypes.UiStationSoh[],
    groupSelected: string
  ): StationStatisticsRow[] =>
    cloneDeep(
      flatMap(stationSohs, stationSoh => {
        const stationData = {
          stationName: stationSoh?.stationName,
          stationStatus: stationSoh?.sohStatusSummary,
          stationCapabilityStatus: groupSelected
            ? stationSoh.stationGroups.find(group => group.groupName === groupSelected)
                ?.sohStationCapability
            : this.getWorstStationCapabilityStatus(stationSoh)
        };

        // Data for channel detail

        // Channel level detail/statistics
        const channelLag = this.generateChannelCellData(stationSoh, SohTypes.SohMonitorType.LAG);
        const channelMissing = this.generateChannelCellData(
          stationSoh,
          SohTypes.SohMonitorType.MISSING
        );
        const channelEnvironment = this.generateChannelCellData(stationSoh, 'ENVIRONMENT');
        const channelTimeliness = this.generateChannelCellData(
          stationSoh,
          SohTypes.SohMonitorType.TIMELINESS
        );

        // Station level details/statistics
        // Average transmission time for acquired data samples.
        const stationLag = this.getStationStats(stationSoh, SohTypes.StationAggregateType.LAG);

        // Gets the total percentage of 'bad' indicators across all channels and all environmental monitors.
        const stationEnvironment = this.getStationStats(
          stationSoh,
          SohTypes.StationAggregateType.ENVIRONMENTAL_ISSUES
        );

        // Returns the total percentage of missing data across all channels.
        const stationMissing = this.getStationStats(
          stationSoh,
          SohTypes.StationAggregateType.MISSING
        );

        // Latest data sample time that has been acquired on any channel.
        const stationTimeliness = this.getStationStats(
          stationSoh,
          SohTypes.StationAggregateType.TIMELINESS
        );

        return {
          id: `${stationSoh.stationName}`,
          location: 'cell',
          stationData,
          stationGroups: groupSelected
            ? [stationSoh.stationGroups.find(group => group.groupName === groupSelected)].filter(
                group => group !== undefined
              )
            : stationSoh.stationGroups,
          needsAcknowledgement: stationSoh.needsAcknowledgement,
          needsAttention: stationSoh.needsAttention,
          channelLag,
          channelMissing,
          channelEnvironment,
          channelTimeliness,
          stationLag,
          stationMissing,
          stationEnvironment,
          stationTimeliness
        };
      }).sort((stationA, stationB) => stationA.id.localeCompare(stationB.id))
    );

  /**
   * Returns the contributor for soh monitor type by station status
   *
   * @param stationStatus SohTypes.UiStationSoh
   * @param monitorType: SohTypes.SohMonitorType
   * @returns SohTypes.SohContributor
   */
  private readonly getContributorForSohMonitorType = (
    stationStatus: SohTypes.UiStationSoh,
    monitorType: SohTypes.SohMonitorType
  ): SohTypes.SohContributor =>
    stationStatus.statusContributors.find(contributor => contributor.type === monitorType);

  /**
   * Toggles the highlight for the button that toggles the dropdown filter list
   * Calls a method that updates the toggleHighlight state
   *
   * @param ref ref to the filter button
   */
  private readonly toggleHighlight = (ref: HTMLDivElement): void => {
    this.setIsHighlighted(!this.state.isHighlighted);
    this.highlightButtonRef = ref ?? this.highlightButtonRef;
    if (this.highlightButtonRef && this.highlightButtonRef.classList) {
      this.highlightButtonRef.classList.toggle('isHighlighted');
    }
  };

  /**
   * Updates the state for isHighlighted
   *
   * @param isHighlightedValue value to update the state with
   */
  private readonly setIsHighlighted = (isHighlightedValue: boolean): void => {
    this.setState({
      isHighlighted: isHighlightedValue
    });
  };

  /**
   * Updates the state for which columns are displayed
   *
   * @param columnsToDisplay value to update the state with
   */
  private readonly setColumnsToDisplay = (
    columnsToDisplay: Immutable.Map<Columns, boolean>
  ): void => {
    this.setState({ columnsToDisplay });
    if (this.tableAcknowledged) {
      this.tableAcknowledged.updateColumnVisibility(columnsToDisplay);
    }
    if (this.tableUnacknowledged) {
      this.tableUnacknowledged.updateColumnVisibility(columnsToDisplay);
    }
  };

  /**
   * Gets the rolled up data for the environment column
   *
   * @param stationStatus station soh to get environment rollup for
   */
  private readonly getEnvRollup = (
    stationStatus: SohTypes.UiStationSoh
  ): SohTypes.SohContributor => {
    const envContributors = stationStatus.statusContributors.filter(
      contributor => SohTypes.isEnvironmentalIssue(contributor.type) && contributor.contributing
    );

    const worseStatus = envContributors
      .map(envContributor => envContributor.statusSummary)
      .reduce(getWorseStatus, SohTypes.SohStatusSummary.NONE);

    const worseValue = max(
      envContributors
        .filter(contributor => contributor.statusSummary === worseStatus)
        .map(contributor => (contributor.valuePresent ? contributor.value : undefined))
    );

    // return rollup of worse status - and the worst value of the worst status - type is not used
    return {
      contributing: envContributors.filter(c => c.contributing).length > 0,
      statusSummary: worseStatus,
      type: undefined,
      value: worseValue,
      valuePresent: worseValue !== undefined
    };
  };

  /**
   * Gets the worst capability status for the given station
   *
   * @param station station soh
   */
  private readonly getWorstStationCapabilityStatus = (station: SohTypes.UiStationSoh) =>
    station?.stationGroups
      ?.map(group => group.sohStationCapability)
      .reduce(getWorseStatus, SohTypes.SohStatusSummary.NONE);

  /**
   * Finds any one of envIssues, lag, missing, or timeliness stat for a given station
   *
   * @param stationStatus - ui station soh we will find stats for
   * @param stationAggType - the stat we wish to find
   * @returns the percentage or seconds of the stat
   */
  private readonly getStationStats = (
    stationStatus: SohTypes.UiStationSoh,
    stationAggType: SohTypes.StationAggregateType
  ): number => {
    const stationAggregate = stationStatus.allStationAggregates.filter(
      stAgg => stAgg.aggregateType === stationAggType
    )[0];
    return stationAggregate && stationAggregate.valuePresent ? stationAggregate.value : undefined;
  };
}
