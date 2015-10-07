#!/bin/sh
cd buildSrc
gradle -Prelease clean build publishToMavenLocal
cd ..
gradle -Prelease clean build jacocoCombinedReport publishToMavenLocal
