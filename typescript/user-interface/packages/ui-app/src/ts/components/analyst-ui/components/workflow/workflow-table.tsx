/* eslint-disable class-methods-use-this */
import type { CommonTypes } from '@gms/common-model';
import { WorkflowTypes } from '@gms/common-model';
import {
  isActivityInterval,
  isAutomaticProcessingStage,
  isInteractiveAnalysisStage,
  isInteractiveAnalysisStageInterval
} from '@gms/common-model/lib/workflow/types';
import type GoldenLayout from '@gms/golden-layout';
import { ScrollBarOverride } from '@gms/ui-core-components';
import type { StageIntervalList } from '@gms/ui-state';
import {
  addGlUpdateOnResize,
  addGlUpdateOnShow,
  memoizedGetScrollBarWidth,
  UILogger
} from '@gms/ui-util';
import Immutable from 'immutable';
import flatMap from 'lodash/flatMap';
import flatten from 'lodash/flatten';
import isEqual from 'lodash/isEqual';
import throttle from 'lodash/throttle';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';
import memoizeOne from 'memoize-one';
import React from 'react';
import type { CollectionCellRendererParams } from 'react-virtualized';
import { Collection } from 'react-virtualized';
import type { ScrollParams } from 'react-virtualized/dist/es/Grid';

import { ActivityIntervalCell } from './activity-interval-cell';
import {
  HEIGHT,
  HUNDRED_TWENTY_SECOND_BUFFER,
  TABLE_BUTTON_WIDTH,
  TABLE_LABEL_WIDTH,
  TABLE_SIDE_PADDING,
  TIME_AXIS_HEIGHT,
  TOOLBAR_HEIGHT_PX
} from './constants';
import { DayBoundaryIndicator } from './day-boundary-indicator';
import { StageColumnEntry } from './stage-column-entry';
import { StageExpansionButton } from './stage-expansion-button';
import { TimeRange } from './time-range';
import { WorkflowRowLabel } from './workflow-row-label';
import { WorkflowTimeAxis } from './workflow-time-axis';
import { Tooltip } from './workflow-tooltip';
import { calculateWidth, getScaleForTimeRange } from './workflow-util';

const logger = UILogger.create('GMS_LOG_WORKFLOW', process.env.GMS_LOG_WORKFLOW);

interface Empty {
  startTime: number;
  endTime: number;
}

export function isValue(object: Partial<Value>): object is Value {
  return object && object.name !== undefined;
}

type Value = WorkflowTypes.StageInterval | WorkflowTypes.ActivityInterval;

type ValueOrEmpty = WorkflowTypes.StageInterval | WorkflowTypes.ActivityInterval | Empty;
interface TableCell {
  readonly index: number;
  readonly rowIndex: number;
  readonly colIndex: number;
  readonly value: ValueOrEmpty;
}

export interface RowState {
  name: string;
  isExpanded: boolean;
  subRows: { name: string; isActivityRow: boolean }[];
}

export interface WorkflowTableProps {
  readonly glContainer: GoldenLayout.Container;
  readonly widthPx: number;
  readonly heightPx: number;
  readonly timeRange: CommonTypes.TimeRange;
  readonly stageIntervals: StageIntervalList;
  readonly workflow: WorkflowTypes.Workflow;
  readonly staleStartTime: number;
}

export interface WorkflowTableState {
  readonly expandedDataMap: Immutable.Map<string, RowState>;
  readonly overrideScrollLeft: number;
}

/**
 * @returns the reference to the virtualized table element
 */
const getVirtualizedContainer = (): Element =>
  document.querySelector('.workflow-table .workflow-table-container .ReactVirtualized__Collection');

/**
 * @returns the reference to the inner virtualized table element
 */
const getVirtualizedInnerContainer = (): Element =>
  document.querySelector(
    '.workflow-table .workflow-table-container .ReactVirtualized__Collection__innerScrollContainer'
  );
/**
 * Processes the Workflow data into a structure that can be used for the virtualized table
 *
 * @param stage the stage
 * @param stageIntervals the stage intervals
 * @returns the processed data
 */
