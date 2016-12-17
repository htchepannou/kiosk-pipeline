#!/bin/bash

APP="kiosk-pipeline"
if [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "curl http://kribi.tchepannou.io/v1/application/$APP/init_artifact?version=$TRAVIS_COMMIT"
  curl -H "api_key: $KRIBI_API_KEY" http://kribi.tchepannou.io/v1/application/$APP/init_artifact?version=$TRAVIS_COMMIT

  echo "curl http://kribi.tchepannou.io/v1/application/$APP/deploy?name=$APP&environment=PROD&region=us-east-1&undeployOld=true&version=$TRAVIS_COMMIT"
  curl -H "api_key: $KRIBI_API_KEY" http://kribi.tchepannou.io/v1/application/$APP/deploy?name=$APP&environment=PROD&region=us-east-1&undeployOld=true&version=$TRAVIS_COMMIT
  exit $?

fi
