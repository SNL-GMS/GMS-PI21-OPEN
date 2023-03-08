/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { Checkbox, Position, Tooltip } from '@blueprintjs/core';
import React from 'react';

import { messageConfig } from '~analyst-ui/config/message-config';

import type { MagnitudeDataForRow } from '../types';

/**
 * Universal tool tip renderer
 *
 * @param props to render the tool tip content and ag grid props
 */
// eslint-disable-next-line react/function-component-definition
export const ToolTipRenderer: React.FunctionComponent<any> = props => {
  const div = (
    <div
      style={{
        width: `${props.eParentOfValue.clientWidth}px`,
        height: `${props.eParentOfValue.clientHeight}px`
      }}
    >
      {props.valueFormatted}
    </div>
  );

  const children = props.children ? props.children : div;

  return props.tooltip ? (
    <Tooltip content={props.tooltip} position={Position.BOTTOM}>
      {children}
    </Tooltip>
  ) : (
    children
  );
};

// eslint-disable-next-line react/function-component-definition
export const MagDefiningCheckBoxCellRenderer: React.FunctionComponent<any> = props => {
  const magType = props.magnitudeType;
  const maybeDataForMag: MagnitudeDataForRow = props.data.dataForMagnitude.get(magType);
  const isChecked: boolean = maybeDataForMag ? maybeDataForMag.defining : false;
  const hasAmplitudeForMag = maybeDataForMag && maybeDataForMag.amplitudeValue !== undefined;
  const theCheckbox = (
    <Checkbox
      label=""
      checked={isChecked}
      disabled={
        props.data.historicalMode || props.data.rejectedOrUnnassociated || !hasAmplitudeForMag
      }
      onChange={() => {
        props.data.checkBoxCallback(
          props.magnitudeType,
          props.data.station,
          !maybeDataForMag.defining
        );
      }}
      data-cy={`mag-defining-checkbox-${magType}`}
      title={props.tooltip}
    />
  );

  return (
    <ToolTipRenderer
      // eslint-disable-next-line react/jsx-props-no-spreading
      {...props}
      tooltip={
        !hasAmplitudeForMag
          ? messageConfig.tooltipMessages.magnitude.noAmplitudeMessage
          : props.tooltip
      }
    >
      {theCheckbox}
    </ToolTipRenderer>
  );
};
