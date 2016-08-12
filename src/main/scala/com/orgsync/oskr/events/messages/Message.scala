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

import com.orgsync.oskr.events.messages.parts.{ChannelAddress, DigestSpecification, Recipient, TemplateSet}
import com.orgsync.oskr.events.streams.deliveries.TemplateCache
import org.json4s.JsonAST.JArray
import org.threeten.extra.Interval

final case class Message(
  id          : UUID,
  senderIds   : Set[String],
  recipient   : Recipient,
  channels    : List[ChannelAddress],
  sentInterval: Interval,
  tags        : Set[String],
  digest      : Option[DigestSpecification],
  templates   : TemplateSet,
  partIds     : Set[String],
  partData    : JArray
) extends Deliverable {
  override def delivery(address: ChannelAddress, cache: TemplateCache): Option[Delivery] = {
    val content = templates.renderBase(address, this, cache)
    content.flatMap(c => {
      val deliveryId = address.deliveryId
      val deliveredAt = Instant.now()

      deliveryId.map(id => Delivery(
        id, address.channel, senderIds, sentInterval, recipient.id, deliveredAt, tags,
        partIds, c
      ))
    })
  }

  def digestKey: String = this.digest.map(_.key).getOrElse("default")

  def toDigest: Digest = {
    val idSource = digestKey + this.id.toString
    val digestId = UUID.nameUUIDFromBytes(idSource.getBytes())

    Digest(digestId, senderIds, recipient, channels, sentInterval, tags,
      templates, partIds, List(this)
    )
  }
}
