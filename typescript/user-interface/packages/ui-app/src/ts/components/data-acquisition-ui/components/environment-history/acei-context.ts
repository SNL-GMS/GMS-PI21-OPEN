import type { SohTypes } from '@gms/common-model';
import React from 'react';

export type AceiMonitorTypeOption = 'CHOOSE_A_MONITOR_TYPE' | SohTypes.AceiType;

/** ACEI selected context data */
export interface AceiContextData {
  selectedAceiType: SohTypes.AceiType;
  setSelectedAceiType(aceiType: SohTypes.AceiType): void;
}

/** ACEI context, provides the selected ACEI type and the function to select the ACEI type */
export const AceiContext: React.Context<AceiContextData> = React.createContext<AceiContextData>(
  undefined
);
