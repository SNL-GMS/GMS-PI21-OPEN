import { ConfigurationTypes } from '@gms/common-model';

import type { EnvironmentConfig } from './environment-config';
import { environmentConfig } from './environment-config';
import type { SystemConfig } from './system-config';
import { systemConfig } from './system-config';
import type { UserPreferences } from './user-preferences';
import { userPreferences } from './user-preferences';

export interface AnalystUiConfig {
  userPreferences: UserPreferences;
  environment: EnvironmentConfig;
  systemConfig: SystemConfig;
  unassociatedSDColor: string;
}

export const analystUiConfig: AnalystUiConfig = {
  userPreferences,
  environment: environmentConfig,
  systemConfig,
  unassociatedSDColor: ConfigurationTypes.defaultColorTheme.unassociatedSDColor
};
export { EnvironmentConfig, environmentConfig } from './environment-config';
export { SystemConfig, systemConfig } from './system-config';
export { QcMaskDisplayFilters, UserPreferences, userPreferences } from './user-preferences';
