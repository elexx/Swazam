#!/bin/bash

# Test suite for our SWAzam project
#
# This suite performs several test of our components (server, peers, clients).
#
# Usage: test.sh [<root-directory>] [cleanup]
#
# <root-directory> denotes the directory of the project root. It has to contains
#                  directories like Client, Sever, Peer, and so on.
#
# cleanup          when selected, a cleanup is performed, exiting all started
#                  server, peer and client components, if any.
#


PEER_COUNT=5

function start_peer() {
	(cd "$PEER_DIR" ; screen -dmS peer$1 -t java /bin/bash -c "java -jar target/peer-0.0.1.jar --config '$TEST_WORKING_DIR/peer$1/peer.properties' >'$TEST_WORKING_DIR/peer$1.out' 2>&1" )
}

function send() {
	bash -c "screen -S "$1" -p java -X eval 'stuff \"$2\"\015'"
}

function check_screen() {
	if [ ! 0 -eq $(screen -q -ls $1 | grep $1 | wc -l) ] ; then print_red "There is a screen session named \"$1\" already in use. The test suite won't work in this situation." ; exit ; fi
}

function check_ps() {
	psc=$(ps -ef | grep $1 | grep -v grep | wc -l)
	if [ ! 0 -eq $psc ] ; then print_red "There are one or more processes matching \"$1\". The test suite won't work in this situation." ; exit ; fi
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
		print_red "Server is probably running (ports 8080 or 9090 are in use) - aborting"
		exit
	fi

	check_ps server-0.0.1.jar
	check_ps peer-0.0.1.jar

	check_screen server
	for i in $(seq $PEER_COUNT) ; do check_screen peer$i ; done
}

function cleanup() {
	screen -wipe >/dev/null 2>&1

	print_heading "ENVIRONMENT CLEANUP"

	echo "Killing left-over servers..."
	quit_ps server-0.0.1.jar 2>/dev/null
	quit_screen server 2>/dev/null

	echo "Killing left-over peers..."
	quit_ps peer-0.0.1.jar 2>/dev/null
	for i in $(seq $PEER_COUNT) ; do quit_screen peer$i 2>/dev/null ; done

	if [ -d "$TEST_WORKING_DIR" ]
	then
		echo "Cleaning up working directory..."
		rm -rf "$TEST_WORKING_DIR"
	else
		echo "Working directory ($TEST_WORKING_DIR) is not a directory (not deleted)"
	fi

	echo "Checking for clean environment..."
	check_clean
	echo "Done."

	screen -wipe >/dev/null 2>&1
}

function wait_for_output() {
	wait_for_output_timeout "$1" "$2" 60 "$3"
}

