#!/bin/bash

if [ $# -lt 1 ]; then
	echo "Usage $0 nr_peers"
	exit
fi

NR_PEERS=$1
ANT_CMD="ant peer"
EXT="test"
SEP="_"

ant build

for (( i=0; i<$NR_PEERS; i++)); do
	# Build the command
	CMD="$ANT_CMD$i$SEP$EXT"


	# Run the peer and redirect the output
	echo "Running $CMD"
	$CMD &> "out$i" &
done

echo "Waiting for test to finish"
sleep 15
echo "Test finished, gathering statistics..."

rm -f traffic.dat
for (( i=0; i<$NR_PEERS; i++)); do
	sent=`cat "out$i" | grep Traffic | cut -d ':' -f 2 | cut -d ' ' -f 1`
	received=`cat "out$i" | grep Traffic | cut -d ':' -f 2 | cut -d ' ' -f 2`
	echo "$((i * 2)) sent$i $sent" >> traffic.dat
	echo "$((i * 2 + 1)) received$i $received" >> traffic.dat
done

echo "Showing traffic statistics"
gnuplot traffic.plt
feh traffic.png
