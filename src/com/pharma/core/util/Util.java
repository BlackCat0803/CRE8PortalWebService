package com.pharma.core.util;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;

import javax.swing.text.rtf.RTFEditorKit;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component("util")
public class Util {

	public static int getIntAmount(String strAmount) {
		int amount = 0;
		try {
			if (strAmount != null && !strAmount.equals("")) {
			 	 Double dObj = new Double(strAmount);
				 amount =dObj.intValue();
			}
		} catch (Exception err) {
			amount = 0;
		}
		return amount;
	}

	public static java.sql.Timestamp getSqlTimeStampFromString(String date) {
		SimpleDateFormat inDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		java.sql.Timestamp dt = null;
		try {
			dt = new java.sql.Timestamp( inDate.parse(date).getTime() );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dt;
	}
	
	public static java.sql.Timestamp getSqlTimeStampFromString2(String date) {
		SimpleDateFormat inDate = new SimpleDateFormat("MMM dd, yyyy");
		
		java.sql.Timestamp dt = null;
		try {
			dt = new java.sql.Timestamp( inDate.parse(date).getTime() );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dt;
	}
	public static void main (String[] a)
	{
		
		/*StringTextConverter converter = new StringTextConverter();
		converter.convert(new RtfStreamSource(inputStream));
		String extractedText = converter.getText();*/
		/*String HiddenComment="{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl{\\f0\\fnil\\fcharset0 Microsoft Sans Serif;}}\\viewkind4\\uc1\\pard\\f0\\fs17 CRE8-1030\\par}";
		//Pattern p = Pattern.compile("@{\\rtf1[\\\w]+(?<fontInfo>{\\fonttbl{[^}]*}})?([\r\n]*)(?<colorInfo>{\\colortbl[\s\\\w\d;]*})?([\r\n]*)(?<innerText>.*)}([\r\n]*)$");
	    //Pattern p = Pattern.compile("cat(z?s?)");
	   // Matcher m = p.matcher("one catz two cats in the yard");
	    
		 Regex content = new Regex("^[{\\rtf1[\\\w]+(?<fontInfo>{\\fonttbl{[^}]*}})?([\r\n]*)(?<colorInfo>{\\colortbl[\s\\\w\d;]*})?([\r\n]*)(?<innerText>.*)}([\r\n]*)]$",RegexOptions.Singleline);

	            Match contentMatch = content.Match(rtfText);
	            string htmlString = string.Empty;
	            
		HiddenComment=HiddenComment.replaceAll("\rtf1"," ");
		HiddenComment=HiddenComment.replaceAll("\\ansi","");
		HiddenComment=HiddenComment.replaceAll("\\ansicpg1252","");
		HiddenComment=HiddenComment.replaceAll("\\deff0","");
		HiddenComment=HiddenComment.replaceAll("\\deflang1033","");
		HiddenComment=HiddenComment.replaceAll("\\fonttbl","");
		HiddenComment=HiddenComment.replaceAll("\\f0","");
		HiddenComment=HiddenComment.replaceAll("\\fnil","");
		HiddenComment=HiddenComment.replaceAll("\fcharset0 Microsoft Sans Serif;","");
		HiddenComment=HiddenComment.replaceAll("\\viewkind4","");
		
		HiddenComment=HiddenComment.replaceAll("[\\uc1]","");
		HiddenComment=HiddenComment.replaceAll("\\pard","");
		HiddenComment=HiddenComment.replaceAll("\\f0","");
		HiddenComment=HiddenComment.replaceAll("\\fs17 ","");
		HiddenComment=HiddenComment.replaceAll("\\par","");
		System.out.println("HiddenComment ==="+HiddenComment);*/
	}
}
