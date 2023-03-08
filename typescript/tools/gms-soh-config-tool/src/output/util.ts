import produce from 'immer';
import isEqual from 'lodash/isEqual';
import { RollupOperatorOperands } from '../state/retrieve-station-group-capability';
import { OperatorType } from '../state/station-controls-slice';

/**
 * Performs a series of checks to determine if top entry matches default
 *
 * @param entry top level RollupOperatorOperands
 * @param defaultEntry default RollupOperatorOperands
 * @param allChannelNames names of all the channels
 * @returns boolean
 */
export const doesStationCapabilityEntryMatchDefault = (
  entry: RollupOperatorOperands,
  defaultEntry: RollupOperatorOperands,
  allChannelNames: string[]
) => {
  if (entry.rollupOperatorOperands && entry.rollupOperatorOperands.length > 0) {
    return false;
  }

  // defaults and default overrides(default.json overrides.json) will never have listed channels thus
  // with defaults since no channels are provided it means all are included, so
  // if the lengths do not match it must be different than default
  if (
    entry.channelOperands &&
    allChannelNames.length !== entry.channelOperands.length
  ) {
    return false;
  }

  if (entry.operatorType === defaultEntry.operatorType) {
    if (
      entry.operatorType === OperatorType.MIN_GOOD_OF &&
      entry.goodThreshold &&
      defaultEntry.goodThreshold &&
      entry.marginalThreshold &&
      defaultEntry.marginalThreshold &&
      (entry.goodThreshold !== defaultEntry.goodThreshold ||
        entry.marginalThreshold !== defaultEntry.marginalThreshold)
    ) {
      return false;
    }
    return true;
  }
  return false;
};

/**
 * Runs a series of checks to determine if monitors for two entries are identical
 *
 * @param entry
 * @param defaultEntry
 * @returns boolean
 */
const doSohMonitorTypeOperandsMatch = (
  entry: RollupOperatorOperands,
  defaultEntry: RollupOperatorOperands
): boolean => {
  // if both do not have monitors defined then both have all selected so it's a match
  if (!defaultEntry.sohMonitorTypeOperands && !entry.sohMonitorTypeOperands) {
    return true;
  }
  // if sohMonitorTypeOperands is undefined means all selected so if all is selected but
  // entry doesn't have them all must be different
  if (
    (!defaultEntry.sohMonitorTypeOperands && entry.sohMonitorTypeOperands) ||
    (defaultEntry.sohMonitorTypeOperands && !entry.sohMonitorTypeOperands)
  ) {
    return false;
  }
  // Means top rollup is a rollupType is a rollup of monitors
  if (!defaultEntry.rollupOperatorOperands && !entry.rollupOperatorOperands) {
    // If they both have some defined need to check if they are the same
    if (defaultEntry.sohMonitorTypeOperands && entry.sohMonitorTypeOperands) {
      const sortedDefaultEntryMonitors = produce(
        defaultEntry.sohMonitorTypeOperands,
        (draft) => {
          draft.sort();
        }
      );
      const sortedEntryMonitors = produce(
        entry.sohMonitorTypeOperands ?? [],
        (draft) => {
          draft.sort();
        }
      );
      if (!isEqual(sortedDefaultEntryMonitors, sortedEntryMonitors)) {
        return false;
      }
    }
  }
  return true;
};

const isRollupOperatorOperandEqualToDefault = (
  entry: RollupOperatorOperands,
  defaultEntry: RollupOperatorOperands,
  hasMismatch: boolean
): boolean => {
  let matchesDefault = hasMismatch;
  if (
    defaultEntry &&
    defaultEntry.rollupOperatorOperands &&
    defaultEntry.rollupOperatorOperands.length !==
      entry?.rollupOperatorOperands?.length
  ) {
    matchesDefault = false;
  }
  if (!isEqual(defaultEntry.operatorType, entry.operatorType)) {
    matchesDefault = false;
  }

  if (defaultEntry.operatorType === OperatorType.MIN_GOOD_OF) {
    if (
      defaultEntry.goodThreshold !== entry?.goodThreshold ||
      defaultEntry.marginalThreshold !== entry?.marginalThreshold
    ) {
      matchesDefault = false;
    }
  }

  const doMonitorsMatch = doSohMonitorTypeOperandsMatch(entry, defaultEntry);

  if (!doMonitorsMatch) {
    matchesDefault = false;
  }

  if (defaultEntry.rollupOperatorOperands && matchesDefault === true) {
    if (!entry.rollupOperatorOperands) {
      matchesDefault = false;
    }
    if (entry.rollupOperatorOperands) {
      entry.rollupOperatorOperands.forEach((operand, index) => {
        if (defaultEntry.rollupOperatorOperands) {
          matchesDefault = isRollupOperatorOperandEqualToDefault(
            operand,
            defaultEntry.rollupOperatorOperands[index],
            matchesDefault
          );
        }
      });
    }
  }
  return matchesDefault;
};

/**
 * Performs a series of checks to determine if top entry matches default
 *
 * @param entry top level RollupOperatorOperands
 * @param defaultEntry default RollupOperatorOperands
 * @param supportedMonitorNames names of supported monitor names
 * @returns boolean
 */
export const doesChannelCapabilityEntryMatchDefault = (
  entry: RollupOperatorOperands,
  defaultEntry: RollupOperatorOperands,
  supportedMonitorNames: string[]
) => {
  if (entry.rollupOperatorOperands && entry.rollupOperatorOperands.length > 0) {
    return isRollupOperatorOperandEqualToDefault(entry, defaultEntry, true);
  }

  const doMonitorsMatch = doSohMonitorTypeOperandsMatch(entry, defaultEntry);

  if (!doMonitorsMatch) {
    return false;
  }

  if (entry.operatorType === defaultEntry.operatorType) {
    if (
      entry.operatorType === OperatorType.MIN_GOOD_OF &&
      entry.goodThreshold &&
      defaultEntry.goodThreshold &&
      entry.marginalThreshold &&
      defaultEntry.marginalThreshold &&
      (entry.goodThreshold !== defaultEntry.goodThreshold ||
        entry.marginalThreshold !== defaultEntry.marginalThreshold)
    ) {
      return false;
    }
    return true;
  }

  if (entry.operatorType !== defaultEntry.operatorType) {
    return false;
  }

  return true;
};
