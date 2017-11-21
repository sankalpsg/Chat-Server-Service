#!/bin/bash
#Running of ChatServer Java
echo Started Executing
THIS_IP=10.62.0.64
PORT=$1
cd "../../src/"
java ScalableComputing.ChatServer $PORT $THIS_IP

