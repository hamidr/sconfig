/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.{lang => jl}

final class FromMapMode private (name: String, ordinal: Int)
    extends jl.Enum[FromMapMode](name, ordinal)

object FromMapMode {

  final val KEYS_ARE_PATHS = new FromMapMode("KEYS_ARE_PATHS", 0)
  final val KEYS_ARE_KEYS  = new FromMapMode("KEYS_ARE_KEYS", 1)

  private[this] final val _values: Array[FromMapMode] =
    Array(KEYS_ARE_PATHS, KEYS_ARE_KEYS)

  def values: Array[FromMapMode] = _values.clone()

  def valueOf(name: String): FromMapMode =
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException("No enum const FromMapMode." + name)
    }
}
