#!/bin/bash

version="$1"
packageName="software.aws.solution:clickstream"
regex="[0-9]\+\.[0-9]\+\.[0-9]\+"

sed -i "s/${packageName}:${regex}/${packageName}:${version}/g" README.md
sed -i "s/VERSION_NAME=${regex}/VERSION_NAME=${version}/g" gradle.properties
