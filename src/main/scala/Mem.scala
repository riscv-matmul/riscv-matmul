package MMA

import chisel3._
import chisel3.util._
import Constants._

class M2W() extends Bundle(){
    val wb_data =   Output(UInt(32.W))
    val wb_addr =   Output(UInt(5.W))
}

class MemIo() extends Bundle()
{
    val ClockB  =   Input(Clock())
    val X2M     =   Flipped(new X2M())
    val M2W     =   new M2W()
}

class Mem extends Module{
    val io = IO(new MemIo())
    val dmem_out=Wire(UInt(32.W))

    //Mem
    val DMEM = SyncReadMem(1024*1024*3, UInt(32.W))
    when(io.X2M.dmem_en) {
        when(io.X2M.dmem_wt) {
            DMEM.write(io.X2M.alu_addr, io.X2M.dmem_data)
            dmem_out:= DontCare
        } .otherwise {
        dmem_out:= DMEM.read(io.X2M.alu_addr)
        }
    } .otherwise {
        dmem_out:= DontCare
    }

    val wb_data=Wire(UInt(32.W))
    wb_data := Mux(io.X2M.wb_en,Mux(io.X2M.dmem_rd,dmem_out,io.X2M.alu_addr),0.U(32.W))

    //下一级寄存器
    withClock(io.ClockB) {
	    io.M2W.wb_addr	    :=	RegNext(io.X2M.wb_addr)
	    io.M2W.wb_data	    :=	RegNext(wb_data)
    }
}
