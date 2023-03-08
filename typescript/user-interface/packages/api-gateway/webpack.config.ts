import type { Configuration, WebpackConfig } from '@gms/webpack-config';
import { DefinePlugin, getWebpackPaths, nodeConfig, webpackMerge } from '@gms/webpack-config';
import { resolve } from 'path';

const config = (env: { [key: string]: string | boolean }): Configuration[] => {
  const webpackPaths = getWebpackPaths({
    baseDir: resolve(__dirname, '.'),
    useWorkspace: true,
    tsconfigFileName: 'tsconfig-build.json'
  });

  const commonConfig: Configuration = {
    externals: ['bufferutil', 'utf-8-validate'],
    plugins: [
      // https://github.com/lorenwest/node-config/wiki/Webpack-Usage
      // eslint-disable-next-line @typescript-eslint/no-var-requires, global-require, @typescript-eslint/no-require-imports
      new DefinePlugin({ CONFIG: JSON.stringify(require('config')) }),
      new DefinePlugin({
        // double stringify because node-config expects this to be a string
        'process.env.NODE_CONFIG': JSON.stringify(JSON.stringify(config))
      })
    ]
  };

  const webpackConfig: WebpackConfig = {
    name: 'api-gateway-server',
    title: 'Api Gateway',
    entry: {
      'api-gateway-server': resolve(webpackPaths.src, 'ts/server/api-gateway-server.ts')
    },
    paths: webpackPaths,
    isProduction: env.production === true,
    shouldIncludeDevServer: false,
    alias: {}
  };

  return [webpackMerge(nodeConfig(webpackConfig), commonConfig)];
};

// eslint-disable-next-line import/no-default-export
export default config;
