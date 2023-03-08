import type { Configuration } from 'webpack';

/**
 * The webpack url and file loader configuration.
 */
export const assetConfig = (): Configuration => ({
  module: {
    rules: [
      {
        test: /\.(png|gif|jpg|jpeg|svg)$/i,
        type:
          'asset' /*  automatically chooses between exporting a data URI and emitting a separate file */,
        generator: {
          filename: 'resources/images/[name]-[contenthash][ext]'
        }
      },
      {
        test: /\.(woff|woff2|eot|ttf)$/i,
        type: 'asset/resource',
        generator: {
          filename: 'resources/fonts/[name]-[contenthash][ext]'
        }
      },
      {
        test: /\.(xml)$/i,
        type: 'asset/resource',
        generator: {
          filename: 'resources/xml/[name]-[contenthash][ext]'
        }
      },
      {
        test: /\.(wasm)$/i,
        type: 'asset/resource',
        generator: {
          filename: '[name][ext]'
        }
      }
    ]
  }
});
