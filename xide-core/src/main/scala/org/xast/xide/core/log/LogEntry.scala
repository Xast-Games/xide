package org.xast.xide.core.log

import java.time.LocalDateTime

case class LogEntry(
   timestamp: LocalDateTime,
   level: LogLevel,
   message: String,
   cause: Option[Throwable],
)
