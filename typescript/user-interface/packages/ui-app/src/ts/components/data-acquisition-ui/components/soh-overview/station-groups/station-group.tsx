/* eslint-disable react/destructuring-assignment */
import { ContextMenu } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { SohTypes } from '@gms/common-model';
import { setDecimalPrecisionAsNumber } from '@gms/common-util';
import { CenterIcon, HorizontalDivider, Resizer } from '@gms/ui-core-components';
import { getSelectionForAction, getSelectionFromClick } from '@gms/ui-util';
import uniq from 'lodash/uniq';
import * as React from 'react';

import { dataAcquisitionUserPreferences } from '~components/data-acquisition-ui/config';
import { DropZone } from '~components/data-acquisition-ui/shared/table/drop-zone';
import { acknowledgeContextMenu } from '~components/data-acquisition-ui/shared/table/utils';

import type { SohOverviewContextData } from '../soh-overview-context';
import { SohOverviewContext } from '../soh-overview-context';
import { StationCell } from './station-cell';
import type { StatusCounts } from './station-group-header';
import { StationGroupHeader } from './station-group-header';

const MIN_GROUP_HEIGHT_PX = 320;
const MIN_BIN_RATIO = 0.02;

/**
 * The interface for the station group component.
 */
export interface StationGroupProps {
  stationGroupName: string;
  statusCounts: StatusCounts;
  totalStationCount: number;
  // The status and station information used to render a cell in the station group table
  sohStatuses: SohTypes.UiStationSoh[];
  // Status and station information for acknowledgeable cells.
  needsAttentionStatuses: SohTypes.UiStationSoh[];
  isHighlighted: boolean;
  selectedStationIds: string[];
  // Used to trigger a refresh of this group
  groupHeight: number;
  topContainerHeight: number;
  // Changed to trigger a refresh of the group
  setGroupHeight(h: number): void;
  setTopContainerHeight(h: number): void;
  setSelectedStationIds(ids: string[]): void;
}

/**
 * * cellDrop
 * removes the highlight from the drop zone and acknowledges that station
 *
 * @param payload the payload from the drag event
 * @param acknowledgeSohStatus the function to call on drop
 */
export const cellDrop = (
  payload: string[],
  acknowledgeSohStatus: (stationIds: string[]) => void
): void => {
  acknowledgeSohStatus(uniq(payload));
};

/**
 * Called when the user right-clicks a station in the top section of a table.
 * Creates a context menu with an acknowledge option that calls the acknowledge function.
 *
 * @param e the mouse event
 */
export const onCellRightClick = (
  e: React.MouseEvent<HTMLDivElement>,
  names: string[],
  index: number,
  stationSohs: SohTypes.UiStationSoh[],
  setSelection: (s: string[]) => void,
  context: SohOverviewContextData
): void => {
  e.preventDefault();
  const items = stationSohs.map(soh => ({ id: soh.stationName }));
  const ids = getSelectionForAction(e, names, index, items);
  setSelection(ids);
  // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
  ContextMenu.show(
    acknowledgeContextMenu(
      ids,
      stationSohs,
      context.sohStationStaleTimeMS,
      (stationIds: string[], comment?: string) => context.acknowledgeSohStatus(stationIds, comment)
    ),
    { left: e.clientX, top: e.clientY },
    undefined,
    true
  );
};

/**
 * Updates the selection based on the intention of the click, and then sets the selection
 *
 * @param e the mouse event that started it all
 * @param selection the stations we have selected currently
 * @param index the index of the station that was clicked
 * @param sohStations the list of stations
 * @param setSelection a function to update the state of the selection
 */
export const handleClick = (
  e: React.MouseEvent,
  selection: string[],
  index: number,
  sohStations: SohTypes.UiStationSoh[],
  setSelection: (s: string[]) => void
): void => {
  e.stopPropagation();
  const updatedSelection = getSelectionFromClick(
    e,
    selection,
    index,
    sohStations.map(soh => ({ id: soh.stationName }))
  );
  setSelection(updatedSelection);
};

/**
 * Finds station group in a station groups object and checks its capability status. If not defined, it will be "NONE."
 * payload -> data -> sohStatus -> stationGroups -> match stationGroupName: "foo" and get groupCapabilityStatus: "_"
 *
 * @param stationGroupsSoh - a stationGroups object which contains all defined station groups
 * @param stationGroupName - the name of the station group we are looking for
 */
