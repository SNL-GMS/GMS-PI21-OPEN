import type { ChannelSegmentTypes } from '@gms/common-model';
import type { WeavessTypes } from '@gms/weavess-core';

export interface UiChannelSegment {
  channelSegmentDescriptor: ChannelSegmentTypes.ChannelSegmentDescriptor;
  channelSegment: WeavessTypes.ChannelSegment;
}
