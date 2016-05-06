#!/bin/sh
VERSION=$1
REPO="~/.m2/repo"
CUSTOM=`grep localRepository ~/.m2/settings.xml`
if  [ ! -z $CUSTOM ] ; then
  # echo $CUSTOM
  REPO=`echo $CUSTOM | sed 's/.localRepository.\(.*\)..localRepository./\1/g' `
fi
REPO=$REPO/tangram
if [ -z $VERSION ] ; then
  echo "Version to be published must be given as the first parameter"
  exit
fi
# echo $REPO
# exit
for f in `(cd $REPO ;find  -type f -name "*$VERSION*"|sort)` ; do
  file=`echo $f |sed -e 's/\.\///g'`
  echo -n $file
  curl -T $REPO/$file -u$ORG_GRADLE_PROJECT_bintrayUser:$ORG_GRADLE_PROJECT_bintrayKey https://api.bintray.com/content/mgoellnitz/maven/tangram/$VERSION/tangram/$file
  echo "."
done
