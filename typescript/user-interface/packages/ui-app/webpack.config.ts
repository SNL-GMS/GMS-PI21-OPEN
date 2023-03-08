import type { Configuration, WebpackConfig, WebpackPaths } from '@gms/webpack-config';
import {
  appConfig,
  babelLoader,
  cesiumConfig,
  DefinePlugin,
  getWebpackPaths,
  webpackCopy,
  webpackMerge
} from '@gms/webpack-config';
import fs from 'fs';
import { join, resolve } from 'path';
import type { EntryObject } from 'webpack';

type GetPaths = (isProduction: boolean) => WebpackPaths;

type GetConfiguration = (isProduction, shouldIncludeDevServer, paths) => WebpackConfig;

/**
 * Queries the published 'sound' directory and returns a list of the
 * available sound files.
 *
 * Note: this assumes that there are only files (no additional directories)
 */
const getAllFilesFromFolder = (dir: string, pattern?: RegExp): string[] => {
  const results: string[] = [];
  fs.readdirSync(dir)
    .filter((file: string) => !pattern || file.match(pattern))
    .forEach((file: string) => {
      results.push(file);
    });
  return results;
};

const defaultMockDataServerUri = 'http://localhost:3001';
const eventManagerProxyUri =
  process.env.EVENT_MANAGER_PROXY_URI || process.env.DEPLOYMENT_URL || 'http://localhost:3001';
const gatewayHttpProxyUri =
  process.env.GATEWAY_HTTP_PROXY_URI || process.env.DEPLOYMENT_URL || `http://localhost:3000`;
const frameworksOsdProxyUri =
  process.env.FRAMEWORK_OSD_PROXY_URI || process.env.DEPLOYMENT_URL || defaultMockDataServerUri;
const signalEnhancementConfigurationProxyUri =
  process.env.SIGNAL_ENHANCEMENT_CONFIGURATION_PROXY_URI ||
  process.env.DEPLOYMENT_URL ||
  defaultMockDataServerUri;
const processingConfigurationProxyUri =
  process.env.PROCESSING_CONFIGURATION_PROXY_URI ||
  process.env.DEPLOYMENT_URL ||
  defaultMockDataServerUri;
const signalDetectionProxyUri =
  process.env.SIGNAL_DETECTION_PROXY_URI || process.env.DEPLOYMENT_URL || 'http://localhost:3004';
const ssamRetrieveDecimatedHistoricalStationSoh =
  process.env.SSAM_CONTROL_PROXY_URI ||
  process.env.SSAM_RETRIEVE_DECIMATED_HISTORICAL_STATION_SOH_URL ||
  process.env.DEPLOYMENT_URL ||
  `http://localhost:3000`;
const ssamRetrieveStationSohMonitoringUiClientParameters =
  process.env.SSAM_CONTROL_PROXY_URI ||
  process.env.SSAM_RETRIEVE_STATION_SOH_MONITORING_UI_CLIENT_PARAMS_URL ||
  process.env.DEPLOYMENT_URL ||
  defaultMockDataServerUri;
const stationDefinitionServiceUri =
  process.env.STATION_DEFINITION_SERVICE_URL ||
  process.env.DEPLOYMENT_URL ||
  defaultMockDataServerUri;
const subscriptionsProxyUri =
  process.env.SUBSCRIPTIONS_PROXY_URI || process.env.DEPLOYMENT_URL || `ws://localhost:4001`;
const systemMessageDefinitionProxyUri =
  process.env.SYSTEM_MESSAGE_DEFINITION_PROXY_URI ||
  process.env.DEPLOYMENT_URL ||
  defaultMockDataServerUri;
const userManagerProxyUri =
  process.env.USER_MANAGER_PROXY_URI || process.env.DEPLOYMENT_URL || defaultMockDataServerUri;
const waveformManagerProxyUri =
  process.env.WAVEFORM_MANAGER_PROXY_URI || process.env.DEPLOYMENT_URL || `http://localhost:3002`;
const workflowManagerProxyUri =
  process.env.WORKFLOW_MANAGER_PROXY_URI || process.env.DEPLOYMENT_URL || `http://localhost:3003`;

