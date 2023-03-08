/**
 * A collection of the physical dimensions in the DOM. These are calculated in a batch
 * in order to reduce calculation time during critical points in time, such as
 * requestAnimationFrame calls.
 */
export interface WeavessContainerDimensions {
  viewport: {
    clientHeight: number;
    clientWidth: number;
    scrollHeight: number;
    scrollWidth: number;
    scrollLeft: number;
    scrollTop: number;
  };
  viewportContentContainer: {
    clientWidth: number;
  };
  canvas: {
    rect: DOMRect;
    offsetWidth: number;
    offsetHeight: number;
    clientWidth: number;
  };
}
