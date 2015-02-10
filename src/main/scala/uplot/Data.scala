package uplot

import org.joda.time.DateTime

trait Data {
  import Data.Option
  def options: Seq[Option]
  def copyWithOptions(opts: Seq[Option]): Data
}

object Data {
  sealed trait Option

  trait SingleDimension extends Data


  def apply(
    x: Seq[Double],
    y: Seq[Double],
    options: Seq[Option] = Seq()
  ): NumericData = NumericData(x = x, y = y, options = options)

  object Color {
    def apply(rgb: (Int, Int, Int)): Color = Color(rgb._1, rgb._2, rgb._3)
  }

  case class Color(r: Int, g: Int, b: Int) extends Option
  case class Alpha(alpha: Double) extends Option
  case class LineStyle(style: uplot.LineStyle) extends Option
  case class LineWidth(w: Float) extends Option
  case class Marker(style: uplot.Marker) extends Option
  case class MarkerSize(size: Float) extends Option
  case class Legend(leg: String) extends Option
  case class YAxisIdx(idx: Int) extends Option
  case class Tooltips(tooltips: Seq[String]) extends Option
}

case class Bands(isXAxis: Boolean, intervals: Seq[(Double, Double)], options: Seq[Data.Option] = Seq()) extends Data {
  def copyWithOptions(options: Seq[Data.Option]): Data = Bands(isXAxis, intervals, options)
}

case class Lines(isXAxis: Boolean, values: Seq[Double], options: Seq[Data.Option] = Seq()) extends Data {
  def copyWithOptions(options: Seq[Data.Option]): Data = Lines(isXAxis, values, options)
}

case class DateTimeBands(isXAxis: Boolean, intervals: Seq[(DateTime, DateTime)], options: Seq[Data.Option] = Seq()) extends Data {
  def copyWithOptions(options: Seq[Data.Option]): Data = DateTimeBands(isXAxis, intervals, options)
}

case class DateTimeLines(isXAxis: Boolean, values: Seq[DateTime], options: Seq[Data.Option] = Seq()) extends Data {
  def copyWithOptions(options: Seq[Data.Option]): Data = DateTimeLines(isXAxis, values, options)
}

case class NumericData(
  x: Seq[Double],
  y: Seq[Double],
  options: Seq[Data.Option] = Seq()
) extends Data.SingleDimension {
  def copyWithOptions(opts: Seq[Data.Option]): Data = NumericData(x, y, opts)
}

case class Numeric2DData(
  z: Seq[Seq[Double]],
  options: Seq[Data.Option] = Seq()
) extends Data {
  def copyWithOptions(opts: Seq[Data.Option]): Data = Numeric2DData(z, opts)
}

object Numeric2DData {
  case class ValuesMin(min: Double) extends Data.Option
  case class ValuesMax(max: Double) extends Data.Option
  case class ColorMap(cm: uplot.ColorMap) extends Data.Option
}

case class DateTimeData(
  x: Seq[DateTime],
  y: Seq[Double],
  options: Seq[Data.Option] = Seq()
) extends Data.SingleDimension {
  def copyWithOptions(opts: Seq[Data.Option]): Data = DateTimeData(x, y, opts)
}

case class BothDateTimeData(
  x: Seq[DateTime],
  y: Seq[DateTime],
  options: Seq[Data.Option] = Seq()
) extends Data.SingleDimension {
  def copyWithOptions(opts: Seq[Data.Option]): Data = BothDateTimeData(x, y, opts)
}
