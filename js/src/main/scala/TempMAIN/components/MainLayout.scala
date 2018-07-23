package TempMAIN.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spgui.circuit.SPGUICircuit
import spgui.menu.SPMenu
import spgui.dashboard.Dashboard
import spgui.dragging.Dragging

/** React-component for the layout,
  * connects the SPGUI-circuit and
  * render the Modal, Dashboard, SPMenu and
  * logic for the dragging.
  *
  * Layout is the "lowest" part of the frontend
  */
object MainLayout {
  val widgetsConnection = SPGUICircuit.connect(model => (model.openWidgets.widgetMap, model.globalState))
  val menuConnection = SPGUICircuit.connect(_.settings)
  val draggingConnection = SPGUICircuit.connect(_.draggingState)

  val component = ScalaComponent.builder[Unit]("Layout")
    .render(_ =>
      <.div(
//        ^.className := GlobalCSS.layout.htmlClass,
        menuConnection(SPMenu(_)),
        widgetsConnection(Dashboard(_)),
        Dragging.mouseMoveCapture,
        draggingConnection(Dragging(_))
      )
    )
    .build

  def apply() = component()
}