/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Alignment, Checkbox, Position, Tooltip } from '@blueprintjs/core';
import React from 'react';

import { systemConfig } from '~analyst-ui/config';
import { messageConfig } from '~analyst-ui/config/message-config';

import { MagDefiningStates } from '../../../types';

/**
 * Renders the header for the various defining types
 */
// eslint-disable-next-line react/function-component-definition
export const DefiningHeader: React.FunctionComponent<any> = props => (
  <div className="location-sd-header">
    <div>{systemConfig.magnitudeTypeToDisplayName.get(props.magnitudeType)}</div>
    <div className="location-sd-subdivider">
      <Tooltip
        content={messageConfig.tooltipMessages.magnitude.setAllStationsDefiningMessage}
        position={Position.BOTTOM}
      >
        <Checkbox
          label="Def All:"
          data-cy={`defining-all-${props.magnitudeType}`}
          alignIndicator={Alignment.RIGHT}
          checked={props.definingState === MagDefiningStates.ALL}
          onChange={() => {
            if (props.definingState !== MagDefiningStates.ALL) {
              props.callback(props.magnitudeType, props.stationIds, true);
            }
          }}
          disabled={props.stationIds.length < 1}
          className="location-sd-checkbox checkbox-horizontal"
        />
      </Tooltip>

      <Tooltip
        content={
          props.definingState === MagDefiningStates.NONE
            ? messageConfig.tooltipMessages.magnitude.noStationsSetToDefiningMessage
            : messageConfig.tooltipMessages.magnitude.setAllStationsNotDefiningMessage
        }
        position={Position.BOTTOM}
      >
        <Checkbox
          label="None:"
          data-cy={`defining-none-${props.magnitudeType}`}
          alignIndicator={Alignment.RIGHT}
          checked={props.definingState === MagDefiningStates.NONE}
          onChange={() => {
            if (props.definingState !== MagDefiningStates.NONE) {
              props.callback(props.magnitudeType, props.stationIds, false);
            }
          }}
          disabled={props.stationIds.length < 1}
          className={`location-sd-checkbox checkbox-horizontal ${
            props.definingState === MagDefiningStates.NONE ? 'checkbox-warning' : ''
          }`}
        />
      </Tooltip>
    </div>
  </div>
);
