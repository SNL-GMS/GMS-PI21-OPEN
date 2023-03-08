import MiniCssExtractPlugin from 'mini-css-extract-plugin';
import type { Configuration, RuleSetRule, WebpackPluginInstance } from 'webpack';

/**
 * CSS webpack loader rule set.
 *
 * @param isProduction true if production; false if development
 */
const cssLoader = (isProduction: boolean): RuleSetRule => ({
  test: /\.css$/,
  use: [
    isProduction
      ? {
          loader: MiniCssExtractPlugin.loader
        }
      : {
          loader: 'style-loader'
        },
    {
      loader: 'css-loader',
      options: {
        sourceMap: !isProduction
      }
    }
  ]
});

/**
 * SCSS/SASS webpack loader rule set.
 *
 * @param isProduction true if production; false if development
 */
const scssLoader = (isProduction: boolean): RuleSetRule => ({
  test: /\.s[ac]ss$/,
  use: [
    isProduction
      ? {
          loader: MiniCssExtractPlugin.loader
        }
      : {
          loader: 'style-loader'
        },
    {
      loader: 'css-loader',
      options: {
        sourceMap: !isProduction,
        modules: {
          mode: 'icss'
        }
      }
    },
    {
      loader: 'resolve-url-loader',
      options: {
        sourceMap: !isProduction
      }
    },
    {
      loader: 'sass-loader',
      options: {
        sourceMap: true, // <-- !!IMPORTANT!! source-maps required for loaders preceding resolve-url-loader
        sassOptions: {
          quietDeps: false
        }
      }
    }
  ]
});

/**
 * Style plugins.
 *
 * @param isProduction true if production; false if development
 */
const stylePlugins = (): WebpackPluginInstance[] => [
  new MiniCssExtractPlugin({
    filename: '[name].[contenthash].css',
    chunkFilename: '[name].[contenthash].css'
  })
];

/**
 * The webpack styles configuration.
 *
 * @param isProduction true if production; false if development
 */
export const styleConfig = (isProduction: boolean): Configuration => ({
  module: {
    rules: [cssLoader(isProduction), scssLoader(isProduction)]
  },
  plugins: stylePlugins(),
  resolve: {
    extensions: ['.css', '.scss', '.sass']
  }
});
