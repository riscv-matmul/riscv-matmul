
package MMA

import chisel3._
import chisel3.util._
import Constants._

class X2M() extends Bundle(){
    val wb_addr =   Output(UInt(5.W))
    val wb_en   =   Output(Bool())
    val dmem_en =   Output(Bool())
    val dmem_rd =   Output(Bool())
    val dmem_wt =   Output(Bool())
    val dmem_data =  Output(UInt(32.W))
    val alu_addr =  Output(UInt(32.W))
}

class ALUIo() extends Bundle()
{
    val ClockB  =   Input(Clock())
    val D2X     =   Flipped(new D2X())
    val X2M     =   new X2M()
    val BR_addr =   Output(UInt(32.W))
    val PCsel   =   Output(UInt(3.W))
}

class ALU extends Module{
    val io = IO(new ALUIo())

    //ALU
    val alu_op1=io.D2X.rs1_data
    val alu_op2=Mux(io.D2X.imm_sel,io.D2X.imm_data,io.D2X.rs2_data)

    val alu_X2M   = Wire(UInt(32.W))
    alu_X2M := MuxCase(0.U, Array(
                  (io.D2X.alu_sel === ALU_ADD)  -> (alu_op1 + alu_op2).asUInt(),
                  (io.D2X.alu_sel === ALU_MUL)  -> (alu_op1 * alu_op2).asUInt(),
                  (io.D2X.alu_sel === ALU_COPY1)-> alu_op1
                  ))
     // ??Br
    io.BR_addr   := (io.D2X.PC.asSInt + io.D2X.imm_br>>1).asUInt()
    io.PCsel  := Mux(io.D2X.br_en && (io.D2X.rs1_data === io.D2X.rs2_data),PC_BR,PC_4)

    //下一级寄存器
    withClock(io.ClockB) {
	    io.X2M.wb_addr	    :=	RegNext(io.D2X.wb_addr)
	    io.X2M.wb_en   	:=	RegNext(io.D2X.wb_en)
	    io.X2M.dmem_en	    :=	RegNext(io.D2X.dmem_en)
	    io.X2M.dmem_rd	    :=	RegNext(io.D2X.dmem_rd)
	    io.X2M.dmem_wt	    :=	RegNext(io.D2X.dmem_wt)
	    io.X2M.alu_addr	:=	RegNext(alu_X2M)
	    io.X2M.dmem_data	:=	RegNext(io.D2X.rs2_data)
    }

}
