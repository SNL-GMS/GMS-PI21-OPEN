/* eslint-disable @typescript-eslint/no-magic-numbers */
import {
  CommonTypes,
  EventTypes,
  LegacyEventTypes,
  SignalDetectionTypes,
  StationTypes
} from '@gms/common-model';
import {
  ChannelBandType,
  ChannelDataType,
  ChannelGroupType,
  ChannelInstrumentType,
  ChannelOrientationType
} from '@gms/common-model/lib/station-definitions/channel-definitions/channel-definitions';

// This is currently a combination of the legacy COI and the new COI and will need to be updated as new elements are implemented
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

export const eventData: EventTypes.Event = {
  name: 'Event one',
  id: '82ca9908-4272-4738-802b-f3d8f3099767',
  monitoringOrganization: 'Test Monitoring Org',
  eventHypotheses: [
    {
      name: 'hypothesis',
      id: {
        eventId: '82ca9908-4272-4738-802b-f3d8f3099767',
        hypothesisId: '44388ae5-5856-45e3-bf2f-4212a5af37c5'
      },
      rejected: false,
      parentEventHypotheses: [],
      locationSolutions: [
        {
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
          featurePredictions: { featurePredictions: [] },
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
                      type: StationTypes.StationType.HYDROACOUSTIC,
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
        }
      ],
      preferredLocationSolution: {
        id: 'c083f904-5083-4532-8b22-94eaf2426c7d'
      },
      associatedSignalDetectionHypotheses: []
    }
  ],
  rejectedSignalDetectionAssociations: [],
  preferredEventHypothesisByStage: [
    {
      stage: {
        name: 'AL1'
      },
      preferredBy: 'ANALYST1',
      preferred: {
        name: 'preferred one',
        id: {
          eventId: '82ca9908-4272-4738-802b-f3d8f3099767',
          hypothesisId: '44388ae5-5856-45e3-bf2f-4212a5af37c5'
        },
        rejected: false,
        parentEventHypotheses: [],
        locationSolutions: [
          {
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
            featurePredictions: { featurePredictions: [] },
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
                        type: StationTypes.StationType.HYDROACOUSTIC,
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
          }
        ],
        preferredLocationSolution: {
          id: 'c083f904-5083-4532-8b22-94eaf2426c7d'
        },
        associatedSignalDetectionHypotheses: []
      }
    }
  ],
  overallPreferred: {
    id: {
      eventId: '82ca9908-4272-4738-802b-f3d8f3099767',
      hypothesisId: '44388ae5-5856-45e3-bf2f-4212a5af37c5'
    },
    name: 'overall preferred',
    rejected: false,
    parentEventHypotheses: [],
    locationSolutions: [
      {
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
        featurePredictions: { featurePredictions: [] },
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
                    type: StationTypes.StationType.HYDROACOUSTIC,
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
      }
    ],
    preferredLocationSolution: {
      id: 'c083f904-5083-4532-8b22-94eaf2426c7d'
    },
    associatedSignalDetectionHypotheses: [
      {
        id: {
          id: '20cc9505-efe3-3068-b7d5-59196f37992c',
          signalDetectionId: null
        }
      }
    ]
  },
  finalEventHypothesisHistory: [
    {
      name: 'final event',
      id: {
        eventId: '82ca9908-4272-4738-802b-f3d8f3099767',
        hypothesisId: '44388ae5-5856-45e3-bf2f-4212a5af37c5'
      },
      rejected: false,
      parentEventHypotheses: [],
      locationSolutions: [
        {
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
          featurePredictions: { featurePredictions: [] },
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
                      type: StationTypes.StationType.HYDROACOUSTIC,
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
        }
      ],
      preferredLocationSolution: {
        id: 'c083f904-5083-4532-8b22-94eaf2426c7d'
      },
      associatedSignalDetectionHypotheses: []
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

export const legacyEventData: LegacyEventTypes.Event = {
  id: 'f43f58f9-6a87-40e3-95ac-44168325fc49',
  conflictingSdIds: [],
  status: LegacyEventTypes.EventStatus.ReadyForRefinement,
  modified: false,
  hasConflict: false,
  currentEventHypothesis: {
    processingStage: {
      id: '1'
    },
    eventHypothesis: {
      locationSolutionSets: [
        {
          count: 1,
          id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
          locationSolutions: [
            {
              id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
              location: {
                latitudeDegrees: 67.57425,
                longitudeDegrees: 33.59468,
                depthKm: 0,
                time: 1274399850.081
              },
              locationToStationDistances: [],
              snapshots: [],
              featurePredictions: [],
              locationRestraint: {
                depthRestraintType: LegacyEventTypes.DepthRestraintType.FIXED_AT_SURFACE,
                depthRestraintKm: null,
                latitudeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
                latitudeRestraintDegrees: null,
                longitudeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
                longitudeRestraintDegrees: null,
                timeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
                timeRestraint: null
              },
              locationUncertainty: {
                xy: 163.665,
                xz: -1,
                xt: -26.6202,
                yy: 400.817,
                yz: -1,
                yt: 20.2312,
                zz: -1,
                zt: -1,
                tt: 6.6189,
                stDevOneObservation: 1.0484,
                ellipses: [
                  {
                    scalingFactorType: LegacyEventTypes.ScalingFactorType.CONFIDENCE,
                    kWeight: 0,
                    confidenceLevel: 0.9,
                    majorAxisLength: '49.1513',
                    majorAxisTrend: 37.23,
                    minorAxisLength: '29.2083',
                    minorAxisTrend: -1,
                    depthUncertainty: -1,
                    timeUncertainty: 'PT4.235S'
                  }
                ],
                ellipsoids: []
              },
              locationBehaviors: [
                {
                  residual: 1.28,
                  weight: 0.734,
                  defining: false,
                  signalDetectionId: '00000000-0000-0000-0000-000000000000',
                  featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE
                }
              ],
              locationType: 'standard',
              networkMagnitudeSolutions: [
                {
                  magnitude: 3.6532,
                  magnitudeType: LegacyEventTypes.MagnitudeType.MB,
                  uncertainty: 0.1611,
                  networkMagnitudeBehaviors: [
                    {
                      defining: true,
                      stationMagnitudeSolution: {
                        type: LegacyEventTypes.MagnitudeType.MB,
                        model: LegacyEventTypes.MagnitudeModel.QFVC1,
                        magnitude: 2.9357,
                        magnitudeUncertainty: 0.4228,
                        modelCorrection: 1,
                        stationCorrection: 1,
                        featureMeasurement: {
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
                          measurementValue: {
                            value: CommonTypes.PhaseType.Pn,
                            confidence: 0.5
                          },
                          featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.PHASE,
                          snr: null
                        },
                        stationName: 'e03b9548-fda0-393e-9606-af73bf25f420',
                        phase: CommonTypes.PhaseType.Pn
                      },
                      residual: -0.6995,
                      weight: 1
                    },
                    {
                      defining: true,
                      stationMagnitudeSolution: {
                        type: LegacyEventTypes.MagnitudeType.MB,
                        model: LegacyEventTypes.MagnitudeModel.QFVC1,
                        magnitude: 3.7008,
                        magnitudeUncertainty: 0.281,
                        modelCorrection: 1,
                        stationCorrection: 1,
                        featureMeasurement: {
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
                        },
                        stationName: 'f7567432-5db0-3a03-a4cd-20db18ded4e7',
                        phase: CommonTypes.PhaseType.P
                      },
                      residual: 0.0657,
                      weight: 1
                    },
                    {
                      defining: true,
                      stationMagnitudeSolution: {
                        type: LegacyEventTypes.MagnitudeType.MB,
                        model: LegacyEventTypes.MagnitudeModel.QFVC1,
                        magnitude: 3.264,
                        magnitudeUncertainty: 0.403,
                        modelCorrection: 1,
                        stationCorrection: 1,
                        featureMeasurement: {
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
                            value: CommonTypes.PhaseType.Pn,
                            confidence: 0.5
                          },
                          featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.PHASE
                        },
                        stationName: '4dd5d739-f4d6-37d6-82c0-099bfaf54b73',
                        phase: CommonTypes.PhaseType.Pn
                      },
                      residual: -0.3711,
                      weight: 1
                    },
                    {
                      defining: true,
                      stationMagnitudeSolution: {
                        type: LegacyEventTypes.MagnitudeType.MB,
                        model: LegacyEventTypes.MagnitudeModel.QFVC1,
                        magnitude: 3.5844,
                        magnitudeUncertainty: 0.3449,
                        modelCorrection: 1,
                        stationCorrection: 1,
                        featureMeasurement: {
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
                        },
                        stationName: '3695d63e-7071-3f74-b6a5-a92681fd2567',
                        phase: CommonTypes.PhaseType.P
                      },
                      residual: -0.0507,
                      weight: 1
                    },
                    {
                      defining: true,
                      stationMagnitudeSolution: {
                        type: LegacyEventTypes.MagnitudeType.MB,
                        model: LegacyEventTypes.MagnitudeModel.QFVC1,
                        magnitude: 3.5574,
                        magnitudeUncertainty: 0.3449,
                        modelCorrection: 1,
                        stationCorrection: 1,
                        featureMeasurement: {
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
                        },
                        stationName: '6856c4c5-54cf-377d-87dc-ed322f36c0bb',
                        phase: CommonTypes.PhaseType.P
                      },
                      residual: -0.0777,
                      weight: 1
                    },
                    {
                      defining: true,
                      stationMagnitudeSolution: {
                        type: LegacyEventTypes.MagnitudeType.MB,
                        model: LegacyEventTypes.MagnitudeModel.QFVC1,
                        magnitude: 4.1441,
                        magnitudeUncertainty: 0.281,
                        modelCorrection: 1,
                        stationCorrection: 1,
                        featureMeasurement: {
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
                        },
                        stationName: 'e7437e38-94ba-34d4-a95c-0a9e3983c689',
                        phase: CommonTypes.PhaseType.P
                      },
                      residual: 0.5089,
                      weight: 1
                    }
                  ]
                }
              ]
            }
          ]
        }
      ],
      id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
      rejected: false,
      event: {
        id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
        status: LegacyEventTypes.EventStatus.ReadyForRefinement
      },
      preferredLocationSolution: {
        locationSolution: {
          id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
          location: {
            latitudeDegrees: 67.57425,
            longitudeDegrees: 33.59468,
            depthKm: 0,
            time: 1274399850.081
          },
          locationToStationDistances: [],
          snapshots: [],
          featurePredictions: [],
          locationRestraint: {
            depthRestraintType: LegacyEventTypes.DepthRestraintType.FIXED_AT_SURFACE,
            depthRestraintKm: null,
            latitudeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
            latitudeRestraintDegrees: null,
            longitudeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
            longitudeRestraintDegrees: null,
            timeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
            timeRestraint: null
          },
          locationUncertainty: {
            xy: 163.665,
            xz: -1,
            xt: -26.6202,
            yy: 400.817,
            yz: -1,
            yt: 20.2312,
            zz: -1,
            zt: -1,
            tt: 6.6189,
            stDevOneObservation: 1.0484,
            ellipses: [
              {
                scalingFactorType: LegacyEventTypes.ScalingFactorType.CONFIDENCE,
                kWeight: 0,
                confidenceLevel: 0.9,
                majorAxisLength: '49.1513',
                majorAxisTrend: 37.23,
                minorAxisLength: '29.2083',
                minorAxisTrend: -1,
                depthUncertainty: -1,
                timeUncertainty: 'PT4.235S'
              }
            ],
            ellipsoids: []
          },
          locationBehaviors: [
            {
              residual: 1.28,
              weight: 0.734,
              defining: false,
              featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE,
              signalDetectionId: '00000000-0000-0000-0000-000000000000'
            }
          ],
          locationType: 'standard',
          networkMagnitudeSolutions: [
            {
              magnitude: 3.6532,
              magnitudeType: LegacyEventTypes.MagnitudeType.MB,
              uncertainty: 0.1611,
              networkMagnitudeBehaviors: [
                {
                  defining: true,
                  stationMagnitudeSolution: {
                    type: LegacyEventTypes.MagnitudeType.MB,
                    model: LegacyEventTypes.MagnitudeModel.QFVC1,
                    magnitude: 2.9357,
                    magnitudeUncertainty: 0.4228,
                    modelCorrection: 1,
                    stationCorrection: 1,
                    featureMeasurement: {
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
                        value: CommonTypes.PhaseType.Pn,
                        confidence: 0.5
                      },
                      featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.PHASE
                    },
                    stationName: 'e03b9548-fda0-393e-9606-af73bf25f420',
                    phase: CommonTypes.PhaseType.Pn
                  },
                  residual: -0.6995,
                  weight: 1
                },
                {
                  defining: true,
                  stationMagnitudeSolution: {
                    type: LegacyEventTypes.MagnitudeType.MB,
                    model: LegacyEventTypes.MagnitudeModel.QFVC1,
                    magnitude: 3.7008,
                    magnitudeUncertainty: 0.281,
                    modelCorrection: 1,
                    stationCorrection: 1,
                    featureMeasurement: {
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
                    },
                    stationName: 'f7567432-5db0-3a03-a4cd-20db18ded4e7',
                    phase: CommonTypes.PhaseType.P
                  },
                  residual: 0.0657,
                  weight: 1
                },
                {
                  defining: true,
                  stationMagnitudeSolution: {
                    type: LegacyEventTypes.MagnitudeType.MB,
                    model: LegacyEventTypes.MagnitudeModel.QFVC1,
                    magnitude: 3.264,
                    magnitudeUncertainty: 0.403,
                    modelCorrection: 1,
                    stationCorrection: 1,
                    featureMeasurement: {
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
                        value: CommonTypes.PhaseType.Pn,
                        confidence: 0.5
                      },
                      featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.PHASE
                    },
                    stationName: '4dd5d739-f4d6-37d6-82c0-099bfaf54b73',
                    phase: CommonTypes.PhaseType.Pn
                  },
                  residual: -0.3711,
                  weight: 1
                },
                {
                  defining: true,
                  stationMagnitudeSolution: {
                    type: LegacyEventTypes.MagnitudeType.MB,
                    model: LegacyEventTypes.MagnitudeModel.QFVC1,
                    magnitude: 3.5844,
                    magnitudeUncertainty: 0.3449,
                    modelCorrection: 1,
                    stationCorrection: 1,
                    featureMeasurement: {
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
                    },
                    stationName: '3695d63e-7071-3f74-b6a5-a92681fd2567',
                    phase: CommonTypes.PhaseType.P
                  },
                  residual: -0.0507,
                  weight: 1
                },
                {
                  defining: true,
                  stationMagnitudeSolution: {
                    type: LegacyEventTypes.MagnitudeType.MB,
                    model: LegacyEventTypes.MagnitudeModel.QFVC1,
                    magnitude: 3.5574,
                    magnitudeUncertainty: 0.3449,
                    modelCorrection: 1,
                    stationCorrection: 1,
                    featureMeasurement: {
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
                    },
                    stationName: '6856c4c5-54cf-377d-87dc-ed322f36c0bb',
                    phase: CommonTypes.PhaseType.P
                  },
                  residual: -0.0777,
                  weight: 1
                },
                {
                  defining: true,
                  stationMagnitudeSolution: {
                    type: LegacyEventTypes.MagnitudeType.MB,
                    model: LegacyEventTypes.MagnitudeModel.QFVC1,
                    magnitude: 4.1441,
                    magnitudeUncertainty: 0.281,
                    modelCorrection: 1,
                    stationCorrection: 1,
                    featureMeasurement: {
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
                    },
                    stationName: 'e7437e38-94ba-34d4-a95c-0a9e3983c689',
                    phase: CommonTypes.PhaseType.P
                  },
                  residual: 0.5089,
                  weight: 1
                }
              ]
            }
          ]
        }
      },
      associationsMaxArrivalTime: 1274399905.01,
      signalDetectionAssociations: [
        {
          id: '527562d9-027e-4b88-941e-91543763b7a4',
          rejected: false,
          eventHypothesisId: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
          signalDetectionHypothesis: {
            id: '1c8f8122-0056-3ed9-9304-ddea79de2393',
            rejected: false,
            parentSignalDetectionId: '1c8f8122-0056-3ed9-9304-ddea79de2393'
          }
        }
      ]
    }
  }
};
