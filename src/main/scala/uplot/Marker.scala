package uplot

sealed trait Marker

object Marker {
  case object Bullet  extends Marker
  case object Diamond extends Marker
  case object Plus    extends Marker
  case object Square extends Marker
  case object Triangle extends Marker
  case object TriangleDown extends Marker
  case object None    extends Marker

  val Default: Marker = None
}
