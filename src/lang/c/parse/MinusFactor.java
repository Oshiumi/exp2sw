package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class MinusFactor extends CParseRule {
	// MinusFactor ::= MINUS unsignedFactor
	private CParseRule number;
	public MinusFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MINUS;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		
		if(unsignedFactor.isFirst(tk)){
			number = new unsignedFactor(pcx);
			number.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			if(number.getCType().getType() == CType.T_int){
				setCType(number.getCType());		// number の型をそのままコピー
				setConstant(number.isConstant());	// number は常に定数
			}else{
				pcx.fatalError("マイナスの後に&は不可");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factor starts");
		if (number != null) { number.codeGen(pcx); }
		o.println("\tMOV\t#0, R0\t;Minus Factor");
		o.println("\tSUB\t-(R6), R0\t;Minus Factor");
		o.println("\tMOV\tR0, (R6)+\t;Minus Factor");
		o.println(";;; factor completes");
	}
}