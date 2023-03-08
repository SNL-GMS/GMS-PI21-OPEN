import CopyWebpackPlugin from 'copy-webpack-plugin';
import { join, resolve } from 'path';
import type { Configuration, RuleSetRule, WebpackPluginInstance } from 'webpack';
import { DefinePlugin } from 'webpack';

import type { WebpackPaths } from '../types';

// Explanations:
// https://cesiumjs.org/tutorials/cesium-and-webpack/

/**
 * Cesium loader rule set.
 */
const cesiumLoader: RuleSetRule = {
  test: /cesium\.js$/,
  // eslint-disable-next-line @blueprintjs/classes-constants
  loader: 'script-loader'
};

/**
 * KML loader rule set.
 */
const kmlLoader: RuleSetRule = {
  test: /\.(kml)$/i,
  type: 'asset/resource',
  generator: {
    filename: 'resources/kml/[name]-[contenthash].[ext]'
  }
};

/**
 * Cesium webpack plugins.
 *
 *
 * @param paths the paths
 * @param isProduction true if production; false if development
 */
const cesiumPlugins = (paths: WebpackPaths, isProduction: boolean): WebpackPluginInstance[] => {
  const scriptBuild = isProduction ? 'Cesium' : 'CesiumUnminified';
  return [
    new CopyWebpackPlugin({
      patterns: [
        {
          from: join(paths.cesium, `../Build/${scriptBuild}/`),
          to: resolve(paths.dist, paths.dist, paths.cesiumDir)
        }
      ]
    }),
    new DefinePlugin({
      CESIUM_BASE_URL: JSON.stringify(resolve(paths.dist, paths.cesiumDir)),
      'process.env.CESIUM_OFFLINE': JSON.stringify(process.env.CESIUM_OFFLINE)
    })
  ];
};

/**
 * The webpack cesium configuration.
 *
 * @param paths the paths
 * @param isProduction true if production; false otherwise
 */
export const cesiumConfig = (paths: WebpackPaths, isProduction: boolean): Configuration => ({
  externals: {
    cesium: 'Cesium'
  },
  amd: {
    // enable webpack-friendly use of require in Cesium
    toUrlUndefined: true
  },
  module: {
    rules: [cesiumLoader, kmlLoader]
  },
  resolve: {
    fallback: {
      fs: false
    },
    alias: {
      cesium: paths.cesium
    },
    extensions: ['.js']
  },
  plugins: cesiumPlugins(paths, isProduction)
});
