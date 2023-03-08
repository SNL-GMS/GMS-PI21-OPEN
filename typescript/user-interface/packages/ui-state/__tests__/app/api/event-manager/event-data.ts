import { CommonTypes, EventTypes, SignalDetectionTypes } from '@gms/common-model';
import { StationType } from '@gms/common-model/lib/common/types';
import {
  ChannelBandType,
  ChannelInstrumentType,
  ChannelOrientationType
} from '@gms/common-model/lib/station-definitions/channel-definitions/channel-definitions';
import { ChannelDataType, ChannelGroupType } from '@gms/common-model/lib/station-processing/types';

const eventFeatureMeasurement: SignalDetectionTypes.FeatureMeasurement = {
  channel: {
    name: 'ASAR.AS01.SHZ',
    effectiveAt: 1636503404
  },
  measuredChannelSegment: {
    id: {
      channel: {
        name: 'ASAR.AS01.SHZ',
        effectiveAt: 1636503404
      },
      startTime: 1636503404,
      endTime: 1636503504,
      creationTime: 1636503404
    }
  },
  snr: null,
  measurementValue: {
    value: CommonTypes.PhaseType.P,
    confidence: 0.5
  },
  featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.PHASE
};

const featurePrediction: EventTypes.FeaturePrediction = {
  channel: {
    name: 'ASAR.AS01.SHZ',
    effectiveAt: 1636503404
  } as any,
  phase: undefined,
  predictionValue: {
    predictedValue: {
      arrivalTime: {
        value: 123,
        standardDeviation: 1
      },
      travelTime: undefined
    },
    featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME,
    featurePredictionComponentSet: []
  },
  predictionChannelSegment: undefined,
  predictionType: undefined,
  receiverLocation: undefined,
  sourceLocation: undefined
};

