# Eduviewer backend for Sisu education & study module structures

This component is created to serve Sisu education data by entity id / entity group id and study year over a REST api.

## Installation and running

1. Get Java (min Java 8)
2. (Install and) build with mvn to a jar
3. Run with java -jar eduviewer.jar
3.1. You have to define external properties location for edu-fs-processing when running the jar by "-Dext.properties=/file/location/props.properties"
3.2. You can define --server-port=<portnumber> to override application.properties server port.
3.3. You can define --spring.config.location=<file-path> to override application.properties. In our example run.sh this is the same as ext.properties.

## Data Source Configuration

In external properties use property

backend-type=FS|Postgres to control source of where data is fetched from.

It's possible to extend the Service system to implement your own backends. You need to implement CourseService
or

### Data in Filesystem

Eduviewer uses Sisu data in Sisu export format. Files are saved to disk and the data location is configured in external properties.
There is an example of the format in default-edu.properties.

NOTE: Course units are assumed to be in separate files named by the course unit id.

#### Properites:

data-location=<Overall data location>
course-units-dir=<Course-units-directory>
modules-dir=<Modules directory>
educations-dir=<Educations directory>

### Data in Postgresql

UoH exports data from Sisu to a postrgresql database. Using the postgresql implementation is possible if you
duplicate the format used (basically everything is exported to sisu_export table with the json, then separated into
table per type, field names as lowercase columns with no underscores).


## Reports configuration

It's possible to run health checks for education structure data. First option to run them is through a rest api url /api/data_check/<lv>
which can (and should!) be configured to hide behind a password.

The results can be checked from url /api/data_view/

The other option is to configure reports to run periodically through a cron service and send reports by
email. The relevant properties are:

nightly-report-enabled=<true|false, defaults to false>
nightly-report-lvs=<default lvs with comma separation>
nightly-report-from=<email from, must be valid enough for your smtp server>
nightly-report-to=<email to>
nightly-report-schedule=0 0 6 * * * <- example, cron format
reports-password=<password>



