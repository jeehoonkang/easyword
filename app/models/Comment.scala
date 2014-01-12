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

case class Comment(authorId: ObjectId, articleId: ObjectId, content: String, created: DateTime)

object Comment {
  val mongoClient = MongoClient("localhost", 27017)
  val db = mongoClient("easyword")
  val collection = db("comment")
  RegisterJodaTimeConversionHelpers()

  private def fromObject(obj: MongoDBObject): Option[(ObjectId, Comment)] = {
    (obj.getAs[ObjectId]("_id"), obj.getAs[ObjectId]("authorId"), obj.getAs[ObjectId]("articleId"), obj.getAs[String]("content"), obj.getAs[DateTime]("created")) match {
      case (Some(id), Some(authorId), Some(articleId), Some(content), Some(created)) => Some((id, Comment(authorId, articleId, content, created)))
      case _ => None
    }
  }

  def findCommentByArticle(articleId: ObjectId): List[(ObjectId, Comment)] = {
    val buffer = new ListBuffer[(ObjectId, Comment)]()
    for (comment <- collection.find(MongoDBObject("articleId" -> articleId)).sort(MongoDBObject("created" -> 1))) {
      fromObject(comment) match {
        case None => ()
        case Some(id_comment) => buffer += id_comment
      }
    }
    buffer.result()
  }

  def numComments(articleId: ObjectId): Int = {
    collection.find(MongoDBObject("articleId" -> articleId)).count
  }

  def create(comment: Comment): Option[ObjectId] = {
    val obj = MongoDBObject(
      "authorId" -> comment.authorId,
      "articleId" -> comment.articleId,
      "content" -> comment.content,
      "created" -> comment.created
    )
    collection.insert(obj)
    obj.getAs[ObjectId]("_id")
  }
}
