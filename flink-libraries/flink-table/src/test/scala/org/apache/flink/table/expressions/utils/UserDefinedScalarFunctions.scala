/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.expressions.utils

import java.sql.{Date, Time, Timestamp}

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.table.api.Types
import org.apache.flink.table.functions.{ScalarFunction, FunctionContext}
import org.junit.Assert

import scala.collection.mutable
import scala.io.Source

case class SimplePojo(name: String, age: Int)

object Func0 extends ScalarFunction {
  def eval(index: Int): Int = {
    index
  }
}

object Func1 extends ScalarFunction {
  def eval(index: Integer): Integer = {
    index + 1
  }
}

object Func2 extends ScalarFunction {
  def eval(index: Integer, str: String, pojo: SimplePojo): String = {
    s"$index and $str and $pojo"
  }
}

object Func3 extends ScalarFunction {
  def eval(index: Integer, str: String): String = {
    s"$index and $str"
  }
}

object Func4 extends ScalarFunction {
  def eval(): Integer = {
    null
  }
}

object Func5 extends ScalarFunction {
  def eval(): Int = {
    -1
  }
}

object Func6 extends ScalarFunction {
  def eval(date: Date, time: Time, timestamp: Timestamp): (Date, Time, Timestamp) = {
    (date, time, timestamp)
  }
}

object Func7 extends ScalarFunction {
  def eval(a: Integer, b: Integer): Integer = {
    a + b
  }
}

object Func8 extends ScalarFunction {
  def eval(a: Int): String = {
    "a"
  }

  def eval(a: Int, b: Int): String = {
    "b"
  }

  def eval(a: String, b: String): String = {
    "c"
  }
}

object Func9 extends ScalarFunction {
  def eval(a: Int, b: Int, c: Long): String = {
    s"$a and $b and $c"
  }
}

object Func10 extends ScalarFunction {
  def eval(c: Long): Long = {
    c
  }

  override def getResultType(signature: Array[Class[_]]): TypeInformation[_] = {
    Types.TIMESTAMP
  }
}

object Func11 extends ScalarFunction {
  def eval(a: Int, b: Long): String = {
    s"$a and $b"
  }
}

object Func12 extends ScalarFunction {
  def eval(a: Long): Long = {
    a
  }

  override def getResultType(signature: Array[Class[_]]): TypeInformation[_] = {
    Types.INTERVAL_MILLIS
  }
}

class RichFunc0 extends ScalarFunction {
  var openCalled = false
  var closeCalled = false

  override def open(context: FunctionContext): Unit = {
    super.open(context)
    if (openCalled) {
      Assert.fail("Open called more than once.")
    } else {
      openCalled = true
    }
    if (closeCalled) {
      Assert.fail("Close called before open.")
    }
  }

  def eval(index: Int): Int = {
    if (!openCalled) {
      Assert.fail("Open was not called before eval.")
    }
    if (closeCalled) {
      Assert.fail("Close called before eval.")
    }

    index + 1
  }

  override def close(): Unit = {
    super.close()
    if (closeCalled) {
      Assert.fail("Close called more than once.")
    } else {
      closeCalled = true
    }
    if (!openCalled) {
      Assert.fail("Open was not called before close.")
    }
  }
}

class RichFunc1 extends ScalarFunction {
  var added = Int.MaxValue

  override def open(context: FunctionContext): Unit = {
    added = context.getJobParameter("int.value", "0").toInt
  }

  def eval(index: Int): Int = {
    index + added
  }

  override def close(): Unit = {
    added = Int.MaxValue
  }
}

class RichFunc2 extends ScalarFunction {
  var prefix = "ERROR_VALUE"

  override def open(context: FunctionContext): Unit = {
    prefix = context.getJobParameter("string.value", "")
  }

  def eval(value: String): String = {
    prefix + "#" + value
  }

  override def close(): Unit = {
    prefix = "ERROR_VALUE"
  }
}

class RichFunc3 extends ScalarFunction {
  private val words = mutable.HashSet[String]()

  override def open(context: FunctionContext): Unit = {
    val file = context.getCachedFile("words")
    for (line <- Source.fromFile(file.getCanonicalPath).getLines) {
      words.add(line.trim)
    }
  }

  def eval(value: String): Boolean = {
    words.contains(value)
  }

  override def close(): Unit = {
    words.clear()
  }
}
