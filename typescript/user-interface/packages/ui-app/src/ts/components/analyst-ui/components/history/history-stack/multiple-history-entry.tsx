/* eslint-disable react/prop-types */
/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable react/destructuring-assignment */
import { Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';

import { GenericHistoryEntry, HistoryEntry } from './history-entry';
import type {
  HistoryEntryDisplayFlags,
  HistoryEntryProps,
  MultipleHistoryEntryProps
} from './types';
import { HistoryEntryAction } from './types';

/**
 * MultipleHistoryEntry creates a GenericHistoryEntry wrapping child changes.
 */
// eslint-disable-next-line react/function-component-definition
export const MultipleHistoryEntry: React.FunctionComponent<MultipleHistoryEntryProps> = props => {
  const [expanded, toggleExpanded] = React.useState(false);
  const parentFlags = props.historyEntryChildren.reduce(
    (accumFlags: HistoryEntryDisplayFlags, child: HistoryEntryProps) => ({
      isAffected: accumFlags.isAffected || child.isAffected,
      isAssociated: accumFlags.isAssociated || child.isAssociated,
      isInConflict: accumFlags.isInConflict || child.isInConflict,
      isOrphaned: accumFlags.isOrphaned || child.isOrphaned,
      isEventReset: accumFlags.isEventReset || child.isEventReset,
      entryType:
        accumFlags.entryType === HistoryEntryAction.undo ||
        child.entryType === HistoryEntryAction.undo
          ? HistoryEntryAction.undo
          : HistoryEntryAction.redo
      // eslint-disable-next-line @typescript-eslint/indent
    }),
    {
      isAffected: false,
      isAssociated: false,
      isInConflict: false,
      isOrphaned: false,
      isEventReset: false,
      entryType: HistoryEntryAction.redo
    }
  );
  return (
    <div className="history-row--multi">
      <HistoryEntry
        message={props.description}
        key={`Multi:${props.id}`}
        {...parentFlags}
        {...props}
      />
      <div className={`history-row__child-container ${expanded ? 'is-expanded' : ''}`}>
        {expanded
          ? props.historyEntryChildren.map((child, index) => (
              // eslint-disable-next-line react/no-array-index-key
              <div key={index}>
                <div />
                <GenericHistoryEntry
                  message={child.message}
                  // eslint-disable-next-line react/no-array-index-key
                  key={`Child={true}:  ${props.id}-${index}`}
                  isChild
                  {...child}
                />
              </div>
            ))
          : ''}
      </div>
      {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
      <div
        className={`toggle-button
          toggle-button--${parentFlags.entryType ? parentFlags.entryType.toLowerCase() : 'hidden'}`}
        onClick={() => {
          toggleExpanded(!expanded);
        }}
      >
        <Icon className={`${expanded ? 'is-inverted' : ''}`} icon={IconNames.CHEVRON_DOWN} />
      </div>
    </div>
  );
};
