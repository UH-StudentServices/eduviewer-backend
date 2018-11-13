#!/bin/sh

kill `ps -ef | grep eduviewer | grep -v grep | grep eduviewer.jar | awk '{print $2}'`
