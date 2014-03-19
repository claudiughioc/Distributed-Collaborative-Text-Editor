#!/bin/bash

ant peer0_test &> out0 &
ant peer1_test &> out1 &
ant peer2_test %> out2 &
