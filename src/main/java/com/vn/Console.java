package com.vn;

import io.lettuce.core.RedisCommandExecutionException;

import java.net.ConnectException;
import java.net.URLDecoder;
import java.util.Scanner;

public class Console {
    public static void main(String[] args) {
        System.out.println("Wellcomme to Redis Client TEN Console.\n\nThe supported commands are:");
        PrintCommands();
        System.out.println("Type cmd to see commands again or exit to close console");
        Scanner scanner = new Scanner(System.in);
        String commandStr;

        while (!(commandStr = scanner.nextLine()).equals("exit")) {
            if (commandStr.toUpperCase().trim().equals("CMD")) {
                PrintCommands();
                continue;
            }

            try {
                commandStr = URLDecoder.decode(commandStr, "UTF-8");
                ExecutionHub commandExecution = new ExecutionHub(commandStr);
                System.out.println(commandExecution.Execute());
            } catch (IllegalArgumentException e) {
                System.out.print("\n" + e.getMessage());
                System.out.println("Invalid command, please try again...");
                continue;
            } catch (RedisCommandExecutionException | ConnectException | UnsupportedOperationException eRedis) {
                System.out.println(eRedis.getMessage());
                continue;
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        System.out.println("I hope that you enjoyed... ;)\nBye!");
    }

    private static void PrintCommands() {
        System.out.println("GET key\nSET key val\nSETEX key expiration(sec) val\nDEL key [key...]\nDBSIZE\nINCR key");
        System.out.println("ZADD key score \"member 1\" [score \"member n\"...]");
        System.out.println("ZADD key score member [score member]");
        System.out.println("ZCARD key\nZRANK key member\nZRANGE key start stop [WITHSCORES]\n");
        System.out.println("For more information visit: https://redis.io/commands/\n");
    }
}
