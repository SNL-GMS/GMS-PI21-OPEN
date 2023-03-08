# CD1.1 RSDF Processor

This processor is a Kafka streams topology that consumes RSDFs from DataMan and produces SOH
 extract, ACEI, and Waveforms.

## By Default Consumes from Topics
* `soh.rsdf`

## By Default Produces to Topics
* `soh.acei`
* `soh.extract`
* `soh.waveform`

## Configuration
### Override via environment variables
* `BOOTSTRAP_SERVERS`
  * default: `kafka:9092`
* `INPUT_RSDF_TOPIC`
  * default: `soh.rsdf`
* `OUTPUT_ACQUIREDCHANNELSOH_TOPIC`
  * default: `soh.acei`
* `OUTPUT_STATIONSOHINPUT_TOPIC`
  * default: `soh.extract`
* `OUTPUT_WAVEFORM_TOPIC`
  * default: `soh.waveform`
* `PARSING_WAVEFORMS`
  * default: `false`
* `CONNECTION_RETRY_COUNT`
  * default: `10`
* `RETRY_BACKOFF_MS`
  * default: (ms): `1000`