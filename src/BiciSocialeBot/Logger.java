package BiciSocialeBot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	
	private String file;

	public Logger(String logFile) {
		this.file = logFile;
	}
	
	public BikeLocation retrieveBikeLocation() {
		String currentLine;
		float lat = 0, lon = 0;
		BikeLocation location = new BikeLocation();
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(this.file));
			while ((currentLine = in.readLine()) != null) {
				if (currentLine.contains("Lat")) {
					lat = Float.parseFloat(currentLine.substring(5));
				} else if (currentLine.contains("Lon")) {
					lon = Float.parseFloat(currentLine.substring(5));
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		location.setLatitude(lat);
		location.setLongitude(lon);
		System.out.println("\nRetrieved the following location from the file: " + this.file + "\n Lat: " + lat + "\n Lon: " + lon);
		
		return location;
	}
	
	public void logLocationToFile(BikeLocation location) {
		String str = "\n----------------------------\n";
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		str += dateFormat.format(date) + "\nLat: " + location.getLatitude() + "\nLon: " + location.getLongitude();
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(this.file, true));
			out.append(str);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void printMessage(String user_name, String user_id, String txt, String bot_answer) {
		String str = "\n----------------------------\n";
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		str += dateFormat.format(date) + "\nMessage from " + user_name + ". (id = " + user_id
				+ ") \n Command: " + txt + "\nBot answer: \n Text: " + bot_answer;

		System.out.println(str);
	}
	
	public void printLocation(String user_name, String user_id, BikeLocation location, String bot_answer) {
		String str = "\n----------------------------\n";
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		str += dateFormat.format(date) + "\nLocation from " + user_name + ". (id = " + user_id
				+ ") \n Lat: " + location.getLatitude() + "\n Lon: " + location.getLongitude() + "\nBot answer: \n Text: " + bot_answer;
		
		System.out.println(str);
	}
}
