#!/bin/bash

if [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "GET http://kribi.tchepannou.io/v1/application/kribi/init_artifact?version=$TRAVIS_COMMIT"
  curl -H "api_key: $KRIBI_API_KEY" http://kribi.tchepannou.io/v1/application/kribi/init_artifact?version=$TRAVIS_COMMIT

  echo
  echo "GET http://kribi.tchepannou.io/v1/application/kribi/deploy?name=kribi&environment=PROD&region=us-east-1&undeployOld=true&version=$TRAVIS_COMMIT"
  curl -H "api_key: $KRIBI_API_KEY" http://kribi.tchepannou.io/v1/application/kribi/deploy?name=kiosk-pipeline&environment=PROD&region=us-east-1&undeployOld=true&version=$TRAVIS_COMMIT
  exit $?

fi
