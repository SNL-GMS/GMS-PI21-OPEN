# GMS API Integration Tests

This test suite runs integration tests against a GMS Deployment. It generates a human readable report: [artifacts/test-report.html](/artifacts/test-report.html). It also generates output `.json` files in the [artifacts](/artifacts) directory

## Environment

See Getting Started with the UI for instructions on setting up your environment.

### Environment Variables

* `DEPLOYMENT_NAME`: *Optional.* The name of the deployment at which to point the test suite. Defaults to `sb-develop-test`.
* `SIMULATOR_DEPLOYMENT_NAME`: *Optional.* The name of the simulator deployment. If not defined, it will skip over the simulator tests.
* `SERVICE_URL`: *Required.* The url for the deployment, minus the deployment name. eg: if the deployment is at `my-deployment.example.com`, then use `SERVICE_URL=example.com`

### Dependencies and Supported Versions

* Node 14.17
* yarn 1.17

## Installation

* `yarn`

## Running the tests

Notes:

* Makes sure to specify `SERVICE_URL=<service_url>`.
* Set `DEPLOYMENT_NAME=<your_deployment_name>` to point the UI at your deployment.
* Specify the `-u` flag to update snapshots.

* `yarn integration-test` for all tests except simulator tests on the default test deployment: ian-develop-test.
* `DEPLOYMENT_NAME=<name> yarn integration-test` for all tests except simulator tests on the specified deployment.
* `SIMULATOR_DEPLOYMENT_NAME=<name2> DEPLOYMENT_NAME=<name> yarn integration-test` simulator tests run on name2, while the other tests run on name
* `DEPLOYMENT_NAME=<name> yarn integration-test -u` run all tests except simulator tests and update snapshots. Be sure to verify that the updated snapshots look correct!
* `DEPLOYMENT_NAME=<name> yarn integration-test 'channel'` for test files with names containing "channel".
