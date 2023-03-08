/* eslint-disable @typescript-eslint/restrict-plus-operands */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { ColorTypes } from '@gms/common-model';
import { uniqueNumberFromString } from '@gms/common-util';
import * as Immutable from 'immutable';
import isObject from 'lodash/isObject';

// CONSTANTS
const GOLDEN_RATIO_CONJUGATE = 0.618033988749895;
const DEGREES_IN_A_CIRCLE = 360;
const HUNDRED_PERCENT = 100;
const DEFAULT_SATURATION = 0.5;
const DEFAULT_VALUE = 0.7;

export const isHexCharacter = (
  candidate: string | number
): candidate is ColorTypes.HexCharacter => {
  if (candidate == null) return false;
  return /^[0-9A-Fa-f]{1}$/g.test(candidate.toString());
};

export const isHexOctet = (candidate: string | number): candidate is ColorTypes.HexOctet => {
  if (candidate == null) return false;
  return /^[0-9A-Fa-f]{2}$/g.test(candidate.toString());
};

export const isHexShorthand = (hex: string): boolean => {
  return /^#?([a-f\d])([a-f\d])([a-f\d])$/i.test(hex);
};

export const isHexColor = (
  candidate: string | number | ColorTypes.RGB
): candidate is ColorTypes.Hex => {
  return /^#[0-9A-Fa-f]{6}$/g.test(candidate.toString()) || isHexShorthand(candidate.toString());
};

export const isRGB = (
  candidate: ColorTypes.Hex | ColorTypes.RGB | string | Record<string, unknown>
): candidate is ColorTypes.RGB => {
  return (
    candidate != null &&
    isObject(candidate) &&
    'red' in candidate &&
    'green' in candidate &&
    'blue' in candidate
  );
};

/**
 * @see https://stackoverflow.com/questions/5623838/rgb-to-hex-and-hex-to-rgb
 */
export function hexToRgb(hex: ColorTypes.Hex): ColorTypes.RGB {
  // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
  const fullHex = hex.replace(/^#?([a-f\d])([a-f\d])([a-f\d])$/i, (m, r, g, b) => {
    // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
    return r + r + g + g + b + b;
  });

  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(fullHex);
  return result
    ? {
        red: parseInt(result[1], 16),
        green: parseInt(result[2], 16),
        blue: parseInt(result[3], 16)
      }
    : null;
}

/**
 * @see https://stackoverflow.com/questions/5623838/rgb-to-hex-and-hex-to-rgb
 */
export function rgbToHex(rgb: ColorTypes.RGB): ColorTypes.Hex {
  const { red, green, blue } = rgb;
  // eslint-disable-next-line no-bitwise
  return `#${((1 << 24) + (red << 16) + (green << 8) + blue).toString(16).slice(1)}`;
}

/**
 * A HSV Color
 * hue: 0 to 360 degrees
 * saturation: 0 to 100 percent
 * value: 0 to 100 percent
 */
export interface HSL {
  hue: number;
  saturation: number;
  lightness: number;
}

/**
 * Converts HSV to HSL colors based on this Stack Overflow
 * discussion: https://stackoverflow.com/questions/3423214/convert-hsb-hsv-color-to-hsl
 *
 * @param h hue between 0 and 360 degrees
 * @param s saturation between 0 and 100 percent
 * @param v value between 0 and 100 percent
 */
export function hsvToHSL(h: number, s: number, v: number): HSL {
  const lightness = ((2 - s) * v) / 2;
  const saturation =
    lightness === 0 || lightness === 1 ? 0 : (v - lightness) / Math.min(lightness, 1 - lightness);
  return {
    hue: h,
    saturation: saturation * HUNDRED_PERCENT,
    lightness: lightness * HUNDRED_PERCENT
  };
}

/**
 * Converts an HSL color into a css-formatted string of the form:
 *   hsl(<hue>deg, <saturation>%, <lightness>%)
 *
 * @param color the HSL color to convert
 */
export const hslToString = (color: HSL): string =>
  `hsl(${color.hue}deg, ${color.saturation}%, ${color.lightness}%)`;

