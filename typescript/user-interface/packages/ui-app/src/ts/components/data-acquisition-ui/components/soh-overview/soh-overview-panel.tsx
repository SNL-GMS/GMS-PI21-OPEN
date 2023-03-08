/* eslint-disable react/destructuring-assignment */
import { Displays, SohTypes } from '@gms/common-model';
import { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import type { FilterableSOHTypes } from '@gms/ui-state';
import {
  dataAcquisitionActions,
  useAppDispatch,
  useAppSelector,
  useStatusesToDisplay
} from '@gms/ui-state';
import Immutable from 'immutable';
import sortBy from 'lodash/sortBy';
import React from 'react';

import { messageConfig } from '~components/data-acquisition-ui/config/message-config';
import { StationDeselectHandler } from '~components/data-acquisition-ui/shared/table/station-deselect-handler';
import { SohToolbar } from '~components/data-acquisition-ui/shared/toolbars/soh-toolbar';

import { SohOverviewContext } from './soh-overview-context';
import { StationGroupsLayout } from './station-groups/station-groups-layout';

function useOverviewStationGroups(
  initialStationGroupsToDisplay: Record<string, boolean>
): [Record<string, boolean>, (newRecord: Record<string, boolean>) => void] {
  const dispatch = useAppDispatch();
  const stationGroupsToDisplay = useAppSelector(
    state => state.app.dataAcquisition.filtersToDisplay['soh-overview-groups']
  );
  const setStationGroupsToDisplay = React.useCallback(
    (groupsToDisplay: Record<string, boolean>) => {
      dispatch(
        dataAcquisitionActions.setFiltersToDisplay({
          list: 'soh-overview-groups',
          filters: groupsToDisplay
        })
      );
    },
    [dispatch]
  );
  React.useEffect(() => {
    if (stationGroupsToDisplay === undefined) {
      setStationGroupsToDisplay(initialStationGroupsToDisplay);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return [stationGroupsToDisplay, setStationGroupsToDisplay];
}

/**
 * Panel creates the toolbar at the top of the display and calls the SOH Overview component
 * to render it's children.
 */
export function SohOverviewPanel() {
  const context = React.useContext(SohOverviewContext);

  const updateKey = JSON.stringify(sortBy(context.stationGroupSoh));
  const initialStationGroupsToDisplay = React.useMemo(() => {
    const groups = {};
    context.stationGroupSoh.forEach(group => {
      groups[group.stationGroupName] = true;
    });
    return groups;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [updateKey]);

  const [stationGroupsToDisplay, setStationGroupsToDisplay] = useOverviewStationGroups(
    initialStationGroupsToDisplay
  );

  const [isHighlighted, setIsHighlighted] = React.useState(false);

  const [statusesToDisplay, setStatusesToDisplay] = useStatusesToDisplay<FilterableSOHTypes>(
    Displays.SohDisplays.SOH_OVERVIEW
  );

  const highlightButtonRef = React.useRef(null);

  const statusToDisplayAsArray = Object.keys(statusesToDisplay).filter(k => statusesToDisplay[k]);

  const toggleHighlight = (ref: HTMLDivElement) => {
    setIsHighlighted(!isHighlighted);
    highlightButtonRef.current = ref ?? highlightButtonRef.current;
    if (highlightButtonRef && highlightButtonRef.current && highlightButtonRef.current.classList) {
      highlightButtonRef.current.classList.toggle('isHighlighted');
    }
  };

  const stationGroupFilterWidthPx = 200;
  const stationGroupsToDisplayCheckBoxDropdown: DeprecatedToolbarTypes.CheckboxDropdownItem = {
    enumOfKeys: stationGroupsToDisplay ? Object.keys(stationGroupsToDisplay) : [],
    label: messageConfig.labels.sohToolbar.filterByStationGroup,
    menuLabel: messageConfig.labels.sohToolbar.filterByStationGroup,
    rank: 0,
    widthPx: stationGroupFilterWidthPx,
    type: DeprecatedToolbarTypes.ToolbarItemType.CheckboxList,
    tooltip: messageConfig.tooltipMessages.sohToolbar.filerByStationGroup,
    values: Immutable.OrderedMap(stationGroupsToDisplay),
    onChange: value => {
      setStationGroupsToDisplay(value.toObject());
    },
    cyData: 'filter-by-station-group-soh'
  };

  return (
    <>
      <div className="soh-overview-toolbar__container" data-cy="soh-overview-toolbar">
        <SohToolbar
          setStatusesToDisplay={setStatusesToDisplay}
          statusesToDisplay={statusesToDisplay}
          toggleHighlight={toggleHighlight}
          leftItems={[stationGroupsToDisplayCheckBoxDropdown]}
          rightItems={[]}
          statusFilterText={messageConfig.labels.sohToolbar.filterStatuses}
          statusFilterTooltip={messageConfig.tooltipMessages.sohToolbar.selectStatuses}
          isDrillDown={false}
        />
      </div>

      <StationDeselectHandler
        setSelectedStationIds={(ids: string[]) => context.setSelectedStationIds(ids)}
      >
        <StationGroupsLayout
          statusesToDisplay={statusToDisplayAsArray.map(s => SohTypes.SohStatusSummary[s])}
          isHighlighted={isHighlighted}
          stationGroupsToDisplay={
            stationGroupsToDisplay
              ? Object.keys(stationGroupsToDisplay).filter(
                  stationGroupName => stationGroupsToDisplay[stationGroupName]
                )
              : []
          }
        />
      </StationDeselectHandler>
    </>
  );
}
