export interface PositionConverters {
  /**
   * Computes the time in seconds for the mouse x position.
   *
   * @param mouseXPosition the mouse x position from 0 to 1, where 0 is the far left and 1 is the far right
   *
   * @returns The computed time in seconds
   */
  computeTimeSecsForMouseXFractionalPosition(mouseXFraction: number): number;

  /**
   * Converts pixels into epoch seconds.
   *
   * @param mouseXPx the offset in pixels from the left side of the screen
   * @returns the computed time in epoch seconds
   */
  computeTimeSecsFromMouseXPixels(mouseXPx: number): number;

  /**
   * Converts pixel x position (from left side of the screen) into a fraction
   * of the position on the canvas, with 0 representing the left side
   * and 1 representing the right side of the canvas. Negative numbers are possible,
   * as are numbers greater than 1.
   *
   * @param mouseXPx the pixel offset from the left side of the screen
   * @returns the fractional position on the canvas
   */
  computeFractionOfCanvasFromMouseXPx(mouseXPx: number): number;
}
