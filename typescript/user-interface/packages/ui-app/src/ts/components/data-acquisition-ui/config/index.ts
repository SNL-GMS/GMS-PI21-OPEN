import type { DataAcquisitionUserPreferences } from './user-preferences';
import { dataAcquisitionUserPreferences } from './user-preferences';

export interface DataAcquisitionUIConfig {
  dataAcquisitionUserPreferences: DataAcquisitionUserPreferences;
}

export const dataAcquisitionUIConfig: DataAcquisitionUIConfig = {
  dataAcquisitionUserPreferences
};

export { dataAcquisitionUserPreferences } from './user-preferences';
