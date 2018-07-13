package com.vn.infrastructure.cache.redis;

import com.vn.util.TryParse;
import io.lettuce.core.KeyValue;
import io.lettuce.core.protocol.CommandType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RedisCommandBuilder {
    private CommandType command;
    private Object key;
    private Object value;
    private KeyValue keyValue;
    private String commandArgs;
    private Map.Entry<Double, Object>[] scoredValues;
    private Long expiration, startPos, stopPos;
    private boolean withScores;
    private Object[] keysToDelete;

    public RedisCommandBuilder(String commandStr) throws IllegalArgumentException {
        this.extractCommandParameters(commandStr);
    }

    @Nullable
    public Object getValue() {
        if (this.value == null) this.value = this.commandArgs;
        return this.value;
    }

    @Nullable
    public Object getKey() {
        if (this.key != null && this.key.equals("")) this.key = null;
        return this.key;
    }

    /**
     * @return Key value, if possible, otherwise, returns null
     */
    @Nullable
    public KeyValue<Object, Object> getKeyValue() {
        if (this.keyValue == null
                && (this.key != null && !this.key.equals(""))
                && (this.getValue() != null && !this.value.equals(""))) {
            this.keyValue = KeyValue.just(this.key, this.getValue());
        }
        return this.keyValue;
    }

    @NotNull
    public CommandType getCommand() {
        return this.command;
    }

    /**
     * @return An array of ScoreValues to use in ZADD function. Be aware, it can be EMPTY
     * @throws UnsupportedOperationException This exception is throwed if was not possible to exctract a ScoredValue
     *                                       array from command argumments
     */
    @NotNull
    public Map.Entry<Double, Object>[] getScoredValues() throws UnsupportedOperationException {
        if (this.scoredValues == null) this.exctractScoredValues();

        return this.scoredValues;
    }

    @NotNull
    public Long getExpiration() {
        if (this.expiration == null) this.expiration = 0L;
        return this.expiration;
    }

    @Nullable
    public Long getStartPos() {
        return this.startPos;
    }

    @Nullable
    public Long getStopPos() {
        return this.stopPos;
    }

    @Nullable
    public Object[] getKeysToDelete() {
        return keysToDelete;
    }

    public boolean isWithScores() {
        return withScores;
    }

    /**
     * Separate the command entries command in 3 parameters: command, key and arguments
     *
     * @param text Input text with the fully command
     */
    private void extractCommandParameters(String text) throws IllegalArgumentException {
        StringBuffer auxStr = new StringBuffer();
        int count = 0;

        for (char c : text.toCharArray()) {
            count++;
            if (this.command == null) { //Extract command, first word before next space
                if (c != ' ') {
                    auxStr.append(c);
                } else { //Throws the IllegalArgumentException because the command are mandatory.
                    this.command = CommandType.valueOf(auxStr.toString().toUpperCase().trim());
                    auxStr.delete(0, auxStr.length());
                    continue;
                }
                //Last array element without complete command
                if (count == text.length()) {
                    this.command = CommandType.valueOf(auxStr.toString().toUpperCase().trim());
                    auxStr.delete(0, auxStr.length());
                }
            } else if (this.key == null) { //Extract key, second word before next space
                if (c != ' ') {
                    auxStr.append(c);
                } else {
                    this.key = auxStr.toString().trim();
                    auxStr.delete(0, auxStr.length());
                    continue;
                }
                //Last array element without complete key
                if (count == text.length()) {
                    this.key = auxStr.toString().trim();
                    auxStr.delete(0, auxStr.length());
                }
            } else {
                auxStr.append(c); //Extract the rest of command argumments.
            }
        }

        this.commandArgs = auxStr.toString().trim();
        auxStr.delete(0, auxStr.length());

        switch (this.command) {
            case SETEX: //Extract expiration
                for (char c : this.commandArgs.toCharArray()) {
                    if (this.expiration == null) {
                        this.expiration = TryParse.toLong(String.valueOf(c));

                        if (this.expiration != null) {
                            auxStr.append(c);
                            this.expiration = null;
                        } else {
                            this.expiration = TryParse.toLong(auxStr.toString());
                            auxStr.delete(0, auxStr.length());
                        }
                    } else {
                        auxStr.append(c);
                    }
                }

                this.commandArgs = auxStr.toString().trim();
                break;
            case DEL: //Extract keys to delete
                List<Object> auxList = new ArrayList<>();
                auxList.add(this.key);

                for (String s : this.commandArgs.split("\\s")) {
                    auxList.add(s.trim());
                }

                this.keysToDelete = new Object[auxList.size()];
                this.keysToDelete = auxList.toArray(this.keysToDelete);
                break;
            case ZRANGE: //Extract startPos, stop and WITHSCORES
                String[] startStop = this.commandArgs.trim().split("\\s");

                if (startStop != null && startStop.length >= 2) {
                    this.startPos = TryParse.toLong(startStop[0].trim());
                    this.stopPos = TryParse.toLong(startStop[1].trim());
                }

                this.withScores = startStop.length >= 3 && startStop[2].trim().toUpperCase().equals("WITHSCORES");
                break;
        }
    }

    /**
     * Mount a valid array of ScoredValues to use on ZADD command
     * - Text must start with a valid number score.
     * - Values must be envolved by quotes, something like "MyValue"
     * - Example of valid text to convert: 1 "one" 2 "two" 3 "three"
     *
     * @return An array of valid ScoredValues
     * @throws UnsupportedOperationException If some validadtion was not OK
     */
    private void exctractScoredValues() throws UnsupportedOperationException {
        List<Map.Entry<Double, Object>> values = new ArrayList<>();
        Double score = null;
        StringBuffer auxStr = new StringBuffer();

        //Members envolved by quotes, Example: ZADD worldcup 1 "Brasil Yellow and Green" 2 "Alemanha is cold"
        if (this.commandArgs.contains("\"")) {
            char[] chars = this.commandArgs.toCharArray();

            if (chars.length <= 0) {
                this.scoredValues = new Map.Entry[0];
                return;
            }

            int count = 0;

            for (char c : chars) {
                // Getting score numbers
                if (c != '"' && score == null) {
                    if (c == '.' || c == ',') {
                        auxStr.append('.');
                    } else if (c != ' ') {
                        auxStr.append(c);
                    }
                } else { //Getting values
                    if (score == null) { //It guarantee that it occurs only once
                        score = TryParse.toDouble(auxStr.toString());

                        if (score == null) //Scores is mandatory, without that a exception is throwed
                            throw new UnsupportedOperationException("Invalid score value informed: " + auxStr);

                        auxStr.delete(0, auxStr.length());
                    }

                    if (c == '"') count++;
                    else auxStr.append(c);

                    if (count == 2) { //A valid member, reset aux variables for the next interation
                        if (auxStr.toString().trim().length() <= 0)
                            throw new UnsupportedOperationException("Can't insert empty or blank elements");

                        values.add(new AbstractMap.SimpleEntry<>(score, auxStr.toString().trim()));
                        auxStr.delete(0, auxStr.length());
                        score = null;
                        count = 0;
                    }
                }
            }
        } else { //Member separated by spaces. Ex: ZADD worldcup 1 France 2 UnitedKingdom 3 Nigery
            String[] auxArr = this.commandArgs.split("\\s");

            if (auxArr.length % 2 != 0)
                throw new UnsupportedOperationException("All menbers must to have a score and value [score value]");

            for (int i = 0; i < auxArr.length; i++) {
                auxStr.delete(0, auxStr.length());
                auxStr.append(auxArr[i].trim());

                if (i % 2 == 0) { //Even elements are score and odd are members
                    score = TryParse.toDouble(auxStr.toString().replace(',', '.'));

                    if (score == null)
                        throw new UnsupportedOperationException("Invalid score informed: " + auxStr.toString());
                } else {
                    values.add(new AbstractMap.SimpleEntry<>(score, auxStr.toString().trim()));
                }
            }
        }

        this.scoredValues = new Map.Entry[values.size()];
        this.scoredValues = values.toArray(this.scoredValues);
    }
}
