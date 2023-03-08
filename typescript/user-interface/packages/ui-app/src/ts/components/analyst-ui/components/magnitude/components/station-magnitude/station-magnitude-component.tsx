/* eslint-disable react/destructuring-assignment */
import { LegacyEventTypes } from '@gms/common-model';
import type { GridReadyEvent, TableApi } from '@gms/ui-core-components';
import { Table } from '@gms/ui-core-components';
import classNames from 'classnames';
import Immutable from 'immutable';
import flatMap from 'lodash/flatMap';
import uniqueId from 'lodash/uniqueId';
import React from 'react';

import { getNetworkMagSolution } from '~analyst-ui/common/utils/magnitude-util';
import { messageConfig } from '~analyst-ui/config/message-config';

import type { AmplitudesByStation } from '../../types';
import { MagDefiningStates } from '../../types';
import { generateStationMagnitudeColumnDefs } from './table-utils/column-defs';
import type {
  MagnitudeAndSdData,
  MagnitudeDataForRow,
  StationMagAndSignalDetection,
  StationMagnitudeProps,
  StationMagnitudeRow,
  StationMagnitudeState
} from './types';

/**
 * Station magnitude component using an core component table to display data
 * Uses a mutation to calculate mag and res when defining are changed.
 */
export class StationMagnitude extends React.Component<
  StationMagnitudeProps,
  StationMagnitudeState
