import * as Cesium from 'cesium';

import { dataAcquisitionUserPreferences } from '~components/data-acquisition-ui/config';
import fonts from '~css/gms-fonts.scss';

export const colorDictionary = {
  GOOD: Cesium.Color.fromCssColorString(dataAcquisitionUserPreferences.colors.ok),
  MARGINAL: Cesium.Color.fromCssColorString(dataAcquisitionUserPreferences.colors.warning),
  BAD: Cesium.Color.fromCssColorString(dataAcquisitionUserPreferences.colors.strongWarning),
  NONE: Cesium.Color.fromCssColorString(dataAcquisitionUserPreferences.colors.none)
};

export const fontStyle = `14px ${fonts.gmsSans}`;
export const monoFontStyle = `14px ${fonts.gmsMono}`;
export const monoFontStyleNoSize = `${fonts.gmsMono}`;
export const MAP_MIN_HEIGHT_PX = 500;

export const imageScale = 0.12;
const imageScaleMultiplier = 1.6;
export const imageScaleSelected = imageScale * imageScaleMultiplier;
const yCartesian = 10;
export const unselectedPixelOffset = new Cesium.Cartesian2(0, yCartesian);
export const selectedPixelOffset = new Cesium.Cartesian2(0, yCartesian + 2);
