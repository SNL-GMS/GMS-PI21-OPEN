import type { Configuration, RuleSetRule } from 'webpack';

/**
 * Source map loader rule set.
 *
 * @param paths the paths
 */
const sourceMapLoader = (): RuleSetRule => ({
  test: /\.js$/,
  use: ['source-map-loader'],
  enforce: 'pre',
  exclude: [
    // these packages have problems with their sourcemaps
    /node_modules\/cesium/,
    /node_modules\/deprecated-decorator/
  ]
});

/**
 * The webpack load source maps config.
 *
 * @param isProduction true if production, false otherwise
 */
export const sourceMapConfig = (isProduction: boolean): Configuration => ({
  module: {
    rules: isProduction
      ? []
      : // development load source maps
        [sourceMapLoader()]
  },
  resolve: {
    extensions: ['.js.map']
  }
});
