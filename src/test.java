import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.tinyRustBaseListener;
import generated.tinyRustParser;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Stack;

public class test extends tinyRustBaseListener implements ParseTreeListener{
    ParseTreeProperty<String> rustTree = new ParseTreeProperty<>();

    private static FileWriter fw;
    static HashMap<String, Integer> localVarMap;
    // breakLabels를 클래스 멤버로 추가
    private static Stack<String> breakLabels;
    static int localVar_curIdx = 0;

    private static int labelCount = 0; // 비교연산 할때 분기 시점 뒤에 숫자 관리

    private static void assignLocalVar(String VarName){
        if(!(localVarMap.containsKey(VarName))) localVarMap.put(VarName, localVar_curIdx);
        localVar_curIdx++; // 변수 선언되면 이 메서드가 불리고 인덱스 1 증가.
    }

    private static int getLocalVarTableIdx(String VarName) {
        return localVarMap.get(VarName);
    }

    // reset 함수 추가 (enterProgram에서 호출)
    private static void resetLabelCount() {
        labelCount = 0;
    }

    @Override public void enterProgram(tinyRustParser.ProgramContext ctx) {
        // 파일 출력
        File outputFile = new File("./Test.j");
        //변수 테이블
        localVarMap = new HashMap<>();
        resetLabelCount();
        breakLabels = new Stack<>();
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
        String main_decl = rustTree.get(ctx.main_decl());
        rustTree.put(ctx, main_decl);
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

    // 얘는 이제 메인을 빠져나갈 때 복합문 + \n + return + .end method\n\n 이렇게 문자열 노드에 결과값으로 저장하는 역할
    @Override public void exitMain_decl(tinyRustParser.Main_declContext ctx) {
        String compound_stmt = rustTree.get(ctx.compound_stmt());
        // return 추가
        rustTree.put(ctx, compound_stmt + "return\n" + ".end method\n\n");
    }

    //얘는 그냥 자기 자식으로 달려있는 지역변수 선언이랑 문장들 노드에 저장된 문자열을 싹 가져다가 문자열로 |선언들 문장들| 순서로 이어붙여서
    //노드 결과값에 저장해놓는 역할 (빠져나갈 때)
    @Override public void exitCompound_stmt(tinyRustParser.Compound_stmtContext ctx) {
        StringBuilder result = new StringBuilder();
        int local_count = ctx.local_decl().size();
        int stmt_count = ctx.stmt().size();

        // 먼저 모든 지역 변수 선언을 처리
        for (int i = 0; i < local_count; i++)
            result.append(rustTree.get(ctx.local_decl(i)));

        // 그 다음 모든 문장들을 처리
        for (int i = 0; i < stmt_count; i++)
            result.append(rustTree.get(ctx.stmt(i)));

        rustTree.put(ctx, result.toString());
    }

    //얘는 변수선언, 그리고 실제로 jvm 으로 변환하는 부분.
    // 변수 할당에 대해서 스택에 로드. 그리고 istore 해서 로컬변수에 저장하는 jvm 작성하는 부분.
    @Override public void exitLocal_decl(tinyRustParser.Local_declContext ctx) {//변수 할당(Assignment)
        String result = "";
        String val = rustTree.get(ctx.val()); // 이미 자식에서 val에 값 받아놨고 그거 가져오는 거(바이푸쉬나 아이로드 문자열)
        String id = rustTree.get(ctx.id());  // 이미 자식에서 id 값 받아놨고 그거 가져오는 거 (그냥 그 id문자)
        if(localVarMap.containsKey(id)) //이미 선언된적 있는 변수면 그 인덱스 가져와야지 당근
            result = "istore_" + getLocalVarTableIdx(id); // istore_는 스택에 저장(로드) 되있는거를 _i 면 i번째 로컬변수에 저장하는거.
        else {// 선언된 적 없을경우는 새로 숫자 할당 그러고나서 거기에 isore.
            result = "istore_" + localVar_curIdx;
            assignLocalVar(id); // 이제 변수가 선언되었으니 이거 헬퍼 메서드 불러서 해쉬맵에 저장.
        }
        rustTree.put(ctx, val + result + "\n"); // 순서를 val 먼저 함으로써 우변 값 스택에 로드하고 그 다음에 istore 불림.
    }
    @Override public void exitId(tinyRustParser.IdContext ctx) {
        rustTree.put(ctx, ctx.ID().getText());
    }

    @Override public void exitLiteral(tinyRustParser.LiteralContext ctx) {
        rustTree.put(ctx, ctx.LITERAL().getText());
    }

    //얘는 변수선언의 우변의 값 노드 처리. 얘도 jvm 으로 변환하는 부분.
    //우변노드를 나갈때 상수냐 문자냐에 따라서 bipush나 iload 해주고 한줄띄고 그걸 노드의 결과값 저장.
    //어떤 jvm 코드냐? 그냥 한줄임 우변값 가져다 쓸라고 바이푸쉬 하거나 변수면 값 아이로드로 가져오는거
    @Override public void exitVal(tinyRustParser.ValContext ctx) {//변수 할당 우변의, 할당될 값
        String result = "";
        if(ctx.literal() != null)
            result = "bipush " + rustTree.get(ctx.literal());
        else if(ctx.id() != null)
            result = "iload_" + getLocalVarTableIdx(rustTree.get(ctx.id())); // 상식적으로 선언이 된적있는 변수니까 우변에 갔다쓸 수 있는거임
        rustTree.put(ctx, result + "\n");                              // 그래서 우변에 갔다 쓸때 해쉬맵에서 그거 몇번 인덱스였는지 가져오는거고
    }

    // 얘는 그냥 각 문장들에서 결과값 가져와서 그대로 나갈때 뱉어주는 역할.
    @Override public void exitStmt(tinyRustParser.StmtContext ctx) {
        String result = "";
        if(ctx.expr_stmt() != null)
            result = rustTree.get(ctx.expr_stmt());
        else if(ctx.assignment_stmt() != null)
            result = rustTree.get(ctx.assignment_stmt());
        else if(ctx.compound_stmt() != null)
            result = rustTree.get(ctx.compound_stmt());
        else if(ctx.if_stmt() != null)  // 추가 필요
            result = rustTree.get(ctx.if_stmt());
        else if(ctx.loop_stmt() != null)   // 추가
            result = rustTree.get(ctx.loop_stmt());
        else if(ctx.break_stmt() != null)  // break도 추가
            result = rustTree.get(ctx.break_stmt());
        else if(ctx.return_stmt() != null)
            result = rustTree.get(ctx.return_stmt());
        else if(ctx.print_stmt() != null)
            result = rustTree.get(ctx.print_stmt());
        rustTree.put(ctx, result);
    }

    // expr거 가져와서 저장
    @Override public void exitExpr_stmt(tinyRustParser.Expr_stmtContext ctx) {
        rustTree.put(ctx, rustTree.get(ctx.expr()));
    }

    //relative_expr 거 가져와서 저장
    @Override public void exitExpr(tinyRustParser.ExprContext ctx) {
        rustTree.put(ctx, rustTree.get(ctx.relative_expr()));
    }

    @Override public void exitComparative_expr(tinyRustParser.Comparative_exprContext ctx) {
        String result = "";
        if(ctx.comparative_expr() != null) {
            String left = rustTree.get(ctx.comparative_expr());
            String right = rustTree.get(ctx.additive_expr());
            String op = ctx.op.getText();

            // 두 값을 비교한 후 0(false) 또는 1(true) 생성
            String label1 = "Label" + labelCount++;
            String label2 = "Label" + labelCount++;

            result = left + right +
                    "if_icmp" + getCompareOp(op) + " " + label1 + "\n" +
                    "iconst_0\n" +
                    "goto " + label2 + "\n" +
                    label1 + ":\n" +
                    "iconst_1\n" +
                    label2 + ":\n";
        } else {
            result = rustTree.get(ctx.additive_expr());
        }
        rustTree.put(ctx, result);
    }

    private String getCompareOp(String op) {
        return switch(op) {
            case ">" -> "gt";
            case "<" -> "lt";
            case ">=" -> "ge";
            case "<=" -> "le";
            case "==" -> "eq";
            case "!=" -> "ne";
            default -> throw new RuntimeException("Unknown operator: " + op);
        };
    }

    @Override public void exitRelative_expr(tinyRustParser.Relative_exprContext ctx) {
        String result = "";
        if(ctx.relative_expr() != null) {
            String left = rustTree.get(ctx.relative_expr());
            String right = rustTree.get(ctx.comparative_expr());
            String op = ctx.op.getText();

            String label1 = "Label" + labelCount++;
            String label2 = "Label" + labelCount++;

            if(op.equals("&&")) {
                result = left +
                        right +
                        "iand\n";  // 두 값의 AND 연산 수행
            } else if(op.equals("||")) {
                result = left +
                        right +
                        "ior\n";   // 두 값의 OR 연산 수행
            }
        } else {
            result = rustTree.get(ctx.comparative_expr());
        }
        rustTree.put(ctx, result);
    }
    @Override public void exitIf_stmt(tinyRustParser.If_stmtContext ctx) {
        String result = "";
        String condition = rustTree.get(ctx.relative_expr());
        String ifBlock = rustTree.get(ctx.compound_stmt(0));

        String label1 = "Label" + labelCount++;
        String label2 = "Label" + labelCount++;

        result = condition +  // 조건식 평가
                "ifeq " + label1 + "\n" +  // false면 label1으로
                ifBlock;  // true면 if 블록 실행

        if(ctx.compound_stmt().size() > 1) {  // else 블록 존재
            result += "goto " + label2 + "\n" +
                    label1 + ":\n" +
                    rustTree.get(ctx.compound_stmt(1)) +  // else 블록
                    label2 + ":\n";
        } else {
            result += label1 + ":\n";  // else 없는 경우
        }

        rustTree.put(ctx, result);
    }

    @Override public void exitLoop_stmt(tinyRustParser.Loop_stmtContext ctx) {
        String startLabel = "Label" + labelCount++;
        String endLabel = "Label" + labelCount++;

        String result = startLabel + ":\n" +
                rustTree.get(ctx.compound_stmt()) +
                "goto " + startLabel + "\n" +
                endLabel + ":\n";

        // 현재 loop의 break 레이블을 저장
        breakLabels.push(endLabel);
        rustTree.put(ctx, result);
        breakLabels.pop();
    }

    // break 문 처리
    @Override public void exitBreak_stmt(tinyRustParser.Break_stmtContext ctx) {
        rustTree.put(ctx, "goto " + breakLabels.peek() + "\n");
    }


    // 얘의 결과값은 bipush 나 iload 후에 iadd or isub 하는거.
    @Override public void exitAdditive_expr(tinyRustParser.Additive_exprContext ctx) {
        String result = "";
        String op, left, right; // 연산자, 피연산자
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

    //얘의 결과값은 bipush 나 iload 후에 곱연산 하는거.
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

    //지금은 그냥 사칙연산을 위한 factor 반환만 하고 있는데 거기에 음수 추가 및 논리연산을 위한 부정을 추가해야함.
//    @Override public void exitUnary_expr(tinyRustParser.Unary_exprContext ctx) {
//        String result = rustTree.get(ctx.factor());
//        rustTree.put(ctx, result);
//    }
    //변경 후.
    @Override public void exitUnary_expr(tinyRustParser.Unary_exprContext ctx) {
        String result = "";
        if (ctx.op != null) { // 단항 연산자가 있는 경우
            String factor = rustTree.get(ctx.factor());
            switch (ctx.op.getText()) {
                case "-" -> result = factor + "ineg\n"; // 음수 변환
                case "!" -> result = factor + "iconst_1\n" + "ixor\n"; // 논리 부정(0->1, 1->0)
            }
        } else { // 단항 연산자가 없는 경우
            result = rustTree.get(ctx.factor());
        }
        rustTree.put(ctx, result);
    }

    // 괄호연산이 지금은 구현이 되어있지 않음.
    @Override public void exitFactor(tinyRustParser.FactorContext ctx) {//expr 막바지에 호출, literal, id 터미널 호출하거나 괄호 연산
        String result = "";
        if (ctx.id() != null)
            result = "iload_" + getLocalVarTableIdx(rustTree.get(ctx.id()));
        else if (ctx.literal() != null)
            result = "bipush " + rustTree.get(ctx.literal());
        rustTree.put(ctx, result + "\n");
    }

    //    @Override public void exitAssignment_stmt(tinyRustParser.Assignment_stmtContext ctx) {
//        String expr = rustTree.get(ctx.expr());
//        rustTree.put(ctx, expr);
//    }
    @Override public void exitAssignment_stmt(tinyRustParser.Assignment_stmtContext ctx) {
        String expr = rustTree.get(ctx.expr());
        // 여기서 istore_x 를 추가해야 함
        String id = rustTree.get(ctx.id());
        rustTree.put(ctx, expr + "istore_" + getLocalVarTableIdx(id) + "\n");
    }

    @Override
    public void exitPrint_stmt(tinyRustParser.Print_stmtContext ctx) {
        String result = "getstatic java/lang/System/out Ljava/io/PrintStream;\n";
        result += "iload_" + getLocalVarTableIdx(rustTree.get(ctx.id())) + "\n";
        result += "invokevirtual java/io/PrintStream.println(I)V\n";
        rustTree.put(ctx, result);
    }

    @Override public void exitReturn_stmt(tinyRustParser.Return_stmtContext ctx) {
        rustTree.put(ctx, "return\n");
    }





}
