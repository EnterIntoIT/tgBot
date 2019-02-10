package com.enterintoit.pupptmstr;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotOptions;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;


public class Main {

    static ArrayList<String> vol = new ArrayList<>();//массив выпусков подкастов
    static String key;

    private static String PROXY_HOST = "45.76.84.51";
    private static Integer PROXY_PORT = 1080;
    private static String PROXY_USER = "trytoguesstheusername";
    private static String PROXY_PASSWORD = "tipidorda";

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

}
