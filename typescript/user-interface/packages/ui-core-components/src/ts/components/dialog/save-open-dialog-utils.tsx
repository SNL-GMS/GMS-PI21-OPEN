import type { OpenDialogProps, SaveableItem, SaveDialogProps, SaveOpenDialogProps } from './types';

/**
 * Checks if a save/open dialog has saveable items
 */
export const hasValidSavableItems = (
  props: OpenDialogProps | SaveDialogProps | SaveOpenDialogProps
): boolean => (props.itemList ? props.itemList.length > 0 : false);

/**
 * Generates the state from a user selection in save/open
 *
 * @param props props of save/open dialog
 * @param selectedId Id of selected item
 */
export const getNewStateFromUserSelection = (
  props: OpenDialogProps | SaveDialogProps | SaveOpenDialogProps,
  selectedId: string
): { selectedId: string; saveName: string } => {
  const maybeItem = hasValidSavableItems(props)
    ? props.itemList.find(item => item.id === selectedId)
    : undefined;
  return {
    selectedId,
    saveName: maybeItem ? maybeItem.title : ''
  };
};

/**
 * Generates an "untitled" object if there is no default filename
 *
 * @param defaultSaveName default file name for empty item list
 */
export const generateEmptySaveableItemList = (
  defaultSaveName?: string
): {
  title: string;
  id: string;
}[] => [
  {
    title: defaultSaveName || 'Untitled',
    id: '-1'
  }
];

/**
 * Generates a default list of saveable items if the provided list
 *
 * @param itemList list of saveable items, may be empty
 * @param defaultSaveName default file name for empty item list
 */
export const generateSaveableItemList = (
  itemList: SaveableItem[],
  defaultSaveName?: string
): SaveableItem[] =>
  itemList.length > 0 ? itemList : generateEmptySaveableItemList(defaultSaveName);
