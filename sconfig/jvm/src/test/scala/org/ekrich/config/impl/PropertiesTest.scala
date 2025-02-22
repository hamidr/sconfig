/**
 *   Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._
import java.util.{Date, Properties}
import scala.jdk.CollectionConverters._
import org.ekrich.config.Config
import org.ekrich.config.ConfigParseOptions
import org.ekrich.config.ConfigFactory
import org.ekrich.config.ConfigException
import org.ekrich.config.ConfigResolveOptions

class PropertiesTest extends TestUtils {
  @Test
  def pathSplitting(): Unit = {
    def last(s: String)       = PropertiesParser.lastElement(s)
    def exceptLast(s: String) = PropertiesParser.exceptLastElement(s)

    assertEquals("a", last("a"))
    assertNull(exceptLast("a"))

    assertEquals("b", last("a.b"))
    assertEquals("a", exceptLast("a.b"))

    assertEquals("c", last("a.b.c"))
    assertEquals("a.b", exceptLast("a.b.c"))

    assertEquals("", last(""))
    assertNull(null, exceptLast(""))

    assertEquals("", last("."))
    assertEquals("", exceptLast("."))

    assertEquals("", last(".."))
    assertEquals(".", exceptLast(".."))

    assertEquals("", last("..."))
    assertEquals("..", exceptLast("..."))
  }

  @Test
  def pathObjectCreating(): Unit = {
    def p(key: String) = PropertiesParser.pathFromPropertyKey(key)

    assertEquals(path("a"), p("a"))
    assertEquals(path("a", "b"), p("a.b"))
    assertEquals(path(""), p(""))
  }

  @Test
  def funkyPathsInProperties(): Unit = {
    def testPath(propsPath: String, confPath: String): Unit = {
      val props = new Properties()

      props.setProperty(propsPath, propsPath)

      val conf =
        ConfigFactory.parseProperties(props, ConfigParseOptions.defaults)

      assertEquals(propsPath, conf.getString(confPath))
    }

    // the easy ones
    testPath("x", "x")
    testPath("y.z", "y.z")
    testPath("q.r.s", "q.r.s")

    // weird empty path element stuff
    testPath("", "\"\"")
    testPath(".", "\"\".\"\"")
    testPath("..", "\"\".\"\".\"\"")
    testPath("a.", "a.\"\"")
    testPath(".b", "\"\".b")

    // quotes in .properties
    testPath("\"", "\"\\\"\"")
  }

  @Test
  def objectsWinOverStrings(): Unit = {
    val props = new Properties()

    props.setProperty("a.b", "foo")
    props.setProperty("a", "bar")

    props.setProperty("x", "baz")
    props.setProperty("x.y", "bar")
    props.setProperty("x.y.z", "foo")

    val conf =
      ConfigFactory.parseProperties(props, ConfigParseOptions.defaults)

    assertEquals(2, conf.root.size())
    assertEquals("foo", conf.getString("a.b"))
    assertEquals("foo", conf.getString("x.y.z"))
  }

  @Test
  def makeListWithNumericKeys(): Unit = {

    val props = new Properties()
    props.setProperty("a.0", "0")
    props.setProperty("a.1", "1")
    props.setProperty("a.2", "2")
    props.setProperty("a.3", "3")
    props.setProperty("a.4", "4")

    val conf =
      ConfigFactory.parseProperties(props, ConfigParseOptions.defaults)
    val reference = ConfigFactory.parseString("{ a : [0,1,2,3,4] }")
    assertEquals(Seq(0, 1, 2, 3, 4), conf.getIntList("a").asScala.toSeq)
    conf.checkValid(reference)
  }

  @Test
  def makeListWithNumericKeysWithGaps(): Unit = {

    val props = new Properties()
    props.setProperty("a.1", "0")
    props.setProperty("a.2", "1")
    props.setProperty("a.4", "2")

    val conf =
      ConfigFactory.parseProperties(props, ConfigParseOptions.defaults)
    val reference = ConfigFactory.parseString("{ a : [0,1,2] }")
    assertEquals(Seq(0, 1, 2), conf.getIntList("a").asScala.toSeq)
    conf.checkValid(reference)
  }

  @Test
  def makeListWithNumericKeysWithNoise(): Unit = {

    val props = new Properties()
    props.setProperty("a.-1", "-1")
    props.setProperty("a.foo", "-2")
    props.setProperty("a.0", "0")
    props.setProperty("a.1", "1")
    props.setProperty("a.2", "2")
    props.setProperty("a.3", "3")
    props.setProperty("a.4", "4")

    val conf =
      ConfigFactory.parseProperties(props, ConfigParseOptions.defaults)
    val reference = ConfigFactory.parseString("{ a : [0,1,2,3,4] }")
    assertEquals(Seq(0, 1, 2, 3, 4), conf.getIntList("a").asScala.toSeq)
    conf.checkValid(reference)
  }

  @Test
  def noNumericKeysAsListFails(): Unit = {

    val props = new Properties()
    props.setProperty("a.bar", "0")

    val conf =
      ConfigFactory.parseProperties(props, ConfigParseOptions.defaults)
    val e = intercept[ConfigException.WrongType] {
      conf.getList("a")
    }
    assertTrue("expected exception thrown", e.getMessage.contains("LIST"))
  }

  @Test
  def makeListWithNumericKeysAndMerge(): Unit = {

    val props = new Properties()
    props.setProperty("a.0", "0")
    props.setProperty("a.1", "1")
    props.setProperty("a.2", "2")

    val conf1 =
      ConfigFactory.parseProperties(props, ConfigParseOptions.defaults)
    assertEquals(Seq(0, 1, 2), conf1.getIntList("a").asScala.toSeq)

    val conf2     = ConfigFactory.parseString("""
                a += 3
                a += 4
                a = ${a} [ 5, 6 ]
                a = [-2, -1] ${a}
                """)
    val conf      = conf2.withFallback(conf1).resolve()
    val reference = ConfigFactory.parseString("{ a : [-2,-1,0,1,2,3,4,5,6] }")

    assertEquals(
      Seq(-2, -1, 0, 1, 2, 3, 4, 5, 6),
      conf.getIntList("a").asScala.toSeq
    )
    conf.checkValid(reference)
  }

  @Test
  def skipNonStringsInProperties(): Unit = {
    val props = new Properties()
    props.put("a", new ThreadLocal[String]())
    props.put("b", new Date())

    val conf = ConfigFactory.parseProperties(props)

    assertEquals(0, conf.root.size())
  }
}