// eslint-disable-next-line complexity
export const hslToHex = (color: HSL): ColorTypes.Hex => {
  // see https://css-tricks.com/converting-color-spaces-in-javascript/
  const h = color.hue;
  // Must be fractions of 1
  const s = color.saturation / 100;
  const l = color.lightness / 100;

  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = l - c / 2;
  let r = 0;
  let g = 0;
  let b = 0;

  if (h >= 0 && h < 60) {
    r = c;
    g = x;
  } else if (h >= 60 && h < 120) {
    r = x;
    g = c;
  } else if (h >= 120 && h < 180) {
    g = c;
    b = x;
  } else if (h >= 180 && h < 240) {
    g = x;
    b = c;
  } else if (h >= 240 && h < 300) {
    r = x;
    b = c;
  } else if (h >= 300 && h < 360) {
    r = c;
    b = x;
  } else {
    throw new RangeError(`Hue must be between 0 and 360, was ${h}`);
  }
  // Having obtained RGB, convert channels to hex
  let rStr = Math.round((r + m) * 255).toString(16);
  let gStr = Math.round((g + m) * 255).toString(16);
  let bStr = Math.round((b + m) * 255).toString(16);

  // Prepend 0s, if necessary
  if (isHexCharacter(rStr)) {
    rStr = `0${r}`;
  }
  if (isHexCharacter(gStr)) {
    gStr = `0${g}`;
  }
  if (isHexCharacter(bStr)) {
    bStr = `0${b}`;
  }
  if (isHexOctet(rStr) && isHexOctet(gStr) && isHexOctet(bStr)) {
    // cast these to string so Typescript doesn't lose its mind.
    return '#'.concat(rStr).concat(gStr).concat(bStr) as ColorTypes.Hex;
  }
  throw new Error('Invalid hex code.');
};

/**
 * Calculates luminance of a color according to the w3 definition.
 *
 * @see http://www.w3.org/TR/WCAG20/#relativeluminancedef
 * @see https://stackoverflow.com/questions/9733288/how-to-programmatically-calculate-the-contrast-ratio-between-two-colors
 *
 * @param color an RGB color from which to calculate the luminance
 * @returns a number between 0 and 1
 */
const luminosity = (color: ColorTypes.RGB) => {
  const lum = [color.red, color.green, color.blue].map(c => {
    const chan = c / 255;
    return chan <= 0.03928 ? chan / 12.92 : ((chan + 0.055) / 1.055) ** 2.4;
  });

  return 0.2126 * lum[0] + 0.7152 * lum[1] + 0.0722 * lum[2];
};

/**
 * Calculates the color contrast of two colors.
 *
 * @see http://www.w3.org/TR/WCAG20/#contrast-ratiodef
 * @see https://stackoverflow.com/questions/9733288/how-to-programmatically-calculate-the-contrast-ratio-between-two-colors
 *
 * @param color1 The first rgb color color.
 * @param color2 The second rgb color.
 * @returns the color contrast, between 1 and 21
 */
export function contrast(color1: ColorTypes.RGB, color2: ColorTypes.RGB): number {
  const lum1 = luminosity(color1);
  const lum2 = luminosity(color2);
  const brightest = Math.max(lum1, lum2);
  const darkest = Math.min(lum1, lum2);
  return (brightest + 0.05) / (darkest + 0.05);
}

/**
 * @param color a color that is either hex or an rgb object. We do not currently support other color types
 * @returns a hex version of the color
 */
export function convertToHex(color: ColorTypes.RGB | ColorTypes.Hex): ColorTypes.Hex {
  if (isRGB(color)) {
    return rgbToHex(color);
  }
  return color;
}

/**
 * @param color a color that is either hex or an rgb object. We do not currently support other color types
 * @returns a hex version of the color
 */
export function convertToRGB(color: ColorTypes.RGB | ColorTypes.Hex): ColorTypes.RGB {
  if (isRGB(color)) {
    return color;
  }
  return hexToRgb(color);
}

/**
 * Checks if a color combo produces text with a AA contrast of 4.5:1 (defined by the WCAG standard, version 2.1).
 *
 * @see https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html
 *
 * @param background the background color
 * @param foreground the color of the text or icon.
 * @returns Whether the foreground color is legible against the background
 */
export const isColorLegible = (
  background: ColorTypes.RGB | ColorTypes.Hex,
  foreground: ColorTypes.RGB | ColorTypes.Hex
): boolean => {
  return contrast(convertToRGB(background), convertToRGB(foreground)) > 4.5;
};

