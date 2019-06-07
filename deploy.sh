#!/usr/bin/env bash
version=$(cat version.sbt | sed -n -e '/version/ s/.*\= "*//p'| sed -n -e 's/"//p')
echo "Deploying version: "$version
sbt "project server" "deploySsh scalajs-converter"

