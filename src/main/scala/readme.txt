文件结构
    顶层                cpu

次一层流水线 fetch -> regfile -> alu -> mem
                        ^               |
                        |               |
                        <——————<————<————
定义文件  consts 指令外的常量，如控制信号的定义
         instructions 指令定义
         package 打包方便调用