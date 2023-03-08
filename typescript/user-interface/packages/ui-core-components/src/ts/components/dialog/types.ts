export interface PromptProps {
  title: string;
  actionText: string;
  actionTooltipText: string;
  cancelText: string;
  cancelTooltipText: string;
  isOpen: boolean;
  // Optional parameters to create third button
  optionalButton?: boolean;
  optionalText?: string;
  optionalTooltipText?: string;
  actionDisabled?: boolean;
  actionCallback(): void;
  cancelButtonCallback(): void;
  onCloseCallback(): void;
  // Optional callback for optional button
  optionalCallback?(): void;
}

export interface SaveableItem {
  title: string;
  id: string;
}

export interface SaveOpenDialogState {
  isDialogOpen: boolean;
}

interface SharedDialogProps {
  title?: string;
  actionText?: string;
  itemList: SaveableItem[];
  isDialogOpen: boolean;
  actionTooltipText?: string;
  cancelText?: string;
  cancelTooltipText?: string;
  selectedId?: string;
  openedItemId?: string;
  defaultId: string;
  cancelCallback(): void;
}

export interface SaveOpenDialogProps {
  title: string;
  actionText: string;
  itemList: SaveableItem[];
  actionTooltipText: string;
  isDialogOpen: boolean;
  titleOfItemList?: string;
  cancelText?: string;
  cancelTooltipText?: string;
  selectedId?: string;
  openedItemId?: string;
  defaultId?: string;
  defaultSaveName?: string;
  actionCallback(): void;
  cancelCallback(): void;
  selectEntryCallback(id: string): void;
}

export interface SaveDialogProps extends SharedDialogProps {
  openedItemId?: string;
  defaultChecked?: boolean;
  defaultSaveName?: string;
  actionCallback(
    saveName: string,
    saveAsDefault: boolean,
    defaultId?: string,
    selectedId?: string
  ): void;
}
export interface OpenDialogProps extends SharedDialogProps {
  actionCallback(saveName: string): void;
}
