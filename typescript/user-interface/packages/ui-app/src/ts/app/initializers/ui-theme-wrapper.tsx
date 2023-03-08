import { Classes } from '@blueprintjs/core';
import type { ConfigurationTypes } from '@gms/common-model';
import { useLegibleColorsForEventAssociations, useUiTheme } from '@gms/ui-state';
import { getLegibleHexColor, isHexColor } from '@gms/ui-util';
import * as React from 'react';

/**
 * Note: This will sanitize the variable using encodeURI.
 * See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURI
 *
 * @param colorThemeProperty the camelCase string to convert into the css variable syntax
 * @returns a string in the form `--first-second-third` (that was originally in the format firstSecondThird).
 */
export const convertCamelCaseToCssVar = (colorThemeProperty: string): string => {
  const splitCssVariable = char => `-${char.toLowerCase()}`;
  return `--${window.encodeURI(colorThemeProperty).replace(/[A-Z]/g, splitCssVariable)}`;
};

/**
 * @param colorTheme is the color theme, consisting of string color names in camelCase, and values,
 * which are valid hex codes or alphanumeric color strings.
 *
 * Note, variable names will be sanitized using encodeURI.
 * See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURI
 *
 * @returns an object of the form: {
 *   '--waveform-raw': colorTheme.waveformRaw,
 *   '--waveform-filter-label': colorTheme.waveformFilterLabel
 * }
 */
export const getCssVarsFromTheme = (
  colorTheme: ConfigurationTypes.ColorTheme
): Record<string, string> =>
  Object.keys(colorTheme).reduce((cssVars: Record<string, string>, colorThemeKey: string) => {
    const updatedTheme = { ...cssVars };
    updatedTheme[convertCamelCaseToCssVar(colorThemeKey)] = window.encodeURI(
      colorTheme[colorThemeKey]
    );
    return updatedTheme;
  }, {});

/**
 * Injects the blueprint `.bp4-dark` dark mode class into the html element. Uses imperative query selector to get
 * a handle on the html, since we don't have a ref to it.
 * For light mode, we add a `.gms-light-mode` class instead.
 *
 * @param className the class name to add
 */
const injectBlueprintThemeClass = (className: 'gms-light-mode' | typeof Classes.DARK): void => {
  document.querySelector('html').className = className;
  document.querySelector('body').className = className;
};

export const injectTheme = (
  uiTheme: ConfigurationTypes.UITheme,
  targetElement: HTMLElement = document.querySelector('html')
): void => {
  const cssVars = getCssVarsFromTheme(uiTheme.colors);
  localStorage.setItem('uiTheme', JSON.stringify(uiTheme));
  Object.keys(cssVars).forEach(varName => {
    targetElement.style.setProperty(varName, cssVars[varName]);
  });
  if (uiTheme.isDarkMode) {
    injectBlueprintThemeClass(Classes.DARK);
  } else {
    injectBlueprintThemeClass('gms-light-mode');
  }
};

export interface UIThemeWrapperProps {
  children: React.ReactNode;
  theme?: ConfigurationTypes.UITheme;
}

/**
 * A component that will read in the current UI Theme from Redux, and inject it into the html element
 * `style` as css variables.
 */
export function UIThemeWrapper({ children, theme }: UIThemeWrapperProps) {
  const [uiTheme] = useUiTheme();
  const cssCustomAssociationProperties = useLegibleColorsForEventAssociations();
  const tableSelectionTextColor = isHexColor(uiTheme.colors.gmsTableSelection)
    ? getLegibleHexColor(uiTheme.colors.gmsTableSelection)
    : uiTheme.colors.gmsMain;
  React.useEffect(() => {
    injectTheme(theme ?? uiTheme);
  }, [theme, uiTheme]);
  return (
    <div
      className={`gms-theme-provider ${uiTheme.isDarkMode ? Classes.DARK : ''}`}
      style={
        {
          ...cssCustomAssociationProperties,
          '--table-selection-text-color': tableSelectionTextColor
        } as React.CSSProperties
      }
    >
      {children}
    </div>
  );
}
