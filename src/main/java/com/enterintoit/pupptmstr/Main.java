package com.enterintoit.pupptmstr;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;


public class Main {

    static ArrayList<String> vol = new ArrayList<>();//массив выпусков подкастов
    static String key;


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

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new PodcastBot());
            System.out.println("Бот зарегестрирован...");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

}
