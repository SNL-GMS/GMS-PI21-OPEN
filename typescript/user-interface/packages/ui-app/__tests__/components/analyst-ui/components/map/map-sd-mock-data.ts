export const mockSd: any = {
  id: '1c92ac77-c01b-3f96-b64e-f0d73df0d977',
  type: 'Signal detection',
  detectionTime: '2022-05-19 05:10:00.000',
  azimuthValue: 220.33629,
  slownessValue: {
    measuredValue: {
      value: 26.793263,
      standardDeviation: 0.0005,
      units: 'SECONDS_PER_DEGREE'
    }
  },
  phaseValue: {
    value: 'N',
    referenceTime: 1546711499.4
  },
  associatedEventTimeValue: '2022-05-19 05:10:00.000',
  signalDetectionColor: '#FFFFFF33',
  status: 'Associated to other event',
  edgeSDType: 'Before',
  stationName: 'AAA'
};

export const mockEmptySd: any = {
  id: '1c92ac77-c01b-3f96-b64e-f0d73df0d977',
  type: 'Signal detection',
  detectionTime: undefined,
  azimuthValue: undefined,
  slownessValue: {
    measuredValue: {
      value: undefined,
      standardDeviation: 0.0005,
      units: 'SECONDS_PER_DEGREE'
    }
  },
  phaseValue: {
    value: undefined,
    referenceTime: 1546711499.4
  },
  associatedEventTimeValue: '2022-05-19 05:10:00.000',
  signalDetectionColor: '#FFFFFF33',
  status: 'Associated to other event',
  edgeSDType: 'Before',
  stationName: undefined
};
