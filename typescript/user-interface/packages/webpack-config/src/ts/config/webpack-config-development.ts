import type { Configuration } from 'webpack';
import { DefinePlugin } from 'webpack';

/**
 * Returns the webpack development configuration.
 *
 * @param paths the paths
 */
export const developmentConfig = (): Configuration => ({
  mode: 'development',
  devtool: 'inline-cheap-module-source-map',
  plugins: [
    new DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify('development')
    })
  ]
});
