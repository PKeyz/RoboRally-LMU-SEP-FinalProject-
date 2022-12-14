package bb.roborally.protocol.map;

import bb.roborally.protocol.Message;
import bb.roborally.protocol.Envelope;

/**
 * @author  Philipp Keyzman
 */

public class SelectMap implements Message {
	private String[] availableMaps;

	public SelectMap(){

	}

	public SelectMap(String[] availableMaps){
		this.availableMaps = availableMaps;
	}

	public String[] getAvailableMaps() {
		return availableMaps;
	}

	public void setAvailableMaps(String[] availableMaps) {
		this.availableMaps = availableMaps;
	}

	@Override
	public String toJson() {
		return toEnvelope().toJson();
	}

	@Override
	public Envelope toEnvelope() {
		return new Envelope(Envelope.MessageType.SELECT_MAP,this);
	}
}