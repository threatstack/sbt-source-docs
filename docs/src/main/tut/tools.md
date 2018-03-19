---
layout: docs
title:  "Tools"
position: 2
---
# Tools
The `sbt-source-docs` can integrate with any tool that consumes markdown files.

## Tut
[Tut](https://github.com/tpolecat/tut) is a documentation tool that can interpret Scala code in a markdown file.
`sbt-source-docs` can be used alongside `tut` with some simple setup.

First get the latest version of `tut` and enable the plugin in your build.

If you want to run `source-docs` before `tut` you'll need to configure both plugins' source and target directories in your build.sbt:
```scala
sourceDocsSourceDirectory := (baseDirectory in Compile).value / "docs"
sourceDocsTargetDirectory := crossTarget.value / "tut-tmp"
tutSourceDirectory := sourceDocsTargetDirectory.value
tutTargetDirectory := crossTarget.value / "docs"
```

Files will be run through both `source-docs` and `tut` and will end up in the `docs` folder of the target directory.
At this point, `sbt-source-docs` and `tut` can co-exist in the same markdown file.
However, `sbt-source-docs` can also be used to pull from source code and evaluate it in `tut` 

To enable this you will need to further annotate the code fence to indicate that `sbt-source-docs` should output `tut` instead of `scala`:

    ```
    ```source-docs/tut
    file src/main/scala/Main.scala 0 4
    ```
    ```

Now if you run `sbt-source-docs` followed by `tut`, the code that was pulled in from your source directory will be evaluated by `tut` when it runs.
You can substitute `tut` with any of the existing [tut modifiers](http://tpolecat.github.io/tut//modifiers.html).

## Reveal
[Revealjs](https://revealjs.com/#/) is a tool for building presentations. 
It supports markdown files and this functionality is further improved upon by [reveal-md](https://github.com/webpro/reveal-md).
Using this tool depends on having `npm` installed.

Install reveal-md:
```bash
npm install -g reveal-md
```

Create a markdown file in your project. 
We'll use the same source file from [Quickstart] for this example.

Create a slideshow:

    ```
    # An Awesome Title
    
    ---
    
    ```source-docs
    file src/main/scala/Main.scala 0 4
    ```
    ```

You can then run `reveal-md target/scala-2.12/docs/{file-name}.md` to run the slide show.

## Others
Know of more tools that `sbt-source-docs` can be integrated with? Make a PR with an example of how to use it.
