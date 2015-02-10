package uplot

sealed trait ColorMap

object ColorMap {
  case object Gray extends ColorMap
  case object YlOrRd extends ColorMap
  case object RdBu extends ColorMap
}
