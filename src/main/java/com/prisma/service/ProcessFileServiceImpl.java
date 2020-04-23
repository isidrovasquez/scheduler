package com.prisma.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

@Service
public class ProcessFileServiceImpl implements ProcessFileService {	

	@Override
	public void processPdfFiles(Path path,String formatDateTime,String outputFile,String temporalStoralFile)  {
try {
			
			String movedName = path.getFileName().toString().substring(0, path.getFileName().toString().lastIndexOf('.'))
					+ "-" + formatDateTime;
			String inFile = movePdfFile(path,movedName, temporalStoralFile);
			
			System.out.println("Reading " + inFile);
			PdfReader reader = new PdfReader(inFile);
			int n = reader.getNumberOfPages();
			System.out.println("Number of pages : " + n);
			int i = 0;
			while (i < n) {
				String outFile = outputFile
						+ movedName
						+ "-" + String.format("%03d", i + 1) + ".pdf";
				System.out.println("Writing " + outFile);
				Document document = new Document(reader.getPageSizeWithRotation(1));
				PdfCopy writer = new PdfCopy(document, new FileOutputStream(outFile));
				document.open();
				PdfImportedPage page = writer.getImportedPage(reader, ++i);
				writer.addPage(page);
				document.close();
				writer.close();
				reader.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String movePdfFile(Path fileToMovePath,String preferedName,String temporalStoralFile) {
		Path targetPath = Paths.get(temporalStoralFile);
		String finalTemporalPath = "";

		try {
			finalTemporalPath = Files.move(fileToMovePath, targetPath.resolve(preferedName+".pdf"),StandardCopyOption.REPLACE_EXISTING ).toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return finalTemporalPath;
	}

}
