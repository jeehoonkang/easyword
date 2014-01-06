package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.io.PrintWriter

import scala.collection.mutable.{Set => MSet}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.snu.ids.ha.ma.{Token, MExpression, Sentence}
import org.snu.ids.ha.ma.MorphemeAnalyzer
import org.snu.ids.ha.util.Timer

object Application extends Controller {
	val ma = new MorphemeAnalyzer()

  val words: Set[String] = scala.io.Source.fromFile("public/words/words.txt").getLines.foldLeft(Set[String]())(_ + _)

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def rebuild = Action { implicit request =>
    var count = 0
    for (i <- 1.to(6)) {
      val filename = "public/words/" + i + "000"
      val words = scala.io.Source.fromFile(filename + ".txt").getLines
      val out = new PrintWriter(filename + "-morpheme.txt")
      for (word <- words) {
        count += 1
        if (count % 20 == 0) {
          println(count)
        }

        val result1 = ma.analyze(word)
			  val result2 = ma.postProcess(result1)
			  val result3 = ma.leaveJustBest(result2)

        val sentence: Sentence = ma.divideToSentences(result3).asScala.toList.head
        val eojeol = sentence.head
        val morpheme = eojeol.head
        out.println(morpheme.asInstanceOf[Token].getString())
      }
      out.close()
    }

    Redirect(routes.Application.index())
  }

  def analyze = Action { implicit request =>
    request.body.asFormUrlEncoded match {
      case None =>
        NotFound
      case Some(params) =>
        val text = params("text")(0)
        val out = new StringBuilder

		    try {
          val result1 = ma.analyze(text)
			    val result2 = ma.postProcess(result1)
			    val result3 = ma.leaveJustBest(result2)

          val sentences: List[Sentence] = ma.divideToSentences(result3).asScala.toList
          for (sentence <- sentences) {
				    // out.append("=============================================<br />")
            // out.append(sentence.getSentence() + "<br />")
            for (word <- sentence) {
              for (morpheme <- word) {
                if (
                  !words.contains(morpheme.asInstanceOf[Token].getString()) &&
                  morpheme.getTag()(0) != 'J' &&
                  morpheme.getTag()(0) != 'E' &&
                  morpheme.getTag()(0) != 'S'
                ) {
                  out.append(morpheme + "<br />")
                }

                // morpheme.asInstanceOf[Token].getIndex()
                // morpheme.asInstanceOf[Token].getString()
                // morpheme.getTag()
              }
					    // out.append(word + "<br />")
            }
			    }
		    } catch {
			    case e: Exception => e.printStackTrace()
		    }

        Ok(Json.obj("message" -> out.toString))
    }
  }
}
