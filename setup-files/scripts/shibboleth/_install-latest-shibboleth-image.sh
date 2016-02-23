#!/usr/bin/env bash

filename=$(./_get-latest-shibboleth-image-filename.sh)

cd $(./_get-shibboleth-install-location.sh)

if [ -z "$filename" ]; then

  echo "Unable to find Shibboleth Docker image file."
  exit 1

fi


docker load < $filename