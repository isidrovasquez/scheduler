package com.prisma.scheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

@Component
public class ScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	
	@Value("${app.inputFile}")
	private   String inputFile;
	@Value("${app.outputFile}")
	private   String outputFile ;
	@Value("${app.temporalStoralFile}")
	private   String temporalStoralFile ;
	@Value("${app.localDateTime}")
	private  String localDateTimeFormat ;
	

	@Scheduled(fixedRateString="${intervalTime}")
	public void consumeFiles() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(localDateTimeFormat);
		String formatDateTime = now.format(formatter);
		log.info("The time is now {}", formatDateTime);
//		log.info("inputFile {}",inputFile);
//		log.info("outputFile {}", outputFile);
//		log.info("temporalStoralFile {}", temporalStoralFile);
		Consumer<Path> consumer = a -> processFile(a,formatDateTime);

		try (Stream<Path> paths = Files.walk(Paths.get(inputFile))) {
			paths.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".pdf"))
					.forEach(consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processFile(Path path,String formatDateTime) {
		try {
			
			String movedName = path.getFileName().toString().substring(0, path.getFileName().toString().lastIndexOf('.'))
					+ "-" + formatDateTime;
			String inFile = moveFile(path,movedName);
			
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

	public String moveFile(Path fileToMovePath,String preferedName) {

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