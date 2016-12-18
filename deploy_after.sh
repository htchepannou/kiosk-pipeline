#!/bin/bash

APP="kiosk-pipeline"
if [ "$TRAVIS_BRANCH" == "master" ]; then
  ARTIFACT_URL="http://kribi.tchepannou.io/v1/application/$APP/init_artifact?version=$TRAVIS_COMMIT"
  curl -v -H "api_key: $KRIBI_API_KEY" $ARTIFACT_URL

  echo
  echo
  DEPLOY_URL="http://kribi.tchepannou.io/v1/application/$APP/deploy?environment=PROD&region=us-east-1&release=true&undeployOld=true&version=$TRAVIS_COMMIT"
  curl -v -H "api_key: $KRIBI_API_KEY" -m 600 --connect-timeout 120 $DEPLOY_URL
  exit $?

fi
