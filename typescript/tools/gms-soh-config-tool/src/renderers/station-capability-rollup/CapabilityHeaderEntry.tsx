import { FormControl, Autocomplete, TextField } from '@mui/material';
import React from 'react';
import {
  OperatorType,
  RollupEntry,
  RollupType,
} from '../../state/station-controls-slice';
import { layoutStyles } from '../../styles/layout';
import {
  findAndDetermineMaxThresholdValueForChannelCapabilityEntry,
  findAndDetermineMaxThresholdValueForStationCapabilityEntry,
  getRollupByDefaultAndId,
  updateCapabilityGoodThreshold,
  updateCapabilityMarginalThreshold,
  updateCapabilityOperatorType,
  updateChannelCapabilityRollupType,
  updateStationCapabilityRollupType,
} from './util';
import makeStyles from '@mui/styles/makeStyles';
import { useAppSelector } from '../../state/react-redux-hooks';
import {
  useSelectedMonitors,
  useUpdateChannelCapabilityRollup,
  useUpdateErrorState,
  useUpdateStationGroupCapability,
} from '../../util/custom-hooks';
import {
  AppSections,
  ChannelCapabilityErrorTypes,
  StationCapabilityErrorTypes,
} from '../../routes/types';
import { isGoodGreaterThanOrEqualToMarginal } from '../../util/util';
import { batch } from 'react-redux';

const useStyles = makeStyles({
  ...layoutStyles,
  formControl: {
    margin: 0,
  },
  formEntry: {
    width: '300px',
  },
  threshold: {},
  header: {
    width: '100%',
    '& > *': {
      paddingRight: '1em',
    },
    '& > *:last-child': {
      paddingRight: '0',
    },
  },
  errorBorder: {
    border: 'solid',
    borderWidth: '1px',
    borderColor: 'tomato',
  },
});

/**
 * The type of the props for the {@link CapabilityHeaderEntry} component
 */
export interface CapabilityHeaderEntryProps {
  groupName: string;
  channelName?: string;
  rollupType: RollupType;
  rollupTypeOptions: string[];
  rollupId: string;
  operatorType: OperatorType;
  operatorTypeOptions: string[];
  goodThreshold?: string | number;
  marginalThreshold?: string | number;
}

/**
 * Creates a capability header, including the form inputs
 */
