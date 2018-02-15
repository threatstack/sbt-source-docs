---
layout: home
title:  "Home"
section: "home"
---

The sbt-source-docs plugin let's you pull your source code into your documentation.

It integrates with tut, sbt-microsites, and any other library that can consume markdown.

# Quick Start

`sbt-source-docs` relies on sbt 1.0.0 and higher. Ensure that you set `sbt.version` in your build.properties:
```
sbt.version=1.0.4
```

The `sbt-source-docs` plugin hasn't quite made it to the Central Repository yet, but you can publish it locally.

Add the plugin to `plugins.sbt`:
```scala
addSbtPlugin("com.threatstack" % "sbt-source-docs" % "0.1.0")
```

Add a file to the `sourceDirectory`. For example, in `src/main/scala/Main.scala`:
```scala
object Main extends App {
  println("Hello!")
}
```

Then a markdown file that uses the `source-docs` code fence:

    ```source-docs
    file src/main/scala/Main.scala 0 4
    ```

Then run the `sourceDocs` command in sbt.

A new markdown file will be emitted in `target/scala-2.12/docs` and will be replaced with:

    ```scala
    object Main extends App {
      println("Hello!")
    }
    ```
