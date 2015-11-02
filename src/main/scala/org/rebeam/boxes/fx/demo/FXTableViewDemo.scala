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
import javafx.scene.control.cell._

// class PropertyMap(map: Map[String, Property])

class Person {
  lazy val firstNameProperty = new SimpleStringProperty(this, "firstName")
  lazy val lastNameProperty = new SimpleStringProperty(this, "lastName")
  override def toString = firstNameProperty.get() + " " + lastNameProperty.get()
}

object Person {
  def apply(firstName: String, lastName: String) = {
    val person = new Person()
    person.firstNameProperty.set(firstName)
    person.lastNameProperty.set(lastName)
    person
  }
}

object FXTableViewDemo extends JFXApp {

  val persons = FXCollections.observableArrayList(Person("bob", "bathyscape"), Person("alice", "angstrom"))

  val tableView = new TableView(persons)

  val firstNameCol = new TableColumn[Person, String]("First Name")
  firstNameCol.setCellValueFactory(new PropertyValueFactory("firstName"))
  firstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());

  val lastNameCol = new TableColumn[Person, String]("Last Name")

  lastNameCol.setCellValueFactory(new PropertyValueFactory("lastName"))
  lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());

  tableView.setEditable(true)

  tableView.getColumns().setAll(firstNameCol, lastNameCol)

  // val l = FXCollections.observableArrayList[Map[String, ]](1, 2, 3)
  // val tv = new TableView[Int](l)
  // val col = new TableColumn[Int, Int]("Column")
  
  // col.setCellFactory(new Callback[TableColumn[Int, Int], TableCell[Int, Int]]() {
  //   override def call(p: TableColumn[Int, Int]): TableCell[Int, Int] = {
  //     new TextFieldTableCell(new StringConverter[Int]() {
  //         override def toString(t: Int) = t.toString()
  //         override def fromString(s: String): Int = Integer.parseInt(s)                   
  //     });
  //   }
  // })
  // tv.columns.append(col)
  
//  val c = new Callback
  
//  val grid = new GridPane {
//    padding = Insets(10)
//    hgap = 5
//    vgap = 5
//  }
//  
//  grid.add(tv, 0, 0)
  
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