import java.io.File
import java.time.Duration

import com.typesafe.config.{ConfigException, ConfigFactory}

import scala.collection.JavaConverters._

object Config {

  private var config = {
    val moduleConfig = ConfigFactory.load
    val commonLocalConf = this.getClass.getResource("/common.local.conf")
    val configWithCommonLocalConf = if (commonLocalConf == null) moduleConfig
    else ConfigFactory.parseFile(new File(commonLocalConf.getFile)).withFallback(moduleConfig)

    val localConf = this.getClass.getResource("/local.conf")
    if (localConf == null)
      configWithCommonLocalConf
    else
      ConfigFactory.parseFile(new File(localConf.getFile)).withFallback(configWithCommonLocalConf)
  }

  def getInt(key: String, default: Int = 0): Int =
    try {
      getInt(key)
    } catch {
      case _: ConfigException.Missing => default
    }

  def getList(key: String): Array[String] = config.getList(key).asScala.toArray.map(_.unwrapped.toString)

  def getInt(key: String): Int = config.getInt(key)

  def getString(key: String, default: String): String =
    try {
      getString(key)
    } catch {
      case _: ConfigException.Missing => default
    }

  def getString(key: String): String = config.getString(key)

  def getBoolean(key: String, default: Boolean): Boolean =
    try {
      getBoolean(key)
    } catch {
      case _: ConfigException.Missing => default
    }

  def getBoolean(key: String): Boolean = config.getBoolean(key)

}