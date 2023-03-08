/**
 * Arbitrary css style definitions
 */
export interface Styles {
  [name: string]: string;
}

/**
 * Creates a facsimile of a details cell for rendering
 * a drag image of one or more cells being dragged.
 */
export class CellImage {
  private readonly leftIndent: number = 1;

  private readonly topIndent: number = -1.5;

  private readonly opacity: number = 0.3;

  private readonly zIndex: number = 4;

  private readonly message: string;

  private readonly element: HTMLElement;

  /**
   * @param iteration which drag cell number is this?
   * The higher the iteration, the farther down and over,
   * and the more transparent it will be rendered.
   * @param message the text that goes in the cell.
   * Background cells should still be given text to set their
   * size correctly, but it will be rendered as transparent text
   * so it only contributes to the size of the cell.
   * @param isBackground should the cell be rendered as
   * a background cell or not? Background cells will be
   * farther behind and more transparent.
   */
  public constructor(iteration: number, message: string, isBackground: boolean) {
    this.leftIndent *= iteration;
    this.topIndent *= iteration;
    this.opacity = 1 - this.opacity * iteration;
    this.zIndex -= iteration;
    this.message = message;
    this.element = isBackground ? this.createBackgroundElement() : this.createCellElement();
  }

  /**
   * @returns a HTMLElement with styles applied for rendering in the DOM.
   */
  public readonly getElement = (): HTMLElement => this.element;

  private readonly createBackgroundElement = () => {
    const styles: Styles = {
      left: `${this.leftIndent}em`,
      top: `${this.topIndent}em`,
      color: 'transparent',
      opacity: `${this.opacity}`
    };
    return this.createCellElement(styles);
  };

  /**
   * applies default styles and any extras provided
   */
  private readonly applyCellStyles = (el: HTMLElement, s: Styles) => {
    const styles = {
      position: 'relative',
      background: 'var(--gms-input-highlight)',
      borderRadius: '4px',
      padding: '.5rem 1em',
      boxShadow: '3px 3px 3px rgba(0, 0, 0, .25)',
      fontSize: '1.25em',
      zIndex: `${this.zIndex}`,
      ...s
    };
    Object.assign(el.style, styles);
  };

  /**
   * creates an HTMLDiv element, applies the styles, and
   * adds the message as innerHTML
   */
  private readonly createCellElement = (styles?: Styles) => {
    const el = document.createElement('div');
    this.applyCellStyles(el, styles);
    el.innerHTML = this.message;
    return el;
  };
}
