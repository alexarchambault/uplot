package uplot

sealed trait Scale

object Scale {
  case object Log    extends Scale
  case object Linear extends Scale
}
