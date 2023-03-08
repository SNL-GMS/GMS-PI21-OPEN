## Analyst Core Electron
Electron code for window management/layout persistence 

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

## Documentation

To generate HTML documentation files:
```
$ yarn docs
```

## Running

By default, this will start an electron application and connect to `http://localhost:8080`, unless a `SERVER_URL` environment variable is specified

```bash
[.../ui-electron] $ yarn start
```

or, to connect to a different url

```bash
[.../ui-electron] $ SERVER_URL=http://otherdomain.com:8080 yarn start
```

## Generating Binaries

Generate binaries (on mac os, wine will need to be installed first. This can be done using `brew install wine`)

Set the `SERVER_URL` environment variable to set the default backend that the electron app will attempt to connect to. Otherwise, the url will be set to a development default (localhost)

```bash
[.../ui-electron] $ SERVER_URL=http://otherdomain.com:8080 yarn generate-bin
```

Binaries for darwin (mac os) and windows (win32) will be generated under `dist/`
