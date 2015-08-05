package ru.kontur.ytissues.client

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 30.07.2015
 */

/**
 * State of issue which can have two values `Opened` and `Resolved`
 */
sealed trait State
case object Opened extends State
case object Resolved extends State

/**
 * Represents summary of YouTrack issue
 * @param id The YouTrack issue id
 * @param summary Issue summary text
 * @param state State of issue: Opened or Resolved
 */
case class Issue(id: String, summary: String, state: State)
