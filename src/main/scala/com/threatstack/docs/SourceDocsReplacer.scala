/*
 * Copyright 2015-2022 F5 Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
