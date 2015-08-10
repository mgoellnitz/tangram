#!/bin/sh
GAE=$1

if [ -z "$GAE" ] ; then
  exit
fi

OUTPUT=`basename $GAE .xml`-new.xml

echo $OUTPUT

sed -e 's/<value>//g' $GAE | sed -e 's/<\/value>//g' \
|sed -e 's/<bytes>//g' | sed -e 's/<\/bytes>//g' \
|sed -e 's/<contentIds.*<\/contentIds>//g' \
|sed -e 's/<inGroupIds.*<\/inGroupIds>//g' \
|sed -e 's/<thumbnailId>.*<\/thumbnailId>//g' \
> $OUTPUT
