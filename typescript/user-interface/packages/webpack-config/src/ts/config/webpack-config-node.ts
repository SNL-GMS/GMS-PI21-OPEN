import type { Configuration } from 'webpack';
import merge from 'webpack-merge';

import type { WebpackConfig } from '../types';
import { bundleAnalyzeConfig } from './webpack-config-analyze';
import { commonNodeConfig, getEntry } from './webpack-config-common';
import { developmentConfig } from './webpack-config-development';
import { productionConfig } from './webpack-config-production';
import { tsConfig } from './webpack-config-typescript';

/**
 * The webpack common configuration for node applications
 *
 * @param webpackConfig the webpack configuration
 */
const nodeCommonConfig = (webpackConfig: WebpackConfig): Configuration => {
  const entry = getEntry(webpackConfig);

  const common: Configuration = merge(commonNodeConfig(webpackConfig));

  return merge(common, {
    entry,
    target: 'node',
    output: {
      filename: '[name].js',
      chunkFilename: '[name].[contenthash].js',
      path: webpackConfig.paths.dist,
      sourcePrefix: '',
      library: '[name]',
      libraryTarget: 'umd',
      libraryExport: 'default'
    },
    externalsPresets: { node: true }
  });
};

/**
 * The webpack configuration for node applications
 *
 * @param webpackConfig the webpack configuration
 */
export const nodeConfig = (webpackConfig: WebpackConfig): Configuration =>
  merge(
    tsConfig(webpackConfig),
    nodeCommonConfig(webpackConfig),
    webpackConfig.isProduction ? productionConfig() : developmentConfig(),
    bundleAnalyzeConfig(webpackConfig)
  );
