package com.prisma.service;

import java.nio.file.Path;

public interface ProcessFileServiceImpl {	
	public void processPdfFiles(Path fileToMovePath,String preferedName,String outputFile,String temporalStoralFile);
	public String movePdfFile(Path fileToMovePath,String preferedName,String temporalStoralFile);
}
