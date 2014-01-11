package models

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future

import com.mongodb.casbah.Imports._

import org.mindrot.jbcrypt.BCrypt

case class User(email: String, name: String)

object User {
  val mongoClient = MongoClient("localhost", 27017)
  val db = mongoClient("easyword")
  val collection = db("user")

  private def fromObject(obj: MongoDBObject): Option[(ObjectId, User, String)] = {
    (obj.getAs[ObjectId]("_id"), obj.getAs[String]("email"), obj.getAs[String]("name"), obj.getAs[String]("password")) match {
      case (Some(id), Some(email), Some(name), Some(password)) => Some((id, User(email, name), password))
      case _ => None
    }
  }

  private def findByEmail(email: String): Option[(ObjectId, User, String)] = {
    collection.findOne(MongoDBObject("email" -> email)) match {
      case None => None
      case Some(obj) => fromObject(obj)
    }
  }
  
  def findUserByEmail(email: String): Option[(ObjectId, User)] = {
    findByEmail(email) map { case (id, user, _) =>
      (id, user)
    }
  }
  
  def authenticate(email: String, password: String): Option[User] = {
    findByEmail(email) match {
      case None => None
      case Some((_, user, hashed_password)) =>
        if (BCrypt.checkpw(password, hashed_password)) Some(user)
        else None
    }
  }
  
  def create(user: User, password: String): Boolean = {
    findByEmail(user.email) match {
      case None =>
        collection.insert(MongoDBObject(
          "email" -> user.email,
          "name" -> user.name,
          "password" -> BCrypt.hashpw(password, BCrypt.gensalt(12))))
        true
      case Some(_) => false
    }
  }
}
