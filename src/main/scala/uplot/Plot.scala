package uplot

import org.joda.time.DateTime

case class Plot(
  position: Plot.Position = Plot.DefaultPosition,
  title: String = "",
  data: Seq[Data] = Seq(),
  options: Seq[Plot.Option] = Seq()
)

object Plot {
  sealed trait Position
  sealed trait Option

  case class ShowXValue(xValue: Double) extends Option
  case class ShowYValue(yValue: Double) extends Option
  case class Grid(enable: Boolean) extends Option

  case class XLim(min: Double, max: Double) extends Option

  case class XLimDateTime(min: DateTime, max: DateTime) extends Option

  case class YLim(min: Double, max: Double, idx: Int) extends Option
  object YLim {
    def apply(minMax: (Double, Double)): YLim = YLim(minMax._1, minMax._2, 0)
    def apply(min: Double, max: Double): YLim = YLim(min, max, 0)
  }

  case class XTicks(enable: Boolean) extends Option
  case class XLabel(label: String) extends Option
  case class YTicks(enable: Boolean) extends Option
  case class YLabel(label: String) extends Option
  case class SetXTicks(ticks: Seq[Double]) extends Option
  case class SetYTicks(ticks: Seq[Double]) extends Option
  case class SetXTicksWithLabels(ticks: Seq[(Double, String)]) extends Option
  case class SetYTicksWithLabels(ticks: Seq[(Double, String)]) extends Option
  case class XScale(scale: Scale) extends Option
  case class YScale(scale: Scale, axisIdx: Int) extends Option
  case class YAxisPosition(opposite: Boolean, axisIdx: Int) extends Option
  case class YAxisColor(color: (Int, Int, Int), axisIdx: Int) extends Option
  case class YAxisTitle(title: String, axisIdx: Int) extends Option
  case class YAxisUnit(unit: String, axisIdx: Int) extends Option
  case class XAxisTitle(title: String, axisIdx: Int) extends Option
  case class XAxisUnit(unit: String, axisIdx: Int) extends Option
  case class Legend(show: Boolean) extends Option

  object YScale {
    def apply(scale: Scale): YScale = YScale(scale, 0)
  }

  case object DefaultPosition extends Position
  case class GridPosition(row: Int, col: Int) extends Position
}
