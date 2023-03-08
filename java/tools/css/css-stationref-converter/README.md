#### Command-line Usage
Command-line application that takes CSS (Center for Seismic Studies) 'station reference' files and produces JSON files containing GMS station reference objects - one file per object type. 
The program takes arguments that each specify an input file, plus one argument `-outputDir` that specifies where to write the output files. **All arguments are required.**

The arguments are:
 - `-affiliationFile`: a CSS affiliation file
 - `-instrumentFile`: a CSS instrument file
 - `-networkFile`: a CSS network file
 - `-sensorFile`: a CSS sensor file
 - `-siteFile`: a CSS site file
 - `-siteChanFile`: a CSS sitechan file

The program writes these files to `outputDir`:
 - `reference-network.json` containing a `ReferenceNetwork[]`
 - `reference-station.json` containing a `ReferenceStation[]`
 - `reference-site.json` containing a `ReferenceSite[]`
 - `reference-channel.json` containing a `ReferenceChannel[]`
 - `reference-sensor.json` containing a `ReferenceSensor[]`
 - `reference-network-memberships.json` containing a `ReferenceNetworkMembership[]`
 - `reference-station-memberships.json` containing a `ReferenceStationMembership[]`
 - `reference-site-memberships.json` containing a `ReferenceSiteMembership[]`
 - `processing-station-group.json` containing a `StationGroup[]`; these are the `StationGroup`'s corresponding to the 'raw' station's (derived internally from the station reference information produced by this program)
 - `processing-response.json` containing a `Response[]`; these are the latest `Response` objects corresponding to the `ReferenceResponse` objects this program produces and with the proper (processing) channel name association
 
The program also writes a directory of files `outputDir/responses` that contains JSON files with `Response` objects, named as `response-1.json`, `response-2.json`, ..., each containing a single `ReferenceResponse`.

#### Using the CSS to COI Script
Ensure that `<gms-common-root>/bin` is on your path or change into that directory

Note: If the specified output directory is not empty, you will be prompted to allow the script to remove the contents. If the contents are not removed, the conversion process cannot continue.

Example:

`gms-css-to-coi -s /Users/user1/css-root -n config.network`

Usage:
```shell script
-c|--cssroot <CSS root in Docker container>                    | optional, defaults to /css-stationref-converter/css-root
-d|--destination <absolute path to local COI output directory> | optional, defaults to <working dir>/coi-root
-i|--imagename <name of docker image to use>                   | optional, defaults to gms-common/css-stationref-converter
-j|--coiroot <COI root in Docker container>                    | optional, defaults to /css-stationref-converter/coi-root
-n|--networkfilename <network filename with no path>           | optional, defaults to network.dat
-r|--registry <Docker registry>                                | optional, defaults to ${CI_DOCKER_REGISTRY}
-s|--source <absolute path to local CSS root>                  | optional, defaults to <working dir>/css-root
-t|--imagetag <Docker image tag>                               | optional, defaults to latest

-h|--help, print this help message

Note: The full Docker image name is <registry>/<imagename>:<imagetag>,
      e.g. ${CI_DOCKER_REGISTRY}/gms-common/css-stationref-converter:latest
```
