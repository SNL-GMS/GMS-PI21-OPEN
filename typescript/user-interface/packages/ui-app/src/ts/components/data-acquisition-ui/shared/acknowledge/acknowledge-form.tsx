/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { Form, FormTypes, WidgetTypes } from '@gms/ui-core-components';
import * as React from 'react';

import type { WithAcknowledgeProps } from './types';

export interface AcknowledgeFormProps extends WithAcknowledgeProps {
  /**
   * String containing one or more classnames to add to the form, separated by whitespace
   */
  classNames?: string;

  /**
   * Whether to require that the user modify the form. True will disable the submit button
   * until the form has been modified.
   */
  requiresModificationForSubmit?: boolean;

  /**
   * A list of station names to acknowledge
   */
  stationNames: string[];

  /**
   * A function to call when the form is closed, either on submit or cancel.
   */
  onClose(): void;
}

// max number of characters for the text area
const MAX_ACK_COMMENT_CHAR = 1024;

/**
 * Creates the acknowledge dialog form for acknowledging with a comment.
 */
export function AcknowledgeForm(props: AcknowledgeFormProps) {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { stationNames, onClose, acknowledgeStationsByName } = props;
  if (!stationNames || !stationNames.length) {
    return null;
  }
  const formItems: FormTypes.FormItem[] = [];
  formItems.push({
    itemKey: 'stationLabel',
    labelText: stationNames.length === 0 ? `Station` : `Stations`,
    itemType: FormTypes.ItemType.Display,
    displayText:
      stationNames.length > 4
        ? `${stationNames.slice(0, 4).join(', ')}...`
        : stationNames.join(', '),
    tooltip: stationNames.join(', ')
  });

  formItems.push({
    itemKey: 'comment',
    labelText: 'Comment',
    itemType: FormTypes.ItemType.Input,
    topAlign: true,
    'data-cy': 'acknowledge-comment',
    value: {
      params: {
        tooltip: 'Enter the comment',
        maxChar: MAX_ACK_COMMENT_CHAR
      },
      defaultValue: ``,
      type: WidgetTypes.WidgetInputType.TextArea
    }
  });

  const acknowledgePanel: FormTypes.FormPanel = {
    formItems,
    name: 'AcknowledgeWithComment'
  };

  return (
    <div className={`acknowledge-form ${props.classNames}`}>
      <Form
        header="Acknowledge"
        defaultPanel={acknowledgePanel}
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        onSubmit={(data: any) => {
          acknowledgeStationsByName(stationNames, data.comment);
          onClose();
        }}
        onCancel={onClose}
        submitButtonText="Acknowledge"
        requiresModificationForSubmit={props.requiresModificationForSubmit ?? true}
      />
    </div>
  );
}
