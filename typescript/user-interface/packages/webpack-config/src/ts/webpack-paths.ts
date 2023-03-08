import { resolve } from 'path';

import type { WebpackPaths } from './types';

export interface WebpackPathsParams {
  // the path to the directory of the package that is being built (see the relevant webpack.config.ts file)
  baseDir: string;
  // the eslint configuration file name
  eslintConfigFileName?: string;
  // (optional) the typescript configuration file name
  tsconfigFileName?: string;
  // (optional) whether we are using yarn workspaces
  useWorkspace?: boolean;
  // (optional) the subdirectory that is created under the dist folder
  subDir?: string;
  // (optional) provide a path for the service worker entry point
  swPath?: string;
  // (optional) provide a path for the app manifest
  appManifest?: string;
}

/**
 * Returns the Webpack build paths
 *
 * @param params the settings to setup and configure the webpack paths
 */
export const getWebpackPaths = (params: WebpackPathsParams): WebpackPaths => {
  const baseDirName = params.baseDir.match(/([^/]*)\/*$/)[1];
  const nodeModules = resolve(params.baseDir, 'node_modules/');
  const eslint = resolve(params.baseDir, params.eslintConfigFileName || '.eslintrc-config.yaml');
  const tsconfig = resolve(params.baseDir, params.tsconfigFileName || 'tsconfig.json');
  const packageJson = resolve(params.baseDir, 'package.json');
  const cesium = params.useWorkspace
    ? // path to the workspace node_modules (support for hoisted packages)
      resolve(params.baseDir, '../../node_modules/cesium/Source/')
    : resolve(nodeModules, 'cesium/Source/');
  const src = resolve(params.baseDir, 'src');
  const resources = resolve(src, 'resources');
  const dist = resolve(params.baseDir, params.subDir ? `dist/${params.subDir}` : 'dist');
  const cesiumDir = `cesium_${Date.now()}`; // add timestamp as unique string per bundle
  const bundleAnalyze = resolve(params.baseDir, 'bundle-analyzer');

  return {
    baseDirName,
    baseDir: params.baseDir,
    nodeModules,
    eslint,
    tsconfig,
    packageJson,
    cesium,
    cesiumDir,
    sw: params.swPath,
    src,
    resources,
    dist,
    bundleAnalyze,
    appManifest: params.appManifest,

    resolveModule: (module: string) => resolve(nodeModules, module),
    resolveResource: (resource: string) => resolve(resources, resource)
  };
};
