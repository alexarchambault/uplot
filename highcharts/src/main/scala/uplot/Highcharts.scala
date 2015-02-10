package uplot


import language.implicitConversions
import collection.mutable
import java.io.{ File, PrintWriter }
import java.util.{UUID, TimeZone}
import org.joda.time.DateTimeZone
import com.github.nscala_time.time.Implicits.DateTimeOrdering
import ops._
import scala.xml.NodeSeq
import argonaut.Json

object Highcharts {
  import language.postfixOps


  private val localDateTimeZone = DateTimeZone forTimeZone TimeZone.getDefault


  // Themes: see http://ilearntanewthingeveryday.wordpress.com/tag/highcharts/

  val highchartsHeaderXml = {
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
      <script src="http://code.highcharts.com/stock/highstock.js"></script>
      <script src="http://code.highcharts.com/stock/modules/exporting.js"></script>
      <script type="text/javascript" src="http://www.highcharts.com/js/themes/grid.js"></script>
  }

  def highchartsLocalHeaderXml(server: String, path: String, withScheme: Boolean = true) = {
    val schemePart = if (withScheme) "http://" else ""

    <script src={s"$schemePart$server/$path/jquery.min.js"}></script>
      <script src={s"$schemePart$server/$path/highstock.js"}></script>
      <script src={s"$schemePart$server/$path/exporting.js"}></script>
      <script src={s"$schemePart$server/$path/grid.js"}></script>
  }


  def plotHighchartsJs(p: Plot, containerId: String): String =
    ("""
       |$(function () {
       |    $('#""" + containerId + "').highcharts(\n" + plotHighchartsJson(p, containerId).spaces2 + """
       |    );
       |});
       """).stripMargin
  
