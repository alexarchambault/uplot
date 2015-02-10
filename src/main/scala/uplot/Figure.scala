package uplot

case class Figure(
  plots: Seq[Plot] = Seq(),
  options: Seq[Figure.Option] = Seq()
)

object Figure {
  sealed trait Option

  case class Size(width: Float, height: Float) extends Option
  case class BackgroundColor(color: String) extends Option
  case class Dpi(dpi: Int) extends Option
  case class Title(title: String) extends Option
  case class SharedX(enable: Boolean) extends Option
}
