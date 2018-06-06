package spgui.circuit

import diode._
import diode.react.ReactConnector
import org.scalajs.dom.ext.LocalStorage
import sp.domain.JSWrites
import spgui.theming.Theming.Theme
import spgui.dashboard.Dashboard

object SPGUICircuit extends Circuit[SPGUIModel] with ReactConnector[SPGUIModel] {
  def initialModel = BrowserStorage.load.getOrElse(InitialState())
  val actionHandler = composeHandlers(
    new PresetsHandler(
      zoomRW(m => PresetsHandlerScope(m.presets, m.openWidgets, m.widgetData))
      ((m, phs) => m.copy(presets = phs.presets, openWidgets = phs.openWidgets, widgetData = phs.widgetData))
    ),
    new OpenWidgetsHandler(
      zoomRW(_.openWidgets)((m, v) => m.copy(openWidgets = v))
    ),
    new GlobalStateHandler(
      zoomRW(_.globalState)((m, v) => m.copy(globalState = v))
    ),
    new SettingsHandler(
      zoomRW(_.settings)((m, v) => m.copy(settings = v))
    ),
    new WidgetDataHandler(
      zoomRW(_.widgetData)((m,v) => m.copy(widgetData = v))
    ),
    new DraggingHandler(
      zoomRW(_.draggingState)((m,v) => m.copy(draggingState = v))
    ),
    new ModalHandler(
      zoomRW(_.modalState)((m,v) => m.copy(modalState = v))
    )
  )
  // store state upon any model change
  subscribe(zoomRW(myM => myM)((m,v) => v))(m => BrowserStorage.store(m.value))
}

case class PresetsHandlerScope(presets: DashboardPresets, openWidgets: OpenWidgets, widgetData: WidgetData)

class PresetsHandler[M](modelRW: ModelRW[M, PresetsHandlerScope]) extends ActionHandler(modelRW) {

  override def handle: PartialFunction[Any, ActionResult[M]] = {
    case AddDashboardPreset(preset) =>  // Takes current state of dashboard and saves in list of presets
      println("AddDashboardPreset")
      updated(value.copy(presets = value.presets + preset))

    case RemoveDashboardPreset(preset) => // Removes the preset corresponding to the given name
      val newPresets = value.presets.removeFirst(_.name == preset.name).getOrElse(value.presets)
      updated(value.copy(presets = newPresets))

    case SetDashboardPresets(presets: Set[DashboardPreset]) =>
      updated(value.copy(presets = DashboardPresets(presets)))

    case RecallDashboardPreset(preset) =>
      updated(value.copy(openWidgets = OpenWidgets())) //First remove all widgets to let them unmount
      updated(value.copy(openWidgets = preset.widgets, widgetData = preset.widgetData))
  }
}

class OpenWidgetsHandler[M](modelRW: ModelRW[M, OpenWidgets]) extends ActionHandler(modelRW) {
  override def handle = {
    case AddWidget(widgetType, width, height, id) =>
      val occupiedGrids = value.xs.values.flatMap(w =>
        for {
          x <- w.layout.x until w.layout.x + w.layout.w
          y <- w.layout.y until w.layout.y + w.layout.h
        } yield (x, y)
      )
      val bestPosition: Int = Stream.from(0).find(i => {
        val x = i % Dashboard.cols
        val y = i / Dashboard.cols

        val potentialCollisions = for {
          reqX <- Stream range (x, x + width)
          reqY <- Stream range (y, y + height)
        } yield {
          val exceedsColumns = reqX >= Dashboard.cols
          val collides = occupiedGrids.exists { case (occX, occY) => occX == reqX && occY == reqY }
          exceedsColumns || collides
        }

        val collision = potentialCollisions.contains(true)

        collision
      }).get

      val x: Int = bestPosition % Dashboard.cols
      val y: Int = bestPosition / Dashboard.cols

      val newWidget = OpenWidget(
        id,
        WidgetLayout(x, y, width, height),
        widgetType
      )

      updated(OpenWidgets(value.xs + (id -> newWidget)))

    case CloseWidget(id) =>
      updated(OpenWidgets(value.xs - id))

    case CollapseWidgetToggle(id) =>
      val targetWidget = value.xs(id)
      val modifiedWidget = targetWidget.layout.h match {
        case 1 => targetWidget.copy(
          layout = targetWidget.layout.copy(
            collapsedHeight = 1,
            h = targetWidget.layout.collapsedHeight match {
              // this deals with the fact that panels can already have a height of 1
              // it would be strange to "restore" the height to the current height
              case 1 => 4
              case _ => targetWidget.layout.collapsedHeight
            }
          )
        )
        case _ => targetWidget.copy(
          layout = targetWidget.layout.copy(
            collapsedHeight = targetWidget.layout.h ,
            h = 1
          )
        )
      }
      updated(OpenWidgets((value.xs - id ) + (id -> modifiedWidget)))
    case CloseAllWidgets => updated(OpenWidgets())
    case UpdateLayout(id, newLayout) => {
      val updW = value.xs.get(id)
        .map(_.copy(layout = newLayout))
        .map(x => value.xs + (x.id -> x))
        .getOrElse(value.xs)
      updated(OpenWidgets(updW))
    }
    case SetLayout(newLayout) => {
      val updW = OpenWidgets(value.xs.map(x =>
        (
          x._1,
          x._2.copy(
            layout = newLayout(x._1)
          )
        )
      ))
      updated(updW)
    }
  }
}