  def plotHighchartsJson(p: Plot, containerId: String): Json = {
    val dataColors = {
      val d = p.singleDimensionData.zip(Colors.rgbColorsStream).map(t => (t._1, t._1.color getOrElse t._2))
      d.map(t => (t._1, f"#${t._2._1}%02x${t._2._2}%02x${t._2._3}%02x"))
    }

    val dataColorsPerYAXis = dataColors.groupBy(_._1.yAxisIdx getOrElse 0)

    val onlyXDateTimeDate = dataColors .forall {
      case (dt: DateTimeData , _) => true
      case (dt: DateTimeBands, _) => true
      case (dt: DateTimeLines, _) => true
      case (dt: BothDateTimeData, _) => true
      case _ => false
    }

    val xAxisType =
      if (onlyXDateTimeDate) "datetime"
      else p.xScale.getOrElse(Scale.Linear) match {
        case Scale.Log => "logarithmic"
        case _ => "linear"
      }

    val yAxesType =
      for ((idx, yAxisDataColors) <- dataColorsPerYAXis) yield idx -> {
        val onlyYDateTimeDate = yAxisDataColors .forall {
          case (dt: BothDateTimeData, _) => true
          case _ => false
        }

        if (onlyYDateTimeDate) "datetime"
        else p.yScaleWithIdx(idx).getOrElse(Scale.Linear) match {
          case Scale.Log => "logarithmic"
          case _ => "linear"
        }
      }


    val d = for ((dt, color) <- dataColors) yield {
      val xyStr = dt match {
        case dt: NumericData =>
          var xy = dt.x zip dt.y

          if (xAxisType == "logarithmic")
            xy = xy.filter{case (x, _) => x > 0.0}
          if (yAxesType(dt.yAxisIdx getOrElse 0) == "logarithmic")
            xy = xy.filter{case (_, y) => y > 0.0}

          xy = xy.sortBy{case (x, _) => x}

          xy map {case (x, y) =>
            (Json jNumberOrString x, Json jNumberOrString y)
          }

        case dt: DateTimeData =>
          var xy = dt.x zip dt.y
          if (yAxesType(dt.yAxisIdx getOrElse 0) == "logarithmic")
            xy = xy.filter{case (_, y) => y > 0.0}

          xy = xy.sortBy{case (x, _) => x}

          xy map {case (x, y) =>
            (Json jNumberOrString localDateTimeZone.convertUTCToLocal(x.getMillis), Json jNumberOrString y)
          }

        case dt: BothDateTimeData =>
          var xy = dt.x zip dt.y

          xy = xy.sortBy{case (x, _) => x}

          xy map {case (x, y) =>
            (Json jNumberOrString localDateTimeZone.convertUTCToLocal(x.getMillis), Json jNumberOrString localDateTimeZone.convertUTCToLocal(y.getMillis))
          }

        case _ =>
          Nil
      }

      val tooltipOptions = dt.options.collectFirst{case Data.Tooltips(tooltips) => tooltips}

      val d =
        tooltipOptions match {
          case Some(tooltips) =>
            xyStr zip tooltips map { case ((x, y), t) => Json("x" -> x, "y" -> y, "customTooltip" -> Json.jString(t)) }
          case None =>
            xyStr map { case (x, y) => Json.jArrayElements(x, y) }
        }

      val ls = dt.lineStyle getOrElse LineStyle.Continuous match {
        case LineStyle.Dashed => Some("Dash")
        case LineStyle.Dotted => Some("Dot")
        case LineStyle.None => None
        case _ => Some("Solid")
      }

      val series = new mutable.ListBuffer[(Json.JsonField, Json)]
      series += "name" -> Json.jString(dt.legend getOrElse "")
      series += "lineWidth" -> Json.jNumberOrString(ls.map(_ => dt.lineWidth getOrElse 1.0f) getOrElse 0.0f)
      series += "color" -> Json.jString(color)
      series += "dashStyle" -> Json.jString(ls getOrElse "")
      series += "yAxis" -> Json.jNumberOrString(dt.yAxisIdx getOrElse 0)
      series += "data" -> Json.jArrayElements(d: _*)

      for (_ <- tooltipOptions)
        series += "tooltip" -> Json("pointFormat" -> Json.jString("{point.customTooltip}"))

      dt.marker.filter(_ != Marker.None) match {
        case Some(m) =>
          val symbol = m match {
            case Marker.Bullet =>
              "circle"
            case Marker.Diamond =>
              "diamond"
            case Marker.Plus =>
              "plus"
            case Marker.Square =>
              "square"
            case Marker.Triangle =>
              "triangle"
            case Marker.TriangleDown =>
              "triangle-down"
            case Marker.None =>
              ""
          }

          series += "marker" -> Json(
            "enabled" -> Json.jBool(true),
            "radius" -> Json.jNumberOrString(dt.markerSize getOrElse 2.0f),
            "symbol" -> Json.jString(symbol)
          )

        case None =>
          series += "marker" -> Json("enabled" -> Json.jBool(false))
      }

      Json(series: _*)
    }


    def bandsDesc(bands: Seq[(Json, Json, Data)]): Seq[Json] =
      for {
        (from, to, data) <- bands
      } yield {
        val t = data.color getOrElse (156, 156, 156)
        val a = data.alpha getOrElse 0.5

        Json(
          "from" -> from,
          "to" -> to,
          "color" -> Json.jString(f"rgba(${t._1}, ${t._2}, ${t._3}, $a)")
        )
      }

    def linesDesc(lines: Seq[(Json, Data)]): Seq[Json] =
      for {
        (value, data) <- lines
      } yield {
        val t = data.color getOrElse (156, 156, 156)
        val a = data.alpha getOrElse 1.0

        Json(
          "value" -> value,
          "color" -> Json.jString(f"rgba(${t._1}, ${t._2}, ${t._3}, $a)"),
          "width" -> Json.jNumberOrString(data.lineWidth getOrElse 1.0f)
        )
      }

    val xBands: Seq[(Json, Json, Data)] =
      if (onlyXDateTimeDate)
        for {
          data @ DateTimeBands(true, intervals, _) <- p.data
          (from, to) <- intervals
        } yield (Json jNumberOrString localDateTimeZone.convertUTCToLocal(from.getMillis), Json jNumberOrString localDateTimeZone.convertUTCToLocal(to.getMillis), data)
      else
        for {
          data @ Bands(true, intervals, _) <- p.data
          (from, to) <- intervals
        } yield (Json jNumberOrString from, Json jNumberOrString to, data)

    val yBands: Seq[(Json, Json, Data)] =
      for {
        data @ Bands(false, intervals, _) <- p.data
        (from, to) <- intervals
      } yield (Json jNumberOrString from, Json jNumberOrString to, data)

    val xLines: Seq[(Json, Data)] =
      if (onlyXDateTimeDate)
        for {
          data @ DateTimeLines(true, values, _) <- p.data
          v <- values
        } yield (Json jNumberOrString localDateTimeZone.convertUTCToLocal(v.getMillis), data)
      else
        for {
          data @ Lines(true, values, _) <- p.data
          v <- values
        } yield (Json jNumberOrString v, data)

    val yLines: Seq[(Json, Data)] =
      for {
        data @ Lines(false, values, _) <- p.data
        v <- values
      } yield (Json jNumberOrString v, data)


    val xAxisPlotBands = bandsDesc(xBands)
    val xAxisPlotLines = linesDesc(xLines)

    val additionalXAxisOptions = {
      var xAxisOptions = List.empty[(Json.JsonField, Json)]
      if (onlyXDateTimeDate)
        for ((min, max) <- p.xLimDateTime)
          xAxisOptions = ("min" -> Json.jNumberOrString(localDateTimeZone convertUTCToLocal min.getMillis)) :: ("max" -> Json.jNumberOrString(localDateTimeZone convertUTCToLocal max.getMillis)) :: xAxisOptions
      else
        for ((min, max) <- p.xLim)
          xAxisOptions = ("min" -> Json.jNumberOrString(min)) :: ("max" -> Json.jNumberOrString(max)) :: xAxisOptions

      val xAxisUnitOption = p.options.collectFirst{case Plot.XAxisUnit(unit, axisIdx) if axisIdx == 0 => unit }

//      for (unit <- xAxisUnitOption)
//        // xAxisOptions = s"labels: { formatter: function() { if (this.value >= 1000000000 || this.value <= -1000000000) return (this.value / 1000000000) + ' G$unit'; else if (this.value >= 1000000 || this.value <= -1000000) return (this.value / 1000000) + ' M$unit'; else if (this.value >= 1000 || this.value <= -1000) return (this.value / 1000) + ' k$unit'; else return this.value + ' $unit' } }" :: xAxisOptions
//        xAxisOptions = ("labels" -> Json("formatter" -> Json.jString(s"formatterPlaceHolder:$unit"))) :: xAxisOptions

      for (Plot.XAxisTitle(title, _) <- p.options.collectFirst{case opt: Plot.XAxisTitle if opt.axisIdx == 0 => opt})
        xAxisOptions = ("title" -> Json("text" -> Json.jString(title))) :: xAxisOptions

      xAxisOptions
    }


    val yAxisPlotBands = bandsDesc(yBands)
    val yAxisPlotLines = linesDesc(yLines)

    val _additionalYAxesOptions = for ((idx, _) <- yAxesType) yield idx -> {
      var yAxisOptions = List.empty[(Json.JsonField, Json)]
      for ((min, max) <- p.yLimWithIdx(idx))
        yAxisOptions = ("min" -> Json.jNumberOrString(min)) :: ("max" -> Json.jNumberOrString(max)) :: yAxisOptions

      for (opt <- p.options.collectFirst{case opt: Plot.YAxisPosition if opt.axisIdx == idx => opt} if opt.opposite)
        yAxisOptions = ("opposite" -> Json.jBool(true)) :: yAxisOptions

      val yAxisUnitOption = p.options.collectFirst{case Plot.YAxisUnit(unit, axisIdx) if axisIdx == idx => unit }

      val yAxisColorOption =
        for (Plot.YAxisColor(t, _) <- p.options.collectFirst{case opt: Plot.YAxisColor if opt.axisIdx == idx => opt}) yield {
          val color = f"rgba(${t._1}, ${t._2}, ${t._3}, 1.0)"
          var opts = List[(Json.JsonField, Json)](
            "style" -> Json(
              "color" -> Json.jString(color)
            )
          )

//          for (unit <- yAxisUnitOption) {
//            // opts = s"formatter: function() { if (this.value >= 1000000000 || this.value <= -1000000000) return (this.value / 1000000000) + ' G$unit'; else if (this.value >= 1000000 || this.value <= -1000000) return (this.value / 1000000) + ' M$unit'; else if (this.value >= 1000 || this.value <= -1000) return (this.value / 1000) + ' k$unit'; else return this.value + ' $unit' }" :: opts
//            opts = ("formatter" -> Json.jString(s"formatterPlaceHolder:$unit")) :: opts
//          }

          yAxisOptions = ("labels" -> Json(opts: _*)) :: yAxisOptions
          color
        }

      for (Plot.YAxisTitle(title, _) <- p.options.collectFirst{case opt: Plot.YAxisTitle if opt.axisIdx == idx => opt}) {
        var ops = List[(Json.JsonField, Json)]("text" -> Json.jString(title))
        for (color <- yAxisColorOption)
          ops = ("style" -> Json("color" -> Json.jString(color))) :: ops
        yAxisOptions = ("title" -> Json(ops: _*)) :: yAxisOptions
      }

      yAxisOptions
    }

    val yAxesDescMap =
      for ((idx, yAxisType) <- yAxesType)
        yield idx -> Json(Seq(
          "type" -> Json.jString(yAxisType),
          "title" -> Json("text" -> Json.jString(p.ylabel getOrElse "")),
          "plotBands" -> Json.jArrayElements(yAxisPlotBands: _*),
          "plotLines" -> Json.jArrayElements(yAxisPlotLines: _*)
        ) ++ _additionalYAxesOptions(idx): _*)

    val yAxesDesc = Json.jArrayElements((0 to yAxesDescMap.keySet.max) .map(yAxesDescMap.getOrElse(_, Json())): _*)

    val legendOptions = Json(
      "enabled" -> Json.jBool(p.options.collectFirst{case opt: Plot.Legend => opt.show } getOrElse true)
    )

    var _chartOptions = List[(Json.JsonField, Json)](
      "zoomType" -> Json.jString("xy")
    )
    if (p.options.exists{
      case Plot.YLim(_, _, idx) if idx > 0 => true
      case _ => false
    })
      _chartOptions = ("alignTicks" -> Json.jBool(false)) :: _chartOptions

    Json(
      "chart" -> Json(_chartOptions: _*),
      "title" -> Json("text" -> Json.jString(p.title)),
      "xAxis" -> Json.jSingleArray(Json(Seq(
        "type" -> Json.jString(xAxisType),
        "title" -> Json(
          "text" -> Json.jString(p.xlabel getOrElse "")
        ),
        "plotBands" -> Json.jArrayElements(xAxisPlotBands: _*)
      ) ++ additionalXAxisOptions: _*)),
      "yAxis" -> yAxesDesc,
      "series" -> Json.jArrayElements(d: _*),
      "legend" -> legendOptions
    )
  }


