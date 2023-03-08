import kebabCase from 'lodash/kebabCase';
import React from 'react';

import type { LabelValueProps } from './types';

/**
 * Label value formatter
 *
 * @param props value label props
 * @returns a formatted value label container
 */
// eslint-disable-next-line react/function-component-definition
export const LabelValue: React.FunctionComponent<LabelValueProps> = (props: LabelValueProps) => {
  const defaultContainerClass = 'weavess-label-value-container';
  const labelClass = 'weavess-label-value__label';
  const valueClass = 'weavess-label-value__value';
  const { containerClass, label, tooltip, styleForValue, value, valueColor } = props;
  const customContainerClass = `${defaultContainerClass} ${containerClass || ''}`;
  const labelKebab = kebabCase(label);
  return (
    <div className={containerClass ? customContainerClass : defaultContainerClass}>
      <div className={labelClass} data-cy={`${labelKebab}-label`}>
        {label && label.length > 0 ? `${label}: ` : ''}
      </div>
      <div
        title={tooltip}
        className={valueClass}
        data-cy={`${labelKebab}-value`}
        style={{
          color: valueColor || '',
          ...styleForValue
        }}
      >
        {value}
      </div>
    </div>
  );
};
