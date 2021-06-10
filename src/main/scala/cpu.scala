package MMA

import chisel3._
import chisel3.util._

class CoreIo() extends Bundle
{
  val WriteIMEM = Input(Bool())
  val IMEMAddr = Input(UInt(32.W))
  val IMEMData = Input(UInt(32.W))
}

class Core() extends Module{

  val io = IO(new CoreIo())
  val f = Module(new Fetch())
  val r = Module(new Regfile())
  val a = Module(new ALU())
  val m = Module(new Mem())

  //f input
  f.io.WriteIMEM  := io.WriteIMEM
  f.io.IMEMAddr   := io.IMEMAddr
  f.io.IMEMData   := io.IMEMData
  f.io.BR_addr    := a.io.BR_addr
  f.io.PCsel      := a.io.PCsel

  //r input
  r.io.inst   :=f.io.inst
  r.io.F2D_PC :=f.io.F2D_PC
  r.io.M2W    <>  m.io.M2W

  //a input
  a.io.D2X    <> r.io.D2X

  //m input
  m.io.X2M    <> a.io.X2M

  //计数器，流水线时钟，可能要更改
  val (notcare, bool_ClockB) = Counter(true.B,5)
  val ClockB = bool_ClockB.asClock()

  //流水线时钟
  f.io.ClockB := ClockB
  r.io.ClockB := ClockB
  a.io.ClockB := ClockB
  m.io.ClockB := ClockB

}