function wait_for_output_timeout() {
	absolute_wait=${4:-"no"}
	prev_count=$(cat "$1" | grep "$2" | wc -l)
	timeout=$3

	#echo "[debug] waiting for \"$1\" at \"$2\" timeout \"$timeout\" absolute \"

	if [ $absolute_wait != "no" ] ; then prev_count=0 ; fi

	while [ $prev_count -eq $(cat "$1" | grep "$2" | wc -l) ]
	do
		sleep 1 ; echo -n "."

		timeout=$(( timeout - 1 ))
		if [ 0 -eq $timeout ]
		then
			print_red " Timeout, something went wrong. Please consider calling \"$0 cleanup\"."
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

function print_bold() {
	tput bold
	echo "$1"
	tput sgr0
}

function print_green() {
	tput setaf 2
	print_bold "$1"
}

function print_red() {
	tput setaf 1
	print_bold "$1"
}

function print_yellow() {
	tput setaf 3
	print_bold "$1"
}

function print_heading() {
	echo
	echo $(print_bold "========== $1 ==========")
}

# ########################### CONFIGURATION ###########################

ROOT_DIR="${1:-..}"
if [ "x$ROOT_DIR" == "xcleanup" ] ; then ROOT_DIR=".." ; fi
ROOT_DIR="$(realpath $ROOT_DIR)"
TESTSUITE_DIR="$ROOT_DIR/Testsuite"
TEST_WORKING_DIR="$TESTSUITE_DIR/workingdir"
TEST_DATA_DIR="$TESTSUITE_DIR/data"

if [ \( "x$1" == "xcleanup" \) -a \( -z "$2" \) ] ; then cleanup ; exit ; fi
if [ \( -d "$1" \) -a \( "x$2" == "xcleanup" \) ] ; then cleanup ; exit ; fi

if [ ! -d "$ROOT_DIR" ] ; then print_red "ROOT_DIR ($ROOT_DIR) is not an existing directory!" ; exit ; fi


SERVER_DIR="$ROOT_DIR/Server"
PEER_DIR="$ROOT_DIR/Peer"
CLIENT_DIR="$ROOT_DIR/Client"



# ########################### PRE-CHECKS ###########################

screen -wipe >/dev/null 2>&1

if [ ! -d "$TESTSUITE_DIR" ] ; then print_red "TESTSUITE_DIR ($TESTSUITE_DIR) is not an existing directory!" ; exit ; fi
if [ ! -d "$SERVER_DIR" ] ; then print_red "SERVER_DIR ($SERVER_DIR) is not an existing directory!" ; exit ; fi
if [ ! -d "$PEER_DIR" ] ; then print_red "PEER_DIR ($PEER_DIR) is not an existing directory!" ; exit ; fi
if [ ! -d "$CLIENT_DIR" ] ; then print_red "PEER_DIR ($PEER_DIR) is not an existing directory!" ; exit ; fi
if [ ! -d "$(dirname '$TEST_WORKING_DIR')" ] ; then print_red "Parent of TEST_WORKING_DIR (parent of $TEST_WORKING_DIR) is not an existing directory!" ; exit ; fi
if [ ! -d "$TEST_DATA_DIR" ] ; then print_red "TEST_DATA_DIR ($TEST_DATA_DIR) is not an existing directory!" ; exit ; fi

for i in $(seq $PEER_COUNT)
do
	if [ ! -d "$TEST_DATA_DIR"/Peer$i ] ; then print_red "Peer $i needs a music directory ($TEST_DATA_DIR/Peer$i)!" ; exit ; fi
	ls "$TEST_DATA_DIR"/Peer$i/*.mp3 >/dev/null 2>&1
	if [ $? -ne 0 ] ; then print_red "Peer $i needs MP3 files in its music directory ($TEST_DATA_DIR/Peer$i/*.mp3)!" ; exit ; fi
done

if [ ! -d "$TEST_DATA_DIR"/Client ] ; then print_red "Client needs a music directory ($TEST_DATA_DIR/Client)!" ; exit ; fi
ls "$TEST_DATA_DIR"/Client/*.mp3 >/dev/null 2>&1
if [ $? -ne 0 ] ; then print_red "Client needs MP3 files in its music directory ($TEST_DATA_DIR/Client/*.mp3)!" ; exit ; fi

check_clean

# ########################### TEST RUN ###########################

print_heading "BUILDING MAVEN PROJECT"
( cd "$ROOT_DIR" ; mvn package -Dmaven.test.skip=true )

print_heading "STARTING UP TEST ENVIRONMENT"
echo "[testsuite] Starting with ROOTDIR [$ROOT_DIR]"

mkdir -p "$TEST_WORKING_DIR"

echo -n "[server] Starting..."
echo "" > "$TEST_WORKING_DIR"/server.out
(cd "$SERVER_DIR" ; screen -dmS server -t java /bin/bash -c "java -jar target/server-0.0.1.jar >'$TEST_WORKING_DIR/server.out' 2>&1" )

wait_for_output "$TEST_WORKING_DIR"/server.out "to exit"
print_green " started."

echo -n "[testsuite] Creating peer directories... "
for i in $(seq $PEER_COUNT)
do
	echo -n "$i "
	confpath="$TEST_WORKING_DIR"/peer$i/peer.properties

	mkdir -p "$TEST_WORKING_DIR"/peer$i/storage
	mkdir -p "$TEST_WORKING_DIR"/peer$i/music

	if [ -f "$TEST_DATA_DIR"/tags_$i ]
	then
		cp "$TEST_DATA_DIR"/tags_$i "$TEST_WORKING_DIR"/peer$i/storage/tags
	fi

	echo "" > "$confpath"
	echo "credentials.user=chrissi" >> "$confpath"
	echo "credentials.pass=chrissi" >> "$confpath"
	echo "music.root=$TEST_WORKING_DIR/peer$i/music" >> "$confpath"
	echo "storage.root=$TEST_WORKING_DIR/peer$i/storage" >> "$confpath"
	echo "server.hostname=localhost" >> "$confpath"
	echo "server.port=9090" >> "$confpath"

	echo "" > "$TEST_WORKING_DIR"/peer$i.out
done
print_green "done."

for i in $(seq $PEER_COUNT)
do
	echo -n "[peer $i] Starting..."
	start_peer $i
	wait_for_output "$TEST_WORKING_DIR"/peer$i.out "to exit"
	print_green " done."
done

print_heading "STARTING PEER TESTS"

for i in $(seq $PEER_COUNT)
do
	echo -n "[peer $i] Copying mp3 files..."
	cp "$TEST_DATA_DIR"/Peer$i/*.mp3 "$TEST_WORKING_DIR"/peer$i/music
	print_green " done."
done

for i in $(seq $PEER_COUNT)
do
	echo -n "[peer $i] Waiting for all tags..."
	for f in "$TEST_WORKING_DIR"/peer$i/music/*.mp3
	do
		wait_for_output_timeout "$TEST_WORKING_DIR"/peer$i.out "$f generated" 600000 "alex"
	done
	print_green " done."
done

print_heading "STARTING CLIENT TESTS"

mkdir "$TEST_WORKING_DIR"/client
confpath="$TEST_WORKING_DIR"/client/client.properties
echo "" > "$confpath"
echo "credentials.user=chrissi" >> "$confpath"
echo "credentials.pass=chrissi" >> "$confpath"
echo "server.hostname=localhost" >> "$confpath"
echo "server.port=9090" >> "$confpath"

client=1
for sample in "$TEST_DATA_DIR"/Client/*.mp3
do
	echo $sample | grep "fail" >/dev/null 2>&1
	shouldfail=$?
	if [ $shouldfail -eq 0 ] ; then should="fail" ; else should="succeed" ; fi
	echo -n "[client $client] Testing snippet "$(basename $sample)" (should $should)..."

	echo "" > "$TEST_WORKING_DIR"/client$client.out
	(cd "$CLIENT_DIR" ; java -jar target/client-0.0.1.jar --test --sample "$sample" --config "$confpath" > "$TEST_WORKING_DIR"/client$client.out 2>&1 )
	retval=$?
	retfile="$TEST_WORKING_DIR/client$client.out"
	songname=$(cat "$retfile" | awk 'NR==1')
	songartist=$(cat "$retfile" | awk 'NR==2')

	if [ $retval -eq 0 ]
	then
		if [ $shouldfail -ne 0 ]
		then
			print_green " success ($songname)"
		else
			print_red " success (should have failed)"
		fi
	elif [ $retval -eq 1 ]
	then
		if [ $shouldfail -ne 0 ]
		then
			print_red " not found"
		else
			print_green " not found (as planned)"
		fi
	else
		print_red " failed (retcode $retval)"
	fi

	sleep 5

	client=$(( client + 1 ))
done

print_heading "END OF AUTOMATIC TESTS"
echo "[testsuite] Automatic tests passed. The P2P suite is now ready for you to test it further, if needed."
echo "[testsuite] When finished, press any key to gracefully shutdown test suite."
read -n1 -r

print_heading "STOPPING TEST ENVIRONMENT"

for i in $(seq $PEER_COUNT)
do
	echo -n "[peer $i] Stopping..."

	if [ -f "$TEST_WORKING_DIR"/peer$i/storage/tags ]
	then
		cp "$TEST_WORKING_DIR"/peer$i/storage/tags "$TEST_DATA_DIR"/tags_$i
	fi

	send peer$i quit
	wait_for_screen_end peer$i
	print_green " done."
done

echo -n "[server] Quitting..."
send server quit
wait_for_screen_end server
print_green " done."

sleep 1

echo -n "[testsuite] Cleaning up working directory..."
rm -rf "$TEST_WORKING_DIR"
print_green " done."

sleep 1

echo -n "[testsuite] Checking for clean environment..."
check_clean
print_green " done."

echo -n "[testsuite] " && print_green "Test suite finished regularly."

