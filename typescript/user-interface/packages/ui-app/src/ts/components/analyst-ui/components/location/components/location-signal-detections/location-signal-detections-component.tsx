/* eslint-disable react/destructuring-assignment */
import { Classes } from '@blueprintjs/core';
import type { LegacyEventTypes } from '@gms/common-model';
import {
  dateToString,
  ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  MILLISECONDS_IN_SECOND
} from '@gms/common-util';
import type { TableApi } from '@gms/ui-core-components';
import { Table } from '@gms/ui-core-components';
import classNames from 'classnames';
import defer from 'lodash/defer';
import isEqual from 'lodash/isEqual';
import sortBy from 'lodash/sortBy';
import uniqueId from 'lodash/uniqueId';
import memoizeOne from 'memoize-one';
import React from 'react';

import { gmsColors } from '~scss-config/color-preferences';

import type { SignalDetectionSnapshotWithDiffs } from '../../types';
import { generateLocationSDColumnDef } from './table-utils/column-defs';
import type { LocationSDRow, LocationSignalDetectionsProps } from './types';
import { DefiningChange, DefiningStates } from './types';

/**
 * Enables the analyst to select which signal detection feature measurements are defining
 * for an event location.
 */
export class LocationSignalDetections extends React.Component<LocationSignalDetectionsProps> {
  /**
   * A memoized function for generating the table rows.
   * The memoization function caches the results using
   * the most recent argument and returns the results.
   *
   * @param signalDetections the signal detections (with snapshot differences)
   * @param distances distance related to the current event
   * @param event The current event
   * @param historicalMode the historical mode setting
   *
   * @returns row objects for Location Signal Detection table
   */
  private readonly memoizedGenerateTableRows: (
    signalDetections: SignalDetectionSnapshotWithDiffs[],
    distances: LegacyEventTypes.LocationToStationDistance[],
    event: LegacyEventTypes.Event,
    historicalMode: boolean
  ) => LocationSDRow[];

  /**
   * To interact directly with the table
   */
  private mainTable: TableApi;

