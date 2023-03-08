import type { QcMaskTypes } from '@gms/common-model';

export const overlappingQcMaskData: QcMaskTypes.QcMask[] = [
  {
    id: '27',
    channelName: 'ASAR.AS03.SHZ',
    currentVersion: {
      version: '1',
      channelSegmentIds: ['AS03/SHZ'],
      category: 'WAVEFORM_QUALITY',
      type: 'CALIBRATION',
      startTime: 1274395161,
      endTime: 1274395221,
      rationale: 'Hedgehog crossed the road upside down'
    },
    qcMaskVersions: [
      {
        version: '0',
        channelSegmentIds: ['AS03/SHZ'],
        category: 'WAVEFORM_QUALITY',
        type: 'CALIBRATION',
        startTime: 1274395156,
        endTime: 1274395216,
        rationale: 'Hedgehog crossed the road upside down'
      },
      {
        version: '1',
        channelSegmentIds: ['AS03/SHZ'],
        category: 'WAVEFORM_QUALITY',
        type: 'CALIBRATION',
        startTime: 1274395161,
        endTime: 1274395221,
        rationale: 'Hedgehog crossed the road upside down'
      }
    ]
  },
  {
    id: '28',
    channelName: 'ASAR.AS03.SHZ',
    currentVersion: {
      version: '1',
      channelSegmentIds: ['AS03/SHZ'],
      category: 'ANALYST_DEFINED',
      type: 'CALIBRATION',
      startTime: 1274395161,
      endTime: 1274395221,
      rationale: 'Hedgehog crossed the road upside down'
    },
    qcMaskVersions: [
      {
        version: '0',
        channelSegmentIds: ['AS03/SHZ'],
        category: 'ANALYST_DEFINED',
        type: 'CALIBRATION',
        startTime: 1274395156,
        endTime: 1274395216,
        rationale: 'Hedgehog crossed the road upside down'
      },
      {
        version: '1',
        channelSegmentIds: ['AS03/SHZ'],
        category: 'ANALYST_DEFINED',
        type: 'CALIBRATION',
        startTime: 1274395161,
        endTime: 1274395221,
        rationale: 'Hedgehog crossed the road upside down'
      }
    ]
  }
];

export const qcMaskData = {
  id: '1',
  channelName: 'ASAR.AS02.SHZ',
  currentVersion: {
    version: '1',
    channelSegmentIds: ['AS02/SHZ'],
    category: 'ANALYST_DEFINED',
    type: 'SENSOR_PROBLEM',
    startTime: 1274392801,
    endTime: 1274392861,
    rationale: 'Hedgehog crossed the road upside down'
  },
  qcMaskVersions: [
    {
      version: '0',
      channelSegmentIds: ['AS02/SHZ'],
      category: 'ANALYST_DEFINED',
      type: 'SENSOR_PROBLEM',
      startTime: 1274392796,
      endTime: 1274392856,
      rationale: 'Hedgehog crossed the road upside down'
    },
    {
      version: '1',
      channelSegmentIds: ['AS02/SHZ'],
      category: 'ANALYST_DEFINED',
      type: 'SENSOR_PROBLEM',
      startTime: 1274392801,
      endTime: 1274392861,
      rationale: 'Hedgehog crossed the road upside down'
    }
  ]
};
