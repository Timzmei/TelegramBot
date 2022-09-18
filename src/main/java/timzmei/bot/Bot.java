package timzmei.bot;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import timzmei.bot.hh.HeadHanter;
import timzmei.bot.hh.Vacancies;
import timzmei.bot.post.TrackingPost;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Bot extends TelegramLongPollingBot {

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {


        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {

                String messageText = message.getText();

                if (messageText.toLowerCase().contains("какая погода")) {
                    execute(SendMessage.builder().chatId(message.getChatId().toString()).text(new Weather(54.775002, 56.037498).getWeather("Уфе")).build());

                } else if (messageText.toLowerCase().contains("работа") || messageText.toLowerCase().contains("работу")) {
                    execute(SendMessage.builder().chatId(message.getChatId().toString()).text(getHHRequest()).build());

                } else if (messageText.toLowerCase().contains("когда") || messageText.toLowerCase().contains("зачем") || messageText.toLowerCase().contains("почему") || messageText.toLowerCase().contains("что") || messageText.toLowerCase().contains("кто")) {
                    if (messageText.toLowerCase().contains("?")) {
                        execute(SendMessage.builder().chatId(message.getChatId().toString()).text("хороший вопрос...").replyToMessageId(message.getMessageId()).build());
                    }

                } else if (messageText.toLowerCase().contains("привет") || messageText.toLowerCase().contains("здравствуйте")) {
                    execute(SendMessage.builder().chatId(message.getChatId().toString()).text("Привет, " + message.getFrom().getFirstName() + "!").replyToMessageId(message.getMessageId()).build());

                } else if (messageText.toLowerCase().contains("почта") || messageText.toLowerCase().contains("почту")) {
                    String barCode = messageText.toLowerCase().replaceAll("почта", "").replaceAll("почту", "").replaceAll("Почта", "").replaceAll("Почту", "").trim();

                    execute(SendMessage.builder().chatId(message.getChatId().toString()).text(new TrackingPost(barCode).start()).replyToMessageId(message.getMessageId()).build());
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "@TimZmeiBot";
    }

    @Override
    public String getBotToken() {
        return "1031731840:AAFUpNX2-XwW4OQziSRZNCuXk0q7_0u6pqE";
    }

    @SneakyThrows
    public static void main(String[] args) {
        Bot bot = new Bot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

    private String getHHRequest() {
        String textRequest = new String();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest requestHttp = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hh.ru/vacancies?area=99&specialization=1.295&industry=9&order_by=salary_desc"))
                .build();
        try {
            HttpResponse<String> response = client.send(requestHttp, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            HeadHanter hh = new Gson().fromJson(body, HeadHanter.class);
            Vacancies vacancies = hh.getItems().get(0);
            textRequest = "Предлагаю ознакомиться с вакансией:\n" +
                    "В " + vacancies.getEmployer().getName() +
                    "\nсрочно требуется " + vacancies.getName() + "\n" +
                    "\nподробности тут: " + vacancies.getAlternate_url();
            return textRequest;
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return textRequest;
    }
}

