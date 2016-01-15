package lang.c;

import lang.SymbolTableEntry;

// 記号表へ登録するための一要素
public class CSymbolTableEntry extends SymbolTableEntry{
	private CType type;			// この識別子に対して宣言された型
	private int size;			// メモリ上に確保すべきワード数
	private boolean constp;		// 定数宣言か
	private boolean isGlobal;	// 大域変数か
	private int address;		// 割り当て番地

	public CSymbolTableEntry(CType type, int size, boolean constp, boolean isGlobal, int addr){
		this.type = type;
		this.size = size;
		this.constp = constp;
		this.isGlobal = isGlobal;
		this.address = addr;
	}
	
	public CType getType(){	return this.type; }
	public int getSize(){ return this.size; }
	public boolean getConstp(){	return this.constp; }
	public boolean getIsGlobal(){ return this.isGlobal; }
	public int getAddress(){ return this.address; }

	public String toExplainString() {
		return this.toString();
	}
}
