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

package com.streamxhub.streamx.flink.connector.influx.sink

import com.streamxhub.streamx.common.util.{ConfigUtils, Utils}
import com.streamxhub.streamx.flink.connector.influx.bean.InfluxEntity
import com.streamxhub.streamx.flink.connector.influx.function.InfluxFunction
import com.streamxhub.streamx.flink.connector.sink.Sink
import com.streamxhub.streamx.flink.core.scala.StreamingContext
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.scala.DataStream

import java.util.Properties
import scala.annotation.meta.param

object InfluxSink {

  def apply(@(transient@param)
            property: Properties = new Properties(),
            parallelism: Int = 0,
            name: String = null,
            uid: String = null)(implicit ctx: StreamingContext): InfluxSink = new InfluxSink(ctx, property, parallelism, name, uid)

}

class InfluxSink(@(transient@param) ctx: StreamingContext,
                 property: Properties = new Properties(),
                 parallelism: Int = 0,
                 name: String = null,
                 uid: String = null) extends Sink {

  def sink[T](stream: DataStream[T], alias: String = "")(implicit entity: InfluxEntity[T]): DataStreamSink[T] = {
    val prop = ConfigUtils.getInfluxConfig(ctx.parameter.toMap)(alias)
    Utils.copyProperties(property, prop)
    val sinkFun = new InfluxFunction[T](prop)
    val sink = stream.addSink(sinkFun)
    afterSink(sink, parallelism, name, uid)
  }

}
