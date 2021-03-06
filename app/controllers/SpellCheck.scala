package controllers

import java.io.PrintWriter

import scala.collection.mutable.{Set => MSet}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.snu.ids.ha.ma.{Token, MExpression, Sentence}
import org.snu.ids.ha.ma.MorphemeAnalyzer
import org.snu.ids.ha.util.Timer

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future

import com.typesafe.plugin._

object SpellCheck extends Controller {
  val ma = new MorphemeAnalyzer()
  val words: Set[String] = scala.io.Source.fromFile("public/words/words.txt").getLines.foldLeft(Set[String]())(_ + _)
  val words1000: Set[String] = scala.io.Source.fromFile("public/words/words1000.txt").getLines.foldLeft(Set[String]())(_ + _)

  def spellcheck = Action { implicit request =>
    request.queryString.get("args") match {
      case None =>
        NotFound
      case Some(argstrs) =>
        val args = argstrs(0).split('\u0001')
        val tfs = new StringBuilder
        val xss = new StringBuilder
        for (arg <- args) {
          if (spellcheckLookup(arg)) {
            tfs.append("T")
            xss.append("-")
          }
          else {
            tfs.append("F")
            xss.append("X")
          }
        }
        Ok("CTXSPELL" + '\u0005' + 0 + '\u0005' + tfs.toString + '\u0005' + xss.toString)
    }
  }

  def spellcheck1000 = Action { implicit request =>
    request.queryString.get("args") match {
      case None =>
        NotFound
      case Some(argstrs) =>
        val args = argstrs(0).split('\u0001')
        val tfs = new StringBuilder
        val xss = new StringBuilder
        for (arg <- args) {
          if (spellcheckLookup1000(arg)) {
            tfs.append("T")
            xss.append("-")
          }
          else {
            tfs.append("F")
            xss.append("X")
          }
        }
        Ok("CTXSPELL" + '\u0005' + 0 + '\u0005' + tfs.toString + '\u0005' + xss.toString)
    }
  }

  def spellcheckLookup(text: String): Boolean = {
    if (text.length > 10000) return false

    try {
      if (text != "") {
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
                morpheme.getTag()(0) != 'J' &&
                  morpheme.getTag()(0) != 'E' &&
                  morpheme.getTag()(0) != 'S' &&
                  !words.contains(morpheme.asInstanceOf[Token].getString())
              ) {
                return false
              }

              // morpheme.asInstanceOf[Token].getIndex()
              // morpheme.asInstanceOf[Token].getString()
              // morpheme.getTag()
            }
          }
        }
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }

    return true
  }

  def spellcheckLookup1000(text: String): Boolean = {
    if (text.length > 10000) return false

    try {
      if (text != "") {
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
                morpheme.getTag()(0) != 'J' &&
                  morpheme.getTag()(0) != 'E' &&
                  morpheme.getTag()(0) != 'S' &&
                  !words1000.contains(morpheme.asInstanceOf[Token].getString())
              ) {
                return false
              }

              // morpheme.asInstanceOf[Token].getIndex()
              // morpheme.asInstanceOf[Token].getString()
              // morpheme.getTag()
            }
          }
        }
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }

    return true
  }

  // def build = Action { implicit request =>
  //   val filename = "public/words/fables.txt"
  //   val lines = scala.io.Source.fromFile(filename).getLines.reduce(_ + " " + _)
  //   val ws = lines.split("\\s")
  //   val out = new PrintWriter("public/words/fables-morpheme.txt")
  //   for (word <- ws) {
  //     if (word != "") {
  //       val result1 = ma.analyze(word)
  //       if (result1 != null) {
  //         val result2 = ma.postProcess(result1)
  //         val result3 = ma.leaveJustBest(result2)

  //         for (sentence <- ma.divideToSentences(result3).asScala.toList) {
  //           for (eojeol <- sentence) {
  //             for (morpheme <- eojeol) {
  //               if (
  //                 morpheme.getTag()(0) != 'J' &&
  //                   morpheme.getTag()(0) != 'E' &&
  //                   morpheme.getTag()(0) != 'S' &&
  //                   !words.contains(morpheme.asInstanceOf[Token].getString())) {
  //                 out.println(morpheme.asInstanceOf[Token].getString())
  //               }
  //             }
  //           }
  //         }
  //       }
  //     }
  //   }
  //   out.close()

  //   Redirect(routes.Article.index())
  // }
}
