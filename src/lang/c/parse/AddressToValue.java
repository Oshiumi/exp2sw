package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class AddressToValue extends CParseRule {
	// addressToValue ::= primary
	private CParseRule primary;
	public AddressToValue(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {	
		return Primary.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		
		if(Primary.isFirst(tk)){
			primary = new Primary(pcx);
			primary.parse(pcx);
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
		o.println(";;; addressToValue starts");
		if (primary != null) { primary.codeGen(pcx); }
		o.println("\tMOV\t-(R6),\tR0\t;変数の番地を取り出す");
		o.println("\tMOV\t(R0),\t(R6)+\t;変数の値を積む");
		o.println(";;; addressToValue completes");
	}
}