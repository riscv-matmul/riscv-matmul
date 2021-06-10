
package MMA

import chisel3._
import chisel3.util._
import Constants._

class FetchIo() extends Bundle()
{
    val inst = Output(UInt(32.W))
    val F2D_PC = Output(UInt(32.W))
    //write mem when test
    val WriteIMEM = Input(Bool())
    val IMEMAddr = Input(UInt(32.W))
    val IMEMData = Input(UInt(32.W))
    val BR_addr = Input(UInt(32.W))
    val PCsel = Input(UInt(3.W))
    val ClockB = Input(Clock())
}
//ps:指令mem地址均为1，位宽32，有需要更改加入memory模块
class Fetch extends Module{
    val io = IO(new FetchIo())

    val pc_next          = Wire(UInt(32.W))
    val pc_plus4         = Wire(UInt(32.W))
    val br_target        = Wire(UInt(32.W))
    val pc_regout          = Wire(UInt(32.W))

   // PC Register
    br_target:= io.BR_addr

    withClock(io.ClockB) {
      val pc_reg = RegInit(0.U(32.W))
      pc_reg := pc_next
      pc_regout := pc_reg
    }

    pc_next := MuxCase(pc_plus4, Array(
                  (io.PCsel === PC_4)   -> pc_plus4,
                  (io.PCsel === PC_BR)  -> br_target,
                  ))

    pc_plus4 := (pc_regout + 1.U(32.W))

    //IMEM,改成8bit
    val instTemp = Wire(UInt(32.W))
    val IMEM = SyncReadMem(1024, UInt(32.W))
    when(io.WriteIMEM) {
      IMEM.write(io.IMEMAddr, io.IMEMData)
    }
    instTemp := IMEM.read(pc_regout)

    //pipeline reg fetch    
    withClock(io.ClockB) {
      io.inst   := RegNext(instTemp)
      io.F2D_PC := RegNext(pc_regout)
    }
}

