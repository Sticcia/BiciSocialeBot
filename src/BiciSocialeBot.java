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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BiciSocialeBot extends TelegramLongPollingBot {
	private Location bikeLocation;
	private boolean taken;
	
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
					answer = "Send me the new location as an answer to this message";
					this.sendMessage(chat_id, answer);
					break;
				case "/find":
				case "/find@BiciSocialeBot":
					if (getBikeLocation() != null) {
						answer = "Lat: " + bikeLocation.getLatitude().toString() + " Long: "
								+ bikeLocation.getLongitude().toString();
						this.sendReplyMarkup(chat_id, bikeLocation, "Take bike", "take");
					} else if (this.taken) {
						answer = "Bike is already in use";
						this.sendMessage(chat_id, answer);
					} else {
						answer = "The location of the bike is missing";
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
				message_text = "Lat: " + bikeLocation.getLatitude().toString() + " Long: "
						+ bikeLocation.getLongitude().toString();
				
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
	
	public Location getBikeLocation() {
		return this.bikeLocation;
	}
	
	public void sendReplyMarkup(long id, Location location, String button, String callbackData) {
		List<List<InlineKeyboardButton>> keyboard = Arrays.asList(Arrays.asList(new InlineKeyboardButton().setText(button).setCallbackData(callbackData)));
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
		markup.setKeyboard(keyboard);
		SendLocation s = new SendLocation().setChatId(id);
		s.setLatitude(location.getLatitude());
		s.setLongitude(location.getLongitude()).setReplyMarkup(markup);
		
		try {
			execute(s); // Sending our message object to user
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
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

	private void log(String first_name, String last_name, String user_id, String txt, String bot_answer) {
		System.out.println("\n ----------------------------");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		System.out
				.println("Message from " + first_name + " " + last_name + ". (id = " + user_id + ") \n Text - " + txt);
		System.out.println("Bot answer: \n Text - " + bot_answer);
	}
}
