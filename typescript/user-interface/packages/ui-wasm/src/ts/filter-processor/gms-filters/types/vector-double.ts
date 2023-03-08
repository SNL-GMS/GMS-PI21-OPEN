export interface VectorDoubleModule {
  new (): VectorDouble;
}

export interface VectorDouble {
  get: (index: number) => number;

  push_back: (value: number) => void;

  set: (index: number, value: number) => void;

  size: () => number;

  resize: (size: number, sizeBytes: number) => void;
}
