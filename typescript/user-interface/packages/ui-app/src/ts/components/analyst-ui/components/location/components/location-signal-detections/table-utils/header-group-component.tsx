/* eslint-disable react/destructuring-assignment */
import { Alignment, Checkbox } from '@blueprintjs/core';
import React from 'react';

import { DefiningStates, DefiningTypes } from '../types';

/**
 * Renders the header for the various defining types
 */
export class DefiningHeader extends React.PureComponent<any> {
  public render(): JSX.Element {
    const { definingState } = this.props;
    const { definingType } = this.props;
    return (
      <div className="location-sd-header">
        <div>
          {/* eslint-disable-next-line no-nested-ternary */}
          {definingType === DefiningTypes.ARRIVAL_TIME
            ? 'Time'
            : definingType === DefiningTypes.AZIMUTH
            ? 'Azimuth (\u00B0)'
            : 'Slowness'}
        </div>
        <div className="location-sd-subdivider">
          <Checkbox
            label="Def All:"
            alignIndicator={Alignment.RIGHT}
            checked={definingState === DefiningStates.ALL}
            onClick={() => {
              this.props.definingCallback(true, definingType);
            }}
            className="location-sd-checkbox checkbox-horizontal"
          />
          <Checkbox
            label="None: "
            alignIndicator={Alignment.RIGHT}
            checked={definingState === DefiningStates.NONE}
            onClick={() => {
              this.props.definingCallback(false, definingType);
            }}
            className="location-sd-checkbox checkbox-horizontal"
          />
        </div>
      </div>
    );
  }
}
