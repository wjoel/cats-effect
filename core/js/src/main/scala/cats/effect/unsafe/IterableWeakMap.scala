/*
 * Copyright 2020-2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats.effect.unsafe

import scala.collection.mutable
import scala.scalajs.js

import IterableWeakMap._

// https://github.com/tc39/proposal-weakrefs#iterable-weakmaps
private[unsafe] class IterableWeakMap[K, V] {
  private[this] val weakMap = new WeakMap[K, V]
  private[this] val refSet = mutable.Set[js.WeakRef[K]]()
  private[this] val finalizationGroup =
    new js.FinalizationRegistry[K, Finalizer[K], js.WeakRef[K]](_.cleanup())

  def set(key: K, value: V): Unit = {
    val ref = new js.WeakRef(key)

    weakMap.set(key, value)
    refSet.add(ref)
    finalizationGroup.register(key, Finalizer(refSet, ref), ref)
  }

  def get(key: K): Option[V] = weakMap.get(key).toOption

  def entries(): Iterator[(K, V)] =
    refSet.iterator.flatMap { ref =>
      (for {
        key <- ref.deref()
        value <- weakMap.get(key)
      } yield (key, value)).toOption
    }

}

private[unsafe] object IterableWeakMap {
  private[this] final val Undefined = "undefined"
  def isAvailable: Boolean =
    js.typeOf(js.Dynamic.global.WeakMap) != Undefined &&
      js.typeOf(js.Dynamic.global.WeakRef) != Undefined &&
      js.typeOf(js.Dynamic.global.FinalizationRegistry) != Undefined

  private final case class Finalizer[K](set: mutable.Set[js.WeakRef[K]], ref: js.WeakRef[K]) {
    def cleanup(): Unit = {
      set.remove(ref)
      ()
    }
  }
}