const processData = (
  stage: WorkflowTypes.Stage,
  stageIntervals: WorkflowTypes.StageInterval[]
): Value[][] => {
  let subRowNames: string[] = [];
  if (isAutomaticProcessingStage(stage)) {
    subRowNames = stage.sequences.map(sequence => sequence.name);
  } else if (isInteractiveAnalysisStage(stage)) {
    subRowNames = stage.activities.map(activity => activity.name);
  }

  const cells: Value[][] = [[]];
  Array(subRowNames.length)
    .fill(undefined)
    .forEach(() => cells.push([]));

  stageIntervals?.forEach((stageInterval: WorkflowTypes.StageInterval, index: number) => {
    cells[0][index] = stageInterval;

    let intervals: WorkflowTypes.Interval[] = [];
    if (WorkflowTypes.isInteractiveAnalysisStageInterval(stageInterval)) {
      intervals = stageInterval.activityIntervals;
    } else if (WorkflowTypes.isAutomaticProcessingStageInterval(stageInterval)) {
      intervals = stageInterval.sequenceIntervals;
    }

    subRowNames?.forEach((name, subRowIndex) => {
      if (stageInterval.stageMode === WorkflowTypes.StageMode.INTERACTIVE) {
        const interval: WorkflowTypes.ActivityInterval = (intervals.find(
          int => int.name === name
        ) as unknown) as WorkflowTypes.ActivityInterval;
        cells[subRowIndex + 1][index] = interval;
      } else {
        const interval: WorkflowTypes.StageInterval = (intervals.find(
          int => int.name === name
        ) as unknown) as WorkflowTypes.StageInterval;
        cells[subRowIndex + 1][index] = interval;
      }
    });
  });
  return cells;
};

/**
 * Component for rendering the workflow table
 */
export class WorkflowTable extends React.Component<WorkflowTableProps, WorkflowTableState> {
  private timeAxisRef: WorkflowTimeAxis;

  private dayBoundaryIndicatorRef: DayBoundaryIndicator;

  private timeRangeRef: TimeRange;

  private virtualizedContainer: Element;

  private readonly virtualizedInnerResizeObserver: ResizeObserver;

  private readonly virtualizedInnerMutationObserver: MutationObserver;

  private readonly virtualizedContainerResizeObserver: ResizeObserver;

  private workflowTableRef: HTMLDivElement;

  private memoizedProcessData: Immutable.Map<
    string,
    (stage: WorkflowTypes.Stage, stageIntervals: WorkflowTypes.StageInterval[]) => Value[][]
  > = Immutable.Map();

  private previousMinStartTime: number;

  private currentMinStartTime: number;

  private data: Immutable.List<TableCell> = Immutable.List();

  private shouldAutoScroll = true;

  private mouseDown = false;

  /**
   * Constructor.
   *
   * @param props the initial props
   */
  public constructor(props: WorkflowTableProps) {
    super(props);
    this.virtualizedInnerResizeObserver = new ResizeObserver(() => {
      this.forceUpdate();
    });
    this.virtualizedContainerResizeObserver = new ResizeObserver(() => {
      this.forceUpdate();
    });
    this.virtualizedInnerMutationObserver = new MutationObserver(() => {
      this.forceUpdate();
    });
    let expandedDataMap: Immutable.Map<string, RowState> = Immutable.Map();
    let subRows: { name: string; isActivityRow: boolean }[] = [];
    const { workflow } = this.props;
    workflow.stages?.forEach(stage => {
      if (isInteractiveAnalysisStage(stage)) {
        stage.activities?.forEach(a => {
          subRows.push({ name: a.name, isActivityRow: true });
        });
      }
      if (isAutomaticProcessingStage(stage)) {
        stage.sequences?.forEach(s => {
          subRows.push({ name: s.name, isActivityRow: true });
        });
      }
      expandedDataMap = expandedDataMap.set(stage.name, {
        name: stage.name,
        isExpanded: false,
        subRows
      });
      subRows = [];
    });
    this.state = {
      expandedDataMap,
      overrideScrollLeft: undefined
    };
  }

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount(): void {
    this.virtualizedContainer = getVirtualizedContainer();
    if (this.virtualizedContainer) {
      this.virtualizedInnerResizeObserver.observe(getVirtualizedInnerContainer());
      this.virtualizedContainerResizeObserver.observe(this.workflowTableRef);
      this.virtualizedInnerMutationObserver.observe(this.workflowTableRef, {
        childList: true,
        subtree: true
      });
      this.updateScrollLeft(this.virtualizedContainer.scrollWidth);
    }

    // also scroll to the left on a resize or window show; this will guarantee
    // that the virtualized table will render properly
    const { glContainer } = this.props;
    addGlUpdateOnShow(glContainer, () => {
      if (this.virtualizedContainer) {
        this.updateScrollLeft(this.virtualizedContainer.scrollWidth);
      }
    });
    addGlUpdateOnResize(glContainer, () => {
      if (this.virtualizedContainer) {
        this.updateScrollLeft(this.virtualizedContainer.scrollWidth);
      }
    });
  }

