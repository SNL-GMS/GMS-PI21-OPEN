import { SohTypes } from '@gms/common-model';

/**
 * @returns a substring of a monitor type string that removes the first underscore and any prefix that
 * occurs before that underscore.
 */
export const convertSohMonitorTypeToAceiMonitorType = (
  sohMonitorTypeName: string
): SohTypes.AceiType => {
  if (!sohMonitorTypeName) {
    return undefined;
  }
  // TODO: remove this check once this monitor type drops the ENV prefix.
  // ENV_LAST_GPS_SYNC_TIME is an exception that never had it ENV_ prefix
  // This is a known issue.
  if (sohMonitorTypeName === SohTypes.AceiType.ENV_LAST_GPS_SYNC_TIME) {
    return SohTypes.AceiType[sohMonitorTypeName];
  }
  const firstUnderscore = sohMonitorTypeName.indexOf('_');
  return SohTypes.AceiType[sohMonitorTypeName.substring(firstUnderscore + 1)];
};

export const isAnalogAceiMonitorType = (aceiMonitorType: SohTypes.AceiType): boolean =>
  SohTypes.AnalogAceiType[aceiMonitorType];
