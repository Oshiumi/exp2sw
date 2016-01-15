package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class DeclItem extends CParseRule {
	// declItem ::= [ MULT ] IDENT [ LBRA NUMBER RBRA ]
	private CToken mult;
	private CToken ident;
	private CToken num;
	
	public DeclItem(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MULT || tk.getType() == CToken.TK_IDENT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		
		if(tk.getType() == CToken.TK_MULT){
			mult = tk;
			tk = ct.getNextToken(pcx);
		}
		if(tk.getType() == CToken.TK_IDENT){
			ident = tk;
			tk = ct.getNextToken(pcx);
		}else{
			pcx.fatalError("Identがありません");
		}
		if(tk.getType() == CToken.TK_LBRA){
			tk = ct.getNextToken(pcx);
			if(tk.getType() == CToken.TK_NUM){
				num = tk;
				tk = ct.getNextToken(pcx);
			}else{
				pcx.fatalError("'[]'の中は数字でなければなりません");
			}
			if(tk.getType() == CToken.TK_RBRA){
				tk = ct.getNextToken(pcx);
			}else{
				pcx.fatalError("'['に対応するものがありません");
			}
		}
		
		if(pcx.getTable().search(ident.getText()) != null){
			pcx.fatalError("すでに変数が宣言されています。");
		}
		
		if(mult == null){
			if(num == null){
				// 普通の変数
				pcx.getTable().register(ident.getText(), false, CType.getCType(CType.T_int), 1, 0);
			}else{
				// 配列
				pcx.getTable().register(ident.getText(), false, CType.getCType(CType.T_aint), Integer.valueOf(num.getText()), 0);
			}
		}else{
			if(num == null){
				// ポインタ変数
				pcx.getTable().register(ident.getText(), false, CType.getCType(CType.T_pint), 1, 0);
			}else{
				// ポインタ配列
				pcx.getTable().register(ident.getText(), false, CType.getCType(CType.T_apint), Integer.valueOf(num.getText()), 0);
			}
		}
		
		
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; declItem starts");
		String s = ident.getText();
		CType type = pcx.getTable().search(s).getType();
		if (pcx.getTable().search(s).getSize() > 0) {
			if (type == CType.getCType(CType.T_int) || type == CType.getCType(CType.T_pint)) {
				if (pcx.getTable().search(s).getConstp()) {
					o.println(s + ":\t.WORD\t" + pcx.getTable().search(s).getAddress());
				} else {
					o.println(s + ":\t.WORD\t0");
				}
			} else if (type == CType.getCType(CType.T_aint) || type == CType.getCType(CType.T_apint)) {
				o.println(s + ":\t.BLKW\t" + pcx.getTable().search(s).getSize());
			}
		}
		o.println(";;; decl item completes");
	}
}