class GlobalStateHandler[M](modelRW: ModelRW[M, GlobalState]) extends ActionHandler(modelRW) {
  override def handle = {
    case UpdateGlobalState(state) =>
      updated(state)
    case UpdateGlobalAttributes(key, v) =>
      val attr = value.attributes + (key->v)
      updated(value.copy(attributes = attr))
  }
}

class WidgetDataHandler[M](modelRW: ModelRW[M, WidgetData]) extends ActionHandler(modelRW) {
  override def handle = {
    case UpdateWidgetData(id, d) =>
      val updW = value.xs + (id -> d)
      updated(WidgetData(updW))
  }
}

class SettingsHandler[M](modelRW: ModelRW[M, Settings]) extends ActionHandler(modelRW) {
  override def handle = {
    case SetTheme(newTheme) => {
      updated(value.copy(
        theme = newTheme
      ))
    }
    case ToggleHeaders => {
      updated(value.copy(
        showHeaders = !value.showHeaders
      ))
    }
  }
}

class DraggingHandler[M](modelRW: ModelRW[M, DraggingState]) extends ActionHandler(modelRW) {
  override def handle = {
    case SetDraggableRenderStyle(renderStyle) => updated(value.copy(renderStyle = renderStyle))
    case SetDraggableData(data) => updated(value.copy(data = data))
    case SetCurrentlyDragging(dragging) => updated(value.copy(dragging = dragging))
    case SetDraggingTarget(id) => updated(value.copy(target = Some(id)))
    case UnsetDraggingTarget => updated(value.copy(target = None))
    case DropEvent(dropped, target) =>
      //println("dragged " + dropped + " onto " + target)
      updated(value.copy(latestDropEvent = Some(DropEventData(dropped, target))))
  }
}

class ModalHandler[M](modelRW: ModelRW[M, ModalState]) extends ActionHandler(modelRW) {
  override def handle = {
    case OpenModal(title, component, onComplete) => {
      updated(value.copy(
        modalVisible = true,
        title = title,
        component = Some(component),
        onComplete = Some(onComplete)
      ))
    }

    case CloseModal => {
      updated(value.copy(modalVisible = false, component = None, onComplete = None))
    }

    case x => {
      println(s"Circuit received: $x")
      updated(value)
    }
  }
}

object BrowserStorage {
  import sp.domain._
  import sp.domain.Logic._
  import JsonifyUIState._
  val namespace = "SPGUIState"

  def store(spGUIState: SPGUIModel) = LocalStorage(namespace) = SPValue(spGUIState).toJson
  def load: Option[SPGUIModel] =
    LocalStorage(namespace).flatMap(x => SPAttributes.fromJsonGetAs[SPGUIModel](x))
}

object JsonifyUIState {
  import sp.domain._
  import Logic._
  import play.api.libs.json._


  implicit val fTheme: JSFormat[Theme] = Json.format[Theme]
  implicit val fSettings: JSFormat[Settings] = Json.format[Settings]
  implicit val fWidgetData: JSFormat[WidgetData] = Json.format[WidgetData]
  implicit val fWidgetLayout: JSFormat[WidgetLayout] = Json.format[WidgetLayout]
  implicit val fOpenWidget: JSFormat[OpenWidget] = Json.format[OpenWidget]
  implicit val fDropEvent: JSFormat[DropEventData] = Json.format[DropEventData]
  implicit val fDraggingState: JSFormat[DraggingState] = Json.format[DraggingState]

  implicit lazy val modalStateWrites: JSWrites[ModalState] = _ => JsObject(Map("nothing" -> SPValue.empty))

  // we actually don't care about parsing modalState so we just instantiate a new one
  implicit lazy val modalStateReads: JSReads[ModalState] = _ => JsSuccess(ModalState())

  implicit lazy val dashboardPresetsMapReads: JSReads[Map[String, DashboardPreset]] =
    (json: JsValue) => {
      json.validate[Map[String, SPValue]].map { xs =>
        def isCorrect(k: String, v: SPValue) = v.to[DashboardPreset].isSuccess

        xs.collect { case (k, v) if isCorrect(k, v) => k -> v.to[DashboardPreset].get }
      }
    }

  implicit lazy val dashboardPresetsMapWrites: JSWrites[Map[String, DashboardPreset]] =
    (xs: Map[String, DashboardPreset]) => {
      val toFixedMap = xs.map { case (k, v) => k -> SPValue(v) }
      JsObject(toFixedMap)
    }

  implicit lazy val openWidgetMapReads: JSReads[Map[ID, OpenWidget]] = (json: JsValue) => {
    json.validate[Map[String, SPValue]].map { xs =>
      def isCorrect(k: String, v: SPValue) = ID.isID(k) && v.to[OpenWidget].isSuccess

      xs.collect { case (k, v) if isCorrect(k, v) => ID.makeID(k).get -> v.to[OpenWidget].get }
    }
  }

  implicit lazy val openWidgetMapWrites: JSWrites[Map[ID, OpenWidget]] = (xs: Map[ID, OpenWidget]) => {
    val toFixedMap = xs.map { case (k, v) => k.toString -> SPValue(v) }
    JsObject(toFixedMap)
  }

  implicit val fOpenWidgets: JSFormat[OpenWidgets] = Json.format[OpenWidgets]
  implicit val fDashboardPreset: JSFormat[DashboardPreset] = Json.format[DashboardPreset]
  implicit val fDashboardPresets: JSFormat[DashboardPresets] = Json.format[DashboardPresets]
  implicit val fGlobalState: JSFormat[GlobalState] = Json.format[GlobalState]
  implicit val fSPGUIModel: JSFormat[SPGUIModel] = Json.format[SPGUIModel]
}
