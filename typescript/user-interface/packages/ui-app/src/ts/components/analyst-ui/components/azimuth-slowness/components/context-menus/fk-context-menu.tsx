import { Menu, MenuItem } from '@blueprintjs/core';
import React from 'react';

/**
 * functions which will review or clear fks
 */
export interface FkClearOrReview {
  (): void;
}
/**
 * Blueprint version of the context menu
 * Context menu for reviewing FK's
 *
 * @param clearAll Callback function to clear all selected FK
 * @param fksCanBeCleared True is some fks can be removed from the selection
 */
export function FkThumbnailBlueprintContextMenu(
  clearAll: FkClearOrReview,
  fksCanBeCleared: boolean
): JSX.Element {
  return (
    <Menu>
      <MenuItem text="Clear selected" onClick={clearAll} disabled={!fksCanBeCleared} />
    </Menu>
  );
}
