/* eslint-disable react/destructuring-assignment */
import { Colors, Popover, PopoverInteractionKind, Position } from '@blueprintjs/core';
import type { FkTypes, LegacyEventTypes } from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import * as d3 from 'd3';
import isEqual from 'lodash/isEqual';
import React from 'react';

import { getFkData } from '~analyst-ui/common/utils/fk-utils';

import { FK_RENDERING_HEIGHT_OFFSET, SIZE_OF_FK_RENDERING_AXIS_PX } from '../../constants';
import type { FkUnits } from '../../types';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import * as fkUtil from '../fk-util';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { FkColorScale } from './fk-color-scale';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { FkLegend } from './fk-legend';

/**
 * FkRendering Props
 */
export interface FkRenderingProps {
  data: SignalDetectionTypes.SignalDetection;
  signalDetectionFeaturePredictions: LegacyEventTypes.FeaturePrediction[];
  analystCurrentFk: AnalystCurrentFk;
  fkUnitDisplayed: FkUnits;
  renderingHeightPx: number;
  renderingWidthPx: number;
  currentMovieSpectrumIndex: number;

  updateCurrentFk(point: AnalystCurrentFk): void;
}

/**
 * FkRendering State
 */
export interface FkRenderingState {
  // the min/max value in the fk data
  minFkValue: number;
  maxFkValue: number;
  currentFkDisplayData: number[][];
}

/**
 * The X,Y coordinates for the analysts dot in the fk
 */
export interface AnalystCurrentFk {
  x: number;
  y: number;
}

/**
 * FkRendering Component
 */
export class FkRendering extends React.Component<FkRenderingProps, FkRenderingState> {
  /** Reference to the canvas to draw the fk. */
  private canvasRef: HTMLCanvasElement | undefined;

  /** Used to resize the canvas to fit the container. */
  private containerRef: HTMLDivElement | undefined;

  /** The current fk represented as an ImageBitmap. */
  private currentImage: ImageBitmap | undefined;

  /** The y-axis div container. */
  private yAxisContainerRef: HTMLDivElement | undefined;

  /** The x-axis div container. */
  private xAxisContainerRef: HTMLDivElement | undefined;

  public constructor(props: FkRenderingProps) {
    super(props);
    this.state = {
      minFkValue: 0,
      maxFkValue: 1,
      currentFkDisplayData: [] // Init empty (double array)
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div
        className="fk"
        style={{
          height: `${this.props.renderingHeightPx + SIZE_OF_FK_RENDERING_AXIS_PX}px`,
          width: `${this.props.renderingWidthPx + SIZE_OF_FK_RENDERING_AXIS_PX}px`
        }}
      >
        <div
          ref={ref => {
            this.containerRef = ref;
          }}
          className="fk-rendering"
        >
          <div
            ref={ref => {
              this.yAxisContainerRef = ref;
            }}
            className="fk-rendering__y-axis"
          />
          <div
            ref={ref => {
              this.xAxisContainerRef = ref;
            }}
            className="fk-rendering__x-axis"
          />
          <div className="fk-rendering__slowness">
            <Popover
              interactionKind={PopoverInteractionKind.CLICK}
              position={Position.BOTTOM}
              modifiers={{
                constraints: [{ attachment: 'together', to: 'scrollParent' }]
              }}
            >
              <div className="fk-color-scale__button">slowness (s/??)</div>
              <div className="fk-rendering__slowness-label">
                <FkLegend />
                <FkColorScale
                  minSlow={this.state.minFkValue}
                  maxSlow={this.state.maxFkValue}
                  fkUnits={this.props.fkUnitDisplayed}
                />
              </div>
            </Popover>
          </div>
          <canvas
            className="fk-rendering__canvas"
            data-cy="primary-fk-rendering"
            width={this.props.renderingWidthPx}
            height={this.props.renderingHeightPx - FK_RENDERING_HEIGHT_OFFSET}
            ref={ref => {
              this.canvasRef = ref;
            }}
            onClick={this.onPrimaryFkClick}
          />
        </div>
      </div>
    );
  }

