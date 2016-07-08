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

package com.orgsync.oskr.events.messages.events

import org.json4s._

sealed trait EventType {
  def name: String
}

case object Delivery extends EventType {
  val name = "delivery"
}

case object Acknowledgement extends EventType {
  val name = "acknowledgement"
}

case object Failure extends EventType {
  val name = "failure"
}

object EventTypeSerializer extends CustomSerializer[EventType](f => ( {
  case JString(Delivery.name) => Delivery
  case JString(Acknowledgement.name) => Acknowledgement
  case JString(Failure.name) => Failure
}, {
  case eventType: EventType => JString(eventType.name)
}))
