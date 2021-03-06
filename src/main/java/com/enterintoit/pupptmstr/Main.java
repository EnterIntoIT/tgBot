package com.enterintoit.pupptmstr;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;


public class Main {

    private static final ArrayList<String> vol = new ArrayList<>();//массив выпусков подкастов
    private static String key;

    private static String PROXY_HOST = "...";//хост прокси
    private static Integer PROXY_PORT = 1080;
    private static String PROXY_USER = "...";//юзернейм для авторизации в прокси
    private static String PROXY_PASSWORD = "...";//пароль для авторизации в прокси

    public static void main(String[] args) {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(new File("key.txt")));
            key = reader.readLine();
            System.out.println("Ключ прочитан...");
            reader = new BufferedReader(new FileReader(new File("audioIds.txt")));
            int i = 0;
            while ((line = reader.readLine()) != null) {
                vol.add(i, line);
                i++;
            }
            System.out.println("Выпуски прочитаны и добавлены в массив..");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(PROXY_USER, PROXY_PASSWORD.toCharArray());
                }
            });


            ApiContextInitializer.init();
            TelegramBotsApi botsApi = new TelegramBotsApi();

            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

            botsApi.registerBot(new PodcastBot(botOptions));
            System.out.println("Бот зарегестрирован...");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    /**package-private*/
    static int getNumOfEpisodes() {
        return vol.size();
    }

    /**package-private*/
    static String getEpisode(int numOfEpisode) {
        if (numOfEpisode <= getNumOfEpisodes())
            return vol.get(numOfEpisode - 1);

        return "Error";
    }

    /**package-private*/
    static String getKey() {
        return key;
    }

}