const baseDir = resolve(__dirname, '.');
const webpackSohPaths: GetPaths = (isProduction: boolean): WebpackPaths =>
  getWebpackPaths({
    baseDir: resolve(__dirname, '.'),
    useWorkspace: true,
    tsconfigFileName: 'tsconfig-build.json',
    subDir: isProduction ? 'soh/production' : 'soh/development'
  });

const webpackIanPaths: GetPaths = (isProduction: boolean): WebpackPaths =>
  getWebpackPaths({
    baseDir: resolve(__dirname, '.'),
    useWorkspace: true,
    tsconfigFileName: 'tsconfig-build.json',
    subDir: isProduction ? 'ian/production' : 'ian/development',
    swPath: resolve(baseDir, '../ui-workers/src/ts/sw.ts'),
    appManifest: './ui-ian-app.webmanifest'
  });

const availableSoundFiles = (webpackPaths: WebpackPaths) => {
  const files = getAllFilesFromFolder(join(webpackPaths.baseDir, 'resources/sounds'), /.mp3$/).join(
    ';'
  );
  console.log(`Configured available sound files: ${files}`);
  return files;
};

const commonProxyRouteConfig = {
  // !WARNING: A backend server running on HTTPS with an invalid certificate
  // !will not be accepted by default - must set to false to accept
  secure: false,
  changeOrigin: true
};

const ianWebpackConfig: GetConfiguration = (
  isProduction: boolean,
  shouldIncludeDevServer: boolean,
  webpackPaths: WebpackPaths
): WebpackConfig => {
  const entryPoints: EntryObject = {};
  // add the service worker entry point if set in env
  if (process.env.GMS_SW !== 'false') {
    entryPoints.sw = {
      import: resolve(webpackPaths.sw)
    };
  }

  entryPoints['ui-ian-app'] = {
    import: resolve(webpackPaths.src, 'ts/app/ui-ian-app/index.tsx')
  };

  return {
    name: 'ui-ian-app',
    title: 'GMS Interactive Analysis',
    paths: webpackPaths,
    isProduction,
    shouldIncludeDevServer,
    entry: entryPoints,
    htmlWebpackPluginOptions: {
      envInjectScript: `<script src="./env-inject.js"></script>`,
      cesiumScript: `<script src="./${webpackPaths.cesiumDir}/Cesium.js"></script>`,
      appManifest: `<link rel="manifest" href="${webpackPaths.appManifest}">`
    },
    alias: {}
  };
};

const sohWebpackConfig: GetConfiguration = (
  isProduction: boolean,
  shouldIncludeDevServer: boolean,
  webpackPaths: WebpackPaths
): WebpackConfig => {
  const entryPoints: EntryObject = {};

  entryPoints['ui-soh-app'] = {
    import: resolve(webpackPaths.src, 'ts/app/ui-soh-app/index.tsx')
  };

  return {
    name: 'ui-soh-app',
    title: 'GMS SOH Monitoring',
    paths: webpackPaths,
    isProduction,
    shouldIncludeDevServer,
    entry: entryPoints,
    htmlWebpackPluginOptions: {
      envInjectScript: `<script src="./env-inject.js"></script>`,
      cesiumScript: `<script src="./${webpackPaths.cesiumDir}/Cesium.js"></script>`
    },
    alias: {}
  };
};

const getModuleLoaders = (webpackPaths: WebpackPaths) => {
  const loaders = [babelLoader(webpackPaths.nodeModules)];
  if (!process.env.DISABLE_INSTRUMENTATION) {
    loaders.push({
      test: /\.js$|\.jsx$/,
      use: {
        loader: 'istanbul-instrumenter-loader',
        options: { esModules: true, debug: false }
      },
      enforce: 'post',
      exclude: /node_modules|\.spec\.js$/
    });
  }
  return loaders;
};

