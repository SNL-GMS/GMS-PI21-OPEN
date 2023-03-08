# Standalone Ingite Node

This service allows users to host data on a standalone Ignite node.
* Currently, no data is hosted and it starts in Client mode

Ignite Tools are hosted on the standalone node
* Visor: useful for inspecting the cluster and caches and can be used to reconfigure the cluster
    * To launch the visor console:
        * ```exec``` into cache-service pod
        * run ```visor``` to bring up console
        * ```open```
        * select the config you want to use to connect to the cache (ie: config/ian-zk-discovery.xml)
        * example commands:
            * cache, cache -a, cache -scan, top, node
            * See visor documentation for more info
          
            
