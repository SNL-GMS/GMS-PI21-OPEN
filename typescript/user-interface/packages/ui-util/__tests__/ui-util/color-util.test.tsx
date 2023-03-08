/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { ColorTypes } from '@gms/common-model';
import isEqual from 'lodash/isEqual';

import * as ColorUtils from '../../src/ts/ui-util/color-util';

describe('Color utils', () => {
  const numColors = ['one', 'two', 'three'];
  const stationName = 'ASAR';
  const palette = new ColorUtils.DistinctColorPalette(numColors, stationName);

  it('to be defined', () => {
    expect(ColorUtils.hsvToHSL).toBeDefined();
    expect(ColorUtils.hslToString).toBeDefined();
    expect(ColorUtils.DistinctColorPalette).toBeDefined();
  });

  describe('color contrast', () => {
    const white: ColorTypes.RGB = { red: 255, green: 255, blue: 255 };
    const black: ColorTypes.RGB = { red: 0, green: 0, blue: 0 };
    const whiteHex: ColorTypes.Hex = '#FFFFFF';

    it('can tell that white is legible against black', () => {
      const legibleColor = ColorUtils.getLegibleColor(black, white, black);
      expect(isEqual(legibleColor, white)).toBeTruthy();
    });
    it('can tell that black is legible against white', () => {
      const legibleColor = ColorUtils.getLegibleColor(white, white, black);
      expect(isEqual(legibleColor, black)).toBeTruthy();
    });
    it('can handle hex colors', () => {
      const legibleColor = ColorUtils.getLegibleColor(whiteHex, white, black);
      expect(isEqual(legibleColor, black)).toBeTruthy();
    });
    it('can return hex colors', () => {
      expect(ColorUtils.getLegibleHexColor(whiteHex, white, black)).toEqual('#000000');
      expect(ColorUtils.getLegibleHexColor('#000000', white, black)).toEqual('#ffffff');
    });
    it('uses defaults of white and black', () => {
      expect(ColorUtils.getLegibleHexColor('#FFFFFF')).toEqual('#000000');
      expect(ColorUtils.getLegibleHexColor('#000000')).toEqual('#ffffff');
    });
    it('can convert hex short hand to rgb', () => {
      expect(ColorUtils.hexToRgb('#000')).toEqual({ red: 0, green: 0, blue: 0 });
    });
    it('gets the expected rgb colors', () => {
      expect(
        ColorUtils.getLegibleColor(
          { red: 0, green: 255, blue: 0 },
          { red: 24, green: 24, blue: 24 },
          { red: 240, green: 240, blue: 240 }
        )
      ).toStrictEqual({ red: 24, green: 24, blue: 24 });
    });
    it('uses can handle hex short hand', () => {
      expect(ColorUtils.getLegibleHexColor('#FFF')).toEqual('#000000');
      expect(ColorUtils.getLegibleHexColor('#000')).toEqual('#ffffff');
      expect(ColorUtils.getLegibleHexColor('#333', '#111', '#999')).toEqual('#999999');
    });
    it('gets the expected contrast', () => {
      expect(
        ColorUtils.contrast({ red: 44, green: 116, blue: 216 }, { red: 16, green: 22, blue: 26 })
      ).toBeCloseTo(3.991);
    });
    it('gets the expected contrast for blue', () => {
      expect(
        ColorUtils.contrast({ red: 255, green: 255, blue: 255 }, { red: 0, green: 0, blue: 255 })
      ).toBeCloseTo(8.592);
    });
    it('gets the expected contrast for yellow', () => {
      expect(
        ColorUtils.contrast({ red: 255, green: 255, blue: 255 }, { red: 255, green: 255, blue: 0 })
      ).toBeCloseTo(1.074);
    });
  });

  it('can generate a color', () => {
    const color = palette.getColor('one');

    // hue
    expect(color.hue).toBeDefined();
    expect(color.hue).toBeLessThanOrEqual(360);
    expect(color.hue).toBeGreaterThanOrEqual(0);

    // saturation
    expect(color.saturation).toBeDefined();
    expect(color.saturation).toBeLessThanOrEqual(100);
    expect(color.saturation).toBeGreaterThanOrEqual(0);

    // lightness
    expect(color.lightness).toBeDefined();
    expect(color.lightness).toBeLessThanOrEqual(100);
    expect(color.lightness).toBeGreaterThanOrEqual(0);
  });

  it('fires off accessors', () => {
    expect(palette.getColors()).toBeDefined();
    expect(palette.getColorStrings()).toBeDefined();
    expect(palette.getKeys()).toBeDefined();
  });

  it('throws an error for unknown palette indices', () => {
    expect(() => palette.getColor(3)).toThrow();
  });

  it('can generate a color string', () => {
    const color: ColorUtils.HSL = {
      hue: 211,
      saturation: 12,
      lightness: 11
    };
    const colorString = ColorUtils.hslToString(color);
    expect(colorString.replace(/\s+/g, '')).toEqual('hsl(211deg,12%,11%)');
    expect(typeof palette.getColorString('one') === 'string').toBeTruthy();
  });

  it('can generate a list of distinct colors', () => {
    numColors.forEach(firstColor => {
      const color = palette.getColor(firstColor);
      // hue
      expect(color.hue).toBeDefined();
      expect(color.hue).toBeLessThanOrEqual(360);
      expect(color.hue).toBeGreaterThanOrEqual(0);

      // saturation
      expect(color.saturation).toBeDefined();
      expect(color.saturation).toBeLessThanOrEqual(100);
      expect(color.saturation).toBeGreaterThanOrEqual(0);

      // lightness
      expect(color.lightness).toBeDefined();
      expect(color.lightness).toBeLessThanOrEqual(100);
      expect(color.lightness).toBeGreaterThanOrEqual(0);

      numColors
        .filter(c => c !== firstColor)
        .forEach(secondColor => {
          const color2 = palette.getColor(secondColor);
          expect(color.hue).not.toEqual(color2.hue);
        });
    });
  });

  it('gets the same color palette when provided the same number as a seed', () => {
    const seedNum = 123;
    const a = new ColorUtils.DistinctColorPalette(numColors, seedNum);
    const b = new ColorUtils.DistinctColorPalette(numColors, seedNum);

    expect(a.getSize()).toEqual(b.getSize());
    numColors.forEach(color => expect(a.getColor(color)).toEqual(b.getColor(color)));

    expect(a.getSize()).toEqual(b.getSize());
    numColors.forEach(color => expect(a.getColor(color)).toEqual(b.getColor(color)));
  });

  it('produces a different palette for a different seed', () => {
    const seedNumA = 123;
    const seedNumB = 321;
    const a = new ColorUtils.DistinctColorPalette(numColors, seedNumA);
    const b = new ColorUtils.DistinctColorPalette(numColors, seedNumB);
    numColors.forEach(color => expect(a.getColor(color)).not.toEqual(b.getColor(color)));
  });

  it('can take a string for a seed', () => {
    const seedStr = 'this string gets converted into a number internally';
    const a = new ColorUtils.DistinctColorPalette(numColors, seedStr);
    const b = new ColorUtils.DistinctColorPalette(numColors, seedStr);

    expect(a.getSize()).toEqual(b.getSize());
    numColors.forEach(color => expect(a.getColor(color)).toEqual(b.getColor(color)));

    const differentSeed = 'this should produce a different palette';
    const differentPalette = new ColorUtils.DistinctColorPalette(numColors, differentSeed);
    numColors.forEach(color =>
      expect(a.getColor(color)).not.toEqual(differentPalette.getColor(color))
    );
  });

  it('can add a color to the palette', () => {
    const testPalette = new ColorUtils.DistinctColorPalette([0, 1]);
    const newColor = testPalette.addColor();
    expect(newColor.hue).toBeCloseTo(307.4767078498866, 10);
    expect(newColor.saturation).toBeCloseTo(36.8421052631579, 10);
    expect(newColor.lightness).toBeCloseTo(52.49999999999999, 10);
  });

  it('can convert HSV to HSL with lightness 0', () => {
    // eslint-disable-next-line no-restricted-syntax
    for (const hue of [0, 40, 154, 360]) {
      const hsl = ColorUtils.hsvToHSL(hue, 0, 0);
      expect(hsl.hue).toEqual(hue);
      expect(hsl.saturation).toEqual(0);
      expect(hsl.lightness).toEqual(0);
    }
  });

  it('can convert HSV to HSL with lightness 1', () => {
    // eslint-disable-next-line no-restricted-syntax
    for (const hue of [0, 40, 154, 360]) {
      const hsl = ColorUtils.hsvToHSL(hue, 0, 1);
      expect(hsl.hue).toEqual(hue);
      expect(hsl.saturation).toEqual(0);
      expect(hsl.lightness).toEqual(100);
    }
  });

  it('can convert an arbitrary HSV to HSL', () => {
    const hue = 35;
    const hsl = ColorUtils.hsvToHSL(hue, 0.4, 0.25);
    expect(hsl.hue).toEqual(hue);
    expect(hsl.saturation).toBeCloseTo(25, 10);
    expect(hsl.lightness).toEqual(20);
  });

  it('can convert HSL to RGB', () => {
    let rgb: string;
    rgb = ColorUtils.hslToHex({ hue: 30, lightness: 75, saturation: 50 });
    expect(rgb).toEqual('#dfbf9f');
    rgb = ColorUtils.hslToHex({ hue: 90, lightness: 75, saturation: 50 });
    expect(rgb).toEqual('#bfdf9f');
    rgb = ColorUtils.hslToHex({ hue: 150, lightness: 50, saturation: 50 });
    expect(rgb).toEqual('#40bf80');
    rgb = ColorUtils.hslToHex({ hue: 210, lightness: 50, saturation: 75 });
    expect(rgb).toEqual('#2080df');
    rgb = ColorUtils.hslToHex({ hue: 270, lightness: 25, saturation: 75 });
    expect(rgb).toEqual('#401070');
    rgb = ColorUtils.hslToHex({ hue: 330, lightness: 50, saturation: 50 });
    expect(rgb).toEqual('#bf4080');
    expect(() => ColorUtils.hslToHex({ hue: 390, lightness: 50, saturation: 50 })).toThrow(
      RangeError
    );
  });

  it('can zero-pad RGB hex colors', () => {
    const rgb = ColorUtils.hslToHex({ hue: 330, lightness: 0, saturation: 50 });
    expect(rgb).toEqual('#000000');
  });

  it('can blend hex defined colors', () => {
    const percent = 0.5;
    expect(ColorUtils.blendColors('red', '#182026', percent)).toEqual('red');
    expect(ColorUtils.blendColors('#182026', 'red', percent)).toEqual('#182026');
    expect(ColorUtils.blendColors('#182026', '#f5f8fa', undefined)).toEqual('#182026');
    expect(ColorUtils.blendColors('#182026', '#f5f8fa', percent)).toMatchInlineSnapshot(
      `"#878c90"`
    );
  });
});