export const locationSolution: EventTypes.LocationSolution = {
  id: 'c083f904-5083-4532-8b22-94eaf2426c7d',
  name: 'location solution one',
  location: {
    latitudeDegrees: 1.1,
    longitudeDegrees: 1.1,
    depthKm: 1.1,
    time: 0
  },
  locationRestraint: {
    depthRestraintType: EventTypes.RestraintType.UNRESTRAINED,
    positionRestraintType: EventTypes.RestraintType.UNRESTRAINED,
    timeRestraintType: EventTypes.RestraintType.UNRESTRAINED
  },
  locationUncertainty: {
    stdDevOneObservation: 1.1,
    xx: 1.1,
    xy: 1.1,
    xz: 1.1,
    xt: 1.1,
    yy: 1.1,
    yz: 1.1,
    yt: 1.1,
    zz: 1.1,
    zt: 1.1,
    tt: 1.1,
    ellipses: [
      {
        scalingFactorType: EventTypes.ScalingFactorType.CONFIDENCE,
        kWeight: 0,
        confidenceLevel: 0.5,
        semiMajorAxisLengthKm: 0,
        semiMajorAxisTrendDeg: 0,
        semiMinorAxisLengthKm: 0,
        depthUncertaintyKm: 0,
        timeUncertainty: 'PT5S'
      }
    ],
    ellipsoids: []
  },
  featurePredictions: { featurePredictions: [featurePrediction] },
  locationBehaviors: [],
  networkMagnitudeSolutions: [
    {
      type: EventTypes.MagnitudeType.MB,
      magnitude: {
        value: 1.1,
        standardDeviation: 0.1,
        units: CommonTypes.Units.MAGNITUDE
      },
      magnitudeBehaviors: [
        {
          residual: 1.1,
          isDefining: true,
          weight: 1.1,
          stationMagnitudeSolution: {
            type: EventTypes.MagnitudeType.MB,
            model: EventTypes.MagnitudeModel.UNKNOWN,
            magnitude: {
              value: 1.1,
              standardDeviation: 0.1,
              units: CommonTypes.Units.MAGNITUDE
            },
            station: {
              name: 'STA',
              effectiveAt: 1634678314.635,
              location: {
                latitudeDegrees: 35.647,
                longitudeDegrees: 100,
                depthKm: 50,
                elevationKm: 10
              },
              type: StationType.HYDROACOUSTIC,
              channelGroups: [
                {
                  name: 'Test Channel Group',
                  effectiveAt: 0,
                  description: 'Channel Group Description',
                  location: {
                    latitudeDegrees: 35,
                    longitudeDegrees: -125,
                    depthKm: 100,
                    elevationKm: 5500
                  },
                  effectiveUntil: 253370764800,
                  type: ChannelGroupType.PROCESSING_GROUP,
                  channels: [
                    {
                      name: 'Real Channel Name One',
                      effectiveAt: 0,
                      configuredInputs: [],
                      station: {
                        name: 'STA'
                      },
                      canonicalName: 'Canonical Name One',
                      location: {
                        latitudeDegrees: 35,
                        longitudeDegrees: -125,
                        depthKm: 100,
                        elevationKm: 5500
                      },
                      response: {
                        id: '5dc3dce4-ae3c-3fb0-84ba-2f83fb5a89d2',
                        effectiveAt: 0,
                        calibration: {
                          calibrationPeriodSec: 14.5,
                          calibrationTimeShift: 'PT2S',
                          calibrationFactor: {
                            value: 1.2,
                            standardDeviation: 0.112,
                            units: CommonTypes.Units.SECONDS
                          }
                        },
                        fapResponse: undefined,
                        effectiveUntil: 253370764800
                      },
                      channelDataType: ChannelDataType.DIAGNOSTIC_SOH,
                      channelBandType: ChannelBandType.BROADBAND,
                      channelInstrumentType: ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
                      channelOrientationType: ChannelOrientationType.VERTICAL,
                      channelOrientationCode: 'Z',
                      nominalSampleRateHz: 65,
                      orientationAngles: {
                        horizontalAngleDeg: 65,
                        verticalAngleDeg: 135
                      },
                      processingDefinition: undefined,
                      processingMetadata: undefined,
                      description: 'Example description',
                      effectiveUntil: 1634680114.58,
                      units: CommonTypes.Units.NANOMETERS
                    }
                  ]
                }
              ],
              relativePositionsByChannel: {
                'Real Channel Name One': {
                  northDisplacementKm: 50,
                  eastDisplacementKm: 5,
                  verticalDisplacementKm: 10
                },
                'Real Channel Name Two': {
                  northDisplacementKm: 50,
                  eastDisplacementKm: 5,
                  verticalDisplacementKm: 10
                }
              },
              allRawChannels: [
                {
                  name: 'Real Channel Name One',
                  effectiveAt: 0,
                  configuredInputs: [],
                  station: {
                    name: 'STA'
                  },
                  canonicalName: 'Canonical Name One',
                  location: {
                    latitudeDegrees: 35,
                    longitudeDegrees: -125,
                    depthKm: 100,
                    elevationKm: 5500
                  },
                  response: {
                    id: '5dc3dce4-ae3c-3fb0-84ba-2f83fb5a89d2',
                    effectiveAt: 0,
                    calibration: {
                      calibrationPeriodSec: 14.5,
                      calibrationTimeShift: 'PT2S',
                      calibrationFactor: {
                        value: 1.2,
                        standardDeviation: 0.112,
                        units: CommonTypes.Units.SECONDS
                      }
                    },
                    fapResponse: undefined,
                    effectiveUntil: 253370764800
                  },
                  channelDataType: ChannelDataType.DIAGNOSTIC_SOH,
                  channelBandType: ChannelBandType.BROADBAND,
                  channelInstrumentType: ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
                  channelOrientationType: ChannelOrientationType.VERTICAL,
                  channelOrientationCode: 'Z',
                  nominalSampleRateHz: 65,
                  orientationAngles: {
                    horizontalAngleDeg: 65,
                    verticalAngleDeg: 135
                  },
                  processingDefinition: undefined,
                  processingMetadata: undefined,
                  description: 'Example description',
                  effectiveUntil: 1634680114.58,
                  units: CommonTypes.Units.NANOMETERS
                },
                {
                  name: 'Real Channel Name Two',
                  effectiveAt: 0,
                  configuredInputs: [],
                  station: {
                    name: 'STA'
                  },
                  canonicalName: 'Canonical Name Two',
                  location: {
                    latitudeDegrees: 35,
                    longitudeDegrees: -125,
                    depthKm: 100,
                    elevationKm: 5500
                  },
                  response: {
                    id: 'a9497b80-f49c-3bdc-9283-7c64b5562cd6',
                    effectiveAt: 0,
                    calibration: {
                      calibrationPeriodSec: 14.5,
                      calibrationTimeShift: 'PT2S',
                      calibrationFactor: {
                        value: 1.2,
                        standardDeviation: 0.112,
                        units: CommonTypes.Units.SECONDS
                      }
                    },
                    fapResponse: undefined,
                    effectiveUntil: 253370764800
                  },
                  channelDataType: ChannelDataType.DIAGNOSTIC_SOH,
                  channelBandType: ChannelBandType.BROADBAND,
                  channelInstrumentType: ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
                  channelOrientationType: ChannelOrientationType.VERTICAL,
                  channelOrientationCode: 'Z',
                  nominalSampleRateHz: 65,
                  orientationAngles: {
                    horizontalAngleDeg: 65,
                    verticalAngleDeg: 135
                  },
                  processingDefinition: undefined,
                  processingMetadata: undefined,
                  description: 'Example description',
                  effectiveUntil: 1634680114.582,
                  units: CommonTypes.Units.NANOMETERS
                }
              ],
              description: 'This is a test station',
              effectiveUntil: 253370764800
            },
            phase: CommonTypes.PhaseType.UNKNOWN,
            measurement: eventFeatureMeasurement
          }
        }
      ]
    }
  ]
};

export const channels = [
  {
    name: 'Real Channel Name One',
    effectiveAt: 0,
    configuredInputs: [],
    station: {
      name: 'STA'
    },
    canonicalName: 'Canonical Name One',
    location: {
      latitudeDegrees: 35,
      longitudeDegrees: -125,
      depthKm: 100,
      elevationKm: 5500
    },
    response: {
      id: '5dc3dce4-ae3c-3fb0-84ba-2f83fb5a89d2',
      effectiveAt: 0,
      calibration: {
        calibrationPeriodSec: 14.5,
        calibrationTimeShift: 'PT2S',
        calibrationFactor: {
          value: 1.2,
          standardDeviation: 0.112,
          units: CommonTypes.Units.SECONDS
        }
      },
      fapResponse: undefined,
      effectiveUntil: 253370764800
    },
    channelDataType: ChannelDataType.DIAGNOSTIC_SOH,
    channelBandType: ChannelBandType.BROADBAND,
    channelInstrumentType: ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
    channelOrientationType: ChannelOrientationType.VERTICAL,
    channelOrientationCode: 'Z',
    nominalSampleRateHz: 65,
    orientationAngles: {
      horizontalAngleDeg: 65,
      verticalAngleDeg: 135
    },
    processingDefinition: undefined,
    processingMetadata: undefined,
    description: 'Example description',
    effectiveUntil: 1634680114.58,
    units: CommonTypes.Units.NANOMETERS
  }
];
