/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { LegacyEventTypes, SignalDetectionTypes } from '@gms/common-model';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';

import {
  convertGraphicsXYtoCoordinate,
  convertPolarToXY,
  convertXYtoPolar,
  getSortedSignalDetections
} from '../../../../../src/ts/components/analyst-ui/components/azimuth-slowness/components/fk-util';
import { canvasDimension, fkSpectra, incrementAmt, sqrtOfFifty } from './fk-spectra';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

/**
 * Tests helper function that converts graphic space to xy coordinates
 */
describe('convertGraphicsXYtoCoordinate', () => {
  test('tests calling function with valid inputs and known output', () => {
    let xyValue = 0;

    let xy = convertGraphicsXYtoCoordinate(
      xyValue,
      xyValue,
      fkSpectra,
      canvasDimension,
      canvasDimension
    );
    expect(xy.x).toEqual(-20);
    expect(xy.y).toEqual(20);

    xyValue += incrementAmt;
    xy = convertGraphicsXYtoCoordinate(
      xyValue,
      xyValue,
      fkSpectra,
      canvasDimension,
      canvasDimension
    );
    expect(xy.x).toEqual(-10);
    expect(xy.y).toEqual(10);

    xyValue += incrementAmt;
    xy = convertGraphicsXYtoCoordinate(
      xyValue,
      xyValue,
      fkSpectra,
      canvasDimension,
      canvasDimension
    );
    expect(xy.x).toEqual(0);
    expect(xy.y).toEqual(0);

    xyValue += incrementAmt;
    xy = convertGraphicsXYtoCoordinate(
      xyValue,
      xyValue,
      fkSpectra,
      canvasDimension,
      canvasDimension
    );
    expect(xy.x).toEqual(10);
    expect(xy.y).toEqual(-10);

    xyValue += incrementAmt;
    xy = convertGraphicsXYtoCoordinate(
      xyValue,
      xyValue,
      fkSpectra,
      canvasDimension,
      canvasDimension
    );
    expect(xy.x).toEqual(20);
    expect(xy.y).toEqual(-20);
  });

  test('bad input', () => {
    const xyValue = 0;
    let xy = convertGraphicsXYtoCoordinate(
      undefined,
      xyValue,
      fkSpectra,
      canvasDimension,
      canvasDimension
    );
    expect(xy).toBeUndefined();
    xy = convertGraphicsXYtoCoordinate(
      xyValue,
      undefined,
      fkSpectra,
      canvasDimension,
      canvasDimension
    );
    expect(xy).toBeUndefined();
    xy = convertGraphicsXYtoCoordinate(
      xyValue,
      xyValue,
      undefined,
      canvasDimension,
      canvasDimension
    );
    expect(xy).toBeUndefined();
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, undefined, canvasDimension);
    expect(xy).toBeUndefined();
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, canvasDimension, undefined);
    expect(xy).toBeUndefined();
  });
});

describe('convertXYtoPolar', () => {
  test('valid inputs to xy to polar conversion', () => {
    const polar = convertXYtoPolar(5, 5);
    expect(polar.azimuthDeg).toEqual(45);
    expect(polar.radialSlowness).toEqual(sqrtOfFifty);
  });

  test('bad inputs for conversion', () => {
    const polar = convertXYtoPolar(undefined, undefined);
    expect(polar.azimuthDeg).toBeUndefined();
    expect(polar.radialSlowness).toBeUndefined();
  });
});

describe('convertPolarToXY', () => {
  test('valid inputs to polar to xy conversion', () => {
    const xy = convertPolarToXY(sqrtOfFifty, 45);
    expect(xy.x).toBeCloseTo(5, 5);
    expect(xy.y).toBeCloseTo(-5, 5);
  });

  test('bad inputs for conversion', () => {
    const xy = convertPolarToXY(undefined, undefined);
    expect(xy.x).toBeNaN();
    expect(xy.y).toBeNaN();
  });
});

describe('getSortedSignalDetections', () => {
  it('test getSortedSignalDetections returns empty list with empty lists', () => {
    const emptySds: SignalDetectionTypes.SignalDetection[] = [];
    const emptyDistanceToSource: LegacyEventTypes.LocationToStationDistance[] = [];
    const sortedSds = getSortedSignalDetections(
      emptySds,
      AnalystWorkspaceTypes.WaveformSortType.distance,
      emptyDistanceToSource
    );
    expect(sortedSds).toEqual([]);
  });
  it('test getSortedSignalDetections correctly sorts by station', () => {
    const sds: SignalDetectionTypes.SignalDetection[] = signalDetectionsData;
    const emptyDistanceToSource: LegacyEventTypes.LocationToStationDistance[] = [];
    const sortedSds = getSortedSignalDetections(
      sds,
      AnalystWorkspaceTypes.WaveformSortType.stationNameAZ,
      emptyDistanceToSource
    );
    expect(sortedSds).toMatchSnapshot();
  });
});
