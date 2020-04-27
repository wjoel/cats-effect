/*
 * Copyright (c) 2017-2019 The Typelevel Cats-effect Project Developers
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

package cats.effect.tracing

final case class IOTrace(lines: List[TraceLine]) {
  def push(that: IOTrace): IOTrace =
    IOTrace(that.lines ++ lines)

  def printTrace(): Unit = {
    lines.foreach { line =>
      println(s"\t${line.className}.${line.methodName} (${line.fileName}:${line.lineNumber})")
    }
  }
}

object IOTrace {
  val Empty = IOTrace(List())
}
