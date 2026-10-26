package org.xast.xide.logs_plugin

import org.xast.xide.core.log.LogLevel
import org.xast.xide.core.utils.LucideIcon

import java.awt.Color
import javax.swing.{Icon, UIManager}

private val infoColor  = Color(110, 190, 130)
private val warnColor  = Color(230, 180, 80)
private val errorColor = Color(235, 100, 100)

private lazy val infoIcon  = LucideIcon.INFO.icon(16, UIManager.getColor("Label.foreground"))
private lazy val warnIcon  = LucideIcon.TRIANGLE_ALERT.icon(16, Color.YELLOW)
private lazy val errorIcon = LucideIcon.CIRCLE_X.icon(16, Color.RED)

extension (level: LogLevel)
   def toIcon: Icon = level match
      case LogLevel.Info  => infoIcon
      case LogLevel.Warn  => warnIcon
      case LogLevel.Error => errorIcon

   def toColor: Color = level match
      case LogLevel.Info  => infoColor
      case LogLevel.Warn  => warnColor
      case LogLevel.Error => errorColor
