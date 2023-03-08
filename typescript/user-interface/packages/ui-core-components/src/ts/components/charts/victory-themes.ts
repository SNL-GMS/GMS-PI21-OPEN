import type { VictoryThemeDefinition } from 'victory-core';

// TODO use import to bring in the css colors definitions

const CORE_PROMINENT_COLOR = 'var(--core-prominent)';
const CORE_SOFT_COLOR = 'var(--core-soft)';
const CORE_MAIN_COLOR = 'var(--core-main)';

// Typography
const FONT = 'var(--core-sans)';
const letterSpacing = 'normal';
const fontSize = 14;

// Labels
export const baseLabelStyles = {
  fontFamily: FONT,
  fontSize,
  letterSpacing,
  padding: 14,
  fill: CORE_PROMINENT_COLOR,
  stroke: 'transparent'
};

export const tickLabelStyles = {
  fontFamily: FONT,
  fontSize,
  letterSpacing,
  padding: 10,
  fill: CORE_MAIN_COLOR,
  stroke: 'transparent'
};

const tickStyles = {
  strokeWidth: 1,
  size: 4,
  stroke: CORE_MAIN_COLOR
};

export const GMSTheme: VictoryThemeDefinition = {
  axis: {
    style: {
      axis: {
        strokeWidth: 1,
        stroke: CORE_PROMINENT_COLOR,
        offset: fontSize
      },
      grid: {
        fill: 'none',
        stroke: 'none',
        pointerEvents: 'painted'
      }
    }
  },
  bar: {
    style: {
      data: {
        padding: 8,
        strokeWidth: 0
      },
      labels: baseLabelStyles
    }
  },
  independentAxis: {
    style: {
      tickLabels: tickLabelStyles,
      axis: {
        strokeWidth: 1,
        stroke: CORE_SOFT_COLOR,
        offsetInlineStart: 16
      },
      grid: {
        stroke: 'none'
      },
      ticks: tickStyles
    }
  },
  dependentAxis: {
    style: {
      tickLabels: tickLabelStyles,
      axis: {
        strokeWidth: 1,
        stroke: CORE_SOFT_COLOR,
        offsetInlineStart: 16
      },
      grid: {
        stroke: 'none'
      },
      ticks: tickStyles
    }
  }
};
