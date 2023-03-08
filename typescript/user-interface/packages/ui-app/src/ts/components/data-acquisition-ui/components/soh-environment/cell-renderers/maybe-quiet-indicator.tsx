/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import * as React from 'react';

import { QuietIndicator } from '~components/data-acquisition-ui/shared/quiet-indicator';

import type { EnvironmentalSoh } from '../types';
import type { QuietIndicatorWrapperProps } from './types';

const quietTimingInfoIsDefined = (quietData: EnvironmentalSoh) =>
  quietData &&
  quietData.quietTimingInfo &&
  quietData.quietTimingInfo.quietUntilMs &&
  quietData.quietTimingInfo.quietDurationMs;

/**
 * Renders a quiet indicator if the quietTimingInfo is all there.
 * Renders nothing if no quietUntilMs timestamp has been provided.
 */
// eslint-disable-next-line react/function-component-definition
export const MaybeQuietIndicator: React.FunctionComponent<QuietIndicatorWrapperProps> = props => {
  if (quietTimingInfoIsDefined(props.data)) {
    return (
      <QuietIndicator
        style={{ diameterPx: props.diameterPx }}
        status={props.data?.status?.toLowerCase()}
        quietTimingInfo={props.data.quietTimingInfo}
        className={props.className}
      />
    );
  }
  return null;
};
