/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import React from 'react';

export interface HistoryStackRowProps {
  isFirstRow: boolean;
  undoTarget: boolean;
  redoTarget: boolean;
  areUndoRedoAdjacent: boolean;
  areUndoRedoJoined: boolean;
}

export const HistoryStackRow: React.FunctionComponent<React.PropsWithChildren<
  HistoryStackRowProps
  // eslint-disable-next-line react/function-component-definition
>> = props => (
  <li
    className={`list__row entry-row
      ${props.undoTarget ? 'action-indicator-bottom ' : ''}
      ${
        props.redoTarget && (!props.areUndoRedoAdjacent || props.isFirstRow)
          ? 'action-indicator-top '
          : ''
      }`}
  >
    <div
      className={`list__column list__column--meta-container
        ${props.redoTarget ? 'bottom' : ''}
        ${props.redoTarget && props.areUndoRedoAdjacent ? 'move-up' : ''}
        ${props.areUndoRedoJoined ? 'mixed' : ''}`}
    >
      <span className="action-indicator__text">{props.undoTarget ? 'UNDO' : ''}</span>
      <span className="action-indicator__text">{props.redoTarget ? 'REDO' : ''}</span>
    </div>
    {props.children}
  </li>
);