  implicit class HighchartsWebPage(val figure: Figure) extends AnyVal {
    def highcharts: NodeSeq = {
      val id = UUID.randomUUID().toString

      val l =
        for ((p, idx) <- figure.plots.zipWithIndex) yield {
          val containerId = s"container$id-$idx"
          <div id={containerId} style="height: 100%; width: 100%"></div>
            <script>{plotHighchartsJs(p, containerId)}</script>
        }

      <div id={s"container$id"}>{l}</div>
    }

    def highchartsWebPage: NodeSeq = highchartsWebPageWithExtraContent()

    def highchartsWebPageWithExtraContent(serverPathOption: Option[(String, String)] = None): NodeSeq = {
      val style =
        s"""html { height: 100%; }
           |body { background-color: ${figure.bgColor getOrElse "white"}; height: 100%; } """.stripMargin

      <html>
        <head>
          <meta charset="utf-8"/>
          {serverPathOption.fold(highchartsHeaderXml){case (s, p) => highchartsLocalHeaderXml(s, p)}}
          <title>{figure.title getOrElse figure.plots.head.title}</title>
          <style type="text/css">{style}</style>
        </head>
        <body>{highcharts}</body>
      </html>
    }

    def highchartsWebPageToFile(f: File): Unit = {
      val w = new PrintWriter(f, "UTF-8")
      w println "<!DOCTYPE html>"
      w write highchartsWebPage.toString()
      w.close()
    }

  }

}
