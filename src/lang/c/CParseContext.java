package lang.c;

import lang.*;

public class CParseContext extends ParseContext {
	
	private CSymbolTable table;
	
	public CParseContext(IOContext ioCtx,  CTokenizer tknz) {
		super(ioCtx, tknz);
		table = new CSymbolTable();
	}

	@Override
	public CTokenizer getTokenizer()		{ return (CTokenizer) super.getTokenizer(); }

	private int seqNo = 0;
	public int getSeqId() { return ++seqNo; }
	public CSymbolTable getTable() { return table;}
}
