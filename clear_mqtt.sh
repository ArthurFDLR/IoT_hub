#!/bin/sh
#recquiered package: mosquitto-clients 
echo "cleaning retained MQTT messages on localhost"
timeout 1s mosquitto_sub -h localhost -t "#" -v -R | while read line; do mosquitto_pub -h localhost -t "${line% *}" -r -n; done