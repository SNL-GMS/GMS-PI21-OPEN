/* eslint-disable react/destructuring-assignment */
import uniqueId from 'lodash/uniqueId';
import React from 'react';

import type { PercentBarProps } from './types/percent-bar';

/**
 * A simple component for rendering a percent bar.
 */
export class PercentBar extends React.PureComponent<PercentBarProps> {
  private readonly id: string;

  public constructor(props: PercentBarProps) {
    super(props);
    this.id = uniqueId();
  }

  public render(): JSX.Element {
    return (
      <div
        className={`percent-bar${this.props.classNames ? ' ' : ''}${
          this.props.classNames ? this.props.classNames : ''
        }`}
        key={this.id}
        style={{
          width: `100%`,
          // Parent has overflow hidden, which cuts off the overflow,
          // so translation will only show the appropriate amount of the bar.
          // eslint-disable-next-line @typescript-eslint/no-magic-numbers
          transform: `translateX(-${100 - (this.props.percentage || 0)}%)`
        }}
      />
    );
  }
}
