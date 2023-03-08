# Purpose

The frameworks-data-injector is an application that allows user to inject spcific type of data into a Kafka topic.
For developers to use it, they should do the following:
1. Implement the Modifier class to provide modifications to the type they are producing
2. Add a new InjectableType with the class and modifier
3. Create a json file containing a single object of the desired type

# Usage

In order to use the frameworks-data-injector, first stand up your docker containers, which must contain etcd and kafka, 
plus any of their dependencies.
Run the frameworks-data-injector with the following command:

```docker run --rm -it -v <path_to_local_directory_with_base_file>:/mockdata --network <network_from_compose_file> local/gms-common/frameworks-data-injector:<version> --type <InjectableType_to_inject> --interval <interval_to_inject_at> --initialDelay <desired_initial_delay> --base /mockdata/<file_containing_base_object> --topic <kafka_topic_to_submit_on> --batchSize <number_to_produce_at_a_time> --batchCount <number_of_batches_to_emit> --bootstrapServer <kafka_bootstrap_server> --retries <number_of_times_retry_kafka_connection> --retryBackoff <millis_to_wait_before_trying_again>```

The `batchSize` argument is optional and defaults to 1.
The `batchCount` argument is optional and defaults to `null`.
To emit 100 messages, specify `--batchCount 10 --batchSize 10`.
The `initialDelay` argument is optional and defaults to 0.
The `retries` argument is optional and defaults to 5.
The `retryBackoff` argument is optional and defaults to 3000.