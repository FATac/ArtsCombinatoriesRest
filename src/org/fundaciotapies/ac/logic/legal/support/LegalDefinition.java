package org.fundaciotapies.ac.logic.legal.support;

import java.util.List;


public class LegalDefinition {
	
	private String startBlock;
	private List<LegalBlock> blocks;
	
	public void setStartBlock(String startBlock) {
		this.startBlock = startBlock;
	}
	public String getStartBlock() {
		return startBlock;
	}
	public void setBlocks(List<LegalBlock> blocks) {
		this.blocks = blocks;
	}
	public List<LegalBlock> getBlocks() {
		return blocks;
	}
	
	public LegalBlock getBlock(String name) {
		if (name==null) return null;
		
		for(LegalBlock b : blocks)
			if (b.getName().equals(name)) {
				return b;
			}
		
		return null;
	}
	
}
