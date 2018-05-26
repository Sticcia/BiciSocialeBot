import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class BiciSocialeBot extends TelegramLongPollingBot {
	private BikeLocation bikeLocation;
	private String bikeCombination;
	private boolean taken;
	private Logger logger;
	
	public BiciSocialeBot(String logFile, String combination) {
		this.logger = new Logger(logFile);
		this.bikeCombination = combination;
		this.bikeLocation = logger.retrieveBikeLocation();
	}
	
	@Override
	public void onUpdateReceived(Update update) {
		// Check if the update has a message
		if (update.hasMessage()) {
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
							+ "\n\n- If you move the bike please use /update, then send the location as an answer to the message."
							+ "\n\n- To find the bike use /find."
							+ "\n\n- To get the bikes' lock combination use /combination."
							+ "\n\n- Use /cancel at any moment to cancel the current action.";
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
					answer = "The bikes' current lock combination is:\n" + this.bikeCombination;
					this.sendMessage(chat_id, answer);
					break;
				case "/find":
				case "/find@BiciSocialeBot":
					if (!this.taken) {
						answer = "Lat: " + bikeLocation.getLatitude() + " Long: " + bikeLocation.getLongitude();
						InlineKeyboardMarkup markup = this.setReplyMarkup(chat_id, "Take bike", "take");
						this.sendLocation(chat_id, bikeLocation, markup);
					} else {
						answer = "Bike is already in use";
						this.sendMessage(chat_id, answer);
					}
					
					break;
				case "/cancel":
				case "/cancel@BiciSocialeBot":
					this.taken = false;
					answer = "Cancelled all current actions!";
					this.sendMessage(chat_id, answer);
					break;
				default:
					answer = "No such command.\nUse /help for help.";
					this.sendMessage(chat_id, answer);
					break;
				}
				
				logger.printMessage(user_username, Long.toString(user_id), message_text, answer);
				
			} else if (update.getMessage().hasLocation()) {
				this.bikeLocation.setLatitude(update.getMessage().getLocation().getLatitude());
				this.bikeLocation.setLongitude(update.getMessage().getLocation().getLongitude());
				logger.logLocationToFile(bikeLocation);
				
				answer = "Thanks for sending me the new location.\nUse /find to find the bike at any moment";
				this.sendMessage(chat_id, answer);
				
				logger.printLocation(user_username, Long.toString(user_id), bikeLocation, answer);
			}
		} else if (update.hasCallbackQuery()) {
			CallbackQuery query = update.getCallbackQuery();
			long chat_id = query.getMessage().getChatId();
			long message_id = query.getMessage().getMessageId();
			String username = query.getFrom().getUserName();
			
			if (query.getData().equals("take")) {
				this.taken = true;
				this.answerCallback(query.getId(), "Please don't wreck the bike!");
				InlineKeyboardMarkup markup = this.setReplyMarkup(chat_id, "Bike taken from " + username, "sorry");
				this.editMessageMarkup(chat_id, (int) (long) message_id, markup);
			} else if (query.getData().equals("sorry")) {
				this.answerCallback(query.getId(), "I'm sorry but the bike was already taken.");
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
	
	public InlineKeyboardMarkup setReplyMarkup(long chat_id, String message, String callbackData) {
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		
		rowInline.add(new InlineKeyboardButton().setText(message).setCallbackData(callbackData));
		rowsInline.add(rowInline);
		markup.setKeyboard(rowsInline);
		
		return markup;
	}
	
	public void answerCallback(String id, String message) {
		AnswerCallbackQuery s = new AnswerCallbackQuery();
		s.setCallbackQueryId(id);
		s.setText(message);
		
		try {
			execute(s);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(long chat_id, String message) {
		SendMessage s = new SendMessage()
				.setChatId(chat_id).setText(message);
		
		try {
			execute(s);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public void editMessageMarkup(long chat_id, int message_id, InlineKeyboardMarkup markup) {
		EditMessageReplyMarkup s = new EditMessageReplyMarkup()
				.setChatId(chat_id).setMessageId(message_id).setReplyMarkup(markup);
		
		try {
			execute(s);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public void sendLocation(long chat_id, BikeLocation location) {
		SendLocation s = new SendLocation().setChatId(chat_id);
		s.setLatitude(location.getLatitude());
		s.setLongitude(location.getLongitude());
		
		try {
			execute(s);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public void sendLocation(long chat_id, BikeLocation location, InlineKeyboardMarkup markup) {
		SendLocation s = new SendLocation().setChatId(chat_id).setReplyMarkup(markup);
		s.setLatitude(location.getLatitude());
		s.setLongitude(location.getLongitude());
		
		try {
			execute(s);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public BikeLocation getBikeLocation() {
		return this.bikeLocation;
	}
}
	