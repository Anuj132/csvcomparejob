package com.csvcompare.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(ServiceImpl.class);

    //compare csv file for file size, row count and keys
    public void compareCsvFiles(String mobiCsvFile, String tivoCsvFile){

        try{
            logger.info("mobiCsvFile: "+mobiCsvFile);
            logger.info("tivoCsvFile: "+tivoCsvFile);
            File mobiChannelList = new File(mobiCsvFile);
            File tivoChannelList = new File(tivoCsvFile);
            logger.info("mobiChanelList: "+mobiChannelList);
            logger.info("tivoChannelList: "+tivoChannelList);

            //check if the file Exists
            if(!mobiChannelList.exists() || !tivoChannelList.exists()){
                logger.error("one or both csv files do no exist");
                throw new FileNotFoundException("one or both csv files do no exist");
            }

            if(mobiChannelList.length() ==0 || tivoChannelList.length() ==0){
                logger.error("one or both csv files are empty");
                throw new IOException("one or both csv files are empty");
            }

            // 1- check file size
            long mobiFileSize = 0;
            long tivoFileSize = 0;
            if(mobiChannelList.isFile()){
                mobiFileSize = mobiChannelList.length();
            }
            if(tivoChannelList.isFile()){
                tivoFileSize = tivoChannelList.length();
            }

            if(mobiFileSize == tivoFileSize){
                logger.info("both of the file have same size: "+tivoFileSize+ " Byte");
            } else{
                logger.warn("both have the file is different size");
                logger.info("mobiFile size is: "+ mobiFileSize +" Byte");
                logger.info("tivoFile size is: "+ tivoFileSize +" Byte");
            }

            Map<String, List<String>> mobiCsvMap = readCsvToMap(mobiCsvFile);
            Map<String, List<String>> tivoCsvMap = readCsvToMap(tivoCsvFile);

            //2- check row count
            if(mobiCsvMap.size() != tivoCsvMap.size()){
                logger.error("Number of ro count in both the file is not same");
                logger.info("Row count in mobiFile is: "+ mobiCsvMap.size());
                logger.info("Row count in tivoFile is: "+ tivoCsvMap.size());
            } else{
                logger.info("Number of row count in both the file is same: "+ mobiCsvMap.size());
            }

            //3- Account number or key matches/mismatches
            List<String> mobiKeysNotInTivo = mobiCsvMap.keySet().stream()
                    .filter(key -> !tivoCsvMap.containsKey(key))
                    .collect(Collectors.toList());

            List<String> tivoKeysNotInMobi = tivoCsvMap.keySet().stream()
                    .filter(key -> !mobiCsvMap.containsKey(key))
                    .collect(Collectors.toList());

            if(mobiKeysNotInTivo.isEmpty() && tivoKeysNotInMobi.isEmpty()){
                logger.info("Keys are matching in both the files");
            } else{
                logger.info("Keys are not matching in both the files");
                logger.error("Keys missing in tivo (Present only in Mobi): "+ mobiKeysNotInTivo);
                logger.error("Keys missing in mobi (Present only in tivo): "+tivoKeysNotInMobi);
            }

            //4- Values misMatch for matching keys
            List<String> keysWithMisMatchedValues = mobiCsvMap.keySet().stream()
                    .filter(key -> tivoCsvMap.containsKey(key))
                    .filter(key -> !(mobiCsvMap.get(key).containsAll(tivoCsvMap.get(key)) && tivoCsvMap.get(key).containsAll(mobiCsvMap.get(key))))
                    .collect(Collectors.toList());
            if(keysWithMisMatchedValues.isEmpty()){
                logger.info("Success: All the values are matching in both the csv files");
            } else{
                logger.error("Matching keys with mismatched values: "+ keysWithMisMatchedValues);
            }

        }
        catch (FileNotFoundException e){
            logger.error("File not found exception: "+ e.getMessage());
        }
        catch (IOException e){
            logger.error("IO exception: "+ e.getMessage());
        }
        catch (Exception e){
            logger.error("General Exception: "+ e.getMessage());
        }
    }

    public Map<String, List<String>> readCsvToMap(String fileName) throws IOException{
        Map<String, List<String>> resultMap = new HashMap<>();
        Stream<String> dataStream = Files.lines(Paths.get(fileName));

        try{
            resultMap = dataStream.map(line -> line.split(",",2))
                .collect(Collectors.toMap(
                    line -> line[0],
                    line -> {
                        List<String> valueList = new ArrayList<>();
                        if(line.length>1){
                            valueList.addAll(Arrays.asList(line[1].split(",")));
                            valueList = valueList.stream().map(element -> element.equals("UPLIFT")? "GEMSN": element)
                                    .collect(Collectors.toList());
                        } else{
                            valueList.add(" ");
                        }
                        logger.info("Channel count in file: "+fileName+ " for Account: "+ line[0]+ " is "+ valueList.size());
                        return valueList;
                    }
                ));
            dataStream.close();
        }
        catch (Exception e){
            logger.error("Exception occured while reading csv: " +e.getMessage());
        }
        return resultMap;
    }
}
