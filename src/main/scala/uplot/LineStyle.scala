package uplot

sealed trait LineStyle

object LineStyle {
  case object Continuous extends LineStyle
  case object Dashed     extends LineStyle
  case object Dotted     extends LineStyle
  case object None       extends LineStyle

  val Default: LineStyle = Continuous
}
