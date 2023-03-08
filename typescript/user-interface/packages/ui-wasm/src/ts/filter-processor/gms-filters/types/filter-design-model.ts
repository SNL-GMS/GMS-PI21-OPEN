export enum FilterDesignModel {
  BUTTERWORTH = 0,
  CHEBYSHEV_I = 1,
  CHEBYSHEV_II = 2,
  ELLIPTIC = 3
}

export interface IFilterDesignModel {
  BUTTERWORTH: {
    value: FilterDesignModel.BUTTERWORTH;
  };

  CHEBYSHEV_I: {
    value: FilterDesignModel.CHEBYSHEV_I;
  };

  CHEBYSHEV_II: {
    value: FilterDesignModel.CHEBYSHEV_II;
  };

  ELLIPTIC: {
    value: FilterDesignModel.ELLIPTIC;
  };
}
