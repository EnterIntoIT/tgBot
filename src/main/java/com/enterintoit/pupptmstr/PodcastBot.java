package com.enterintoit.pupptmstr;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.ArrayList;

public class PodcastBot extends TelegramLongPollingBot {

    protected PodcastBot(DefaultBotOptions botOptions) {
        super(botOptions);
    }

    private static final String BLACKLIST = "blacklist.txt";
    private static final String ADMINS = "admins.txt";
    private static final String VOLS_SUBS = "volsSubs.txt";
    private static final String NEWS_SUBS = "newsSubs.txt";

    //TODO() сделать команду для автоматического добавления файл-ид нового подкаста

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            logUsersMessage(update);
            if (update.getMessage().getText().charAt(0) == '/') {
                commandChecker(update);
            } else if(update.getMessage().getText().charAt(0) == '='){
                wait(update);
                messageFormatChecker(update);
            } else phraseChecker(update);

        } else if (update.hasMessage()
                && (update.getMessage().getAudio() != null)
                && isAdmin(update.getMessage().getChatId().toString())) {
            logEvent("Админ прислал аудиозапись, отправляю ему её id");
            String audioId = update.getMessage().getAudio().getFileId();
            SendAudio msg = new SendAudio()
                    .setChatId(update.getMessage().getChatId())
                    .setCaption(audioId)
                    .setAudio(audioId);
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                logEvent(e.getLocalizedMessage());
            }
        } else
            sorry(update);
    }


    @Override
    public String getBotUsername() {
        return "EnterIntoItBot";
    }

    @Override
    public String getBotToken() {
        return Main.key;
    }

    //-------------------------------------Чекеры----------------------------------
    //TODO() переделать vol чтобы не редачить исходники с выходом каждого нового эпизода
    private void commandChecker(Update update) {
        switch(update.getMessage().getText()) {
            case "/start":
                hello(update);
            break;
            case "/help":
                help(update);
                break;
            case "/vol1":
                vol(update, 1);
                break;
            case "/vol2":
                vol(update, 2);
                break;
            case "/vol3":
                vol(update, 3);
                break;
            case "/vol4":
                vol(update, 4);
                break;
            case "/vol5":
                vol(update, 5);
                break;
            case "/vol6":
                vol(update, 6);
                break;
            case "/vol7":
                vol(update, 7);
                break;
            case "/vol8":
                replyMessageSender(update, "Данный выпуск пока не добавлен в мою бд ");
                break;
            case "/allVolumes":
                wait(update);
                allVolumes(update);
            break;
            case "/volumesList":
                volumesList(update);
                break;
            case "/news":
                wait(update);
                subsAction(update, 1);
                break;
            case "/vols":
                wait(update);
                subsAction(update, 2);
                break;
            case "/sub":
                wait(update);
                subsAction(update, 0);
                break;
            //Блок с админскими командами
            case "/status":
                wait(update);
                if (isAdmin(update.getMessage().getChatId().toString())) {
                        statusAdmin(update);
                    }
                    else {
                        statusSub(update);
                    }
                break;
            case "/admin":
                if (isAdmin(update.getMessage().getChatId().toString())) {
                    sendAdminCommands(update);
                } else {
                    requestForAdminMessageFormat(update);
                }
                break;
            case "/reply":
                if (isAdmin(update.getMessage().getChatId().toString())) {
                    adminReplyMessageFormat(update);
                } else {
                    replyMessageSender(update, "Эмммм, ты кто такой?\n" +
                            "Ты не админ, брат.\nНе используй эту команду");
                }
                break;
            case "/distribution":
                if (isAdmin(update.getMessage().getChatId().toString())) {
                    distributionMessageFormat(update);
                } else {
                    replyMessageSender(update, "Эмммм, ты кто такой?\n" +
                            "Ты не админ, брат.\nНе используй эту команду");
                }
                break;
            case "/ban":
                if (isAdmin(update.getMessage().getChatId().toString()))
                    banMessageFormat(update);
                else
                    replyMessageSender(update, "Эмммм, ты кто такой?\n" +
                            "Ты не админ, брат.\nНе используй эту команду");
                break;
            default:
                replyMessageSender(update, "Вау, комада! Сейчас я её как выполню!" +
                        "\n...\nНичего не вышло, а жаль\n" +
                        "Исправте команду и попробуйте еще раз");
        }
    }

    private void messageFormatChecker(Update update) {
        AdminMessageParser messageParser = new AdminMessageParser(update.getMessage().getText());
        messageParser.checkMatchingAndParse();
        if (!messageParser.getError().equals("error")) {
            switch (messageParser.getMessageType()) {
                case "adm":
                    switch (messageParser.getContentType()) {
                        case "thm":
                            if (isBaned(update.getMessage().getChatId().toString()))
                                replyMessageSender(update, "Вы находитесь в черном списке, " +
                                        "данная функция для вас недоступна");
                            else
                                themeForAdminMessageSender(update, messageParser.getMessageText());
                            break;
                        case "req":
                            if (isBaned(update.getMessage().getChatId().toString()))
                                replyMessageSender(update, "Вы находитесь в черном списке, " +
                                        "данная функция для вас недоступна");
                            else
                                requestForAdminsMessageSender(update, messageParser.getMessageText());
                            break;
                        default:
                            replyMessageSender(update, "Неверный тип содержания");
                    }
                    break;
                case "dis":
                    if(isAdmin(update.getMessage().getChatId().toString())) {
                        switch (messageParser.getContentType()) {
                            case "news":
                                distributionMessageSender(update, 1, messageParser.getMessageText());
                                break;
                            case "episode":
                                distributionMessageSender(update, 2, messageParser.getMessageText());
                                break;
                            case "all":
                                distributionMessageSender(update, 1, messageParser.getMessageText());
                                distributionMessageSender(update, 2, messageParser.getMessageText());
                                break;
                            default:
                                replyMessageSender(update, "Неверный тип содержания.");
                        }
                    } else
                        replyMessageSender(update, "Простите, у вас нет прав администратора.");
                    break;
                case "rep":
                    if (isAdmin(update.getMessage().getChatId().toString())) {
                        if (messageParser.getContentType().length() == 9) {
                            adminReplyMessageSender(update,
                                    messageParser.getContentType(),
                                    messageParser.getMessageText());
                        } else
                            replyMessageSender(update, "Неверный chatId");
                    } else
                        replyMessageSender(update, "Простите, у вас нет прав администратора");
                    break;
                case "ban":
                    if (isAdmin(update.getMessage().getChatId().toString())){
                        if (messageParser.getContentType().length() == 9) {
                            if (!isBaned(messageParser.getContentType()))
                                ban(update, messageParser.getContentType());
                            else
                                replyMessageSender(update, "Данный юзер уже забанен...");
                        } else
                            replyMessageSender(update, "Неверный chatId");
                    }
                default:
                    replyMessageSender(update, "Ошибка в типе сообщения, попробуйте еще раз.");

            }
        } else
            replyMessageSender(update, "Неверный формат сообщения, исправте и попробуйте еще раз.");

    }

    private void phraseChecker(Update update) {
        switch (update.getMessage().getText().toLowerCase()) {
            case "хуй":
                replyMessageSender(update, "залупа");
                break;
            case "пидор":
                replyMessageSender(update, "ты");
                break;
            case "спасибо":
                replyMessageSender(update, "всегда пожалуйста");
                break;
            case "привет":
                replyMessageSender(update, "Дарова)\nЛадно, шучу");
                hello(update);
                break;
            default:
                missunderstand(update);
        }
    }
    //-----------------------------------------------------------------------------




    //-------------------------------------Сендеры---------------------------------
    private void replyMessageSender(Update update, String text) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(text);
        try {
            execute(message);
            logBotsMessage(message.getText());
        } catch (TelegramApiException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }
    }

    private void themeForAdminMessageSender(Update update, String text) {
        ArrayList<String> admins = new ArrayList<>();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(ADMINS)));
            while ((line = reader.readLine()) != null) {
                admins.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }

        for (String adm : admins) {
            SendMessage message = new SendMessage()
                    .setChatId(adm)
                    .setText("Предложение темы для обсуждения в выпуске:\n" +
                            "От "+ update.getMessage().getFrom().getFirstName() +
                            "(" + update.getMessage().getFrom().getUserName() + ")\n\n" +
                            "Предложенная тема: " + text);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                logEvent(e.getLocalizedMessage());
            }
        }
        done(update);
        logEvent("Пользователь " +
                update.getMessage().getFrom().getFirstName() + " - " +  update.getMessage().getFrom().getUserName() +
                "предложил тему: " + text);
    }

    private void requestForAdminsMessageSender(Update update, String text) {
        String line;
        ArrayList<String> admins = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(new File(ADMINS));
            BufferedReader reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                admins.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }

        for (String adm : admins) {
            SendMessage message = new SendMessage()
                    .setChatId(adm)
                    .setText("Пришел запрос:\n" +
                            text +
                            "\nАвтор:");
            SendMessage sendClientChatId = new SendMessage()
                    .setChatId(adm)
                    .setText(update.getMessage().getChatId().toString()
                    + "(" + update.getMessage().getFrom().getUserName() +
                            update.getMessage().getFrom().getFirstName() + ")");
            try {
                execute(message);
                execute(sendClientChatId);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                logEvent(e.getLocalizedMessage());
            }

        }
        done(update);
        logEvent("Пользователь " +
                update.getMessage().getFrom().getFirstName() + " - " +  update.getMessage().getFrom().getUserName() +
                "отправил сообщение: " + text);
    }

    //только текстовые ответы
    private void adminReplyMessageSender(Update update, String destinationChatId, String messageText){
        SendMessage message = new SendMessage()
                .setChatId(destinationChatId)
                .setText("Вам поступил ответ от админа:" +
                                update.getMessage().getFrom().getFirstName() +
                                "(" + update.getMessage().getFrom().getUserName() + ")" +
                        "\n" +
                        "Ответ: " + messageText);
        try {
            execute(message);
        }catch (TelegramApiException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }
        done(update);
        logEvent("Пользователю отправлен ответ от админа");
    }

    //1 - новость, 2 - новый эпизод
    private void distributionMessageSender(Update update, int typeOfMes, String messageText) {
        String fileName;
        BufferedReader reader;
        ArrayList<String> subs = new ArrayList<>();
        String line;

        if (typeOfMes == 1)
            fileName = NEWS_SUBS;
        else
            fileName = VOLS_SUBS;

        try {
            reader = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = reader.readLine()) != null) {
                subs.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }

        for (String sub : subs) {
            SendMessage message = new SendMessage()
                    .setChatId(sub)
                    .setText(messageText);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                logEvent(e.getLocalizedMessage());
            }
        }
        replyMessageSender(update, "Выполнено...");
        logEvent("Осуществлена рассылка для " + fileName);
    }
    //-----------------------------------------------------------------------------





    //-----------------------------Пользовательские команды------------------------
    private void help(Update update) {
        replyMessageSender(update,
                "Я чат-бот проекта ЗайдиВАйТи\n" +
                        "Это тестовая версия, так что умею я пока не много\n" +
                        "Буду рад feedback'y)\n" +
                        "Вот что я могу:\n" +
                        "/help - помощь(это самое меню)\n" +
                        "/vol{№} - выслать выпуск c указанным номером\n" +
                        "Например /vol2\n" +
                        "/allVolumes - выслать все выпуски по порядку\n" +
                        "/volumesList - выслать полный список выпусков\n" +
                        "/vols - подписаться на рассылку новых выпусков\n" +
                        "/news - подписаться на новостную рассылку проекта\n" +
                        "/sub - полная подписка\n" +
                        "/status - проверка статуса своих подписок\n" +
                        "/admin - для взаимодействия с администрацией\n" +
                        "Для отмены подписки используются те же команды, что и для её активации\n" +
                        "Если вы администратор, то используйте /admin для того, чтобы узнать свои возможности");
    }

    private void vol(Update update, int numOfPodcast) {
        SendAudio audio = new SendAudio()
                .setChatId(update.getMessage().getChatId())
                .setAudio(Main.vol.get(numOfPodcast - 1));
        try {
            execute(audio);
            logEvent("Отправлен " + numOfPodcast + "й эпизод подкаста");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }
    }

    private void allVolumes(Update update) {
        for (int i = 0; i < 7; i++) {
            vol(update, i+1);
        }
    }

    private void volumesList(Update update) {
        replyMessageSender(update,
                "Список всех эпизодов подкаста:\n" +
                        "/vol1 - 1.Вводный выпуск(ч.1)\n" +
                        "/vol2 - 2.Вводный выпуск(ч.2)\n" +
                        "/vol3 - 3.От железа до ОСей(ч.1 Железо)\n" +
                        "/vol4 - 4.От железа до ОСей(ч.2 Операционные системы)\n" +
                        "/vol5 - 5.От железа до ОСей(ч.3 Сети)\n" +
                        "/vol6 - 6.Как понять андроида(ч.1 Старые языки)\n" +
                        "/vol7 - 7.Как понять андроида(ч.2 Новые языки)");
    }

    //0 - полная подписка, 1 - новостная, 2 - эпизодическая
    private void subsAction(Update update, int typeOfSub) {
        switch (typeOfSub) {
            case 1:
                if (isSub(update, "newsSubs.txt"))
                    deleteSub(update, "newsSubs.txt");
                else
                    addSub(update, "newsSubs.txt");
                break;
            case 2:
                if (isSub(update, "volsSubs.txt"))
                    deleteSub(update, "volsSubs.txt");
                else
                    addSub(update, "volsSubs.txt");
                break;
            case 0:
                if (!isSub(update, "volsSubs.txt")){
                    if (!isSub(update, "newsSubs.txt"))
                        addSub(update, "newsSubs.txt");
                    addSub(update, "volsSubs.txt");
                } else if (!isSub(update, "newsSubs.txt")){
                    if (!isSub(update, "volsSubs.txt"))
                        addSub(update, "volsSubs.txt");
                    addSub(update, "newsSubs.txt");
                } else {
                    deleteSub(update, "volsSubs.txt");
                    deleteSub(update, "newsSubs.txt");
                }
                break;
        }
    }

    private void addSub(Update update, String fileName) {
        String subType;
        if (fileName.equals(NEWS_SUBS))
            subType = "новостная";
        else
            subType = "эпизодическая";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName), true));
            writer.write(update.getMessage().getChatId().toString());
            writer.newLine();
            writer.close();
            replyMessageSender(update, "Добавлена " + subType + " подписка...");
        } catch (IOException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }

    }

    private void deleteSub(Update update, String fileName) {
        String subType;
        if (fileName.equals(NEWS_SUBS))
            subType = "новостная";
        else
            subType = "эпизодическая";
        try {
            File file = new File(fileName);
            File tempFile = new File(fileName + ".tmp");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, true));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.equals(update.getMessage().getChatId().toString())) continue;
                writer.write(line);
                writer.newLine();
            }
            writer.close();
            reader.close();
            if (file.delete()) {
                System.out.println("Удаление успешно...");
                if (tempFile.renameTo(file))
                    System.out.println("Переименование успешно...");
                else
                    System.out.println("Ошибка переименования...");
            } else
                System.out.println("Ошибка удаления...");

            replyMessageSender(update, "Удалена " + subType + " подписка...");
        } catch (Exception e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }
    }

    private boolean isSub(Update update, String fileName) {
        boolean res = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(update.getMessage().getChatId().toString())){
                    res = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }
        return res;
    }

    private void statusSub(Update update) {
        String newsSub = "off", volsSub = "off";
        if (isSub(update, NEWS_SUBS))
            newsSub = "on";
        if(isSub(update, VOLS_SUBS))
            volsSub = "on";
        replyMessageSender(update,  update.getMessage().getFrom().getFirstName() +
                "(" + update.getMessage().getFrom().getUserName() + ")" +
                "\nНовостная подписка: " + newsSub +
                "\nЭпизодическая подписка: " + volsSub);
    }

    private void requestForAdminMessageFormat(Update update) {
        replyMessageSender(update, "Формат сообщения для обращения к админам:\n" +
                "=adm(тип сообщения - обращение к админам)\n" +
                "=thm/req(тип содержимого - предложение темы/обращение к админам)\n" +
                "=Текст сообщения\n\n" +
                "Возможные типы содержимого в данной ситуации:\n" +
                "thm - предложить тему для будущих выпусков\n" +
                "req - обратиться к админам с просьбой\n" +
                "Прошу вас заметить, что формат сообщения строгий и должен соблюдаться\n" +
                "Пример сообщения админу:\n");
        replyMessageSender(update, "=adm\n" +
                "=req\n" +
                "=Уважаемые админы, помогите мне пожалуйста с такой-то и такой-то проблемой\n\n");
    }
    //-----------------------------------------------------------------------------



    //-----------------------------Админские команды-------------------------------
    private void sendAdminCommands(Update update){
        replyMessageSender(update, "Здравствуйте, господин админ!\n" +
                "Список админских команд:\n" +
                "/admin - получить список админских команд\n" +
                "/status - получить информацию о количестве подписчиков\n" +
                "/distribution - получить формат сообщения для рассылки его подписчикам\n" +
                "/reply - получить формат сообщения для ответа конкретному подписчику\n" +
                "/ban - получить формат сообщения, для отправки подписчика в бан\n" +
                "Прошу вас заметить, что формат сообщения строгий и должен соблюдаться\n" +
                "Так же у вас есть возмножность отправить мне аудиозапись и получить её файл-ид\n" +
                "Это пригодится вам для добавления новых эпизодов в мою бд");
    }

    private void statusAdmin(Update update) {
        int newsCounter = 0, volsCounter = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(NEWS_SUBS)));
            while (reader.readLine() != null)
                newsCounter++;

            reader = new BufferedReader(new FileReader(new File(VOLS_SUBS)));
            while (reader.readLine() != null)
                volsCounter++;

        } catch (IOException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }
        replyMessageSender(update, "Подписчики новостей: " + newsCounter +
                "\nПодписчики выпусков: " + volsCounter);
    }

    private void distributionMessageFormat(Update update){
        replyMessageSender(update, "Формат сообщения для рассылки:\n" +
                "=dis(тип сообщения - рассылка)\n" +
                "=news/episode/all(тип содержимого = тип рассылки)\n" +
                "=Текст сообщения самой рассылки\n\n" +
                "Для самой рассылки просто отправте сообщение нужного формата.\n" +
                "Пример:");
        replyMessageSender(update, "=dis\n" +
                "=all\n" +
                "=Внимание друзья! Это рассылка!");
    }

    private void adminReplyMessageFormat(Update update) {
        replyMessageSender(update, "Формат сообщения для ответа подписчику:\n" +
                "=rep(тип сообщения - ответ подписчику)\n" +
                "=000000000(тип содержания - chatId подписчика)\n" +
                "=Текст сообщения ответа\n\n" +
                "Для ответа просто отправте сообщение нужного формата.\n" +
                "Пример:");
        replyMessageSender(update,
                "=rep\n" +
                "=299233972\n" +
                "=Спасибо за ваше сообщение! Мой ответ бла бла бля");
    }

    private void banMessageFormat(Update update) {
        replyMessageSender(update, "Формат сообщения для отправки юзера в бан-лист:\n" +
                "=ban(тип сообщения - забанить юзера\n" +
                "=0000000(тип содержания - chatId юзера)\n" +
                "=(текст - в данном случае не важно какой, " +
                "парсер сообщений настроен так, что тут должно быть хоть что-то)\n" +
                "Пример:");
        replyMessageSender(update,
                "=ban\n" +
                "=0000000\n" +
                "=что угодно");
    }
    //-----------------------------------------------------------------------------



    //----------------------------Вспомогательные мессенджи------------------------
    private void sorry(Update update) {
        replyMessageSender(update, "Извините, мы пока можем отвечать только на текстовые сообщения");
    }

    private void hello(Update update) {
        replyMessageSender(update, "Здравствуй " + update.getMessage().getFrom().getFirstName() +
                "\nРады видеть вас здесь!");
        help(update);
    }

    private void missunderstand(Update update) {
        replyMessageSender(update, "Простите, возможно я вас не понимаю...\n" +
                "Это поможет нам лучше понимать друг друга:");
        help(update);
    }

    private void wait(Update update) {
        replyMessageSender(update, "Подождите, выполняю...");
    }

    private void done(Update update) {
        replyMessageSender(update, "Выполнено...");
    }

    private void ban(Update update, String chatId) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(BLACKLIST), true));
            writer.write(chatId);
            writer.newLine();
            writer.close();
            replyMessageSender(update, "Юзер " + chatId + " забанен...");
        } catch (IOException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }
    }
    //-----------------------------------------------------------------------------



    //-------------------------------Системные команды-----------------------------
    private void logUsersMessage(Update update) {

        String loggingMessage = "--------------------------------------------\n" +
                update.getMessage().getFrom().getFirstName() +
                "(" + update.getMessage().getFrom().getUserName() + " - " + update.getMessage().getChatId() + ")" +
                ": " + update.getMessage().getText() + "\n";
        System.out.println(loggingMessage);

        try {

            File file = new File("Log.txt");
            FileWriter fileReader = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileReader);

            bufferedWriter.write(loggingMessage + "\n");
            bufferedWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void logBotsMessage(String messageText) {
        String loggingMessage = "--------------------------------------------\n" +
                "Bot: " + messageText + "\n";
        System.out.println(loggingMessage);

        try {

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("Log.txt"), true));
            bufferedWriter.write(loggingMessage + "\n");
            bufferedWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logEvent(String event) {
        String loggingMessage = "--------------------------------------------\n" +
                event + "\n";
        System.out.println(loggingMessage);

        try {
            FileWriter fileWriter = new FileWriter(new File("Log.txt"), true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(loggingMessage + "\n");
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAdmin(String chatId) {
        boolean res = false;
        try{
            File file = new File(ADMINS);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;

            while((line = bufferedReader.readLine()) != null) {
                if (chatId.equals(line))
                    res = true;
            }

        } catch (Exception e){
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }
        return res;
    }

    private boolean isBaned(String chatId) {
        boolean res = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(BLACKLIST)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(chatId)) {
                    res = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logEvent(e.getLocalizedMessage());
        }

        return res;
    }
    //-----------------------------------------------------------------------------

}