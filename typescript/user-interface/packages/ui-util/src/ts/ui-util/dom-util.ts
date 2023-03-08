import memoizeOne from 'memoize-one';

/**
 * Returns true if it is a DOM element
 *
 * @param element
 */
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any
export function isDomElement(element: any): element is Element {
  return element instanceof Element || element instanceof HTMLDocument;
}

/**
 * @param element
 * @returns the first parent of the provided element that is scrollable, or undefined
 * if none are found. If the element itself is scrollable, then this will return
 * that same original element.
 */
export function getScrollParent(element: Element): Element {
  if (!element) {
    return undefined;
  }

  if (element.scrollHeight > element.clientHeight) {
    return element;
  }
  return getScrollParent(element.parentElement);
}

/**
 * Calculates the width of the given rendered text.
 *
 * @param text the text string to render and calculate the width
 * @param font the font style of the text
 * @returns
 */
export const getTextWidth = (text: string, font: string = null): number => {
  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');
  context.font = font || getComputedStyle(document.body).font;
  return context.measureText(text).width;
};

/**
 * Calculates browser/OS specific scrollbar width
 *
 * @returns the scroll bar width for the current browser
 */
export function getScrollBarWidth(): number {
  const inner = document.createElement('p');
  inner.style.width = '100%';
  inner.style.height = '200px';

  const outer = document.createElement('div');
  outer.style.position = 'absolute';
  outer.style.top = '0px';
  outer.style.left = '0px';
  outer.style.visibility = 'hidden';
  outer.style.width = '200px';
  outer.style.height = '150px';
  outer.style.overflow = 'hidden';
  outer.appendChild(inner);

  document.body.appendChild(outer);
  const w1 = inner.offsetWidth;
  outer.style.overflow = 'scroll';
  let w2 = inner.offsetWidth;
  if (w1 === w2) w2 = outer.clientWidth;

  document.body.removeChild(outer);

  return w1 - w2;
}

export const memoizedGetScrollBarWidth = memoizeOne(getScrollBarWidth);

/**
 * Check if an element provided is "out of view" due to scrolling or overflow.
 *
 * @param element the element to check
 * @param threshold how permissive to be.
 * A number > 0 will count the element as out of view before it is fully off the screen
 * @returns true if it is out of view. False if not. Undefined if element is falsy.
 */
export const isElementOutOfView: (element: Element, threshold?: number) => boolean = (
  element: Element,
  threshold = 20
) => {
  if (element) {
    const bounding = element.getBoundingClientRect();
    const scrollBounding = getScrollParent(element).getBoundingClientRect();
    return (
      scrollBounding.bottom - bounding.bottom < threshold ||
      bounding.top - scrollBounding.top < threshold
    );
  }
  return undefined;
};
