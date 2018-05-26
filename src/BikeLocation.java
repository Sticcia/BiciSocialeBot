public class BikeLocation {
	
	private Float latitude;
	private Float longitude;

	public void setLatitude(float lat) {
		this.latitude = lat;
	}
	
	public void setLongitude(float lon) {
		this.longitude = lon;
	}
	
	public Float getLatitude() {
		return this.latitude;
	}
	
	public Float getLongitude() {
		return this.longitude;
	}
	
	public String toString() {
		return "Location{" +
				"longitude=" + longitude +
				", latitude=" + latitude +
				'}';
	}
}
