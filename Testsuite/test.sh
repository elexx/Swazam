#!/bin/bash

ROOT_DIR=/home/michael/uni/swa/Swazam
PEER_COUNT=5

function start_peer() {

	(cd $ROOT_DIR/Peer ; screen -dmS peer$1 java -jar target/peer-0.0.1.jar --config $ROOT_DIR/Testsuite/peerdata/peer$1/peer.properties )
	
}

function send() {
	screen -S "$1" -X "$2" 
}


echo -n "[server] Starting..."
(cd $ROOT_DIR/Server ; screen -dmS server java -jar target/server-0.0.1.jar)

while [ 0 -eq `netstat -ant | grep 9090 | grep LISTEN | wc -l` ]
do
	sleep 1
	echo -n "."
done
echo " started."

echo "[testsuite] Creating peer directories..."
for i in $(seq $PEER_COUNT)
do
	confpath=$ROOT_DIR/Testsuite/peerdata/peer$i/peer.properties

	mkdir -p $ROOT_DIR/Testsuite/peerdata/peer$i/storage
	mkdir -p $ROOT_DIR/Testsuite/peerdata/peer$i/music
	
	echo "" > $confpath
	echo "credentials.user=chrissi" >> $confpath
	echo "credentials.pass=chrissi" >> $confpath
	echo "music.root=$ROOT_DIR/Testsuite/peerdata/peer$1/music" >> $confpath
	echo "storage.root=$ROOT_DIR/Testsuite/peerdata/peer$1/data" >> $confpath
	echo "server.hostname=localhost" >> $confpath
	echo "server.port=9090" >> $confpath
done

for i in $(seq $PEER_COUNT)
do
	echo -n "[peer $i] Starting... "
	start_peer $i
	echo "done."
done

echo "now i would make some peer checks (pending)"
sleep 5
#send peer0 "add file1.wav"

for i in $(seq $PEER_COUNT)
do
	echo -n "[peer $i] Stopping... "
	send peer$i quit
	echo "done."
done

echo "[server] Quitting..."
send server quit

echo "[testsuite] Cleaning up peer directories..."
rm -rf $ROOT_DIR/Testsuite/peerdata

echo "[testsuite] Done."