export const getGroupSohCapability = (
  stationGroupsSoh: SohTypes.StationGroupSohStatus[],
  stationGroupName: string
): SohTypes.SohStatusSummary => {
  const stationGroup = stationGroupsSoh.find(sg => sg.stationGroupName === stationGroupName);
  return stationGroup ? stationGroup.groupCapabilityStatus : SohTypes.SohStatusSummary.NONE;
};

/**
 * Finds a station's capability status, with respect to the station group it is in.
 *
 * @param soh - a sohStatus.stationSoh object
 * @param stationGroupName - the name of the station group we are looking for, to which the station belongs
 */
export const getStationSohCapability = (
  soh: SohTypes.UiStationSoh,
  stationGroupName: string
): SohTypes.SohStatusSummary => {
  const stationGroup = soh.stationGroups.find(sg => sg.groupName === stationGroupName);
  return stationGroup ? stationGroup.sohStationCapability : SohTypes.SohStatusSummary.NONE;
};

/**
 * Custom hook which resizes the HorizontalDivider to maintain the ratio
 * that the user has set. When the table resizes, this will trigger divider resizing
 * as well to keep the ratio.
 *
 * @param tableContainerRef the table that contains the Horizontal Divider. Used to
 * find the full height.
 * @param dividerRef the HorizontalDivider, which is resized.
 * @returns scaleTopBinHeightToRatio tells the dividerRatio to maintain the current
 * ratio by updating the height of the top bin.
 * updateDividerRatio changes the ratio to equal the current ratio of the top bin
 * to the parent table.
 */