  /**
   * React component lifecycle
   */
  public async componentDidMount(): Promise<void> {
    const fkData = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(this.props.data.signalDetectionHypotheses)
        .featureMeasurements
    );
    const predictedPoint = fkUtil.getPredictedPoint(this.props.signalDetectionFeaturePredictions);
    const fkDisplaySpectrum = fkData.spectrums[this.props.currentMovieSpectrumIndex];
    const fkDisplayData = fkUtil.getFkHeatmapArrayFromFkSpectra(
      fkDisplaySpectrum,
      this.props.fkUnitDisplayed
    );
    const [min, max] = fkUtil.computeMinMaxFkValues(fkDisplayData);
    this.currentImage = await fkUtil.createFkImageBitmap(fkDisplayData, min, max);
    const { analystCurrentFk } = this.props;
    // TODO: Not sure why but sometimes the canvas ref is not set on some
    // TODO: event selections
    // Guard against canvas ref is not yet set (happens in render)
    if (this.canvasRef) {
      fkUtil.draw(
        this.canvasRef,
        this.containerRef,
        this.currentImage,
        fkData,
        predictedPoint,
        this.props.currentMovieSpectrumIndex,
        0,
        false,
        false,
        analystCurrentFk
      );
      this.drawAxis(fkData);
      this.setState({
        maxFkValue: max,
        minFkValue: min,
        currentFkDisplayData: fkDisplayData
      });
    }
  }

  /**
   * React component lifecycle
   */
  public async componentDidUpdate(prevProps: FkRenderingProps): Promise<void> {
    const currentFkData = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(this.props.data.signalDetectionHypotheses)
        .featureMeasurements
    );
    const predictedPoint = fkUtil.getPredictedPoint(this.props.signalDetectionFeaturePredictions);
    const newFkDisplayData = fkUtil.getFkHeatmapArrayFromFkSpectra(
      currentFkData.spectrums[this.props.currentMovieSpectrumIndex],
      this.props.fkUnitDisplayed
    );

    if (!isEqual(this.state.currentFkDisplayData, newFkDisplayData)) {
      const [min, max] = fkUtil.computeMinMaxFkValues(newFkDisplayData);
      this.currentImage = await fkUtil.createFkImageBitmap(newFkDisplayData, min, max);
      fkUtil.draw(
        this.canvasRef,
        this.containerRef,
        this.currentImage,
        currentFkData,
        predictedPoint,
        this.props.currentMovieSpectrumIndex
      );
      this.drawAxis(currentFkData);
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        maxFkValue: max,
        minFkValue: min,
        currentFkDisplayData: newFkDisplayData
      });
    } else if (
      this.props.renderingHeightPx !== prevProps.renderingHeightPx ||
      this.props.renderingWidthPx !== prevProps.renderingWidthPx
    ) {
      fkUtil.draw(
        this.canvasRef,
        this.containerRef,
        this.currentImage,
        currentFkData,
        predictedPoint,
        this.props.currentMovieSpectrumIndex
      );
      this.drawAxis(currentFkData);
    }
  }

  /**
   * Draws the x and y axis
   *
   * @param fkDate Fk data to create axis for
   */
  private readonly drawAxis = (fkSpectrum: FkTypes.FkPowerSpectra) => {
    this.createXAxis(fkSpectrum);
    this.createYAxis(fkSpectrum);
  };

  /**
   * Create and draw the x-axis.
   *
   * @param fkDate Fk data to create axis for
   */
  private readonly createXAxis = (fkData: FkTypes.FkPowerSpectra) => {
    if (!this.xAxisContainerRef) return;
    this.xAxisContainerRef.innerHTML = '';

    const svg = d3
      .select(this.xAxisContainerRef)
      .append('svg')
      .attr('width', this.xAxisContainerRef.clientWidth)
      .attr('height', this.xAxisContainerRef.clientHeight)
      .style('fill', Colors.LIGHT_GRAY5);

    const svgAxis = svg.append('g').attr('class', 'fk-axis');

    const padding = 10;
    const x = d3
      .scaleLinear()
      .domain(fkUtil.getXAxisForFkSpectra(fkData))
      .range([padding, this.xAxisContainerRef.clientWidth - padding - 1]);

    const tickSize = 7;
    const xAxis = d3.axisBottom(x).tickSize(tickSize);
    svgAxis.call(xAxis);
  };

  /**
   * Create and draw the y-axis.
   *
   * @param fkDate Fk data to create axis for
   */
  private readonly createYAxis = (fkData: FkTypes.FkPowerSpectra) => {
    if (!this.yAxisContainerRef) return;
    this.yAxisContainerRef.innerHTML = '';

    const svg = d3
      .select(this.yAxisContainerRef)
      .append('svg')
      .attr('width', this.yAxisContainerRef.clientWidth)
      .attr('height', this.yAxisContainerRef.clientHeight)
      .style('fill', Colors.LIGHT_GRAY5);

    const svgAxis = svg.append('g').attr('class', 'fk-axis').attr('transform', 'translate(34, 0)');

    const padding = 10;
    const y = d3
      .scaleLinear()
      .domain(fkUtil.getYAxisForFkSpectra(fkData))
      .range([this.yAxisContainerRef.clientHeight - padding - 1, padding]);

    const tickSize = 7;
    const yAxis = d3.axisLeft(y).tickSize(tickSize);
    svgAxis.call(yAxis);
  };

  /**
   * When primary fk is clicked, will draw black circle
   */
  private readonly onPrimaryFkClick = async (e: React.MouseEvent<HTMLCanvasElement>) => {
    const x = e.clientX - e.currentTarget.getBoundingClientRect().left;
    const y = e.clientY - e.currentTarget.getBoundingClientRect().top;

    const fkData = getFkData(
      SignalDetectionTypes.Util.getCurrentHypothesis(this.props.data.signalDetectionHypotheses)
        .featureMeasurements
    );
    const predictedPoint = fkUtil.getPredictedPoint(this.props.signalDetectionFeaturePredictions);

    const { currentFkDisplayData } = this.state;
    const [min, max] = fkUtil.computeMinMaxFkValues(currentFkDisplayData);
    this.currentImage = await fkUtil.createFkImageBitmap(currentFkDisplayData, min, max);

    fkUtil.draw(
      this.canvasRef,
      this.containerRef,
      this.currentImage,
      fkData,
      predictedPoint,
      this.props.currentMovieSpectrumIndex,
      0,
      false,
      false,
      { x, y }
    );
    const scaledXY = fkUtil.convertGraphicsXYtoCoordinate(
      x,
      y,
      fkData,
      this.canvasRef.width,
      this.canvasRef.height
    );
    if (this.props.updateCurrentFk) {
      // Converting x y point from graphics space to coordinate space
      this.props.updateCurrentFk({ x: scaledXY.x, y: scaledXY.y });
    }
  };
}
