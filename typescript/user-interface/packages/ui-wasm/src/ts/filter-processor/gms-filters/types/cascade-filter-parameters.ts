export interface CascadedFiltersParametersModule {
  build: (
    comments: string,
    isCausal: boolean,
    sampleRate: number,
    sampleRateTolerance: number,
    groupDelay: number
  ) => CascadedFiltersParameters;
}

export interface CascadedFiltersParameters {
  comments: string;
  isCausal: boolean;
  sampleRate: number;
  sampleRateTolerance: number;
  groupDelay: number;
  delete: () => void;
}
