import type { FilterListTypes } from '@gms/common-model';
import { nonIdealStateWithNoSpinner } from '@gms/ui-core-components';
import {
  useCurrentIntervalWithBuffer,
  useGetFilterListsDefinitionQuery,
  useSelectedFilterList,
  useUiTheme
} from '@gms/ui-state';
import type { SerializedError } from '@reduxjs/toolkit';
import * as React from 'react';

import { FilterListOrNonIdealState } from './filter-list';
import { FilterListPicker } from './filter-list-picker';
import { checkForUniqueness } from './filter-list-util';

const filterPanelNonIdealStateTitle = 'Filters';

export interface FilterListNonIdealStateProps extends React.PropsWithChildren<unknown> {
  error: string | SerializedError | undefined;
  filterLists: FilterListTypes.FilterList[] | undefined;
}

/**
 * Either renders the children, or a non ideal state. If an error is provided, or if there
 * is not a current interval, renders the non ideal state.
 */
export const FilterListNonIdealState: React.FC<FilterListNonIdealStateProps> = ({
  children,
  error,
  filterLists
}: FilterListNonIdealStateProps) => {
  const currentInterval = useCurrentIntervalWithBuffer();
  if (error) {
    let errorMsg = `Error loading filter list`;
    if (typeof error === 'string') {
      errorMsg += ` ${error}`;
    } else {
      errorMsg += ` ${error.message}`;
    }
    return nonIdealStateWithNoSpinner(filterPanelNonIdealStateTitle, errorMsg);
  }
  if (currentInterval?.endTimeSecs == null || currentInterval?.startTimeSecs == null) {
    return nonIdealStateWithNoSpinner(
      filterPanelNonIdealStateTitle,
      'Open an interval to see filters'
    );
  }
  if (!filterLists || filterLists.length === 0) {
    return nonIdealStateWithNoSpinner(filterPanelNonIdealStateTitle, 'No filter lists found');
  }
  if (!checkForUniqueness(filterLists)) {
    return nonIdealStateWithNoSpinner(
      'Error: Filter Lists',
      'Filter List names must be unique. Duplicate names found.',
      'error'
    );
  }
  return children;
};

/**
 * Renders the filter list and filter list picker, or non ideal states.
 */
// eslint-disable-next-line react/function-component-definition
export const FiltersPanel: React.FunctionComponent = () => {
  const filterListQuery = useGetFilterListsDefinitionQuery();
  const selectedFilterList = useSelectedFilterList();
  const [uiTheme] = useUiTheme();

  return (
    <FilterListNonIdealState
      error={filterListQuery.error}
      filterLists={filterListQuery.data?.filterLists}
    >
      <FilterListPicker
        filterLists={filterListQuery.data?.filterLists}
        isLoading={filterListQuery.isLoading}
        selectedFilterList={selectedFilterList}
      />
      <FilterListOrNonIdealState
        // key controlled to rebuild if the theme or filter list changes
        key={`${selectedFilterList?.name}-${uiTheme.name}`}
        filterList={selectedFilterList}
      />
    </FilterListNonIdealState>
  );
};