  public shouldComponentUpdate(
    nextProps: WorkflowTableProps,
    nextState: WorkflowTableState
  ): boolean {
    const { widthPx, heightPx, timeRange, workflow, stageIntervals } = this.props;
    const { expandedDataMap, overrideScrollLeft } = this.state;

    return (
      heightPx !== nextProps.heightPx ||
      widthPx !== nextProps.widthPx ||
      timeRange.startTimeSecs !== nextProps.timeRange.startTimeSecs ||
      timeRange.endTimeSecs !== nextProps.timeRange.endTimeSecs ||
      overrideScrollLeft !== nextState.overrideScrollLeft ||
      expandedDataMap !== nextState.expandedDataMap ||
      !isEqual(workflow, nextProps.workflow) ||
      !isEqual(stageIntervals, nextProps.stageIntervals)
    );
  }

  /**
   * Invoked after a component is updated.
   */
  public componentDidUpdate(prevProps: WorkflowTableProps): void {
    const { timeRange: prevTimeRange, widthPx: prevWidthPx, heightPx: prevHeightPx } = prevProps;
    const { timeRange, widthPx, heightPx } = this.props;
    // only attempt to auto scroll or adjust scroll position if the data has changed
    if (
      prevTimeRange.startTimeSecs !== timeRange.startTimeSecs ||
      prevTimeRange.endTimeSecs !== timeRange.endTimeSecs ||
      prevWidthPx !== widthPx ||
      prevHeightPx !== heightPx
    ) {
      // determine the correct scroll position based on the updated data and auto scroll
      this.virtualizedContainer = getVirtualizedContainer();
      if (this.virtualizedContainer) {
        if (this.virtualizedContainer) {
          const { scrollLeft, scrollWidth, clientWidth } = this.virtualizedContainer;
          this.shouldAutoScroll = Math.abs(scrollWidth - scrollLeft - clientWidth) < 4;
        }
        const { scaleToPosition, totalWidth } = getScaleForTimeRange(timeRange);
        if (this.shouldAutoScroll) {
          this.updateScrollLeft(totalWidth);
        } else {
          this.updateScrollLeft(scaleToPosition(this.previousMinStartTime));
        }
      }
    }
  }

  /**
   * Invoked when components unmounts.
   */
  public componentWillUnmount(): void {
    this.virtualizedInnerResizeObserver.disconnect();
  }

  /**
   * @returns the total number of cells
   */
  private readonly getCellCount = (): number => this.data.size;

  /**
   * Sets the state for override scroll left and sets virtualized container scroll left
   *
   * @param scrollLeft for virtualized container scrolling
   */
  private readonly updateScrollLeft = (scrollLeft: number) => {
    if (this.virtualizedContainer) {
      this.virtualizedContainer.scrollLeft = scrollLeft;
      this.setState({ overrideScrollLeft: scrollLeft });
    }
  };

  /**
   * @returns the total number of rows
   */
  private readonly getRowCount = (): number => uniq([...this.data.map(c => c.rowIndex)]).length;

  /**
   * @param index the index of the cell
   * @returns the cell value
   */
  private readonly getCellValue = (index: number): TableCell => this.data.get(index);

  /**
   * @param param (index) the index of the cell
   * @returns the position of the cell at the provided index
   */
  private readonly cellSizeAndPositionGetter = ({ index }) => {
    const { timeRange } = this.props;
    const { rowIndex, value } = this.getCellValue(index);
    const { scaleToPosition } = getScaleForTimeRange(timeRange);

    const x = scaleToPosition(value.startTime);
    const y: number = rowIndex * HEIGHT;
    const width = calculateWidth(value.startTime, value.endTime);
    const height = HEIGHT;
    return { height, width, x, y };
  };

  /**
   * @returns the JSX.Element that is rendered when no context exists
   */
  private readonly noContentRenderer = (): JSX.Element => <div>No cells</div>;

