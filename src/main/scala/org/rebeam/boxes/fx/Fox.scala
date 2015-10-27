package org.rebeam.boxes.fx

import java.util.concurrent.Executor
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.property._

import java.util.concurrent.{Executor, Executors}

import org.rebeam.boxes.core._
import org.rebeam.boxes.core.util._
import BoxTypes._
import BoxUtils._
import BoxScriptImports._

object JavaFXExecutorService extends Executor {
  override def execute(command: Runnable) = Platform.runLater(command)
}

object Fox {
  
  def later(effect: => Unit) = Platform.runLater(new Runnable(){ def run(): Unit = effect })

  val defaultExecutorPoolSize = 8
  val defaultThreadFactory = DaemonThreadFactory()
  lazy val defaultExecutor: Executor = Executors.newFixedThreadPool(defaultExecutorPoolSize, defaultThreadFactory)

  def observer[A](script: BoxScript[A])(effect: A => Unit): Observer = Observer(
    script, 

    //TODO - coalesce these updates with similar system to SwingView?
    //Run effect later in the JavaFX thread
    (a: A) => later(effect(a)), 

    //Run script on our own default executor
    defaultExecutor,  

    //Only most recent revisions
    true
  )

  def bind[T](b: BoxM[T], f: Property[T]) = new FoxBindingGeneric(b, f): FoxBinding  

  // def bind(b: BoxM[Boolean], f: BooleanProperty) = new FoxBindingBoolean(b, f): FoxBinding
  // def bind(b: BoxM[Int], f: IntegerProperty) = new FoxBindingInteger(b, f): FoxBinding
  // def bind(b: BoxM[Long], f: LongProperty) = new FoxBindingLong(b, f): FoxBinding
  // def bind(b: BoxM[Float], f: FloatProperty) = new FoxBindingFloat(b, f): FoxBinding
  // def bind(b: BoxM[Double], f: DoubleProperty) = new FoxBindingDouble(b, f): FoxBinding
  
  // def bindBox[T, P <: Property[_ <: T]](b: Box[T], f: P) = new FoxBoxBindingGeneric[T, P](b, f): FoxBinding
  // def bindFX[T, P <: Property[_ >: T]](b: Box[T], f: P) = new FoxFXBindingGeneric[T, P](b, f): FoxBinding
}

// private class FoxBoxBindingGeneric[T, P <: Property[_ <: T]](val b: Box[T], val f: P) extends ChangeListener[T] with FoxBinding {
//   f.addListener(this)  
//   def changed(observable: ObservableValue[_ <: T], oldValue: T, newValue: T) = shelf.transact(implicit txn => b() = newValue)
// }

// private class FoxFXBindingGeneric[T, P <: Property[_ >: T]](val b: Box[T], val f: P) extends FoxBinding {
//   val view = Fox.view(implicit txn => {
//     //Always read b, to ensure we view it
//     val bv = b()
//     if (f.getValue() != bv) f.setValue(bv)
//   })
// }

trait FoxBinding

/*
 * Note that we use non-weak listeners and references to the Property and Box in each case. The only important
 * weak reference is from the Box to the boxes Observer, so that the Box will not retain the JavaFX view. We don't
 * mind that the JavaFX view retains the Box - this is desirable in a GUI where the only way the data model is
 * retained is by being displayed in a GUI element. We also make sure that we retain the boxes Observer so that
 * the observer will not be GCed as long as this binding is retained (by the Property it is listening to)
 */

private class FoxBindingGeneric[T, P <: Property[T]](val b: BoxM[T], val f: P) extends ChangeListener[T] with FoxBinding{
  
  //We don't need any synchronisation on ourRevision or setting vars, since 
  //changed() and the Fox.view are always called from the JavaFX thread.
  var ourRevision: Option[Long] = None
  var setting = false

  //TODO - could we get rid of setting by updating ourRevision before setting the new value, and using this to
  //ignore the call to changed? This would slightly reduce complexity and memory use.

  f.addListener(this)
  
  def changed(observable: ObservableValue[_ <: T], oldValue: T, newValue: T) = if (!setting) ourRevision = Some(atomicToRevision(b() = newValue).index)
  
  val observer = Fox.observer{
    for {
      bv <- b()
      ri <- revisionIndex
    } yield (bv, ri)
  }{ 
    case (bv, ri) => {
      val moreRecent = ourRevision.map(_ < ri).getOrElse(true)
      if (moreRecent) {
        setting = true
        if (f.getValue() != bv) f.setValue(bv)
        setting = false
        ourRevision = Some(ri)
      }
    }
  }

  atomic { observe(observer) }  

}

// //TODO de-duplicate specialised versions below - tricky to do, I think due to Scala/Java wrapper mismatches in parametric types

// private class FoxBindingBoolean(val b: Box[Boolean], val f: BooleanProperty) extends ChangeListener[java.lang.Boolean] with FoxBinding {
//   //We don't need any synchronisation on ourRevision or setting vars, since 
//   //changed() and the Fox.view are always called from the JavaFX thread.
//   var ourRevision: Option[Long] = None
//   var setting = false

//   f.addListener(this)
  