const getCommonConfig = (
  mode: string,
  isProduction: boolean,
  webpackPaths: WebpackPaths
): Configuration => {
  console.log(`${!isProduction ? 'Applying' : 'Not Applying'} code instrumentation for ${mode}`);

  const GMS_KEYCLOAK_REALM = `${process.env.GMS_KEYCLOAK_REALM}`;
  const GMS_KEYCLOAK_URL = `${process.env.GMS_KEYCLOAK_URL}`;
  const GMS_KEYCLOAK_CLIENT_ID = `${process.env.GMS_KEYCLOAK_CLIENT_ID}`;
  const GMS_DISABLE_KEYCLOAK_AUTH = process.env.GMS_DISABLE_KEYCLOAK_AUTH !== 'false';

  if (GMS_DISABLE_KEYCLOAK_AUTH) {
    console.log(`KEYCLOAK authentication is disabled`);
  } else {
    console.log(`KEYCLOAK authentication is enabled with the following parameters`);
    console.log(`  --> GMS_KEYCLOAK_REALM : ${GMS_KEYCLOAK_REALM}`);
    console.log(`  --> GMS_KEYCLOAK_URL : ${GMS_KEYCLOAK_URL}`);
    console.log(`  --> GMS_KEYCLOAK_CLIENT_ID : ${GMS_KEYCLOAK_CLIENT_ID}`);
    console.log(`If any of the KEYCLOAK parameters are not defined, KEYCLOAK will be disabled`);
  }

  return webpackMerge(
    {
      externals: {
        electron: 'electron'
      },
      module: {
        // only apply code instrumentation when not production
        rules: !isProduction ? getModuleLoaders(webpackPaths) : []
      },
      plugins: [
        webpackCopy({
          patterns: [
            {
              from: join(webpackPaths.baseDir, `resources/sounds`),
              to: resolve(webpackPaths.dist, `resources/sounds`)
            },
            {
              from: join(webpackPaths.baseDir, 'ui-ian-app.webmanifest'),
              to: resolve(webpackPaths.dist, 'ui-ian-app.webmanifest')
            },
            {
              from: join(webpackPaths.baseDir, 'resources/images'),
              to: resolve(webpackPaths.dist, 'resources/images')
            },
            {
              from: join(webpackPaths.baseDir, 'src/ts/env/env-inject-template.js'),
              to: resolve(webpackPaths.dist, 'env-inject-template.js')
            },
            {
              from: join(webpackPaths.baseDir, 'src/ts/env/env-inject-template.js'),
              to: resolve(webpackPaths.dist, 'env-inject.js'),
              /* eslint-disable no-template-curly-in-string */
              transform(content) {
                return content
                  .toString()
                  .replace('${GMS_KEYCLOAK_REALM}', GMS_KEYCLOAK_REALM)
                  .replace('${GMS_KEYCLOAK_URL}', GMS_KEYCLOAK_URL)
                  .replace('${GMS_KEYCLOAK_CLIENT_ID}', GMS_KEYCLOAK_CLIENT_ID)
                  .replace('${GMS_DISABLE_KEYCLOAK_AUTH}', GMS_DISABLE_KEYCLOAK_AUTH.toString());
                /* eslint-enable no-template-curly-in-string */
              }
            }
          ]
        }),
        new DefinePlugin({
          'process.env.AVAILABLE_SOUND_FILES': JSON.stringify(availableSoundFiles(webpackPaths)),
          'process.env.GMS_UI_MODE': JSON.stringify(mode)
        })
      ]
    },
    cesiumConfig(webpackPaths, isProduction)
  );
};

