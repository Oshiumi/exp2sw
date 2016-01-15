package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class ConstItem extends CParseRule {
	// constItem ::= [ MULT ] IDENT ASSIGN [ AMP ] NUMBER
	private CToken mult;
	private CToken ident;
	private CToken amp;
	private CToken num;
	public ConstItem(CParseContext pcx) {
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
		if(tk.getType() == CToken.TK_ASSIGN){
			tk = ct.getNextToken(pcx);
		}else{
			pcx.fatalError("'='がありません");
		}
		if(tk.getType() == CToken.TK_AMP){
			amp = tk;
			tk = ct.getNextToken(pcx);
		}
		if(tk.getType() == CToken.TK_NUM){
			num = tk;
			tk = ct.getNextToken(pcx);
		}else{
			pcx.fatalError("右辺に数値がありません");
		}
		
		if(pcx.getTable().search(ident.getText()) != null){
			pcx.fatalError("すでに変数が定義されています。");
		}
		
		if(mult == null){
			if(amp == null){
				// 通常の定数
				pcx.getTable().register(ident.getText(), true, CType.getCType(CType.T_int), num.getIntValue(), 0);
			}else{
				pcx.fatalError("ポインタでない変数にポインタ型の値を代入できません");
			}
		}else{
			if(amp != null){
				// ポインタの定数
				pcx.getTable().register(ident.getText(), true, CType.getCType(CType.T_pint), num.getIntValue(), 0);
			}else{
				pcx.fatalError("ポインタへの代入は&をつけなければいけません");
			}
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; constItem starts");
		String s = ident.getText();
		CType type = pcx.getTable().search(s).getType();
		if (pcx.getTable().search(s).getSize() > 0) {
			if (type == CType.getCType(CType.T_int) || type == CType.getCType(CType.T_pint)) {
				if (pcx.getTable().search(s).getConstp()) {
					o.println(s + ":\t.WORD\t" + pcx.getTable().search(s).getSize());
				} else {
					o.println(s + ":\t.WORD\t0");
				}
			} else if (type == CType.getCType(CType.T_aint) || type == CType.getCType(CType.T_apint)) {
				o.println(s + ":\t.BLKW\t" + pcx.getTable().search(s).getSize());
			}
		}
		o.println(";;; constItem completes");
	}
}
