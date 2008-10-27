/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.Converter123;
import ch.qos.logback.core.pattern.ConverterHello;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;


public class CompilerTest extends TestCase {

  Map<String, String> converterMap = new HashMap<String, String>();
  Context context = new ContextBase();
  
  public CompilerTest(String arg0) {
    super(arg0);
    converterMap.put("OTT", Converter123.class.getName());
    converterMap.put("hello", ConverterHello.class.getName());
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  String write(final Converter<Object> head, Object event) {
    StringBuffer buf = new StringBuffer();
    Converter<Object> c = head;
    while (c != null) {
      c.write(buf, event);
      c = c.getNext();
    }
    return buf.toString();
  }

  public void testLiteral() throws Exception {
    Parser<Object> p = new Parser<Object>("hello");
    Node t = p.parse();
    Converter<Object> head = p.compile(t, converterMap);
    String result = write(head, new Object());
    assertEquals("hello", result);
  }

  public void testBasic() throws Exception {
    {
      Parser<Object> p = new Parser<Object>("abc %hello");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc Hello", result); 
    }
    {
      Parser<Object> p = new Parser<Object>("abc %hello %OTT");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc Hello 123", result);
    }
  }

  public void testFormat() throws Exception {
    {
      Parser<Object> p = new Parser<Object>("abc %7hello");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc   Hello", result);
    }

    {
      Parser<Object> p = new Parser<Object>("abc %-7hello");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc Hello  ", result);
    }

    {
      Parser<Object> p = new Parser<Object>("abc %.3hello");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc llo", result);
    }

    {
      Parser<Object> p = new Parser<Object>("abc %.-3hello");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc Hel", result);
    }

    {
      Parser<Object> p = new Parser<Object>("abc %4.5OTT");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc  123", result);
    }
    {
      Parser<Object> p = new Parser<Object>("abc %-4.5OTT");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc 123 ", result);
    }
    {
      Parser<Object> p = new Parser<Object>("abc %3.4hello");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc ello", result);
    }
    {
      Parser<Object> p = new Parser<Object>("abc %-3.-4hello");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("abc Hell", result);
    }
  }

  public void testComposite() throws Exception {
//    {
//      Parser<Object> p = new Parser<Object>("%(ABC)");
//      p.setContext(context);
//      Node t = p.parse();
//      Converter<Object> head = p.compile(t, converterMap);
//      String result = write(head, new Object());
//      assertEquals("ABC", result);
//    }
    {
      Context c = new ContextBase();
      Parser<Object> p = new Parser<Object>("%(ABC %hello)");
      p.setContext(c);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      StatusPrinter.print(c);
      assertEquals("ABC Hello", result);
    }
    {
      Parser<Object> p = new Parser<Object>("%(ABC %hello)");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("ABC Hello", result);
    }
  }

  public void testCompositeFormatting() throws Exception {
    {
      Parser<Object> p = new Parser<Object>("xyz %4.10(ABC)");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("xyz  ABC", result);
    }

    {
      Parser<Object> p = new Parser<Object>("xyz %-4.10(ABC)");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("xyz ABC ", result);
    }

    {
      Parser<Object> p = new Parser<Object>("xyz %.2(ABC %hello)");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("xyz lo", result);
    }

    {
      Parser<Object> p = new Parser<Object>("xyz %.-2(ABC)");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("xyz AB", result);
    }

    {
      Parser<Object> p = new Parser<Object>("xyz %30.30(ABC %20hello)");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("xyz       ABC                Hello", result);
    }
  }

  public void testUnknownWord() throws Exception {
    Parser<Object> p = new Parser<Object>("%unknown");
    p.setContext(context);
    Node t = p.parse();
    p.compile(t, converterMap);
    StatusChecker chercker = new StatusChecker(context.getStatusManager());
    assertTrue(chercker
        .containsMatch("\\[unknown] is not a valid conversion word"));
  }

  public void testWithNopEscape() throws Exception {
    {
      Parser<Object> p = new Parser<Object>("xyz %hello\\_world");
      p.setContext(context);
      Node t = p.parse();
      Converter<Object> head = p.compile(t, converterMap);
      String result = write(head, new Object());
      assertEquals("xyz Helloworld", result);
    }
  }

}
