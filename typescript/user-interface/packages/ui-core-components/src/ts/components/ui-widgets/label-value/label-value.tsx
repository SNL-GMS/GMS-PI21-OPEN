/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import classNames from 'classnames';
import kebabCase from 'lodash/kebabCase';
import React from 'react';

import type { LabelValueProps } from './types';

// eslint-disable-next-line react/function-component-definition
export const LabelValue: React.FunctionComponent<LabelValueProps> = props => {
  const containerClass = props.ianApp ? 'ian-label-value-container' : 'label-value-container';
  const labelClass = props.ianApp ? 'ian-label-value__label' : 'label-value__label';
  const valueClass = props.ianApp ? 'ian-label-value__value' : 'label-value__value';
  const numericClass = props.numeric ? `${valueClass}--numeric` : null;
  const customContainerClass = `${containerClass} ${
    props.containerClass ? props.containerClass : ''
  }`;
  const labelKebab = kebabCase(props.label);
  return (
    <div className={props.containerClass ? customContainerClass : containerClass}>
      <div className={labelClass} data-cy={`${labelKebab}-label`}>
        {props.label && props.label.length > 0 ? `${props.label}: ` : ''}
      </div>
      <div
        title={props.tooltip}
        className={classNames(valueClass, numericClass)}
        data-cy={`${labelKebab}-value`}
        style={{
          color: props.valueColor ? props.valueColor : '',
          ...props.styleForValue
        }}
      >
        {props.value}
      </div>
    </div>
  );
};
