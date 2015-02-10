package uplot

import java.awt.Color

object Colors {

  /* Colors from breeze-plot */

  /** The Category10 palette from Protovis http://vis.stanford.edu/protovis/docs/color.html */
  object Category10 {
    val values : Array[Color] = Array(
      "#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd",
      "#8c564b", "#e377c2", "#7f7f7f", "#bcbd22", "#17becf"
    ).map(Color.decode)

    val blue = values(0)
    val orange = values(1)
    val green = values(2)
    val red = values(3)
    val purple = values(4)
    val brown = values(5)
    val magenta = values(6)
    val gray = values(7)
    val gold = values(8)
    val teal = values(9)

    def apply(i : Int) = values(i)
  }

  /** The Category20 palette from Protovis http://vis.stanford.edu/protovis/docs/color.html */
  object Category20 {
    val values : Array[Color] = Category10.values ++ Array(
      "#aec7e8", "#ffbb78", "#98df8a", "#ff9896", "#c5b0d5",
      "#c49c94", "#f7b6d2", "#c7c7c7", "#dbdb8d", "#9edae5"
    ).map(Color.decode)

    val lightblue = values(10)
    val lightorange = values(11)
    val lightgreen = values(12)
    val lightred = values(13)
    val lightpurple = values(14)
    val lightbrown = values(15)
    val lightmagenta = values(16)
    val lightgray = values(17)
    val lightgold = values(18)
    val lightteal = values(19)

    def apply(i : Int) = values(i)
  }

  val colors = Category20.values.map(c => c.getRed.toString + "," + c.getGreen.toString + "," + c.getBlue.toString).toIndexedSeq
  val rgbColors = Category20.values.map(c => (c.getRed, c.getGreen, c.getBlue)).toIndexedSeq

  def rgbColorsStream: Stream[(Int, Int, Int)] = Stream.from(0).map(_ % rgbColors.length).map(rgbColors)

}
