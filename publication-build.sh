#!/bin/sh
#
# Copyright 2015-2017 Martin Goellnitz
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#
JDK=`ls -d /opt/*jdk*1.7*|tail -1`
if [ -z "$JDK" ] ; then
  JDK=`ls -d /usr/lib/jvm/*java*1.7*|tail -1`
fi
if [ ! -z "$JDK" ] ; then
  export JAVA_HOME=$JDK
  export PATH=$JAVA_HOME/bin:$PATH
else
  echo "Didn't find Java7 - exiting"
fi
export JAVA_OPTS="-XX:PermSize=128m -XX:MaxPermSize=256m"
cd buildSrc
../gradlew -Prelease clean build publishToMavenLocal
cd ..
./gradlew -Prelease clean build jacocoCombinedReport publishToMavenLocal
