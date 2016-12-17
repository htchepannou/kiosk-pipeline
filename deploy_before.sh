#!/bin/bash

if [ "$TRAVIS_BRANCH" == "master" ]; then
  mkdir target/deploy
  cp target/kiosk-pipeline.jar target/deploy/.
fi
