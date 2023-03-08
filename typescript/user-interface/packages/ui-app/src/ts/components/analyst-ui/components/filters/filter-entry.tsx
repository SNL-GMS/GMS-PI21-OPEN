import { Icon } from '@blueprintjs/core';
import { Tooltip2 } from '@blueprintjs/popover2';
import type { FilterListTypes } from '@gms/common-model';
import classNames from 'classnames';
import React from 'react';

import { FilterListEntryHotkeyCycleButton } from './filter-list-icon';
import { getFilterName } from './filter-list-util';
import { FilterTooltipContent } from './filter-tooltip-content';

export interface FilterEntryProps {
  filter: FilterListTypes.Filter;
  isActive: boolean;
  isSelected: boolean;
  isWithinHotKeyCycle: boolean;
  handleClick: () => void;
  setIsFilterWithinHotkeyCycle: (isWithinCycle: boolean) => void;
  setSelectedFilter: (filter: FilterListTypes.Filter) => void;
  setRef: (ref) => void;
}

// eslint-disable-next-line react/function-component-definition
const InternalFilterEntry: React.FC<FilterEntryProps> = ({
  filter,
  isActive,
  isSelected,
  isWithinHotKeyCycle,
  handleClick,
  setIsFilterWithinHotkeyCycle,
  setSelectedFilter,
  setRef
}: FilterEntryProps) => {
  const filterName = getFilterName(filter);
  const [isTooltipOpen, setIsTooltipOpen] = React.useState(false);
  return (
    // eslint-disable-next-line jsx-a11y/click-events-have-key-events
    <li
      ref={setRef}
      tabIndex={-1}
      // eslint-disable-next-line jsx-a11y/no-noninteractive-element-to-interactive-role
      role="button"
      key={filterName}
      className={classNames({
        'filter-list-entry': true,
        'filter-list-entry--selected': isSelected,
        'filter-list-entry--active': isActive
      })}
      onClick={e => {
        e.preventDefault();
        e.stopPropagation();
        setSelectedFilter(filter);
        handleClick();
      }}
    >
      <FilterListEntryHotkeyCycleButton
        isWithinHotKeyCycle={isWithinHotKeyCycle}
        setIsFilterWithinHotkeyCycle={setIsFilterWithinHotkeyCycle}
      />
      <Tooltip2
        className="filter-list-entry__tooltip-wrapper"
        canEscapeKeyClose
        content={filterName}
        hoverOpenDelay={450}
        minimal
        position="auto-start"
        isOpen={isTooltipOpen}
        onInteraction={() => setIsTooltipOpen(true)}
        onClose={() => setIsTooltipOpen(false)}
      >
        <div className="filter-list-entry__title">
          {filterName}
          <Tooltip2
            className={classNames('filter-list-entry__info', {
              'filter-list-entry__icon': true
            })}
            canEscapeKeyClose
            content={<FilterTooltipContent name={filterName} filter={filter} />}
            hoverOpenDelay={500}
            minimal
            onOpening={() => setIsTooltipOpen(false)}
            position="auto-start"
          >
            <Icon icon="info-sign" />
          </Tooltip2>
        </div>
      </Tooltip2>
    </li>
  );
};

export const FilterEntry = React.memo(InternalFilterEntry);
