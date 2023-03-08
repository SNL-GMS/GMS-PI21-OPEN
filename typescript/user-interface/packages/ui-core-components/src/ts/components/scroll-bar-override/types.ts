export interface ScrollBarOverrideProps {
  readonly targetElement: Element;
  readonly scrollLeft: number;
  readonly orientation: 'x' | 'y';
  readonly className?: string;
}
