/* eslint-disable react/destructuring-assignment */
import classNames from 'classnames';
import React from 'react';

// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { TextFormats } from '../../types';

/**
 * Props for form text display
 */
export interface FormDisplayTextProps {
  displayText: string;
  tooltip?: string;
  formatAs?: TextFormats;
  className?: string;
  widthPx: number;
}
/**
 * Displays text for Form
 */
export class FormDisplayText extends React.PureComponent<FormDisplayTextProps, unknown> {
  /**
   * Renders the component
   */
  public render(): JSX.Element {
    const className =
      this.props.formatAs === TextFormats.Time
        ? 'form-value form-value--uneditable form-value--time'
        : 'form-value form-value--uneditable';
    return (
      <div className={classNames(className, this.props.className)} title={this.props.tooltip}>
        {this.props.displayText}
      </div>
    );
  }
}
