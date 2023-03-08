export enum FilterComputationType {
  FIR = 0,
  IIR = 1,
  AR = 2,
  PM = 3
}

export interface IFilterComputationType {
  FIR: {
    value: FilterComputationType.FIR;
  };

  IIR: {
    value: FilterComputationType.IIR;
  };

  AR: {
    value: FilterComputationType.AR;
  };

  PM: {
    value: FilterComputationType.PM;
  };
}
