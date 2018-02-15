package com.threatstack.docs

sealed trait SourceReplacement
case class FileReplacement(filename: String, start: Int, end: Int) extends SourceReplacement
case class MethodReplacement(filename: String, method: String) extends SourceReplacement
case class AssignmentReplacement(filename: String, name: String) extends SourceReplacement
