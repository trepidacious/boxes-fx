package org.rebeam.boxes.fx

import javafx.beans.property.Property
import scala.language.implicitConversions
import javafx.beans.property._

import org.rebeam.boxes.core._
import BoxTypes._
import BoxUtils._
import BoxScriptImports._



// class FoxBindableLongProperty(val f: LongProperty) {
//   def |==| (b: Box[Long]) = Fox.bind(b, f)
// }
// class FoxBindableFloatProperty(val f: FloatProperty) {
//   def |==| (b: Box[Float]) = Fox.bind(b, f)
// }
// class FoxBindableDoubleProperty(val f: DoubleProperty) {
//   def |==| (b: Box[Double]) = Fox.bind(b, f)
// }

object Includes {

  implicit class FoxBindableProperty[T](val f: Property[T]) {
    
    def |==|  (b: BoxM[T]) = Fox.bind(b, f)
    def |==|  (b: Box[T]) = Fox.bind(b.m, f)

    def |== [S <: T] (b: BoxR[S]) = Fox.bindFX[S, T, Property[T]](b, f)
    def ==| [S >: T] (b: BoxM[S]) = Fox.bindBox(b, f)

  }

  implicit class FoxBindableBooleanProperty(val f: Property[java.lang.Boolean]) {
    def convert(bs: BoxM[Boolean]): BoxM[java.lang.Boolean] = BoxM(
      bs.read.map(b => new java.lang.Boolean(b)), 
      b => bs.write(b)
    )

    def |==| (b: Box[Boolean]) = Fox.bind(convert(b.m), f)
    def |==| (b: BoxM[Boolean]) = Fox.bind(convert(b), f)
  }

  implicit class FoxBindableIntegerProperty(val f: Property[java.lang.Integer]) {
    def convert(bs: BoxM[Int]): BoxM[java.lang.Integer] = BoxM(
      bs.read.map(b => new java.lang.Integer(b)), 
      b => bs.write(b)
    )

    def |==| (b: Box[Int]) = Fox.bind(convert(b.m), f)
    def |==| (b: BoxM[Int]) = Fox.bind(convert(b), f)
  }

  implicit class FoxBindableLongProperty(val f: Property[java.lang.Long]) {
    def convert(bs: BoxM[Long]): BoxM[java.lang.Long] = BoxM(
      bs.read.map(b => new java.lang.Long(b)), 
      b => bs.write(b)
    )

    def |==| (b: Box[Long]) = Fox.bind(convert(b.m), f)
    def |==| (b: BoxM[Long]) = Fox.bind(convert(b), f)
  }

  implicit class FoxBindableFloatProperty(val f: Property[java.lang.Float]) {
    def convert(bs: BoxM[Float]): BoxM[java.lang.Float] = BoxM(
      bs.read.map(b => new java.lang.Float(b)), 
      b => bs.write(b)
    )

    def |==| (b: Box[Float]) = Fox.bind(convert(b.m), f)
    def |==| (b: BoxM[Float]) = Fox.bind(convert(b), f)
  }

  implicit class FoxBindableDoubleProperty(val f: Property[java.lang.Double]) {
    def convert(bs: BoxM[Double]): BoxM[java.lang.Double] = BoxM(
      bs.read.map(b => new java.lang.Double(b)), 
      b => bs.write(b)
    )

    def |==| (b: Box[Double]) = Fox.bind(convert(b.m), f)
    def |==| (b: BoxM[Double]) = Fox.bind(convert(b), f)
  }

}