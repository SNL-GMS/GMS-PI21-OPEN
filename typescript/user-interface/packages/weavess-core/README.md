# Weavess Util

Provides access to core WEAVESS features without a dependency on the window object. This allows these WEAVESS features to be used in web workers in external packages.

## Build Scripts
  * `clean:node_modules`: removes the node_modules directory
  * `clean:build`: removes all of the build directories, files, and artifacts
  * `clean`: cleans and removes all
  * `build:eslint`: runs eslint checks on the package for the source
  * `build:eslint:test`: runs eslint checks on the package for tests
  * `build`: runs the build of the package for the source
  * `build:test`: runs the build of the package for tests
  * `watch`: runs the build and watches for changes to recompile
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

```bash
yarn docs
```

## Building

```bash
[.../weavess-core] $ yarn build
```

## Generating Binaries

```bash
[.../weavess-core] $ yarn build
```
