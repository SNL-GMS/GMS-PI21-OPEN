// eslint-disable-next-line @typescript-eslint/no-require-imports, @typescript-eslint/no-var-requires, @typescript-eslint/unbound-method
const { resolve } = require('path');

const root = resolve(__dirname, '.');

module.exports = {
  rootDir: root,
  bail: true,
  silent: true,
  cacheDirectory: '<rootDir>/.jest-cache',
  testEnvironmentOptions: {
    url: 'http://localhost/'
  },
  automock: false,
  globals: {
    'ts-jest': {
      isolatedModules: true,
      diagnostics: false,
      tsconfig: '<rootDir>/tsconfig-test.json'
    }
  },
  globalSetup: '<rootDir>/jest.global-setup.ts',
  setupFiles: ['jsdom-worker', '<rootDir>/jest.setup.ts'],
  snapshotSerializers: ['enzyme-to-json/serializer'],
  transform: {
    '^.+\\.tsx?$': 'ts-jest',
    '.(css|less|scss)$': '<rootDir>/__mocks__/style-mock.ts'
  },
  testRegex: '/__tests__/.*\\.test\\.(ts|tsx)$',
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json'],
  moduleNameMapper: {
    '~css/(.*)': '<rootDir>/src/css/$1',
    '~app/(.*)': '<rootDir>/src/ts/app/$1',
    '~env(.*)': '<rootDir>/src/ts/env',
    '~components/(.*)': '<rootDir>/src/ts/components/$1',
    '~analyst-ui/(.*)': '<rootDir>/src/ts/components/analyst-ui/$1',
    '~data-acquisition-ui/(.*)': '<rootDir>/src/ts/components/data-acquisition-ui/$1',
    '~common-ui/(.*)': '<rootDir>/src/ts/components/common-ui/$1',
    '~config/(.*)': '<rootDir>/src/ts/config/$1',
    '~resources/(.*)': '<rootDir>/src/ts/resources/$1',
    '~scss-config/(.*)': '<rootDir>/__mocks__/$1',
    '~workers/(.*)': '<rootDir>/src/ts/workers/$1',
    '@gms/((?!golden-layout)[^/]+)$': '<rootDir>/../$1/src/ts/$1',
    '@gms/((?!golden-layout)[^/]+)(/lib/)(.*)$': '<rootDir>/../$1/src/ts/$3',
    '^worker-loader*': '<rootDir>/../../node_modules/worker-loader',
    '.*\\.(css|less|styl|scss|sass)$': '<rootDir>/__mocks__/style-mock.ts',
    '.*\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga|wasm)$':
      '<rootDir>/__mocks__/file-mock.ts',
    '^dexie$': require.resolve('dexie')
  },
  testEnvironment: 'jsdom',
  collectCoverage: true,
  coverageReporters: ['lcov', 'html']
};
