# GMS System Test Framework
The GMS system test framework
1. installs a temporary instance of the system (either `ian`, `sb`, or `soh`),
2. runs a test augmentation against it, collecting results and reporting them
   back, and
3. removes the instance after testing completes.

The framework is organized as follows:
* [`python/utils/gms_system_test/gms_system_test/gms_system_test.py`](./gms_system_test/gms_system_test.py):
  The driver script that any user, developer, or CI service will use to run
  these tests.  This script contains the `GMSSystemTest` class, which handles
  * standing up the temporary instance,
  * waiting for all the pods to be running,
  * waiting for the application to be ready for testing,
  * firing off the test and collecting results, and
  * tearing down the temporary instance.

  See `gms_system_test.py --help` for details.
* [`deploy/augmentation`](../../../deploy/augmentation/GMS_SUBCHART_README.md):
  Contains Helm sub-charts specifying augmentations (both tests and harnesses)
  that can be applied to a running instance.
* [`test/docker`](../../../test/docker):  Contains the files necessary to build
  containers to encapsulate a test along with all of its dependencies.  These
  test container images are run by the test augmentations.

> **Note:**  See all the links above for additional documentation, either in
> ``README.md`` files, or in docstrings.

## Continuous Integration Testing
This framework is used in the jobs in the **Checkout** stage of [our CI
pipelines](../../.gitlab/ci) to run automated integration tests.  Those job
specifications can be found in
[`.gitlab/ci/checkout.gitlab-ci.yml`](../../../.gitlab/ci/checkout.gitlab-ci.yml).
Our CI jobs run these tests in the same way you would in a terminal, using the
[`gms_system_test.py`](./gms_system_test/gms_system_test.py) script.
