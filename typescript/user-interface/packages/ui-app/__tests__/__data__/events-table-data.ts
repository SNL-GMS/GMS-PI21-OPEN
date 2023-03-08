import type { Row } from '@gms/ui-core-components';

export const getRowData = (changed: boolean): Row[] => {
  const dataRows: Row[] = [];
  const row: any = {
    aFiveMeasurement: 3.6,
    aFivePeriod: 0.79,
    alrMeasurement: null,
    alrPeriod: null,
    assocEventId: '3a06fac7-46ad-337e-a8da-090a1cc801a1',
    azimuth: 52.56,
    color: '#D9822B',
    hypothesisId: '65b64194-47d3-4b5a-91ce-ec1b6dc36fe9',
    isComplete: false,
    isSelectedEvent: true,
    modified: changed,
    phase: 'Pg',
    possiblyConflictingEvents: [],
    slowness: 18.65,
    station: 'PDAR',
    time: 1274392950.85,
    timeUnc: 0.12
  };
  dataRows.push(row);
  return dataRows;
};
