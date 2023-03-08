import React from 'react';

import { gmsColors } from '~scss-config/color-preferences';

// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import * as fkUtil from '../fk-util';

/**
 * Class that renders the FK Display legend which describes peak, predicted, and analyst
 */
export class FkLegend extends React.PureComponent {
  /** Reference to the canvas to draw different dots for the legend */
  private canvasRef: HTMLCanvasElement | undefined;

  /** Canvas rendering context used to draw different dots for the legend */
  private ctx: CanvasRenderingContext2D;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount(): void {
    this.ctx = this.canvasRef.getContext('2d');
    this.drawFkLegend();
  }

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div className="fk-legend">
        <canvas
          className="fk-legend__canvas"
          ref={ref => {
            this.canvasRef = ref;
          }}
        />
        <div className="fk-legend-font__analyst">Selected</div>
        <div className="fk-legend-font__peak">Peak</div>
        <div className="fk-legend-font__predicted">Predicted</div>
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Draws the fk display legend
   */
  private readonly drawFkLegend = () => {
    const xForDots = 6;
    const yForAnalystDot = 12;
    const yForPredictedDot = 28;
    const yForCrossHairDot = 44;
    const canvasHeight = 80;
    const canvasWidth = 20;

    this.canvasRef.width = canvasWidth;
    this.canvasRef.height = canvasHeight;

    fkUtil.drawCircle(
      this.ctx,
      xForDots,
      yForAnalystDot,
      [fkUtil.markerRadiusSize],
      gmsColors.gmsRecessed,
      true
    );
    fkUtil.drawCircle(
      this.ctx,
      xForDots,
      yForPredictedDot,
      [fkUtil.markerRadiusSize],
      gmsColors.gmsMain,
      true
    );
    fkUtil.drawCircle(
      this.ctx,
      xForDots,
      yForCrossHairDot,
      [fkUtil.markerRadiusSize],
      gmsColors.gmsMain,
      true
    );
    fkUtil.drawCrosshairDot(
      this.ctx,
      xForDots,
      yForCrossHairDot,
      gmsColors.gmsRecessed,
      fkUtil.markerRadiusSize
    );
  };
}
