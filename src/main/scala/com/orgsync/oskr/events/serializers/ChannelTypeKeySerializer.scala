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

package com.orgsync.oskr.events.serializers

import com.orgsync.oskr.events.messages.parts.ChannelType
import org.json4s.{CustomKeySerializer, DefaultFormats, _}

object ChannelTypeKeySerializer extends CustomKeySerializer[ChannelType](f => ( {
  case s: String => {
    implicit val formats = DefaultFormats + ChannelTypeSerializer
    JString(s).extract[ChannelType]
  }
}, {
  case x: ChannelType => x.name
}))
