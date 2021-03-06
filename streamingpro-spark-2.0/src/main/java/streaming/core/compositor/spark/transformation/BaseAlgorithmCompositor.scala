package streaming.core.compositor.spark.transformation

import java.util
import java.util.{List => JList, Map => JMap}
import java.util.concurrent.atomic.AtomicReference

import org.apache.spark.sql.DataFrame
import serviceframework.dispatcher.{Compositor, Processor, Strategy}
import streaming.core.CompositorHelper

/**
  * Created by allwefantasy on 2/5/2017.
  */
abstract class BaseAlgorithmCompositor[T] extends Compositor[T] with CompositorHelper {

  val TABLE = "_table_"
  val FUNC = "_func_"

  var _configParams: util.List[util.Map[Any, Any]] = _

  override def initialize(typeFilters: util.List[String], configParams: util.List[util.Map[Any, Any]]): Unit = {
    this._configParams = configParams
  }

  def labelCol = {
    config[String]("label", _configParams).getOrElse("label")
  }

  def featuresCol = {
    config[String]("features", _configParams).getOrElse("features")
  }


  def outputTableName = {
    config[String]("outputTableName", _configParams)
  }

  def inputTableName = {
    config[String]("inputTableName", _configParams)
  }

  def mapping: Map[String, String]

  def result(alg:JList[Processor[T]],ref:JList[Strategy[T]],middleResult:JList[T],params:JMap[Any,Any]):JList[T]

  val instance = new AtomicReference[Any]()

  def algorithm(training: DataFrame, params: Array[Map[String, Any]]) = {
    val clzzName = mapping(config[String]("algorithm", _configParams).get)
    if (instance.get() == null) {
      instance.compareAndSet(null, Class.forName(clzzName).
        getConstructors.head.
        newInstance(training, params))
    }
    instance.get()
  }

  def algorithm(path: String) = {
    val name = config[String]("algorithm", _configParams).get
    val clzzName = mapping.getOrElse(name, name)
    if (instance.get() == null) {
      instance.compareAndSet(null, Class.forName(clzzName).
        getConstructors.head.
        newInstance(path, parameters))
    }
    instance.get()
  }

  def path = {
    config[String]("path", _configParams).get
  }

  def parameters = {
    import scala.collection.JavaConversions._
    (_configParams(0) - "path" - "algorithm" - "outputTableName").map { f =>
      (f._1.toString, f._2.toString)
    }.toMap
  }
}
