package lang.c.parse;

import java.io.PrintStream;
import lang.*;
import lang.c.*;
import java.util.ArrayList;

public class Program extends CParseRule {
	// program ::= { declaration } { statement } EOF
	private CParseRule statement;
	private ArrayList<CParseRule> statementArray = new ArrayList<CParseRule>();
	private CParseRule declaration;
	private ArrayList<CParseRule> declarationArray = new ArrayList<CParseRule>();

	public Program(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Declaration.isFirst(tk) || Statement.isFirst(tk) || tk.getType() == CToken.TK_EOF;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		
		while(Statement.isFirst(tk) || Declaration.isFirst(tk)){
			if(Statement.isFirst(tk)){
				statement = new Statement(pcx);
				statement.parse(pcx);
				statementArray.add(statement);
			}else{
				declaration = new Declaration(pcx);
				declaration.parse(pcx);
				declarationArray.add(declaration);
			}
			tk = ct.getCurrentToken(pcx);
		}
		
		tk = ct.getCurrentToken(pcx);
		if (tk.getType() != CToken.TK_EOF) {
			pcx.fatalError(tk.toExplainString() + "プログラムの最後にゴミがあります");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		CParseRule st;
		CParseRule decl;
		for(int i=0;i < declarationArray.size();i++){
			decl = declarationArray.get(i);
			decl.semanticCheck(pcx);
			setCType(decl.getCType());
			setConstant(decl.isConstant());
		}
		
		for(int i=0;i < statementArray.size();i++){
			st = statementArray.get(i);
			st.semanticCheck(pcx);
			setCType(st.getCType());
			setConstant(st.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; program starts");
		o.println("\t. = 0x100");
		o.println("\tJMP\t__START\t; ProgramNode: 最初の実行文へ");
		for(int i=0;i < declarationArray.size();i++){
			CParseRule decl = declarationArray.get(i);
			decl.codeGen(pcx);
		}
		o.println("__START:");
		o.println("\tMOV\t#0x1000, R6\t; ProgramNode: 計算用スタック初期化");
		for(int i=0;i < statementArray.size();i++){
			CParseRule st = statementArray.get(i);
			st.codeGen(pcx);
		}
		o.println("\tMOV\t-(R6), R0\t; ProgramNode: 計算結果確認用");
		o.println("\tHLT\t\t\t; ProgramNode:");
		o.println("\t.END\t\t\t; ProgramNode:");
		o.println(";;; program completes");
	}
}
