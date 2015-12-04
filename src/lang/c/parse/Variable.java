package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Variable extends CParseRule {
	// variable ::= ident [ array ]
	private CParseRule array;
	private CParseRule ident;

	public Variable(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Ident.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		ident = new Ident(pcx);
		ident.parse(pcx);

		tk = ct.getCurrentToken(pcx);
		if (Array.isFirst(tk)) {
			array = new Array(pcx);
			array.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (ident != null) {
			ident.semanticCheck(pcx);
			setCType(ident.getCType());
			setConstant(ident.isConstant());
		}
		if (array != null) {
			if (ident.getCType() == CType.getCType(CType.T_aint)) {
				array.semanticCheck(pcx);
				setCType(CType.getCType(CType.T_int));
				setConstant(array.isConstant());
			} else if (ident.getCType() == CType.getCType(CType.T_apint)) {
				array.semanticCheck(pcx);
				setCType(CType.getCType(CType.T_pint));
				setConstant(array.isConstant());
			} else {
				pcx.fatalError("Identが配列の場合は型がint[]またはint*[]でなければいけません");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; variable starts");
		if (ident != null) {
			ident.codeGen(pcx);
		}
		if (array != null) {
			array.codeGen(pcx);
		}
		o.println(";;; variable completes");
	}
}