  /**
   * @param renderer the table cell render
   * @returns the rendered table cell for the provided renderer
   */
  private readonly cellRenderer = (renderer: CollectionCellRendererParams): JSX.Element => {
    const { index, key, style } = renderer;
    const { staleStartTime, workflow } = this.props;
    const { value } = this.getCellValue(index);

    if (isValue(value)) {
      if (isActivityInterval(value)) {
        const activityIntervalKey = `${value.stageName}_${value.name}_${value.startTime}_${value.endTime}`;
        return (
          <div key={key} style={style}>
            <Tooltip key={activityIntervalKey} interval={value} staleStartTime={staleStartTime}>
              <ActivityIntervalCell key={activityIntervalKey} activityInterval={value} />
            </Tooltip>
          </div>
        );
      }

      const stageIntervalKey = `${value.name}_${value.startTime}_${value.endTime}`;
      const activeAnalystRollup = isInteractiveAnalysisStageInterval(value)
        ? uniqBy(
            flatten(value.activityIntervals.map(activity => activity.activeAnalysts)),
            name => {
              return name;
            }
          )
        : undefined;

      return (
        <div key={key} style={style}>
          <Tooltip
            key={stageIntervalKey}
            interval={value}
            activeAnalysts={activeAnalystRollup}
            staleStartTime={staleStartTime}
          >
            <StageColumnEntry key={stageIntervalKey} stageInterval={value} workflow={workflow} />
          </Tooltip>
        </div>
      );
    }
    return <div key={key} style={style} />;
  };

  private readonly updateExpandedDataMap = (stageName: string) => {
    const { expandedDataMap } = this.state;
    const entry = expandedDataMap.get(stageName);
    this.setState(state => ({
      expandedDataMap: state.expandedDataMap.set(stageName, {
        ...entry,
        isExpanded: !entry.isExpanded
      })
    }));
  };

  /**
   * Handles the event called when a new table section is rendered
   *
   * @param params the section rendered params
   */
  private readonly onSectionRendered = (): void => {
    // !the passed in params do not match the expected API type
    this.updateCurrentTimeRange();
    this.currentMinStartTime = this.getViewableMinStartTime();
  };

  // eslint-disable-next-line react/sort-comp
  private readonly throttledOnSectionRendered = throttle(this.onSectionRendered);

  /**
   * Handles the onScroll event
   *
   * @param event the event
   */
  private readonly onScroll = (event: ScrollParams) => {
    if (event) {
      const { scrollLeft } = event;
      if (this.timeAxisRef) {
        this.timeAxisRef.setScrollLeft(scrollLeft);
      }

      if (this.dayBoundaryIndicatorRef) {
        this.dayBoundaryIndicatorRef.scrollDayIndicator(scrollLeft);
      }
    }
  };

  // eslint-disable-next-line react/sort-comp
  private readonly throttledOnScroll = throttle(this.onScroll);

  /**
   * Handles updating scrollLeft for horizontal scrolling. Will do nothing for vertical
   * scrolling, as that is handled natively.
   */
  public readonly onWheel = (event: React.WheelEvent<HTMLDivElement>): void => {
    if (!event || !this.virtualizedContainer) return;
    const { deltaX, deltaY, shiftKey } = event;

    let scrollValue: number;
    if (deltaX !== 0) {
      // Mac & trackpads
      // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
      scrollValue = this.virtualizedContainer.scrollLeft + deltaX;
    } else if (shiftKey) {
      // On Windows machines, deltaX is always 0 even with shift held down. Fall back to Y.
      // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
      scrollValue = this.virtualizedContainer.scrollLeft + deltaY;
    } else {
      // Vertical scrolling is handled natively; do nothing.
      return;
    }

    if (this.timeAxisRef) {
      this.timeAxisRef.setScrollLeft(scrollValue);
    }
    this.updateScrollLeft(scrollValue);
  };

  private readonly onMouseUp = () => {
    this.mouseDown = false;
    document.removeEventListener('mouseup', this.onMouseUp);
  };

  private readonly onMouseDown = (event: React.MouseEvent<HTMLDivElement>) => {
    if (event) {
      this.mouseDown = true;
      document.addEventListener('mouseup', this.onMouseUp);
    }
  };

