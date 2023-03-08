/* eslint-disable class-methods-use-this */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable react/destructuring-assignment */
import {
  Checkbox,
  Classes,
  Colors,
  FormGroup,
  Intent,
  Label,
  NumericInput,
  Spinner
} from '@blueprintjs/core';
import { UILogger } from '@gms/ui-util';
import { WeavessConstants, WeavessTypes, WeavessUtil } from '@gms/weavess-core';
import * as d3 from 'd3';
import memoizeOne from 'memoize-one';
import React from 'react';

import { Weavess } from '../weavess';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

const EVENT_TIMEOUT_MS = 500;
const ONE_HOUR = 3600;
const MIN_OFFSET_SECONDS = 0;
const MAX_OFFSET_SECONDS = 1800;
const MIN_LINES = 1;
const MAX_LINES = 150;
const MIN_HOURS = 1;
const MAX_HOURS = 1152; // 48 Days
const MIN_POINT_FREQUENCY = 1;
const MAX_POINT_FREQUENCY = 60;

const INITIAL_HOURS = 24;

// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface WeavessLineChartExampleProps {}

export interface WeavessLineChartExampleState {
  endTimeSecs: number;
  useTypedArray: boolean;
  numberOfHours: number;
  numberOfLines: number;
  timeOffsetSeconds: number;
  pointFrequency: number;
  isGeneratingData: boolean;
}

const getRandomColor = () => {
  const letters = '0123456789ABCDEF';
  let color = '#';
  for (let i = 0; i < 6; i += 1) {
    color += letters[Math.floor(WeavessUtil.getSecureRandomNumber() * 16)];
  }
  return color;
};

const generateDummyData = memoizeOne(
  (
    startTimeSecs: number,
    endTimeSecs: number,
    numberOfLines: number,
    useTypedArray: boolean,
    timeOffsetSeconds: number,
    pointFrequency: number
  ) => {
    const start = new Date('2016-01-01T00:00:00Z').valueOf();

    const stations: WeavessTypes.Station[] = [];
    const dataSegments: WeavessTypes.DataSegment[] = [];

    const timeToGlScale = d3.scaleLinear().domain([startTimeSecs, endTimeSecs]).range([0, 100]);

    for (let i = 0; i < numberOfLines; i += 1) {
      const data: WeavessTypes.TimeValuePair[] = Array.from(
        { length: endTimeSecs - startTimeSecs },
        (v: number, idx: number) => ({
          timeSecs: startTimeSecs + pointFrequency * idx,
          value: WeavessUtil.getSecureRandomNumber() * 100
        })
      );

      let dataByTime: WeavessTypes.DataByTime;
      if (useTypedArray) {
        const values: Float32Array = new Float32Array(data.length * 2);
        let n = 0;
        data.forEach(value => {
          // eslint-disable-next-line no-plusplus
          values[n++] = timeToGlScale(value.timeSecs + timeOffsetSeconds);
          // eslint-disable-next-line no-plusplus
          values[n++] = value.value;
        });
        dataByTime = { values };
      } else {
        dataByTime = { values: data };
      }

      dataSegments.push({
        color: getRandomColor(),
        displayType: [WeavessTypes.DisplayType.LINE],
        pointSize: 4,
        data: dataByTime
      });
    }

    const channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
    channelSegmentsRecord.data = [
      {
        channelName: 'ExampleChannel',
        isSelected: false,
        wfFilterId: WeavessTypes.UNFILTERED,
        dataSegments
      }
    ];
    stations.push({
      id: 'id',
      name: ``,
      defaultChannel: {
        height: 750,
        yAxisTicks: [
          0,
          5,
          10,
          15,
          20,
          25,
          30,
          35,
          40,
          45,
          50,
          55,
          60,
          65,
          70,
          75,
          80,
          85,
          90,
          95,
          100
        ],
        defaultRange: {
          min: -1,
          max: 101
        },
        id: 'id',
        name: ``,
        timeOffsetSeconds,
        waveform: {
          channelSegmentId: 'data',
          channelSegmentsRecord
        }
      },
      nonDefaultChannels: undefined, // Set it to undefined means no Expand/Collapse button on Station Label
      areChannelsShowing: false
    });

    logger.debug(`Took ${(Date.now() - start) / 1000} seconds to generate data`);

    return stations;
    /* eslint-enable @typescript-eslint/no-magic-numbers */
  }
);

// eslint-disable-next-line react/function-component-definition
const CustomYLabel: React.FunctionComponent<WeavessTypes.LabelProps> = () => (
  <div
    style={{
      width: '150px',
      transform: 'rotate(270deg)',
      transformOrigin: 'left top 0'
    }}
  >
    Example Line Chart
  </div>
);

export class WeavessLineChartExample extends React.Component<
  WeavessLineChartExampleProps,
  WeavessLineChartExampleState
