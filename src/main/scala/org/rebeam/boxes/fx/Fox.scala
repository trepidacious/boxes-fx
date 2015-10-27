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
  
  def bindBox[T, P <: Property[_ <: T]](b: BoxM[T], f: P) = new FoxFXToBoxBindingGeneric[T, P](b, f): FoxBinding
  def bindFX[T, PT >: T, P <: Property[PT]](b: BoxR[T], f: P) = new FoxBoxToFXBindingGeneric[T, PT, P](b, f): FoxBinding
}

trait FoxBinding

/*
 * Note that we use non-weak listeners and references to the Property and Box in each case. The only important
 * weak reference is from the Box to the boxes Observer, so that the Box will not retain the JavaFX view. We don't
 * mind that the JavaFX view retains the Box - this is desirable in a GUI where the only way the data model is
 * retained is by being displayed in a GUI element. We also make sure that we retain the boxes Observer so that
 * the observer will not be GCed as long as this binding is retained (by the Property it is listening to)
 */

private class FoxBindingGeneric[T, P <: Property[T]](val b: BoxM[T], val f: P) extends ChangeListener[T] with FoxBinding {
  
  //We don't need any synchronisation on ourRevision or setting vars, since 
  //changed() and the Fox.view are always called from the JavaFX thread.
  var ourRevision: Option[Long] = None
  var setting = false

  //TODO - could we get rid of setting by updating ourRevision before setting the new value, and using this to
  //ignore the call to changed? This would slightly reduce complexity and memory use.

  f.addListener(this)
  
  def changed(observable: ObservableValue[_ <: T], oldValue: T, newValue: T) = {
    // println("FX |==| Box, FX change, setting " + setting + ", newValue " + newValue)
    if (!setting) {
      ourRevision = Some(atomicToRevision(b() = newValue).index)
      // println("FX |==| Box, FX change, applied " + newValue + " as revision " + ourRevision)
    }
  }
  
  val observer = Fox.observer{
    for {
      bv <- b()
      ri <- revisionIndex
    } yield (bv, ri)
  }{ 
    case (bv, ri) => {
      // println("FX |==| Box, Box observation " + bv + ", in revision " + ri)   
      val moreRecent = ourRevision.map(_ < ri).getOrElse(true)
      if (moreRecent) {
        // println("FX |==| Box, Box has new new value " + bv + ", about to set FX")   
        setting = true
        if (f.getValue() != bv) f.setValue(bv)
        setting = false
        ourRevision = Some(ri)
        // println("FX |==| Box, Box new value " + bv + " has been used")   
      } else {
        // println("FX |==| Box, Box new value " + bv + " ignored, we already have same or more recent version")   
      }
    }
  }

  atomic { observe(observer) }  

}

//Unidirectional versions

private class FoxFXToBoxBindingGeneric[T, P <: Property[_ <: T]](val b: BoxM[T], val f: P) extends ChangeListener[T] with FoxBinding {
  f.addListener(this)  
  def changed(observable: ObservableValue[_ <: T], oldValue: T, newValue: T) = {
    // println("FX -> Box newValue " + newValue)
    atomic { b() = newValue }
  }
}

private class FoxBoxToFXBindingGeneric[T, PT >: T, P <: Property[PT]](val b: BoxR[T], val f: P) extends ChangeListener[PT] with FoxBinding {

  //We listen to the property so it will retain us, but do nothing with the updates
  //This is also the cause of the interesting parametric types!
  f.addListener(this)
  def changed(observable: ObservableValue[_ <: PT], oldValue: PT, newValue: PT) = {}

  val observer = Fox.observer(b){ 
    bv => {
      // println("Box -> FX newValue " + bv)
      if (f.getValue() != bv) f.setValue(bv) 
    }
  }
  atomic { observe(observer) }  

}