> {
  /** The ag-grid table reference */
  private mainTable: TableApi;

  /** Whether stations with undefined magnitude should show all defining */
  private readonly defaultAllDefining: boolean = false;

  /**
   * constructor
   */
  public constructor(props: StationMagnitudeProps) {
    super(props);
    this.state = {
      computeNetworkMagnitudeSolutionStatus: Immutable.Map<
        string,
        [{ stationName: string; rational: string }]
      >()
    };
  }

  /**
   * React component lifecycle
   *
   * @param prevProps The previous properties available to this react component
   */
  public componentDidUpdate(prevProps: StationMagnitudeProps): void {
    // If the current open event ID has changed and doesn't have a status, create an entry and set status to undefined
    if (prevProps.openEventId !== this.props.openEventId) {
      const hasStatus = this.state.computeNetworkMagnitudeSolutionStatus.has(
        this.props.openEventId
      );
      if (!hasStatus) {
        // eslint-disable-next-line react/no-did-update-set-state
        this.setState(prevState => ({
          computeNetworkMagnitudeSolutionStatus: prevState.computeNetworkMagnitudeSolutionStatus.set(
            this.props.openEventId,
            undefined
          )
        }));
      }
    }
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const mainTableRowData: StationMagnitudeRow[] = this.generateTableRows();
    const defStatesForMagnitudeType = new Map<LegacyEventTypes.MagnitudeType, MagDefiningStates>();
    Object.keys(LegacyEventTypes.MagnitudeType).forEach(magnitudeKey =>
      defStatesForMagnitudeType.set(
        LegacyEventTypes.MagnitudeType[magnitudeKey],
        this.getAllNoneDef(mainTableRowData, LegacyEventTypes.MagnitudeType[magnitudeKey])
      )
    );
    const stationIdsForMagnitudeType = new Map<LegacyEventTypes.MagnitudeType, string[]>();

    Object.keys(LegacyEventTypes.MagnitudeType).forEach(magTypeKey =>
      stationIdsForMagnitudeType.set(
        LegacyEventTypes.MagnitudeType[magTypeKey],
        mainTableRowData
          .filter(row => {
            const maybeMagData = row.dataForMagnitude.get(
              LegacyEventTypes.MagnitudeType[magTypeKey]
            );
            return maybeMagData ? maybeMagData.mag !== undefined : false;
          })
          .map(row => row.station)
      )
    );
    return (
      <div className={classNames('ag-theme-dark', 'table-container')}>
        <div className="list-wrapper">
          <div className="max">
            <Table
              gridOptions={this.props.options}
              context={{}}
              columnDefs={generateStationMagnitudeColumnDefs(
                defStatesForMagnitudeType,
                stationIdsForMagnitudeType,
                this.props.displayedMagnitudeTypes,
                this.props.historicalMode,
                this.stationDefiningCallback
              )}
              rowData={mainTableRowData}
              getRowId={node => node.data.id}
              rowSelection="none"
              onGridReady={this.onMainTableReady}
              rowDeselection
              onCellClicked={this.onCellClicked}
              suppressContextMenu
              overlayNoRowsTemplate="No associated signal detections with appropriate phases"
            />
          </div>
        </div>
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Updates the state when a checkbox defining changes
   *
   * @param magnitudeType the magnitude type of the station defining changed
   * @param stationNames list of station ids
   * @param defining defining value
   */
  private readonly stationDefiningCallback = async (
    magnitudeType: LegacyEventTypes.MagnitudeType,
    stationNames: string[],
    defining: boolean
  ): Promise<void> => {
    const status = await this.props.checkBoxCallback(magnitudeType, stationNames, defining);
    this.setState(prevState => ({
      computeNetworkMagnitudeSolutionStatus: prevState.computeNetworkMagnitudeSolutionStatus.set(
        this.props.openEventId,
        status
      )
    }));
  };

  /**
   * Event handler for ag-gird that is fired when the table is ready
   *
   * @param event event of the table action
   */
  private readonly onMainTableReady = (event: GridReadyEvent) => {
    this.mainTable = event.api;
  };

  private readonly generateTableRows = (): StationMagnitudeRow[] => {
    const stationMagnitudeRows = [];
    if (!this.props.locationSolution) {
      return stationMagnitudeRows;
    }

    // Generate an array of station mag and signal detections for later use
    const stationMagAndSignalDetections: StationMagAndSignalDetection[] = this.props.amplitudesByStation.map(
      this.generateStationMagAndSignalDetection
    );

    // For each station mag and signal detection entry, create a row
    const rows: StationMagnitudeRow[] = stationMagAndSignalDetections.map(
      stationMagAndSignalDetection => {
        const distance = this.props.locationSolution.locationToStationDistances.find(
          dist => dist.stationId === stationMagAndSignalDetection.stationName
        );

        return {
          azimuth: distance ? distance.azimuth : 0,
          azimuthTooltip: messageConfig.tooltipMessages.magnitude.azimuthSourceToReceiverMessage,
          checkBoxCallback: this.stationDefiningCallback,
          dataForMagnitude: this.generateDataForMagnitude(stationMagAndSignalDetection),
          dist: distance ? distance.distance.degrees : 0,
          historicalMode: this.props.historicalMode,
          id: uniqueId(),
          selectedSdIds: this.props.selectedSdIds,
          station: stationMagAndSignalDetection.stationName
        };
      }
    );
    return rows;
  };

  /**
   * Create a Map between MagnitudeTypes and MagnitudeDataForRow objects. This
   * data is passed into each ag-grid row and used to render the grid cells.
   *
   * @param stationMagAndSignalDetection contains the magnitude and signal detection for a station magnitude
   *
   * @returns a Map<LegacyEventTypes.MagnitudeType, MagnitudeDataForRow>
   */
  private readonly generateDataForMagnitude = (
    stationMagAndSignalDetection: StationMagAndSignalDetection
  ): Map<LegacyEventTypes.MagnitudeType, MagnitudeDataForRow> => {
    const dataPerMagnitude = new Map<LegacyEventTypes.MagnitudeType, MagnitudeDataForRow>();
    Object.keys(LegacyEventTypes.MagnitudeType).forEach(key => {
      const magnitudeType = LegacyEventTypes.MagnitudeType[key];
      const magnitudeAndSDData = stationMagAndSignalDetection.magnitudeAndSdData.get(magnitudeType);
      /** Determines if network magnitudes have been calculated from the service */
      if (magnitudeAndSDData.sdData !== undefined) {
        const computeNetworkMagnitudeSolutionStatus =
          this.state.computeNetworkMagnitudeSolutionStatus.get(this.props.openEventId) &&
          this.state.computeNetworkMagnitudeSolutionStatus
            .get(this.props.openEventId)
            .find(rejected => rejected.stationName === magnitudeAndSDData.sdData.stationName);
        const stationMagnitude = this.getMagnitudeSolutionForStation(
          magnitudeAndSDData.magSolution,
          magnitudeAndSDData.sdData.stationName
        );
        const mag = stationMagnitude
          ? stationMagnitude.stationMagnitudeSolution.magnitude
          : undefined;
        const res = stationMagnitude
          ? stationMagnitude.stationMagnitudeSolution.magnitudeUncertainty
          : undefined;
        const data: MagnitudeDataForRow = {
          amplitudePeriod: magnitudeAndSDData.sdData.amplitudePeriod,
          amplitudeValue: magnitudeAndSDData.sdData.amplitudeValue,
          channel: magnitudeAndSDData.sdData.channel,
          defining: stationMagnitude ? stationMagnitude.defining : false,
          flagForReview: magnitudeAndSDData.sdData.flagForReview,
          mag,
          res,
          phase: magnitudeAndSDData.sdData.phase,
          signalDetectionId: magnitudeAndSDData.sdData.signalDetectionId,
          hasMagnitudeCalculationError: !mag || !res,
          computeNetworkMagnitudeSolutionStatus: computeNetworkMagnitudeSolutionStatus
            ? computeNetworkMagnitudeSolutionStatus.rational
            : 'No mag data retrieved from OSD, please select defining to calculate'
        };
        dataPerMagnitude.set(magnitudeType, data);
      } else {
        dataPerMagnitude.set(magnitudeType, {
          amplitudePeriod: undefined,
          amplitudeValue: undefined,
          channel: undefined,
          defining: undefined,
          flagForReview: false,
          mag: undefined,
          phase: undefined,
          res: undefined,
          signalDetectionId: undefined,
          hasMagnitudeCalculationError: false,
          computeNetworkMagnitudeSolutionStatus: undefined
        });
      }
    });
    return dataPerMagnitude;
  };

  /**
   * Create a StationMagAndSignalDetection object for easier access to
   * deeply nested magnitude and signal detection data.
   *
   * @param amplitudeByStation contains the network magnitude solution and signal detection data
   *
   * @returns a StationMagAndSignalDetection object
   */
  private readonly generateStationMagAndSignalDetection = (
    amplitudeByStation: AmplitudesByStation
  ): StationMagAndSignalDetection => {
    const magnitudeTypeToAmplitudeData = new Map<
      LegacyEventTypes.MagnitudeType,
      MagnitudeAndSdData
    >();
    Object.keys(LegacyEventTypes.MagnitudeType).forEach(magKey => {
      magnitudeTypeToAmplitudeData.set(LegacyEventTypes.MagnitudeType[magKey], {
        magSolution: getNetworkMagSolution(
          this.props.locationSolution,
          LegacyEventTypes.MagnitudeType[magKey]
        ),
        sdData: amplitudeByStation.magTypeToAmplitudeMap.get(magKey)
      });
    });
    return {
      magnitudeAndSdData: magnitudeTypeToAmplitudeData,
      stationName: amplitudeByStation.stationName
    };
  };

  /**
   * Gets the Network Magnitude Behavior matching the station id
   *
   * @param magnitudeSolution a magnitude solution
   * @param stationName id of the station
   *
   * @returns a NetworkMagnitudeBehavior
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly getMagnitudeSolutionForStation = (
    magnitudeSolution: LegacyEventTypes.NetworkMagnitudeSolution,
    stationName: string
  ): LegacyEventTypes.NetworkMagnitudeBehavior =>
    magnitudeSolution
      ? magnitudeSolution.networkMagnitudeBehaviors.find(
          nmb => nmb.stationMagnitudeSolution.stationName === stationName
        )
      : undefined;

  /**
   * On click handler for selecting mb and ms rows
   *
   * @params params, returned from ag grid
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onCellClicked = (params: any) => {
    if (
      params &&
      params.column &&
      !this.props.historicalMode &&
      params.column.colDef.headerName !== 'Def'
    ) {
      const magType = params.column.colDef.magnitudeType as LegacyEventTypes.MagnitudeType;
      if (magType) {
        if (params.event.shiftKey) {
          this.onShiftClick(params, magType);
        } else if (params.event.ctrlKey || params.event.metaKey) {
          this.onControlClick(params, magType);
        } else {
          this.onDefaultClick(params, magType);
        }
      }
    }
  };

  /**
   * Handles a shift click
   *
   * @param params params from ag grid
   * @param magType the magnitude type for the clicked column
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onShiftClick = (params: any, magType: LegacyEventTypes.MagnitudeType) => {
    const rowIndices = this.getSelectedRowIndicesForPhase(magType);
    const { data } = params;
    if (rowIndices.length < 1) {
      if (data.dataForMagnitude.get(magType).signalDetectionId) {
        this.props.setSelectedSdIds([data.dataForMagnitude.get(magType).signalDetectionId]);
      }
    } else {
      rowIndices.sort((a, b) => a - b);
      const lowestIndex = rowIndices[0];
      const highestIndex = rowIndices[rowIndices.length - 1];
      const newLowest = params.node.rowIndex < lowestIndex ? params.node.rowIndex : lowestIndex;
      const newHighest = params.node.rowIndex > highestIndex ? params.node.rowIndex : highestIndex;
      const newlySelectedIds = flatMap(this.mainTable.getRenderedNodes(), node => {
        if (
          node.data.dataForMagnitude.get(magType).signalDetectionId &&
          node.rowIndex >= newLowest &&
          node.rowIndex <= newHighest
        ) {
          return node.data.dataForMagnitude.get(magType).signalDetectionId;
        }
        return undefined;
      }).filter(id => id !== undefined);
      this.props.setSelectedSdIds(newlySelectedIds);
    }
  };

  /**
   * Handles a ctrl or meta click
   *
   * @param params params from ag grid
   * @param magType the magnitude type for the column clicked on
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onControlClick = (params: any, magType: LegacyEventTypes.MagnitudeType) => {
    const { data } = params;
    const sdId = data.dataForMagnitude.get(magType).signalDetectionId;
    const selectedSDIds: string[] =
      this.props.selectedSdIds.indexOf(sdId) > -1
        ? this.props.selectedSdIds.filter(id => id !== sdId)
        : [...this.props.selectedSdIds, sdId];
    this.props.setSelectedSdIds(selectedSDIds);
  };

  /**
   * Handles a click without modifiers
   *
   * @param params params from ag grid
   * @param magType the magnitude type for the column clicked on
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onDefaultClick = (params: any, magType: LegacyEventTypes.MagnitudeType) => {
    this.props.setSelectedSdIds([params.data.dataForMagnitude.get(magType).signalDetectionId]);
  };

  /**
   * Gets the indices of all rows which has an mbSignalDetectionId
   * that matches the selected sd ids in the props
   *
   * @param magType the magnitude type for the column clicked on
   * @returns list of indices
   */
  private readonly getSelectedRowIndicesForPhase = (
    magType: LegacyEventTypes.MagnitudeType
  ): number[] => {
    const indices = [];
    if (this.mainTable) {
      this.mainTable.getRenderedNodes().forEach(node => {
        const { data } = node;
        const rowSdId = data.dataForMagnitude.get(magType).signalDetectionId;
        if (this.props.selectedSdIds.find(sdId => sdId === rowSdId)) {
          indices.push(node.rowIndex);
        }
      });
    }
    return indices;
  };

  /**
   * Determines whether all, none, or some of the checkboxes are checked
   *
   * @param mainTableRowData the table data for the row
   * @param magType the magnitude type being operated on
   */
  private readonly getAllNoneDef = (
    mainTableRowData: StationMagnitudeRow[],
    magType: LegacyEventTypes.MagnitudeType
  ): MagDefiningStates => {
    // Generate an array of booleans based on the defining settings in the main table row data
    const definingList: boolean[] = mainTableRowData
      .map(mtrd => mtrd.dataForMagnitude.get(magType))
      .filter(mData => mData !== undefined && mData.mag !== undefined)
      .map(mData => mData.defining);
    const allDefining =
      definingList.length > 0
        ? definingList.reduce((accumulator, currentValue) => accumulator && currentValue, true)
        : this.defaultAllDefining;
    const noneDefining = definingList.reduce(
      (accumulator, currentValue) => !currentValue && accumulator,
      true
    );

    // eslint-disable-next-line no-nested-ternary
    return definingList.length < 1
      ? MagDefiningStates.UNDEFINED
      : // eslint-disable-next-line no-nested-ternary
      allDefining
      ? MagDefiningStates.ALL
      : noneDefining
      ? MagDefiningStates.NONE
      : MagDefiningStates.SOME;
  };
}