  private readonly onMouseMove = (event: React.MouseEvent<HTMLDivElement>) => {
    const deltaY = event.movementY;
    if (event) {
      if (this.mouseDown) {
        if (this.virtualizedContainer) {
          if (this.timeAxisRef) {
            // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
            this.timeAxisRef.setScrollLeft(this.virtualizedContainer.scrollLeft + deltaY);
          }
          // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
          this.updateScrollLeft(this.virtualizedContainer.scrollLeft + deltaY);
        }
      }
    }
  };

  /**
   * Pans the table by the provided number of seconds
   *
   * @param seconds the number of seconds to pan
   */
  public readonly panBy = (seconds: number): void => {
    if (this.virtualizedContainer) {
      const { timeRange } = this.props;
      const { scaleToPosition, scaleToTime } = getScaleForTimeRange(timeRange);
      const time = scaleToTime(this.virtualizedContainer.scrollLeft) + seconds;
      this.updateScrollLeft(scaleToPosition(time));
    }
  };

  /**
   * @returns the current time range display as a string
   */
  private readonly updateCurrentTimeRange = (): void => {
    if (this.timeRangeRef) {
      if (this.virtualizedContainer) {
        const { timeRange } = this.props;
        const { scaleToTime } = getScaleForTimeRange(timeRange);
        const startTime = scaleToTime(this.virtualizedContainer.scrollLeft);
        const endTime = scaleToTime(
          // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
          this.virtualizedContainer.scrollLeft + this.virtualizedContainer.clientWidth
        );
        this.timeRangeRef.update(startTime, endTime);
      }
    }
  };

  /**
   * @param workflow the workflow
   * @param stageIntervals the stage intervals
   * @returns the table data
   */
  private readonly getTableData = (
    workflow: WorkflowTypes.Workflow,
    stageIntervals: StageIntervalList
  ): Immutable.List<TableCell> => {
    const {
      timeRange: { startTimeSecs, endTimeSecs }
    } = this.props;
    const listData: ValueOrEmpty[][] = flatMap(
      workflow.stages.map(stage => {
        const intervals = stageIntervals.find(si => si.name === stage.name)?.value ?? [];
        if (!this.memoizedProcessData.has(stage.name)) {
          this.memoizedProcessData = this.memoizedProcessData.set(
            stage.name,
            memoizeOne(processData, isEqual)
          );
        }

        // add empty buffers to the start and end to ensure that the entire scroll region is rendered
        const startBuffer: Empty = {
          startTime: startTimeSecs,
          endTime: startTimeSecs + HUNDRED_TWENTY_SECOND_BUFFER
        };
        const endBuffer: Empty = {
          startTime: endTimeSecs - HUNDRED_TWENTY_SECOND_BUFFER,
          endTime: endTimeSecs
        };

        return this.memoizedProcessData
          .get(stage.name)(stage, intervals)
          .map(v => [startBuffer, ...v, endBuffer]);
      })
    );

    const { expandedDataMap } = this.state;
    const dataToShow: ValueOrEmpty[][] = [];
    let index = -1;

    expandedDataMap.forEach(entry => {
      index += 1;
      dataToShow.push(listData[index]);
      if (entry.isExpanded) {
        entry.subRows.forEach((subRow, i) => {
          dataToShow.push(listData[index + (i + 1)]);
        });
      }
      index += entry.subRows.length;
    });

    return Immutable.List(
      flatMap(
        dataToShow.map((row, rowIndex) => {
          const startIndex = dataToShow.reduce(
            (prev, val, key) => (key < rowIndex ? prev + val.length : prev),
            0
          );
          return row.map((cell, colIndex) => ({
            index: startIndex + colIndex,
            rowIndex,
            colIndex,
            value: cell
          }));
        })
      )
    );
  };

  /**
   * @returns current viewable min start time
   */
  public readonly getViewableMinStartTime = (): number => {
    const { timeRange } = this.props;
    if (this.virtualizedContainer && timeRange) {
      const { scaleToTime } = getScaleForTimeRange(timeRange);
      return scaleToTime(this.virtualizedContainer.scrollLeft);
    }
    return undefined;
  };

