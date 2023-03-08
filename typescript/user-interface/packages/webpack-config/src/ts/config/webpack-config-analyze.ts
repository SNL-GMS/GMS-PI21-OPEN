import { resolve } from 'path';
import type { Configuration } from 'webpack';
import { BundleAnalyzerPlugin } from 'webpack-bundle-analyzer';

import type { WebpackConfig } from '../types';

/**
 * Analyzes the webpack analyze bundle configuration.
 *
 * @param webpackConfig the webpack configuration
 */
export const bundleAnalyzeConfig = (webpackConfig: WebpackConfig): Configuration =>
  webpackConfig.isProduction && process.env.BUNDLE_ANALYZE === 'true'
    ? {
        plugins: [
          new BundleAnalyzerPlugin({
            analyzerMode: 'static',
            openAnalyzer: false,
            generateStatsFile: true,
            reportFilename: resolve(
              webpackConfig.paths.bundleAnalyze,
              `${webpackConfig.name}.html`
            ),
            statsFilename: resolve(webpackConfig.paths.bundleAnalyze, `${webpackConfig.name}.json`)
            // ! See https://github.com/webpack/webpack/issues/11630
          }) as any
        ]
      }
    : {};
