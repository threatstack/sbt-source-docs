package com.threatstack.docs

import java.io.{File, PrintWriter}
import java.nio.file.Paths

import sbt.util.Logger

import scala.io.Source

case class Replacement(name: String, lines: List[String])

trait SourceDocsReplacer extends MarkdownParser {

  val logger: Logger

  def replaceWithSrcCode(markdown: List[File], srcFiles: Map[String, File], out: File): List[Either[Throwable, Replacement]] = {
    logger.debug(s"Replacing the following files: $markdown")
    markdown.map({ md =>
      logger.debug(s"Parsing markdown ${md.getPath}")
      parse(srcFiles)(Source.fromFile(md).getLines().toList, List.empty).map(raw => Replacement(md.getName, raw))
    })
  }

  def writeReplacedFile(out: File, log: Logger)(replacement: Replacement): Unit = {
    val outputFile = Paths.get(out.getAbsolutePath, replacement.name).toFile
    logger.debug(s"Creating new file ${outputFile.getPath}")
    outputFile.getParentFile.mkdirs()
    outputFile.createNewFile()

    val writer = new PrintWriter(outputFile)
    try for (line <- replacement.lines) writer.write(s"$line\n")
    catch {
      case e: Throwable => log.error(s"Failed to write file $e")
    }
    finally writer.close()
  }
}
