/* eslint-disable react/jsx-no-useless-fragment */
import { Tooltip2 } from '@blueprintjs/popover2';
import { WeavessTypes } from '@gms/weavess-core';
import React from 'react';

/**
 * The type of the props for the {@link DistanceAzimuth} component
 */
export interface DistanceAzimuthProps {
  /** Distance */
  distance: number;

  /** Distance units */
  distanceUnits: WeavessTypes.DistanceUnits;

  /** Azimuth */
  azimuth: number;
}

/**
 * Creates a distance/azimuth value for the label with wrapped tooltips and classes for
 * styling. Use km vs degree ('\u00B0') symbol depending on distanceUnits enum from props
 *
 */
// eslint-disable-next-line react/function-component-definition
export const InternalDistanceAzimuth: React.FC<DistanceAzimuthProps> = (
  props: DistanceAzimuthProps
) => {
  const { distanceUnits, distance, azimuth } = props;
  const disFixBy = distanceUnits === WeavessTypes.DistanceUnits.degrees ? 1 : 2;

  return (
    <>
      {distance !== 0 && (
        <Tooltip2 className="label-tooltip-wrapper__value" content={`Distance (${distanceUnits})`}>
          <>
            {distance.toFixed(disFixBy)}
            {distanceUnits === WeavessTypes.DistanceUnits.km ? ' km' : '\u00B0'}
          </>
        </Tooltip2>
      )}
      {azimuth !== 0 && (
        <>
          /
          <Tooltip2 className="label-tooltip-wrapper__value" content="Azimuth (degrees)">
            <>{`${azimuth.toFixed(1)}\u00B0`}</>
          </Tooltip2>
        </>
      )}
    </>
  );
};

export const DistanceAzimuth = React.memo(InternalDistanceAzimuth);
