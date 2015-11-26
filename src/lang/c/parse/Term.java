package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Term extends CParseRule {
	// term ::= factor { termMult | termDiv }
	private CParseRule term;
	public Term(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Factor.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CParseRule factor = null, list = null;
		factor = new Factor(pcx);
		factor.parse(pcx);
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		while (true) {
			if(termMult.isFirst(tk)){
				list = new termMult(pcx, factor);
			}else if(termDiv.isFirst(tk)){
				list = new termDiv(pcx, factor);
			}else{
				break;
			}
			list.parse(pcx);
			factor = list;
			tk = ct.getCurrentToken(pcx);
		}
		term = factor;
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (term != null) {
			term.semanticCheck(pcx);
			this.setCType(term.getCType());		// factor の型をそのままコピー
			this.setConstant(term.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; term starts");
		if (term != null) { term.codeGen(pcx); }
		o.println(";;; term completes");
	}
}

class termMult extends CParseRule{
	// termMult ::= MULT factor
	private CToken mult;
	private CParseRule left, right;
	
	public termMult(CParseContext cpx, CParseRule left){
		this.left = left;
	}
	
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MULT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		mult = ct.getCurrentToken(pcx);
		// *の次の字句を読む
		CToken tk = ct.getNextToken(pcx);
		if (Factor.isFirst(tk)) {
			right = new Factor(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "*の後ろはfactorです");
		}
		
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		// かけ算の型計算規則
			final int s[][] = {
			//		T_err			T_int			T_pint
				{	CType.T_err,	CType.T_err,	CType.T_err },	// T_err
				{	CType.T_err,	CType.T_int,	CType.T_err },	// T_int
				{	CType.T_err,	CType.T_err,	CType.T_err },	// T_pint
			};
			int lt = 0, rt = 0;
			boolean lc = false, rc = false;
			if (left != null) {
				left.semanticCheck(pcx);
				lt = left.getCType().getType();		// *の左辺の型
				lc = left.isConstant();
			} else {
				pcx.fatalError(mult.toExplainString() + "左辺がありません");
			}
			if (right != null) {
				right.semanticCheck(pcx);
				rt = right.getCType().getType();	// *の右辺の型
				rc = right.isConstant();
			} else {
				pcx.fatalError(mult.toExplainString() + "右辺がありません");
			}
			int nt = s[lt][rt];						// 規則による型計算
			if (nt == CType.T_err) {
				pcx.fatalError(mult.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は乗算できません");
			}
			this.setCType(CType.getCType(nt));
			this.setConstant(lc && rc);				// *の左右両方が定数のときだけ定数
	}
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			o.println("\tJSR\tMUL; termMult: MULを呼び出す<" + mult.toExplainString() + ">");
			o.println("\tSUB\t#2, R6\t; termMult:");
			o.println("\tMOV\tR0, (R6)+\t; termMult:");
		}
	}
}

class termDiv extends CParseRule{
	// termDiv ::= DIV factor
	private CToken div;
	private CParseRule left, right;
	
	public termDiv(CParseContext cpx, CParseRule left){
		this.left = left;
	}
	
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_DIV;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		div = ct.getCurrentToken(pcx);
		// /の次の字句を読む
		CToken tk = ct.getNextToken(pcx);
		if (Factor.isFirst(tk)) {
			right = new Factor(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "/の後ろはfactorです");
		}
		
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		//　除算の型計算規則
			final int s[][] = {
			//		T_err			T_int			T_pint
				{	CType.T_err,	CType.T_err,	CType.T_err },	// T_err
				{	CType.T_err,	CType.T_int,	CType.T_err },	// T_int
				{	CType.T_err,	CType.T_err,	CType.T_err },	// T_pint
			};
			int lt = 0, rt = 0;
			boolean lc = false, rc = false;
			if (left != null) {
				left.semanticCheck(pcx);
				lt = left.getCType().getType();		// /の左辺の型
				lc = left.isConstant();
			} else {
				pcx.fatalError(div.toExplainString() + "左辺がありません");
			}
			if (right != null) {
				right.semanticCheck(pcx);
				rt = right.getCType().getType();	// /の右辺の型
				rc = right.isConstant();
			} else {
				pcx.fatalError(div.toExplainString() + "右辺がありません");
			}
			int nt = s[lt][rt];						// 規則による型計算
			if (nt == CType.T_err) {
				pcx.fatalError(div.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は乗算できません");
			}
			this.setCType(CType.getCType(nt));
			this.setConstant(lc && rc);				// /の左右両方が定数のときだけ定数
	}
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			o.println("\tJSR\tDIV	  ; termDiv: DIVを呼び出す<" + div.toExplainString() + ">");
			o.println("\tSUB\t#2, R6\t; termDiv:");
			o.println("\tMOV\tR0, (R6)+\t; termDiv:");
		}
	}
}
