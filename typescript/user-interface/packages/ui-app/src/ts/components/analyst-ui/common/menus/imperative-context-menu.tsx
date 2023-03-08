import type { ContextMenu2ContentProps } from '@blueprintjs/popover2';
import { ContextMenu2 } from '@blueprintjs/popover2';
import React from 'react';

export interface ImperativeContextMenuProps {
  content: JSX.Element | ((props: ContextMenu2ContentProps) => JSX.Element);
  getOpenCallback: (open: (event: MouseEvent) => void) => void;
}
/**
 * Menu item designed to replace the imperative call from ContextMenu that was deprecated in ContextMenu2
 */
export function ImperativeContextMenu(props: ImperativeContextMenuProps) {
  const { content, getOpenCallback } = props;
  const menuChildRef = React.useRef<HTMLDivElement>();

  const open = (event: MouseEvent) => {
    event.preventDefault();

    const newEvent: MouseEvent = new MouseEvent(event.type, event);
    menuChildRef.current.dispatchEvent(newEvent);
  };

  // on mount use effect
  // pass the open function back to the parent prop so that it can be called to open the menu
  React.useEffect(
    () => {
      getOpenCallback(open);
    },
    // We only want this to run onMount so we need no dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  return (
    <ContextMenu2 content={content}>
      <div ref={menuChildRef} />
    </ContextMenu2>
  );
}
