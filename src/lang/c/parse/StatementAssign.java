package lang.c.parse;

import java.io.PrintStream;
import lang.*;
import lang.c.*;

public class StatementAssign extends CParseRule {
	// statementAssign ::= primary ASSIGN expression SEMI
	private CParseRule primary;
	private CParseRule expression;

	public StatementAssign(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Primary.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		primary = new Primary(pcx);
		primary.parse(pcx);
		
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		
		if(tk.getType() == CToken.TK_ASSIGN){
			tk = ct.getNextToken(pcx);
		}else{
			pcx.fatalError("primaryの後には'='が続かなければなりません");
		}
		if(Expression.isFirst(tk)){
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);
		}else{
			pcx.fatalError("'='の後にはexpressionが続かなければなりません");
		}
		if (tk.getType() != CToken.TK_SEMI) {
			pcx.fatalError(";がありません");
		}
		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(primary != null && expression != null){
			primary.semanticCheck(pcx);
			expression.semanticCheck(pcx);
			if(primary.getCType() != expression.getCType()){
				pcx.fatalError("変数と値の型が違います。");
			}
			if(primary.isConstant()){
				pcx.fatalError("定数には代入できません");
			}
		}
		
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementAssign starts");
		// ここには将来、宣言に対するコード生成が必要
		if (primary != null) {
			primary.codeGen(pcx);
		}
		if(expression != null){
			expression.codeGen(pcx);
		}
		o.println("\tMOV\t-(R6), R0\t;代入する数値を取り出す");
		o.println("\tMOV\t-(R6), R1\t;代入先の変数を取り出す");
		o.println("\tMOV\tR0, (R1)\t;代入する数値を取り出す");
		o.println(";;; statementAssign completes");
	}
}
