
package MMA

import chisel3._
import chisel3.util._
import Constants._
import Instructions._

class D2X() extends Bundle(){
    val rs1_data = Output(UInt(32.W))
    val rs2_data = Output(UInt(32.W))
    val wb_addr = Output(UInt(5.W))
    val wb_en = Output(Bool())
    val imm_data = Output(UInt(32.W))
    val imm_sel = Output(Bool())
    val br_en=Output(Bool())
    val alu_sel=Output(UInt(4.W))
    val dmem_en = Output(Bool())
    val dmem_rd = Output(Bool())
    val dmem_wt = Output(Bool())
    val PC = Output(UInt(32.W))
    val imm_br = Output(SInt(32.W))
}

class RegfileIo() extends Bundle()
{
    val inst = Input(UInt(32.W))
    val F2D_PC = Input(UInt(32.W))
    val ClockB = Input(Clock())

    val D2X = new D2X()
    val M2W = Flipped(new M2W())
}

class Regfile extends Module{
    val io = IO(new RegfileIo())

    //decode
    //支持指令 ADD,MUL,ADDI,LW,SW,BEQ
    val rs1_addr = io.inst(RS1_MSB, RS1_LSB)
    val rs2_addr = Mux(io.inst === ADD || io.inst === MUL || io.inst === BEQ || io.inst === SW, io.inst(RS2_MSB, RS2_LSB), 0.U(32.W))
    //wb
    val wb_addr  = Mux(io.inst === ADD || io.inst === MUL || io.inst === LW || io.inst === ADDI, io.inst(RD_MSB,  RD_LSB), 0.U(32.W))
    val wb_en = Mux(io.inst === ADD || io.inst === MUL || io.inst === LW || io.inst === ADDI, true.B, false.B)
    //立即数及其选择
    val imm_i = io.inst(31, 20)
    val imm_s = Cat(io.inst(31, 25), io.inst(11,7))
    val imm_b = Cat(io.inst(31), io.inst(7), io.inst(30,25), io.inst(11,8))
    val imm_b_sext = Cat(Fill(19,imm_b(11)), imm_b, 0.U).asSInt()
    val imm_data= Mux(io.inst === LW || io.inst === ADDI, imm_i, 
               Mux(io.inst === SW, imm_s, 0.U(32.W)))
    val imm_sel= Mux(io.inst === LW || io.inst === SW, true.B, false.B)
    //BEQ
    val br_en = Mux(io.inst === BEQ, true.B, false.B)
    //ALU
    val alu_sel = Mux(io.inst === ADD || io.inst === ADDI, ALU_ADD, 
               Mux(io.inst === MUL, ALU_MUL , ALU_COPY1))
    //MEM
    val dmem_en = Mux(io.inst === SW || io.inst === LW, true.B, false.B)
    val dmem_rd = Mux(io.inst === LW, true.B, false.B)
    val dmem_wt = Mux(io.inst === SW, true.B, false.B)
    
    //Register File
    val regfile = Mem(32, UInt(32.W))
    //外部
    when (io.M2W.wb_addr =/= 0.U)
    {
        regfile(io.M2W.wb_addr) := io.M2W.wb_data
    }
    //
    val rs1_data = Mux((rs1_addr =/= 0.U), regfile(rs1_addr), 0.asUInt(32.W))
    val rs2_data = Mux((rs2_addr =/= 0.U), regfile(rs2_addr), 0.asUInt(32.W))

    //下一级寄存器
    withClock(io.ClockB) {
        io.D2X.rs1_data := RegNext(rs1_data)
        io.D2X.rs2_data := RegNext(rs2_data)
        io.D2X.wb_addr := RegNext(wb_addr)
        io.D2X.wb_en := RegNext(wb_en)
        io.D2X.imm_data := RegNext(imm_data)
        io.D2X.imm_sel := RegNext(imm_sel)
        io.D2X.br_en:=RegNext(br_en)
        io.D2X.alu_sel:=RegNext(alu_sel)
        io.D2X.dmem_en := RegNext(dmem_en)
        io.D2X.dmem_rd := RegNext(dmem_rd)
        io.D2X.dmem_wt := RegNext(dmem_wt)
        io.D2X.PC  :=   RegNext(io.F2D_PC)
        io.D2X.imm_br := RegNext(imm_b_sext)
    }
}
