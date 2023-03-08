# Custom Metrics
This document will provide guidance on how to add custom exported metrics to GMS Java services as well as the process necessary to connect the Prometheus metrics monitoring system to the newly exported metrics. Please note, as the initial effort to add metrics was more of an exploratory or proof-of-concept demonstration, there are limitations, recommended approaches, as well as alternatives. Recommended approaches will be described in the documentation where appropriate and limitations and alternatives will be discussed at the end of in the [limitations](#limitations) and [alternatives](#alternatives) sections.


## Overview

At a high level, the current process of exporting custom metrics in a Java project for use in a Prometheus monitoring ecosystem is as follows:

1. Creating an Java MBeans object to represent the state of the custom metrics. For more information on what a managed Java object is: https://docs.oracle.com/javase/tutorial/jmx/mbeans/index.html
2. Register the MBean with a `javax.management.MBeanServer`.
3. Wherever the metric is to be updated in the code, mutate the MBean accordingly.

These three steps are sufficient in registering the custom metrics with the javax system. However, in order to expose these metrics in a form suited for consumption by Prometheus, the following are also necessary.

4. Add the JMX exporter to the application dependencies. For more information on the JMX Exporter and what that is: https://github.com/prometheus/jmx_exporter
5. Start the application with the JMX exporter running as a Java Agent by specifying the `-javaagent` JVM argument.

The effect that these two steps have is that alongside the core application/service, a separate HTTP endpoint hosting the custom metrics and also the standard JVM metrics is started. The port that the metrics endpoint runs on, as well as other configurations can be specified either in configuration files or Java arguments. This will be discussed in greater detail later.

Finally, the receiving Prometheus monitoring system must be configured to consume the custom metrics.

6. Update the Prometheus configurations to include the new custom metrics endpoint
7. Add visualizations to Grafana or whatever visualization tool is being used.

The following documentation categorizes each step into one of three categories that are discussed in detail in their own section:
- [Inserting/collecting metrics](#insertingcollecting-metrics) (includes steps 1-3)
- [Exporting metrics for Prometheus](#exporting-metrics-for-use-with-prometheus) (includes steps 4-5)
- [Consuming/visualizing metrics](#consuming-and-visualizing-metrics-with-prometheus-and-grafana) (includes steps 6-7)

## Inserting/collecting metrics
**Note**: As the extant metrics effort was designed as a proof-of-concept demonstration, the existing code in the GMS codebase is meant to serve as a general purpose helper that allows for creation and maintenance of small one-off metrics. For alternatives and possible recommended updates, see the [alternatives](#alternatives) section.
 
### Creating, registering, and modifying a Java MBeans object
At its simplest form, collecting custom metrics in a Java project is simply a matter of creating an managed Java object and registering it with the javax manager. For a good tutorial on the general subject matter: https://sysdig.com/blog/jmx-monitoring-custom-metrics/.

### How to add new metrics based on the current utilities
GMS currently contains a general purpose utility called `gms.shared.metrics.CustomMetric` for implementing simple, generic, custom metrics. This utility is meant to allow the developer to easily create, register and update single-value metrics through a simple factory interface. 

#### The CustomMetric class
The principle behind this class is the idea that when updating the state of any particular metric, the new/next value of the metric is based on the current value *and* some arbitrary additional context (for example, the state of some other Java object). To that end, the CustomMetric has the following generic types:


```java
class CustomMetric<T, U>
``` 

- `T` is the type of the "attached object" or the aforementioned "additional context". 
- `U` is the type of the metric itself (ex. `Long`, `Integer`, etc.)

#### Defining how a metric is updated
The next step is to define how the metric is updated. This is done simply by defining a Java BiFunction or static method with arity 2 where the first argument is the current/old value of the metric and the second argument is the "attached object". For example, a simple metric would be an `incrementor` where each time the metric is updated, the old metric value is simply increased. This update function would look like the following:

```java
BiFunction<Long, Object, Long> incrementor = (oldValue, _attachedObject) -> oldValue + 1;
```

In this instance, the attached object is ignored; additional context is not necessary to create a metric whose value is updated simply by incrementing the previous value. However, consider the case where when updated, a metric is incremented by an arbitrary value defined by the "attached object". In that case, the update function might resemble the following:

```java
BiFunction<Long, Long, Long> incrementor = (oldValue, offset) -> oldValue + offset;
```

In this instance, the "attached object" is simply an offset to add to the existing metric. Finally, this behavior can be generalized to any arbitrary object, operation, or expression. For example:

```java
BiFunction<Long, CustomClass, Long> updater = (oldValue, context) -> context.getValue() * oldValue;
```

#### Creating the metric instance
The creation of an instance of CustomMetric is simply a combination of the following:
- The name/description of the metric.
  - For more information on the "type": https://prometheus.io/docs/instrumenting/writing_exporters/#types
- The initial value of the metric
- The function describing how the metric should be updated.

Thus, if you want to create a custom metric using the "updater" function described above, you would simply do the following:

```java
String metricName = "my_metric_name:type=Metric";
Long initialValue = 0L;
BiFunction<Long, CustomClass, Long> updater = (oldValue, context) -> context.getValue() * oldValue;

var myMetric = CustomMetric.create(updater, metricName, initialValue);
```

This method will create and register your custom, single-value metric with the Management server and upon creation, a metric called `my_metric_name` will be available for consumption.

#### Updating the metric
Because the process of updating the metric is defined upon creation of the metric, the only thing that must be done to update the metric is to call the `CustomMetric::update` method; the argument to the method is simply the "attached object" or additional context that is needed to update the metric. For example, if we have the `myMetric` metric defined above and wanted to update it with a new instance of `CustomClass` called `newContext`, we would simply call:

```java
myMetric.update(newContext);
```

### Next steps
As described above, this current metric utility was designed as a demonstration/proof-of-concept and is best suited in instances where you have small one-off, single-value metrics. If you have metrics with more complex needs (ex. a metric must be updated via context from multiple Java objects or the the custom metric needs to be a collection of multiple values), is is recommended simply to create a new MBean or additional custom metric classes where necessary. For more information on this, see the [alternatives](#alternatives) section.

## Exporting metrics for use with Prometheus
Once metrics have been defined in the code, they must be exported if they are to be used in conjunction with Prometheus. The path that we have currently taken is to use the JMX exporter: https://github.com/prometheus/jmx_exporter which allows for the export of all JMX metrics as an HTTP service in the format that Prometheus recognizes. In order to do this, the following must be done:

- Including the JMX exporter as a dependency.
- Updating the runtime Java arguments to run the JMX Exporter as a javaagent alongside the main service.

### Adding JMX Agent to Gradle Build File
The core changes necessary to export the metrics can be done primarily in the `build.gradle` of the sub-project defining the **runnable application**  or **service** that you want to associate the metrics endpoint with. The following are the additions to the `build.gradle` that are currently necessary to:
- Selectively enable or disable the metrics using a `jmxEnable` property
- Enable metrics both when running a service/application via gradle or when the application is bundled/distributed.

```groovy
dependencies {
    // Custom metrics JMX dependency
    implementation("io.prometheus.jmx:jmx_prometheus_javaagent:${jmxVersion}")
}

ext {
    jmxJarName = "jmx_prometheus_javaagent-${jmxVersion}.jar"
    jmxExporterFlag = "-javaagent:%s=${jmxPort}:%s/${jmxConfigName}"
}

if (project.hasProperty('jmxEnable') && jmxEnable == "true") {
    /**
     * Configure the "application" plugin.
     *
     * For all application runtime tasks, include the Java agent parameter that enables metrics as the default JVM
     * argument. The defaults args resemble the following when fully rendered:
     *
     *   ex. -javaagent:/path/to/jmx_prometheus_javaagent-0.12.0.jar=8383:/path/to/prometheus-jmx-config.yaml
     *
     */
    application {
        def jmxJarPath = "${sourceSets.main.runtimeClasspath.find { it.name == jmxJarName }}"
        applicationDefaultJvmArgs = [sprintf("${jmxExporterFlag}", jmxJarPath, projectDir)]
    }

    /**
     * Configure the "distribution" plugin.
     *
     * Include the Prometheus metrics yaml file which is needed in the runtime environment of the distribution.
     */
    distributions.main.contents { from jmxConfigName }

    /**
     * Configure the "startScripts" task provided by the "distribution" plugin.
     *
     * Set the JVM_OPTS of the start script such that the Java agent parameter that enables metrics is included. The
     * format of this argument is the same as described in the comment for the application configuration section.
     */
    tasks.startScripts {
        defaultJvmOpts = [sprintf("${jmxExporterFlag}", "${jmxConfigDockerPath}/${jmxJarName}", ".")]
    }
}
```
Note that these configurations must be merged in with an existing `build.gradle` and do not represent the fully-qualified gradle configurations necessary to run an arbitrary project. See the [reference](../frameworks/frameworks-osd/frameworks-osd-service/build.gradle) for what a fully-qualified

In this current form, the build.gradle requires several new variables for configurations purposes:

- jmxEnable: Whether or not to enable JMX exporter at all
- jmxVersion: The version of JMX Exporter to use
- jmxPort: The port on which to run the JMX Exporter agent
- jmxConfigName: The location of the JMX Exporter configuration file 
- jmxConfigDockerPath: The path in the Docker container containing the JMX exporter jar.
 
 These values are defined in a local `gradle.properties` file. A sample `gradle.properties` file is provided which has defaults for these values. However any and all of these value can be hardcoded into the `build.gradle` file if desired, updated, or overridden. See the [reference](../frameworks/frameworks-osd/frameworks-osd-service/gradle.properties)

### JMX Config file
In order for the JMX agent to run it needs a config file in `.yaml` format passed to it. The most basic config is a blank file, as all of the parameters are optional. Even if no configs are to be customized, a blank config file must still be passed to the JMX Agent. 

The configuration file additionally allows for specifying either a whitelist or a blacklist of metrics. If neither are specified, the agent will query and export all metrics. The blacklist takes precedence over the whitelist if both are specified. A list of all parameters for the config file can be found [here](https://github.com/prometheus/jmx_exporter#configuration). 

Which metrics are to be collected and exported can also be defined by using rules, which allow for finer grain filtering rather that using either white or black lists. Examples of different configuration files can be found [here](https://github.com/prometheus/jmx_exporter/tree/master/example_configs)

Currently, the project contains a blank configuration file. See the [reference](../frameworks/frameworks-osd/frameworks-osd-service/prometheus-jmx-config.yaml).

If everything is configured correctly, making an HTTP GET request while the service/application is running to the port specified will return metrics. For example:

```bash
$ curl localhost:8383
```

...will stop:

```
HELP jmx_config_reload_failure_total Number of times configuration have failed to be reloaded.
# TYPE jmx_config_reload_failure_total counter
jmx_config_reload_failure_total 0.0
# HELP process_cpu_seconds_total Total user and system CPU time spent in seconds.
# TYPE process_cpu_seconds_total counter
process_cpu_seconds_total 13.901252
# HELP process_start_time_seconds Start time of the process since unix epoch in seconds.
# TYPE process_start_time_seconds gauge
process_start_time_seconds 1.585674386418E9
...
```

### Next steps
As with the overall design of the custom metrics, this was set up primarily as a standalone demonstration. As a result, for full integration with the GMS system, there are further recommendations that are included in the [alternatives](#alternatives) section of this documentation.

## Consuming and visualizing metrics with Prometheus and Grafana

### Exporting Custom Metrics to Prometheus
As covered before, all mBeans are exposed through the jmx-exporter agent by default unless a whitelist or a blacklist has been implemented to restrict which metrics are exported. The jmx-exporter that is being used is explicitly designed to export metrics in the correct format for Prometheus but in order to ensure that Prometheus can pull these metrics, the Prometheus config file must be updated.

In particular, a job can be added to the list of 
[scrape configs](https://prometheus.io/docs/prometheus/latest/configuration/configuration/#scrape_config) in the `prometheus.yml` config file. 
These `scrape_configs` are a list of jobs that Prometheus will pull data from. Currently the [dns_sd_configs](https://prometheus.io/docs/prometheus/latest/configuration/configuration/#dns_sd_config) are used for services in the stack as this takes advantage of Docker's own internal dns service and allows us to specify services by name. Thus the name of the service and the port that jmx-exporter has been configured to expose metrics on are needed to create the scrape job. 

```
- job_name: 'fk-control-service'
    dns_sd_configs:
      - names:
        - 'tasks.fk-control-service'
        type: 'A'
        port: 8383

```

Once the Prometheus configuration file has been changed, the prometheus container (if already running) needs to be restarted for the change in the configuration to take effect. 

### Visualizing Custom Metrics with Grafana
After configuring Prometheus to pull metrics from the service, we can create visualizations with Grafana. Once logged into Grafana, you can either create a new dashboard, a process which is detailed in the top level documentation of Grafana under `/docs/metrics/usages/dashboards.md` of this repository, or add a panel to an existing dashboard. A panel is a visualization of one or more metrics. Once in a dashboard, click on the button on the top panel that has a bar graph and a plus sign. This will create a new panel. Click and the `Add Query` button. Select the appropriate Prometheus instances from the drop down menu labeled `Query`. The query will be in [PromQL](https://prometheus.io/docs/prometheus/latest/querying/basics/) which is Prometheus' own query language. A good place to start is to just type the metric name into the query field; Prometheus will begin to search for the metric matching on the character provided. If everything is working correctly, it will populate the suggestions with your metric's name. This will display just your custom metrics and if you want to converted into a rate or another format, you can use the PromQL functions.

If you want to customize the visualization of the metric, click on the `Visualizations` button located under the highlighted `Queries` button on left side of the screen. This will allow you to select the visualization of your metrics. You can now save your new panel, and be able to come back view your metrics in real time through Grafana. 

## Limitations
### Persistent metrics
Currently, this set-up is best suited for reporting metrics regarding instantaneous information (ex. what the current value of a variable is, the execution time of the last request). While monotonically increasing information such as a counter can be accomplished, there is a limitation in the current set-up in that on restart, the metric is initialized again. This means that a counter recording the total number of hits will be reverted to zero if a service restarts.

One possible solution would be to set-up the MBean to be initialized from some persistent data source. Alternatively, as Prometheus is capable of doing aggregations and taking ranges of instant values and turning them into range vectors, another approach would be to cater the metrics reported by the custom metrics endpoint to only expose "instant" values and allow Prometheus to store the instant values and process them into range vectors.

## Alternatives
### Separate CustomMetric and the metric updating utilities
Currently, CustomMetric has a static `incremeter` method that matches the incrementer described in the [updating](#defining-how-a-metric-is-updated) section. This is meant to be a utility for use with the updating function for simple incrementer metrics. Ultimately, it likely makes more sense to place these updater utilities in a *different location* than the CustomMetric class itself, in order to separate the concerns.

### Alternative metric MBean setup
As described above, the current metric utility was designed for single, one-off, customizable metrics. In some cases, it may be appropriate to have more specific MBeans metric definitions. For example:
- Rather than one metric value, you have multiple related metrics that you want to collect and update together.
- One metric or a set of metrics have complex updating parameters, requiring complex operations and data gathered from different sources (ex. external sources, multiple objects, etc.)

In such a case, the recommendation would be to create new metric classes entirely that are specific to their particular use case. This would simply be a matter of creating a new valid MBean as described in this helpful tutorial: https://sysdig.com/blog/jmx-monitoring-custom-metrics/

### Alternative to native JMX
In the current proof-of-concept, we strove to demonstrate how arbitrary metrics could be added to an application. Through these means, complex and highly customizable metrics are enabled, allowing the developer to define any arbitrary computations such as incorporating the values of arbitrary Java objects. The focal point of the effort was to demonstrate how any arbitrary metric could be added.

However, for some cases, the power of fully customizable metrics is not necessary, especially if there is a fixed set of metrics that need to be applied to for a particular service. As an example, one such metric might be the execution time of various API endpoints. Because this is a very standard operation/need, API frameworks may simply offer this as a built-in feature.

As a result, for *common, standard, or regular* metrics operations such as execution time of a function or API endpoint, it would be advisable to look into other options or frameworks designed to provide a standard set of metrics.

### Alternative JMX Exporter configurations
Currently, the project uses only the default JMX exporter configurations, but it may prove useful to configure the JMX exporter further. The full list of configurations are here: https://github.com/prometheus/jmx_exporter#configuration.

### Alternative JMX Exporter project structure
Currently, the `gradle` settings used to configure and enable the exporting of JMX metrics in a Prometheus form are co-located with the `frameworks-osd-service`. These include:
   - The JMX exporter dependency declaration and its version
   - The various default variable values required to configure the JMX endpoint
   - The `build.gradle` scripting required to enable the JMX endpoint.
 
 However, given that the `frameworks-osd-service` is but *one* of the services defined by GMS, it will likely make sense in the future to migrate these configurations into a shared location such as the root of the project and then have the sub-projects selectively import the JMX configurations when necessary. 

### Alternative JMX Exporter run configurations
Currently, JMX Exporter runs as a Java Agent along side each service/application that is exposing metrics. As described in the README for the JMX Exporter (https://github.com/prometheus/jmx_exporter), it is possible to run the JMX exporter as a standalone service that hooks into JMX enabled services. While it is *encouraged* to run the JMX exporter as a Java Agent, it may be possible that the needs of GMS are such that running the JMX exporter as a standalone service makes more sense.

## Appendix
The following is auxiliary information on how the existing custom metrics operate.

### Public mBeans Interface
This interface in order to work correctly must be named *someClassName*MBeans, where *someClassName* is the name of the class that is going to implement the interface. For example if I want a class that counts the number of http requests, and its named *HTTPCounter* then the mBeans interface would be named *HTTPCounterMBean*. This is not implementing some library interface, but is simply the naming convention required by JMX. All of the following code snippets are examples illustrating whats needed to create a basic mBean metric. The full code used in creating the the custom metrics for the `fk-control-service` can be found in the metrics directory under `fk-control/.../fk/control` 

```
public interface CustomMetricMBean {
    public T getMetricVal();
    public void updateMetric(T current);

}
```

The mBeans interface at minimum must define a `getter` method for the member fields that should be exported via JMX. Thus if we want to export some member called `metric` then the interface should define a a `getMetric()` method. 

### Class That Implements mBeans
After the mBeans interface called *someClassName*MBeans is created, a class named *someClassName* needs to be created an instantiated. This class will add the logic to the methods defined in the interface as well as any other necessary fields or logic needed to produce the correct metric. 

```

public class CustomMetric implements CustomMetricMBean{
    private T metricVal;
    ...

     public T getMetricVal(){
        return this.metricVal;
    }

    public void updateMetric(T current) {
        this.metricVal = this.update.apply(this.metricVal, current);
    }

   ...

}
```

Other classes can also extend this class, and provided that they do not implement another mBeans interface directly, they will be treated as using the parent's MBean interface if they are registered with the MBeans server. 

### mBeans Server

The mBeans server is part of the `javax.management` library. By importing this library, you can make a copy of the `MBeansServer` object. Once you have a copy of the `MBeansServer` object, you can register instances of your class that implement an MBeans interface with it, using its `register` method. The `register` method takes two arguments, the instance of the class that implements the mBeans interface and an `ObjectName` object.  The ObjectName represents the object name of an MBean, and it must be constructed following specific patterns, which can be found [here](https://docs.oracle.com/javase/8/docs/api/javax/management/ObjectName.html). Once the object is registered to the MBeanServer, it will be exported via the JMX Exporter. 

```
import javax.management.*;
import java.lang.management.*;

public class MetricRegister {
    private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    public static void register(CustomMetric customMetric, ObjectName name) throws JMException {
        mBeanServer.registerMBean(customMetric, name);
    }
}
```
