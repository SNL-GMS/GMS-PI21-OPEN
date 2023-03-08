# WEAVESS: Web-Enhanced Application for the Viewing and Exploitation of Sensor Samples

WEAVESS is a JavaScript library for high-performance display of waveform data. It is currently in super-alpha, and only supports very limited functionality.

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

## Development (Examples)
To run the WEAVESS examples (see: http://localhost:8080)
```
$ yarn dev
```

## Documentation

To generate HTML documentation files:
```
$ yarn docs
```

See the [examples](./src/ts/examples) directory for example usage.
