import { H6 } from '@blueprintjs/core';
import type { FilterListTypes } from '@gms/common-model';
import { nonIdealStateWithNoSpinner, WithNonIdealStates } from '@gms/ui-core-components';
import {
  analystActions,
  selectSelectedFilterIndex,
  useAppDispatch,
  useAppSelector,
  useHotkeyCycle,
  useLegibleColor,
  useSelectedFilter,
  useUiTheme
} from '@gms/ui-state';
import { blendColors, isHexColor } from '@gms/ui-util';
import * as React from 'react';

import { FilterEntry } from './filter-entry';
import { getFilterName } from './filter-list-util';

const COLOR_BLEND_FRACTION = 0.5;

const useSetIsFilterWithinHotkeyCycle = (setIsWithinHotkeyCycle: (i: number) => void) => {
  // A referentially stable set of callbacks
  const { current: handlerCallbacks } = React.useRef({});
  return React.useCallback(
    (index: number) => {
      if (!handlerCallbacks[index]) {
        handlerCallbacks[index] = setIsWithinHotkeyCycle(index);
      }
      return handlerCallbacks[index];
    },
    [setIsWithinHotkeyCycle, handlerCallbacks]
  );
};

/**
 * This higher order function dynamically creates a referentially stable list of functions,
 * so we can use them as props in memoized components.
 *
 * @param handleClick a handler that should be called with the index of the element clicked.
 * @returns a function that takes an index, and generates an on click handler with the index injected in.
 */
const useHandleClickFor = (handleClick: (i: number) => void) => {
  // A referentially stable set of callbacks
  const { current: handlerCallbacks } = React.useRef({});
  return React.useCallback(
    (index: number) => {
      if (!handlerCallbacks[index]) {
        handlerCallbacks[index] = () => handleClick(index);
      }
      return handlerCallbacks[index];
    },
    [handleClick, handlerCallbacks]
  );
};

/**
 * This creates a setFilterRef higher order function which creates referentially stable setters for our
 * children elements. This allows us to keep the logic for our indices scoped to one function, rather
 * than creating a dependency between parents and children.
 *
 * @returns
 * `setFilterRef`: a function that takes an index, and generates ref setter for that index;
 * `scrollIntoView`: a function that takes an index (position of the child) and scrolls that child element into view
 */
const useFilterRefs = () => {
  // an ref containing an array of all the list entry elements
  const filterRefs = React.useRef<HTMLLIElement[]>([]);
  // A referentially stable set of callbacks
  const { current: setRefCallbacks } = React.useRef({});
  /** A memoized higher order function to create ref setter functions that will be referentially stable */
  const setFilterRef = React.useCallback(
    (index: number) => {
      if (!setRefCallbacks[index]) {
        setRefCallbacks[index] = ref => {
          filterRefs.current[index] = ref;
        };
      }
      return setRefCallbacks[index];
    },
    [setRefCallbacks]
  );
  const scrollIntoView = React.useCallback((newI: number) => {
    if (filterRefs.current && filterRefs.current[newI]) {
      filterRefs.current[newI].scrollIntoView({
        behavior: 'smooth',
        block: 'center',
        inline: 'start'
      });
    }
    return newI;
  }, []);
  return { setFilterRef, scrollIntoView };
};

/**
 * @returns a color for the active element text that blends the selection color with gmsMain. The color
 * will have a guaranteed acceptable contrast against the gms background color.
 * @throws if the gmsBackground color or gmsSelection or gmsMain are not hex colors.
 */
const useActiveTextColor = () => {
  const [uiTheme] = useUiTheme();
  const blendedColor = blendColors(
    uiTheme.colors.gmsSelection,
    uiTheme.colors.gmsMain,
    COLOR_BLEND_FRACTION
  );
  if (isHexColor(uiTheme.colors.gmsBackground) && isHexColor(blendedColor)) {
    return useLegibleColor(uiTheme.colors.gmsBackground, blendedColor);
  }
  throw new Error(
    `Cannot call useLegibleColor hook with non-hex colors. ${uiTheme.colors.gmsBackground} or ${blendedColor} is not a hex color.`
  );
};

/**
 * @returns an object containing:
 * @param selectedFilterIndex the index of the filter that is selected (out of Redux)
 * @param setSelectedFilterIndex a setter function that sets the index of the selected filter in Redux.
 */
const useSelectedFilterIndex = () => {
  const dispatch = useAppDispatch();
  const selectedFilterIndex = useAppSelector(selectSelectedFilterIndex);
  const setSelectedFilterIndex = React.useCallback(
    (i: number) => {
      dispatch(analystActions.setSelectedFilterIndex(i));
    },
    [dispatch]
  );
  return { selectedFilterIndex, setSelectedFilterIndex };
};

/**
 *
 * @param selectedFilterIndex the index of the selected filter (the global state)
 * @param setActiveIndex the setter for the active filter (the local state)
 * @returns an object containing:
 * @param handleFocus a function that sets the active index to the default position (referentially stable)
 * @param handleBlur a function that sets the active index to null, clearing the local "active element" state (referentially stable)
 */
