package models

import scala.collection.mutable.ListBuffer

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future

import org.joda.time.DateTime

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._

case class Article(authorId: ObjectId, content: String, created: DateTime, hardcore: Boolean)

object Article {
  val mongoClient = MongoClient("localhost", 27017)
  val db = mongoClient("easyword")
  val collection = db("article")
  val likeCollection = db("like")
  RegisterJodaTimeConversionHelpers()
  
  private def fromObject(obj: MongoDBObject): Option[(ObjectId, Article)] = {
    (obj.getAs[ObjectId]("_id"),
      obj.getAs[ObjectId]("authorId"),
      obj.getAs[String]("content"),
      obj.getAs[DateTime]("created"),
      obj.getAs[Boolean]("hardcore")
    ) match {
      case (Some(id), Some(authorId), Some(content), Some(created), hardcore) =>
        Some((id, Article(authorId, content, created, hardcore.getOrElse(false))))
      case _ => None
    }
  }

  def findById(id: ObjectId): Option[Article] = {
    collection.findOne(MongoDBObject("_id" -> id)) match {
      case None => None
      case Some(obj) => fromObject(obj) map { case (_, article) => article }
    }
  }

  def findArticlesBefore(criteria: DateTime): List[(ObjectId, Article)] = {
    val buffer = new ListBuffer[(ObjectId, Article)]()
    for (article <- collection.find("created" $lte criteria).sort(MongoDBObject("created" -> -1)).limit(20 + 1)) {
      fromObject(article) match {
        case None => ()
        case Some(id_article) => buffer += id_article
      }
    }
    buffer.result()
  }

  def findArticlesAfter(criteria: DateTime): List[(ObjectId, Article)] = {
    val buffer = new ListBuffer[(ObjectId, Article)]()
    for (article <- collection.find("created" $gte criteria).sort(MongoDBObject("created" -> -1))) {
      fromObject(article) match {
        case None => ()
        case Some(id_article) => buffer += id_article
      }
    }
    buffer.result()
  }

  def create(article: Article): Option[ObjectId] = {
    val obj = MongoDBObject(
      "authorId" -> article.authorId,
      "content" -> article.content,
      "created" -> article.created,
      "hardcore" -> article.hardcore
    )
    collection.insert(obj)
    obj.getAs[ObjectId]("_id")
  }

  def update(articleId: ObjectId, article: Article): Boolean = {
    val obj = MongoDBObject(
      "authorId" -> article.authorId,
      "content" -> article.content,
      "created" -> article.created,
      "hardcore" -> article.hardcore
    )
    val result = collection.update(
      MongoDBObject("_id" -> articleId),
      obj
    )
    result.getN > 0
  }

  def numLikes(articleId: ObjectId): Int = {
    likeCollection.find(MongoDBObject("articleId" -> articleId)).count
  }

  def liked(userId: ObjectId, articleId: ObjectId): Boolean = {
    likeCollection.findOne(MongoDBObject("userId" -> userId, "articleId" -> articleId)).isDefined
  }

  def like(articleId: ObjectId, userId: ObjectId) {
    val obj = MongoDBObject("articleId" -> articleId, "userId" -> userId)
    likeCollection.findOne(obj) match {
      case None => 
        likeCollection.insert(MongoDBObject("articleId" -> articleId, "userId" -> userId))
      case Some(_) => ()
    }
  }

  def unlike(articleId: ObjectId, userId: ObjectId) {
    likeCollection.remove(MongoDBObject("articleId" -> articleId, "userId" -> userId))
  }
}
