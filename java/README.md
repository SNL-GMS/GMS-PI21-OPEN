# GMS Common - Java

## Description
This directory represents the entirety of all common GMS java code, configured as a gradle multi-project build.

## How To Run
Given the multi-project build structure, certain gradle tasks can be run at the root level to build and test
the entire project. All example commands are run from the **java** directory.

`gradle build` - builds and tests the entire project, leveraging the gradle build cache where appropriate

`gradle test` - runs all tests, ignoring the cache

`gradle componentTest` - runs all TestContainers component tests

`gradle postgresTest` - runs all Postgres TestContainers component tests

`gradle kafkaTest` - runs all Postgres TestContainers component tests

`gradle zookeeperTest` - runs all Postgres TestContainers component tests

`gradle docker` - builds all java application/spring boot images (**NOTE**: see docker documentation for details on
proper environment setup to run this task)

Subprojects can be run individually using gradle's syntax

`gradle :subproject:build` - builds the subproject named `subproject`

To see all available tasks for a subproject, run `gradle :subproject:tasks`

## Version Locking
GMS Java projects now incorporate version locking to ensure repeatable builds.
Each subproject will contain its own lock file representing all versions of all dependencies, including
transitive dependencies.

To update lock files, use the `--write-locks` option when running a task that resolves configuration for a project.
The simplest task to run for a subproject would be `dependencies`:

`gradle :subproject:dependencies --write-locks`

But in order to update all locks, the `allDependencies` task should be run, as `dependencies` does not work as expected
at the root level:

`gradle allDependencies --write-locks`