const useFocusHandlers = (
  selectedFilterIndex: number | null,
  setActiveIndex: React.Dispatch<React.SetStateAction<number>>
) => {
  const handleFocus = React.useCallback(() => {
    setActiveIndex(selectedFilterIndex);
  }, [selectedFilterIndex, setActiveIndex]);
  const handleBlur = React.useCallback(() => {
    setActiveIndex(null);
  }, [setActiveIndex]);
  return { handleFocus, handleBlur };
};

/**
 *
 * @param setActiveIndex to set the active filter index
 * @param scrollIntoView scrolls the filter at the provided index into view
 * @param handleBlur callback when losing focus
 * @param setSelectedFilterIndex setter for the filter index
 * @param filterList the active filter list
 * @param activeIndex the index of the element that is active (has keyboard focus)
 * @returns a function to call that handles keydown events
 */
export const buildHandleKeyDown = (
  setActiveIndex,
  scrollIntoView,
  handleBlur,
  setSelectedFilterIndex,
  filterList,
  activeIndex
) => e => {
  if (e.key === 'Escape') {
    e.preventDefault();
    e.stopPropagation();
    (document.activeElement as HTMLElement).blur();
    handleBlur();
  } else if (e.key === 'Enter') {
    e.preventDefault();
    e.stopPropagation();
    setSelectedFilterIndex(activeIndex);
  } else if (e.key === 'ArrowUp' || (e.key === 'Tab' && e.shiftKey)) {
    e.preventDefault();
    e.stopPropagation();
    setActiveIndex((i: number) => {
      const newI = i === 0 ? filterList.filters.length - 1 : i - 1;
      scrollIntoView(newI);
      return newI;
    });
  } else if (e.key === 'ArrowDown' || e.key === 'Tab') {
    e.preventDefault();
    e.stopPropagation();
    setActiveIndex((i: number) => {
      const newI = i === filterList.filters.length - 1 ? 0 : i + 1;
      scrollIntoView(newI);
      return newI;
    });
  }
};

/**
 * A useEffect call that scrolls the selected filter into view.
 *
 * @param scrollIntoView a function that scrolls the filter at the provided index into view
 * @param selectedFilterIndex the index of the selected filter
 */
const useScrollSelectedFilterInViewEffect = (
  scrollIntoView: (i: number) => void,
  selectedFilterIndex: number
) => {
  React.useEffect(() => {
    scrollIntoView(selectedFilterIndex);
  }, [scrollIntoView, selectedFilterIndex]);
};

/**
 * The type of the props for the {@link FilterList} component
 */
export interface FilterListProps {
  filterList: FilterListTypes.FilterList;
}

/**
 * Creates the interactive list of filters, which are keyboard and mouse selectable
 */
// eslint-disable-next-line react/function-component-definition
export const FilterListComponent: React.FC<FilterListProps> = ({ filterList }: FilterListProps) => {
  const activeTextColor = useActiveTextColor();
  // whether or not this has focus
  const [activeIndex, setActiveIndex] = React.useState<number | null>(null);
  const { setFilterRef, scrollIntoView } = useFilterRefs();
  const { setSelectedFilter } = useSelectedFilter();
  const { selectedFilterIndex, setSelectedFilterIndex } = useSelectedFilterIndex();
  useScrollSelectedFilterInViewEffect(scrollIntoView, selectedFilterIndex);
  const { handleFocus, handleBlur } = useFocusHandlers(selectedFilterIndex, setActiveIndex);
  const handleClickFor = useHandleClickFor(setActiveIndex);
  const { hotkeyCycle, setIsFilterWithinHotkeyCycle } = useHotkeyCycle();
  const buildSetterForFilterWithinHotkeyCycle = React.useCallback(
    (i: number) => (isWithinHotkeyCycle: boolean) => {
      setIsFilterWithinHotkeyCycle(i, isWithinHotkeyCycle);
    },
    [setIsFilterWithinHotkeyCycle]
  );
  const setFilterWithinHotkeyCycleFor = useSetIsFilterWithinHotkeyCycle(
    buildSetterForFilterWithinHotkeyCycle
  );
  const handleKeyDown = buildHandleKeyDown(
    setActiveIndex,
    scrollIntoView,
    handleBlur,
    setSelectedFilterIndex,
    filterList,
    activeIndex
  );

  return (
    <section
      className="filter-list__container filter-list__container--scrollable"
      style={{ '--active-text-color': activeTextColor } as React.CSSProperties}
      onFocusCapture={handleFocus}
      onBlur={handleBlur}
      role="menu"
      tabIndex={-1}
      onKeyDown={handleKeyDown}
    >
      <H6 className="filter-list__header">Filters</H6>
      <ol className="filter-list">
        {filterList.filters.map((filter, index) => (
          <FilterEntry
            key={getFilterName(filter)}
            setRef={setFilterRef(index)}
            isActive={index === activeIndex}
            filter={filter}
            handleClick={handleClickFor(index)}
            isWithinHotKeyCycle={hotkeyCycle[index]}
            setIsFilterWithinHotkeyCycle={setFilterWithinHotkeyCycleFor(index)}
            isSelected={selectedFilterIndex === index}
            setSelectedFilter={setSelectedFilter}
          />
        ))}
      </ol>
    </section>
  );
};

export const FilterListOrNonIdealState = WithNonIdealStates<FilterListProps>(
  [
    {
      condition: props => props.filterList == null,
      element: nonIdealStateWithNoSpinner('Filter List', 'No filter lists found')
    }
  ],
  FilterListComponent
);
