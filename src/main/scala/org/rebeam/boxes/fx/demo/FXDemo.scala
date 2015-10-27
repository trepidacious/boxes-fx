package boxes.transact.fx.demo

import scalafx.application.JFXApp
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.geometry.Insets
import javafx.scene.paint.Color
import javafx.scene.layout.Background
import javafx.scene.paint.Paint
import javafx.beans.property.ObjectProperty

import org.rebeam.boxes.fx.Includes._
import org.rebeam.boxes.fx._
import org.rebeam.boxes.core._
import BoxTypes._
import BoxUtils._
import BoxScriptImports._

object FXDemo extends JFXApp {

  val s = atomic { create("Hi!") }
  
  val text = new TextField
  text.textProperty |==| s

  val text2 = new TextField
  text2.textProperty |==| s

  val label = new Label
  label.textProperty |==| s
  
  val check = new CheckBox {
    text = "CheckBox"
  }

  val b = atomic { create(false) }
  check.selectedProperty |==| b

  val check2 = new CheckBox {
    text = "CheckBox2"
  }
  check2.selectedProperty |==| b

  val bString = b().map(b => if (b) "selected" else "not selected")
  val bLabel = new Label
  bLabel.textProperty |== bString
  
  val grid = new GridPane {
    padding = Insets(10)
    hgap = 5
    vgap = 5
  }

  val c = atomic { create(Color.BLACK) }
  
  val cp = new ColorPicker
  cp.valueProperty |==| c
  
  val cLabel = new Label
  val cs = c().map(_.toString)
  cLabel.textProperty |== cs

  val swatch = new Label("COLOR!")
  swatch.textFillProperty |== c
  
  grid.add(text, 0, 0)
  grid.add(text2, 0, 1)
  grid.add(label, 0, 2)
  grid.add(check, 0, 3)
  grid.add(check2, 0, 4)
  grid.add(bLabel, 0, 5)
  grid.add(cp, 0, 6)
  grid.add(cLabel, 0, 7)
  grid.add(swatch, 0, 8)

  stage = new PrimaryStage {
    title = "CheckBox Test"
    scene = new Scene {
      fill = Color.LIGHTGRAY
      content = grid
    }
  }

}