export interface UserPreferences {
  baseSoundsPath: string;
  tableRowHeightPx: number;
  tableHeaderHeightPx: number;
  soundFileNotFound(filename: string): string;
  configuredAudibleNotificationFileNotFound(filename: string): string;
}

export const userPreferences: UserPreferences = {
  baseSoundsPath: `./resources/sounds/`,
  tableRowHeightPx: 36,
  tableHeaderHeightPx: 18,
  soundFileNotFound: (filename: string) => `Failed to load sound file "${filename}".`,
  configuredAudibleNotificationFileNotFound: (filename: string) =>
    `Failed to load sound file "${filename}" for configured audible notification. Sound will not play.`
};
