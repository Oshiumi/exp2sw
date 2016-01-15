package lang.c.parse;

import java.util.ArrayList;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class IntDecl extends CParseRule {
	// intDecl ::= INT declItem { COMMA declItem } SEMI
	private ArrayList<CParseRule> declItem;
	public IntDecl(CParseContext pcx) {
		declItem = new ArrayList<CParseRule>();
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_INT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		
		if(DeclItem.isFirst(tk)){
			declItem.add(new DeclItem(pcx));
			declItem.get(declItem.size()-1).parse(pcx);
			tk = ct.getCurrentToken(pcx);
			while(tk.getType() == CToken.TK_COMMA){
				tk = ct.getNextToken(pcx);
				if(DeclItem.isFirst(tk)){
					declItem.add(new DeclItem(pcx));
					declItem.get(declItem.size()-1).parse(pcx);
					tk = ct.getCurrentToken(pcx);
				}else{
					pcx.fatalError(",の後ろに変数がありません");
				}
			}	
		}else{
			pcx.fatalError("Intの後ろに変数がありません");
		}

		tk = ct.getCurrentToken(pcx);
		if(tk.getType() == CToken.TK_SEMI){
			tk = ct.getNextToken(pcx);
		}else{
			pcx.fatalError(";がありません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		for(int i=0;i < declItem.size(); i++){
			declItem.get(i).semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; IntDecl starts");
		for(CParseRule r : declItem){
			r.codeGen(pcx);
		}
		o.println(";;; IntDecl completes");
	}
}
