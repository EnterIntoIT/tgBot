package com.enterintoit.pupptmstr;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;


public class Main {

    static ArrayList<File> vol = new ArrayList<>();//массив выпусков подкастов
    static String key;


    public static void main(String[] args) {
        vol.add(0, new File("1.mp3"));
        vol.add(1, new File("2.mp3"));
        vol.add(2, new File("3.mp3"));
        vol.add(3, new File("4.mp3"));
        vol.add(4, new File("5.mp3"));
        vol.add(5, new File("6.mp3"));
        vol.add(6, new File("7.mp3"));
        System.out.println("Файлы прочитаны...");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("key.txt")));
            key = reader.readLine();
            System.out.println("Ключ прочитан...");
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
