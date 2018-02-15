---
layout: docs
title:  "Usage"
position: 1
---
# Usage
The `sbt-source-docs` plugin includes a dsl for defining what source code is pulled into a code fence and some sbt related configuration.

First include the plugin in a `plugins.sbt` or similar file:

```scala
addSbtPlugin("com.threatstack" % "sbt-source-docs" % "0.1.0-SNAPSHOT")
```

Then enable the plugin on the project that contains the source code that you want to pull from:

```scala
enablePlugins(SourceDocsPlugin)
```

## Configuration

| Key                       | Description |
| ------------------------  | ----------- |
| sourceDocs                | The task that runs executes the plugin |
| sourceDocsSourceDirectory | The directory to run the source replacements on |
| sourceDocsTargetDirectory | The directory to write the markdown files with replaced source to |


## Replacement Directives

The `sbt-source-docs` plugin currently supports several ways of pulling source code into documentation.
Code can be pulled in by:
- A line number range in the file
- An assignment name (i.e. `var` or `val`)
- A method name

### File Range Replacement
Source code can be pulled in based on line numbers.
```
file <file-name> <start> <end>
``` 

The file directive takes three arguments that control what part of the file is pulled in:
- `file-name`: The name of the file relative to `sourceDocsSourceDirectory`
- `start`: A natural number that represents the first line of the file to be included 
- `end`: A positive number that represents the last line to be included in the code fence. Must be greater than or equal to `start`

### Assignment Replacement
Source code can be pulled in based on an assignment name:
```
assignment <file-name> <assignment-name>
```

The assignment directive takes two arguments to control what assignment is pulled into the code fence:
- `file-name`: The name of the file relative to `sourceDocsSourceDirectory`
- `assignment-name`: The name of the `var` or `val` whose declaration and body will be added to the code fence

### Method Replacement
Source code can be pulled in based on an method name:
```
method <path-to-file> <method-name>
```

The method directive takes two arguments to control what method is pulled into the code fence:
- `file-name`: The name of the file relative to `sourceDocsSourceDirectory`
- `method-name`: The name of the `def` whose declaration and body will be added to the code fence.

### Others
There are certainly more parts of code that could be pulled in as well as directives to handle filtering pieces of the source that is being pulled in.
If you have ideas for improving the replacement dsl, please open an issue or even better a PR!
