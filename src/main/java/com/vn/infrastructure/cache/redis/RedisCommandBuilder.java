package com.vn.infrastructure.cache.redis;

import com.vn.util.TryParse;
import io.lettuce.core.KeyValue;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.protocol.CommandType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RedisCommandBuilder {
    private CommandType command;
    private Object key;
    private Object value;
    private KeyValue keyValue;
    private String commandArgs = "";
    private ScoredValue<Object>[] scoredValues;
    private Long expiration, startPos, stopPos;
    private boolean withScores;
    private Object[] keysToDelete;

    public RedisCommandBuilder(String commandStr) throws IllegalArgumentException {
        this.extractCommandParameters(commandStr);
    }

    @Nullable
    private Object getValue() {
        if (this.value == null) this.value = this.commandArgs;
        return this.value;
    }

    @Nullable
    public Object getKey() {
        if (this.key != null && this.key.equals("")) this.key = null;
        return this.key;
    }

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

    @NotNull
    public ScoredValue<Object>[] getScoredValues() throws UnsupportedOperationException {
        if (this.scoredValues == null) this.exctractScoredValues();

        return this.scoredValues;
    }

    @NotNull
    public Long getExpiration() {
        if (this.expiration == null) this.expiration = 0L;
        return this.expiration;
    }

    @NotNull
    public Long getStartPos() {
        if (this.startPos == null) this.startPos = 0L;
        return this.startPos;
    }

    @NotNull
    public Long getStopPos() {
        if (this.stopPos == null) this.stopPos = 0L;
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
            if (this.command == null) {
                if (c != ' ') {
                    auxStr.append(c);
                } else {
                    this.command = CommandType.valueOf(auxStr.toString().toUpperCase().trim());
                    auxStr = new StringBuffer();
                    continue;
                }
                //Last array element without complete command
                if (count == text.length()) {
                    this.command = CommandType.valueOf(auxStr.toString().toUpperCase().trim());
                    auxStr = new StringBuffer();
                }
            } else if (this.key == null) {
                if (c != ' ') {
                    auxStr.append(c);
                } else {
                    this.key = auxStr.toString();
                    auxStr = new StringBuffer();
                    continue;
                }
                //Last array element without complete key
                if (count == text.length()) {
                    this.key = auxStr.toString();
                    auxStr = new StringBuffer();
                }
            } else {
                auxStr.append(c);
            }
        }

        this.commandArgs = auxStr.toString();

        switch (this.command) {
            case SETEX: //Extract expiration
                auxStr = new StringBuffer();

                for (char c : this.commandArgs.toCharArray()) {
                    if (this.expiration == null) {
                        this.expiration = TryParse.toLong(String.valueOf(c));

                        if (this.expiration != null) {
                            auxStr.append(c);
                            this.expiration = null;
                        } else {
                            this.expiration = TryParse.toLong(auxStr.toString());
                            auxStr = new StringBuffer();
                        }
                    } else {
                        auxStr.append(c);
                    }
                }

                this.commandArgs = auxStr.toString();
                break;
            case DEL: //Extract keys to delete
                List<Object> auxList = new ArrayList<>();
                auxList.add(this.key);

                if (this.commandArgs != null) {
                    for (String s : this.commandArgs.split("\\s")) {
                        auxList.add(s.trim());
                    }
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

        this.commandArgs = this.commandArgs.trim();
        if (this.key != null) this.key = this.key.toString().trim();
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
        char[] chars = this.commandArgs.toCharArray();
        int countQuota = 0;

        if (chars.length <= 0) {
            this.scoredValues = new ScoredValue[0];
            return;
        }

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
            } else { // Getting values
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
                    if (auxStr.toString().trim().length() <= 0)
                        throw new UnsupportedOperationException("Cant insert empty or blank elements");
                    values.add(ScoredValue.just(score, auxStr.toString().trim()));
                    auxStr = new StringBuffer();
                    score = null;
                    count = 0;
                }
            }
        }

        ScoredValue<Object>[] valuesArr = new ScoredValue[values.size()];
        this.scoredValues = values.toArray(valuesArr);
    }
}
