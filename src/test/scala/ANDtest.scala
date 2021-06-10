// ANDtest.scala
package MMA
 
import chisel3._
 
object CPUMain extends App {
  Driver.execute(args, () => new Core)
}


