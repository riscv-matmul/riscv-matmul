源文件在 src/main/scala下
生成文件在 src/test/scala
生成的文件在 generated/cpu下

编译指令 在chisel-template下运行sbt "test:runMain MMA.CPUMain --target-dir generated/cpu --no-dce" 

文件结构
    顶层                cpu
次一层流水线 fetch -> regfile -> alu -> mem
                        ^             		  |
                         |              		  |
                        <——————<————<-
定义文件  consts 指令外的常量，如控制信号的定义
         instructions 指令定义
         package 打包方便调用
