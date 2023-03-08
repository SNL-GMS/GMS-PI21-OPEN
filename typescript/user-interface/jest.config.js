// eslint-disable-next-line @typescript-eslint/no-require-imports, @typescript-eslint/no-var-requires, @typescript-eslint/unbound-method
const { resolve } = require('path');

const root = resolve(__dirname, '.');

module.exports = {
  rootDir: root,
  bail: true,
  cacheDirectory: '<rootDir>/.jest-cache',
  testEnvironmentOptions: {
    url: 'http://localhost/'
  },
  globals: {
    'ts-jest': {
      isolatedModules: true,
      diagnostics: false
    }
  },
  globalSetup: '<rootDir>/jest.global-setup.ts',
  setupFiles: ['<rootDir>/jest.setup.ts'],
  roots: [
    // '<rootDir>/packages/api-gateway',
    '<rootDir>/packages/common-model',
    '<rootDir>/packages/common-util',
    '<rootDir>/packages/mock-data-server',
    '<rootDir>/packages/ui-app',
    '<rootDir>/packages/ui-core-components',
    '<rootDir>/packages/ui-electron',
    '<rootDir>/packages/ui-state',
    '<rootDir>/packages/ui-util',
    '<rootDir>/packages/ui-wasm',
    '<rootDir>/packages/ui-workers',
    '<rootDir>/packages/weavess',
    '<rootDir>/packages/weavess-core'
  ],
  projects: [
    // '<rootDir>/packages/api-gateway/jest.config.js',
    '<rootDir>/packages/common-model/jest.config.js',
    '<rootDir>/packages/common-util/jest.config.js',
    '<rootDir>/packages/mock-data-server/jest.config.js',
    '<rootDir>/packages/ui-app/jest.config.js',
    '<rootDir>/packages/ui-core-components/jest.config.js',
    '<rootDir>/packages/ui-electron/jest.config.js',
    '<rootDir>/packages/ui-state/jest.config.js',
    '<rootDir>/packages/ui-util/jest.config.js',
    '<rootDir>/packages/ui-wasm/jest.config.js',
    '<rootDir>/packages/ui-workers/jest.config.js',
    '<rootDir>/packages/weavess/jest.config.js',
    '<rootDir>/packages/weavess-core/jest.config.js'
  ],
  snapshotSerializers: ['enzyme-to-json/serializer'],
  transform: {
    '^.+\\.m?jsx?$': ['babel-jest', { configFile: './jest-babelrc.js' }],
    '^.+\\.tsx?$': 'ts-jest'
  },
  moduleNameMapper: {
    '.*\\.(css|less|styl|scss|sass)$': '<rootDir>/__mocks__/style-mock.ts',
    '.*\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga|wasm)$':
      '<rootDir>/__mocks__/file-mock.ts',
    '^worker-loader*': '<rootDir>/../../node_modules/worker-loader'
  },
  testRegex: '/__tests__/.*\\.test\\.(ts|tsx)$',
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json'],
  testEnvironment: 'jsdom',
  collectCoverage: true,
  coverageReporters: ['lcov', 'html', 'text-summary']
};
