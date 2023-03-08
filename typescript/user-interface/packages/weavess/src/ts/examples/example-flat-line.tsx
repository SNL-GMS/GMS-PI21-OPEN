/* eslint-disable @typescript-eslint/no-magic-numbers */
import { Classes, Colors } from '@blueprintjs/core';
import { WeavessConstants, WeavessTypes, WeavessUtil } from '@gms/weavess-core';
import React from 'react';

import { Weavess } from '../weavess';

export interface WeavessFlatLineExampleProps {
  showExampleControls: boolean;
}

export interface WeavessFlatLineExampleState {
  stations: WeavessTypes.Station[];
}

export class WeavessFlatLineExample extends React.Component<
  WeavessFlatLineExampleProps,
  WeavessFlatLineExampleState
> {
  // eslint-disable-next-line react/static-property-placement
  public static defaultProps: WeavessFlatLineExampleProps = {
    // eslint-disable-next-line react/default-props-match-prop-types
    showExampleControls: true
  };

  public static SAMPLE_RATE = 0.1;

  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  public static NUM_SAMPLES: number = WeavessFlatLineExample.SAMPLE_RATE * 1800; // 10 minutes of data

  public static startTimeSecs = 1507593600; // Tue, 10 Oct 2017 00:00:00 GMT

  public static endTimeSecs: number = WeavessFlatLineExample.startTimeSecs + 1800; // + 30 minutes

  public weavess: Weavess;

  public constructor(props: WeavessFlatLineExampleProps) {
    super(props);
    this.state = {
      stations: []
    };
  }

  public componentDidMount(): void {
    this.setState({
      stations: this.generateDummyData()
    });
  }

  // eslint-disable-next-line complexity
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const { stations } = this.state;

    return (
      <div
        className={Classes.DARK}
        style={{
          height: '90%',
          width: '100%',
          padding: '0.5rem',
          color: Colors.GRAY4,
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center'
        }}
      >
        <div
          className={Classes.DARK}
          style={{
            height: '100%',
            width: '100%'
          }}
        >
          <div
            style={{
              height: '100%',
              width: '100%',
              display: 'flex',
              flexDirection: 'column'
            }}
          >
            <div
              style={{
                flex: '1 1 auto',
                position: 'relative'
              }}
            >
              <div
                style={{
                  position: 'absolute',
                  top: '0px',
                  bottom: '0px',
                  left: '0px',
                  right: '0px'
                }}
              >
                <Weavess
                  ref={ref => {
                    if (ref) {
                      this.weavess = ref;
                    }
                  }}
                  viewableInterval={{
                    startTimeSecs: WeavessFlatLineExample.startTimeSecs,
                    endTimeSecs: WeavessFlatLineExample.endTimeSecs
                  }}
                  minimumOffset={0}
                  maximumOffset={0}
                  stations={stations}
                  selections={{
                    channels: undefined
                  }}
                  initialConfiguration={{
                    suppressLabelYAxis: true
                  }}
                  events={WeavessConstants.DEFAULT_UNDEFINED_EVENTS}
                  markers={{
                    verticalMarkers: [
                      {
                        id: 'marker',
                        color: 'pink',
                        lineStyle: WeavessTypes.LineStyle.DASHED,
                        timeSecs: WeavessFlatLineExample.startTimeSecs + 1200
                      }
                    ]
                  }}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // eslint-disable-next-line class-methods-use-this
  private readonly generateDummyData = () => {
    const stations: WeavessTypes.Station[] = [];

    // create channels w/ random noise as data
    for (let i = 0; i < 10; i += 1) {
      const values: WeavessTypes.TimeValuePair[] = [];
      /* eslint-disable max-len */
      let value = Math.round(WeavessUtil.getSecureRandomNumber());
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs, value });
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 100, value });
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 300, value });
      value = Math.round(WeavessUtil.getSecureRandomNumber());
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 300, value });
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1000, value });
      value = Math.round(WeavessUtil.getSecureRandomNumber());
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1000, value });
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1100, value });
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1200, value });
      value = Math.round(WeavessUtil.getSecureRandomNumber());
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1200, value });
      value = Math.round(WeavessUtil.getSecureRandomNumber());
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1200, value });
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1300, value });
      value = Math.round(WeavessUtil.getSecureRandomNumber());
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1300, value });
      values.push({ timeSecs: WeavessFlatLineExample.startTimeSecs + 1500, value });
      values.push({ timeSecs: WeavessFlatLineExample.endTimeSecs, value });
      /* eslint-enable max-len */
      const data: WeavessTypes.DataByTime = { values };

      const channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
      channelSegmentsRecord.data = [
        {
          channelName: 'ExampleChannel',
          wfFilterId: WeavessTypes.UNFILTERED,
          isSelected: false,
          dataSegments: [
            {
              color: 'dodgerblue',
              displayType: [WeavessTypes.DisplayType.LINE],
              pointSize: 4,
              data
            }
          ]
        }
      ];
      stations.push({
        id: String(i),
        name: `station ${i}`,
        defaultChannel: {
          height: 50,
          defaultRange: {
            min: -1,
            max: 2
          },
          id: String(i),
          name: `channel ${i}`,
          waveform: {
            channelSegmentId: 'data',
            channelSegmentsRecord,
            markers: {
              verticalMarkers: [
                {
                  id: 'marker',
                  color: 'lime',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessFlatLineExample.startTimeSecs + 5
                }
              ]
            }
          }
        },
        nonDefaultChannels: undefined, // Set it to undefined means no Expand/Collapse button on Station Label,
        areChannelsShowing: false
      });
    }
    return stations;
  };
}
