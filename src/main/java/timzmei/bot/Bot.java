package timzmei.bot;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import timzmei.bot.hh.HeadHanter;
import timzmei.bot.hh.Vacancies;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class Bot {

    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));

    public void serve() {

        // Create your bot passing the token received from @BotFather

        // Register for updates
        bot.setUpdatesListener(this::process);


    }

    private void process(Update update) throws IOException {

        // Send messages
        Message message = update.message();


        BaseRequest request = null;

        if(message != null) {
            long chatId = message.chat().id();

            if (message.text().toLowerCase().contains("какая погода")) {
                request = new SendMessage(chatId, new Weather().getWeather());
            } else if (message.text().toLowerCase().contains("работа") || message.text().toLowerCase().contains("работу")) {
                request = getHHRequest(request, chatId, message);
            } else if (message.text().toLowerCase().contains("когда") || message.text().toLowerCase().contains("зачем") || message.text().toLowerCase().contains("почему") || message.text().toLowerCase().contains("что") ||message.text().toLowerCase().contains("кто")){
                if (message.text().toLowerCase().contains("?")) {
                    request = new SendMessage(chatId, "хороший вопрос...").replyToMessageId(message.messageId());


                }
            } else if (message.text().toLowerCase().contains("привет") || message.text().toLowerCase().contains("здравствуйте")){
                request = new SendMessage(chatId, "Привет, " + message.from().username() + "!") .parseMode(ParseMode.Markdown ).replyToMessageId(message.messageId());
            }
        }

        if(request != null) {
            BaseResponse response = bot.execute(request);
        }
    }

    private static BaseRequest getHHRequest(BaseRequest request, long chatId, Message message) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest requestHttp = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hh.ru/vacancies?area=99&specialization=1.295&industry=9&order_by=salary_desc"))
                .build();
        try {
            HttpResponse<String> response = client.send(requestHttp, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            HeadHanter hh = new Gson().fromJson(body, HeadHanter.class);
            Vacancies vacancies = hh.getItems().get(0);
            String textRequest = "Привет " + message.from().username() + "!\nПредлагаю ознакомиться с вакансией:\n" +
                    "В " + vacancies.getEmployer().getName() +
                    "\nсрочно требуется " + vacancies.getName() + "\n" +
                    "\nподробности тут: " + vacancies.getAlternate_url();
            request = new SendMessage(chatId, textRequest);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return request;
    }

    private int process(List<Update> updates) {
        // ... process updates
        updates.forEach(update -> {
            try {
                process(update);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // return id of last processed update or confirm them all
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}

