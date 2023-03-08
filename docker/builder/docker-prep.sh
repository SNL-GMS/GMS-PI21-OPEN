#!/bin/bash -ex

# rsync the src dirs
rsync -av ../ubi/src/ ./src/_ubi
rsync -av ../ubi/typescript/src/ ./src/_typescript
rsync -av ../ubi/python/src/ ./src/_python
