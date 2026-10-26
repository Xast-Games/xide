package org.xast.xide.core.log

import java.time.LocalDateTime

trait LogService:

   def log(
      level: LogLevel,
      message: String,
      cause: Option[Throwable],
   ): Unit

   def info(message: String): Unit =
      log(LogLevel.Info, message, None)

   def warn(message: String): Unit =
      log(LogLevel.Warn, message, None)

   def error(message: String): Unit =
      log(LogLevel.Error, message, None)

   def error(message: String, cause: Throwable): Unit =
      log(LogLevel.Error, message, Some(cause))

/** Application-wide log. Keeps the latest entries in memory for viewers like the Logs plugin. */
object Log extends LogService:

   private val capacity: Int = 1000
   private var entries: List[LogEntry] = Nil
   private var listeners: List[() => Unit] = Nil

   override def log(
      level: LogLevel,
      message: String,
      cause: Option[Throwable]
   ): Unit =
      val entry = LogEntry(
         timestamp = LocalDateTime.now(),
         level     = level,
         message   = message,
         cause     = cause
      )

      val currentListeners = synchronized {
         entries = (entry :: entries).take(capacity)
         listeners
      }

      printToConsole(entry)
      currentListeners.foreach(_())

   def clear(): Unit =
      val currentListeners = synchronized {
         entries = Nil
         listeners
      }

      currentListeners.foreach(_())

   /** Newest entry first. */
   def logEntries: List[LogEntry] = synchronized {
      entries
   }

   def addListener(listener: () => Unit): Unit = synchronized {
      listeners = listener :: listeners
   }

   def removeListener(listener: () => Unit): Unit = synchronized {
      listeners = listeners.filter(_ != listener)
   }

   // Same console output as the old Debug utility
   private def printToConsole(entry: LogEntry): Unit =
      val stream = if entry.level == LogLevel.Error then System.err else System.out
      stream.println(s"[${entry.level.toString.toUpperCase}] ${entry.message}")
      entry.cause.foreach(_.printStackTrace(stream))
