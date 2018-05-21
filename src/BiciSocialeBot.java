import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BiciSocialeBot extends TelegramLongPollingBot {
	private Location bikeLocation;
	private String bikeCombination;
	private boolean taken;
	private String file;
	
	public BiciSocialeBot(String logFile, String combination) {
		this.file = logFile;
		this.bikeCombination = combination;
	}
	
	@Override
	public void onUpdateReceived(Update update) {
		// Check if the update has a message
		if (update.hasMessage()) {
			String user_first_name = update.getMessage().getChat().getFirstName();
			String user_last_name = update.getMessage().getChat().getLastName();
			String user_username = update.getMessage().getChat().getUserName();
			long user_id = update.getMessage().getChat().getId();
			long chat_id = update.getMessage().getChatId();
			String message_text;
			String answer;

			// Check if update has text
			if (update.getMessage().hasText() && update.getMessage().isCommand()) {
				message_text = update.getMessage().getText();
				switch (message_text) {
				case "/start":
				case "/start@BiciSocialeBot":
					answer = "Welcome " + user_username + "!";
					this.sendMessage(chat_id, answer);
					break;
				case "/help":
				case "/help@BiciSocialeBot":
					answer = "BiciSocialeBot"
							+ "\nIf you move the bike please use /update, then send the location as an answer to the message."
							+ "\nIf you want to find the bike use /find.";
					this.sendMessage(chat_id, answer);
					break;
				case "/update":
				case "/update@BiciSocialeBot":
					this.taken = false;
					answer = "Send me the new location as an answer to this message";
					this.sendMessage(chat_id, answer);
					break;
				case "/combination":
				case "/combination@BiciSocialeBot":
					answer = "The bikes' current combination is:\n" + this.bikeCombination;
					this.sendMessage(chat_id, answer);
					break;
				case "/find":
				case "/find@BiciSocialeBot":
					if (!this.taken) {
						answer = this.sendReplyMarkup(chat_id, bikeLocation, "Take bike", "take");
					} else {
						answer = "Bike is already in use";
						this.sendMessage(chat_id, answer);
					}
					
					break;
				default:
					answer = "No such command.\nUse /help for help.";
					this.sendMessage(chat_id, answer);
					break;
				}
				
				log(user_first_name, user_last_name, Long.toString(user_id), message_text, answer);
				
			} else if (update.getMessage().hasLocation()) {
				this.bikeLocation = update.getMessage().getLocation();
				message_text = "Lat: " + bikeLocation.getLatitude().toString() + "\nLon: "
						+ bikeLocation.getLongitude().toString();
				this.logLocation(message_text);
				
				answer = "Thanks for sending me the new location.\nUse /find to find the bike at any moment";
				this.sendMessage(chat_id, answer);
				
				log(user_first_name, user_last_name, Long.toString(user_id), message_text, answer);
			}
		}
		
		if (update.hasCallbackQuery()) {
			CallbackQuery query = update.getCallbackQuery();
			if (query.getData().equals("take")) {
				this.taken = true;
				this.answerCallback(query.getId(), "Please don't wreck the bike!");
			}
		}
	}

	@Override
	public String getBotUsername() {
		// Return bot username
		// If bot username is @MyAmazingBot, it must return 'MyAmazingBot'
		return "BiciSocialeBot";
	}

	@Override
	public String getBotToken() {
		// Return bot token from BotFather
		return "577331603:AAHLutNZ7Brr98TaX4LjlagRULigAq4Vhzw";
	}
	
	public String sendReplyMarkup(long id, Location location, String button, String callbackData) {
		List<List<InlineKeyboardButton>> keyboard = Arrays.asList(Arrays.asList(new InlineKeyboardButton().setText(button).setCallbackData(callbackData)));
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
		markup.setKeyboard(keyboard);
		
		SendLocation s;
		
		if (location == null) {
			s = this.retrieveBikeLocation();
		} else {
			s = new SendLocation();
			s.setLatitude(location.getLatitude());
			s.setLongitude(location.getLongitude());
		}
		s.setChatId(id).setReplyMarkup(markup);
		
		try {
			execute(s); // Sending our message object to user
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		return "Lat: " + s.getLatitude().toString() + " Long: " + s.getLongitude().toString();
	}
	
	public void answerCallback(String id, String message) {
		AnswerCallbackQuery s = new AnswerCallbackQuery();
		s.setCallbackQueryId(id);
		s.setText(message);
		
		try {
			execute(s); // Sending our message object to user
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(long id, String message) {
		SendMessage s = new SendMessage() // Create a message object object
				.setChatId(id).setText(message);
		
		try {
			execute(s); // Sending our message object to user
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public void sendLocation(long id, Location location) {
		SendLocation s = new SendLocation().setChatId(id);
		s.setLatitude(location.getLatitude());
		s.setLongitude(location.getLongitude());
		
		try {
			execute(s); // Sending our message object to user
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public Location getBikeLocation() {
		return this.bikeLocation;
	}
	
	public SendLocation retrieveBikeLocation() {
		String currentLine;
		float lat = 0, lon = 0;
		SendLocation newLocation = new SendLocation();
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(this.file));
			while ((currentLine = in.readLine()) != null) {
				System.out.println(currentLine);
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
		
		newLocation.setLatitude(lat);
		newLocation.setLongitude(lon);
		
		return newLocation;
	}
	
	public void logLocation(String location) {
		String str = "\n----------------------------\n";
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		str += dateFormat.format(date) + "\n" + location;
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(this.file, true));
			out.append(str);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void log(String first_name, String last_name, String user_id, String txt, String bot_answer) {
		String str = "\n----------------------------\n";
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		str += dateFormat.format(date) + "\nMessage from " + first_name + " " + last_name + ". (id = " + user_id
				+ ") \n Text - " + txt + "\nBot answer: \n Text - " + bot_answer;

		System.out.println(str);
	}
}
