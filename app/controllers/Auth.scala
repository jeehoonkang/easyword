package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future

import models._
import com.mongodb.casbah.Imports._

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withUser(f: (ObjectId, User) => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    User.findUserByEmail(username).map { case (userId, user) =>
      f(userId, user)(request)
    }.getOrElse(onUnauthorized(request))
  }
}

object Auth extends Controller with Secured {
  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying ("Email이나 패스워드가 이상해요.", result => result match {
      case (email, password) => User.authenticate(email, password).isDefined
    })
  )

  val registerForm = Form(
    tuple(
      "register_email" -> text,
      "register_name" -> text,
      "register_password" -> text,
      "register_password_repeat" -> text,
      "register_answer" -> text
    ) verifying ("이메일이 이상해요.", result => result match {
      case (email, _, _, _, _) =>
        """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\b""".r.findFirstIn(email) != None
    }) verifying ("이름이 없어요.", result => result match {
      case (_, name, _, _, _) => name != ""
    }) verifying ("패스워드가 달라요.", result => result match {
      case (_, _, password, password_repeat, _) => password == password_repeat
    }) verifying ("패스워드가 없어요.", result => result match {
      case (_, _, password, _, _) => password != ""
    }) verifying ("답이 틀렸어요.", result => result match {
      case (_, _, _, _, answer) => answer == "파주"
    }) verifying ("이미 가입한 이메일이에요.", result => result match {
      case (email, _, _, _, _) =>
        User.findUserByEmail(email) == None
    })
  )

  def modifyForm(email: String) = Form(
    tuple(
      "modify_name" -> text,
      "modify_password_original" -> text,
      "modify_password" -> text,
      "modify_password_repeat" -> text
    ) verifying ("이름이 없어요.", result => result match {
      case (name, _, _, _) => name != ""
    }) verifying ("패스워드가 달라요.", result => result match {
      case (_, _, password, password_repeat) => password == password_repeat
    }) verifying ("패스워드가 없어요.", result => result match {
      case (_, _, password, _) => password != ""
    }) verifying ("패스워드가 틀려요.", result => result match {
      case (_, password_original, _, _) => User.authenticate(email, password_original).isDefined
    })
  )

  def login = Action { implicit request =>
    username(request) match {
      case None => Ok(views.html.login(registerForm, loginForm))
      case Some(_) => Redirect(routes.Board.index())
    }
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(registerForm, formWithErrors)),
      user => Redirect(routes.Board.index).withSession(Security.username -> user._1)
    )
  }

  def modifyGet = withUser { case (userId, user) => implicit request =>
    Ok(views.html.modify(modifyForm(user.email).fill(user.name, "", "", "")))
  }

  def modify = withUser { case (userId, user) => implicit request =>
    modifyForm(user.email).bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.modify(formWithErrors)),
      modify => {
        if (User.update(userId, User(user.email, modify._1), modify._3)) {
          Redirect(routes.Board.index)
            .flashing("success" -> "개인 정보를 바꿨어요!")
        }
        else {
          BadRequest("")
        }
      }
    )
  }

  def register = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.login(formWithErrors, loginForm))
      },
      register => {
        User.create(User(register._1, register._2), register._3)
        Redirect(routes.Board.index)
          .withSession(Security.username -> register._1)
          .flashing("success" -> "백일장에 참여하러 오신 여러분을 환영합니다. 글도 남기고, 댓글도 남기고, 좋아요도 눌러보세요!")
      }
    )
  }
  
  def logout = Action {
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> "로그아웃 했어요."
    )
  }
}
