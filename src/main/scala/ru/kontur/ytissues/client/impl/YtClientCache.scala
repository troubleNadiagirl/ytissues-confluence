package ru.kontur.ytissues.client.impl

import java.util.concurrent.atomic.AtomicReference

import ru.kontur.ytissues.client.YtClient
import ru.kontur.ytissues.settings.YtSettings

import scala.concurrent.ExecutionContext

/**
 * This is thread safe one value cache for YtClient. Key is YtSettings.
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 03.09.2015
 */
// TODO: test
class YtClientCache(implicit ec: ExecutionContext) {
  /** Don't use this! Is settings changed this may be not changed */
  private val old: AtomicReference[Option[(YtSettings, YtClient)]] =
    new AtomicReference[Option[(YtSettings, YtClient)]](None)

  private def createClient(settings: YtSettings): YtClient = {
    val backClient = new YtClientImpl(settings.toYtClientSettings)
    new YtClientProxy(settings.toYtProxySettings, backClient)
  }

  /**
   * Get stored YtClient for this settings. Or else create new.
   * @param settings YtSettings of result client
   * @return cached YtClient
   */
  def get(settings: YtSettings): YtClient = {
    if (!old.get().map(_._1).contains(settings)) {
      old.set(Some(settings, createClient(settings)))
    }

    // old is initialized with some
    old.get().map(_._2).get
  }
}