/**
 * Get a color that is most legible against the provided background color. By default it compares
 * white and black foreground colors, but you may provide other colors which can be used instead.
 *
 * @example getLegibleColor({red: 255, green: 0, blue: 0}) // returns white, which is legible against red.
 * @example getLegibleColor({red: 0, green: 255, blue: 0}) // returns black, which is legible against green
 * @example getLegibleColor({red: 0, green: 255, blue: 0}, {red: 24, green: 24, blue: 24}, {red: 240, green: 240, blue: 240}) // Determines which of the last two colors is more legible against the first color. In this case, it's { red: 24, green: 24, blue: 24 }
 *
 * @param backgroundColor the color on which the text will sit
 * @param lightForegroundColor What light text color to compare. @default white.
 * @param darkForegroundColor What dark text color to use. @default black.
 * @returns a color that will be most legible against the provided background color.
 */
export const getLegibleColor = (
  backgroundColor: ColorTypes.RGB | ColorTypes.Hex,
  lightForegroundColor: ColorTypes.RGB | ColorTypes.Hex = { red: 255, green: 255, blue: 255 },
  darkForegroundColor: ColorTypes.RGB | ColorTypes.Hex = { red: 0, green: 0, blue: 0 }
): ColorTypes.RGB => {
  const backgroundRgb: ColorTypes.RGB = convertToRGB(backgroundColor);
  const lightContrast = contrast(convertToRGB(lightForegroundColor), backgroundRgb);
  const darkContrast = contrast(convertToRGB(darkForegroundColor), backgroundRgb);
  return lightContrast > darkContrast
    ? convertToRGB(lightForegroundColor)
    : convertToRGB(darkForegroundColor);
};

/**
 * Get a color that is most legible against the provided background color. By default it compares
 * white and black foreground colors, but you may provide other colors which can be used instead.
 *
 * @example getLegibleHexColor({red: 255, green: 0, blue: 0}) // returns '#FFFFFF', which is legible against red.
 * @example getLegibleHexColor({red: 0, green: 255, blue: 0}) // returns '#000000', which is legible against green
 * @example getLegibleHexColor('#333', '#111', '#999') // returns '#999999', which is more legible against '#333'
 *
 * @param backgroundColor the color on which the text will sit
 * @param lightForegroundColor What light text color to compare. @default white.
 * @param darkForegroundColor What dark text color to use. @default black.
 * @returns a color in the hex format that will be most legible against the provided background color.
 */
export const getLegibleHexColor = (
  backgroundColor: ColorTypes.RGB | ColorTypes.Hex,
  lightForegroundColor: ColorTypes.RGB | ColorTypes.Hex = { red: 255, green: 255, blue: 255 },
  darkForegroundColor: ColorTypes.RGB | ColorTypes.Hex = { red: 0, green: 0, blue: 0 }
): ColorTypes.Hex => {
  return rgbToHex(getLegibleColor(backgroundColor, lightForegroundColor, darkForegroundColor));
};

/**
 * Generates colors that are guaranteed to have a different hue.
 * Colors are generated in HSL format.
 */
export class DistinctColorPalette {
  /**
   * Start as random value. Each time generateRandomColorHSL
   * is called, this will update. Used to ensure that the new
   * color is sufficiently different from previously generated
   * colors.
   */
  private nextHue: number;

  /**
   * The internal map of colors in the palette
   */
  private colorMap: Immutable.Map<string | number, HSL>;

  /**
   * A color palette with distinct colors. Each color will have the same
   * saturation and lightness, and a unique hue.
   *
   * @param size the number of colors to generate.
   * @param keys array of keys
   * @param seed an optional number or string that sets the starting hue.
   * The same seed will always generate the same palette
   */
  public constructor(keys: string[] | number[], seed?: number | string) {
    this.nextHue = this.getHueFromSeed(seed);
    this.colorMap = Immutable.Map<string | number, HSL>();

    this.generateNewListOfDistinctColorsWithKeysHSL(keys);
  }

  /**
   * Adds a new color to the internal color palette.
   * The new color will appear at the end of the list.
   *
   * @returns the HSL color generated
   */
  public addColor(): HSL {
    this.colorMap = this.colorMap.set(this.colorMap.size, this.generateDistinctColorHSL());
    return this.colorMap.get(this.colorMap.size - 1);
  }

  /**
   * Gets all of the HSL colors from the palette.
   *
   * hue: 0 to 360
   * saturation: 0% to 100%
   * value: 0% to 100%
   *
   * @returns the map of HSL colors by predefined key
   */
  public getColors(): Immutable.Map<string | number, HSL> {
    return this.colorMap;
  }

  /**
   * Gets the HSL color with the given key from the palette.
   *
   * hue: 0 to 360
   * saturation: 0% to 100%
   * value: 0% to 100%
   *
   * @throws Error if a key is not in the palette
   * @param key the key of the color in the palette list
   * @returns the HSL color at the given index
   */
  public getColor(key: number | string): HSL {
    if (!key || !this.colorMap.has(key)) {
      throw new Error(`Unknown color key: ${key}`);
    }
    return this.colorMap.get(key);
  }

