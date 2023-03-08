# Analyst Core UI

Analysis display core codebase.

## Build Scripts
  * `clean:node_modules`: removes the node_modules directory
  * `clean:build`: removes all of the build directories, files, and artifacts
  * `clean`: cleans and removes all
  * `build:eslint`: runs eslint checks on the package for the source
  * `build:eslint:test`: runs eslint checks on the package for tests
  * `build`: runs the build of the package for the source
  * `build:test`: runs the build of the package for tests
  * `watch`: runs the build and watches for changes to recompile
  * `dev`: starts the webpack dev server in development mode `localhost:8080/`
  * `start`: starts the webpack dev server in production mode `localhost:8080/`
  * `build:webpack`: "npm-run-all -p build:webpack:dev build:webpack:prod",
  * `build:webpack:dev`: runs and builds the webpack development bundle
  * `build:webpack:prod`: runs and builds the webpack production bundle
  * `bundle:dev`: cleans the dist and runs and builds the webpack development bundle
  * `bundle:prod`: cleans the dist and runs and builds the webpack production bundle
  * `bundle`: cleans the dist and runs and builds the webpack development bundle
  * `docs`: generates the package source documentation
  * `sonar`: runs sonar lint checks
  * `test`: builds and runs the package jest tests as development
  * `test:dev:jest`: runs the package jest tests as development
  * `test:prod:jest`: runs the package jest tests as production
  * `test:dev`: builds and runs the package jest tests as development
  * `test:prod`: builds and runs the package jest tests as production 
  * `version`: returns the version of the package

## Build

```
$ yarn build
```

## Usage

```
$ yarn start
```

## Development
To run the application (see: http://localhost:8080)
```
$ yarn dev
```

## Documentation

To generate HTML documentation files:
```
$ yarn docs
```

## Development

To build the node application

```bash
[.../ui-app] $ yarn build
```

To start a development server hosting the analysis UI

```bash
[.../ui-app] $ yarn start
```

Then, access `http://localhost:8080` in your browser, or run [../ui-electron](../ui-electron) to access the native version of the analysis UI

