#!/bin/bash -ex

# capture java spec files
rsync -av ../spec/ ./src/spec
