package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Primary extends CParseRule {
	// primary ::= primaryMult | variable
	private CParseRule primary;
	public Primary(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {	
		return PrimaryMult.isFirst(tk) || Variable.isFirst(tk);
	}
	
	public CParseRule getPrimary(){
		return primary;
	}
	
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(PrimaryMult.isFirst(tk)){
			primary = new PrimaryMult(pcx);
			primary.parse(pcx);
		}else if(Variable.isFirst(tk)){
			primary = new Variable(pcx);
			primary.parse(pcx);
		}else{
			pcx.fatalError(tk.toExplainString() + "primaryの構文定義に従っていません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (primary != null) {
			primary.semanticCheck(pcx);
			setCType(primary.getCType());
			setConstant(primary.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; primary starts");
		if (primary != null) { primary.codeGen(pcx); }
		o.println(";;; primary completes");
	}
}

class PrimaryMult extends CParseRule {
	// primaryMult ::= Mult variable
	private CToken mult;
	private CParseRule variable;
	public PrimaryMult(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {	
		return tk.getType() == CToken.TK_MULT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		
		mult = tk;
		
		tk = ct.getNextToken(pcx);
		if(Variable.isFirst(tk)){
			variable = new Variable(pcx);
			variable.parse(pcx);
		}else{
			pcx.fatalError(tk.toExplainString() + "PrimaryMultの構文定義に従っていません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (variable != null) {
			variable.semanticCheck(pcx);
			if(variable.getCType() == CType.getCType(CType.T_pint)){
				setCType(CType.getCType(CType.T_int));
				setConstant(variable.isConstant());
			}else{
				pcx.fatalError("*の後ろはpintでなければいけません");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (mult != null && variable != null) {
			variable.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; PrimaryMult: アドレスを取り出して、内容を参照して、積む<"	+ mult.toExplainString() + ">");
			o.println("\tMOV\t(R0), (R6)+\t; PrimaryMult:");
		}
	}
}