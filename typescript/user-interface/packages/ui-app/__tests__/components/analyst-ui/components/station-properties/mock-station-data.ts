import type { ResponseTypes } from '@gms/common-model';
import { ChannelTypes, CommonTypes, ProcessingStationTypes, StationTypes } from '@gms/common-model';
import { toEpochSeconds } from '@gms/common-util';

interface MockDataType {
  station: StationTypes.Station;
  startTime: number;
  endTime: number;
}

const exampleChannel: ChannelTypes.Channel = {
  station: {
    name: 'BPPPP.BPP01'
  },
  response: {
    fapResponse: ({
      // TODO no id currently in FAP response
      id: '966deee1-545e-44ed-a346-b029ab111151'
    } as unknown) as ResponseTypes.FrequencyAmplitudePhase,
    calibration: {
      calibrationPeriodSec: 14.5,
      calibrationTimeShift: 'PT2S',
      calibrationFactor: {
        value: 1.2,
        standardDeviation: 0.112,
        units: CommonTypes.Units.SECONDS
      }
    },
    effectiveAt: toEpochSeconds('2021-04-20T16:11:30.674083Z'),
    effectiveUntil: toEpochSeconds('2021-04-20T16:11:30.674083Z'),
    id: '5dc3dce4-ae3c-3fb0-84ba-2f83fb5a89d2'
  },
  processingMetadata: new Map<ChannelTypes.ChannelProcessingMetadataType, any>([
    [ChannelTypes.ChannelProcessingMetadataType.CHANNEL_GROUP, 'CHANNEL_GROUP']
  ]),
  channelBandType: ChannelTypes.ChannelBandType.BROADBAND, // 'BROADBAND',
  channelInstrumentType: ChannelTypes.ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
  channelOrientationType: ChannelTypes.ChannelOrientationType.VERTICAL,
  channelOrientationCode: 'Z',
  channelDataType: ProcessingStationTypes.ChannelDataType.DIAGNOSTIC_SOH,
  nominalSampleRateHz: 66.0,
  orientationAngles: {
    horizontalAngleDeg: 65.0,
    verticalAngleDeg: 135.0
  },
  configuredInputs: [],
  processingDefinition: new Map<string, any>(),
  description: 'Example description',
  canonicalName: 'Canonical Name One',
  location: {
    latitudeDegrees: 35.0,
    longitudeDegrees: -125.0,
    depthKm: 100.0,
    elevationKm: 5500.0
  },
  units: CommonTypes.Units.NANOMETERS,
  effectiveAt: toEpochSeconds('1970-01-01T00:00:00Z'),
  effectiveUntil: toEpochSeconds('2021-04-20T16:11:30.674083Z'),
  name: 'Real Channel Name One'
};

const channel1: ChannelTypes.Channel = {
  ...exampleChannel,
  name: 'BPPPP.BPP01.CNN',
  canonicalName: 'Canonical Name One',
  location: {
    latitudeDegrees: 35.45,
    longitudeDegrees: -125.2345,
    depthKm: 100.0,
    elevationKm: 5500.0
  },
  response: {
    fapResponse: ({
      // TODO no id currently in FAP response
      id: '966deee1-545e-44ed-a346-b029ab111151'
    } as unknown) as ResponseTypes.FrequencyAmplitudePhase,
    calibration: {
      calibrationPeriodSec: 14.5,
      calibrationTimeShift: 'PT2S',
      calibrationFactor: {
        value: 1.2,
        standardDeviation: 0.112,
        units: CommonTypes.Units.SECONDS
      }
    },
    effectiveAt: toEpochSeconds('2021-04-20T16:11:30.674083Z'),
    effectiveUntil: toEpochSeconds('2021-04-20T16:11:30.674083Z'),
    id: '5dc3dce4-ae3c-3fb0-84ba-2f83fb5a89d2'
  }
};

