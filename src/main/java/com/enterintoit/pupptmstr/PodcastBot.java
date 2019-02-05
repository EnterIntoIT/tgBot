package com.enterintoit.pupptmstr;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;

public class PodcastBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            if (update.getMessage().getText().charAt(0) == '/') {
                commandChecker(update);
            } else phraseChecker(update);

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

    private void commandChecker(Update update) {
        switch(update.getMessage().getText()) {
            case "/start":
                hello(update);
                break;
            case "/vol1":
                wait(update);
                sendPodcast(update, 1);
                break;
            case "/vol2":
                wait(update);
                sendPodcast(update, 2);
                break;
            case "/vol3":
                wait(update);
                sendPodcast(update, 3);
                break;
            case "/vol4":
                wait(update);
                sendPodcast(update, 4);
                break;
            case "/vol5":
                wait(update);
                sendPodcast(update, 5);
                break;
            case "/vol6":
                wait(update);
                sendPodcast(update, 6);
                break;
            case "/vol7":
                wait(update);
                sendPodcast(update, 7);
                break;
            case "/allVolumes":
                sendAllPodcasts(update);
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
            case "/help":
                help(update);
                break;
            case "/admin":
                if (isAdmin(update.getMessage().getChatId().toString())) {
                    SendMessage message = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Вау, админ! Позже я прикручу рассылку сообщений для вас\n...\nЖдите");
                    try {
                        execute(message);
                        logMessage(update, message.getText());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    SendMessage notAdminMessage = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Эмммм, ты кто такой?\nТы не админ, брат.\nНе используй эту команду");
                    try {
                        execute(notAdminMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                SendMessage defaultMessage = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Вау, комада! Сейчас я её как выполню!\n...\nНичего не вышло, а жаль\n" +
                                "Исправте команду и попробуйте еще раз");
                try {
                    execute(defaultMessage);
                    logMessage(update, defaultMessage.getText());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
        }
    }

    private void messageSender(Update update, String text) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(text);
        try {
            execute(message);
            logMessage(update, message.getText());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void phraseChecker(Update update) {
        switch (update.getMessage().getText()) {
            case "хуй":
                messageSender(update, "залупа");
                break;
            case "Хуй":
                messageSender(update, "Залупа");
                break;
            case "пидор":
                messageSender(update, "ты");
                break;
            case "Пидор":
                messageSender(update, "Ты");
                break;
            case "спасибо":
                messageSender(update, "всегда пожалуйста");
                break;
            case "Спасибо":
                messageSender(update, "Всегда пожалуйста");
                break;
            case "привет":
                messageSender(update, "Дарова)\nЛадно, шучу");
                hello(update);
                break;
            case "Привет":
                messageSender(update, "Дарова)\nЛадно, шучу");
                hello(update);
                break;
                default:
                    missunderstand(update);
        }
    }

    private void logMessage(Update update, String messageText) {

        String loggingMessage = "--------------------------------------------\n" +
                "Incoming: " +
                update.getMessage().getFrom().getFirstName() +
                "(" + update.getMessage().getFrom().getUserName() + " - " + update.getMessage().getChatId() + ")" +
                ": " + update.getMessage().getText() +
                "\nReply: " + messageText;
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

    private boolean isAdmin(String chatId) {
        boolean res = false;
        try{
            File file = new File("admins.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;

            while((line = bufferedReader.readLine()) != null) {
                if (chatId.equals(line))
                    res = true;
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    private void sorry(Update update) {
        messageSender(update, "Извините, мы пока можем отвечать только на текстовые сообщения");
    }

    private void help(Update update) {
        messageSender(update,
                "Я чат-бот проекта ЗайдиВАйТи\n" +
                        "Это тестовая версия, так что умею я пока не много и буду рад feedback'y\n" +
                        "Вот что я могу:\n" +
                        "/vol<номер выпуска> - выслать выпуск c указанным номером\n" +
                        "Например /vol2\n" +
                        "/allVolumes - выслать полный список всех выпусков\n" +
                        "/help - помощь(это самое меню)\n" +
                        "/vols - подписаться на рассылку новых выпусков\n" +
                        "/news - подписаться на новостную рассылку проекта\n" +
                        "/sub - полная подписка\n" +
                        "Прошу заметить, что отправка выпуска может занять несколько минут.");
    }

    private void hello(Update update) {
        messageSender(update, "Здравствуй " + update.getMessage().getFrom().getFirstName() +
                "\nРады видеть вас здесь!");
        help(update);
    }

    private void missunderstand(Update update) {
        messageSender(update, "Простите, возможно я вас не понимаю...\nЭто поможет нам лучше понимать друг друга:");
        help(update);
    }

    private void sendPodcast(Update update, int numOfPodcast) {
        SendAudio audio = new SendAudio()
                .setChatId(update.getMessage().getChatId())
                .setAudio(Main.vol.get(numOfPodcast - 1))
                .setTitle(numOfPodcast + "й выпуск");
        try {
            execute(audio);
            logMessage(update,  "отправлено аудио - " + audio.getTitle());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAllPodcasts(Update update) {
        messageSender(update,
                "Список всех эпизодов подкаста:\n" +
                        "/vol1 - 1.Вводный выпуск(ч.1)\n" +
                        "/vol2 - 2.Вводный выпуск(ч.2)\n" +
                        "/vol3 - 3.От железа до ОСей(ч.1 Железо)\n" +
                        "/vol4 - 4.От железа до ОСей(ч.2 Операционные системы)\n" +
                        "/vol5 - 5.От железа до ОСей(ч.3 Сети)\n" +
                        "/vol6 - 6.Как понять андроида(ч.1 Старые языки)\n" +
                        "/vol7 - 7.Как понять андроида(ч.2 Новые языки)");
    }

    private void wait(Update update) {
        messageSender(update, "Подождите, выполняю...");
        messageSender(update, "Это может занять определенное время(не расстраивайтесь)");
    }

    //0 - полная подписка, 1 - новостная, 2 - подписка на новые выпуски
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
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
            writer.write(update.getMessage().getChatId().toString());
            writer.close();
            messageSender(update, "Добавлена подписка...");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void deleteSub(Update update, String fileName) {
        try {
            File file = new File(fileName);
            File tempFile = new File(fileName + ".tmp");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
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

            messageSender(update, "Подписка удалена...");
        } catch (Exception e) {
            e.printStackTrace();
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
        }
        return res;
    }

}