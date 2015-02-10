# uplot

*Quick referentially transparent plotting*

uplot ("micro-plot") is a plotting library for Scala, aiming at
keeping a minimalist API while still allowing to render complex plots.
It allows to quickly describe plots, through a set of self-describing
case classes / ADTs, mainly for plotting during interactive REPL sessions.
It features only immutable and referentialy transparent objects.

Renderering is done via external engines: 
[Highcharts](http://www.highcharts.com/)
is supported, [matplotlib](http://matplotlib.org/)
rendering should quickly follow.

It differs from [WISP](https://github.com/quantifind/wisp), having a minimalist and case classes / ADTs based
API, making it easier to describe more complex plots.
Like it, it can render through Highcharts.

It does not aim at rendering graphs as interactive as
the ones [Bokeh](http://bokeh.pydata.org/en/latest/) can render,
only those that can be described through case classes / ADTs.
It does not either aims at rendering publication quality graphs, like
matplotlib does.

## Usage

Add to your `build.sbt`,
```scala
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += Seq(
  "com.github.alexarchambault" %% "uplot" % "0.1.0-SNAPSHOT",
  // For exporting graphs as Highcharts javascript
  "com.github.alexarchambault" %% "uplot-highcharts" % "0.1.0-SNAPSHOT"
)
```

## Examples

TBD

--

Copyright 2015 Alexandre Archambault

Released under the LGPLv3 license.
