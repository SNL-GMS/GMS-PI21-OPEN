# GMS Common Python Environments
GMS uses `conda` for managing Python packages for all our environments.

## Development/Test Environment
This environment contains all requirements necessary to run any GMS
Python script.  In general, this is the environment to install on your
development machine.

### Direct Dependencies and Lock File
The direct dependencies (i.e., packages that are imported in scripts) are
contained in the [`gms-test-environment.yml`](./gms-test-environment.yml) file,
and the versions are explicitly defined.  However, these dependencies also have
their own (transitive) dependencies that also need to be locked to specific
versions.  To handle this, we use `conda` to export a list of all packages in
the environment and save this into a "lock" file
([`gms-test-environment.lock.yml`](./gms-test-environment.lock.yml)).  This
file can then be used to create exact copies of the environment in the future.
The process of creating the lock file is described below in **Changing the
Environment**.

### Installing the Environment
To build the `conda` environment for GMS:
```bash
cd /path/to/gms-common/python
conda env create --name gms --file gms-test-environment.lock.yml
```
> **Note:**  This may take some time to complete.  Be patient.

> **Note:**  If you're not using one of our supported development environments,
> and you run into problems with the command above, try `conda env create
> --name gms --file gms-test-environment.yml` (omitting the `.lock`) instead.

### Activating the Environment
When you wish to work on Python code for GMS, you'll need to ensure our `conda`
environment is active with:
```bash
conda activate gms
```

### Deactivating the Environment
If you no longer need the environment active, you can turn it off with:
```bash
conda deactivate
```

### Removing an Environment
If you wish to get rid of the environment, for any reason, first deactivate it,
and then:
```bash
conda env remove --name gms
```

### Updating an Environment
As development continues on GMS, there may be changes made to the environment.
To ensure your environment corresponds to the current commit you have checked
out in the repository:
```bash
cd /path/to/gms-common/python
conda activate gms
conda env update --file gms-test-environment.lock.yml --prune
```

> **Note:**  If you're not using one of our supported development environments,
> and you run into problems with the command above, try `conda env update
> --file gms-test-environment.yml --prune` (omitting the `.lock`) instead.

### Changing the Environment
Any time you need to make changes to the GMS environment, the process is
three-fold:
1. Edit the [`gms-test-environment.yml`](./gms-test-environment.yml) file.
2. Rebuild the `conda` environment by first removing it (see above) and then
   installing it again, pointing to the non-lock file; that is, with
   ```bash
   conda env create --name gms --file gms-test-environment.yml
   ```
3. Regenerate the corresponding lock file:
   ```bash
   conda env export \
     --name gms \
     --no-builds \
     --channel conda-forge \
     --override-channels | \
     grep -v "prefix" > gms-test-environment.lock.yml
   ```

> **Note:**  Updating the lock file *must* take place on one of our supported
> development environments.

## Individual Application Environments
Each Python application should have an environment file (`environment.yml`) and
lock file (`environment.lock.yml`) that contain the minimum set of
requirements necessary to run.  This lock environment is used inside Docker
containers.  Each module should also have a `setup.py` file that defines the
dependencies.  When installing, the user (or `Dockerfile`) will run:
```bash
pip install .
```
When adding/updating individual application requirements, the same changes
should also be made to the **Development/Test Environment**.
