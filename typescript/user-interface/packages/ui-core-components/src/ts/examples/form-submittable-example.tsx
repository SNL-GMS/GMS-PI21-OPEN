/**
 * Example of using the form that actually accepts input
 */

import React from 'react';

import { Form, FormTypes, WidgetTypes } from '../components';

enum ExampleEnum {
  ExampleValue1 = 'Example Value One',
  ExampleValue2 = 'Example Value Two',
  ExampleValue3 = 'Example Value Three'
}
/**
 * Example displaying how to use the Table component.
 */
export class FormSubmittableExample extends React.Component<unknown, unknown> {
  /**
   * React render method
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const mainPanel: FormTypes.FormPanel = {
      formItems: this.makePetItems(),
      name: 'Form Example'
    };

    return (
      <div
        className="ag-dark"
        style={{
          flex: '1 1 auto',
          position: 'relative',
          width: '700px'
        }}
      >
        <Form
          header="Document Builder"
          defaultPanel={mainPanel}
          submitButtonText="Submit"
          requiresModificationForSubmit
          onCancel={() => {
            // eslint-disable-next-line no-alert
            alert('Form Canceled');
          }}
          // Important! If submit isn't disabled then you have to provide a callback
          onSubmit={this.onSubmit}
        />
      </div>
    );
  }

  // eslint-disable-next-line class-methods-use-this
  private readonly onSubmit = (returnedValues: any) => {
    const { name } = returnedValues;
    const { expiration } = returnedValues;
    const { details } = returnedValues;
    const alertString = `Name: ${name} \n Expiration Date: ${String(
      expiration
    )} \n Details: ${details}`;
    // eslint-disable-next-line no-alert
    alert(alertString);
  };

  // eslint-disable-next-line class-methods-use-this
  private makePetItems(): FormTypes.FormItem[] {
    const name: FormTypes.FormItem = {
      itemKey: 'name',
      labelText: 'Name',
      itemType: FormTypes.ItemType.Input,
      value: {
        defaultValue: '',
        type: WidgetTypes.WidgetInputType.TextArea
      }
    };
    const date: FormTypes.FormItem = {
      itemKey: 'expiration',
      labelText: 'Expiration Date',
      itemType: FormTypes.ItemType.Input,
      value: {
        // eslint-disable-next-line @typescript-eslint/no-magic-numbers
        defaultValue: new Date(1535760000000),
        type: WidgetTypes.WidgetInputType.TimePicker
      }
    };
    const details: FormTypes.FormItem = {
      itemKey: 'details',
      labelText: 'Details',
      itemType: FormTypes.ItemType.Input,
      value: {
        params: {
          dropDownItems: ExampleEnum
        },
        defaultValue: ExampleEnum.ExampleValue1,
        type: WidgetTypes.WidgetInputType.DropDown
      }
    };
    return [name, date, details];
  }
}
