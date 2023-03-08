/* eslint-disable react/destructuring-assignment */
/*
The FormBody iterates through the FormItems and renders them
*/

import React from 'react';

import type { FormItem, FormItemState } from '../types';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { ItemType } from '../types';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { FormDisplayText } from './form-item/form-display-text';
import { FormLabel } from './form-item/form-label';
import { FormValue } from './form-item/form-value';
/**
 * FormBody Props
 */
export interface FormBodyProps {
  labelFontSizeEm: number;
  formItems: FormItem[];
  hasHold?: boolean;
  maxLabelWidthEm: number;
  formItemStates: Map<string, FormItemState>;
  onHoldChange(valueKey: string, holdStatus: boolean);
  onValue(valueKey: string, payload: any);
}

/**
 * FormBody state
 */
export interface FormBodyState {
  dummy?: boolean;
  hasHold: boolean;
  value: any;
}

/**
 * FormBody component.
 */
export class FormBody extends React.Component<FormBodyProps, FormBodyState> {
  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    /*
     * So that the FormLabels are all the same rendered width, we do a tricky trick
     * The 'em' unit is the width of a capitol M, the widest character
     * So we set the width of each FormLabel to the width of the widest possible label
     * May need to be adjusted in the future with a better heuristic
     */
    const DEFAULT_VALUE_WIDTH_PX = 280;
    return (
      <div className="form-body">
        {this.props.formItems.map(item => (
          <React.Fragment key={item.itemKey}>
            <FormLabel
              key={`${item.itemKey}-Label`}
              fontSizeEm={this.props.labelFontSizeEm}
              text={item.labelText}
              hideColon={item.hideLabelColon}
              modified={this.isModified(item)}
              widthEm={this.props.maxLabelWidthEm}
            />
            {item.itemType === ItemType.Input && item.value !== undefined ? (
              <FormValue
                key={`${item.itemKey}-Value`}
                className={item.className}
                value={this.getValueForItemKey(item)}
                data-cy={item['data-cy']}
                itemKey={item.itemKey}
                onHoldChange={this.props.onHoldChange}
                onValue={this.props.onValue}
                widthPx={DEFAULT_VALUE_WIDTH_PX}
              />
            ) : (
              <FormDisplayText
                key={`${item.itemKey}-DisplayText`}
                className={item.className}
                displayText={item.displayText !== undefined ? item.displayText : ''}
                tooltip={item.tooltip ? item.tooltip : ''}
                widthPx={DEFAULT_VALUE_WIDTH_PX}
                formatAs={item.displayTextFormat}
              />
            )}
          </React.Fragment>
        ))}
      </div>
    );
  }

  private readonly getValueForItemKey = (item: FormItem) => {
    const formItemState = this.props.formItemStates.get(item.itemKey);
    if (formItemState !== undefined) {
      return formItemState.value ? formItemState.value : item.value;
    }
    return item.value;
  };

  private readonly isModified = (item: FormItem): boolean => {
    const itemState = this.props.formItemStates.get(item.itemKey);
    if (itemState) {
      return itemState.modified;
    }
    return false;
  };
}
