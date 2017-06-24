package com.khronodragon.bluestone;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Start {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String jsonCode = new String(Files.readAllBytes(Paths.get("config.json")));
        JSONObject config = new JSONObject(jsonCode);

        String token = config.getString("token");
        int shardCount = config.optInt("shardCount", 1); // 1
        String type = config.optString("type"); // "bot"

        AccountType accountType;
        if (type.equals("bot")) {
            accountType = AccountType.BOT;
        } else if (type.equals("user")) {
            accountType = AccountType.CLIENT;
        } else {
            System.out.println("Warning: unrecognized account type! Use either 'client' (user) or 'bot' (bot). Assuming bot.");
            accountType = AccountType.BOT;
        }

        Unirest.setDefaultHeader("User-Agent", Bot.USER_AGENT);

        try {
            Bot.start(token, shardCount, accountType, config);
        } catch (LoginException|RateLimitedException e) {
            e.printStackTrace();
        }
    }
}