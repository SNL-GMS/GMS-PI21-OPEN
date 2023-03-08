# GMS Test Container Images
These test containers are used by [the GMS system test
framework](../../python/gms_system_test).  Container images are specified with
one image per directory.  Each directory contains a `Dockerfile` that is used
to build the container image.  Any dependent software must be installed and any
test scripts should be copied into the container and run as the container's
`CMD`.

When creating your own test augmentation container, see
[`cypress-tests`](./cypress-tests) for an example.  Certain commands must be
executed before and after a test, and these are captured in the
[`pre-test`](./test-augmentation-scripts/pre-test) and
[`post-test`](./test-augmentation-scripts/post-test) scripts.
