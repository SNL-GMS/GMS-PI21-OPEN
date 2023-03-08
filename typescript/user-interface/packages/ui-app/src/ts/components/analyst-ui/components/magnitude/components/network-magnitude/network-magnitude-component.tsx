/* eslint-disable react/destructuring-assignment */
import { LegacyEventTypes } from '@gms/common-model';
import { uuid } from '@gms/common-util';
import type { TableApi } from '@gms/ui-core-components';
import { Table } from '@gms/ui-core-components';
import classNames from 'classnames';
import defer from 'lodash/defer';
import sortBy from 'lodash/sortBy';
import React from 'react';

import { getNetworkMagSolution } from '~analyst-ui/common/utils/magnitude-util';

import { generateNetworkMagnitudeColumnDefs } from './table-utils/column-defs';
import type {
  NetworkMagnitudeData,
  NetworkMagnitudeProps,
  NetworkMagnitudeRow,
  NetworkMagnitudeState
} from './types';

/**
 * Network magnitude table, uses table from core-components
 */
export class NetworkMagnitude extends React.Component<
  NetworkMagnitudeProps,
  NetworkMagnitudeState
> {
  /** The ag-grid table reference */
  private mainTable: TableApi;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React component lifecycle
   *
   * @param prevProps The previous properties available to this react component
   */
  public componentDidUpdate(prevProps: NetworkMagnitudeProps): void {
    // If the selected solution has changed select it
    if (prevProps.selectedSolutionId !== this.props.selectedSolutionId) {
      this.selectRowsFromProps(this.props);
    }
  }

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const rowData = this.generateTableRows();
    return (
      <div className={classNames('ag-theme-dark', 'table-container')}>
        <div className="list-wrapper">
          <div className="max">
            <Table
              columnDefs={generateNetworkMagnitudeColumnDefs(this.props.displayedMagnitudeTypes)}
              gridOptions={this.props.options}
              rowData={rowData}
              getRowId={node => node.data.id}
              rowSelection="multiple"
              onGridReady={this.onMainTableReady}
              rowDeselection
              suppressContextMenu
              onRowClicked={this.onRowClicked}
              overlayNoRowsTemplate="No Data"
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
   * Event handler for ag-gird that is fired when the table is ready
   *
   * @param event event of the table action
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readonly onMainTableReady = (event: any) => {
    this.mainTable = event.api;
  };

  /**
   * Generate NetworkMagnitudeRow[] based on query.
   *
   * @returns a NetworkMagnitudeRow[]
   */
  private readonly generateTableRows = (): NetworkMagnitudeRow[] => {
    if (!this.props.locationSolutionSet) return [];
    const rows = [];
    this.props.locationSolutionSet.locationSolutions.forEach(ls => {
      const dataForMagnitude = new Map<LegacyEventTypes.MagnitudeType, NetworkMagnitudeData>();
      Object.keys(LegacyEventTypes.MagnitudeType).forEach(key => {
        const defStations = this.calculateDefiningStationNumbers(
          ls,
          LegacyEventTypes.MagnitudeType[key]
        );
        const magSolution = getNetworkMagSolution(ls, LegacyEventTypes.MagnitudeType[key]);
        dataForMagnitude.set(LegacyEventTypes.MagnitudeType[key], {
          magnitude: magSolution ? magSolution.magnitude : undefined,
          stdDeviation: magSolution ? magSolution.uncertainty : undefined,
          numberOfDefiningStations: defStations ? defStations.numberOfDefining : undefined,
          numberOfNonDefiningStations: defStations ? defStations.numberOfNonDefining : undefined
        });
      });
      const locationSolutionRow: NetworkMagnitudeRow = {
        id: uuid.asString(),
        isPreferred: ls.id === this.props.preferredSolutionId,
        location: LegacyEventTypes.PrettyDepthRestraint[ls.locationRestraint.depthRestraintType],
        dataForMagnitude
      };
      rows.push(locationSolutionRow);
    });
    return sortBy(rows, row => row.location);
  };

  /**
   * Calculates the number of defining and non defining stations for a location solution
   *
   * @param locationSolution a location solution
   * @param magnitudeType a magnitude type
   *
   * @returns a object {numberOfDefining, numberOfNonDefining}
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly calculateDefiningStationNumbers = (
    locationSolution: LegacyEventTypes.LocationSolution,
    magnitudeType: string
  ): { numberOfDefining: number; numberOfNonDefining: number } => {
    const networkMagnitudeSolution = locationSolution.networkMagnitudeSolutions.find(
      netMagSol => netMagSol.magnitudeType === magnitudeType
    );
    let numberOfNetworkMagnitudeBehaviors;
    let numberOfDefining = 0;
    if (networkMagnitudeSolution) {
      numberOfNetworkMagnitudeBehaviors = networkMagnitudeSolution.networkMagnitudeBehaviors.length;
      networkMagnitudeSolution.networkMagnitudeBehaviors.forEach(netMagBehavior => {
        if (netMagBehavior.defining) {
          numberOfDefining += 1;
        }
      });
    }
    if (numberOfNetworkMagnitudeBehaviors) {
      return {
        numberOfDefining,
        numberOfNonDefining: numberOfNetworkMagnitudeBehaviors - numberOfDefining
      };
    }
    return { numberOfDefining: undefined, numberOfNonDefining: undefined };
  };

  /**
   * Update the selected location solution when the user clicks on a row in the table.
   */
  private readonly onRowClicked = () => {
    if (this.mainTable) {
      defer(() => {
        const selectedId = this.mainTable.getSelectedNodes().map(node => node.data.id)[0];
        this.props.setSelectedLocationSolution(this.props.locationSolutionSet.id, selectedId);
      });
    }
  };

  /**
   * Select rows in the table based on the selected location solution id in the properties.
   *
   * @param props signal detection props
   */
  private readonly selectRowsFromProps = (props: NetworkMagnitudeProps) => {
    if (this.mainTable) {
      this.mainTable.deselectAll();
      this.mainTable.forEachNode(node => {
        if (node.data.id === props.selectedSolutionId) {
          node.setSelected(true);
          // Must pass in null here as ag-grid expects it
          this.mainTable.ensureNodeVisible(node, null);
        }
      });
    }
  };
}
