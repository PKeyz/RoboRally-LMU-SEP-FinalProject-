package bb.roborally.protocol;


/**
 * @author Philipp Keyzman
 * @author Veronika Heckel
 */
public enum Orientation {

	LEFT("left"),
	RIGHT("right"),
	TOP("top"),
	BOTTOM("bottom"),
	CLOCKWISE("clockwise"),
	COUNTERCLOCKWISE("counterclockwise");

	public final String orientation;
	Orientation(final String orientation){
		this.orientation = orientation;
	}
	@Override
	public String toString(){
		return orientation;
	}

	public static Orientation toOrientation(String orientation){
		for(Orientation o: values()){
			if(o.toString().equals(orientation))
				return o;
		}
		throw new IllegalArgumentException();
	}
}