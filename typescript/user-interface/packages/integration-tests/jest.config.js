// eslint-disable-next-line @typescript-eslint/no-require-imports, @typescript-eslint/no-var-requires, @typescript-eslint/unbound-method
const { resolve } = require('path');

const root = resolve(__dirname, '.');

module.exports = {
  rootDir: root,
  bail: false,
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
  reporters: [
    'default',
    [
      '../../node_modules/jest-html-reporter',
      {
        pageTitle: 'Integration Test Report',
        includeFailureMsg: true,
        logo: '../../ui-app/resources/images/gms-logo.png',
        outputPath: './artifacts/test-report.html',
        includeSuiteFailure: true,
        includeConsoleLog: true
      }
    ]
  ],
  roots: ['<rootDir>'],
  setupFiles: ['<rootDir>/jest.setup.ts'],
  transform: {
    '^.+\\.jsx?$': 'babel-jest',
    '^.+\\.tsx?$': 'ts-jest'
  },
  moduleNameMapper: {
    '@gms/((?!golden-layout)[^/]+)$': '<rootDir>/../$1/src/ts/$1',
    '@gms/((?!golden-layout)[^/]+)(/lib/)(.*)$': '<rootDir>/../$1/src/ts/$3',
    '.*\\.(css|less|styl|scss|sass)$': '<rootDir>/__mocks__/style-mock.ts',
    '.*\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$':
      '<rootDir>/__mocks__/file-mock.ts'
  },
  testRegex: '/__tests__/.*\\.test\\.(ts|tsx)$',
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json'],
  testEnvironment: 'node'
};
