package de.hhz.dbe.distributed.system.message;

public interface MessageProcessorIF {
	
	public void processMessage(MessageObject message);

}
