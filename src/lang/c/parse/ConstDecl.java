package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;
import java.util.ArrayList;

public class ConstDecl extends CParseRule {
	// constDecl ::= CONST INT constItem { COMMA constItem } SEMI
	private ArrayList<CParseRule> constItem;
	
	public ConstDecl(CParseContext pcx) {
		constItem = new ArrayList<CParseRule>();
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_CONST;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		
		if(tk.getType() == CToken.TK_INT){
			tk = ct.getNextToken(pcx);
		}else{
			pcx.fatalError("constの後にintがありません");
		}
		
		if(ConstItem.isFirst(tk)){
			constItem.add(new ConstItem(pcx));
			constItem.get(constItem.size()-1).parse(pcx);
			tk = ct.getCurrentToken(pcx);
			while(tk.getType() == CToken.TK_COMMA){
				tk = ct.getNextToken(pcx);
				if(ConstItem.isFirst(tk)){
					constItem.add(new ConstItem(pcx));
					constItem.get(constItem.size()-1).parse(pcx);
					tk = ct.getCurrentToken(pcx);
				}else{
					pcx.fatalError(",の後ろに変数がありません");
				}
			}	
		}else{
			pcx.fatalError("Intの後ろに変数がありません");
		}
		
		if(tk.getType() == CToken.TK_SEMI){
			tk = ct.getNextToken(pcx);
		}else{
			pcx.fatalError(";がありません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		for(int i=0;i < constItem.size(); i++){
			constItem.get(i).semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; constDecl starts");
		for(CParseRule r : constItem){
			r.codeGen(pcx);
		}
		o.println(";;; constDecl completes");
	}
}
