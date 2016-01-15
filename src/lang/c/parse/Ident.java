package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Ident extends CParseRule {
	// variable ::= ident [ array ]
	private CToken ident;
	private CSymbolTableEntry symbol;
	public Ident(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {	
		return tk.getType() == CToken.TK_IDENT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		
		ident = tk;
		symbol = pcx.getTable().search(tk.getText());
		if(symbol == null){
			pcx.fatalError("変数が宣言されていません");
		}
		
		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		this.setCType(symbol.getType());
		this.setConstant(symbol.getConstp());
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; ident starts");
		if (ident != null) {
			o.println("\tMOV\t#" + ident.getText() + ", (R6)+\t; Ident: 変数アドレスを積む<"	+ ident.toExplainString() + ">");
		}
		o.println(";;; ident completes");
	}
}