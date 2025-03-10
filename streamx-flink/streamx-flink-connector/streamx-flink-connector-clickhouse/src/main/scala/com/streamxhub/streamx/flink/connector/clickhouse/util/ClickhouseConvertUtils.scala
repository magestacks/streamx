/*
 * Copyright 2019 The StreamX Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.streamxhub.streamx.flink.connector.clickhouse.util

object ClickhouseConvertUtils {

  def convert[T](value: T): String = {
    val buffer = new StringBuilder("(")
    val fields = value.getClass.getDeclaredFields
    fields.foreach(f => {
      f.setAccessible(true)
      val v = f.get(value)
      f.getType.getSimpleName match {
        case "String" => buffer.append(s""""$v",""".stripMargin)
        case _ => buffer.append(s"""$v,""".stripMargin)
      }
    })
    buffer.toString().replaceFirst(",$", ")")
  }
}
