/* eslint-disable @typescript-eslint/no-magic-numbers */
import { LogLevel } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import type { WeavessTypes } from '@gms/weavess-core';
import { WeavessConstants, WeavessUtil } from '@gms/weavess-core';
import React from 'react';

import { Weavess } from '../weavess';
import { WeavessGenericContainerWrapper } from './container-wrapper';

const logger = UILogger.create('GMS_LOG_WEAVESS', LogLevel.DEBUG);

export class EventsExample extends React.Component<unknown, unknown> {
  public weavess: Weavess;

  public render(): JSX.Element {
    const waveforms: WeavessTypes.Station[] = [];

    const startTimeSecs =
      new Date('2016-01-01T00:00:00Z').valueOf() / WeavessConstants.MILLISECONDS_IN_SECOND;
    const endTimeSecs = startTimeSecs + 1800; // + 30 minutes

    for (let i = 0; i < 25; i += 1) {
      const waveform = WeavessUtil.createDummyWaveform(
        'ExampleChannel',
        startTimeSecs,
        endTimeSecs,
        20,
        WeavessUtil.getSecureRandomNumber() * 2,
        WeavessUtil.getSecureRandomNumber() * 0.25
      );
      waveform.id = `Channel${i}`;
      waveform.name = `Channel ${i}`;
      waveforms.push(waveform);
    }

    return (
      <WeavessGenericContainerWrapper>
        <Weavess
          ref={ref => {
            if (ref) {
              this.weavess = ref;
            }
          }}
          stations={waveforms}
          viewableInterval={{
            startTimeSecs,
            endTimeSecs
          }}
          minimumOffset={0}
          maximumOffset={0}
          events={{
            stationEvents: {
              defaultChannelEvents: {
                labelEvents: {},
                events: {
                  onSignalDetectionClick: () => {
                    logger.debug('signal detection deleted!');
                  },
                  onSignalDetectionDragEnd: () => {
                    logger.debug('signal detection modified!');
                  }
                }
              },
              nonDefaultChannelEvents: {
                labelEvents: {},
                events: {}
              }
            }
          }}
          flex={false}
        />
      </WeavessGenericContainerWrapper>
    );
  }
  // eslint-disable-next-line max-lines
}
