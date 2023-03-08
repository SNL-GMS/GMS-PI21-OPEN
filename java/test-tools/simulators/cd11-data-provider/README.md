# CD1.1 Data Provider

Provides a capability of injecting CD1.1 data into a deployment that is running on the SRN or SON clusters. This
capability serves as input to the data acquisition processes mimicking live operation either using: a known data set
that has been prerecorded, or a copy of the data feed from another running deployment

## Data-provider Running and usage

Start the Data-Provider when the deployment starts

Starting the Data-Provider after a deployment is already running

common datasets used are 81for10min, 20for10min and others that can be found in
the test data set repo

## Data-Provider Configuration

The Data-Provider can be run in two different modes
<ul>
<li> File Based Mode, aka "Injector Mode" runs using stored data that is feed into the system</li>
<li> Kafka Mode, aka "Repeater Mode", runs by coping a data feed from another running deployment </li>
</ul>

### Required Environment Variables

There are certain env vars that must be set no matter the mode, with additional env vars that are mode specific, these
are taken care of under the hood but are noted here for reference in case things change in the future

CONNMAN_ADDRESS: da-connman CONNMAN_PORT: 8041 providerInputMode: kafka

The mode of the data-provider capability is switched via the ENV var "providerInputMode" and then depending on the mode
there are other variables that need to be set

providerInputMode "kafka" for kafka mode or anything else defaults to file based mode

When in kafka mode the following env vars must also be set, example values shown below

CD11_DATA_PROVIDER__REPEATER_SERVERS: ${BASE_DOMAIN}:9094, ${BASE_DOMAIN}:9095, ${BASE_DOMAIN}:9096
CD11_DATA_PROVIDER__CONSUMER_ID: ${STACK_NAME}

When in file based mode the data set to be used will be specified as a config var through the orchestration tool,
gmskube or the older gmsctl but as those may change they are noted here.

dataLocation: set to the checked out location of the cd11-test data set submodule, default to resources dir
referenceTime: now is the default but can be changed to replay data back in time initialDelaySeconds: 0 but can be set
if a delay is desired before data starts flowing

## Troubleshooting/FAQs

Steady state operation will result in acknack messages like shown below this is normal and indicates that the
connections are still alive

{"@timestamp":"2021-01-12T01:01:12.338+00:00","@version":"1","message":"Acknack received for station: ATAH, updating
last update time to now","logger_name":"gms.dataacquisition.stationreceiver.cd11.dataprovider.Cd11Client","
thread_name":"reactor-tcp-epoll-3","level":"INFO","level_value":20000,"component":"common-util"} {"@timestamp":"
2021-01-12T01:01:12.338+00:00","@version":"1","message":"Acknack received for station: ASF, updating last update time to
now","logger_name":"gms.dataacquisition.stationreceiver.cd11.dataprovider.Cd11Client","thread_name":"
reactor-tcp-epoll-2","level":"INFO","level_value":20000,"component":"common-util"} {"@timestamp":"2021-01-12T01:01:
12.340+00:00","@version":"1","message":"Acknack received for station: ATD, updating last update time to now","
logger_name":"gms.dataacquisition.stationreceiver.cd11.dataprovider.Cd11Client","thread_name":"reactor-tcp-epoll-4","
level":"INFO","level_value":20000,"component":"common-util"}