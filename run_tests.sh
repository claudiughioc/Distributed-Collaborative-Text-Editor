#!/bin/bash

ant build

ant peer0_test &> out0 &
sleep 0.1
ant peer1_test &> out1 &
sleep 0.1
ant peer2_test %> out2 &
sleep 0.1
ant peer3_test %> out3 &
sleep 0.1
ant peer4_test %> out4 &
