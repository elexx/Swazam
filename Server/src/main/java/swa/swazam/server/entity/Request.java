package swa.swazam.server.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name="request")
public class Request {

	@Id
	@Column(name="uuid")
	private UUID uuid;
	
	@Column(name="song")
	private String song;
	
	@Column(name="artist")
	private String artist;
	
	@Column(name="date")
	private Date date;
	
	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="requestor")
	private User requestor;	

	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="solver")
	private User solver;
	
	@Column(name="status", columnDefinition="TINYINT")
	private boolean status;

	public Request() {
		this.status = false;
	}

	public Request(String song, String artist, Date date, User requestor, User solver, boolean status, UUID uuid) {
		this.song = song;
		this.artist = artist;
		this.date = date;
		this.requestor = requestor;
		this.solver = solver;
		this.status = status;
		this.uuid = uuid;
	}
	
	public String getSong() {
		return song;
	}

	public void setSong(String song) {
		this.song = song;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getRequestor() {
		return requestor;
	}

	public void setRequestor(User requestor) {
		this.requestor = requestor;
	}

	public User getSolver() {
		return solver;
	}

	public void setSolver(User solver) {
		this.solver = solver;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
	public String getStatusTextRequested(){
		if(status == false)
			return "unsolved";
		
		return "solved by " + solver.getUsername();
	}
	
	public String getStatusTextSolved() {		
		return "requested by " + requestor.getUsername();
	}
	
	public String getSongText(){
		if(song != null && !song.trim().equals(""))
			return song;
		
		return "unknown";
	}
	
	public String getArtistText(){
		if(artist != null && !artist.trim().equals(""))
			return artist;
		
		return "unknown";
	}
}