const channel2: ChannelTypes.Channel = {
  ...exampleChannel,
  name: 'BPPPP.BPP01.BBC',
  canonicalName: 'Canonical Name Two',
  location: {
    latitudeDegrees: 35.0,
    longitudeDegrees: -125.0,
    depthKm: 100.0,
    elevationKm: 5500.0
  },
  response: {
    fapResponse: ({
      // TODO no id currently in FAP response
      id: '966deee1-545e-44ed-a346-b029ab1221151'
    } as unknown) as ResponseTypes.FrequencyAmplitudePhase,
    calibration: {
      calibrationPeriodSec: 14.5,
      calibrationTimeShift: 'PT2S',
      calibrationFactor: {
        value: 1.2,
        standardDeviation: 0.112,
        units: CommonTypes.Units.SECONDS
      }
    },
    effectiveAt: toEpochSeconds('2021-04-20T16:11:30.674083Z'),
    effectiveUntil: toEpochSeconds('2021-04-20T16:11:30.674083Z')
  }
};

const exampleChannelGroup: ChannelTypes.ChannelGroup = {
  name: 'Test Channel Group EXAMPLE',
  effectiveAt: toEpochSeconds('1970-01-01T00:00:00Z'),
  effectiveUntil: toEpochSeconds('1970-01-01T00:00:00Z'),
  description: 'Channel Group Description',
  location: {
    latitudeDegrees: 35.03456,
    longitudeDegrees: -125.05678,
    depthKm: 101.0,
    elevationKm: 5500.0678
  },
  type: ChannelTypes.ChannelGroupType.PROCESSING_GROUP,
  channels: [channel1, channel2]
};

const channelGroup1: ChannelTypes.ChannelGroup = {
  ...exampleChannelGroup,
  name: 'Test Channel Group 1',
  location: {
    latitudeDegrees: 35.03456,
    longitudeDegrees: -125.05678,
    depthKm: 101.0,
    elevationKm: 5500.0678
  }
};
const channelGroup2: ChannelTypes.ChannelGroup = {
  ...exampleChannelGroup,
  name: 'Test Channel Group 2',
  location: {
    latitudeDegrees: 35.0,
    longitudeDegrees: -125.0,
    depthKm: 100.0,
    elevationKm: 5600.0
  }
};

export const data: MockDataType = {
  station: {
    name: 'STA',
    effectiveAt: toEpochSeconds('2021-04-20T16:11:31.118870Z'),
    effectiveUntil: toEpochSeconds('2021-04-20T16:11:31.118870Z'),
    relativePositionsByChannel: {
      'BPPPP.BPP01.CNN': {
        northDisplacementKm: 50,
        eastDisplacementKm: 5,
        verticalDisplacementKm: 10
      },
      'BPPPP.BPP01.BBC': {
        northDisplacementKm: 30.0,
        eastDisplacementKm: 2.5,
        verticalDisplacementKm: 5.0
      }
    },
    channelGroups: [channelGroup1, channelGroup2],
    allRawChannels: [channel1, channel2],
    description: 'This is a test station',
    location: {
      latitudeDegrees: 35.647,
      longitudeDegrees: 100.0,
      depthKm: 50.0,
      elevationKm: 10.0
    },
    type: StationTypes.StationType.HYDROACOUSTIC
  },
  startTime: toEpochSeconds('1970-01-01T00:00:00Z'),
  endTime: toEpochSeconds('1970-01-01T00:00:05Z')
};

export const effectiveTimesMockData = [
  '1970-01-01T00:00:00Z',
  '1970-01-01T00:00:30Z',
  '1970-01-01T00:01:00Z',
  '1970-01-01T00:01:30Z',
  '1970-01-01T00:02:00Z',
  '1970-01-01T00:02:30Z',
  '1970-01-01T00:03:00Z',
  '1970-01-01T00:03:30Z',
  '1970-01-01T00:04:00Z',
  '1970-01-01T00:04:30Z',
  '1970-01-01T00:05:00Z'
];
