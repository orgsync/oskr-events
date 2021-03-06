/*
 * Copyright 2016 OrgSync.
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

package com.orgsync.oskr.events.streams.parts

import com.orgsync.oskr.events.messages.Part
import com.orgsync.oskr.events.parsers.PartParser
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector

class ParsePart(parameters: Configuration)
  extends RichFlatMapFunction[String, Part] {

  var parser: PartParser = _

  override def flatMap(json: String, out: Collector[Part]): Unit = {
    parser.parsePart(json).foreach(out.collect)
  }

  override def open(parameters: Configuration): Unit = {
    parser = new PartParser(parameters)
  }
}
