#!/bin/bash
set -e
sbt ++$TRAVIS_SCALA_VERSION test unidoc
sbt ++$TRAVIS_SCALA_VERSION 'project core' assembly
