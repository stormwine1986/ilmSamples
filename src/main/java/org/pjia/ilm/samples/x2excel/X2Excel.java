package org.pjia.ilm.samples.x2excel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 抽取Word和PDF的文字，然后拆成句子，形成Excel条目
 * 
 * @author pjia
 *
 */
public class X2Excel {
	
	public static void main(String[] args) throws IOException {
		File file = new File("Z:/1/1.doc");
		File file2 = new File("Z:/1/1.pdf");

		
		word2Excel(file);
//		pdf2Excel(file2);
		
		System.out.println("转换完成");
	}
	
	public static File pdf2Excel(File pdf) throws FileNotFoundException, IOException {
		byte[] txt = pdfToTxt(pdf);
		List<String> result = parseByteArrary(txt);
        File file = outputToExcel(result);
        
        return file;
	}
	
	public static byte[] pdfToTxt(File pdf){
		try {
			//pdf文件路径
//			String filePath = "Z:/1/测试1.pdf";
//			File fdf = new File(filePath);
			//生成的word的文件路径
//			String wordPath = "Z:/1/测试1.txt";
			//通过文件名加载文档
			PDDocument doc = PDDocument.load(pdf);
			//获取文档的页数
			int pageNumber = doc.getNumberOfPages();
			//剥离器（读取pdf文件）
			PDFTextStripper  stripper = new PDFTextStripper();
			//排序
			stripper.setSortByPosition(true);
			//设置要读取的起始页码
			stripper.setStartPage(1);
			//设置要读取的结束页码
			stripper.setEndPage(pageNumber);
//			File word = new File(wordPath);
//			if(!word.exists()){
//				word.createNewFile();
//			}
			//文件输出流
//			FileOutputStream fos = new FileOutputStream(word);
			//
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Writer  writer = new OutputStreamWriter(outputStream);
			stripper.writeText(doc, writer);
			writer.close();
//			fos.close();			
			return outputStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[] {};
		}
	}
	
	private static File word2Excel(File word) throws IOException {
		
		String txt = word2txt(word);
		
		List<String> result = parseByteArrary(txt.getBytes());
        
        File file = outputToExcel(result);
        return file;
	}
	
	private static List<String> parseByteArrary(byte[] buffer) throws UnsupportedEncodingException {
        List<String> result = new ArrayList<String>();
        
        String txt = new String(buffer, "UTF-8");
        
        if(StringUtils.isEmpty(txt)) return new ArrayList<String>();
		
        String[] split = txt.split("\\n");
        
        for(int i = 0; i < split.length; i++) {
        	String line = split[i];
        	
        	if(StringUtils.isEmpty(line)) continue;
        	
        	if(isHeading(line)) {
        		result.add(line);
        	}else {
        		
        		if(line.indexOf("。") > 0) {
        			String[] split2 = line.split("。");
        			for(int j = 0; j < split2.length; j++) {
        				result.add(split2[j]);
        			}
        		} else if(line.indexOf(".") > 0) {
        			line = preprocessNumber(line);
        			String[] split2 = line.split("\\.");
        			for(int j = 0; j < split2.length; j++) {
        				result.add(postprocessNumber(split2[j]));
        			}
        		}else {
        			result.add(line);
        		}
        	}
        }
        
        return result;
	}

	private static File outputToExcel(List<String> result) throws FileNotFoundException, IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		
		Iterator<String> iterator = result.iterator();
		int index = 0;
		while(iterator.hasNext()) {
			String next = iterator.next();
			next = next.trim();
			if(StringUtils.isEmpty(next)) continue;
			
			Row row = sheet.createRow(index++);
			row.createCell(0).setCellValue(next);
		}
		
		File file = new File("Z:/1/1.xlsx");
		if(file.exists()) {
			file.delete();
		}
		workbook.write(new FileOutputStream(file));
		
		return file;
	}

	private static String postprocessNumber(String str) {
		return str.replaceAll("。", ".");
	}

	private static String preprocessNumber(String line) {
		String pattern = "\\d+\\.\\d+";
    	Pattern r = Pattern.compile(pattern);
    	char[] charArray = line.toCharArray();
    	Matcher m = r.matcher(line);

    	while(m.find()) {
    		MatchResult matchResult = m.toMatchResult();
    		String group = matchResult.group();
    		int start = matchResult.start();
    		int indexOf = group.indexOf(".");
    		charArray[start+indexOf] = '。';
    	}
    	
    	return String.valueOf(charArray);
    	
	}
	
	private static boolean isHeading(String line) {
		String pattern = "^\\d+(\\.\\d+)*\\s+";
    	Pattern r = Pattern.compile(pattern);
    	return r.matcher(line.trim()).find();
	}
	
	private static String word2txt(File word) throws IOException {
		InputStream is = new FileInputStream(word);
        WordExtractor ex = new WordExtractor(is);
        return ex.getText();
	}
}
