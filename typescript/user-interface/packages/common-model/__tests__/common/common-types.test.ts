import { CommonTypes } from '../../src/ts/common-model';

const distance: CommonTypes.Distance = {
  degrees: 90.1,
  km: 2.1
};
const location: CommonTypes.Location = {
  latitudeDegrees: 90.0,
  longitudeDegrees: 180.0,
  elevationKm: 10,
  depthKm: 0.1
};
const distanceToSource: CommonTypes.DistanceToSource = {
  distance,
  azimuth: 260,
  sourceLocation: location,
  sourceType: CommonTypes.DistanceSourceType.UserDefined,
  sourceId: 'foo',
  stationId: 'AAK'
};

describe('Common Type Definitions', () => {
  it('expect Distance and DistanceToSource to defined', () => {
    expect(distance).toBeDefined();
    expect(distanceToSource).toBeDefined();
  });
});
