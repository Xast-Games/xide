package org.xast.xide.logs_plugin

import org.xast.xide.core.log.LogEntry

import java.time.format.DateTimeFormatter
import javax.swing.table.AbstractTableModel

object LogTableModel:
   enum Column(val title: String):
      case Time    extends Column("Time")
      case Level   extends Column("Level")
      case Message extends Column("Message")

   private val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

class LogTableModel extends AbstractTableModel:
   import LogTableModel.*

   private var entries: IndexedSeq[LogEntry] = IndexedSeq.empty

   def setEntries(newEntries: Seq[LogEntry]): Unit =
      entries = newEntries.toIndexedSeq
      fireTableDataChanged()

   def entryAt(row: Int): LogEntry = entries(row)

   override def getRowCount: Int = entries.size

   override def getColumnCount: Int = Column.values.length

   override def getColumnName(column: Int): String = Column.fromOrdinal(column).title

   override def getColumnClass(column: Int): Class[?] = Column.fromOrdinal(column) match
      case Column.Level => classOf[LogEntry]
      case _            => classOf[String]

   // The level column holds the whole entry, so LogLevelRenderer can read its level
   override def getValueAt(row: Int, column: Int): AnyRef =
      val entry = entries(row)
      Column.fromOrdinal(column) match
         case Column.Time    => entry.timestamp.format(timeFormat)
         case Column.Level   => entry
         case Column.Message => entry.message
