/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import type { SohTypes } from '@gms/common-model';
import type { HSL } from '@gms/ui-util';
import { DistinctColorPalette, hslToHex, UILogger } from '@gms/ui-util';
import { Weavess } from '@gms/weavess';
import { WeavessConstants, WeavessTypes, WeavessUtil } from '@gms/weavess-core';
import sortBy from 'lodash/sortBy';
import * as React from 'react';

import { dataAcquisitionUserPreferences } from '~components/data-acquisition-ui/config';

const logger = UILogger.create('GMS_SOH_WEAVESS', process.env.GMS_SOH_WEAVESS);

export const WeavessDisplay: React.FunctionComponent<{
  station: SohTypes.UiStationSoh;
  channelSohs: SohTypes.ChannelSoh[];
  sohHistoricalDurations: number[];
  startTimeMs: number;
  endTimeMs: number;
  aceiData: SohTypes.UiHistoricalAcei[];
  // eslint-disable-next-line react/function-component-definition
}> = props => {
  const { channelSohs } = props;
  const sortedChannelSohs = sortBy<SohTypes.ChannelSoh>(channelSohs, [
    channelSoh => channelSoh.channelName
  ]);
  const channelNames = React.useMemo(
    () => sortedChannelSohs.map(channel => channel.channelName).sort(),
    [sortedChannelSohs]
  );

  /** unique color palette created for the channels and station */
  const colorPalette = React.useMemo(
    () => new DistinctColorPalette(channelNames, props.station.stationName),
    [channelNames, props.station.stationName]
  );

  const stations: WeavessTypes.Station[] = [];
  sortedChannelSohs.forEach((channelSoh, i) => {
    const data = props.aceiData?.find(c => c.channelName === channelSoh.channelName);
    let channelColor: HSL;
    try {
      channelColor = colorPalette.getColor(channelSoh.channelName);
    } catch (e) {
      logger.warn(`Failed to find color for channel ${channelSoh.channelName} skipping. ${e}`);
    }

    const channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
    channelSegmentsRecord.data =
      data && channelColor
        ? [
            {
              channelName: channelSoh.channelName,
              wfFilterId: WeavessTypes.UNFILTERED,
              isSelected: false,
              dataSegments: data?.issues.map<WeavessTypes.DataSegment>(issue => ({
                color: hslToHex(channelColor),
                displayType: [WeavessTypes.DisplayType.LINE],
                pointSize: 4,
                data: WeavessUtil.createStepPoints(issue)
              }))
            }
          ]
        : [];

    stations.push({
      id: String(i),
      name: `station ${channelSoh.channelName}`,
      defaultChannel: {
        height: 60,
        defaultRange: {
          min: -0.2,
          max: 1.2
        },
        yAxisTicks: [0, 1],
        id: channelSoh.channelName,
        name: `${channelSoh.channelName.replace(`${props.station.stationName}.`, '')}`,
        waveform: {
          channelSegmentId: 'data',
          channelSegmentsRecord
        }
      },
      nonDefaultChannels: undefined, // Has no child channels
      areChannelsShowing: false
    });
  });

  return (
    <div
      style={{
        height: `${dataAcquisitionUserPreferences.minChartHeightPx}px`
      }}
      className="weavess-container"
      // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex
      tabIndex={0}
    >
      <div className="weavess-container__wrapper">
        <Weavess
          viewableInterval={{
            startTimeSecs: props.startTimeMs / WeavessConstants.MILLISECONDS_IN_SECOND,
            endTimeSecs: props.endTimeMs / WeavessConstants.MILLISECONDS_IN_SECOND
          }}
          isControlledComponent={false}
          minimumOffset={0}
          maximumOffset={0}
          stations={stations}
          selections={{
            channels: undefined
          }}
          initialConfiguration={{
            labelWidthPx: 180,
            defaultChannel: {
              disableMeasureWindow: true,
              disableMaskModification: true
            }
          }}
          events={WeavessConstants.DEFAULT_UNDEFINED_EVENTS}
        />
      </div>
    </div>
  );
};
