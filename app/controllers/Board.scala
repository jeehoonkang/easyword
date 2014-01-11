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

  def create = withUser { case (userid, user) => implicit request =>
    val content = request.body.asFormUrlEncoded.get("text")(0)
    val contents = content.split("\\s")

    if (contents.forall(SpellCheck.spellcheckLookup(_))) {
      Article.create(Article(userid, content, new DateTime()))
      Redirect(routes.Board.index).flashing(
        "success" -> "글을 저장했어요!"
      )
      // val mail = use[MailerPlugin].email
      // mail.setSubject("Easyword from " + user.name + " (" + user.email +  ")")
      // mail.addRecipient("EasyWord <easyword@jeehoon.me>","easyword@jeehoon.me")
      // mail.addFrom("EasyWord <easyword@jeehoon.me>")
      // mail.send("author: " + user.name + " (" + user.email +  ")" + "\ncontent:\n" + content)
    }
    else {
      Redirect(routes.Board.index).flashing(
        "error" -> "어려운 단어가 있어서 저장 못했어요."
      )
    }
  }

  def createComment(id: Int) = withUser { case (userid, user) => implicit request =>
    // TODO
    Ok(views.html.index())
  }

  def articles(skip: Int, limit: Int) = withUser { case (userid, user) => implicit request =>
    val reducedLimit = if (limit > 20) 20 else limit
    val articles = Article.findArticles(skip, reducedLimit)
    Ok(articles.toString) // TODO

    // {id: '1234',
    //  content: '안녕하세요\n안녕하세요',

    //  authorEmail: 'jeehoon@jeehoon.me',
    //  authorName: '강지훈',
    //  authorId: "123",

    //  created: "created-value",

    //  numComments: 10,
    //  liked: true
    // }
  }

  def comments(articleId: String) = withUser { case (userid, user) => implicit request =>
    Comment.findCommentByArticle(new ObjectId(articleId))
    Article.like(new ObjectId(articleId), userid);
    Ok("")
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
