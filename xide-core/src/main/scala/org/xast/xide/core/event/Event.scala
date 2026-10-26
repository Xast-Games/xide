package org.xast.xide.core.event

import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import org.xast.xide.core.utils.discard
import scala.util.control.NonFatal

trait XideEvent

final case class FileOpenRequested(
   file: File
) extends XideEvent

final case class FileSaveRequested(
   file: File, 
   saveAs: Boolean,
) extends XideEvent

final case class TabCloseRequested(
   file: File
) extends XideEvent

final case class FileSystemChanged(
   dir: File
) extends XideEvent

final case class ThemeChanged() extends XideEvent

trait Subscription extends AutoCloseable

final class EventBus2:
   private val handlers = CopyOnWriteArrayList[PartialFunction[XideEvent, Unit]]()

   def subscribe(f: PartialFunction[XideEvent, Unit]): Subscription = 
      handlers.add(f)

      new Subscription:
         override def close(): Unit =
            discard(handlers.remove(f))

   def publish(e: XideEvent): Unit =
      handlers.forEach(h =>
         try 
            h.applyOrElse(e, _ => ())
         catch
            case NonFatal(t) => print(t)
      )