package ch.qos.logback.core.joran.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.action.ext.IncAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class IncludeActionTest {

  final static String INCLUDE_KEY = "includeKey";
  final static String SUB_FILE_KEY = "subFileKey";
  final static String SECOND_FILE_KEY = "secondFileKey";

  Context context = new ContextBase();
  TrivialConfigurator tc;

  static final String INCLUSION_DIR_PREFIX = "src/test/input/joran/inclusion/";

  static final String TOP_BY_FILE = INCLUSION_DIR_PREFIX + "topByFile.xml";

  static final String SUB_FILE = INCLUSION_DIR_PREFIX + "subByFile.xml";

  static final String MULTI_INCLUDE_BY_FILE = INCLUSION_DIR_PREFIX
      + "multiIncludeByFile.xml";

  static final String SECOND_FILE = INCLUSION_DIR_PREFIX + "second.xml";

  static final String TOP_BY_URL = INCLUSION_DIR_PREFIX + "topByUrl.xml";

  static final String INCLUDE_BY_RESOURCE = INCLUSION_DIR_PREFIX
      + "topByResource.xml";

  static final String INCLUDED_FILE = INCLUSION_DIR_PREFIX + "included.xml";
  static final String URL_TO_INCLUDE = "file:./" + INCLUDED_FILE;

  static final String INVALID = INCLUSION_DIR_PREFIX + "invalid.xml";

  static final String INCLUDED_AS_RESOURCE = "input/joran/inclusion/includedAsResource.xml";

  public IncludeActionTest() {
    HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
    rulesMap.put(new Pattern("x"), new NOPAction());
    rulesMap.put(new Pattern("x/inc"), new IncAction());
    rulesMap.put(new Pattern("x/include"), new IncludeAction());

    tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
  }

  @Before
  public void setUp() throws Exception {
    IncAction.reset();
  }

  @After
  public void tearDown() throws Exception {
    context = null;
    System.clearProperty(INCLUDE_KEY);
    System.clearProperty(SECOND_FILE_KEY);
    System.clearProperty(SUB_FILE_KEY);
  }

  @Test
  public void basicFile() throws JoranException {
    System.setProperty(INCLUDE_KEY, INCLUDED_FILE);
    tc.doConfigure(TOP_BY_FILE);
    verifyConfig(2);
  }

  @Test
  public void basicResource() throws JoranException {
    System.setProperty(INCLUDE_KEY, INCLUDED_AS_RESOURCE);
    tc.doConfigure(INCLUDE_BY_RESOURCE);
    StatusPrinter.print(context);
    verifyConfig(2);
  }

  @Test 
  public void basicURL() throws JoranException {
    System.setProperty(INCLUDE_KEY, URL_TO_INCLUDE);
    tc.doConfigure(TOP_BY_URL);
    StatusPrinter.print(context);
    verifyConfig(2);
  }

  @Test
  public void noFileFound() throws JoranException {
    System.setProperty(INCLUDE_KEY, "toto");
    tc.doConfigure(TOP_BY_FILE);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(FileNotFoundException.class));
  }

  @Test
  public void withCorruptFile() throws JoranException {
    System.setProperty(INCLUDE_KEY, INVALID);
    tc.doConfigure(TOP_BY_FILE);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(SAXParseException.class));
  }

  @Test
  public void malformedURL() throws JoranException {
    System.setProperty(INCLUDE_KEY, "htp://logback.qos.ch");
    tc.doConfigure(TOP_BY_URL);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(MalformedURLException.class));
  }

  @Test
  public void unknownURL() throws JoranException {
    System.setProperty(INCLUDE_KEY, "http://logback2345.qos.ch");
    tc.doConfigure(TOP_BY_URL);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(UnknownHostException.class));
  }

  @Test
  public void nestedInclude() throws JoranException {
    System.setProperty(SUB_FILE_KEY, INCLUDED_FILE);
    System.setProperty(INCLUDE_KEY, SECOND_FILE);
    tc.doConfigure(TOP_BY_FILE);
    StatusPrinter.print(context);
    verifyConfig(1);

  }

  @Test
  public void multiInclude() throws JoranException {
    System.setProperty(INCLUDE_KEY, INCLUDED_FILE);
    System.setProperty(SECOND_FILE_KEY, SECOND_FILE);
    tc.doConfigure(MULTI_INCLUDE_BY_FILE);
    verifyConfig(3);
  }

  @Test
  public void errorInDoBegin() {
    
  }
  
  
  void verifyConfig(int expected) {
    assertEquals(expected, IncAction.beginCount);
    assertEquals(expected, IncAction.endCount);
  }
}
