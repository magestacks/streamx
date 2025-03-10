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

package com.streamxhub.streamx.flink.connector.jdbc.bean

import com.streamxhub.streamx.common.util.Utils

import scala.collection.mutable

case class Transaction(transactionId: String = Utils.uuid(), sql: mutable.MutableList[String] = mutable.MutableList.empty[String], var insertMode: Boolean = true, var invoked: Boolean = false) extends Serializable {
  def +(text: String): Unit = sql += text

  override def toString: String = s"(transactionId:$transactionId,size:${sql.size},insertMode:$insertMode,invoked:$invoked)"
}
