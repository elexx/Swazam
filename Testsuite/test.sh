#!/bin/bash

PEER_COUNT=3

function start_peer() {
	(cd $PEER_DIR ; screen -dmS peer$1 -t java /bin/bash -c "java -jar target/peer-0.0.1.jar --config $TEST_WORKING_DIR/peer$1/peer.properties >$TEST_WORKING_DIR/peer$1.out 2>&1" )
}

function send() {
	bash -c "screen -S "$1" -p java -X eval 'stuff \"$2\"\015'"
}

function check_screen() {
	if [ ! 0 -eq $(screen -q -ls $1 | grep $1 | wc -l) ] ; then echo "There is a screen session named \"$1\" already in use. The test suite won't work in this situation." ; exit ; fi
}

function check_ps() {
	psc=$(ps -ef | grep $1 | grep -v grep | wc -l)
	if [ ! 0 -eq $psc ] ; then echo "There are one or more processes matching \"$1\". The test suite won't work in this situation." ; exit ; fi
}

function quit_screen() {
	if [ ! 0 -eq $(screen -q -ls $1 | grep $1 | wc -l) ] ; then screen -S "$1" -p java -X quit exit ; fi
}

function quit_ps() {
	ps -ef | grep $1 | grep -v grep | awk '{ print $2 }' | xargs kill -9
}

function check_clean() {
	if [ ! \( 0 -eq $(netstat -ant | grep 9090 | grep LISTEN | wc -l) -a 0 -eq $(netstat -ant | grep 8080 | grep LISTEN | wc -l) \) ]
	then
		echo "Server is probably running (ports 8080 or 9090 are in use) - aborting"
		exit
	fi

	check_ps server-0.0.1.jar
	check_ps peer-0.0.1.jar

	check_screen server
	for i in $(seq $PEER_COUNT) ; do check_screen peer$i ; done
}

function cleanup() {
	screen -wipe >/dev/null 2>&1

	echo "Killing left-over servers..."
	quit_ps server-0.0.1.jar 2>/dev/null
	quit_screen server 2>/dev/null

	echo "Killing left-over peers..."
	quit_ps peer-0.0.1.jar 2>/dev/null
	for i in $(seq $PEER_COUNT) ; do quit_screen peer$i 2>/dev/null ; done

	echo "Checking for clean environment..."
	check_clean
	echo "Done."

	screen -wipe >/dev/null 2>&1
}

function wait_for_output() {
	timeout=30
	while [ 0 -eq $(cat "$1" | grep "$2" | wc -l) ]
	do
		sleep 1 ; echo -n "."

		timeout=$(( timeout - 1 ))
		if [ 0 -eq $timeout ]
		then
			echo "Timeout, something went wrong. Please consider calling \"$0 cleanup\"."
			exit
		fi
	done
}

function wait_for_screen_end() {
	while [ ! 0 -eq $(screen -q -ls $1 | grep $1 | wc -l ) ] ; do sleep 1 ; echo -n "." ; done
}

function realpath() {
	( cd "$1" 2>/dev/null || return $? ; echo "$(pwd -P)"; )
}

# ########################### CONFIGURATION ###########################

ROOT_DIR=${1:-..}

if [ "x"$1 == "xcleanup" ] ; then cleanup ; exit ; fi

if [ ! -d $ROOT_DIR ] ; then echo "ROOT_DIR ($ROOT_DIR) is not an existing directory!" ; exit ; fi

ROOT_DIR=$(realpath $ROOT_DIR)

TESTSUITE_DIR=$ROOT_DIR/Testsuite

SERVER_DIR=$ROOT_DIR/Server
PEER_DIR=$ROOT_DIR/Peer

TEST_WORKING_DIR=$TESTSUITE_DIR/workingdir
TEST_DATA_DIR=$TESTSUITE_DIR/data


# ########################### PRE-CHECKS ###########################

screen -wipe >/dev/null 2>&1

if [ ! -d $TESTSUITE_DIR ] ; then echo "TESTSUITE_DIR ($TESTSUITE_DIR) is not an existing directory!" ; exit ; fi
if [ ! -d $SERVER_DIR ] ; then echo "SERVER_DIR ($SERVER_DIR) is not an existing directory!" ; exit ; fi
if [ ! -d $SERVER_DIR ] ; then echo "PEER_DIR ($PEER_DIR) is not an existing directory!" ; exit ; fi
if [ ! -d $(dirname $TEST_WORKING_DIR) ] ; then echo "Parent of TEST_WORKING_DIR (parent of $TEST_WORKING_DIR) is not an existing directory!" ; exit ; fi

check_clean

# ########################### TEST RUN ###########################

echo "[testsuite] Starting with ROOTDIR [$ROOT_DIR]"

mkdir -p $TEST_WORKING_DIR

echo -n "[server] Starting..."
echo blank > $TEST_WORKING_DIR/server.out
(cd $SERVER_DIR ; screen -dmS server -t java /bin/bash -c "java -jar target/server-0.0.1.jar >$TEST_WORKING_DIR/server.out 2>&1" )

wait_for_output $TEST_WORKING_DIR/server.out "to exit"
echo " started."

echo -n "[testsuite] Creating peer directories... "
for i in $(seq $PEER_COUNT)
do
	echo -n "(peer $i) "
	confpath=$TEST_WORKING_DIR/peer$i/peer.properties

	mkdir -p $TEST_WORKING_DIR/peer$i/storage
	mkdir -p $TEST_WORKING_DIR/peer$i/music

	echo "" > $confpath
	echo "credentials.user=chrissi" >> $confpath
	echo "credentials.pass=chrissi" >> $confpath
	echo "music.root=$TEST_WORKING_DIR/peer$i/music" >> $confpath
	echo "storage.root=$TEST_WORKING_DIR/peer$i/data" >> $confpath
	echo "server.hostname=localhost" >> $confpath
	echo "server.port=9090" >> $confpath

	echo blank > $TEST_WORKING_DIR/peer$i.out
done
echo "done."

for i in $(seq $PEER_COUNT)
do
	echo -n "[peer $i] Starting..."
	start_peer $i
	wait_for_output $TEST_WORKING_DIR/peer$i.out "to exit"
	echo " done."
done

echo -n "[peer 1] Adding song, waiting for tag..."
cp $TEST_DATA_DIR/01.mp3 $TEST_WORKING_DIR/peer1/music
wait_for_output $TEST_WORKING_DIR/peer1.out "01.mp3 generated"
echo " done."

echo "now i would make some peer checks (pending)"
sleep 5
#send peer0 "add file1.wav"

for i in $(seq $PEER_COUNT)
do
	echo -n "[peer $i] Stopping..."
	send peer$i quit
	wait_for_screen_end peer$i
	echo " done."
done

echo -n "[server] Quitting..."
send server quit
wait_for_screen_end server
echo " done."

echo "[testsuite] Cleaning up working directories..."
rm -rf $TEST_WORKING_DIR

sleep 2

echo "[testsuite] Checking for clean environment..."
check_clean
echo "[testsuite] Done."
