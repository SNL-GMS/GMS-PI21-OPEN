/* eslint-disable @typescript-eslint/no-magic-numbers */
import { WeavessConstants, WeavessUtil } from '@gms/weavess-core';
import React from 'react';

import { WeavessRecordSection } from '../weavess';
import { WeavessGenericContainerWrapper } from './container-wrapper';

export class RecordSectionExample extends React.Component<unknown, unknown> {
  public recordSection: WeavessRecordSection;

  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, no-magic-numbers, react/sort-comp
  public render(): JSX.Element {
    return (
      <WeavessGenericContainerWrapper>
        <WeavessRecordSection
          ref={ref => {
            if (ref) {
              this.recordSection = ref;
            }
          }}
        />
      </WeavessGenericContainerWrapper>
    );
  }

  public componentDidMount(): void {
    if (this.recordSection) {
      this.recordSection.update(true);

      this.recordSection.addWaveformArray(
        [
          {
            channel: 'close',
            // eslint-disable-next-line prefer-spread
            data: Array.apply(null, Array(100000)).map(() =>
              Math.round(WeavessUtil.getSecureRandomNumber() * 100)
            ),
            distance: 1079184.644731988, // in meters
            phase: 'P',
            // eslint-disable-next-line
            startTime: new Date('2016-01-01T00:00:00Z').valueOf() / WeavessConstants.MILLISECONDS_IN_SECOND,
            sampleRate: 40,
            signalDetection: [
              {
                time: new Date('2016-01-01T00:02:30Z'),
                uncertaintySecs: 1.5,
                showUncertaintyBars: true,
                id: 0,
                color: '#f00',
                label: 's'
              }
            ]
          },
          {
            channel: 'medium',
            // eslint-disable-next-line prefer-spread
            data: Array.apply(null, Array(100000)).map(() =>
              Math.round(WeavessUtil.getSecureRandomNumber() * 5000)
            ),
            distance: 3379184.644731988,
            phase: 'P',
            // eslint-disable-next-line
            startTime: new Date('2016-01-01T00:00:00Z').valueOf() / WeavessConstants.MILLISECONDS_IN_SECOND,
            sampleRate: 40,
            signalDetection: [
              {
                time: new Date('2016-01-01T00:05:00Z'),
                uncertaintySecs: 1.5,
                id: 1,
                color: '#f00',
                label: ''
              }
            ]
          },
          {
            channel: 'far',
            // eslint-disable-next-line prefer-spread
            data: Array.apply(null, Array(100000)).map(() =>
              Math.round(WeavessUtil.getSecureRandomNumber() * 5000)
            ),
            distance: 3914023.8687042934,
            phase: 'P',
            // eslint-disable-next-line
            startTime: new Date('2016-01-01T00:00:00Z').valueOf() / WeavessConstants.MILLISECONDS_IN_SECOND,
            sampleRate: 40,
            signalDetection: [
              {
                time: new Date('2016-01-01T00:10:00'),
                uncertaintySecs: 1.5,
                id: 2,
                color: '#f00',
                label: ''
              }
            ]
          }
        ],
        false
      );

      this.recordSection.update(false);
      this.recordSection.forceUpdate();
    }
  }
}
