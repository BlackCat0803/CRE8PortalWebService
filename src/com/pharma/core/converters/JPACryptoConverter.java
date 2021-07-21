package com.pharma.core.converters;


import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.Base64;



public class JPACryptoConverter implements AttributeConverter<String, String> {

	static Logger logger = LoggerFactory.getLogger(JPACryptoConverter.class);

	private static String ALGORITHM = null;
	private static byte[] KEY = null;

	
	
	static {
		
		try {
			
			 String algorithm_property_key_value="";
			 String secret_property_key_value="";  
			
			
			 algorithm_property_key_value="AES/ECB/PKCS5Padding";
			 secret_property_key_value="MySuperSecretKey";
			 //System.out.println(env.getProperty(algorithm_property_key));
			 //System.out.println(env.getProperty(secret_property_key));
			 
			 ALGORITHM = algorithm_property_key_value;
			 KEY = secret_property_key_value.getBytes();

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

	}

	public String convertToDatabaseColumn(String sensitive) {
		if(sensitive!=null){
			Key key = new SecretKeySpec(KEY, "AES");
			try {
				final Cipher c = Cipher.getInstance(ALGORITHM);
				c.init(Cipher.ENCRYPT_MODE, key);
				final String encrypted = new String(Base64.encode(c
						.doFinal(sensitive.getBytes())), "UTF-8");
				//System.out.println("encrypttext===="+encrypted);
				return encrypted;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else
			return "";
	}

	public String convertToEntityAttribute(String sensitive) {
		if(sensitive!=null){
			Key key = new SecretKeySpec(KEY, "AES");
			try {
				final Cipher c = Cipher.getInstance(ALGORITHM);
				c.init(Cipher.DECRYPT_MODE, key);
				final String decrypted = new String(c.doFinal(Base64
						.decode(sensitive.getBytes("UTF-8"))));
				//System.out.println("decrypttext===="+decrypted);
				return decrypted;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else
			return "";
	}
	public static void main (String[] a)
	{
		System.out.println(new JPACryptoConverter().convertToDatabaseColumn("test"));
		System.out.println(new JPACryptoConverter().convertToEntityAttribute("hA9J2Z37UNmjHu49RJi6gQ=="));
		
		
		
	}
}