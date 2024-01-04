#!/usr/bin/env bash

sbt clean compile scalafmtAll scalastyleAll coverage Test/test it/test coverageOff coverageReport dependencyUpdates
