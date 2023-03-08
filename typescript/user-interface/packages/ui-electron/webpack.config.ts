import type { Configuration, WebpackConfig } from '@gms/webpack-config';
import {
  DefinePlugin,
  getWebpackPaths,
  nodeConfig,
  WebpackHtmlWebpackPlugin,
  webpackMerge
} from '@gms/webpack-config';
import { resolve } from 'path';

const SERVER_URL = process.env.SERVER_URL || 'http://localhost:8080';

const config = (env: { [key: string]: string | boolean }): Configuration[] => {
  const webpackPaths = getWebpackPaths({
    baseDir: resolve(__dirname, '.'),
    useWorkspace: true,
    tsconfigFileName: 'tsconfig-build.json'
  });
  const webpackConfig: WebpackConfig = {
    name: 'ui-electron',
    title: 'Interactive Analysis',
    paths: webpackPaths,
    isProduction: env.production === true,
    entry: resolve(webpackPaths.src, 'ts/index.ts'),
    alias: {},
    shouldIncludeDevServer: false
  };

  return [
    webpackMerge(nodeConfig(webpackConfig), {
      target: 'electron-main',
      externals: ['utf-8-validate', 'bufferutil'],
      plugins: [
        new WebpackHtmlWebpackPlugin({
          template: resolve(webpackConfig.paths.baseDir, 'index.html'),
          title: `${webpackConfig.title}`,
          favicon: resolve(webpackConfig.paths.baseDir, '../../resources/gms-logo-favicon.ico')
        }),

        new DefinePlugin({
          DEFAULT_SERVER_URL: JSON.stringify(SERVER_URL)
        })
      ]
    })
  ];
};

// eslint-disable-next-line import/no-default-export
export default config;