  /**
   * constructor
   */
  public constructor(props: LocationSignalDetectionsProps) {
    super(props);
    this.memoizedGenerateTableRows =
      typeof memoizeOne === 'function'
        ? memoizeOne(
            this.generateTableRows,
            /* tell memoize to use a deep comparison for complex objects */
            isEqual
          )
        : this.generateTableRows;
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React component lifecycle
   *
   * @param prevProps The previous properties available to this react component
   */
  public componentDidUpdate(prevProps: LocationSignalDetectionsProps): void {
    if (this.props && !isEqual(this.props, prevProps)) {
      this.selectRowsFromProps(this.props);
    }
  }

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const mainTableRowData = this.memoizedGenerateTableRows(
      this.props.signalDetectionDiffSnapshots,
      this.props.distances,
      this.props.event,
      this.props.historicalMode
    );

    const timeAllDefining = mainTableRowData
      .map(row => row.arrivalTimeDefining)
      .reduce((accumulator, currentValue) => accumulator && currentValue, true);
    const timeNoneDefining = mainTableRowData
      .map(row => row.arrivalTimeDefining)
      .reduce((accumulator, currentValue) => !currentValue && accumulator, true);
    // eslint-disable-next-line no-nested-ternary
    const timeDefiningState: DefiningStates = timeAllDefining
      ? DefiningStates.ALL
      : timeNoneDefining
      ? DefiningStates.NONE
      : DefiningStates.SOME;

    const slownessAllDefining = mainTableRowData
      .map(row => row.slownessDefining)
      .reduce((accumulator, currentValue) => accumulator && currentValue, true);
    const slownessNoneDefining = mainTableRowData
      .map(row => row.slownessDefining)
      .reduce((accumulator, currentValue) => !currentValue && accumulator, true);
    // eslint-disable-next-line no-nested-ternary
    const slownessDefiningState: DefiningStates = slownessAllDefining
      ? DefiningStates.ALL
      : slownessNoneDefining
      ? DefiningStates.NONE
      : DefiningStates.SOME;

    const azimuthAllDefining = mainTableRowData
      .map(row => row.azimuthDefining)
      .reduce((accumulator, currentValue) => accumulator && currentValue, true);
    const azimuthNoneDefining = mainTableRowData
      .map(row => row.azimuthDefining)
      .reduce((accumulator, currentValue) => !currentValue && accumulator, true);
    // eslint-disable-next-line no-nested-ternary
    const azimuthDefiningState: DefiningStates = azimuthAllDefining
      ? DefiningStates.ALL
      : azimuthNoneDefining
      ? DefiningStates.NONE
      : DefiningStates.SOME;
    return (
      <div className={classNames('ag-theme-dark', 'table-container', Classes.DARK)}>
        <div className="list-wrapper">
          <div className="max">
            <Table
              context={{}}
              columnDefs={generateLocationSDColumnDef(
                this.props.setDefining,
                timeDefiningState,
                azimuthDefiningState,
                slownessDefiningState,
                this.props.historicalMode
              )}
              gridOptions={{
                rowClass: 'location-sd-row'
              }}
              rowData={sortBy(mainTableRowData, row => row.distance)}
              getRowId={node => node.data.id}
              rowSelection="multiple"
              onGridReady={this.onMainTableReady}
              rowDeselection
              getRowStyle={this.getDiffRowStyles}
              suppressContextMenu
              onRowClicked={this.onRowClicked}
              onCellContextMenu={this.onCellContextMenu}
              overlayNoRowsTemplate="No SDs Associated to Selected Event"
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
   * Convert the event data into table rows
   *
   * @param signalDetections the signal detections (with snapshot differences)
   * @param distances distance related to the current event
   * @param event The current event
   * @param historicalMode the historical mode setting
   *
   * @returns row objects for Location Signal Detection table
   */
  private readonly generateTableRows = (
    signalDetections: SignalDetectionSnapshotWithDiffs[],
    distances: LegacyEventTypes.LocationToStationDistance[],
    event: LegacyEventTypes.Event,
    historicalMode: boolean
  ): LocationSDRow[] =>
    // eslint-disable-next-line complexity
    signalDetections.map(sd => {
      const distance = distances
        ? distances.find(d => d.stationId === sd.stationName).distance
        : undefined;
      // id is a unique string part of row used by node.id to identify unique table rows (using SDH id since unique)
      return {
        id: uniqueId(),
        signalDetectionId: sd.signalDetectionId,
        eventId: event.id,
        station: sd.stationName,
        // TODO: What should we display? Using SD channel name example would display 'PDAR.fkb FK_BEAM'
        channel: 'fkb',
        phase: sd.phase,
        distance: distance ? distance.degrees : undefined,
        timeObs: dateToString(
          new Date(sd.time.observed * MILLISECONDS_IN_SECOND),
          ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
        ),
        timeRes: sd.time.residual ? sd.time.residual : undefined,
        timeCorr: sd.time.correction ? sd.time.correction : undefined,
        azimuthObs: sd.azimuth.observed ? sd.azimuth.observed : undefined,
        azimuthRes: sd.azimuth.residual ? sd.azimuth.residual : undefined,
        azimuthCorr: sd.azimuth.correction ? sd.azimuth.correction : undefined,
        slownessObs: sd.slowness.observed ? sd.slowness.observed : undefined,
        slownessRes: sd.slowness.residual ? sd.slowness.residual : undefined,
        slownessCorr: sd.slowness.correction ? sd.slowness.correction : undefined,
        updateIsDefining: this.props.updateIsDefining,
        arrivalTimeDefining: sd.rejectedOrUnassociated ? false : sd.time.defining,
        slownessDefining: sd.rejectedOrUnassociated ? false : sd.slowness.defining,
        azimuthDefining: sd.rejectedOrUnassociated ? false : sd.azimuth.defining,
        isAssociatedDiff: sd.diffs.isAssociatedDiff,
        timeDefiningDiff: sd.diffs.arrivalTimeDefining !== DefiningChange.NO_CHANGE,
        azimuthDefiningDiff: sd.diffs.azimuthDefining !== DefiningChange.NO_CHANGE,
        slownessDefiningDiff: sd.diffs.slownessDefining !== DefiningChange.NO_CHANGE,
        arrivalTimeDiff: sd.diffs.arrivalTimeDiff,
        azimuthObsDiff: sd.diffs.azimuthObsDiff,
        slownessObsDiff: sd.diffs.slownessObsDiff,
        phaseDiff: sd.diffs.phaseDiff,
        channelNameDiff: sd.diffs.channelNameDiff,
        historicalMode,
        rejectedOrUnassociated: sd.rejectedOrUnassociated
      };
    });

  /**
   * Set class members when main table is ready
   *
   * @param event table event
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onMainTableReady = (event: any): void => {
    this.mainTable = event.api;
  };

  /**
   * Select rows in the table based on the selected SD IDs in the properties.
   *
   * @param props signal detection props
   */
  private readonly selectRowsFromProps = (props: LocationSignalDetectionsProps) => {
    if (this.mainTable) {
      this.mainTable.deselectAll();
      this.mainTable.forEachNode(node => {
        props.selectedSdIds.forEach(sdId => {
          if (node.data.signalDetectionId === sdId) {
            node.setSelected(true);
            // Must pass in null here as ag-grid expects it
            this.mainTable.ensureNodeVisible(node, null);
          }
        });
      });
    }
  };

  /**
   * Creates a context menu and displays it
   *
   * @param params table parameters can be found in ag grid docs
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onCellContextMenu = (params: any) => {
    params.event.preventDefault();
    params.event.stopPropagation();
    if (this.props.historicalMode) {
      this.props.toast('Select current location solution set to modify signal detections');
      return;
    }
    const selectedIdsInTable = this.mainTable
      .getSelectedNodes()
      .map(node => node.data.signalDetectionId);
    const selectedSdIds =
      selectedIdsInTable.indexOf(params.node.data.signalDetectionId) < 0
        ? [...selectedIdsInTable, params.node.data.signalDetectionId]
        : selectedIdsInTable;
    this.props.setSelectedSdIds(selectedSdIds);
    this.props.showSDContextMenu(selectedSdIds, params.event.clientX, params.event.clientY);
  };

  /**
   * Shows sd details menu.
   *
   * @param rowParams in format which can be found in ag grid docs
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onRowClicked = (rowParams: any): void => {
    if (this.mainTable) {
      if (rowParams.event.altKey) {
        if (this.props.historicalMode) {
          this.props.toast('Select current location solution set to view signal detection details');
          return;
        }
        this.props.showSDDetails(
          rowParams.data.signalDetectionId,
          rowParams.event.clientX,
          rowParams.event.clientY
        );
      } else if (!this.props.historicalMode) {
        defer(() => {
          const selectedSdIds = this.mainTable
            .getSelectedNodes()
            .map(node => node.data.signalDetectionId);
          this.props.setSelectedSdIds(selectedSdIds);
        });
      }
    }
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any, class-methods-use-this
  private readonly getDiffRowStyles = (params: any) => {
    if (params.data.isAssociatedDiff) {
      if (params.data.rejectedOrUnassociated) {
        return {
          color: `${gmsColors.gmsSoft} !important`
        };
      }
      return {
        'background-color': gmsColors.gmsTableChangeMarker
      };
    }
    return {};
  };
}
