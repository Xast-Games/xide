package org.xast.xide.logs_plugin

import org.xast.xide.core.log.LogEntry

import scala.swing.Table

class LogLevelRenderer extends Table.LabelRenderer[LogEntry](
   entry => (
      entry.level.toIcon, 
      entry.level.toString
   )
):

   override def configure(
      table: Table,
      isSelected: Boolean,
      hasFocus: Boolean,
      entry: LogEntry,
      row: Int,
      column: Int,
   ): Unit =
      super.configure(
         table, 
         isSelected, 
         hasFocus, 
         entry, 
         row, 
         column,
      )

      if !isSelected then
         component.foreground = entry.level.toColor
