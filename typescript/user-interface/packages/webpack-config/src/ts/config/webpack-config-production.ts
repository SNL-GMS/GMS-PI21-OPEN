import CssMinimizerPlugin from 'css-minimizer-webpack-plugin';
import type { Configuration } from 'webpack';
import { DefinePlugin, LoaderOptionsPlugin } from 'webpack';

/**
 * Returns the webpack production configuration.
 */
export const productionConfig = (): Configuration => {
  return {
    performance: {
      hints: 'warning',
      maxAssetSize: 4000000,
      maxEntrypointSize: 10000000
    },
    mode: 'production',
    optimization: {
      nodeEnv: 'production',
      runtimeChunk: false,
      usedExports: true,
      mergeDuplicateChunks: true,
      minimize: true,
      removeEmptyChunks: true,
      removeAvailableModules: true,
      sideEffects: true,
      mangleExports: 'size',
      minimizer: [new CssMinimizerPlugin({ parallel: true }), '...'],
      splitChunks: {
        chunks(chunk) {
          return (
            // ! Chunking breaks in service worker, since chunking uses apis that do not work on a service worker.
            // ! Note: all dependencies required in the service worker must be excluded here. They may not appear in cacheGroups, either.
            // ! See https://stackoverflow.com/questions/72114876/webpack-transpiled-typescript-service-worker-code-doesnt-seem-to-work
            chunk.name !== 'env-inject' &&
            chunk.name !== 'sw' &&
            chunk.name !== 'common-model' &&
            chunk.name !== 'common-util'
          );
        },
        maxInitialRequests: Infinity,
        minSize: 0,
        minChunks: 2,
        cacheGroups: {
          styles: {
            name: 'styles',
            test: /\.css$/,
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          cesium: {
            test: /[\\/]node_modules[\\/]cesium[\\/]/,
            name: 'cesium',
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          'golden-layout': {
            test: /golden-layout/,
            name: 'golden-layout',
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          'ui-core-components': {
            test: /ui-core-components/,
            name: 'ui-core-components',
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          'ui-state': {
            test: /ui-state/,
            name: 'ui-state',
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          'ui-util': {
            test: /ui-util/,
            name: 'ui-util',
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          'ui-wasm': {
            test: /ui-wasm/,
            name: 'ui-wasm',
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          weavess: {
            test: /weavess/,
            name: 'weavess',
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          'weavess-core': {
            test: /weavess-core/,
            name: 'weavess-core',
            chunks: 'all',
            reuseExistingChunk: true,
            enforce: true
          },
          vendor: {
            test: /[\\/]node_modules[\\/](?!cesium)/,
            name(module) {
              // get the name. E.g. node_modules/packageName/not/this/part.js or node_modules/packageName
              const matched = (module.context as string).match(
                /[\\/]node_modules[\\/](.*?)([\\/]|$)/
              );
              if (matched) {
                const packageName = matched?.length > 1 ? matched[1] : matched[0];
                // npm package names are URL-safe, but some servers don't like @ symbols
                return `vender.${packageName.replace('@', '')}`;
              }
              return module.context;
            },
            reuseExistingChunk: true,
            enforce: true
          }
        }
      }
    },
    plugins: [
      new DefinePlugin({
        'process.env.NODE_ENV': JSON.stringify('production')
      }),

      // Some loaders accept configuration through webpack internals
      new LoaderOptionsPlugin({
        debug: false,
        minimize: true
      })
    ]
  };
};
