import { MenuItem } from '@blueprintjs/core';
import type { IItemModifiers } from '@blueprintjs/select';
import type { FilterListTypes } from '@gms/common-model';
import { selectSelectedFilterListName, useAppSelector } from '@gms/ui-state';
import classNames from 'classnames';
import React from 'react';

interface FilterListEntryProps {
  filterList: FilterListTypes.FilterList;
  handleFocus: () => void;
  modifiers: IItemModifiers;
  handleClick: React.MouseEventHandler<HTMLElement>;
}

/**
 * Creates a single filter list entry that, when clicked, or when Enter is pressed while it is focused,
 * will set itself as the filter list.
 */
// eslint-disable-next-line react/function-component-definition
const InternalFilterListPickerEntry: React.FC<FilterListEntryProps> = ({
  filterList,
  handleClick,
  handleFocus,
  modifiers
}: FilterListEntryProps) => {
  const selectedFilterList = useAppSelector(selectSelectedFilterListName);

  return (
    <MenuItem
      className={classNames(
        'filter-list-picker__option',
        {
          'filter-list-picker__option--selected': selectedFilterList === filterList.name
        },
        {
          'filter-list-picker__option--active': modifiers.active
        },
        {
          'filter-list-picker__option--disabled': modifiers.disabled
        }
      )}
      active={modifiers.active}
      disabled={modifiers.disabled}
      selected={selectedFilterList === filterList.name}
      key={filterList.name}
      onClick={handleClick}
      onFocus={handleFocus}
      text={filterList.name}
    />
  );
};

export const FilterListPickerEntry = React.memo(InternalFilterListPickerEntry);
