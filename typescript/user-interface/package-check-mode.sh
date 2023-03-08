#!/usr/bin/env bash
if [ "$GMS_UI_MODE" = "soh" ]
then
  echo "Running in SOH mode"
  exit 0
elif [ "$GMS_UI_MODE" = "ian" ]
then
  echo "Running in IAN mode"
  exit 0
else
  echo "Env GMS_UI_MODE must be set [soh, ian]"
  exit 1
fi
