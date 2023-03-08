# GMS Test Utilities
The contents of this directory are as follows:
* [`bin`](./bin):  Test scripts written in bash or Python.  To run these
  scripts, you must have the `gms` test [Python
  environment](../python/README.md) installed and activated on your local
  machine.
* [`config`](./config):  Test processing configurations used in various system
  test procedures.
* [`docker`](./docker):  Test container images applied to a running instance of
  the system as test augmentations.
* [`jupyter`](./jupyter):  Jupyter notebooks used to test the system.  These
  are automatically packaged and are available via the `jupyter` augmentation.
  To run these Jupyter notebooks locally, you must have the `gms` test [Python
  environment](../python/README.md) installed and activated on your local
  machine (see above).
* [`oracle`](./oracle):  Test scripts used for testing and validating the
  Oracle configuration.

## GMS System Test Framework
The ``python/gms_system_test/gms_system_test/gms_system_test.py`` script is the
entry point to the GMS system test framework.  See
[here](../python/gms_system_test) for more details.
