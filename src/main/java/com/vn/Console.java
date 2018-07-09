package com.vn;

import com.vn.infrastructure.cache.redis.RedisClientTen;
import com.vn.util.TryParse;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.protocol.CommandType;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Console {
    public static void main(String[] args) {

        System.out.println("Please, enter a valid redis command or exit to leave");
        Scanner scanner = new Scanner(System.in);
        String commandStr;

        while (!(commandStr = scanner.nextLine()).equals("exit")) {

            try {
                commandStr = URLDecoder.decode(commandStr, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String[] commandParts = GetCommandParameters(commandStr);

            if (commandParts.length < 1) {
                System.out.println("Invalid command, please try again...");
                continue;
            }

            CommandType command;

            try {
                command = CommandType.valueOf(commandParts[0].trim().toUpperCase());
            } catch (Exception e) {
                System.out.println("Invalid command, please try again...");
                continue;
            }

            KeyValue item;
            String key;

            switch (command) {
                case GET:
                    if (!ValidateKey(commandParts)) {
                        System.out.println("Key not informed for GET command");
                        break;
                    }

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.Get(commandParts[1].trim()));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SET:
                    if (!ValidateKeyValue(commandParts)) {
                        System.out.println("Key and/or Value are not informed for SET command");
                        break;
                    }

                    item = KeyValue.just(commandParts[1].trim(), commandParts[2].trim());

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.Set(item));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SETEX:
                    if (!ValidateKeyValue(commandParts)) {
                        System.out.println("Invalid arguments for SETEX command");
                        break;
                    }

                    StringBuffer aux = new StringBuffer();
                    Long expiration = null;

                    for (char c : commandParts[2].toCharArray()) {
                        if (expiration == null) {
                            expiration = TryParse.toLong(String.valueOf(c));

                            if (expiration != null) {
                                aux.append(c);
                                expiration = null;
                            } else {
                                expiration = TryParse.toLong(aux.toString());
                                aux = new StringBuffer();
                            }
                        } else {
                            aux.append(c);
                        }
                    }

                    if ((expiration == null || expiration <= 0) || (aux.toString().trim().length() <= 0)) {
                        System.out.println("Invalid arguments for SETEX command");
                        break;
                    }

                    item = KeyValue.just(commandParts[1].trim(), aux.toString().trim());

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.SetEX(item, expiration));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DEL:
                    if (!ValidateKey(commandParts)) {
                        System.out.println("No keys informed for DEL command");
                        break;
                    }

                    List<String> listAux = new ArrayList<>();

                    listAux.add(commandParts[1].trim());

                    if (commandParts[2] != null) {
                        for (String s : commandParts[2].split("\\s")) {
                            listAux.add(s.trim());
                        }
                    }

                    String[] keysToDel = new String[listAux.size()];
                    keysToDel = listAux.toArray(keysToDel);

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.Del(keysToDel));
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
                    if (!ValidateKey(commandParts)) {
                        System.out.println("Key not informed for INCR command");
                        break;
                    }

                    key = commandParts[1].trim();

                    try (RedisClientTen client = new RedisClientTen()) {
                        System.out.println(client.Incr(key));
                    } catch (RedisCommandExecutionException eCmd) {
                        System.out.println(eCmd.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ZADD:
                    if (!ValidateKeyValue(commandParts)) {
                        System.out.println("Invalid parameters for ZADD command");
                        break;
                    }

                    ScoredValue<String>[] values;
                    RedisClientTen client = null;
                    key = commandParts[1].trim();

                    try {
                        values = GetScoredValues(commandParts[2].trim());
                        client = new RedisClientTen();
                        System.out.println(client.ZAdd(key, values));
                    } catch (UnsupportedOperationException | RedisCommandExecutionException eMsg) {
                        System.out.println(eMsg.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (client != null) client.close();
                    }
                    break;
                default:
                    System.out.println("Command not implemented, bad... baaad programmer...");
                    break;
            }
        }
    }

    /**
     * Separate the command entrie command in 3 parameters: command, key and arguments
     *
     * @param text Input text with the fully command
     * @return An array where ar[0] conrains the command, ar[1] contains the key and ar[2] contais de others argumments
     */
    @NotNull
    private static String[] GetCommandParameters(String text) {
        String command = null;
        String key = null;
        StringBuffer aux = new StringBuffer();
        int count = 0;

        for (char c : text.toCharArray()) {
            count++;
            if (command == null) {
                if (c != ' ') {
                    aux.append(c);
                } else {
                    command = aux.toString();
                    aux = new StringBuffer();
                    continue;
                }
                //Last array element without complete command
                if (count == text.length()) {
                    command = aux.toString();
                    aux = new StringBuffer();
                }
            } else if (key == null) {
                if (c != ' ') {
                    aux.append(c);
                } else {
                    key = aux.toString();
                    aux = new StringBuffer();
                    continue;
                }
                //Last array element without complete key
                if (count == text.length()) {
                    key = aux.toString();
                    aux = new StringBuffer();
                }
            } else {
                aux.append(c);
            }
        }

        return new String[]{command, key, aux.toString()};
    }

    /**
     * Mount a valid array of ScoredValues to use on ZADD command
     * - Text must start with a valid number score.
     * - Values must be envolved by quotes, something like "MyValue"
     * - Example of valid text to convert: 1 "one" 2 "two" 3 "three"
     * @param textToExtract Text with scores and values
     * @return An array of valid ScoredValues
     * @throws UnsupportedOperationException if some validadtion was not OK
     */
    @NotNull
    private static ScoredValue<String>[] GetScoredValues(String textToExtract) throws UnsupportedOperationException {
        char[] chars = textToExtract.toCharArray();
        int countQuota = 0;

        Double auxDouble = TryParse.toDouble(String.valueOf(chars[0]));
        if (auxDouble == null) {
            throw new UnsupportedOperationException("Command arguments must start with a valid double score");
        }

        if (chars[chars.length - 1] != '"') {
            throw new UnsupportedOperationException("The last argument must be a value envolved by quotes");
        }

        for (char c : chars) {
            if (c == '"') {
                countQuota++;
            }
        }

        if (countQuota == 0 || (countQuota % 2) > 0) {
            throw new UnsupportedOperationException("All values must be envolved by quotes");
        }

        List<ScoredValue<String>> values = new ArrayList<>();
        Double score = null;
        int count = 0;
        StringBuffer auxStr = new StringBuffer();

        for (char c : chars) {
            // Getting score numbers
            if (c != '"' && score == null) {
                if (c == '.' || c == ',') {
                    auxStr.append('.');
                } else if (c != ' ') {
                    auxStr.append(c);
                }
            } else { // Getting value
                if (score == null) {
                    score = TryParse.toDouble(auxStr.toString());
                    if (score == null) {
                        throw new UnsupportedOperationException("Invalid score value informed: " + auxStr);
                    }
                    auxStr = new StringBuffer();
                }

                if (c == '"') count++;
                else auxStr.append(c);

                if (count == 2) {
                    values.add(ScoredValue.just(score, auxStr.toString().trim()));
                    auxStr = new StringBuffer();
                    score = null;
                    count = 0;
                }
            }
        }

        ScoredValue<String>[] valuesArr = new ScoredValue[values.size()];
        valuesArr = values.toArray(valuesArr);

        return valuesArr;
    }

    /**
     * Validate if command argumments contains a valid key
     *
     * @param commandArgumments
     * @return true if is ok, otherwise, returns false
     */
    public static boolean ValidateKey(String[] commandArgumments) {
        return (commandArgumments[1] != null && !commandArgumments[1].trim().equals(""));
    }

    /**
     * Validate if command argumments contains valids key and value
     *
     * @param commandArgumments
     * @return true if is ok, otherwise, returns false
     */
    private static boolean ValidateKeyValue(String[] commandArgumments) {
        return ValidateKey(commandArgumments) &&
                (commandArgumments[2] != null && !commandArgumments[2].equals(""));
    }
}

