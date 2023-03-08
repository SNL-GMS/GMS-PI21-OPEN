export enum FilterBandType {
  LOW_PASS = 0,
  HIGH_PASS = 1,
  BAND_PASS = 2,
  BAND_REJECT = 3
}

export interface IFilterBandType {
  LOW_PASS: {
    value: FilterBandType.LOW_PASS;
  };

  HIGH_PASS: {
    value: FilterBandType.HIGH_PASS;
  };

  BAND_PASS: {
    value: FilterBandType.BAND_PASS;
  };

  BAND_REJECT: {
    value: FilterBandType.BAND_REJECT;
  };
}
