# Eduviewer backend for Sisu education & study module structures

This component is created to serve Sisu education data by entity id / entity group id and study year over a REST api.

## Installation and running

1. Get Java (min Java 8)
2. (Install and) build with mvn to a jar
3. Run with java -jar eduviewer.jar
3.1. You can use --server-port=<portnumber> to override application.properties server port.

## Configuration

Eduviewer uses Sisu data in Sisu export format. Files are saved to disk and the data location is configured in edu.properties.

NOTE: Course units are assumed to be in separate files named by the course unit id.

## Future

At some point when Funidata creates an API to fetch education / study module data it will be implemented to this component
assuming that API will not be open to the world and configurable to be connected to HY ESB.