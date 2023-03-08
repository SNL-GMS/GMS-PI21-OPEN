import type { Configuration } from 'webpack';
import merge from 'webpack-merge';
import nodeExternals from 'webpack-node-externals';

import type { WebpackConfig } from '../types';
import { assetConfig } from './webpack-asset';
import { bundleAnalyzeConfig } from './webpack-config-analyze';
import { commonWebConfig, getEntry } from './webpack-config-common';
import { developmentConfig } from './webpack-config-development';
import { productionConfig } from './webpack-config-production';
import { styleConfig } from './webpack-config-styles';
import { tsConfig } from './webpack-config-typescript';

/**
 * The webpack common configuration for libraries
 *
 * @param webpackConfig the webpack configuration
 */
const libCommonConfig = (webpackConfig: WebpackConfig): Configuration => {
  const common: Configuration = merge(commonWebConfig(webpackConfig), {
    externalsPresets: { node: true },
    externals: [
      nodeExternals({}),
      nodeExternals({
        modulesFromFile: true
      }),
      // support for `yarn workspaces`
      nodeExternals({
        modulesDir: '../../node_modules'
      })
      // ! https://github.com/liady/webpack-node-externals/issues/105
    ] as Configuration['externals']
  });
  return merge(
    common,
    tsConfig(webpackConfig),
    styleConfig(webpackConfig.isProduction),
    assetConfig(),
    webpackConfig.isProduction ? productionConfig() : developmentConfig(),
    bundleAnalyzeConfig(webpackConfig)
  );
};

/**
 * The webpack configuration for libraries (umd)
 *
 * @param webpackConfig the webpack configuration
 */
export const libUmdConfig = (webpackConfig: WebpackConfig): Configuration => {
  const libUmd: Configuration = {
    entry: getEntry(webpackConfig),
    output: {
      filename: '[name].js',
      chunkFilename: '[name].[contenthash].js',
      path: webpackConfig.paths.dist,
      sourcePrefix: '',
      libraryTarget: 'umd',
      library: webpackConfig.name
    }
  };
  return merge(libCommonConfig(webpackConfig), libUmd);
};

/**
 * The webpack configuration for libraries (commonJS)
 *
 * @param webpackConfig the webpack configuration
 */
export const libCjsConfig = (webpackConfig: WebpackConfig): Configuration => {
  const libCjs: Configuration = {
    entry: getEntry(webpackConfig),
    output: {
      filename: '[name].js',
      chunkFilename: '[name].[contenthash].js',
      path: webpackConfig.paths.dist,
      sourcePrefix: '',
      libraryTarget: 'commonjs2',
      library: webpackConfig.name
    }
  };
  return merge(libCommonConfig(webpackConfig), libCjs);
};
