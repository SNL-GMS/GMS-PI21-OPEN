import { Alignment, Button, H6, MenuItem } from '@blueprintjs/core';
import { Select2 } from '@blueprintjs/select';
import type { FilterListTypes } from '@gms/common-model';
import { useSetFilterList } from '@gms/ui-state';
import React from 'react';

import { FilterListPickerEntry } from './filter-list-picker-entry';

interface FilterListPickerProps {
  filterLists: FilterListTypes.FilterList[];
  isLoading: boolean;
  selectedFilterList: FilterListTypes.FilterList;
}

const FilterListSelect = Select2.ofType<FilterListTypes.FilterList>();

/**
 * A component that lets the user choose the filter list to use.
 */
// eslint-disable-next-line react/function-component-definition
export const FilterListPicker: React.FC<FilterListPickerProps> = ({
  filterLists,
  isLoading,
  selectedFilterList
}: FilterListPickerProps) => {
  const setFilterList = useSetFilterList();
  const filterListRenderer = (item: FilterListTypes.FilterList, itemRenderer) => (
    <FilterListPickerEntry
      key={item.name}
      filterList={item}
      modifiers={itemRenderer.modifiers}
      handleClick={itemRenderer.handleClick}
      handleFocus={itemRenderer.handleFocus}
    />
  );
  return (
    <section className="filter-list__container filter-list-picker__container">
      <H6 className="filter-list__header">Filter List</H6>
      <FilterListSelect
        fill
        items={filterLists}
        itemPredicate={(query: string, item: FilterListTypes.FilterList) =>
          item.name.toLowerCase().includes(query.toLowerCase())
        }
        itemRenderer={filterListRenderer}
        noResults={<MenuItem disabled text="No results." />}
        onItemSelect={(item: FilterListTypes.FilterList) => {
          setFilterList(item);
        }}
        popoverProps={{ matchTargetWidth: true, minimal: true }}
        resetOnClose
        resetOnQuery
        resetOnSelect
      >
        <Button
          alignText={Alignment.LEFT}
          large
          fill
          rightIcon="double-caret-vertical"
          tabIndex={-1}
          loading={isLoading}
          disabled={filterLists == null}
          text={selectedFilterList?.name ?? 'Select a filter list'}
        />
      </FilterListSelect>
    </section>
  );
};