//   def changed(observable: ObservableValue[_ <: java.lang.Boolean], oldValue: java.lang.Boolean, newValue: java.lang.Boolean) = if (!setting) {
//     val newRevision = shelf.transactToRevision(implicit txn => b() = newValue)._2.index
//     ourRevision = Some(newRevision)
//   }
  
//   val view = Fox.view(implicit txn => {
//     //Always read b, to ensure we view it
//     val bv = b()
//     val moreRecent = ourRevision.map(_ < txn.revision.index).getOrElse(true)

//     //Update if we have a more recent revision - i.e. if we have committed no revisions, or this txn's revision is more recent than
//     //the most recent we have committed.
//     if (moreRecent) {
//       setting = true
//       if (f.getValue() != b()) f.setValue(bv)
//       setting = false
//       ourRevision = Some(txn.revision.index)
//     }
//   })
// }

// private class FoxBindingInteger(val b: Box[Int], val f: IntegerProperty) extends ChangeListener[Number] with FoxBinding {
//   //We don't need any synchronisation on ourRevision or setting vars, since 
//   //changed() and the Fox.view are always called from the JavaFX thread.
//   var ourRevision: Option[Long] = None
//   var setting = false

//   f.addListener(this)

//   def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number) = if (!setting) {
//     val newRevision = shelf.transactToRevision(implicit txn => b() = newValue.intValue())._2.index
//     ourRevision = Some(newRevision)
//   }
  
//   val view = Fox.view(implicit txn => {
//     //Always read b, to ensure we view it
//     val bv = b()
//     val moreRecent = ourRevision.map(_ < txn.revision.index).getOrElse(true)

//     //Update if we have a more recent revision - i.e. if we have committed no revisions, or this txn's revision is more recent than
//     //the most recent we have committed.
//     if (moreRecent) {
//       setting = true
//       if (f.getValue() != b()) f.setValue(bv)
//       setting = false
//       ourRevision = Some(txn.revision.index)
//     }
//   })
// }

// private class FoxBindingLong(val b: Box[Long], val f: LongProperty) extends ChangeListener[Number] with FoxBinding {
//   //We don't need any synchronisation on ourRevision or setting vars, since 
//   //changed() and the Fox.view are always called from the JavaFX thread.
//   var ourRevision: Option[Long] = None
//   var setting = false

//   f.addListener(this)

//   def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number) = if (!setting) {
//     val newRevision = shelf.transactToRevision(implicit txn => b() = newValue.longValue())._2.index
//     ourRevision = Some(newRevision)
//   }
  
//   val view = Fox.view(implicit txn => {
//     //Always read b, to ensure we view it
//     val bv = b()
//     val moreRecent = ourRevision.map(_ < txn.revision.index).getOrElse(true)

//     //Update if we have a more recent revision - i.e. if we have committed no revisions, or this txn's revision is more recent than
//     //the most recent we have committed.
//     if (moreRecent) {
//       setting = true
//       if (f.getValue() != b()) f.setValue(bv)
//       setting = false
//       ourRevision = Some(txn.revision.index)
//     }
//   })
// }

// private class FoxBindingFloat(val b: Box[Float], val f: FloatProperty) extends ChangeListener[Number] with FoxBinding {
//   //We don't need any synchronisation on ourRevision or setting vars, since 
//   //changed() and the Fox.view are always called from the JavaFX thread.
//   var ourRevision: Option[Long] = None
//   var setting = false

//   f.addListener(this)

//   def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number) = if (!setting) {
//     val newRevision = shelf.transactToRevision(implicit txn => b() = newValue.floatValue())._2.index
//     ourRevision = Some(newRevision)
//   }
  
//   val view = Fox.view(implicit txn => {
//     //Always read b, to ensure we view it
//     val bv = b()
//     val moreRecent = ourRevision.map(_ < txn.revision.index).getOrElse(true)

//     //Update if we have a more recent revision - i.e. if we have committed no revisions, or this txn's revision is more recent than
//     //the most recent we have committed.
//     if (moreRecent) {
//       setting = true
//       if (f.getValue() != b()) f.setValue(bv)
//       setting = false
//       ourRevision = Some(txn.revision.index)
//     }
//   })
// }

// private class FoxBindingDouble(val b: Box[Double], val f: DoubleProperty) extends ChangeListener[Number] with FoxBinding {
//   //We don't need any synchronisation on ourRevision or setting vars, since 
//   //changed() and the Fox.view are always called from the JavaFX thread.
//   var ourRevision: Option[Long] = None
//   var setting = false

//   f.addListener(this)

//   def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number) = if (!setting) {
//     val newRevision = shelf.transactToRevision(implicit txn => b() = newValue.doubleValue())._2.index
//     ourRevision = Some(newRevision)
//   }
  
//   val view = Fox.view(implicit txn => {
//     //Always read b, to ensure we view it
//     val bv = b()
//     val moreRecent = ourRevision.map(_ < txn.revision.index).getOrElse(true)

//     //Update if we have a more recent revision - i.e. if we have committed no revisions, or this txn's revision is more recent than
//     //the most recent we have committed.
//     if (moreRecent) {
//       setting = true
//       if (f.getValue() != b()) f.setValue(bv)
//       setting = false
//       ourRevision = Some(txn.revision.index)
//     }
//   })
// }
