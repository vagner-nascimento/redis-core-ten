package com.vn.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TryParseTest {
    private String notEmptyString;
    private Long longValue;
    private Double doubleValue;
    private Float floatValue;
    private Integer integerValue;
    private Character charValue;
    private Byte byteValue;

    @Before
    public void startParameter() {
        this.notEmptyString = "Hi, i'm a simple string";
        this.longValue = Long.MAX_VALUE;
        this.doubleValue = Double.MAX_VALUE;
        this.floatValue = Float.MAX_VALUE;
        this.integerValue = Integer.MAX_VALUE;
        this.charValue = Character.MAX_VALUE;
        this.byteValue = Byte.MAX_VALUE;
    }

    @Test
    public void testToLong() {
        Long l;

        l = TryParse.toLong("");
        Assert.assertNull(l);

        l = TryParse.toLong(null);
        Assert.assertNull(l);

        l = TryParse.toLong(String.valueOf(this.longValue));
        Assert.assertNotNull(l);

        l = TryParse.toLong(String.valueOf(this.doubleValue));
        Assert.assertNull(l);

        l = TryParse.toLong(String.valueOf(this.floatValue));
        Assert.assertNull(l);

        l = TryParse.toLong(String.valueOf(this.integerValue));
        Assert.assertNotNull(l);

        l = TryParse.toLong(String.valueOf(true));
        Assert.assertNull(l);

        l = TryParse.toLong(String.valueOf(this.notEmptyString));
        Assert.assertNull(l);

        l = TryParse.toLong(String.valueOf(this.charValue));
        Assert.assertNull(l);

        l = TryParse.toLong(String.valueOf(this.byteValue));
        Assert.assertNotNull(l);
    }

    @Test
    public void testToDouble() {
        Double d;

        d = TryParse.toDouble("");
        Assert.assertNull(d);

        d = TryParse.toDouble(null);
        Assert.assertNull(d);

        d = TryParse.toDouble(String.valueOf(this.longValue));
        Assert.assertNotNull(d);

        d = TryParse.toDouble(String.valueOf(this.doubleValue));
        Assert.assertNotNull(d);

        d = TryParse.toDouble(String.valueOf(this.floatValue));
        Assert.assertNotNull(d);

        d = TryParse.toDouble(String.valueOf(this.integerValue));
        Assert.assertNotNull(d);

        d = TryParse.toDouble(String.valueOf(true));
        Assert.assertNull(d);

        d = TryParse.toDouble(String.valueOf(this.notEmptyString));
        Assert.assertNull(d);

        d = TryParse.toDouble(String.valueOf(this.charValue));
        Assert.assertNull(d);

        d = TryParse.toDouble(String.valueOf(this.byteValue));
        Assert.assertNotNull(d);
    }

    @Test
    public void testToInteger() {
        Integer i;

        i = TryParse.toInteger("");
        Assert.assertNull(i);

        i = TryParse.toInteger(null);
        Assert.assertNull(i);

        i = TryParse.toInteger(String.valueOf(this.longValue));
        Assert.assertNull(i);

        i = TryParse.toInteger(String.valueOf(this.doubleValue));
        Assert.assertNull(i);

        i = TryParse.toInteger(String.valueOf(this.floatValue));
        Assert.assertNull(i);

        i = TryParse.toInteger(String.valueOf(this.integerValue));
        Assert.assertNotNull(i);

        i = TryParse.toInteger(String.valueOf(true));
        Assert.assertNull(i);

        i = TryParse.toInteger(String.valueOf(this.notEmptyString));
        Assert.assertNull(i);

        i = TryParse.toInteger(String.valueOf(this.charValue));
        Assert.assertNull(i);

        i = TryParse.toInteger(String.valueOf(this.byteValue));
        Assert.assertNotNull(i);
    }
}
