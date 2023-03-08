import type { FilterDescription } from './filter-description';

export interface VectorFilterDescriptionModule {
  new (): VectorFilterDescription;
}

export interface VectorFilterDescription {
  get: (index: number) => FilterDescription;

  push_back: (index: FilterDescription) => void;

  set: (index: number, value: FilterDescription) => void;

  size: () => number;
}
