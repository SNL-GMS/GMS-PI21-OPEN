import type { PluginOptions } from 'copy-webpack-plugin';
import CopyWebpackPlugin from 'copy-webpack-plugin';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import NodePolyfillPlugin from 'node-polyfill-webpack-plugin';
import type { Configuration as WebpackConfiguration } from 'webpack';
import type { Configuration as WebpackDevServerConfiguration } from 'webpack-dev-server';
import merge from 'webpack-merge';
import nodeExternals from 'webpack-node-externals';

/* export Webpack specific items for packages to limit dependencies on webpack */
export { DefinePlugin } from 'webpack';
export type Configuration = WebpackConfiguration & { devServer?: WebpackDevServerConfiguration };

export const webpackMerge = merge;
export const webpackNodeExternals = nodeExternals;
export const WebpackHtmlWebpackPlugin = HtmlWebpackPlugin;
export const WebpackNodePolyfillPlugin = NodePolyfillPlugin;
export const webpackCopy = (patterns: PluginOptions): CopyWebpackPlugin =>
  new CopyWebpackPlugin(patterns);

export * from './config';
export * from './types';
export * from './webpack-paths';
