import generated.tinyRustBaseListener;
import generated.tinyRustParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


public class tinyRustListener extends tinyRustBaseListener {

    ParseTreeProperty<String> rustTree = new ParseTreeProperty<>();

    private static FileWriter fw;
    static HashMap<String, Integer> localVarMap;

    static int localVar_curIdx = 0;
    static int count = 0;
    private static Stack<String> break_stack;
    private static Stack<Integer> for_stack;
    private String func_ret_type = "";



    private static void assignLocalVar(String VarName){
        if(!(localVarMap.containsKey(VarName))) localVarMap.put(VarName, localVar_curIdx);
        localVar_curIdx++;
    }

    private static int getLocalVarTableIdx(String VarName) {
        return localVarMap.get(VarName);
    }

    @Override public void enterProgram(tinyRustParser.ProgramContext ctx) {
        // 파일 출력
        File outputFile = new File("./Test.j");
        //변수 테이블
        localVarMap = new HashMap<>();
        break_stack = new Stack<>();
        for_stack = new Stack<Integer>();


        try {
            if (!outputFile.exists()) {
                if(!outputFile.createNewFile()) throw new Exception("파일 생성 실패");
            }

            fw = new FileWriter(outputFile);
            fw.write("""
                .class public Test
                .super java/lang/Object
                ; strandard initializer
                .method public <init>()V
                aload_0
                invokenonvirtual java/lang/Object/<init>()V
                return
                .end method
                
                """);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test 클래스 정의 및 기본 생성자

    }

    @Override public void exitProgram(tinyRustParser.ProgramContext ctx) {
        StringBuilder program = new StringBuilder();
        for (int i = 0; i < ctx.decl().size(); i++)
            program.append(rustTree.get(ctx.decl(i)));
        // 프로그램 끝 : output 파일에 write
        try {
            fw.write(program.toString());
            fw.flush();

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override public void exitDecl(tinyRustParser.DeclContext ctx) {
        String result = "";
        if (ctx.fun_decl() != null) {
            result = rustTree.get(ctx.fun_decl());
        } else {
            result = rustTree.get(ctx.main_decl());
        }
        rustTree.put(ctx, result);
        localVar_curIdx = 0;
    }


    @Override public void enterMain_decl(tinyRustParser.Main_declContext ctx) {
        // Main_decl은 main 함수이므로 main을 위한 자료구조 및 변수 초기화
        try {
            fw.write("""
                                
                .method public static main([Ljava/lang/String;)V
                .limit stack 32
                .limit locals 32
                """);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void exitMain_decl(tinyRustParser.Main_declContext ctx) {
        String compound_stmt = rustTree.get(ctx.compound_stmt());
        rustTree.put(ctx, compound_stmt + "\n" + "return\n" + ".end method\n\n");
    }

    @Override
    public void enterFun_decl(tinyRustParser.Fun_declContext ctx) {

        String id = ctx.id().getText();

        int parameter_count = 0;
        String parameter = "";
        String result = "";
        String return_type = "";
        if (ctx.params() != null) {
            parameter_count = ctx.params().param().size();
        }

        for (int i = 0; i < parameter_count; i++) {
            parameter += "I";
        }

        //System.out.println(ctx.ret_type_spec().getText());

        if (ctx.ret_type_spec().getText() == null) {
            //System.out.println("kkkkkkkk");
            return_type = "V";
        } else {
            //System.out.println("hhhhhhhhhhhh");
            return_type = "I";
        }

        result += ".method public static " + id + "(" + parameter + ")" + return_type + "\n";
        result += ".limit stack 32" + "\n";
        result += ".limit locals 32" + "\n";


        rustTree.put(ctx, result);

    }

    @Override
    public void exitFun_decl(tinyRustParser.Fun_declContext ctx) {
        String result = rustTree.get(ctx);
        String compound_stmt = rustTree.get(ctx.compound_stmt());
        result += compound_stmt;
        if(ctx.ret_type_spec() != null) {
            result += "ireturn\n";
        } else {
            result += "return\n";
        }

        result += ".end method\n";

        rustTree.put(ctx, result);
    }

    @Override
    public void exitParams(tinyRustParser.ParamsContext ctx) {
        String params = "";
        if (ctx.param() == null) {
            rustTree.put(ctx, params);
        } else {
            for (int i = 0; i < ctx.param().size(); i++) {
                params += rustTree.get(ctx.param(i));
            }
            rustTree.put(ctx, params);
        }
    }

    @Override
    public void exitParam(tinyRustParser.ParamContext ctx) {
        String id = ctx.id().getText();
        String result = "";
        assignLocalVar(id);
        rustTree.put(ctx, result);
    }

    @Override
    public void exitRet_type_spec(tinyRustParser.Ret_type_specContext ctx) {
        if (ctx.type_spec() != null) {
            String type_spec = ctx.type_spec().getText();
            rustTree.put(ctx, type_spec);


        }
    }

    @Override public void exitCompound_stmt(tinyRustParser.Compound_stmtContext ctx) {
        StringBuilder result = new StringBuilder();
        int local_count = ctx.local_decl().size();
        int stmt_count = ctx.stmt().size();
        for (int i = 0; i < local_count; i++)
            result.append(rustTree.get(ctx.local_decl(i)));
        for (int i = 0; i < stmt_count; i++)
            result.append(rustTree.get(ctx.stmt(i)));
        rustTree.put(ctx, result.toString());
    }

    @Override public void exitLocal_decl(tinyRustParser.Local_declContext ctx) {//변수 할당(Assignment)
        String result = "";
        String val = rustTree.get(ctx.val());
        String id = rustTree.get(ctx.id());
        if(localVarMap.containsKey(id))
            result = "istore_" + getLocalVarTableIdx(id);
        else {
            result = "istore_" + localVar_curIdx;
            assignLocalVar(id);
        }
        rustTree.put(ctx, val + result + "\n");
    }

    @Override public void exitVal(tinyRustParser.ValContext ctx) {//변수 할당 우변의, 할당될 값
        String result = "";
        if(ctx.literal() != null)
            result = "bipush " + rustTree.get(ctx.literal());
        else if(ctx.id() != null)
            result = "iload_" + getLocalVarTableIdx(rustTree.get(ctx.id()));
        rustTree.put(ctx, result + "\n");
    }

    @Override public void exitStmt(tinyRustParser.StmtContext ctx) {
        String result = "";
        if(ctx.expr_stmt() != null)
            result = rustTree.get(ctx.expr_stmt());
        else if(ctx.assignment_stmt() != null)
            result = rustTree.get(ctx.assignment_stmt());
        else if(ctx.compound_stmt() != null)
            result = rustTree.get(ctx.compound_stmt());
        else if(ctx.if_stmt() != null)
            result = rustTree.get(ctx.if_stmt());
        else if(ctx.for_stmt() != null)
            result = rustTree.get(ctx.for_stmt());
        else if(ctx.return_stmt() != null)
            result = rustTree.get(ctx.return_stmt());
        else if(ctx.break_stmt() != null)
            result = rustTree.get(ctx.break_stmt());
        else if(ctx.loop_stmt() != null)
            result = rustTree.get(ctx.loop_stmt());
        else if(ctx.print_stmt() != null)
            result = rustTree.get(ctx.print_stmt());

        rustTree.put(ctx, result);
    }

    @Override public void exitExpr_stmt(tinyRustParser.Expr_stmtContext ctx) {
        rustTree.put(ctx, rustTree.get(ctx.expr()));
    }

    @Override public void exitExpr(tinyRustParser.ExprContext ctx) {
        rustTree.put(ctx, rustTree.get(ctx.relative_expr()));
    }

    @Override public void exitAdditive_expr(tinyRustParser.Additive_exprContext ctx) {
        String result = "";
        String op, left, right;
        if(ctx.additive_expr() != null) {
            left = rustTree.get(ctx.additive_expr());
            op = ctx.getChild(1).getText();
            if(op.equals("+"))
                op = "iadd\n";
            else if(op.equals("-"))
                op = "isub\n";
            right = rustTree.get(ctx.multiplicative_expr());
            result = left + right + op;
        } else
            result = rustTree.get(ctx.multiplicative_expr());
        rustTree.put(ctx, result);
    }

    @Override public void exitMultiplicative_expr(tinyRustParser.Multiplicative_exprContext ctx) {
        String result = "";
        String op, left, right;
        if(ctx.multiplicative_expr() != null) {
            left = rustTree.get(ctx.multiplicative_expr());
            op = ctx.getChild(1).getText();
            switch (op) {
                case "*" -> op = "imul\n";
                case "/" -> op = "idiv\n";
                case "%" -> op = "irem\n";
            }
            right = rustTree.get(ctx.unary_expr());
            result = left + right + op;
        } else
            result = rustTree.get(ctx.unary_expr());
        rustTree.put(ctx, result);
    }

    @Override public void exitUnary_expr(tinyRustParser.Unary_exprContext ctx) {
        String result = rustTree.get(ctx.factor());
        rustTree.put(ctx, result);
    }


    @Override public void exitFactor(tinyRustParser.FactorContext ctx) {//expr 막바지에 호출, literal, id 터미널 호출하거나 괄호 연산
        String result = "";
        String functionName = "";


        if (ctx.args() != null) {
            //System.out.println("here");
            functionName = ctx.id().getText();

            String args = rustTree.get(ctx.args());
            result += args;

            String paramType = "";
            String returnType = "";

            for (int i = 0; i < ctx.args().expr().size(); i++) {
                paramType += "I";
            }
            returnType = "I";


            result += "invokestatic Test/" + functionName + "(" + paramType + ")" + returnType + "\n";

        } else if (ctx.id() != null) {
            result = "iload_" + getLocalVarTableIdx(rustTree.get(ctx.id()));
        } else if (ctx.literal() != null) {
            result = "bipush " + rustTree.get(ctx.literal());
        } else if (ctx.expr() != null) {
            result = rustTree.get(ctx.expr());
        }

        rustTree.put(ctx, result + "\n");

    }

    @Override
    public void exitArgs(tinyRustParser.ArgsContext ctx) {
        String result = "";
        for (int i = 0; i < ctx.expr().size(); i++) {
            result += rustTree.get(ctx.expr(i));
        }
        rustTree.put(ctx, result);

    }

    @Override public void exitComparative_expr(tinyRustParser.Comparative_exprContext ctx) {
        String result = "";
        String op, left, right;

        if(ctx.comparative_expr() != null) {
            left = rustTree.get(ctx.comparative_expr());
            right = rustTree.get(ctx.additive_expr());
            op = ctx.op.getText();

            result += "\n";
            result += left;
            result += right;
            result += "\n";

            String label1 = "LABEL_" + count++;
            String label2 = "LABEL_" + count++;

            if (op.equals("==")) {
                result += "if_icmpeq ";
            } else if (op.equals("!=")) {
                result += "if_icmpne ";
            } else if (op.equals("<")) {
                result += "if_icmplt ";
            } else if (op.equals("<=")) {
                result += "if_icmple ";
            } else if (op.equals(">")) {
                result += "if_icmpgt ";
            } else if (op.equals(">=")) {
                result += "if_icmpge ";
            }
            result += label1 + "\n"; //label1로 이동

            result += "iconst_0\n"; //false면 0저장후 다른 label2로 이동
            result += "goto " + label2 + "\n";

            result += label1 + ":\n";
            result += "iconst_1\n"; //true니까 1저장

            result += label2 + ":\n";
        } else
            result = rustTree.get(ctx.additive_expr());
        rustTree.put(ctx, result);
    }




    @Override
    public void exitIf_stmt(tinyRustParser.If_stmtContext ctx) {
        String condition = rustTree.get(ctx.relative_expr());
        String if_block = rustTree.get(ctx.compound_stmt(0));
        String else_block = "";

        if (ctx.ELSE() != null) {
            else_block = rustTree.get(ctx.compound_stmt(1));
        }

        String label_else = "LABEL_" + count++; // else 블록 라벨
        String label_end = "LABEL_" + count++;  // 종료 라벨

        String result = "";

        // 조건문 결과 생성
        result += condition; // 조건문 결과를 스택에 푸시
        if (!else_block.isEmpty()) {
            result += "ifeq " + label_else + "\n";
        } else {
            result += "ifeq " + label_end + "\n";
        }


        result += if_block;
        result += "goto " + label_end + "\n";

        // else 블록
        if (!else_block.isEmpty()) {
            result += label_else + ":\n";
            result += else_block;
        }

        result += label_end + ":\n";

        rustTree.put(ctx, result);
    }


    @Override public void exitRelative_expr(tinyRustParser.Relative_exprContext ctx) {
        String result = "";
        if (ctx.relative_expr() != null) {
            String left = rustTree.get(ctx.relative_expr());
            String right = rustTree.get(ctx.comparative_expr());
            String op = ctx.getChild(1).getText();

            String label_true = "LABEL_" + count++;
            String label_false = "LABEL_" + count++;
            String label_end = "LABEL_" + count++;

            if (op.equals("&&")) {
                result = left +
                        "ifeq " + label_false + "\n" +
                        right +
                        "ifeq " + label_false + "\n" +
                        "iconst_1\n" +
                        "goto " + label_end + "\n" +
                        label_false + ":\n" +
                        "iconst_0\n" +
                        label_end + ":\n";
            } else if (op.equals("||")) {

                result = left +
                        "ifne " + label_true + "\n" +
                        right +
                        "ifne " + label_true + "\n" +
                        "iconst_0\n" +
                        "goto " + label_end + "\n" +
                        label_true + ":\n" +
                        "iconst_1\n" +
                        label_end + ":\n";
            }
        } else {
            result = rustTree.get(ctx.comparative_expr());
        }
        rustTree.put(ctx, result);
    }



    @Override public void exitAssignment_stmt(tinyRustParser.Assignment_stmtContext ctx) {
        String expr = rustTree.get(ctx.expr());
        String result = expr + "istore_" + getLocalVarTableIdx(rustTree.get(ctx.id())) + "\n";
        rustTree.put(ctx, result);
    }

    @Override
    public void exitPrint_stmt(tinyRustParser.Print_stmtContext ctx) {
        String result = "getstatic java/lang/System/out Ljava/io/PrintStream;\n";
        result += "iload_" + getLocalVarTableIdx(rustTree.get(ctx.id())) + "\n";
        result += "invokevirtual java/io/PrintStream.println(I)V\n";
        rustTree.put(ctx, result);
    }

    @Override public void exitReturn_stmt(tinyRustParser.Return_stmtContext ctx) {
        String result = "";
        if (ctx.expr() != null) {
            result += rustTree.get(ctx.expr());
        } else {
            result += "return\n";
        }
        rustTree.put(ctx, result);
    }

    @Override public void exitLiteral(tinyRustParser.LiteralContext ctx) {
        rustTree.put(ctx, ctx.LITERAL().getText());
    }

    @Override public void exitId(tinyRustParser.IdContext ctx) {
        rustTree.put(ctx, ctx.ID().getText());
    }

    @Override
    public void enterLoop_stmt(tinyRustParser.Loop_stmtContext ctx) {
        String label_end = "LABEL_LOOP_END_" + count++;
        break_stack.push(label_end);
    }

    @Override
    public void exitLoop_stmt(tinyRustParser.Loop_stmtContext ctx) {
        String result = "";
        String loopBody = rustTree.get(ctx.compound_stmt());
        String label_start = "LABEL_LOOP_START_" + count++;
        String label_end = break_stack.peek();


        result += label_start + ":\n";
        break_stack.push(label_start);

        result += loopBody;

        result += "goto " + label_start + "\n";
        result += label_end + ":\n";

        rustTree.put(ctx, result);
        break_stack.pop();
    }

    @Override
    public void exitBreak_stmt(tinyRustParser.Break_stmtContext ctx) {
        String result = "";
        result = "goto " + break_stack.peek() + "\n";
        rustTree.put(ctx, result);
    }

    @Override
    public void enterFor_stmt(tinyRustParser.For_stmtContext ctx) {
        String result = "";
        String id = ctx.id().getText();
        String range1 = ctx.range().literal(0).getText();
        result += "bipush " + range1 + "\n";
        if(localVarMap.containsKey(id)) {
            result += "istore_" + getLocalVarTableIdx(id) + "\n";
            for_stack.push(getLocalVarTableIdx(id));
        }
        else {
            result += "istore_" + localVar_curIdx + "\n";
            assignLocalVar(id);
            for_stack.push(localVar_curIdx);
        }
        rustTree.put(ctx,result);
    }

    @Override
    public void exitFor_stmt(tinyRustParser.For_stmtContext ctx) {
        String result = "";
        String id = ctx.id().getText();
        String range2 = ctx.range().literal(1).getText();
        String compound_stmt = rustTree.get(ctx.compound_stmt());

        String label_start = "LABEL_FOR_START_" + count++;
        String label_end = "LABEL_FOR_END_" + count++;


        String range1 = ctx.range().literal(0).getText();
        result += "bipush " + range1 + "\n";
        if(localVarMap.containsKey(id)) {
            result += "istore_" + getLocalVarTableIdx(id) + "\n";
            for_stack.push(getLocalVarTableIdx(id));
        }
        else {
            result += "istore_" + localVar_curIdx + "\n";
            assignLocalVar(id);
            for_stack.push(localVar_curIdx);
        }

        result += label_start + ":\n";
        result += "iload_" + for_stack.peek() + "\n";
        result += "bipush " + range2 + "\n";

        if (ctx.range().getChild(2).getText().equals("==")) {
            result += "if_icmpgt " + label_end + "\n";
        } else {
            result += "if_icmpge " + label_end + "\n";
        }

        result += compound_stmt;
        result += "iload_" + getLocalVarTableIdx(id) + "\n";
        result += "iconst_1" + "\n";
        result += "iadd\n";
        result += "istore_" + getLocalVarTableIdx(id) + "\n";

        result += "goto " + label_start + "\n";

        result += label_end + ":\n";

        rustTree.put(ctx,result);
    }


}
