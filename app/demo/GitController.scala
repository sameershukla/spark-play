package demo

import play.api.mvc.Controller
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row

/**
 * @author Sameer Shukla
 */
object GitController extends Controller {

  //Json File Directory Location
  val dataFile = "resources/githubfiles"

  //Get DataFrame (RDD)
  val rdd = SparkCommons.sqlContext.read.json(dataFile)

  def health = Action {
    Ok("Spark Play Application is Up and Running")
  }

  /**
   * Find Total No of rows in the File
   */
  def countTotalPushEvents = Action {
    val totalEvents = event
    Ok(Json.obj("Total Push Events " -> totalEvents.count()))
  }

  /**
   * Print all push event details
   */
  def printTotalPushEventDetails = Action {
    val totalEvents = event
    Ok(toJsonString(totalEvents))
  }

  /**
   * Filter By Username
   */
  def fetchUserAndCount = Action {
    val actorLogin = rdd.groupBy("actor.login").count()
    Ok(toJsonString(actorLogin))
  }

  /**
   * Check whether any checkins done by the give user
   */
  def checkUserPushedCode(name: String) = Action {
    val result = toJsonString(rdd.filter(rdd("actor.login").contains(name)))
    Ok(result)
  }

  /**
   * Find User with maximum push events
   */
  def userWithMaximumPush = Action {
    val map = rdd.groupBy("actor.login").count.collect.toList.groupBy { x => x.get(1).asInstanceOf[Long] }
    val maxKey = map.keySet.max
    val result = map(maxKey)
    val name = Json.toJson(result.map { x => x.get(0).toString() })
    Ok("{'name':" + name + ",'count':" + maxKey + "}")
  }

  /**
   * Find User with minimum push events
   */
  def userWithMinimumPush = Action {
    val map = rdd.groupBy("actor.login").count.collect.toList.groupBy { x => x.get(1).asInstanceOf[Long] }
    val minKey = map.keySet.min
    val result = map(minKey)
    val names = Json.toJson(result.map { x => x.get(0).toString() })
    Ok("{'name':" + names + ",'count':" + minKey + "}")
  }

  def event: DataFrame = {
    val totalEvents = rdd.filter("type = 'PushEvent'")
    totalEvents
  }

  /**
   * Convert RDD to Json String
   */
  def toJsonString(rdd: DataFrame): String = {
    "[" + rdd.toJSON.collect.toList.mkString(",\n") + "]"
  }

}