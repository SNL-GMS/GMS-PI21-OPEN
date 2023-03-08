import { ConfigurationTypes } from '@gms/common-model';
import type { Hex } from '@gms/common-model/lib/color/types';
import { Logger } from '@gms/common-util';
import { getLegibleHexColor, isColorLegible, isHexColor } from '@gms/ui-util';
import React from 'react';

import { defaultTheme } from '../api/processing-configuration/default-theme';
import { useGetProcessingAnalystConfigurationQuery } from '../api/processing-configuration/processing-configuration-api-slice';
import { useGetUserProfileQuery } from '../api/user-manager/user-manager-api-slice';
import { useSetThemeInUserProfile } from './user-manager-hooks';

const logger = Logger.create('GMS_LOG_PROCESSING_CONFIG', process.env.GMS_LOG_PROCESSING_CONFIG);

const emptyTheme = [];

/**
 * Get the default interactive analysis station group
 */
export const useDefaultInteractiveAnalysisStationGroup = (): string =>
  useGetProcessingAnalystConfigurationQuery().data?.defaultInteractiveAnalysisStationGroup;

/**
 * @returns the currently selected UI theme, as defined in processing config and selected in the user profile.
 * @default defaultTheme is the default GMS theme (while we are waiting for it to load, for example)
 */
export const useUiTheme = (): [ConfigurationTypes.UITheme, (themeName: string) => void] => {
  // see if the user's profile has a preferred theme
  const userProfileQueryResults = useGetUserProfileQuery();
  const currentUserProfile = userProfileQueryResults.data;
  const currentThemeName = currentUserProfile?.currentTheme ?? defaultTheme.name;

  const uiThemes = useGetProcessingAnalystConfigurationQuery().data?.uiThemes ?? emptyTheme;

  let selectedTheme = uiThemes.find(t => t.name === currentThemeName);
  React.useLayoutEffect(() => {
    if (selectedTheme) {
      localStorage.setItem('uiTheme', JSON.stringify(selectedTheme));
    }
  }, [selectedTheme]);

  const setThemeInProfile = useSetThemeInUserProfile();
  const setUiTheme = React.useCallback(
    async (themeName: string) => {
      logger.info('Set UI Theme', themeName);
      await setThemeInProfile(themeName);
    },
    [setThemeInProfile]
  );

  const themeInLocalStorage = localStorage.getItem('uiTheme');
  if (!selectedTheme && (themeInLocalStorage == null || themeInLocalStorage === 'undefined')) {
    selectedTheme = defaultTheme;
  } else if (!selectedTheme) {
    selectedTheme = JSON.parse(themeInLocalStorage);
  }

  return [selectedTheme, setUiTheme];
};

/**
 * @returns the list of all UI themes from processing configuration
 */
export const useGetAllUiThemes = (): ConfigurationTypes.UITheme[] => {
  return useGetProcessingAnalystConfigurationQuery().data?.uiThemes ?? [];
};

/**
 * Get the unassociated signal detection length in meters from redux and return it
 */
export const useUnassociatedSignalDetectionLengthInMeters = (): number =>
  useGetProcessingAnalystConfigurationQuery().data?.unassociatedSignalDetectionLengthMeters ??
  ConfigurationTypes.defaultUnassociatedSignalDetectionLengthMeters;

/**
 * Get the unassociated signal detection color from redux and return it
 */
export const useUnassociatedSDColor = (): string => {
  const [currentTheme] = useUiTheme();
  return (
    currentTheme.colors.unassociatedSDColor ??
    ConfigurationTypes.defaultColorTheme.unassociatedSDColor
  );
};

/**
 * Get the associated open detection color from redux and return it
 */
export const useOpenEventSDColor = (): string => {
  const [currentTheme] = useUiTheme();
  return (
    currentTheme.colors.openEventSDColor ?? ConfigurationTypes.defaultColorTheme.openEventSDColor
  );
};

/**
 * Get the MapStationDefault color from redux and return it
 */
