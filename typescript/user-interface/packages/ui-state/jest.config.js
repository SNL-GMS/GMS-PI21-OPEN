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
      diagnostics: false,
      tsconfig: '<rootDir>/tsconfig-test.json'
    }
  },
  globalSetup: '<rootDir>/jest.global-setup.ts',
  setupFiles: ['<rootDir>/jest.setup.ts'],
  transform: {
    '^.+\\.m?jsx?$': ['babel-jest', { configFile: './jest-babelrc.js' }],
    '^.+\\.tsx?$': 'ts-jest'
  },
  testRegex: '/__tests__/.*\\.test\\.(ts|tsx)$',
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json'],
  moduleNameMapper: {
    '@gms/((?!golden-layout)[^/]+)$': '<rootDir>/../$1/src/ts/$1',
    '@gms/((?!golden-layout)[^/]+)(/lib/)(.*)$': '<rootDir>/../$1/src/ts/$3',
    '.*\\.(css|less|styl|scss|sass)$': '<rootDir>/__mocks__/style-mock.ts',
    '.*\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga|wasm)$':
      '<rootDir>/__mocks__/file-mock.ts',
    '^worker-loader*': '<rootDir>/../../node_modules/worker-loader',
    '^dexie$': require.resolve('dexie')
  },
  testEnvironment: 'jsdom',
  collectCoverage: true,
  coverageReporters: ['lcov', 'html', 'text-summary']
};
