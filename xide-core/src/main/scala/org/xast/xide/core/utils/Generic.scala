package org.xast.xide.core.utils

def discard[T](_value: T): Unit = ()

extension [A](value: A)
   infix def |>[B](f: A => B): B = f(value)

def when[A](pred: A => Boolean, f: A => A)(value: A): A =
   if pred(value) then f(value) else value

def whenElse[A](pred: A => Boolean, f: A => A, elseF: A => A)(value: A): A =
   if pred(value) then f(value) else elseF(value)

def unless[A](pred: A => Boolean, f: A => A)(value: A): A =
   if !pred(value) then f(value) else value