const devServerConfig = (shouldIncludeDevServer: boolean): Configuration =>
  shouldIncludeDevServer
    ? {
        devServer: {
          https: false,
          proxy: {
            '/frameworks-osd-service/osd/coi/acquired-channel-environment-issues/query/station-id-time-and-type': {
              target: gatewayHttpProxyUri,
              ...commonProxyRouteConfig
            },
            '/frameworks-osd-service/osd/station-groups': {
              target: frameworksOsdProxyUri,
              ...commonProxyRouteConfig
            },
            '/interactive-analysis-api-gateway/alive': {
              target: gatewayHttpProxyUri,
              ...commonProxyRouteConfig
            },
            '/interactive-analysis-api-gateway/ready': {
              target: gatewayHttpProxyUri,
              ...commonProxyRouteConfig
            },
            '/interactive-analysis-api-gateway/health-check': {
              target: gatewayHttpProxyUri,
              ...commonProxyRouteConfig
            },
            '/interactive-analysis-api-gateway/client-log': {
              target: `${gatewayHttpProxyUri}`,
              ...commonProxyRouteConfig
            },
            '/interactive-analysis-api-gateway/acknowledge-soh-status': {
              target: `${gatewayHttpProxyUri}`,
              ...commonProxyRouteConfig
            },
            '/interactive-analysis-api-gateway/quiet-soh-status': {
              target: `${gatewayHttpProxyUri}`,
              ...commonProxyRouteConfig
            },
            '/interactive-analysis-api-gateway/subscriptions': {
              target: subscriptionsProxyUri,
              ws: true,
              ...commonProxyRouteConfig
            },

            '/user-manager-service/user-preferences': {
              target: userManagerProxyUri,
              ...commonProxyRouteConfig
            },
            '/ui-processing-configuration-service': {
              target: processingConfigurationProxyUri,
              ...commonProxyRouteConfig
            },
            '/signal-enhancement-configuration-manager-service/': {
              target: signalEnhancementConfigurationProxyUri,
              ...commonProxyRouteConfig
            },
            '/smds-service/retrieve-system-message-definitions': {
              target: systemMessageDefinitionProxyUri,
              ...commonProxyRouteConfig
            },
            '/ssam-control/retrieve-decimated-historical-station-soh': {
              target: ssamRetrieveDecimatedHistoricalStationSoh,
              ...commonProxyRouteConfig
            },
            '/ssam-control/retrieve-station-soh-monitoring-ui-client-parameters': {
              target: ssamRetrieveStationSohMonitoringUiClientParameters,
              ...commonProxyRouteConfig
            },
            '/station-definition-service/': {
              target: stationDefinitionServiceUri,
              ...commonProxyRouteConfig
            },
            '/waveform-manager-service/': {
              target: waveformManagerProxyUri,
              ...commonProxyRouteConfig
            },
            '/workflow-manager-service/': {
              target: workflowManagerProxyUri,
              ...commonProxyRouteConfig
            },
            '/event-manager-service/': {
              target: eventManagerProxyUri,
              ...commonProxyRouteConfig
            },
            '/signal-detection-manager-service/': {
              target: signalDetectionProxyUri,
              ...commonProxyRouteConfig
            }
          }
        }
      }
    : {};

const getWebpackAppConfig = (
  isProduction: boolean,
  shouldIncludeDevServer: boolean,
  mode: string,
  getPaths: GetPaths,
  getConfiguration: GetConfiguration
): Configuration => {
  console.log(`Creating ${isProduction ? 'production' : 'development'} bundle for ${mode}`);
  const paths = getPaths(isProduction);
  const devServerConf = devServerConfig(shouldIncludeDevServer);
  return webpackMerge(
    getCommonConfig(mode, isProduction, paths),
    appConfig(getConfiguration(isProduction, shouldIncludeDevServer, paths)),
    devServerConf
  );
};

const getWebpackAppConfigs = (
  env: { [key: string]: string | boolean },
  mode: string,
  getPaths: GetPaths,
  getConfiguration: GetConfiguration
): Configuration[] => {
  const shouldIncludeDevServer: boolean = env.devserver === true;

  if (env.production === true) {
    return [getWebpackAppConfig(true, shouldIncludeDevServer, mode, getPaths, getConfiguration)];
  }

  if (env.development === true) {
    return [getWebpackAppConfig(false, shouldIncludeDevServer, mode, getPaths, getConfiguration)];
  }

  return [
    getWebpackAppConfig(true, shouldIncludeDevServer, mode, getPaths, getConfiguration),
    getWebpackAppConfig(false, shouldIncludeDevServer, mode, getPaths, getConfiguration)
  ];
};

const config = (env: { [key: string]: string | boolean }): Configuration[] => {
  if (env) {
    if (env.ian) {
      return getWebpackAppConfigs(env, 'IAN', webpackIanPaths, ianWebpackConfig);
    }

    if (env.soh) {
      return getWebpackAppConfigs(env, 'SOH', webpackSohPaths, sohWebpackConfig);
    }
  }

  return [
    ...getWebpackAppConfigs(env, 'IAN', webpackIanPaths, ianWebpackConfig),
    ...getWebpackAppConfigs(env, 'SOH', webpackSohPaths, sohWebpackConfig)
  ];
};

// eslint-disable-next-line import/no-default-export
export default config;
