package com.vn;

import com.vn.infrastructure.cache.redis.RedisClientTen;
import com.vn.infrastructure.cache.redis.RedisCommandBuilder;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisCommandExecutionException;

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

            RedisCommandBuilder commandBuilder;

            try {
                commandStr = URLDecoder.decode(commandStr, "UTF-8");
                commandBuilder = new RedisCommandBuilder(commandStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid command, please try again...");
                continue;
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            KeyValue item;
            switch (commandBuilder.getCommand()) {
                case GET:
                    if (commandBuilder.getKey() == null) {
                        System.out.println("Key not informed for GET command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.Get(commandBuilder.getKey()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SET:
                    if (commandBuilder.getKeyValue() == null) {
                        System.out.println("Key and/or Value are not informed for SET command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.Set(commandBuilder.getKeyValue()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SETEX:
                    if (commandBuilder.getKeyValue() == null || commandBuilder.getExpiration() <= 0) {
                        System.out.println("Invalid arguments for SETEX command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.SetEX(commandBuilder.getKeyValue(), commandBuilder.getExpiration()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DEL:
                    if (commandBuilder.getKeysToDelete() == null) {
                        System.out.println("No keys informed for DEL command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.Del(commandBuilder.getKeysToDelete()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DBSIZE:
                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.DbSize());
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case INCR:
                    if (commandBuilder.getKey() == null) {
                        System.out.println("Key not informed for INCR command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.Incr(commandBuilder.getKey()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ZADD:
                    RedisClientTen lClient = null;

                    try {
                        if (commandBuilder.getKey() == null || commandBuilder.getScoredValues().length <= 0) {
                            System.out.println("Invalid parameters for ZADD method");
                            break;
                        }

                        lClient = new RedisClientTen();
                        System.out.println(lClient.ZAdd(commandBuilder.getKey(), commandBuilder.getScoredValues()));
                    } catch (UnsupportedOperationException | RedisCommandExecutionException eMsg) {
                        System.out.println(eMsg.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (lClient != null) lClient.close();
                    }
                    break;
                case ZCARD:
                    if (commandBuilder.getKey() == null) {
                        System.out.println("Key not informed for ZCARD command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.ZCard(commandBuilder.getKey()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ZRANK:
                    if (commandBuilder.getKeyValue() == null) {
                        System.out.println("Key and/or member not informed for ZRANK command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.ZRank(commandBuilder.getKeyValue()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ZRANGE:
                    if (commandBuilder.getKey() == null
                            || commandBuilder.getStartPos() == null
                            || commandBuilder.getStopPos() == null) {
                        System.out.println("Key, start and/or stop are not informed for ZRANGE command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.ZRange(commandBuilder.getKey(),
                                commandBuilder.getStartPos(),
                                commandBuilder.getStopPos(),
                                commandBuilder.isWithScores()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("Command not implemented");
                    break;
            }
        }
        System.out.println("I hope that you enjoyed... ;)\nBye!");
    }

    private static void PrintCommands() {
        System.out.println("GET key\nSET key val\nSETEX key expiration(sec) val\nDEL key [key...]\nDBSIZE\nINCR key");
        System.out.println("ZADD key score \"member\" [score \"member\"...]\nZCARD key\nZRANK key member");
        System.out.println("ZRANGE key start stop [WITHSCORES]\n");
        System.out.println("For more information visit: https://redis.io/commands/\n");
    }
}
