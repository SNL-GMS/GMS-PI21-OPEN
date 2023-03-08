/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Icon, Intent, Position, Tooltip } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';

import { systemConfig } from '~analyst-ui/config';
import { messageConfig } from '~analyst-ui/config/message-config';

import { canPerformAction } from '../utils/history-utils';
import type { GenericHistoryEntryProps, HistoryEntryProps } from './types';
import { HistoryEntryAction } from './types';

export const makeMouseHandler = (
  handler: (evt: React.MouseEvent<HTMLDivElement, MouseEvent>) => void,
  focus?: boolean
) => e => {
  if (focus) {
    e.currentTarget.focus();
  }
  if (handler) {
    handler(e);
  }
};

export const makeKeyHandler = (
  handler: (evt: React.KeyboardEvent<HTMLDivElement>) => void,
  focus?: boolean
) => e => {
  if (focus) {
    e.currentTarget.focus();
  }
  if (handler) {
    handler(e);
  }
};

/**
 * Private function to create the history entry jsx inside of HistoryEntry elements and
 * HiddenHistoryEntry elements.
 *
 * @param props all props are optional
 */
// eslint-disable-next-line complexity, react/function-component-definition
export const GenericHistoryEntry: React.FunctionComponent<GenericHistoryEntryProps> = props => (
  // eslint-disable-next-line jsx-a11y/mouse-events-have-key-events, jsx-a11y/no-static-element-interactions
  <div
    className={`
      list__column history-entry
      history-entry--${props.entryType ? props.entryType.toLowerCase() : 'hidden'}
      ${props.isAssociated ? 'is-associated' : ''}
      ${props.isAffected ? 'is-affected' : ''}
      ${props.isCompleted ? 'is-completed' : ''}
      ${props.isOrphaned ? 'is-orphaned' : ''}
      ${props.isChild ? 'is-child' : ''}
      ${props.isEventReset ? 'is-event-reset' : ''}
    `}
    // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex
    tabIndex={0}
    onClick={e => {
      if (props.handleAction) {
        props.handleAction(e);
      }
    }}
    // eslint-disable-next-line @typescript-eslint/unbound-method
    onMouseOver={makeMouseHandler(props.handleMouseEnter, true)}
    // eslint-disable-next-line @typescript-eslint/unbound-method
    onMouseOut={makeMouseHandler(props.handleMouseOut)}
    // eslint-disable-next-line @typescript-eslint/unbound-method
    onKeyDown={makeKeyHandler(props.handleKeyDown)}
    // eslint-disable-next-line @typescript-eslint/unbound-method
    onKeyUp={makeKeyHandler(props.handleKeyUp)}
  >
    <span className="history-entry__description">{props.message ? props.message : '\u00A0'}</span>
    {props.isInConflict && ( // if we have an entryType, render the conflict icon
      <Tooltip
        content="This action created a conflict"
        hoverOpenDelay={systemConfig.interactionDelay.slow}
        position={Position.BOTTOM}
      >
        <Icon className="history-entry__icon" intent={Intent.DANGER} icon={IconNames.ISSUE} />
      </Tooltip>
    )}
    {props.entryType && ( // if we have an entryType, render the appropriate icon
      <Tooltip
        content={
          // eslint-disable-next-line no-nested-ternary
          props.entryType === HistoryEntryAction.undo
            ? props.isEventReset
              ? `${messageConfig.tooltipMessages.history.undoEventLevelActionMessage}`
              : `${messageConfig.tooltipMessages.history.undoActionMessage}`
            : props.isEventReset
            ? `${messageConfig.tooltipMessages.history.redoEventLevelActionMessage}`
            : `${messageConfig.tooltipMessages.history.redoActionMessage}`
        }
        hoverOpenDelay={systemConfig.interactionDelay.slow}
        position={Position.BOTTOM}
      >
        <Icon
          className="history-entry__icon"
          icon={
            // eslint-disable-next-line no-nested-ternary
            props.isChild
              ? undefined
              : // eslint-disable-next-line no-nested-ternary
              props.entryType === HistoryEntryAction.undo
              ? IconNames.UNDO
              : props.entryType === HistoryEntryAction.redo
              ? IconNames.REDO
              : undefined
          }
        />
      </Tooltip>
    )}
  </div>
);

/**
 * A HistoryEntry component - a clickable entry in the history stack list, which will
 * perform one or more undo or redo actions.
 *
 * @param props message, status effects and handlers.
 */
// eslint-disable-next-line react/function-component-definition
export const HistoryEntry: React.FunctionComponent<HistoryEntryProps> = props => {
  const entryType = canPerformAction(props.changes, HistoryEntryAction.undo)
    ? HistoryEntryAction.undo
    : HistoryEntryAction.redo;
  // eslint-disable-next-line react/jsx-props-no-spreading
  return <GenericHistoryEntry {...props} entryType={entryType} />;
};

/**
 * Displays entry as an empty space
 *
 * @param props any props the hidden history entry should have. All are optional
 */
// eslint-disable-next-line react/function-component-definition
export const HiddenHistoryEntry: React.FunctionComponent<GenericHistoryEntryProps> = props => (
  // eslint-disable-next-line react/jsx-props-no-spreading
  <GenericHistoryEntry {...props} />
);
