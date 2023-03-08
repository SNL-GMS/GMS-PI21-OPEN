import { Button } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { classList } from '@gms/ui-util';
import * as React from 'react';

import type { NewMessageIndicatorProps } from './types';

/**
 * A button that appears hovering over the table near the bottom. Clicking the button
 * scrolls to the latest entry in the table.
 */
export function NewMessageIndicator(props: NewMessageIndicatorProps) {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { isVisible, handleNewMessageIndicatorClick } = props;
  return isVisible ? (
    <Button
      className={classList({
        'system-message-table__button': true,
        'system-message-table__button--floating': true
      })}
      data-cy="new-messages-button"
      onClick={e => handleNewMessageIndicatorClick(e)}
      icon={IconNames.ARROW_DOWN}
      intent="primary"
    >
      New messages
    </Button>
  ) : null;
}
