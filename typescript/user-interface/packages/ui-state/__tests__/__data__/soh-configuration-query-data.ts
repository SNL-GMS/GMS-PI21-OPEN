/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { ConfigurationTypes } from '@gms/common-model';

const twentySecs = 20;
const fiveSecsMs = 5000;
const fiveMinutesMs = 300000;
const tenMinutesMs = fiveMinutesMs * 2;
const fifteenMinutesMs = fiveMinutesMs * 3;
const oneHourMs = fifteenMinutesMs * 4;
const sixHoursMs = oneHourMs * 6;
const twelveHoursMs = oneHourMs * 12;
const twentyFourHoursMs = oneHourMs * 24;
const oneSixtyEightHoursMs = oneHourMs * 168;

export const sohConfiguration: ConfigurationTypes.UiSohConfiguration = {
  reprocessingPeriodSecs: twentySecs,
  displayedStationGroups: [
    'ALL_1',
    'ALL_2',
    'A_TO_H',
    'I_TO_Z',
    'EurAsia',
    'OthCont',
    'IMS_Sta',
    'CD1.1',
    'CD1.0',
    'MiniSD',
    'GSE',
    'Primary',
    'Second',
    'AuxFast',
    'AuxDel',
    'SEISMIC',
    'INFRA',
    'HYDRO'
  ],
  rollupStationSohTimeToleranceMs: tenMinutesMs,
  redisplayPeriodMs: fiveSecsMs,
  acknowledgementQuietMs: fiveMinutesMs,
  availableQuietTimesMs: [
    fiveMinutesMs,
    fifteenMinutesMs,
    oneHourMs,
    twentyFourHoursMs,
    oneSixtyEightHoursMs
  ], // "PT5M", "PT15M", "PT1H", "PT24H", "PT168H"
  sohStationStaleMs: fiveMinutesMs,
  sohHistoricalTimesMs: [sixHoursMs, twelveHoursMs, twentyFourHoursMs], // "PT6H", "PT12H", "PT24H"
  historicalSamplesPerChannel: 50000,
  maxHistoricalQueryIntervalSizeMs: 432000000 // milliseconds in a day * 5
};
