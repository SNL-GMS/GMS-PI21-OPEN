# User Interface

Front end for the GMS User Interface.

This project consists of multiple sub-projects connected using [lerna](https://github.com/lerna/lerna).
Sub-Projects:

* ui-app -> main user interface components, organized using golden layout
* ui-electron -> runs ui-app in an electron stand-alone window
* ui-core-components -> library of reusable ui components
* weavess -> library used in analysis to visualize waveform data

## Installation

Install [Nodejs v14](https://nodejs.org/en/download/), then run

```bash
yarn
yarn build
```

## Getting Started

### State of Health UI

To run the SOH UI, execute the following (in their own terminals) from this directory (`typescript/user-interface`):

```bash
GMS_UI_MODE=soh yarn dev
GMS_UI_MODE=soh yarn mock boot
GMS_UI_MODE=soh cd ./packages/api-gateway && yarn start
```

### Interactive Analysis UI

To run the IAN UI using mock data, run the following commands from separate terminals:

```bash
GMS_UI_MODE=ian yarn mock boot
GMS_UI_MODE=ian yarn start
```


### Running with keycloak enabled locally

Io run the UI with keycloak enabled locally, set these environment variable in your bash_profile or bash_rc
```bash
GMS_KEYCLOAK_REALM="gms"
GMS_KEYCLOAK_URL="KEYCLOAK_URL_HERE"
GMS_KEYCLOAK_CLIENT_ID="gms-development"
GMS_DISABLE_KEYCLOAK_AUTH: false
```
If you would like to disable keycloak for local runs, simply set `GMS_DISABLE_KEYCLOAK_AUTH: true`, or remove any of the other keycloak variables. 

## Build Scripts

* `clean`: runs a lerna clean to remove all node_modules
* `build:dev`: runs a lerna development build for all sub packages
* `build:prod`: runs a lerna production build for all sub packages
* `build`: runs a lerna production build for all sub packages
* `start:dev`: starts the webpack dev server in development mode `localhost:8080/` for analyst-core-ui
* `start:prod`: starts the webpack dev server in production mode `localhost:8080/` for analyst-core-ui
* `start`: starts the webpack dev server in production mode `localhost:8080/` for analyst-core-ui
* `mock`: the SOH mocker. See below for sub-commands.
* `docs`: generates the package source documentation for all sub packages
* `docs-user-interface`: generates the package source documentation for user interface
* `"docs-ui-app`: generates the package source documentation for ui-app
* `"docs-ui-electron`: generates the package source documentation for ui-electron
* `docs-ui-core-components`: generates the package source documentation for ui-core-components
* `docs-weavess`: generates the package source documentation for weavess
* `sonar`: runs sonar lint checks across all sub packages
* `test`: runs lerna test to run the package jest tests for all sub packages
* `test-ian`: runs lerna test to run the package jest tests for all sub packages, then runs IAN cypress tests
* `test-jest`: runs the package jest tests
* `version`: returns the version of the package

### Environment config for Mac

You will need Java 11 for the mocker. To install it on a mac, you can run

```bash
brew tap AdoptOpenJDK/openjdk
brew cask install adoptopenjdk11
```

You will also need to alias localhost to kafka. Add this line to your `/etc/hosts` file:

```bash
127.0.0.1   kafka
```

You will need the $GMS_COMMON_DIR set to contain the absolute path to your `gms-common` directory. This should probably be set in a permanent location, like your `.bashrc` file.

### Mock

The mock script, aliased to `yarn mock`, can be used to build, run, and manage the mock data server, frameworks data injector, and/or mock waveform server. In SOH mode, it consists of two distinct systems, the mock data server (which is an express server run by node), and the frameworks data injector system, which is run using docker-compose. It has many options, which can be explored using yarn mock help, or by running help on any of the following commands:

* `yarn mock boot` The all-in-one command. In IAN mode, simply starts the mock data server. In SOH Mode, it also removes existing docker containers, creates the docker containers with Kafka and Zookeeper and etcd, creates a Kafka topic, and runs the mocker.
* `yarn mock build` For SOH mode, builds the frameworks data injector image and starts the docker containers.
* `yarn mock clean` Stop, remove, and prune docker containers, images and volumes. Also kill the mock data server. Also Removes images of mock waveform server and json server.
* `yarn mock run` Runs the mocker (must run build, first, when in SOH mode).
* `yarn mock info` print info about the docker containers, images, and volumes that may be of interest
* `yarn mock stop` Stop the containers for the mocker and kill the mock data server.

### Using Real Data for a Service

Currently, each endpoint can be configured to point to either a local service or a remote service. By default, most services are
pointing to either `localhost:3000` or `localhost:3001`. In order to change where that service is run, you can run the development
server with some environment variables that tell the [webpack dev server](packages/ui-app/webpack.config.ts) where to point those endpoints.
For example, if you want to point the dev server to a running Station Definition Service, you could start the dev server with: `STATION_DEFINITION_SERVICE_URL="https://station-definition-service.com" GMS_UI_MODE="ian" yarn dev`. Now requests will be made to the endpoint located at `https://station-definition-service.com`. 

#### Using Real Data for All Services

As a helpful shortcut, you can set all endpoints to point at a single deployment by setting the  `DEPLOYMENT_URL` variable like so:

```sh
DEPLOYMENT_URL=https://example.com yarn dev
```

Note that you do not need to add the `interactive-analysis-ui` route.

For a full list of environment variables that can be set, please see the
[webpack file](packages/ui-app/webpack.config.ts) and also refer to the section [Route Configuration Section](#route-configuration)

### <a name="route-configuration">Route Configuration </a>

This app leverages proxying to make requests to services. This enables the UI to make requests to an endpoint on its own server (e.g., `http://localhost:8080/myProxiedService`), offloading the endpoint configuration to the web server itself&mdash;the UI doesn't have to configure endpoint locations. This obviates the need for specialized CORS configurations, and it allows the use of environment variables in the production deployment at run time instead of at build time.

In development mode, [webpack is used](packages/ui-app/webpack.config.ts) to proxy requests, and in the production build, [nginx is used](packages/ui-app/nginx/nginx-ian.template) to proxy requests.

## Deployment

This directory contains a `Dockerfile` and can be built as such, e.g. `docker build -t gms/analyst-ui .`

## Development

After installing dependencies, see the README in any sub-project under [./packages](packages) for instructions on developing in that particular project

## Tests

Unit tests and [integration tests](packages/integration-tests/README.md) are written in TypeScript, using [Jest](https://jestjs.io/).

To run the tests:

```bash
yarn test
```

To update test snapshots:

```bash
yarn test -u
```

To run tests on a specific set of files or directories, you can pass one or more strings as arguments to the test. Jest will attempt to match the string(s)
provided to file names or paths, and will run those tests. For example:

```bash
yarn test waveform # will run all tests that have the string `waveform` in their path or file name.
```

The test command run by the pipeline, which runs all tests is:

```bash
yarn test-all:prod
```

## Packages

[ui-app](./packages/ui-app)

[ui-electron](./packages/ui-electron)

[ui-core-components](./packages/ui-core-components)

[weavess](./packages/weavess)
