/* eslint-disable @typescript-eslint/no-loss-of-precision */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { UiChannelSegment } from '@gms/ui-state';
import { WeavessTypes } from '@gms/weavess-core';

export const weavessChannelSegment: WeavessTypes.ChannelSegment = {
  channelName: 'AAK.AAK.BHZ',
  wfFilterId: 'unfiltered',
  isSelected: false,
  description: 'unfiltered',
  descriptionLabelColor: '#f5f8fa',
  dataSegments: [
    {
      displayType: [WeavessTypes.DisplayType.LINE],
      color: '#4580e6',
      pointSize: 1,
      data: {
        startTimeSecs: 1274391900,
        endTimeSecs: 1274399099.975,
        sampleRate: 40,
        values: [
          -12.501736640930176,
          233.5541229248047,
          -12.501389503479004,
          237.58853149414062,
          -12.501041412353516,
          249.02859497070312,
          -12.500694274902344,
          240.24130249023438,
          -12.500347137451172,
          227.83409118652344
        ]
      }
    },
    {
      displayType: [WeavessTypes.DisplayType.LINE],
      color: '#4580e6',
      pointSize: 1,
      data: {
        startTimeSecs: 1274399100,
        endTimeSecs: 1274400898.975,
        sampleRate: 40,
        values: [
          87.51215362548828,
          226.728759765625,
          87.51250457763672,
          228.63543701171875,
          87.51284790039062,
          228.13804626464844,
          87.51319885253906,
          222.72198486328125,
          87.51354217529297,
          232.2830047607422
        ]
      }
    }
  ],
  channelSegmentBoundaries: {
    topMax: 307.306593,
    bottomMax: 154.606635,
    channelAvg: 230.31431241288792,
    samplesCount: 179980,
    offset: 307.306593,
    channelSegmentId: 'unfiltered'
  }
};

export const uiChannelSegment: UiChannelSegment = {
  channelSegment: weavessChannelSegment,
  channelSegmentDescriptor: {
    channel: {
      name: 'AAK.AAK.BHZ',
      effectiveAt: 1274391900
    },
    startTime: 1274391900,
    endTime: 1274399099,
    creationTime: 1274391900
  }
};
