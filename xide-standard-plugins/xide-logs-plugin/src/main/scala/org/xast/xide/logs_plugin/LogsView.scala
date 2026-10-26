package org.xast.xide.logs_plugin

import org.xast.xide.core.log.{Log, LogEntry, LogLevel}
import org.xast.xide.core.plugin.bottom.BottomPanelView
import org.xast.xide.ui.utils.XideStyle
import org.xast.xide.logs_plugin.LogTableModel.Column

import java.awt.{BorderLayout, Font}
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.{RowFilter, SwingUtilities}
import javax.swing.table.TableRowSorter

import scala.swing.*
import scala.swing.event.{ButtonClicked, SelectionChanged}

object LogsView:
    val timeColumnWidth  = 110
    val levelColumnWidth = 90
    val rowPadding       = 6

class LogsView extends BottomPanelView, Reactor:
    import LogsView.*

    if !SwingUtilities.isEventDispatchThread then
        throw IllegalStateException("Create LogsView on the Swing EDT")

    private val service          = Log
    private val model            = LogTableModel()
    private val table            = Table(model)
    private val sorter           = TableRowSorter[LogTableModel](model)
    private val refreshScheduled = AtomicBoolean()
    private val logListener      = () => scheduleRefresh()

    private val levelOptions: Seq[Option[LogLevel]] =
        None +: LogLevel.values.toSeq.map(Some(_))

    private val levelFilter = ComboBox(levelOptions)

    levelFilter.renderer = ListView.Renderer {
        case None        => "All"
        case Some(level) => level.toString
    }

    private val clearButton = Button("Clear")(())

    setLayout(BorderLayout(0, 6))

    private val style = XideStyle.getCurrent

    add(buildToolbar(style.uiFont).peer, BorderLayout.NORTH)
    add(ScrollPane(configureTable(style.uiFont)).peer, BorderLayout.CENTER)

    listenTo(levelFilter.selection, clearButton)

    reactions += {
        case SelectionChanged(`levelFilter`) => filterByLevel(levelFilter.selection.item)
        case ButtonClicked(`clearButton`)    => service.clear()
    }

    override def addNotify(): Unit =
        super.addNotify()
        service.addListener(logListener)
        refresh()

    override def removeNotify(): Unit =
        service.removeListener(logListener)
        super.removeNotify()

    private def buildToolbar(font: Font): FlowPanel =
        val toolbar = new FlowPanel(FlowPanel.Alignment.Leading)(
            Label("Level:"),
            levelFilter,
            clearButton,
        ):
            hGap = 8
            vGap = 2

        toolbar.contents.foreach(_.font = font)
        toolbar

    private def configureTable(font: Font): Table =
        table.font = font
        table.peer.getTableHeader.setFont(font)
        table.peer.getTableHeader.setReorderingAllowed(false)
        table.rowHeight = table.peer.getFontMetrics(font).getHeight + rowPadding
        table.autoResizeMode = Table.AutoResizeMode.LastColumn
        table.selection.intervalMode = Table.IntervalMode.Single
        table.peer.setFillsViewportHeight(true)

        sorter.setComparator(Column.Level.ordinal, Ordering.by[LogEntry, Int](_.level.ordinal))
        table.peer.setRowSorter(sorter)

        table.peer.setDefaultRenderer(classOf[LogEntry], LogLevelRenderer().peer)

        fixColumnWidth(Column.Time, timeColumnWidth)
        fixColumnWidth(Column.Level, levelColumnWidth)
        table

    private def fixColumnWidth(column: Column, width: Int): Unit =
        val tableColumn = table.peer.getColumnModel.getColumn(column.ordinal)
        tableColumn.setMinWidth(width)
        tableColumn.setMaxWidth(width)
        tableColumn.setPreferredWidth(width)

    private def filterByLevel(selected: Option[LogLevel]): Unit =
        sorter.setRowFilter(selected match
            case Some(level) =>
                new RowFilter[LogTableModel, Integer]:
                    override def include(entry: RowFilter.Entry[? <: LogTableModel, ? <: Integer]): Boolean =
                        entry.getModel.entryAt(entry.getIdentifier).level == level
            case None => null
        )

    private def scheduleRefresh(): Unit =
        if refreshScheduled.compareAndSet(false, true) then
            SwingUtilities.invokeLater(() =>
                refreshScheduled.set(false)
                refresh()
            )

    private def refresh(): Unit =
        model.setEntries(service.logEntries)
