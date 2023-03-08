import uniq from 'lodash/uniq';

const inclusive = 1;
const getInclusiveIndex = (index: number) => index + inclusive;

export interface SelectableItem {
  id: string;
}
/**
 * @param selection the station names that are selected
 * @param stationSohList the station list to search
 * @param items
 * @returns the first and last indices of the stations passed in as selection
 */
export const getBoundingIndices = (selection: string[], items: SelectableItem[]): number[] => {
  let lastIndex = -1;
  let firstIndex = items.length;
  selection.forEach((id: string) => {
    const foundIndex = items.findIndex(status => status.id === id);
    lastIndex = Math.max(foundIndex, lastIndex);
    firstIndex = Math.min(foundIndex, firstIndex);
  });
  return [firstIndex, lastIndex];
};

/**
 * @param theIndex the index to check
 * @param theSelection the current selection
 * @param theStationSohList the list of stations
 * @param items
 */
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const isInSelection = (theIndex: number, theSelection: string[], items: SelectableItem[]) =>
  theSelection.reduce((didWeFindIt, sel) => didWeFindIt || items[theIndex].id === sel, false);

export const getSelectionFromClick = (
  e: React.MouseEvent,
  selection: string[],
  index: number,
  items: SelectableItem[]
): string[] => {
  if (e.metaKey || e.ctrlKey) {
    const isDeselectAction = isInSelection(index, selection, items);
    if (isDeselectAction) {
      const selectionIndex = selection.findIndex(s => items[index].id === s);
      return uniq([
        ...selection.slice(0, selectionIndex),
        ...selection.slice(getInclusiveIndex(selectionIndex), selection.length)
      ]);
    }
    return [...selection, items[index].id];
  }
  if (selection.length > 0 && e.shiftKey) {
    const [firstIndex, lastIndex] = getBoundingIndices(selection, items);
    if (index > lastIndex) {
      return uniq([
        ...selection,
        ...items.slice(lastIndex, getInclusiveIndex(index)).map(sohStatus => sohStatus.id)
      ]);
    }
    if (index < firstIndex) {
      return uniq([
        ...selection,
        ...items.slice(index, getInclusiveIndex(firstIndex)).map(sohStatus => sohStatus.id)
      ]);
    }
    if (index >= firstIndex && index <= lastIndex) {
      return uniq([
        ...items.slice(firstIndex, getInclusiveIndex(index)).map(sohStatus => sohStatus.id)
      ]);
    }
  }
  return [items[index].id];
};

/**
 * Updates the selection if this click would change the selection,
 *
 * @param e the mouse event that started it all
 * @param selection the list of stations currently selected
 * @param index the index of the station which was clicked
 * @param sohStations the list of stations
 * @param items
 */
export const getSelectionForAction = (
  e: React.MouseEvent,
  selection: string[],
  index: number,
  items: SelectableItem[]
): string[] => {
  if (isInSelection(index, selection, items)) {
    return selection;
  }
  return getSelectionFromClick(e, selection, index, items);
};
