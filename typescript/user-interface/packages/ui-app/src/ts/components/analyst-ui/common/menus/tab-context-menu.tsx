import { Icon, Menu, MenuItem } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { toDisplayTitle } from '@gms/common-model/lib/displays/types';
import { UI_BASE_PATH, UI_URL } from '@gms/common-util';
import React from 'react';

import { ImperativeContextMenu } from './imperative-context-menu';

let tabMutationObserver: MutationObserver;
export interface TabContextMenuProps {
  tabName: string;
}
/**
 * Menu item designed to replace the imperative call from ContextMenu that was deprecated in ContextMenu2
 */
export function TabContextMenu(props: TabContextMenuProps) {
  const { tabName } = props;

  // Set up a callback so the imperative context menu can pass along the handler to open the menu
  const openCallback = (open: (event: MouseEvent) => void) => {
    // Read the golden layout title property to get the tab header
    const tab = document.querySelector(`[title="${toDisplayTitle(tabName)}"]`);

    const handleMutation = () => {
      // get the new tab reference
      const newTab = document.querySelector(`[title="${toDisplayTitle(tabName)}"]`);
      // readd the listener
      newTab.addEventListener('contextmenu', open);
      // Add a mouse down to reattached the observer due to a race condition
      newTab.addEventListener('mousedown', () => {
        const mousedownTab = document.querySelector(`[title="${toDisplayTitle(tabName)}"]`);
        tabMutationObserver.disconnect();
        tabMutationObserver = new MutationObserver(handleMutation);
        tabMutationObserver.observe(mousedownTab, { attributes: true });
      });
    };

    // Tab wont be found if the component is open in its own window
    if (tab) {
      tabMutationObserver = new MutationObserver(handleMutation);
      tab.addEventListener('contextmenu', open);
      tabMutationObserver.observe(tab, { attributes: true });
    }
  };

  return (
    <ImperativeContextMenu
      content={
        <Menu className="test-menu">
          <MenuItem
            text={`Open ${tabName} in new tab`}
            onClick={() => window.open(`${UI_URL}${UI_BASE_PATH}/#/${tabName}`)}
            labelElement={<Icon icon={IconNames.OPEN_APPLICATION} />}
          />
        </Menu>
      }
      getOpenCallback={openCallback}
    />
  );
}
