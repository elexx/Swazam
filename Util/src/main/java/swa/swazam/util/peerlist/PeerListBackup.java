package swa.swazam.util.peerlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import swa.swazam.util.exceptions.SwazamException;

public class PeerListBackup {
	private final String storagePath;
	private final String fileName = "knownPeers.json";
	
	/**
	 * Creates a new instance of PersistenceStorage with the given path.
	 * @param storagePath the path were the json file should be stored
	 */
	public PeerListBackup(String storagePath) {
		if(!(storagePath.lastIndexOf("/") == storagePath.length()-1))
			storagePath += "/";
		this.storagePath = storagePath;
	}
	
	/**
	 * Serializes the given Peerlist and stores the peer information to the file system. The file
	 * is placed on the defined path having the defined file name. 
	 * @param peers the list containing the peers that should be serialized
	 * @throws SwazamException in case the storage is unsuccessful.
	 */
	public void storePeers(PeerList<InetSocketAddress> peers) throws SwazamException {
		Gson gson = new Gson();
		Type token = new TypeToken<PeerList<InetSocketAddress>>(){}.getType();
		String json = gson.toJson(peers, token);
		
		try {
			FileWriter writer = new FileWriter(storagePath + fileName);
			writer.write(json);
			writer.close();
		} catch(IOException e) {
			throw new SwazamException("Could not store peer information to file system. Cause was: " + e.getMessage());
		}
	}
	
	/**
	 * Deserializes the stored peer information from the file system and creates a list containing all known peers.
	 * @return a list containing all known peers.
	 * @throws SwazamException in case loading the peer information from the file system was unsuccessful. 
	 */
	public ArrayPeerList<InetSocketAddress> loadPeers() throws SwazamException {
		Gson gson = new Gson();
		Type token = new TypeToken<ArrayPeerList<InetSocketAddress>>(){}.getType();
		
		try {
			File f = new File(storagePath + fileName);
			if(f.exists()){
			    BufferedReader br = new BufferedReader(new FileReader(storagePath + fileName));
			    ArrayPeerList<InetSocketAddress> list = gson.fromJson(br, token);
			    return list;
			}
			return new ArrayPeerList<InetSocketAddress>();
			
		} catch (FileNotFoundException e) {
			throw new SwazamException("Could not load peer information from file system. Cause was: " + e.getMessage());
		}
	}

}
