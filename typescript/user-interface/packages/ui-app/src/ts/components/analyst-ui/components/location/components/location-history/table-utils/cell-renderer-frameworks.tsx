/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { Switch } from '@blueprintjs/core';
import React from 'react';
/**
 * stateless checkbox used in the LocationHistory table
 */
// TODO move to core components
// eslint-disable-next-line react/function-component-definition
export const LocationHistoryCheckBox: React.FunctionComponent<any> = props => (
  <Switch
    checked={props.data.preferred}
    disabled={props.data.latestLSSId !== props.data.locationSetId}
    onChange={() => {
      props.data.setSelectedPreferredLocationSolution(
        props.data.locationSetId,
        props.data.locationSolutionId
      );
    }}
  />
);

// eslint-disable-next-line react/function-component-definition
export const LocationSetSwitch: React.FunctionComponent<any> = props =>
  props.data.isFirstInLSSet ? (
    <Switch
      data-cy="location-set-to-save-switch"
      checked={props.data.isLocationSolutionSetPreferred}
      large
      disabled={props.data.latestLSSId !== props.data.locationSetId}
      onChange={() => {
        props.data.setToSave(props.data.locationSetId, props.data.locationSolutionId);
      }}
    />
  ) : null;
