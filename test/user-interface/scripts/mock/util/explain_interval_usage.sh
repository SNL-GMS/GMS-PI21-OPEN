#!/bin/bash

function explain_interval_usage {
  echo "    Interval must be a valid ISO-8601 Duration. It must also follow the constraints below."
  echo "    It must begin with 'PT' to indicate a period of time. It must be followed by a number. And it must end with one of the following:"
  echo "      s for seconds"
  echo "      m for minutes"
  echo "      h for hours"
  echo "    For example: 'PT20s' is 20 seconds. 'PT1h' is one hour." 
  echo "    Note that other duration formats are not supported at this time"
}

explain_interval_usage
