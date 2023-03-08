import { nonIdealStateWithNoSpinner, nonIdealStateWithSpinner } from '@gms/ui-core-components';

export const nonIdealStateSelectAStation = nonIdealStateWithNoSpinner(
  'No Station Selected',
  'Select a station in the Waveform or Map Display to view station properties',
  'select'
);
export const nonIdealStateLoadingEffectiveAtsQuery = nonIdealStateWithSpinner(
  'Loading Effective-at Times',
  'Please wait'
);
export const nonIdealStateLoadingStationDataQuery = nonIdealStateWithSpinner(
  'Loading Station Data',
  'Please wait'
);
export const nonIdealStateNoDataForStationsSelected = nonIdealStateWithNoSpinner(
  'No Data for Selected Station',
  'There appears to be no data for station',
  'exclude-row'
);
export const nonIdealStateTooManyStationsSelected = nonIdealStateWithNoSpinner(
  'Multiple Stations Selected',
  'Select a single station to view station properties',
  'exclude-row'
);
export const nonIdealStateNoOperationalTimePeriod = nonIdealStateWithNoSpinner(
  'No Operational Time Period',
  'Operational time period has not loaded',
  'exclude-row'
);
export const nonIdealStateSelectChannelGroupRow = nonIdealStateWithNoSpinner(
  'Select a Channel Group',
  'To populate channel data',
  'select'
);
export const nonIdealStateEmptyEffectiveAtsQuery = nonIdealStateWithNoSpinner(
  'Missing Effective-at Times',
  'No effective at times found for Station',
  'exclude-row'
);
