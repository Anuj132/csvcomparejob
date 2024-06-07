package com.csvcompare;

import com.csvcompare.service.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class CsvcompareApplication implements CommandLineRunner {

	@Autowired
	ServiceImpl serviceImpl;

	@Autowired
	Environment env;

	public static int filenameChangeFlag = 0;

	private static final Logger logger = LoggerFactory.getLogger(CsvcompareApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(CsvcompareApplication.class, args);
	}

	public void run(String... args){
		String mobiFile = env.getProperty("mobi.file.location");
		String tivoFile = env.getProperty("tivo.file.location");
		logger.info("mobiFile: "+mobiFile);
		logger.info("tivoFile: "+tivoFile);
		String destinationDir = env.getProperty("destination.file.loaction");
		logger.info("destinationDir: "+destinationDir);

		File destinationObject = new File(destinationDir);
		if(!destinationObject.exists()){
			destinationObject.mkdirs();
			logger.info("Destinnation directory created successsfully!");
		} else{
			logger.info("Destination directory already found");
		}

		try {
			//Get current timestamp
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
			String timeStamp =dateFormat.format(new Date());
			Path mobiPath = Paths.get(mobiFile);
			Path tivoPath = Paths.get(tivoFile);
			String destinationMobiFileName = addTimeStampFileName(mobiPath.getFileName().toString(), timeStamp);
			String destinationTivoFileName = addTimeStampFileName(tivoPath.getFileName().toString(),timeStamp);
			Path destinationMobiPath = Paths.get(destinationDir).resolve(destinationMobiFileName);
			Path destinationTivoPath = Paths.get(destinationDir).resolve(destinationTivoFileName);
			Files.copy(mobiPath, destinationMobiPath, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(tivoPath, destinationTivoPath, StandardCopyOption.REPLACE_EXISTING);
			logger.info("destinationMobiPath.toString(): "+destinationMobiPath.toString());
			logger.info("destinationTivoPath.toString(): "+destinationTivoPath.toString());
			//serviceImpl.compareCsvFiles(mobiFile, tivoFile);
			serviceImpl.compareCsvFiles(destinationMobiPath.toString(), destinationTivoPath.toString());
		}
		catch (Exception e) {
			logger.error("Error: "+ e.getMessage());
		}
	}

	private String addTimeStampFileName(String fileName, String timeStamp){
		logger.info("fileName: "+fileName);
		int extensionIndex = fileName.lastIndexOf(".");
		logger.info("extensionIndex: "+extensionIndex);
		String nameWithoutExtension = fileName.substring(0, extensionIndex);
		logger.info("nameWithoutExtension: "+nameWithoutExtension);
		String extension = fileName.substring(extensionIndex);
		logger.info("extension: "+extension);
		if(filenameChangeFlag == 0){
			filenameChangeFlag =+1;
			return nameWithoutExtension + "_" + "mobi" + "_"+timeStamp+extension;
		}
		else{
			return nameWithoutExtension + "_" + "tivo" + "_" + timeStamp + extension;
		}
	}

}
