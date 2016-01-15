package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.*;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	private int			lineNo, colNo;
	private char		backCh;
	private boolean		backChExist = false;

	public CTokenizer(CTokenRule rule) {
		this.setRule(rule);
		lineNo = 1; colNo = 1;
	}

	private CTokenRule						rule;
	public void setRule(CTokenRule rule)	{ this.rule = rule; }
	public CTokenRule getRule()				{ return rule; }

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n')  { colNo = 1; ++lineNo; }
		//System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}
	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') { --lineNo; }
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;
	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}
	// 新しく次のトークンを読み込んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken();
		//System.out.println("Token='" + currentTk.toString());
		return currentTk;
	}
	private CToken readToken() {
		CToken tk = null;
		char ch;
		int  startCol = colNo;
		int	 bitCount = 0;
		StringBuffer text = new StringBuffer();

		int state = 0;
		boolean accept = false;
		while (!accept) {
			switch (state) {
			case 0:					// 初期状態
				ch = readChar();
				if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
				} else if (ch == (char) -1) {	// EOF
					startCol = colNo - 1;
					state = 1;
				} else if (ch >= '0' && ch <= '9') {
					bitCount = 0;
					startCol = colNo - 1;
					text.append(ch);
					bitCount++;
					if(ch == '0'){
						ch = readChar();
						if(ch >= '0' && ch <= '7'){
							bitCount = 1;
							text.append(ch);
							state = 12;
						}else if(ch == '8' || ch == '9'){
							text.append(ch);
							state = 2;
						}else if(ch == 'x' || ch == 'X'){
							bitCount = 0;
							text.append(ch);
							state = 13;
						}else{
							backChar(ch);
							state = 4;
						}
					}else{
						state = 3;
					}
				}else if((ch >= 'a' && ch <= 'x') || (ch >= 'A' && ch <= 'X') ){ 
					startCol = colNo - 1;
					text.append(ch);
					state = 19;
				}else if (ch == '+') {
					startCol = colNo - 1;
					text.append(ch);
					state = 5;
				} else if (ch == '-') {
					startCol = colNo - 1;
					text.append(ch);
					state = 6;	
				} else if (ch == '/') {
					startCol = colNo - 1;
					text.append(ch);
					state = 7;
				} else if (ch == '&') {
					startCol = colNo - 1;
					text.append(ch);
					state = 11;	
				} else if (ch == '*') {
					startCol = colNo - 1;
					text.append(ch);
					state = 14;	
				} else if (ch == '(') {
					startCol = colNo - 1;
					text.append(ch);
					state = 15;	
				} else if (ch == ')') {
					startCol = colNo - 1;
					text.append(ch);
					state = 16;	
				} else if (ch == '[') {
					startCol = colNo - 1;
					text.append(ch);
					state = 17;	
				} else if (ch == ']') {
					startCol = colNo - 1;
					text.append(ch);
					state = 18;		
				} else if (ch == '=') {
					startCol = colNo - 1;
					text.append(ch);
					state = 21;		
				} else if (ch == ';') {
					startCol = colNo - 1;
					text.append(ch);
					state = 22;	
				} else if (ch == ',') {
					startCol = colNo - 1;
					text.append(ch);
					state = 23;	
				} else {			// ヘンな文字を読んだ
					startCol = colNo - 1;
					text.append(ch);
					state = 2;
				}
				break;
			case 1:					// EOFを読んだ
				tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
				accept = true;
				break;
			case 2:					// ヘンな文字を読んだ
				tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 3:					// 数（10進数）の開始
				ch = readChar();
				if (ch >= '0' && ch <= '9') {
					bitCount++;
					text.append(ch);
				} else {
					backChar(ch);
					state = 4;
				}
				
				if(bitCount >= 10){
					state = 2;
				}
				
				break;
			case 4:					// 数の終わり
				int num = Integer.decode(text.toString()).intValue();
				
				if(num < -32767 || 65535 < num){
					state = 2;
				}else{
					tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
					accept = true;
				}
				break;
			case 5:					// +を読んだ
				tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
				accept = true;
				break;
			case 6:					// -を読んだ
				tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
				accept = true;
				break;
			case 7:					// /を読んだ
				ch = readChar();
				if(ch == '*' ){
					state = 8;
				}else if(ch == '/'){
					state = 9;
				}else{			//　除算
					backChar(ch);
					tk = new CToken(CToken.TK_DIV, lineNo, startCol, "/");
					accept = true;
				}
				break;
			case 8:					// /の後に*を読んだ
				ch = readChar();
				if(ch == '*' ){
					state = 10;
				}else if(ch == (char) -1){
					state = 2;
				}else{	// *以外ならコメント内部のためループ
				}
				break;
			case 9:					// /の後に/を読んだ
				ch = readChar();
				if(ch == (char) -1){
					state = 1;
				}else if(ch == '\n'){
					text.delete(0,1);
					state = 0;
				}else{	// EOF,\n以外ならコメント内部のためループ
				}
				break;
			case 10:					// /*...の後に*を読んだ
				ch = readChar();
				if(ch == (char) -1){
					state = 2;
				}else if(ch == '*'){
				}else if(ch == '/'){
					text.delete(0,1);
					state = 0;
				}else{	
					state = 8;
				}
				break;
			case 11:					// &を読んだ
				tk = new CToken(CToken.TK_AMP, lineNo, startCol, "&");
				accept = true;
				break;
			case 12:					// 8進数の開始
				ch = readChar();
				if(ch >= '0' && ch <= '7'){
					bitCount++;
					text.append(ch);
				}else{
					backChar(ch);
					state = 4;
				}
				
				if(bitCount >= 11){
					state = 2;
				}
				break;
			case 13:					// 16進数の開始
				ch = readChar();
				
				if((ch >= '0' && ch <= '9')	|| (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')){
					bitCount++;
					text.append(ch);
				}else{
					if(bitCount == 0){
						state = 2;
					}else{
						backChar(ch);
						state = 4;
					}
				}
				
				if(bitCount >= 5){
					state = 2;
				}
				break;
			case 14:					// *を呼んだ
				tk = new CToken(CToken.TK_MULT, lineNo, startCol, "*");
				accept = true;
				break;
			case 15:
				tk = new CToken(CToken.TK_LPAR, lineNo, startCol, "(");
				accept = true;
				break;
			case 16:
				tk = new CToken(CToken.TK_RPAR, lineNo, startCol, ")");
				accept = true;
				break;
			case 17:
				tk = new CToken(CToken.TK_LBRA, lineNo, startCol, "[");
				accept = true;
				break;
			case 18:
				tk = new CToken(CToken.TK_RBRA, lineNo, startCol, "]");
				accept = true;
				break;
			case 19:
				ch = readChar();
				if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'x' || ch >= 'A' && ch <= 'X') {
					text.append(ch);
				} else {
					backChar(ch);
					state = 20;
				}
				break;
			case 20:	// 文字列
				String str = text.toString(); 
				Integer i = (Integer)rule.get(str);
				// 切り出した字句が登録済みキーワードかどうかはi がnull かどうかで判定する
				tk = new CToken(((i == null) ? CToken.TK_IDENT : i.intValue()), lineNo, startCol, str);
				accept = true;
				break;
			case 21:
				tk = new CToken(CToken.TK_ASSIGN, lineNo, startCol, "=");
				accept = true;
				break;
			case 22:
				tk = new CToken(CToken.TK_SEMI, lineNo, startCol, ";");
				accept = true;
				break;
			case 23:
				tk = new CToken(CToken.TK_COMMA, lineNo, startCol, ",");
				accept = true;
				break;
			}
		}
		return tk;
	}

	public void skipTo(CParseContext pctx, int t) {
		int i = getCurrentToken(pctx).getType();
		while (i != t && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
	public void skipTo(CParseContext pctx, int t1, int t2) {
		int i = getCurrentToken(pctx).getType();
		while (i != t1 && i != t2 && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
	public void skipTo(CParseContext pctx, int t1, int t2, int t3, int t4, int t5, int t6) {
		int i = getCurrentToken(pctx).getType();
		while (i != t1 && i != t2 && i != t3 && i != t4 && i != t5 && i != t6 && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
}
