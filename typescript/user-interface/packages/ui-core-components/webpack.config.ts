import type { Configuration, WebpackConfig } from '@gms/webpack-config';
import { appConfig, getWebpackPaths, libCjsConfig } from '@gms/webpack-config';
import { resolve } from 'path';

const config = (env: { [key: string]: string | boolean }): Configuration[] => {
  const shouldIncludeDevServer: boolean = env.devserver === true;
  const webpackPaths = getWebpackPaths({
    baseDir: resolve(__dirname, '.'),
    useWorkspace: true,
    tsconfigFileName: 'tsconfig-build.json'
  });
  const webpackConfig: WebpackConfig = {
    name: 'ui-core-components',
    title: 'UI Core Components',
    paths: webpackPaths,
    isProduction: env.production === true,
    shouldIncludeDevServer,
    entry: env.devserver
      ? resolve(webpackPaths.src, 'ts/examples/index.tsx')
      : resolve(webpackPaths.src, 'ts/ui-core-components.ts'),
    alias: {}
  };
  return shouldIncludeDevServer ? [appConfig(webpackConfig)] : [libCjsConfig(webpackConfig)];
};

// eslint-disable-next-line import/no-default-export
export default config;
