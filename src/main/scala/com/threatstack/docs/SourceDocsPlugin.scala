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

import java.io.{File, FilenameFilter}
import java.nio.file.Path

import sbt.Keys._
import sbt.PluginTrigger.NoTrigger
import sbt._

/** The [[SourceDocsPlugin]] takes markdown files with that use the `src-doc` as the
  * info string of a fenced code block and replaces the contents with contents
  * from files in the src directory.
  */
object SourceDocsPlugin extends AutoPlugin {
  override def trigger = NoTrigger
  override def requires = sbt.plugins.JvmPlugin

  object autoImport {
    val sourceDocs = taskKey[Unit]("create documentation with links to source code")
    val sourceDocsSourceDirectory = settingKey[File]("where to look for src doc sources files")
    val sourceDocsTargetDirectory = settingKey[File]("where docs output goes")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = super.projectSettings ++ Seq(
    sourceDocs := runSourceDocs(
      streams.value.log,
      sourceDocsSourceDirectory.value,
      sourceDocsTargetDirectory.value,
      sourceDirectory.value),
    sourceDocsSourceDirectory := (baseDirectory in Compile).value / "docs",
    sourceDocsTargetDirectory := crossTarget.value / "docs")

  def runSourceDocs(log: Logger, in: File, out: File, srcDir: File): Unit = {
    val replacer = new SourceDocsReplacer { val logger = log }
    val markdownSourceDirectory = in
    val markdownTargetDirectory = out
    val scalaSourceDirectory = srcDir

    val markdown = Option(markdownSourceDirectory.listFiles(markdownFilter)).fold(List.empty[File])(_.toList)
    val sources = getSources(scalaSourceDirectory)

    log.debug(s"Replacing stuff in ${in.getPath} and writing to ${out.getPath}")
    val (parsed, failed) = replacer.replaceWithSrcCode(markdown, sources, markdownTargetDirectory).partition(_.isRight)

    if (failed.nonEmpty) {
      failed.collect({ case Left(ex) => ex.getMessage }).foreach(msg => log.error(msg))
      log.error(s"Existing files: ${sources.keys.toList}")
      throw new RuntimeException("Failed to run source docs.")
    }
    else {
      parsed.collect({ case Right(d) => d }).foreach(replacer.writeReplacedFile(markdownTargetDirectory, log))
    }
    ()
  }

  private def getSources(src: File): Map[String, File] = {
    val rootPath = new File(".").toPath.toAbsolutePath
    val relativeFunc = getRelativePath(rootPath) _
    listFiles(src, recurse = true)
      .filter(_.isFile)
      .map(file => relativeFunc(file) -> file).toMap
  }

  private def getRelativePath(root: Path)(file: File): String = {
    root.relativize(file.toPath.toAbsolutePath).normalize().toString.replaceFirst("\\.\\.\\/", "")
  }

  private val markdownFilter: FilenameFilter = (_: File, name: String) => name.endsWith(".md")

  private def listFiles(dir: File, recurse: Boolean): List[File] =
    Option(dir.listFiles).fold(List.empty[File])({ files =>
      val l = files.toList
      if (recurse) l.flatMap(flattenFile) else l
    })

  private def flattenFile(f: File): List[File] =
    f :: (if (f.isDirectory) f.listFiles.toList.flatMap(flattenFile) else Nil)
}
