package uplot

import org.joda.time.DateTime

package object ops {

  implicit class FigureOps(val f: Figure) extends AnyVal {
    def bgColor   : Option[String]    = f.options.collect{ case Figure.BackgroundColor(color) => color }.headOption
    def dpi       : Option[Int]       = f.options.collect{ case Figure.Dpi(dpi) => dpi }.headOption
    def title     : Option[String]    = f.options.collect{ case Figure.Title(title) => title }.headOption
    def positions : Seq[Plot.Position] = f.plots.map(_.position)

    def sharedX   : Boolean           = f.options.collect{ case Figure.SharedX(true) => true }.headOption getOrElse false

    def onlyGridPositions: Boolean = positions.forall { case _: Plot.GridPosition => true; case _ => false }
    def gridPositions: Seq[(Int, Int)] = positions.collect { case Plot.GridPosition(row, col) => (row, col) }

    def withTitle(title: String): Figure = Figure(f.plots, f.options.filter {case Figure.Title(_) => false; case _ => true} ++ Seq(Figure.Title(title)))
    def withPlots(plots: Seq[Plot]): Figure = Figure(plots, f.options)

    def withRanges(xLimOption: Option[(Double, Double)], yLimOption: Option[(Double, Double)]): Figure =
      if (xLimOption.isEmpty && yLimOption.isEmpty)
        f
      else
        f.copy(plots = f.plots map { _.withRanges(xLimOption, yLimOption) })

    def eraseTitles: Figure = Figure(f.plots.map(_ withTitle ""), f.options .filter {case Figure.Title(_) => false; case _ => true})
    def eraseInsideAxisLabels: Figure = {
      val pos = gridPositions
      val nRows = pos.map(_._1).max

      Figure(f.plots.map { p =>
        var p_ = p
        val pos = p.position.asInstanceOf[Plot.GridPosition]

        if (pos.col != 1)
          p_ = p_ withYLabel ""
        if (pos.row != nRows)
          p_ = p_ withXLabel ""

        p_
      }, f.options)
    }
  }

  implicit class PlotOps(val p: Plot) extends AnyVal {
    def withPosition(position: Plot.Position): Plot = Plot(p.position, p.title, p.data, p.options)
    def withTitle(title: String): Plot = Plot(p.position, p.title, p.data, p.options)
    def withXLabel(xlabel: String): Plot = Plot(p.position, p.title, p.data, p.options.filter{case _: Plot.XLabel => false; case _ => true} :+ Plot.XLabel(xlabel))
    def withYLabel(ylabel: String): Plot = Plot(p.position, p.title, p.data, p.options.filter{case _: Plot.YLabel => false; case _ => true} :+ Plot.YLabel(ylabel))
    def addOptions(opts: Plot.Option*): Plot = Plot(p.position, p.title, p.data, p.options ++ opts)
    def addData(d: Data): Plot = Plot(p.position, p.title, p.data :+ d, p.options)
    def withData(data: Seq[Data]): Plot = Plot(p.position, p.title, p.data, p.options)

    def withRanges(xLimOption: Option[(Double, Double)], yLimOption: Option[(Double, Double)]): Plot =
      if (xLimOption.isEmpty && yLimOption.isEmpty)
        p
      else {
        var _options = p.options.toList

        for ((xMin, xMax) <- xLimOption)
          _options = Plot.XLim(xMin, xMax) :: _options.filter{ case Plot.XLim(_, _) => false; case _ => true }

        for ((yMin, yMax) <- yLimOption)
          _options = Plot.YLim(yMin, yMax) :: _options.filter{ case Plot.YLim(_, _, _) => false; case _ => true }

        p.copy(options=_options)
      }

    def xlabel: Option[String] = p.options .collect {case Plot.XLabel(label) => label} .headOption
    def ylabel: Option[String] = p.options .collect {case Plot.YLabel(label) => label} .headOption

    def xLim: Option[(Double, Double)] = p.options .collect {case Plot.XLim(min, max) => (min, max)} .headOption
    def xLimDateTime: Option[(DateTime, DateTime)] = p.options .collect {case Plot.XLimDateTime(min, max) => (min, max)} .headOption
    def yLim: Option[(Double, Double)] = p.options .collect {case Plot.YLim(min, max, _) => (min, max)} .headOption
    def yLimWithIdx(idx: Int): Option[(Double, Double)] = p.options .collect {case Plot.YLim(min, max, _idx) if _idx == idx => (min, max)} .headOption

    def grid: Option[Boolean] = p.options .collect {case Plot.Grid(enable) => enable} .headOption

    def xScale: Option[Scale] = p.options .collect {case Plot.XScale(scale) => scale} .headOption
    def yScale: Option[Scale] = p.options .collect {case Plot.YScale(scale, _) => scale} .headOption
    def yScaleWithIdx(idx: Int): Option[Scale] = p.options .collect {case Plot.YScale(scale, _idx) if _idx == idx => scale} .headOption

    def numericData: Seq[NumericData] = p.data .collect { case d: NumericData => d }
    def singleDimensionData: Seq[Data.SingleDimension] = p.data .collect { case d: Data.SingleDimension => d }
    def numeric2DData: Seq[Numeric2DData] = p.data .collect { case d: Numeric2DData => d }
  }

  implicit class DataOps(val d: Data) extends AnyVal {
    def color: Option[(Int, Int, Int)] = d.options .collectFirst {
      case Data.Color(r, g, b) => (r, g, b)
    }
    def alpha: Option[Double] = d.options .collectFirst {
      case Data.Alpha(a) => a
    }
    def lineStyle: Option[LineStyle] = d.options .collectFirst {
      case Data.LineStyle(ls)             => Some(ls)
    } .flatten
    def lineWidth: Option[Float] = d.options .collectFirst {
      case Data.LineWidth(w) => w
    }
    def marker: Option[Marker] = d.options .collectFirst {
      case Data.Marker(Marker.None) => None
      case Data.Marker(m)           => Some(m)
    } .flatten
    def markerSize: Option[Float] = d.options .collectFirst {
      case Data.MarkerSize(size) => size
    }

    def yAxisIdx: Option[Int] = d.options .collectFirst {
      case Data.YAxisIdx(idx) => idx
    }

    def legend: Option[String] = d.options .collectFirst {
      case Data.Legend(leg) => leg
    }

    def eraseColor: Data = d.copyWithOptions(d.options.filterNot{case _: Data.Color => true; case _ => false})
    def withLineStyle(ls: LineStyle): Data = d.copyWithOptions(d.options.filterNot{case _: Data.LineStyle => true; case _ => false} :+ Data.LineStyle(ls))
    def withLineWidth(width: Float): Data = d.copyWithOptions(d.options.filterNot{case _: Data.LineWidth => true; case _ => false} :+ Data.LineWidth(width))
    def withLegend(legend: String): Data = d.copyWithOptions(d.options.filterNot{case _: Data.Legend => true; case _ => false} :+ Data.Legend(legend))
    def withColor(color: (Int, Int, Int)): Data = d.copyWithOptions(d.options.filterNot{case _: Data.Color => true; case _ => false} :+ Data.Color(color))
  }

}
