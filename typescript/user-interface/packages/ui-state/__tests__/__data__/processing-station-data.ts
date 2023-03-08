export const processingStations = [
  {
    name: 'AAK',
    type: 'SEISMIC_3_COMPONENT',
    description: 'Ala-Archa,_Kyrgyzstan',
    relativePositionsByChannel: {
      'AAK.AAK.BHN': {
        northDisplacementKm: 0.0,
        eastDisplacementKm: 0.0,
        verticalDisplacementKm: 0.0
      },
      'AAK.AAK.BHE': {
        northDisplacementKm: 0.0,
        eastDisplacementKm: 0.0,
        verticalDisplacementKm: 0.0
      },
      'AAK.AAK.BHZ': {
        northDisplacementKm: 0.0,
        eastDisplacementKm: 0.0,
        verticalDisplacementKm: 0.0
      }
    },
    location: {
      latitudeDegrees: 42.6391,
      longitudeDegrees: 74.4942,
      depthKm: 0.0,
      elevationKm: 1.645
    },
    channelGroups: [
      {
        name: 'AAK',
        description: 'Ala-Archa,_Kyrgyzstan',
        location: {
          latitudeDegrees: 42.6391,
          longitudeDegrees: 74.4942,
          depthKm: 0.0,
          elevationKm: 1.645
        },
        type: 'SITE_GROUP',
        channels: [
          {
            name: 'AAK.AAK.BHE',
            canonicalName: 'AAK.AAK.BHE',
            description:
              'Raw Channel created from ReferenceChannel 8273f695-cf66-38d4-8ed6-6817a1709bc1 with version 4f1d08d8-cb26-3c52-ac94-4ce89529ac65',
            station: 'AAK',
            channelDataType: 'SEISMIC',
            channelBandType: 'BROADBAND',
            channelInstrumentType: 'HIGH_GAIN_SEISMOMETER',
            channelOrientationType: 'EAST_WEST',
            channelOrientationCode: 'E',
            units: 'COUNTS_PER_NANOMETER',
            nominalSampleRateHz: 40.0,
            location: {
              latitudeDegrees: 42.6391,
              longitudeDegrees: 74.4942,
              depthKm: 0.03,
              elevationKm: 1.645
            },
            orientationAngles: {
              horizontalAngleDeg: 90.0,
              verticalAngleDeg: 90.0
            },
            configuredInputs: [],
            processingDefinition: {},
            processingMetadata: {
              CHANNEL_GROUP: 'AAK'
            }
          },
          {
            name: 'AAK.AAK.BHN',
            canonicalName: 'AAK.AAK.BHN',
            description:
              'Raw Channel created from ReferenceChannel 455fe77f-4313-34a9-81c4-9d9a2c7fd455 with version 3dd7375e-d667-3eeb-bd6c-ff2420b2ce5a',
            station: 'AAK',
            channelDataType: 'SEISMIC',
            channelBandType: 'BROADBAND',
            channelInstrumentType: 'HIGH_GAIN_SEISMOMETER',
            channelOrientationType: 'NORTH_SOUTH',
            channelOrientationCode: 'N',
            units: 'COUNTS_PER_NANOMETER',
            nominalSampleRateHz: 40.0,
            location: {
              latitudeDegrees: 42.6391,
              longitudeDegrees: 74.4942,
              depthKm: 0.03,
              elevationKm: 1.645
            },
            orientationAngles: {
              horizontalAngleDeg: 0.0,
              verticalAngleDeg: 90.0
            },
            configuredInputs: [],
            processingDefinition: {},
            processingMetadata: {
              CHANNEL_GROUP: 'AAK'
            }
          },
          {
            name: 'AAK.AAK.BHZ',
            canonicalName: 'AAK.AAK.BHZ',
            description:
              'Raw Channel created from ReferenceChannel 23290db1-05fd-3fd2-b927-c1b350c35ef7 with version 391b21b2-67f2-33c5-a18e-10abb851582a',
            station: 'AAK',
            channelDataType: 'SEISMIC',
            channelBandType: 'BROADBAND',
            channelInstrumentType: 'HIGH_GAIN_SEISMOMETER',
            channelOrientationType: 'VERTICAL',
            channelOrientationCode: 'Z',
            units: 'COUNTS_PER_NANOMETER',
            nominalSampleRateHz: 40.0,
            location: {
              latitudeDegrees: 42.6391,
              longitudeDegrees: 74.4942,
              depthKm: 0.03,
              elevationKm: 1.645
            },
            orientationAngles: {
              horizontalAngleDeg: -1.0,
              verticalAngleDeg: 0.0
            },
            configuredInputs: [],
            processingDefinition: {},
            processingMetadata: {
              CHANNEL_GROUP: 'AAK'
            }
          }
        ]
      }
    ],
    channels: [
      {
        name: 'AAK.AAK.BHE',
        canonicalName: 'AAK.AAK.BHE',
        description:
          'Raw Channel created from ReferenceChannel 8273f695-cf66-38d4-8ed6-6817a1709bc1 with version 4f1d08d8-cb26-3c52-ac94-4ce89529ac65',
        station: 'AAK',
        channelDataType: 'SEISMIC',
        channelBandType: 'BROADBAND',
        channelInstrumentType: 'HIGH_GAIN_SEISMOMETER',
        channelOrientationType: 'EAST_WEST',
        channelOrientationCode: 'E',
        units: 'COUNTS_PER_NANOMETER',
        nominalSampleRateHz: 40.0,
        location: {
          latitudeDegrees: 42.6391,
          longitudeDegrees: 74.4942,
          depthKm: 0.03,
          elevationKm: 1.645
        },
        orientationAngles: {
          horizontalAngleDeg: 90.0,
          verticalAngleDeg: 90.0
        },
        configuredInputs: [],
        processingDefinition: {},
        processingMetadata: {
          CHANNEL_GROUP: 'AAK'
        }
      },
      {
        name: 'AAK.AAK.BHN',
        canonicalName: 'AAK.AAK.BHN',
        description:
          'Raw Channel created from ReferenceChannel 455fe77f-4313-34a9-81c4-9d9a2c7fd455 with version 3dd7375e-d667-3eeb-bd6c-ff2420b2ce5a',
        station: 'AAK',
        channelDataType: 'SEISMIC',
        channelBandType: 'BROADBAND',
        channelInstrumentType: 'HIGH_GAIN_SEISMOMETER',
        channelOrientationType: 'NORTH_SOUTH',
        channelOrientationCode: 'N',
        units: 'COUNTS_PER_NANOMETER',
        nominalSampleRateHz: 40.0,
        location: {
          latitudeDegrees: 42.6391,
          longitudeDegrees: 74.4942,
          depthKm: 0.03,
          elevationKm: 1.645
        },
        orientationAngles: {
          horizontalAngleDeg: 0.0,
          verticalAngleDeg: 90.0
        },
        configuredInputs: [],
        processingDefinition: {},
        processingMetadata: {
          CHANNEL_GROUP: 'AAK'
        }
      },
      {
        name: 'AAK.AAK.BHZ',
        canonicalName: 'AAK.AAK.BHZ',
        description:
          'Raw Channel created from ReferenceChannel 23290db1-05fd-3fd2-b927-c1b350c35ef7 with version 391b21b2-67f2-33c5-a18e-10abb851582a',
        station: 'AAK',
        channelDataType: 'SEISMIC',
        channelBandType: 'BROADBAND',
        channelInstrumentType: 'HIGH_GAIN_SEISMOMETER',
        channelOrientationType: 'VERTICAL',
        channelOrientationCode: 'Z',
        units: 'COUNTS_PER_NANOMETER',
        nominalSampleRateHz: 40.0,
        location: {
          latitudeDegrees: 42.6391,
          longitudeDegrees: 74.4942,
          depthKm: 0.03,
          elevationKm: 1.645
        },
        orientationAngles: {
          horizontalAngleDeg: -1.0,
          verticalAngleDeg: 0.0
        },
        configuredInputs: [],
        processingDefinition: {},
        processingMetadata: {
          CHANNEL_GROUP: 'AAK'
        }
      }
    ]
  }
];

export const processingStationGroups = [
  {
    name: 'CD1.1',
    description: 'Stations with a CD1.1 format',
    stations: processingStations
  }
];