export const useMapStationDefaultColor = (): string => {
  const [currentTheme] = useUiTheme();
  return (
    currentTheme.colors.mapStationDefault ?? ConfigurationTypes.defaultColorTheme.mapStationDefault
  );
};

/**
 * Get the MapVisibleStation color from redux and return it
 */
export const useMapVisibleStationColor = (): string => {
  const [currentTheme] = useUiTheme();
  return (
    currentTheme.colors.mapVisibleStation ?? ConfigurationTypes.defaultColorTheme.mapVisibleStation
  );
};

/**
 * Get the associated complete signal detection color from redux and return it
 */
export const useCompleteEventSDColor = (): string => {
  const [currentTheme] = useUiTheme();
  return (
    currentTheme.colors.completeEventSDColor ??
    ConfigurationTypes.defaultColorTheme.completeEventSDColor
  );
};

/**
 * Get the associated other detection color from redux and return it
 */
export const useOtherEventSDColor = (): string => {
  const [currentTheme] = useUiTheme();
  return (
    currentTheme.colors.otherEventSDColor ?? ConfigurationTypes.defaultColorTheme.otherEventSDColor
  );
};

/**
 * @param backgroundColor A hex color which will be in the background behind text or an icon
 * @param foregroundColor an optional color that will be used if it is legible against the
 * provided background color. If not, defaults to `gmsMain` or `gmsMainInverted`
 * @returns a color that is legible against the background color. The color will be either
 * the `gmsMain` or `gmsMainInverted` color from the current theme, or black/white if the
 * color is not a hex color. (non-hex colors are not supported at this time)
 */
export const useLegibleColor = (backgroundColor: Hex, foregroundColor?: Hex): Hex => {
  const [uiTheme] = useUiTheme();
  if (foregroundColor && isColorLegible(backgroundColor, foregroundColor)) {
    return foregroundColor;
  }
  return getLegibleHexColor(
    backgroundColor,
    isHexColor(uiTheme.colors.gmsMain) ? uiTheme.colors.gmsMain : '#FFFFFF',
    isHexColor(uiTheme.colors.gmsMainInverted) ? uiTheme.colors.gmsMainInverted : '#000000'
  );
};

/**
 * Creates legible colors for event association colors.
 *
 * @example <MyComponent style={useLegibleColorsForEventAssociations()} />
 * @returns an object containing the custom properties, which can be used in inline styles to assign these color variables.
 */
export const useLegibleColorsForEventAssociations = (): React.CSSProperties => {
  const [uiTheme] = useUiTheme();
  const openAssociatedColor = useLegibleColor(
    isHexColor(uiTheme.colors.openEventSDColor) ? uiTheme.colors.openEventSDColor : '#000000'
  );
  const completeAssociatedColor = useLegibleColor(
    isHexColor(uiTheme.colors.completeEventSDColor)
      ? uiTheme.colors.completeEventSDColor
      : '#000000'
  );
  const otherAssociatedColor = useLegibleColor(
    isHexColor(uiTheme.colors.otherEventSDColor) ? uiTheme.colors.otherEventSDColor : '#000000'
  );
  const unassociatedColor = useLegibleColor(
    isHexColor(uiTheme.colors.unassociatedSDColor) ? uiTheme.colors.unassociatedSDColor : '#000000'
  );
  return React.useMemo(
    () =>
      ({
        '--open-associated-text-color': openAssociatedColor,
        '--complete-associated-text-color': completeAssociatedColor,
        '--other-associated-text-color': otherAssociatedColor,
        '--unassociated-text-color': unassociatedColor
      } as React.CSSProperties),
    [completeAssociatedColor, openAssociatedColor, otherAssociatedColor, unassociatedColor]
  );
};

/**
 * @returns the keyboard shortcut configuration from processing config, or undefined if the query
 * has not returned data
 */
export const useKeyboardShortcutConfig = ():
  | ConfigurationTypes.KeyboardShortcutConfig
  | undefined => {
  return useGetProcessingAnalystConfigurationQuery().data?.keyboardShortcuts;
};
