import variables from '~css/utils/variables.scss';

const convertPxStringToNumber = (str: string) => parseInt(str.substring(0, str.length - 2), 10);

export interface GMSLayout {
  displayPadding: string;
  displayPaddingPx: number;
  minChartHeightPx: number;
  minChartWidthPx: number;
}

export const gmsLayout: GMSLayout = {
  displayPadding: variables.displayPadding,
  displayPaddingPx: convertPxStringToNumber(variables.displayPadding),
  minChartHeightPx: convertPxStringToNumber(variables.minChartHeight),
  minChartWidthPx: convertPxStringToNumber(variables.minChartWidth)
};
