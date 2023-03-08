import { SohTypes } from '@gms/common-model';

import {
  getHistoricalAceiData,
  getHistoricalSohData
} from '../src/ts/soh/mock-historical-soh-acei';

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
Date.now = jest.fn().mockReturnValue(1575410988600);
const startTimeStr = new Date(0).toISOString();
const endTimeStr = new Date(100).toISOString();

describe('Fetch ACEI Historical data', () => {
  const historicalAceiInput = {
    endTime: endTimeStr,
    startTime: startTimeStr,
    stationName: 'AAK',
    type: SohTypes.AceiType.AMPLIFIER_SATURATION_DETECTED
  };

  const historicalSohInput = {
    endTime: endTimeStr,
    startTime: startTimeStr,
    stationName: 'AAK',
    sohMonitorType: SohTypes.SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
    samplesPerChannel: 100
  };

  it('getHistoricalAceiData matches snapshot', () => {
    const historicalData: SohTypes.UiHistoricalAcei[] = getHistoricalAceiData(historicalAceiInput);
    // Strip out issues since they are random values
    const historicalDataNoIssues = historicalData.map(hd => {
      return {
        ...hd,
        issues: []
      };
    });
    expect(historicalDataNoIssues).toMatchSnapshot();
  });

  it('getHistoricalAceiData errors', () => {
    historicalAceiInput.stationName = undefined;
    const undefinedErr: Error = new Error('Unable to retrieve historical ACEI data due to input');
    expect(() => getHistoricalAceiData(undefined)).toThrowError(undefinedErr);

    const stationNameErr = new Error(
      'Unable to retrieve historical ACEI data due to missing stationName'
    );

    expect(() => getHistoricalAceiData(historicalAceiInput)).toThrowError(stationNameErr);
  });

  it('getHistoricalAceiData returns undefined for bad station name', () => {
    historicalAceiInput.stationName = 'FOO';
    const stationNotFoundErr = new Error(
      'Unable to retrieve historical ACEI data station FOO not found'
    );
    expect(() => getHistoricalAceiData(historicalAceiInput)).toThrowError(stationNotFoundErr);
  });

  /** Historical SOH Data */
  it('getHistoricalSohData matches snapshot', () => {
    const historicalData: SohTypes.UiHistoricalSoh = getHistoricalSohData(historicalSohInput);
    expect(historicalData).toMatchSnapshot();
  });

  it('getHistoricalSohData errors', () => {
    historicalSohInput.stationName = undefined;
    const undefinedErr: Error = new Error('Unable to retrieve historical SOH data due to input');
    expect(() => getHistoricalSohData(undefined)).toThrowError(undefinedErr);

    const stationNameErr = new Error(
      'Unable to retrieve historical SOH data due to missing stationName'
    );

    expect(() => getHistoricalSohData(historicalSohInput)).toThrowError(stationNameErr);
  });

  it('getHistoricalSohData returns undefined for bad station name', () => {
    historicalSohInput.stationName = 'FOO';
    const stationNotFoundErr = new Error(
      'Unable to retrieve historical SOH data station FOO not found'
    );
    expect(() => getHistoricalSohData(historicalSohInput)).toThrowError(stationNotFoundErr);
  });
});
