#!/bin/bash

set -ex

rsync -av --copy-links ../../deploy/ ./_deploy
