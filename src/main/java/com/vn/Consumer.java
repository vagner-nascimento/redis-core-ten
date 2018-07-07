package com.vn;

import com.vn.infrastructure.cache.redis.RedisClientTen;
import com.vn.infrastructure.cache.redis.KeyValue;
import io.lettuce.core.protocol.CommandType;

import java.util.*;

public class Consumer {
    public static void main(String[] args) {

        System.out.println("Please, enter a valid redis command or exit to leave");
        Scanner scanner = new Scanner(System.in);
        String commandStr;

        while (!(commandStr = scanner.nextLine()).equals("exit")) {

            String[] commandParts = commandStr.split(" ");
            CommandType command = GetCommand(commandParts);

            if (command == null) {
                System.out.println("Invalid command, please try again...");
                continue;
            }

            Map.Entry<String, String> keyValue;

            switch (command) {
                case GET:
                    keyValue = GetKeyValue(commandParts);

                    if (keyValue == null || keyValue.getKey().equals("")) {
                        System.out.println("Key not informed for GET command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.get(keyValue.getKey()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SET:
                    keyValue = GetKeyValue(commandParts);

                    if (keyValue == null || keyValue.getKey().equals("") || keyValue.getValue().equals("")) {
                        System.out.println("Key and/or Value are not informed for SET command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.set(keyValue.getKey(), keyValue.getValue()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("Command not implemented, talk with the programmer...");
                    break;
            }
        }
    }

    private static CommandType GetCommand(String[] commandParts) {
        if (commandParts.length == 0) {
            return null;
        }

        String commandStr = commandParts[0];
        CommandType command;

        try {
            command = CommandType.valueOf(commandStr.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }

        return command;
    }

    private static Map.Entry<String, String> GetKeyValue(String[] argumments) {
        if (argumments.length < 2) {
            return null;
        }

        String key = argumments[1].trim();
        StringBuffer value = new StringBuffer();

        if (argumments.length >= 3) {
            for (int i = 2; i <= (argumments.length -1); i++) {
                value.append(" " + argumments[i]);
            }
        }

        return new KeyValue<>(key, value.toString().trim());
    }
}

