/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Button, Dialog, Intent } from '@blueprintjs/core';
import React from 'react';

import type { SaveOpenDialogProps } from './types';

/**
 * Internal functional component used to render most of the save/open dialog
 * Accepts children to be rendered at the end of the dialog.
 */
export const SaveOpenDialog: React.FunctionComponent<React.PropsWithChildren<
  SaveOpenDialogProps
  // eslint-disable-next-line react/function-component-definition
>> = props => {
  const maybeDefaultItem = props.defaultId
    ? props.itemList.find(item => item.id === props.defaultId)
    : undefined;
  const items = maybeDefaultItem
    ? props.itemList.filter(item => item.id !== props.defaultId)
    : props.itemList;
  return (
    <Dialog
      isOpen={props.isDialogOpen}
      onClose={() => {
        props.cancelCallback();
      }}
      title={props.title}
    >
      <div className="dialog dialog__container dialog--save-open">
        {props.titleOfItemList ? props.titleOfItemList : null}
        <ul className="dialog__entries">
          {maybeDefaultItem ? (
            // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-noninteractive-element-interactions
            <li
              key={maybeDefaultItem.id}
              className={`dialog__entry default ${
                props.selectedId === maybeDefaultItem.id ? 'selected' : ''
              }`}
              onClick={() => {
                props.selectEntryCallback(maybeDefaultItem.id);
              }}
            >
              {maybeDefaultItem.title}
              <span key={`defaultId${maybeDefaultItem.id}`} className="dialog__entry--faded">
                (default)
              </span>
            </li>
          ) : null}
          {items.map(item => (
            // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-noninteractive-element-interactions
            <li
              key={item.id}
              className={`dialog__entry ${props.selectedId === item.id ? 'selected' : ''}`}
              onClick={() => {
                props.selectEntryCallback(item.id);
              }}
            >
              {item.title}
              {props.defaultId === item.id ? (
                <span key={`defaultId${item.id}`} className="dialog__entry--faded">
                  (default)
                </span>
              ) : (
                ''
              )}
            </li>
          ))}
        </ul>
        {props.children}
        <div className="dialog__controls">
          <Button
            text={props.actionText}
            title={props.actionTooltipText}
            intent={Intent.PRIMARY}
            onClick={() => props.actionCallback()}
          />
          <Button
            text={props.cancelText ? props.cancelText : 'Cancel'}
            title={props.cancelTooltipText}
            onClick={() => props.cancelCallback()}
          />
        </div>
      </div>
    </Dialog>
  );
};
