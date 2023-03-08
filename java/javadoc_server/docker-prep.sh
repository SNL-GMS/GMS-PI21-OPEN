#!/bin/bash -ex

# copy javadoc files build by gradle
rsync -av ../gms/docs/javadoc/ ./src/javadoc
