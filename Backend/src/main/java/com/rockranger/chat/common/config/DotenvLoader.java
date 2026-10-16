package com.rockranger.chat.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DotenvLoader {

    public static Map<String, Object> loadToMap() {
        Map<String, Object> map = new HashMap<>();
        try {
            Dotenv dotenv = null;
            if (new File(".env").exists()) {
                dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();
            } else if (new File("../.env").exists()) {
                dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
            } else {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
            }

            if (dotenv != null) {
                for (DotenvEntry entry : dotenv.entries()) {
                    map.put(entry.getKey(), entry.getValue());
                    if (System.getProperty(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: .env file loading: " + e.getMessage());
        }
        return map;
    }
}
