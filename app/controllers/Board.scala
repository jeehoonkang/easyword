package controllers

import java.io.PrintWriter

import scala.collection.mutable.{Set => MSet}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future

import org.joda.time.DateTime

import com.typesafe.plugin._

import models._
import com.mongodb.casbah.Imports._

object Board extends Controller with Secured {
  def index = withUser { case (userid, user) => implicit request =>
    Ok(views.html.index())
  }

  def submit = withUser { case (userid, user) => implicit request =>
    val content = request.body.asFormUrlEncoded.get("text")(0)
    val contents = content.split("\\s")

    if (contents.forall(SpellCheck.spellcheckLookup(_))) {
      Article.create(Article(userid, content, new DateTime()))
      // val mail = use[MailerPlugin].email
      // mail.setSubject("Easyword from " + user.name + " (" + user.email +  ")")
      // mail.addRecipient("EasyWord <easyword@jeehoon.me>","easyword@jeehoon.me")
      // mail.addFrom("EasyWord <easyword@jeehoon.me>")
      // mail.send("author: " + user.name + " (" + user.email +  ")" + "\ncontent:\n" + content)

      Ok(views.html.submit("제출되었어요!"))
    }
    else {
      Ok(views.html.submit("어려운 단어가 있어서 제출 못했어요."))
    }
  }

  def submitComment(id: Int) = withUser { case (userid, user) => implicit request =>
    // TODO
    Ok(views.html.index())
  }

  def like(articleId: String) = withUser { case (userid, user) => implicit request =>
    Article.like(new ObjectId(articleId), userid);
    Ok("")
  }

  def unlike(articleId: String) = withUser { case (userid, user) => implicit request =>
    Article.unlike(new ObjectId(articleId), userid);
    Ok("")
  }
}