export const CapabilityHeaderEntry: React.FC<CapabilityHeaderEntryProps> = ({
  groupName,
  channelName,
  rollupType,
  rollupTypeOptions,
  rollupId,
  operatorType,
  operatorTypeOptions,
  goodThreshold,
  marginalThreshold,
}: CapabilityHeaderEntryProps) => {
  const classes = useStyles();
  const [updateStationGroupCapabilityRollup] =
    useUpdateStationGroupCapability();
  const [updateChannelCapabilityRollup] = useUpdateChannelCapabilityRollup();
  const [updateErrorState] = useUpdateErrorState();
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const defaultStationCapabilityRollup = useAppSelector(
    (store) =>
      store.stationControls.stationGroupCapabilityRollup[stationName ?? ''][
        groupName
      ]
  );
  const defaultChannelCapabilityRollup = useAppSelector(
    (store) =>
      store.stationControls.channelCapabilityRollup[stationName ?? ''][
        groupName
      ][channelName ?? '']
  );

  const configErrors = useAppSelector((state) => state.stationControls.error);

  const selectedMonitors = useSelectedMonitors();

  const selectedChannels = useAppSelector(
    (store) => store.stationControls.selectedChannels[stationName ?? '']
  );

  const isChannelCapability = channelName !== undefined;

  const updateCapability = (rollup: RollupEntry) => {
    if (isChannelCapability) {
      updateChannelCapabilityRollup(
        stationName ?? '',
        groupName,
        channelName,
        rollup
      );
    } else {
      updateStationGroupCapabilityRollup(stationName ?? '', groupName, rollup);
    }
  };

  let hasErrors = false;
  Object.keys(configErrors).forEach((errorId) => {
    if (errorId.includes(rollupId) && configErrors[errorId].hasError) {
      hasErrors = true;
    }
  });

  const determineErrorReasonTextForThresholds = React.useCallback(
    (
      doesGoodExceedMaxValue: boolean,
      doesMarginalExceedMaxValue: boolean,
      isMarginalGreaterError: boolean
    ): string => {
      let startOfError = '';
      if (isChannelCapability) {
        startOfError = `Threshold input is invalid for ${AppSections.GROUP} ${groupName} ${AppSections.CHANNEL_CAPABILITY} ${channelName}`;
      } else {
        startOfError = `Threshold input is invalid for ${AppSections.GROUP} ${groupName} ${AppSections.STATION_CAPABILITY}`;
      }
      return `${startOfError} ${
        isMarginalGreaterError
          ? ' - good must be greater than or equal to marginal'
          : ''
      } ${
        doesGoodExceedMaxValue
          ? ` - good input cannot exceed number of ${
              rollupType === RollupType.ROLLUP_OF_ROLLUPS
                ? 'rollups'
                : isChannelCapability
                ? 'monitors'
                : 'channels'
            }`
          : ''
      } ${
        doesMarginalExceedMaxValue
          ? ` - marginal input cannot exceed number of ${
              rollupType === RollupType.ROLLUP_OF_ROLLUPS
                ? 'rollups'
                : isChannelCapability
                ? 'monitors'
                : 'channels'
            }`
          : ''
      }`;
    },
    [channelName, groupName, isChannelCapability, rollupType]
  );

  const determineThresholdsInputErrors = React.useCallback(
    (
      goodThreshold: string,
      marginalThreshold: string
    ): {
      doesGoodExceedMaxValue: boolean;
      doesMarginalExceedMaxValue: boolean;
      isMarginalGreaterError: boolean;
    } => {
      let maxThreshold = -1;
      if (isChannelCapability) {
        maxThreshold =
          findAndDetermineMaxThresholdValueForChannelCapabilityEntry(
            defaultChannelCapabilityRollup,
            rollupId
          );
      } else {
        maxThreshold =
          findAndDetermineMaxThresholdValueForStationCapabilityEntry(
            defaultStationCapabilityRollup,
            rollupId
          );
      }
      const doesGoodExceedMaxValue = parseInt(goodThreshold) > maxThreshold;
      const doesMarginalExceedMaxValue =
        parseInt(marginalThreshold) > maxThreshold;
      const isMarginalGreaterError = !isGoodGreaterThanOrEqualToMarginal(
        false,
        goodThreshold,
        marginalThreshold
      );
      return {
        doesGoodExceedMaxValue,
        doesMarginalExceedMaxValue,
        isMarginalGreaterError,
      };
    },
    [
      defaultChannelCapabilityRollup,
      defaultStationCapabilityRollup,
      isChannelCapability,
      rollupId,
    ]
  );

  let thresholdInputErrors = {
    doesGoodExceedMaxValue: false,
    doesMarginalExceedMaxValue: false,
    isMarginalGreaterError: false,
  };

  if (
    operatorType === OperatorType.MIN_GOOD_OF &&
    goodThreshold &&
    marginalThreshold
  ) {
    thresholdInputErrors = determineThresholdsInputErrors(
      goodThreshold.toString(),
      marginalThreshold.toString()
    );
  }

  const updateErrors = React.useCallback(
    (goodThreshold: string, marginalThreshold: string) => {
      batch(() => {
        const newMarginalInputErrors = determineThresholdsInputErrors(
          goodThreshold.toString(),
          marginalThreshold.toString()
        );
        if (
          operatorType === OperatorType.MIN_GOOD_OF &&
          (newMarginalInputErrors.doesGoodExceedMaxValue ||
            newMarginalInputErrors.doesMarginalExceedMaxValue ||
            newMarginalInputErrors.isMarginalGreaterError)
        ) {
          if (
            newMarginalInputErrors.doesGoodExceedMaxValue ||
            newMarginalInputErrors.doesMarginalExceedMaxValue
          ) {
            updateErrorState(
              `${rollupId} ${ChannelCapabilityErrorTypes.THRESHOLD_EXCEEDS_MAX}`,
              true,
              determineErrorReasonTextForThresholds(
                newMarginalInputErrors.doesGoodExceedMaxValue,
                newMarginalInputErrors.doesMarginalExceedMaxValue,
                false // don't want to include error text for marginalGreaterError
              )
            );
          }
          if (newMarginalInputErrors.isMarginalGreaterError) {
            updateErrorState(
              `${rollupId} ${ChannelCapabilityErrorTypes.MARGINAL_EXCEEDS_GOOD}`,
              true,
              determineErrorReasonTextForThresholds(
                false, // don't want to include error text for goodExceedsMaxValue
                false, // don't want to include error text for marginalExceedsMaxValue
                newMarginalInputErrors.isMarginalGreaterError
              )
            );
          }
        } else {
          updateErrorState(
            `${rollupId} ${ChannelCapabilityErrorTypes.THRESHOLD_EXCEEDS_MAX}`,
            false,
            ''
          );
          updateErrorState(
            `${rollupId} ${ChannelCapabilityErrorTypes.MARGINAL_EXCEEDS_GOOD}`,
            false,
            ''
          );
        }
      });
    },
    [
      determineErrorReasonTextForThresholds,
      determineThresholdsInputErrors,
      operatorType,
      rollupId,
      updateErrorState,
    ]
  );

  React.useEffect(() => {
    if (rollupType === RollupType.ROLLUP_OF_ROLLUPS) {
      let rollup: RollupEntry;
      let errorType: string = StationCapabilityErrorTypes.NO_ROLLUPS;
      let errorMessage: string;
      if (channelName) {
        rollup = getRollupByDefaultAndId(
          defaultChannelCapabilityRollup,
          rollupId
        );
        errorType = ChannelCapabilityErrorTypes.NO_ROLLUPS;
        errorMessage = `Must have one rollup included, go to ${AppSections.GROUP} ${groupName} ${AppSections.CHANNEL_CAPABILITY} ${channelName} and create a rollup entry`;
      } else {
        rollup = getRollupByDefaultAndId(
          defaultStationCapabilityRollup,
          rollupId
        );
        errorMessage = `Must have one rollup included, go to ${AppSections.GROUP} ${groupName} ${AppSections.STATION_CAPABILITY} and create a rollup entry`;
      }
      if (!rollup.rollups || rollup.rollups.length === 0) {
        updateErrorState(`${rollupId} ${errorType}`, true, errorMessage);
      } else {
        updateErrorState(`${rollupId} ${errorType}`, false, '');
      }
    }
  }, [
    channelName,
    defaultChannelCapabilityRollup,
    defaultStationCapabilityRollup,
    groupName,
    rollupId,
    rollupType,
    updateErrorState,
  ]);

  return (
    <div
      className={`${classes.header} ${hasErrors ? classes.errorBorder : ''}`}
    >
      <FormControl margin='none' className={classes.formControl}>
        <Autocomplete
          className={classes.formEntry}
          size={'small'}
          autoComplete
          disablePortal
          disableClearable
          id='#/properties/rollup-type'
          value={rollupType}
          options={rollupTypeOptions}
          onChange={(e, value) => {
            updateCapability(
              !isChannelCapability
                ? updateStationCapabilityRollupType(
                    rollupId,
                    defaultStationCapabilityRollup,
                    value,
                    selectedChannels
                  )
                : updateChannelCapabilityRollupType(
                    rollupId,
                    defaultChannelCapabilityRollup,
                    value,
                    selectedMonitors ?? []
                  )
            );
          }}
          renderInput={(params) => (
            <TextField {...params} label={'Rollup Type'} />
          )}
        />
      </FormControl>
      <FormControl margin='none' className={classes.formControl}>
        <Autocomplete
          className={classes.formEntry}
          size={'small'}
          autoComplete
          disablePortal
          disableClearable
          id='#/properties/operator-type'
          value={operatorType}
          options={operatorTypeOptions}
          onChange={(e, value) => {
            updateCapability(
              updateCapabilityOperatorType(
                rollupId,
                isChannelCapability
                  ? defaultChannelCapabilityRollup
                  : defaultStationCapabilityRollup,
                value
              )
            );
          }}
          renderInput={(params) => (
            <TextField {...params} label={'Operator Type'} />
          )}
        />
      </FormControl>
      <FormControl margin='none' className={classes.formControl}>
        <TextField
          className={classes.threshold}
          disabled={operatorType !== OperatorType.MIN_GOOD_OF}
          size={'small'}
          label={`Good threshold`}
          value={
            operatorType === OperatorType.MIN_GOOD_OF ? goodThreshold : 'N/A'
          }
          error={
            thresholdInputErrors.doesGoodExceedMaxValue ||
            thresholdInputErrors.isMarginalGreaterError
          }
          onChange={(e) => {
            // convert to string and remove leading 0s
            let value = e.currentTarget.value.toString();
            let isNumber = /^\d+$/.test(value);
            // checks if number and doesn't lead with 0s so input cannot be 01
            if (
              (isNumber && value === '0') ||
              (isNumber && !/^0+/.test(value))
            ) {
              updateErrors(value, marginalThreshold?.toString() ?? '');
              updateCapability(
                updateCapabilityGoodThreshold(
                  rollupId,
                  isChannelCapability
                    ? defaultChannelCapabilityRollup
                    : defaultStationCapabilityRollup,
                  e.target.value
                )
              );
            }
          }}
          onClickCapture={(e) => e.stopPropagation()}
        />
      </FormControl>
      <FormControl margin='none' className={classes.formControl}>
        <TextField
          className={classes.threshold}
          disabled={operatorType !== OperatorType.MIN_GOOD_OF}
          size={'small'}
          label={`Marginal threshold`}
          value={
            operatorType === OperatorType.MIN_GOOD_OF
              ? marginalThreshold
              : 'N/A'
          }
          error={
            thresholdInputErrors.doesMarginalExceedMaxValue ||
            thresholdInputErrors.isMarginalGreaterError
          }
          onChange={(e) => {
            let value = e.currentTarget.value.toString();
            let isNumber = /^\d+$/.test(value);
            // checks if number and doesn't lead with 0s so input cannot be 01
            if (
              (isNumber && value === '0') ||
              (isNumber && !/^0+/.test(value))
            ) {
              updateErrors(goodThreshold?.toLocaleString() ?? '', value);
              updateCapability(
                updateCapabilityMarginalThreshold(
                  rollupId,
                  isChannelCapability
                    ? defaultChannelCapabilityRollup
                    : defaultStationCapabilityRollup,
                  e.target.value
                )
              );
            }
          }}
          onClickCapture={(e) => e.stopPropagation()}
        />
      </FormControl>
    </div>
  );
};
