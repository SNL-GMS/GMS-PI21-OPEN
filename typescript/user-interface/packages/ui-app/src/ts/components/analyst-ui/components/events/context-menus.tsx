import { Menu, MenuItem } from '@blueprintjs/core';
import React from 'react';

import { showMapEventDetailsPopover } from '../map/map-event-details';

export interface EventContextMenuProps {
  readonly selectedEventId: string;
  readonly isOpen: boolean;
  readonly includeEventDetailsMenuItem: boolean;
  readonly entityProperties?: unknown;
  readonly mousePosition?: { x: number; y: number };
  readonly openCallback: (eventId: string) => void;
  readonly closeCallback: (eventId: string) => void;
  readonly updateVisibleStationsForCloseEvent?: () => void;
  readonly setEventIdCallback?: (eventId: string) => void;
}

/**
 * Component that renders the interval context menu.
 */
export function EventContextMenu(props: EventContextMenuProps) {
  const {
    selectedEventId,
    isOpen,
    includeEventDetailsMenuItem,
    entityProperties,
    mousePosition,
    openCallback,
    closeCallback,
    setEventIdCallback,
    updateVisibleStationsForCloseEvent
  } = props;

  return (
    <Menu>
      <MenuItem
        className="menu-item-open-event"
        data-cy="menu-item-open-event"
        text="Open event"
        disabled={isOpen}
        onClick={() => openCallback(selectedEventId)}
      />
      <MenuItem
        className="menu-item-close-event"
        data-cy="menu-item-close-event"
        text="Close event"
        disabled={!isOpen}
        onClick={() => {
          if (setEventIdCallback) setEventIdCallback(undefined);
          if (updateVisibleStationsForCloseEvent) updateVisibleStationsForCloseEvent();
          closeCallback(selectedEventId);
        }}
      />
      {includeEventDetailsMenuItem ? (
        <MenuItem
          className="menu-item-event-details"
          text="Open event details"
          label="(Alt + click)"
          shouldDismissPopover={false}
          onClick={() =>
            showMapEventDetailsPopover(entityProperties, mousePosition.x, mousePosition.y)
          }
        />
      ) : undefined}
    </Menu>
  );
}