  public render(): JSX.Element {
    logger.debug(`Rendering WorkflowTable`, this.props);

    const { widthPx, heightPx, timeRange, stageIntervals, workflow } = this.props;
    const { expandedDataMap, overrideScrollLeft } = this.state;
    this.previousMinStartTime = this.currentMinStartTime;
    this.data = this.getTableData(workflow, stageIntervals);

    const height = heightPx - TOOLBAR_HEIGHT_PX - TIME_AXIS_HEIGHT;

    const scrollBarWidth: number = memoizedGetScrollBarWidth();

    const width =
      widthPx -
      TABLE_SIDE_PADDING -
      TABLE_BUTTON_WIDTH -
      TABLE_LABEL_WIDTH -
      TABLE_SIDE_PADDING -
      scrollBarWidth;

    const tableHeight = this.getRowCount() * HEIGHT + scrollBarWidth;

    return (
      <div
        className="workflow-table"
        style={{ height: `${height}px` }}
        ref={ref => {
          if (ref) this.workflowTableRef = ref;
        }}
      >
        <div className="workflow-table-container">
          <div className="workflow-table__curtain-left" style={{ height: `${tableHeight}px` }} />
          <div className="workflow-table__buttons">
            {[...expandedDataMap.values()].map(row => {
              return (
                <div
                  key={`expand ${row.name}`}
                  className="workflow-button"
                  data-cy={`workflow-expand-${row.name}`}
                >
                  <div key={`main row ${row.name}`} className="workflow-button__expand_collapse">
                    <StageExpansionButton
                      isExpanded={row.isExpanded}
                      disabled={false}
                      stageName={row.name}
                      toggle={() => {
                        this.updateExpandedDataMap(row.name);
                      }}
                    />
                  </div>
                  {row.isExpanded
                    ? row.subRows.map(subRow => (
                        <div
                          className="workflow-button__blank"
                          key={`sub row ${row.name} ${subRow.name}`}
                        />
                      ))
                    : undefined}
                </div>
              );
            })}
          </div>
          <div>
            <DayBoundaryIndicator
              timeRange={timeRange}
              width={width}
              height={tableHeight}
              ref={ref => {
                this.dayBoundaryIndicatorRef = ref;
              }}
            />
            <div
              role="presentation"
              style={{ height: tableHeight }}
              data-cy="workflow-table__container"
              onWheel={this.onWheel}
              onMouseDown={this.onMouseDown}
              onMouseUp={this.onMouseUp}
              onMouseMove={throttle(this.onMouseMove)}
            >
              <Collection
                key={`table_${width}`}
                height={tableHeight}
                width={width}
                cellCount={this.getCellCount()}
                cellRenderer={this.cellRenderer}
                cellSizeAndPositionGetter={this.cellSizeAndPositionGetter}
                noContentRenderer={this.noContentRenderer}
                onScroll={this.throttledOnScroll}
                onSectionRendered={this.throttledOnSectionRendered}
                data={this.data}
              />
            </div>
          </div>
          <div className="workflow-table__labels" style={{ height: `${tableHeight}px` }}>
            {flatMap<JSX.Element>(
              [...expandedDataMap.values()].map<JSX.Element[]>(row => {
                const mainRow = (
                  <WorkflowRowLabel
                    key={`label ${row.name}`}
                    label={row.name}
                    isActivityRow={false}
                  />
                );
                const subRows = row.isExpanded
                  ? row.subRows.map(subRow => (
                      <WorkflowRowLabel
                        key={`label ${row.name} ${subRow.name} ${subRow.isActivityRow}`}
                        label={subRow.name}
                        isActivityRow={subRow.isActivityRow}
                      />
                    ))
                  : [];
                return [mainRow, ...subRows];
              })
            )}
          </div>
          <div className="workflow-table__curtain-right" style={{ height: `${tableHeight}px` }} />
        </div>
        <div className="workflow-table__time-axis">
          <div className="scroll-bar-override" style={{ width: `${width}px` }}>
            {getVirtualizedInnerContainer() !== null ? (
              <ScrollBarOverride
                targetElement={getVirtualizedInnerContainer()}
                orientation="x"
                scrollLeft={overrideScrollLeft}
              />
            ) : undefined}
          </div>
          <WorkflowTimeAxis
            timeRange={timeRange}
            width={width}
            ref={ref => {
              this.timeAxisRef = ref;
            }}
          />
          <TimeRange
            ref={ref => {
              this.timeRangeRef = ref;
            }}
            startTime={timeRange.startTimeSecs}
            endTime={timeRange.endTimeSecs}
          />
        </div>
      </div>
    );
  }
}
