import { TextField } from '@mui/material';
import { HelpTextRenderer } from '../../components/HelpTextRenderer';
import {
  isMarginalGreaterThanOrEqualToGood,
  isPercentOrDurationError,
} from '../../util/util';
import { determineThresholdValue } from './util';
import { layoutStyles } from '../../styles/layout';
import { makeStyles } from '@mui/styles';
import { ThresholdsMap } from '../../coi-types/monitor-types';
import { useUpdateErrorState } from '../../util/custom-hooks';
import React from 'react';

const useStyles = makeStyles({
  ...layoutStyles,
  thresholdInputs: {
    '&:focus-within input': {
      background: 'white',
    },
  },
  spacer: {
    width: '1em',
    height: '100%',
  },
});

export interface ThresholdsProps {
  isLoading: boolean;
  name: string;
  thresholdsMap: ThresholdsMap;
  isDuration: boolean;
  helpTextGood?: string;
  helpTextMarginal?: string;
  parentName?: string;
  updateGoodThreshold: (name: string, value: string) => void;
  updateMarginalThreshold: (name: string, value: string) => void;
}

/**
 * ThresholdFields component used to show and update thresholds text fields
 * @returns thresholds text fields
 */
export const Thresholds: React.FC<ThresholdsProps> = ({
  isLoading,
  name,
  thresholdsMap,
  isDuration,
  helpTextGood,
  helpTextMarginal,
  parentName,
  updateGoodThreshold,
  updateMarginalThreshold,
}) => {
  const classes = useStyles();
  const [updateErrorState] = useUpdateErrorState();
  const marginalValue = determineThresholdValue(
    thresholdsMap,
    name
  ).marginalThreshold.toString();
  const goodValue = determineThresholdValue(
    thresholdsMap,
    name
  ).goodThreshold.toString();
  const isMarginalThresholdError = isPercentOrDurationError(
    isDuration,
    marginalValue
  );
  const isGoodThresholdError = isPercentOrDurationError(isDuration, goodValue);

  const determineErrorReasonText = (
    isMarginalGreaterError: boolean
  ): string => {
    if (parentName) {
      return `Threshold input is invalid for ${parentName}_${name} ${
        isMarginalGreaterError
          ? 'marginal must be greater than or equal to good'
          : ''
      }`;
    }
    return `Threshold input is invalid for ${name} ${
      isMarginalGreaterError
        ? 'marginal must be greater than or equal to good'
        : ''
    }`;
  };

  const updateErrors = (isFormatError: boolean, isGreaterError: boolean) => {
    if (isFormatError) {
      updateErrorState(
        `${parentName ?? ''}${name}`,
        true,
        determineErrorReasonText(false)
      );
    } else if (isGreaterError) {
      updateErrorState(
        `${parentName ?? ''}${name}`,
        true,
        determineErrorReasonText(true)
      );
    } else {
      updateErrorState(`${parentName ?? ''}${name}`, false, '');
    }
  };
  return (
    <>
      <HelpTextRenderer helpText={helpTextGood} isLoading={isLoading}>
        <TextField
          key={`${parentName ?? ''}${name}-good-threshold`}
          className={classes.thresholdInputs}
          label={`Good threshold ${isDuration ? 'duration' : '%'}`}
          value={determineThresholdValue(thresholdsMap, name).goodThreshold}
          onChange={(e) => {
            const value = e.currentTarget.value.toString();
            let isValidEntry = true;
            if (!isDuration) {
              // makes sure is only numeric
              isValidEntry = /^\d+$/.test(value);
              // checks if 0 or doesn't contain leading 0s
              if (
                (isValidEntry && value === '0') ||
                (isValidEntry && !/^0+/.test(value))
              ) {
                updateErrors(
                  isPercentOrDurationError(isDuration, value),
                  !isMarginalGreaterThanOrEqualToGood(
                    isDuration,
                    value,
                    marginalValue
                  )
                );
                updateGoodThreshold(name, e.currentTarget.value);
              }
            } else {
              updateErrors(
                isPercentOrDurationError(isDuration, value),
                !isMarginalGreaterThanOrEqualToGood(
                  isDuration,
                  value,
                  marginalValue
                )
              );
              updateGoodThreshold(name, e.currentTarget.value);
            }
          }}
          error={!isLoading && isGoodThresholdError}
          disabled={isLoading}
          inputProps={{ inputMode: 'numeric', pattern: '[0-9]*' }}
          onClickCapture={(e) => e.stopPropagation()}
        />
      </HelpTextRenderer>
      <div className={classes.spacer} />
      <HelpTextRenderer helpText={helpTextMarginal} isLoading={isLoading}>
        <TextField
          key={`${parentName ?? ''}${name}-marginal-threshold`}
          className={classes.thresholdInputs}
          label={`Marginal threshold ${isDuration ? 'duration' : '%'}`}
          value={determineThresholdValue(thresholdsMap, name).marginalThreshold}
          onChange={(e) => {
            const value = e.currentTarget.value.toString();
            let isValidEntry = true;
            if (!isDuration) {
              // makes sure is only numeric
              isValidEntry = /^\d+$/.test(value);
              // checks if 0 or doesn't contain leading 0s
              if (
                (isValidEntry && value === '0') ||
                (isValidEntry && !/^0+/.test(value))
              ) {
                updateErrors(
                  isPercentOrDurationError(isDuration, value),
                  !isMarginalGreaterThanOrEqualToGood(
                    isDuration,
                    goodValue,
                    value
                  )
                );
                updateMarginalThreshold(name, e.currentTarget.value);
              }
            } else {
              updateErrors(
                isPercentOrDurationError(isDuration, value),
                !isMarginalGreaterThanOrEqualToGood(
                  isDuration,
                  goodValue,
                  value
                )
              );
              updateMarginalThreshold(name, e.currentTarget.value);
            }
          }}
          error={!isLoading && isMarginalThresholdError}
          disabled={isLoading}
          inputProps={{ inputMode: 'numeric', pattern: '[0-9]*' }}
          onClickCapture={(e) => e.stopPropagation()}
        />
      </HelpTextRenderer>
    </>
  );
};
