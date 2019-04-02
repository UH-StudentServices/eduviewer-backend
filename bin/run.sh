#!/bin/sh
cd /data/eduviewer/bin/ && java -Dext.properties="db-edu.properties" -jar eduviewer.jar --spring.config.location=file:external-application.properties --server.port=8080 >>/var/log/eduviewer/eduviewer.log 2>&1 &
