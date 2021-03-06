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

package com.orgsync.oskr.events.messages

import java.time.Instant
import java.util.UUID

import com.orgsync.oskr.events.messages.parts.{ChannelAddress, Recipient, TemplateSet}
import com.orgsync.oskr.events.streams.deliveries.TemplateCache
import org.threeten.extra.Interval

final case class Digest(
  id          : UUID,
  emittedAt   : Instant,
  senderIds   : Set[String],
  recipient   : Recipient,
  sentInterval: Interval,
  tags        : Set[String],
  templates   : TemplateSet,
  partIds     : Set[String],
  messages    : List[Message]
) extends Deliverable {
  override def delivery(
    address: ChannelAddress,
    at     : Instant,
    cache  : TemplateCache
  ): Option[Delivery] = {
    val content = templates.renderDigest(address, id, messages, cache)
    content.map(c => {
      Delivery(
        id, address.channel, senderIds, sentInterval, recipient.id,
        at, Instant.now, tags, partIds, c
      )
    })
  }
}
