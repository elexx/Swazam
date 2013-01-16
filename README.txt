README

Necessary tools for running the TestSuite: bash, screen, Java SE 7, Maven (version 2.2.1 at least)


1. TestSuite

*) Please copy your peer mp3 files to Swazam/Testsuite/data/<PeerX>/*.mp3
where PeerX is the folder that contains the music database for peer X.

*) Please copy the mp3 snippets for the client's search to Swazam/Testsuite/data/Client/*.mp3
 
*) In terminal: change to Swazam/Testsuite and execute ./test.sh
The TestSuite starts 5 peers, the server and a client and adds mp3s to the peers directories.

*) The TestSuite halts after executing all automatic tests. 
For further testing, you can now manually start the Client GUI by changing to Swazam/Client and executing:
java -jar target/client-0.0.1.jar 0 g

*) If you like to have a look at the commandline outputs of the server/peers/client you can now switch to 
Swazam/Testsuite/workingdir/<peerX>.out|server.out
These files will be deleted after successful shutdown!!


2. Webserver

The TestSuite also starts the Jetty Server, where the Web Application is deployed. 
The Web Application is available at 

http://localhost:8080/Login.xhtml

The user which is used for the TestSuite is (you may also use this user to login on the web):

Username: chrissi
Passwort: chrissi


