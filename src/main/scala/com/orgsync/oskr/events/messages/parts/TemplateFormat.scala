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

package com.orgsync.oskr.events.messages.parts

import org.json4s._

sealed trait TemplateFormat {
  def name: String
  def delivery(data: JArray): JValue
}

case object Handlebars extends TemplateFormat {
  val name = "handlebars"

  override def delivery(data: JArray): JValue = JString("")
}

case object PassThrough extends TemplateFormat {
  val name = "none"

  override def delivery(data: JArray): JValue = data
}

object TemplateFormatSerializer extends CustomSerializer[TemplateFormat](f => ( {
  case JString(Handlebars.name) => Handlebars
  case JString(PassThrough.name) => PassThrough
}, {
  case templateFormat: TemplateFormat => JString(templateFormat.name)
}))
