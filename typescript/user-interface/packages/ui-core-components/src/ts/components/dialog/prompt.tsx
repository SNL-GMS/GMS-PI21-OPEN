/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Button, Dialog, Intent } from '@blueprintjs/core';
import React from 'react';

import type { PromptProps } from './types';
/**
 * Prompts the user with a modal dialog. Accepts passed in children for the contents
 * of the dialog.
 */
// eslint-disable-next-line react/function-component-definition
export const ModalPrompt: React.FunctionComponent<React.PropsWithChildren<PromptProps>> = props => (
  <Dialog
    className="dialog_parent dialog_parent--wide"
    isOpen={props.isOpen}
    onClose={() => {
      props.onCloseCallback();
    }}
    title={props.title}
  >
    <div className="dialog dialog__container">
      {props.children}
      <div className="dialog__controls">
        <div className="dialog-actions">
          <Button
            text={props.actionText}
            data-cy="modal-action-button"
            title={props.actionTooltipText}
            intent={Intent.PRIMARY}
            onClick={() => props.actionCallback()}
            disabled={props.actionDisabled}
          />
          {props.optionalButton ? (
            <Button
              text={props.optionalText ? props.optionalText : 'optional'}
              data-cy="modal-optional-button"
              title={props.optionalTooltipText}
              onClick={() => (props.optionalCallback ? props.optionalCallback() : undefined)}
            />
          ) : undefined}
        </div>
        <Button
          text={props.cancelText ? props.cancelText : 'Cancel'}
          data-cy="modal-cancel-button"
          title={props.cancelTooltipText}
          onClick={() => props.cancelButtonCallback()}
        />
      </div>
    </div>
  </Dialog>
);
