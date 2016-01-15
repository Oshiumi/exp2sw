package lang.c;

import lang.SymbolTable;

public class CSymbolTable {
	private class OneSymbolTable extends SymbolTable<CSymbolTableEntry> {
		@Override
		public CSymbolTableEntry register(String name, CSymbolTableEntry e) { return put(name, e); }
		@Override
		public CSymbolTableEntry search(String name) { return get(name); }
	}
	
	private OneSymbolTable global;	// 大域変数用
//	private OneSymbolTable local;	// 局所変数用
	
	public CSymbolTable(){
		this.global = new OneSymbolTable();
	}

	public CSymbolTableEntry search(String name){
		CSymbolTableEntry e;
		
		e = global.search(name);
		
		return e;
	}
	
	public CSymbolTableEntry register(String name, boolean constp, CType type, int size, int addr){
		CSymbolTableEntry e;
		
		// 大域変数用
		e = new CSymbolTableEntry(type, size, constp, true, addr);
		return global.register(name, e);
	}

}
