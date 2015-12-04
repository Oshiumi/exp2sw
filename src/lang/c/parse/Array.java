package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Array extends CParseRule {
	//array ::= LBRA expression RBRA
	private CParseRule number;
	public Array(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {	
		return tk.getType() == CToken.TK_LBRA;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		
		if(Expression.isFirst(tk)){
			number = new Expression(pcx);
			number.parse(pcx);
			
			tk = ct.getCurrentToken(pcx);
			if(tk.getType() == CToken.TK_RBRA){
				tk = ct.getNextToken(pcx);
			}else{
				pcx.fatalError(tk.toExplainString() + "Arrayの構文定義に従っていません");
			}
		}else{
			pcx.fatalError(tk.toExplainString() + "Arrayの構文定義に従っていません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			if(number.getCType() == CType.getCType(CType.T_int)){
				setCType(number.getCType());		// number の型をそのままコピー
				setConstant(number.isConstant());	// number は常に定数
			}else{
				pcx.fatalError("Arrayの添え字はintでなければいけません");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; Array starts");
		if (number != null) { number.codeGen(pcx); }
		o.println("\tMOV\t-(R6),\tR0\t;配列の添え字を取り出す");
		o.println("\tMOV\t-(R6),\tR1\t;配列の番地を取り出す");
		o.println("\tADD\tR0,\tR1\t;配列の番地から添え字分動かす");
		o.println("\tMOV\tR1,\t(R6)+\t;足した番地をスタックに積む");
		
		o.println(";;; Array completes");
	}
}