const useDividerRatioOnResize = (
  tableContainerRef: React.MutableRefObject<HTMLElement>,
  dividerRef: React.MutableRefObject<HorizontalDivider>,
  groupHeight: number
) => {
  const context = React.useContext(SohOverviewContext);
  const [dividerRatio, setDividerRatio] = React.useState(1 / 2);

  const getFullGroupHeight = () => tableContainerRef?.current?.clientHeight ?? groupHeight;
  const getTopContainerHeight = () => Math.round(dividerRef?.current?.getTopContainerHeight());

  /**
   * Calculate the current ratio
   */
  const calculateRatio = () =>
    setDecimalPrecisionAsNumber(
      Math.max(
        MIN_BIN_RATIO,
        Math.min(getTopContainerHeight() / getFullGroupHeight(), 1 - MIN_BIN_RATIO)
      )
    );

  /**
   * Updates the divider ratio to whatever the current ratio is between the
   * top container and the full table height.
   */
  const updateDividerRatio = () => {
    const topContainerHeight = getTopContainerHeight();
    if (topContainerHeight > 0) {
      const newRatio = calculateRatio();
      setDividerRatio(newRatio);
    }
  };

  /**
   * Scales the top container to maintain the ratio
   */
  const scaleTopBinHeightToRatio = () => {
    const fullGroupHeight = getFullGroupHeight();
    if (fullGroupHeight > 0) {
      const newTopHeight = fullGroupHeight * dividerRatio;
      if (dividerRef && dividerRef.current) {
        dividerRef.current.setTopContainerHeight(newTopHeight);
      }
    }
  };

  /**
   * Update the top container to maintain the ratio when the golden layout
   * or the table heights change.
   */
  React.useEffect(() => {
    // When a layout is open or restored, the height is 0 temporally
    // do not want to update until container height is established
    if (tableContainerRef?.current?.clientHeight > 0) {
      scaleTopBinHeightToRatio();
    }
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [context.glContainer?.isHidden, context.glContainer?.height, getFullGroupHeight()]);

  return {
    scaleTopBinHeightToRatio,
    updateDividerRatio
  };
};

/**
 * * Station Group
 * Station Group renders the station header and station cells for the group provided
 *
 * @param props for the component
 */
export function StationGroup(props: StationGroupProps) {
  const context = React.useContext(SohOverviewContext);
  const tableContainerRef = React.useRef<HTMLDivElement>(undefined);
  const dividerRef = React.useRef<HorizontalDivider>(undefined);
  const { scaleTopBinHeightToRatio, updateDividerRatio } = useDividerRatioOnResize(
    tableContainerRef,
    dividerRef,
    props.groupHeight
  );

  const groupSohCapabilityStatus = getGroupSohCapability(
    context.stationGroupSoh,
    props.stationGroupName
  );
  const iconSize = 25;

  return (
    <Resizer
      className="quadra-grid-cell soh-overview-table__wrapper"
      dataCy={`soh-overview-group-${props.stationGroupName}`}
      key={props.stationGroupName}
      forwardRef={tableContainerRef}
      onResize={() => {
        scaleTopBinHeightToRatio();
      }}
      onResizeEnd={(h: number) => {
        props.setGroupHeight(h);
      }}
      minHeightPx={MIN_GROUP_HEIGHT_PX}
    >
      <HorizontalDivider
        ref={dividerRef}
        sizeRange={dataAcquisitionUserPreferences.overviewMinContainerRange}
        onResizeEnd={updateDividerRatio}
        top={
          <div className="soh-overview-table">
            <StationGroupHeader
              displayName={props.stationGroupName}
              statusCounts={props.statusCounts}
              capabilityStatus={groupSohCapabilityStatus}
              totalStationCount={props.totalStationCount}
            />
            <div
              className="soh-overview-table__bin soh-overview-table__bin--top"
              key={props.stationGroupName}
            >
              <div className="soh-table-label">Needs Attention</div>
              <div className="soh-overview-cell__container" data-cy="soh-unacknowledged">
                {props.needsAttentionStatuses.length !== 0 ? (
                  props.needsAttentionStatuses?.map((soh, index) => {
                    const stationId = `${props.stationGroupName}-bottom-${soh.stationName}`;
                    const stationSohCapabilityStatus = getStationSohCapability(
                      soh,
                      props.stationGroupName
                    );
                    return (
                      <StationCell
                        status={soh.sohStatusSummary}
                        capabilityStatus={stationSohCapabilityStatus}
                        selected={
                          props.selectedStationIds.find(
                            selectedCell => selectedCell === soh.stationName
                          ) !== undefined
                        }
                        key={stationId}
                        name={soh.stationName}
                        needsAttention
                        onClick={e => {
                          handleClick(
                            e,
                            props.selectedStationIds,
                            index,
                            props.needsAttentionStatuses,
                            ids => props.setSelectedStationIds(ids)
                          );
                        }}
                        onRightClick={e => {
                          onCellRightClick(
                            e,
                            props.selectedStationIds,
                            index,
                            props.needsAttentionStatuses,
                            ids => props.setSelectedStationIds(ids),
                            context
                          );
                        }}
                      />
                    );
                  })
                ) : (
                  <CenterIcon
                    iconName={IconNames.TICK_CIRCLE}
                    description="Nothing to acknowledge"
                    iconSize={iconSize}
                    className="table-background-icon"
                  />
                )}
              </div>
            </div>
          </div>
        }
        bottom={
          <div className="soh-overview-table">
            <div
              className="soh-overview-table__bin soh-overview-table__bin--bottom"
              data-cy="soh-acknowledged"
            >
              <DropZone
                className={`soh-overview-table__drop-zone soh-overview-cell__container
                  ${props.isHighlighted ? 'soh-overview-table__drop-zone--highlighted' : ''}`}
                key={props.stationGroupName}
                onDrop={(payload: string[]) => {
                  cellDrop(payload, (stationIds: string[]) =>
                    context.acknowledgeSohStatus(stationIds)
                  );
                }}
              >
                {props.sohStatuses.map((soh, index) => {
                  const stationSohCapabilityStatus = getStationSohCapability(
                    soh,
                    props.stationGroupName
                  );
                  return (
                    <StationCell
                      ref={null}
                      key={`${props.stationGroupName}-top-${soh.stationName}`}
                      selected={
                        props.selectedStationIds.find(
                          selectedCell => selectedCell === soh.stationName
                        ) !== undefined
                      }
                      status={soh.sohStatusSummary}
                      capabilityStatus={stationSohCapabilityStatus}
                      name={soh.stationName}
                      needsAttention={false}
                      onClick={e => {
                        handleClick(e, props.selectedStationIds, index, props.sohStatuses, ids =>
                          props.setSelectedStationIds(ids)
                        );
                      }}
                      onRightClick={e => {
                        onCellRightClick(
                          e,
                          props.selectedStationIds,
                          index,
                          props.sohStatuses,
                          ids => props.setSelectedStationIds(ids),
                          context
                        );
                      }}
                    />
                  );
                })}
              </DropZone>
            </div>
          </div>
        }
      />
    </Resizer>
  );
}
