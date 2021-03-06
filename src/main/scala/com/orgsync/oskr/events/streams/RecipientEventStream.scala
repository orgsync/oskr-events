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

package com.orgsync.oskr.events.streams

import java.time.{Duration, Instant}

import com.orgsync.oskr.events.messages._
import com.orgsync.oskr.events.streams.recipient_events.{SerializeRecipientEvent, UnreadApproximation, UnreadState}
import com.orgsync.oskr.events.windows.OldestSlidingWindowTrigger
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{DataStream, SplitStream}
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.threeten.extra.Interval

class RecipientEventStream(configuration: Configuration) {
  private val kafkaTopic = configuration.getString("kafkaRecipientEventTopic",
    "Communications.Events.Recipients")

  private val initialHLLState = UnreadState(
    "", "", Interval.of(Instant.MIN, Instant.MIN), UnreadApproximation()
  )

  private val foldHLLFn = (
    state           : UnreadState,
    deliverableEvent: Either[Send, Read]
  ) => {
    val mergedEvent = deliverableEvent.merge
    val lastEvent = mergedEvent match {
      case _: Send => "send"
      case _: Read => "read"
    }

    state.unread.add(mergedEvent)

    val oldStart = state.interval.getStart
    val oldEnd = state.interval.getEnd

    val start =
      if (oldStart == Instant.MIN)
        mergedEvent.at
      else if (oldStart.isBefore(mergedEvent.at))
        oldStart
      else
        mergedEvent.at

    val end =
      if (oldEnd == Instant.MIN)
        mergedEvent.at
      else if (oldEnd.isAfter(mergedEvent.at))
        oldEnd
      else
        mergedEvent.at

    UnreadState(
      mergedEvent.recipientId, lastEvent, Interval.of(start, end), state.unread
    )
  }

  def getStream(
    deliverableEvents: SplitStream[Either[Send, Read]],
    configuration    : Configuration
  ): DataStream[RecipientEvent] = {
    val unreadTime = Time.milliseconds(
      Duration
        .parse(configuration.getString("unreadTime", "PT168H"))
        .toMillis
    )

    val unreadSlide = Time.milliseconds(
      Duration
        .parse(configuration.getString("unreadSlide", "PT24H"))
        .toMillis
    )

    val eventTrigger = new OldestSlidingWindowTrigger[Either[Send, Read], TimeWindow](
      unreadTime, unreadSlide
    )

    val events = deliverableEvents
      .keyBy(_.merge.recipientId)
      .window(SlidingProcessingTimeWindows.of(unreadTime, unreadSlide))
      .trigger(eventTrigger)
      .fold(initialHLLState)(foldHLLFn).name("unread_window")
      .uid("unread_approximations")
      .map(_.toRecipientEvent).name("unread")

    events
      .map(new SerializeRecipientEvent)
      .addSink(new KafkaSink(configuration).sink(kafkaTopic))
      .name("recipient_event_sink")

    events
  }
}
