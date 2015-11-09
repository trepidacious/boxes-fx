package boxes.transact.fx.demo

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.layout.GridPane
import scalafx.geometry.Insets

import scalafx.scene.control._
import scalafx.scene.layout._


import javafx.collections.ObservableList
import javafx.collections.FXCollections
import javafx.scene.control.TableColumn
import javafx.util.Callback
import javafx.scene.control.TableCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.StringConverter
import javafx.beans.property._
import javafx.beans.value._
import javafx.scene.control.cell._

case class PropertyMap(map: Map[String, Property[_]]) {
  override def toString = (for (e <- map) yield e._1 + " = " + e._2).mkString(", ")
}

object FXMapTableViewDemo extends JFXApp {

  def person(firstName: String, lastName: String) = PropertyMap(
    Map(
      "firstName" -> new SimpleStringProperty(null, "firstName", firstName),
      "lastName" -> new SimpleStringProperty(null, "lastName", lastName)
    )
  )

  val persons = FXCollections.observableArrayList(person("bob", "bathyscape"), person("alice", "angstrom"))

  val tableView = new TableView(persons)

  val firstNameCol = new TableColumn[PropertyMap, String]("First Name")
  firstNameCol.setCellValueFactory(new Callback[TableColumn.CellDataFeatures[PropertyMap, String], ObservableValue[String]]() {
     override def call(p: TableColumn.CellDataFeatures[PropertyMap, String]) = p.getValue().map("firstName").asInstanceOf[ObservableValue[String]]
  })

  firstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());

  val lastNameCol = new TableColumn[PropertyMap, String]("Last Name")
  lastNameCol.setCellValueFactory(new Callback[TableColumn.CellDataFeatures[PropertyMap, String], ObservableValue[String]]() {
     override def call(p: TableColumn.CellDataFeatures[PropertyMap, String]) = p.getValue().map("lastName").asInstanceOf[ObservableValue[String]]
  })
  
  lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());

  tableView.setEditable(true)

  tableView.getColumns().setAll(firstNameCol, lastNameCol)

  val box = new VBox {
    layoutX = 30
    layoutY = 20
    spacing = 10
    children = List(
      tableView, 
      new Button {
        text = "Print persons"
        onAction = handle {
          for (person <- persons) {println(person)}
        }
      },
      new Button {
        text = "Add person"
        onAction = handle {
          persons.add(person("a " + persons.size, "b"))
        }
      },
      new Button {
        text = "Modify persons"
        onAction = handle {
          for (person <- persons) {
            val fn = person.map("firstName").asInstanceOf[SimpleStringProperty]
            fn.set(fn.get() + "f")
            val ln = person.map("lastName").asInstanceOf[SimpleStringProperty]
            ln.set(ln.get() + "l")
          }
        }
      }

    )
  }

  stage = new PrimaryStage {
    title = "FX TableView Demo"
    scene = new Scene {
      fill = Color.AntiqueWhite
      content = box
    }
  }

}
