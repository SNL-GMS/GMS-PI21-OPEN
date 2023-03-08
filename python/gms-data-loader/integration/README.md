# Data Loader Integration Tests

## Purpose

These component integration tests were designed to test the data loading libraries contained within the 
`gms-data-loader/dataloaders` area against their respective services.

## Design

These tests utilize the `testcontainers` library to manage the setup and teardown of systems to be integrated with the 
loader libraries (e.g. the OSD or Configuration Service)

Current tests include the following functionality:
* Storing Station Reference Information to the OSD
* Storing Station Processing Data to the OSD

## Local Setup and Testing

Initial setup requires the installation of packages in the provided `requirements.txt` through your favorite package 
manager.

e.g. for pip: `pip install -r requirements.txt`

In order to run these tests locally, the following environment variables must be set:
* CI_DOCKER_REGISTRY: Must be set to determine where to retrieve docker image artifacts from
* DOCKER_IMAGE_TAG: Must be set to determine which docker tag must be used to retrieve the correct images from 
artifactory