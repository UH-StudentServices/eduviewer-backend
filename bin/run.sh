#!/bin/sh
cd /data/eduviewer/bin/ && java -Dext.properties="edu.properties" -jar eduviewer.jar --server.port=8080 >>/var/log/eduviewer/eduviewer.log 2>&1 &