> {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  private readonly startTimeSecs: number = 1507593600; // Tue, 10 Oct 2017 00:00:00 GMT

  private numberOfHoursTimeout: ReturnType<typeof setTimeout>;

  private numberOfLinesTimeout: ReturnType<typeof setTimeout>;

  private timeOffsetSecondsTimeout: ReturnType<typeof setTimeout>;

  private pointFrequencyTimeout: ReturnType<typeof setTimeout>;

  public weavess: Weavess;

  private stations: WeavessTypes.Station[] = [];

  public constructor(props: WeavessLineChartExampleProps) {
    super(props);
    this.state = {
      endTimeSecs: this.startTimeSecs + ONE_HOUR * INITIAL_HOURS,
      useTypedArray: true,
      numberOfHours: INITIAL_HOURS,
      numberOfLines: 10,
      timeOffsetSeconds: 0,
      pointFrequency: 10,
      isGeneratingData: false
    };

    this.stations = generateDummyData(
      this.startTimeSecs,
      this.state.endTimeSecs,
      this.state.numberOfLines,
      this.state.useTypedArray,
      this.state.timeOffsetSeconds,
      this.state.pointFrequency
    );
  }

  public componentDidUpdate(): void {
    if (this.state.isGeneratingData) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      new Promise<void>(resolve => {
        setTimeout(() => {
          this.stations = generateDummyData(
            this.startTimeSecs,
            this.state.endTimeSecs,
            this.state.numberOfLines,
            this.state.useTypedArray,
            this.state.timeOffsetSeconds,
            this.state.pointFrequency
          );
          this.setState({
            isGeneratingData: false
          });
          // eslint-disable-next-line @typescript-eslint/no-magic-numbers
        }, 200);
        resolve();
      }).catch(error => {
        logger.warn(`Failed to generate data ${error}`);
      });
    }
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
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
        {this.state.isGeneratingData ? (
          <div
            style={{
              position: 'fixed',
              width: '100%',
              height: '100%',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              backgroundColor: 'rgba(0,0,0,0.5)',
              zIndex: 6
            }}
          >
            <Spinner intent={Intent.PRIMARY} />
          </div>
        ) : undefined}
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
                display: 'block'
              }}
            >
              <FormGroup className={Classes.INLINE}>
                <Checkbox
                  label="Use Typed Array"
                  checked={this.state.useTypedArray}
                  onClick={() =>
                    this.setState(prevState => ({ useTypedArray: !prevState.useTypedArray }))
                  }
                />
                <Label className={Classes.INLINE}>
                  Number of Hours (1 - 1152 (48 days)):
                  <NumericInput
                    className={Classes.INLINE}
                    allowNumericCharactersOnly
                    onValueChange={this.onValueChangeNumberOfHours}
                    onBlur={this.onBlurNumberOfHours}
                    placeholder="Enter a number"
                    value={this.state.numberOfHours}
                    min={MIN_HOURS}
                    max={MAX_HOURS}
                  />
                </Label>
                <Label className={Classes.INLINE}>
                  Number of Lines (1 - 150):
                  <NumericInput
                    className={Classes.INLINE}
                    allowNumericCharactersOnly
                    onValueChange={this.onValueChangeNumberOfLines}
                    onBlur={this.onBlurNumberOfLines}
                    placeholder="Enter a number"
                    value={this.state.numberOfLines}
                    min={MIN_LINES}
                    max={MAX_LINES}
                  />
                </Label>
                <Label className={Classes.INLINE}>
                  Time Offset Seconds (0 - 1800):
                  <NumericInput
                    className={Classes.INLINE}
                    allowNumericCharactersOnly
                    onValueChange={this.onValueChangeTimeOffsetSeconds}
                    onBlur={this.onBlurTimeOffsetSeconds}
                    placeholder="Enter a number"
                    stepSize={10}
                    value={this.state.timeOffsetSeconds}
                    min={MIN_OFFSET_SECONDS}
                    max={MAX_OFFSET_SECONDS}
                  />
                </Label>
                <Label className={Classes.INLINE}>
                  Point Frequency (1 - 60):
                  <NumericInput
                    className={Classes.INLINE}
                    allowNumericCharactersOnly
                    onValueChange={this.onValueChangePointFrequency}
                    onBlur={this.onBlurPointFrequency}
                    placeholder="Enter a number"
                    value={this.state.pointFrequency}
                    min={MIN_POINT_FREQUENCY}
                    max={MAX_POINT_FREQUENCY}
                  />
                </Label>
              </FormGroup>
              <div>
                Number of Points per line:{' '}
                {(this.state.endTimeSecs - this.startTimeSecs) / this.state.pointFrequency}
              </div>
              <div>
                Total number of Points:{' '}
                {((this.state.endTimeSecs - this.startTimeSecs) / this.state.pointFrequency) *
                  this.state.numberOfLines}
              </div>
            </div>
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
                  minimumOffset={0}
                  maximumOffset={0}
                  viewableInterval={{
                    startTimeSecs: this.startTimeSecs,
                    endTimeSecs: this.state.endTimeSecs
                  }}
                  stations={this.stations}
                  selections={{
                    channels: undefined
                  }}
                  initialConfiguration={{
                    suppressLabelYAxis: false,
                    labelWidthPx: 184,
                    xAxisLabel: `My Custom X-Axis Label`
                  }}
                  customLabel={CustomYLabel}
                  events={WeavessConstants.DEFAULT_UNDEFINED_EVENTS}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  private readonly validateNumberOfHours = (value: number): number => {
    if (value < MIN_HOURS) {
      return MIN_HOURS;
    }
    if (value > MAX_HOURS) {
      return MAX_HOURS;
    }
    return value;
  };

  private readonly onValueChangeNumberOfHours = (valueAsNumber: number): void => {
    const numberOfHours = this.validateNumberOfHours(valueAsNumber);
    clearTimeout(this.numberOfHoursTimeout);
    this.numberOfHoursTimeout = setTimeout(() => {
      this.setState({
        endTimeSecs: this.startTimeSecs + numberOfHours * ONE_HOUR,
        numberOfHours,
        isGeneratingData: true
      });
    }, EVENT_TIMEOUT_MS);
  };

  private readonly onBlurNumberOfHours = (e: React.FocusEvent<HTMLInputElement>) => {
    const numberOfHours = this.validateNumberOfHours(Number((e.target as HTMLInputElement).value));
    clearTimeout(this.numberOfHoursTimeout);
    this.numberOfHoursTimeout = setTimeout(() => {
      this.setState({
        endTimeSecs: this.startTimeSecs + numberOfHours * ONE_HOUR,
        numberOfHours,
        isGeneratingData: true
      });
    }, EVENT_TIMEOUT_MS);
  };

  private readonly validateNumberOfLines = (value: number): number => {
    if (value < MIN_LINES) {
      return MIN_LINES;
    }
    if (value > MAX_LINES) {
      return MAX_LINES;
    }
    return value;
  };

  private readonly onValueChangeNumberOfLines = (valueAsNumber: number): void => {
    const numberOfLines = this.validateNumberOfLines(valueAsNumber);
    clearTimeout(this.numberOfLinesTimeout);
    this.numberOfLinesTimeout = setTimeout(() => {
      this.setState({
        numberOfLines,
        isGeneratingData: true
      });
    }, EVENT_TIMEOUT_MS);
  };

  private readonly onBlurNumberOfLines = (e: React.FocusEvent<HTMLInputElement>) => {
    const numberOfLines = this.validateNumberOfLines(Number((e.target as HTMLInputElement).value));
    clearTimeout(this.numberOfLinesTimeout);
    this.numberOfLinesTimeout = setTimeout(() => {
      this.setState({
        numberOfLines,
        isGeneratingData: true
      });
    }, EVENT_TIMEOUT_MS);
  };

  private readonly validateTimeOffsetSeconds = (value: number): number => {
    if (value < MIN_OFFSET_SECONDS) {
      return MIN_OFFSET_SECONDS;
    }
    if (value > MAX_OFFSET_SECONDS) {
      return MAX_OFFSET_SECONDS;
    }
    return value;
  };

  private readonly onValueChangeTimeOffsetSeconds = (valueAsNumber: number): void => {
    const timeOffsetSeconds = this.validateTimeOffsetSeconds(valueAsNumber);
    clearTimeout(this.timeOffsetSecondsTimeout);
    this.timeOffsetSecondsTimeout = setTimeout(() => {
      this.setState({
        timeOffsetSeconds,
        isGeneratingData: true
      });
    }, EVENT_TIMEOUT_MS);
  };

  private readonly onBlurTimeOffsetSeconds = (e: React.FocusEvent<HTMLInputElement>) => {
    const timeOffsetSeconds = this.validateTimeOffsetSeconds(
      Number((e.target as HTMLInputElement).value)
    );
    clearTimeout(this.timeOffsetSecondsTimeout);
    this.timeOffsetSecondsTimeout = setTimeout(() => {
      this.setState({
        timeOffsetSeconds,
        isGeneratingData: true
      });
    }, EVENT_TIMEOUT_MS);
  };

  private readonly validatePointFrequency = (value: number): number => {
    if (value < MIN_POINT_FREQUENCY) {
      return MIN_POINT_FREQUENCY;
    }
    if (value > MAX_POINT_FREQUENCY) {
      return MAX_POINT_FREQUENCY;
    }
    return value;
  };

  private readonly onValueChangePointFrequency = (valueAsNumber: number): void => {
    const pointFrequency = this.validatePointFrequency(valueAsNumber);
    clearTimeout(this.pointFrequencyTimeout);
    this.pointFrequencyTimeout = setTimeout(() => {
      this.setState({
        pointFrequency,
        isGeneratingData: true
      });
    }, EVENT_TIMEOUT_MS);
  };

  private readonly onBlurPointFrequency = (e: React.FocusEvent<HTMLInputElement>) => {
    const pointFrequency = this.validatePointFrequency(
      Number((e.target as HTMLInputElement).value)
    );
    clearTimeout(this.pointFrequencyTimeout);
    this.pointFrequencyTimeout = setTimeout(() => {
      this.setState({
        pointFrequency,
        isGeneratingData: true
      });
    }, EVENT_TIMEOUT_MS);
  };
}
