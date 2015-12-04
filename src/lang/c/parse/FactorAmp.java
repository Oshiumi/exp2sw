package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class FactorAmp extends CParseRule {
	// factorAMP ::= AMP ( number | primary )
	private Primary factorAmp;
	private CParseRule number;

	public FactorAmp(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_AMP;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if (Number.isFirst(tk)) {
			number = new Number(pcx);
			number.parse(pcx);
		} else if (Primary.isFirst(tk)) {
			factorAmp = new Primary(pcx);
			factorAmp.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factorAmp != null) {
			if(factorAmp.getPrimary() instanceof PrimaryMult){
				pcx.fatalError("&の後ろにポインタは置けません");
			}
			factorAmp.semanticCheck(pcx);
			setCType(CType.getCType(CType.T_pint));
			setConstant(factorAmp.isConstant()); // factorAmp は常に定数
		}else if(number != null){
			number.semanticCheck(pcx);
			setCType(CType.getCType(CType.T_pint));
			setConstant(number.isConstant()); // factorAmp は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factorAmp starts");
		if (factorAmp != null) {
			factorAmp.codeGen(pcx);
		}else if(number != null){
			number.codeGen(pcx);
		}
		o.println(";;; factorAmp completes");
	}
}
