import type { ChannelSegmentTypes, WaveformTypes } from '@gms/common-model';
import { WeavessTypes, WeavessUtil } from '@gms/weavess-core';

import { WaveformStore } from '../worker-store/waveform-store';

const SHOULD_PARALLELIZE = false;

/**
 * Make a DataBySampleRate object from the provided waveform
 *
 * @param wave the waveform from which to get the data
 * @returns the DataBySampleRate object
 */
export const getDataBySampleRate = (
  wave: WaveformTypes.Waveform
): WeavessTypes.DataBySampleRate => ({
  sampleRate: wave.sampleRateHz,
  startTimeSecs: wave.startTime,
  endTimeSecs: wave.endTime,
  values: wave.samples
});

const calculateAndStorePositionBuffer = async (
  id: string,
  wave: WaveformTypes.Waveform,
  domain: WeavessTypes.TimeRange
): Promise<string> => {
  if (!(await WaveformStore.has(id))) {
    const positionBufferPromise = Promise.resolve(
      WeavessUtil.convertToPositionBuffer(getDataBySampleRate(wave), domain)
    );
    if (SHOULD_PARALLELIZE) {
      await WaveformStore.store(id, await positionBufferPromise);
    } else {
      await WaveformStore.store(id, positionBufferPromise);
    }
  }
  return Promise.resolve(id);
};

/**
 * Generate a unique id based on the data fields of the channel segment and waveform
 */
const generateUniqueId = (channelSegment, wave, domain: WeavessTypes.TimeRange) => {
  return JSON.stringify({
    domain,
    id: channelSegment.id,
    type: channelSegment.timeseriesType,
    waveform: {
      type: wave.type,
      startTime: wave.startTime,
      endTime: wave.endTime,
      sampleCount: wave.sampleCount,
      sampleRateHz: wave.sampleRateHz
    }
  });
};

/**
 * Converts the ChannelSegmentTypes.ChannelSegment waveform to a WeavessTypes.DataSegment[]
 *
 * @param channelSegment returned from waveform query
 * @param domain TimeRange of Current Interval
 * @param semanticColors Color for raw waveform
 * @returns object with list of dataSegments, description, showLabel (boolean), channelSegmentBoundaries
 */
export async function formatAndStoreDataSegments(
  channelSegment: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>,
  domain: WeavessTypes.TimeRange,
  waveformColor: string
): Promise<WeavessTypes.DataSegment[]> {
  // If there was no raw data and no filtered data return empty data segments
  if (!channelSegment || !channelSegment.timeseries || channelSegment.timeseries.length === 0) {
    return [];
  }

  return Promise.all(
    channelSegment.timeseries.map<Promise<WeavessTypes.DataSegment>>(
      async (wave: WaveformTypes.Waveform) => {
        // generate a unique id based on the data fields
        const id = generateUniqueId(channelSegment, wave, domain);
        const dataSegId = await calculateAndStorePositionBuffer(id, wave, domain);
        return {
          displayType: [WeavessTypes.DisplayType.LINE],
          color: waveformColor,
          pointSize: 1,
          data: {
            startTimeSecs: wave.startTime,
            endTimeSecs: wave.endTime,
            sampleRate: wave.sampleRateHz,
            values: undefined, // vertices
            id: dataSegId,
            domainTimeRange: domain
          }
        };
      }
    )
  );
}
