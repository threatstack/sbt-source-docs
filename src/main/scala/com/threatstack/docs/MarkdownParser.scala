package com.threatstack.docs

import java.io.File

import cats.instances.either._
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import sbt.util.Logger

import scala.io.Source

/** A Markdown "parser" */
trait MarkdownParser {

  val logger: Logger

  private val Fence = "```"
  private val ScalaInfoString = "scala"
  private val SrcDocInfoString = "source-doc"

  def parse(srcFiles: Map[String, File])
           (remaining: List[String], accumulate: List[String] = List.empty): Either[Throwable, List[String]] = {
    remaining match {
      case Nil =>
        Right(accumulate)
      case head :: tail if head.startsWith(s"$Fence$SrcDocInfoString") =>
        for {
          fenceContents <- parseFence(tail, List.empty)
          fenceReplacement <- insertCorrectBits(srcFiles)(fenceContents)
          withFence = scalaSyntaxFence(fenceReplacement)
          continued <- parse(srcFiles)(remaining.drop(fenceContents.length + 2), accumulate ++ withFence)
        } yield continued
      case head :: tail =>
        parse(srcFiles)(tail, accumulate :+ head)
    }
  }

  def insertCorrectBits(srcFiles: Map[String, File])
                       (block: List[String]): Either[Throwable, List[String]] = {
    val replacements = parseSrcDoc(block)
    replacements.flatMap({ replacements =>
      replacements.foldLeft(Either.right[Throwable, List[String]](List.empty))({ (acc, replacement) =>
        for {
          currentContents <- acc
          nextLines <- insertSingleReplacement(srcFiles, replacement)
        } yield currentContents ++ nextLines
      })
    })
  }

  def insertSingleReplacement(srcFiles: Map[String, File], replace: SourceReplacement): Either[Throwable, List[String]] =
    replace match {
      case f: FileReplacement => replaceWithFileSource(srcFiles, f)
      case m: MethodReplacement => replaceWithMethod(srcFiles, m)
      case a: AssignmentReplacement => replaceWithAssignment(srcFiles, a)
    }

  def scalaSyntaxFence(contents: List[String]): List[String] =
    s"$Fence$ScalaInfoString" +: contents :+ Fence

  def replaceWithFileSource(files: Map[String, File], replacement: FileReplacement): Either[Throwable, List[String]] = {
    val fileOpt = files.get(replacement.filename)
     Either
      .fromOption(fileOpt, new Throwable(s"No such file ${replacement.filename}."))
      .map(file => Source.fromFile(file).getLines().slice(replacement.start, (replacement.end - replacement.start) + 1).toList)
  }

  def replaceWithMethod(files: Map[String, File], replacement: MethodReplacement): Either[Throwable, List[String]] = {
    val fileOpt = files.get(replacement.filename)
    Either
      .fromOption(fileOpt, new Throwable(s"No such file ${replacement.filename}."))
      .flatMap(file => extractMethod(Source.fromFile(file).getLines.toList, replacement))
  }

  def replaceWithAssignment(files: Map[String, File], replacement: AssignmentReplacement): Either[Throwable, List[String]] = {
    val fileOpt = files.get(replacement.filename)
    Either
      .fromOption(fileOpt, new Throwable(s"No such file ${replacement.filename}."))
      .flatMap(file => extractAssignment(Source.fromFile(file).getLines.toList, replacement))
  }

  private def extractMethod(lines: List[String], replacement: MethodReplacement): Either[Throwable, List[String]] = {
    import scala.meta._
    val fullFile = lines.mkString("\n")
    Either
      .catchNonFatal(fullFile.parse[scala.meta.Source].get)
      .flatMap({ src =>
        val method = src.collect({ case method: Defn.Def if method.name.value == replacement.method => method.syntax })
        if (method.isEmpty) Left(new Throwable(s"No content from method ${replacement.method}"))
        else Right(method.flatMap(body => body.split('\n')))
      })
  }

  private def extractAssignment(lines: List[String], replacement: AssignmentReplacement): Either[Throwable, List[String]] = {
    import scala.meta._
    val fullFile = lines.mkString("\n")
    Either
      .catchNonFatal(fullFile.parse[scala.meta.Source].get)
      .flatMap({ src =>
        val method = src.collect({
          case v @ Defn.Var(_, Pat.Var(term) :: Nil, _, _) if term.value == replacement.name => v.syntax
          case v @ Defn.Val(_, Pat.Var(term) :: Nil, _, _) if term.value == replacement.name => v.syntax
        })
        if (method.isEmpty) Left(new Throwable(s"No content from assignment ${replacement.name}"))
        else Right(method.flatMap(body => body.split('\n')))
      })
  }

  /** Returns the contents of the fenced block.
    *
    * Note: this does not include the fenced values.
    */
  def parseFence(lines: List[String], accumulate: List[String]): Either[Throwable, List[String]] = {
    lines match {
      case Nil => Left(new Throwable("Failed to parse the code Fence. Invalid Markdown found."))
      case head :: _ if head.startsWith(Fence) => Right(accumulate)
      case head :: tail => parseFence(tail, accumulate :+ head)
    }
  }

  def parseSrcDoc(lines: List[String]): Either[Throwable, List[SourceReplacement]] = {
    if (lines.isEmpty) Left(new Throwable("Empty Src Doc."))
    else lines.map(parseSrcDocLine).sequenceU
  }

  def parseSrcDocLine(line: String): Either[Throwable, SourceReplacement] = line.split(" ").toList match {
    case "file" :: name :: start :: end :: Nil =>
      Right(FileReplacement(name, start.toInt, end.toInt))
    case "method" :: filename :: name :: Nil =>
      Right(MethodReplacement(filename, name))
    case "assignment" :: filename :: name :: Nil =>
      Right(AssignmentReplacement(filename, name))
    case _ =>
      Left(new Throwable(s"Invalid Source Replacement, $line"))
  }
}
