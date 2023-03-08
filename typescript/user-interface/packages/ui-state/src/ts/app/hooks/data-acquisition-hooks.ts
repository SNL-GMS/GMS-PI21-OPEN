import React from 'react';

import type { FilterableSOHTypes, FilterableSOHTypesDrillDown, FilterLists } from '../state';
import { dataAcquisitionActions } from '../state';
import { useAppDispatch, useAppSelector } from './react-redux-hooks';

export function useStatusesToDisplay<T extends FilterableSOHTypes | FilterableSOHTypesDrillDown>(
  list: FilterLists
): [Record<T, boolean>, (filters: Record<T, boolean>) => void] {
  const statusesToDisplay = useAppSelector(
    state => state.app.dataAcquisition.filtersToDisplay[list]
  );
  const dispatch = useAppDispatch();
  const setStatusesToDisplay = React.useCallback(
    (filters: Record<T, boolean>) => {
      dispatch(
        dataAcquisitionActions.setFiltersToDisplay({
          list,
          filters
        })
      );
    },
    [dispatch, list]
  );
  return [(statusesToDisplay as unknown) as Record<T, boolean>, setStatusesToDisplay];
}