  /**
   * Returns the keys of the palette
   *
   * @returns keys as (string | number)[]
   */
  public getKeys(): (string | number)[] {
    return [...this.colorMap.keys()];
  }

  /**
   * Get an iterator for colorMap.
   *
   * hue: 0 to 360
   * saturation: 0% to 100%
   * value: 0% to 100%
   *
   * @returns the a iterator of color map
   */
  public getColorStrings(): IterableIterator<HSL> {
    return this.colorMap.values();
  }

  /**
   * Gets a css friendly HSL color string of the format:
   *
   * hsl(30deg, 80%, 95%);
   *
   * hue: 0 to 360
   * saturation: 0% to 100%
   * value: 0% to 100%
   *
   * @param key the key of the color from the palette
   * @returns the color in a css-friendly string
   */
  public getColorString(key: number | string): string {
    return hslToString(this.getColor(key));
  }

  /**
   * Returns the number of colors in the color palette.
   *
   * @returns the number of colors
   */
  public getSize(): number {
    return this.colorMap.size;
  }

  /**
   * Generates a color that is significantly different
   * from previously generated colors, starting from a
   * random seed value. Adds the color to the list
   *
   * @returns HSL color value with fixed saturation and lightness
   */
  private readonly generateDistinctColorHSL = (): HSL => {
    this.nextHue += GOLDEN_RATIO_CONJUGATE * DEGREES_IN_A_CIRCLE;
    this.nextHue %= DEGREES_IN_A_CIRCLE;
    const hslColor = hsvToHSL(this.nextHue, DEFAULT_SATURATION, DEFAULT_VALUE);
    return hslColor;
  };

  /**
   * Generates a color palette where each color is as distinct.
   *
   * @param keys
   * @returns an array of HSL colors, each of which has a hue
   * that is the golden ratio away from the preceding color in the list
   * hue is 0 to 360 deg
   * saturation is 0 to 100 percent
   * value is 0 to 100 percent
   */
  private readonly generateNewListOfDistinctColorsWithKeysHSL = (
    keys: string[] | number[]
  ): void => {
    this.generateNewColorList(keys.length).forEach((color, index) => {
      this.colorMap = this.colorMap.set(keys[index], color);
    });
  };

  private readonly generateNewColorList = (size: number): HSL[] => {
    const colorList = new Array(size).fill(0); // Fill it with 0s so the array is not an empty array of length n
    return colorList.map(() => this.generateDistinctColorHSL());
  };

  /**
   * Generates a number of degrees on the color wheel to use as our
   * starting hue.
   *
   * @param seed a seed number or string from which to generate the hue
   * @returns a number between 0 and 360 representing a number of degrees
   * in a circle. If the seed is falsy, return 0;
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly getHueFromSeed = (seed: number | string): number => {
    if (!seed) {
      return 0;
    }
    const seedNum: number = typeof seed === 'string' ? uniqueNumberFromString(seed) : seed;
    return seedNum % DEGREES_IN_A_CIRCLE;
  };
}

/**
 * Generates a hex code from user configuration to add alpha value to edge detection color
 *
 * @param decimal
 * @returns hex code
 */
export const decimalToHex = (decimal: number): string => {
  return (decimal * 255).toString(16);
};

/**
 * Blend two hex colors together by a percentage.
 * If either colors are not hex or the amount is undefined return colorA
 *
 * @param colorA
 * @param colorB
 * @param percentage 0 to 1
 * @returns new color
 */
export function blendColors(
  colorA: string,
  colorB: string,
  percentage: number | undefined
): string {
  if (!colorA.startsWith('#') || !colorB.startsWith('#') || percentage === undefined) {
    return colorA;
  }
  const radix = 16;
  const [rA, gA, bA] = colorA.match(/\w\w/g).map(c => parseInt(c, 16));
  const [rB, gB, bB] = colorB.match(/\w\w/g).map(c => parseInt(c, 16));
  const r = Math.round(rA + (rB - rA) * percentage)
    .toString(radix)
    .padStart(2, '0');
  const g = Math.round(gA + (gB - gA) * percentage)
    .toString(radix)
    .padStart(2, '0');
  const b = Math.round(bA + (bB - bA) * percentage)
    .toString(radix)
    .padStart(2, '0');
  return `#${r}${g}${b}`;
}
