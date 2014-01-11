package controllers

import java.io.PrintWriter

import scala.collection.mutable.{Set => MSet}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
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
  val articleForm = Form(
    "text" -> text.verifying ("어려운 말이 들어있어요.", text => text.split("\\s").forall(SpellCheck.spellcheckLookup(_)))
  )

  val commentForm = Form(
    "text" -> text
  )

  def index = withUser { case (userid, user) => implicit request =>
    Ok(views.html.index(articleForm))
  }

  def create = withUser { case (userid, user) => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.index(formWithErrors)),
      content => {
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
    )
  }

  def createComment(id: Int) = withUser { case (userid, user) => implicit request =>
    // TODO
    Ok(views.html.index(articleForm))
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

    //  numLikes: 10,
    //  numComments: 10,
    //  liked: true
    // }
  }

  def comments(articleId: String) = withUser { case (userid, user) => implicit request =>
    Comment.findCommentByArticle(new ObjectId(articleId))
    Ok("") // TODO
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
