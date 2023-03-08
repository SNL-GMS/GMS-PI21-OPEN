#!/bin/bash

# clean; prepare folders for building 
rm -rf ../typescript/user-interface/packages/ui-wasm/src/ts/wasm/gms-filters
rm -rf ../typescript/user-interface/packages/ui-wasm/lib/wasm/gms-filters

mkdir -p emcmake-build 
mkdir -p ../typescript/user-interface/packages/ui-wasm/src/ts/wasm/gms-filters
mkdir -p ../typescript/user-interface/packages/ui-wasm/lib/wasm/gms-filters

# build wasm
cd emcmake-build
emcmake cmake ..
make

# copy over files to the typescript packages
cp -R wasm/* ../../typescript/user-interface/packages/ui-wasm/src/ts/wasm/gms-filters
cp -R wasm/* ../../typescript/user-interface/packages/ui-wasm/lib/wasm/gms-filters
