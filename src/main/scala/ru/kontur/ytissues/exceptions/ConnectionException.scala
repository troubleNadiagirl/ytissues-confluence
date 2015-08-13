package ru.kontur.ytissues.exceptions

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 12.08.2015
 */
case class ConnectionException(inner: Exception) extends Exception(inner)
