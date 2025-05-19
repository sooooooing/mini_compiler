.class public Test
.super java/lang/Object
; strandard initializer
.method public <init>()V
aload_0
invokenonvirtual java/lang/Object/<init>()V
return
.end method


.method public static main([Ljava/lang/String;)V
.limit stack 32
.limit locals 32
bipush 3
istore_0
bipush 2
istore_1
bipush 0
istore_2

iload_0
bipush 5

if_icmpgt LABEL_0
iconst_0
goto LABEL_1
LABEL_0:
iconst_1
LABEL_1:
ifne LABEL_4

iload_0
bipush 0

if_icmpne LABEL_2
iconst_0
goto LABEL_3
LABEL_2:
iconst_1
LABEL_3:
ifne LABEL_4
iconst_0
goto LABEL_6
LABEL_4:
iconst_1
LABEL_6:
ifeq LABEL_7
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_0
invokevirtual java/io/PrintStream.println(I)V
goto LABEL_8
LABEL_7:
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_1
invokevirtual java/io/PrintStream.println(I)V
LABEL_8:

iload_0
bipush 3

if_icmplt LABEL_9
iconst_0
goto LABEL_10
LABEL_9:
iconst_1
LABEL_10:
ifeq LABEL_16

iload_0
bipush 2

if_icmplt LABEL_11
iconst_0
goto LABEL_12
LABEL_11:
iconst_1
LABEL_12:
ifeq LABEL_13
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_0
invokevirtual java/io/PrintStream.println(I)V
goto LABEL_14
LABEL_13:
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_1
invokevirtual java/io/PrintStream.println(I)V
LABEL_14:
goto LABEL_16
LABEL_16:

iload_0
bipush 3

if_icmpeq LABEL_17
iconst_0
goto LABEL_18
LABEL_17:
iconst_1
LABEL_18:
ifne LABEL_21

iload_1
bipush 2

if_icmpne LABEL_19
iconst_0
goto LABEL_20
LABEL_19:
iconst_1
LABEL_20:
ifne LABEL_21
iconst_0
goto LABEL_23
LABEL_21:
iconst_1
LABEL_23:

ifeq LABEL_27

iload_1
bipush 5

if_icmplt LABEL_24
iconst_0
goto LABEL_25
LABEL_24:
iconst_1
LABEL_25:
ifeq LABEL_27
iconst_1
goto LABEL_28
LABEL_27:
iconst_0
LABEL_28:
ifeq LABEL_30
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_0
invokevirtual java/io/PrintStream.println(I)V
goto LABEL_30
LABEL_30:
LABEL_LOOP_START_36:

iload_2
bipush 10

if_icmpgt LABEL_32
iconst_0
goto LABEL_33
LABEL_32:
iconst_1
LABEL_33:
ifeq LABEL_35
goto LABEL_LOOP_END_31
goto LABEL_35
LABEL_35:
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_2
invokevirtual java/io/PrintStream.println(I)V
iload_2
bipush 1
iadd
istore_2
goto LABEL_LOOP_START_36
LABEL_LOOP_END_31:
return

return
.end method

