import { Icon } from '@blueprintjs/core';
import classNames from 'classnames';
import React from 'react';

export interface FilterListEntryIconProps {
  isWithinHotKeyCycle: boolean;
  setIsFilterWithinHotkeyCycle: (isWithinCycle: boolean) => void;
}

/**
 * Creates an icon for the filter list entry. Either a filled in star if `isWithinHotkeyCycle`
 * is true, or an unfilled star otherwise. If unfiltered
 */
// eslint-disable-next-line react/function-component-definition
export const FilterListEntryHotkeyCycleButton: React.FC<FilterListEntryIconProps> = ({
  isWithinHotKeyCycle,
  setIsFilterWithinHotkeyCycle
}: FilterListEntryIconProps) => {
  const icon = isWithinHotKeyCycle ? 'star' : 'star-empty';
  return (
    <Icon
      className={classNames('filter-list-entry__icon', 'filter-list-entry__button', {
        'filter-list-entry__button--hidden': !isWithinHotKeyCycle
      })}
      icon={icon}
      onClick={e => {
        e.preventDefault();
        e.stopPropagation();
        setIsFilterWithinHotkeyCycle(!isWithinHotKeyCycle);
      }}
    />
  );
};
