#!/bin/bash

#script to run with JBoss 7.1 in the deployments directory to redeploy the app

for i in {1..100}
do
  echo "Reploy number: $i"
  rm accumulo-1858-test.war.deployed
  sleep 10
  touch accumulo-1858-test.war.dodeploy
  sleep 5
done