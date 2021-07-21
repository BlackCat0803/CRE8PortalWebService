package com.pharma.core.webservice.model;
 
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.pharma.core.util.PropertyConfigurer;
/**
 * 
 * The class <<PharmacyService>> is a RX Event Trigger for Pushing the RX Number for the corresponding Prescription.
 *  Creating Web Service Client Call for Capturing the RX Event Data from the Pioneer System and updating the corresponding Prescriptions and creating Order and Invoice
	RX Event Trigger for Pushing the RX Number for the corresponding Prescription
	
	1)If RX Number does not exists, the  RX Number is updated for the corresponding Prescription prescribed by the Prescriber for the Patient with the matched Prescribed Item
	Creates a New Order
	Creates a New Invoice with the Payment Status - "Not Paid" 
	
	2)If RX Number Already Exists just update the status and other info from rx

 *
 */
@Path("/pharmacy")
public class PharmacyService {
 
	 private static final String DB_DRIVER = PropertyConfigurer.getPropertyKeyValue("DB_DRIVER");
	 private static final String DB_CONNECTION1 = PropertyConfigurer.getPropertyKeyValue("DB_CONNECTION1");
	 private static final String DB_USER = PropertyConfigurer.getPropertyKeyValue("DB_USER");
	 private static final String DB_PASSWORD = PropertyConfigurer.getPropertyKeyValue("DB_PASSWORD");

	 private static final String MYSQL_DB_DRIVER = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_DRIVER");
	 private static final String MYSQL_DB_CONNECTION = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_CONNECTION");
	 private static final String MYSQL_DB_USER = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_USER");
	 private static final String MYSQL_DB_PASSWORD = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_PASSWORD");
	 //private static final String pioneer_rxnumber_cre8_no = PropertyConfigurer.getPropertyKeyValue("pioneer_rxnumber_cre8_no");
	 
	 private static final String prescription_number_format = PropertyConfigurer.getPropertyKeyValue("default.prescription_number_format");
	 private static final String defaultPrescriptionNumber = PropertyConfigurer.getPropertyKeyValue("default.prescription_number");

	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchData(InputStream incomingData) {
		
		
		
		
		
		StringBuilder strBuilder = new StringBuilder();
		String rxMessage="";
		JSONObject returnJson=new JSONObject();
		JSONObject messageHeaderJson=new JSONObject();
		JSONObject bodyContentJson=new JSONObject();
		JSONObject patientJson=new JSONObject();
		
		JSONObject rxJson=new JSONObject();
		JSONObject pharmacyJson=new JSONObject();
		JSONObject facilityJson=new JSONObject();
		
		JSONObject employeesJson=new JSONObject();
		JSONObject prescribersJson=new JSONObject();
		JSONObject claimsJson=new JSONObject();
		
		JSONObject employeeJson=new JSONObject();
		JSONObject prescriberJson=new JSONObject();
		JSONObject claimJson=new JSONObject();
		
		
		JSONArray employeeJsonArray=new JSONArray();
		JSONArray prescriberJsonArray=new JSONArray();
		JSONArray claimJsonArray=new JSONArray();
	
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				strBuilder.append(line);
			}
		
			System.out.println("Data Received: " + strBuilder.toString());
			
			if(strBuilder!=null && strBuilder.toString().length()>0){
				rxMessage=strBuilder.toString();
				////System.out.println(rxMessage.indexOf("MessageHeader"));
				if(rxMessage.indexOf("MessageHeader")!=-1)
				{
					
					rxMessage=rxMessage.substring(rxMessage.indexOf("MessageHeader")-2, rxMessage.length());
					////System.out.println("rxMessage =========="+rxMessage);
					
					
					JSONParser parser = new JSONParser(); 
					JSONObject json = (JSONObject) parser.parse(rxMessage);
					messageHeaderJson=(JSONObject)json.get("MessageHeader");
					bodyContentJson=(JSONObject)json.get("Body");
					pharmacyJson=(JSONObject)bodyContentJson.get("Pharmacy");
					
					facilityJson=(JSONObject)bodyContentJson.get("Facility");
					patientJson=(JSONObject)bodyContentJson.get("Patient");
					rxJson=(JSONObject)bodyContentJson.get("Rx");
					
					employeesJson=(JSONObject)bodyContentJson.get("Employees");
					prescribersJson=(JSONObject)bodyContentJson.get("Prescribers");
					claimsJson=(JSONObject)bodyContentJson.get("Claims");
					
					
		            try {
		            	
		            	invokePushPioneerEventData(strBuilder.toString(),messageHeaderJson,bodyContentJson,pharmacyJson,employeesJson,facilityJson,prescribersJson,patientJson,rxJson,claimsJson);
		            	
		            	
		            } catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					    
					    
		            try {
		            	
		            	
		            	invokePushPioneerRXDatatoCRE8Portal(strBuilder.toString(),messageHeaderJson,bodyContentJson,pharmacyJson,employeesJson,facilityJson,prescribersJson,patientJson,rxJson,claimsJson);
		            	
		            	
		            } catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
  
					
					JSONObject retMsgJson =new JSONObject();
					retMsgJson.put("Message_ID",messageHeaderJson.get("MessageID"));
					retMsgJson.put("Message_Type","ACK");
					returnJson.put("Message_Header", retMsgJson);
					
				}else
				{
					
					JSONObject retMsgJson =new JSONObject();
					retMsgJson.put("Message_ID",messageHeaderJson.get("MessageID"));
					retMsgJson.put("Message_Type","NAK");
					retMsgJson.put("Error","Error Occurred and message Not received");
					returnJson.put("Message_Header", retMsgJson);
				}
				
				
				
			}else
			{
				
				JSONObject retMsgJson =new JSONObject();
				retMsgJson.put("Message_ID",messageHeaderJson.get("MessageID"));
				retMsgJson.put("Message_Type","NAK");
				retMsgJson.put("Error","Error Occurred and message Not received");
				returnJson.put("Message_Header", retMsgJson);
			}
			
			/**
			 * Response sent back to RX Server with the following format
			 * {
			  "Message_Header"
			    "Message_ID":"1cc13e63-f271-4ca5-b839-75e645cc45d8",
			    "Message_Type":"ACK",
			   
			  }
			}
			 
			{
			  "Message_Header"
			    "Message_ID":"1cc13e63-f271-4ca5-b839-75e645cc45d8",
			    "Message_Type":"NAK",
			    "Error":"Optional Error Message, will only apply when sending NAK for Message_Type"
			  }
			}
			 */
			System.out.println("Response sent back to Pioneer RX :");
			System.out.println(returnJson);
			
		
		} catch (Exception e) {
			//System.out.println("Error Parsing: - "+e);
			e.printStackTrace();
		}
		// return HTTP response 200 in case of success
		return Response.status(200).entity(returnJson.toString()).build();
	}
	/**
	 * invokePushPioneerEventData
	 * @param messageContent
	 * @param messageHeaderJson
	 * @param bodyContentJson
	 * @param pharmacyJson
	 * @param employeesJson
	 * @param facilityJson
	 * @param prescribersJson
	 * @param patientJson
	 * @param rxJson
	 * @param claimsJson
	 */
	public void invokePushPioneerEventData(String messageContent,JSONObject messageHeaderJson,JSONObject bodyContentJson,JSONObject pharmacyJson,JSONObject employeesJson,JSONObject facilityJson,JSONObject prescribersJson,JSONObject patientJson,JSONObject rxJson,JSONObject claimsJson)
	{
		
		try{
			
			

			////System.out.println("json =========="+json);
			////System.out.println("messageHeaderJson =========="+messageHeaderJson);
			////System.out.println("MessageID =========="+messageHeaderJson.get("MessageID"));
			////System.out.println("bodyContentJson =========="+bodyContentJson);
			/*//System.out.println("pharmacyJson =========="+pharmacyJson);
			
			//System.out.println("facilityJson =========="+facilityJson);
			//System.out.println("rxJson =========="+rxJson);
			//System.out.println("patientJson =========="+patientJson);
			//System.out.println("prescribersJson =========="+prescribersJson);
			
			//System.out.println("claimJson =========="+claimJson);*/
			////System.out.println("messageHeaderJson =========="+messageHeaderJson);
			////System.out.println("employeesJson =========="+employeesJson);
			if(messageHeaderJson!=null){
			    JSONArray nameArr = new JSONArray();
			    JSONArray valArr =  new JSONArray();
			    
			 	Set keys = messageHeaderJson.keySet();
			    Iterator a = keys.iterator();
			    String MessageID="";
			    nameArr.add("MessageContent");
			    valArr.add(messageContent);
			    while(a.hasNext()) {
			    	String key = (String)a.next();
			        // loop to get the dynamic key
			    	String value =(String) messageHeaderJson.get(key);
			    	
			    	if(key.equalsIgnoreCase("MessageID"))
			    		MessageID=value;
			    	
			        //System.out.print("key : "+key+" value :"+value+" =||=");
			        nameArr.add(key);
				    valArr.add(value);
			    }
			
			   
			    pushPioneerEventData(nameArr, valArr,"pioneerevent.messageheader");
			    
			    pushPioneerEventRXJSONData(rxJson,MessageID);
			    
			    pushPioneerEventClaimJSONData(claimsJson,MessageID);
			    
			    pushPioneerEventPrescriberJSONData(prescribersJson,MessageID);
			    
			    pushPioneerEventPatientJSONData(patientJson,MessageID);
			 
			   //###################################################################################################################################//
			
		}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	  /**
	   * pushPioneerEventRXJSONData
	   * @param rxJson
	   * @param MessageID
	   */
	  public static void pushPioneerEventRXJSONData(JSONObject rxJson,String MessageID)
	  {
		  try{
			  
			    
			    
			    
			    JSONArray nameArr4 = new JSONArray();
			    JSONArray valArr4 =  new JSONArray();
			    
			    if(rxJson!=null){
			    //System.out.println("rxJson =========="+rxJson);
			
			    JSONArray nameArr1 = new JSONArray();
			    JSONArray valArr1 =  new JSONArray();
			    
			    JSONArray nameArr2 = new JSONArray();
			    JSONArray valArr2 =  new JSONArray();
			    
			    JSONArray nameArr3 = new JSONArray();
			    JSONArray valArr3 =  new JSONArray();
			    
			  
			    
			    nameArr1.add("MessageID");
				valArr1.add(MessageID);
				nameArr2.add("MessageID");
				valArr2.add(MessageID);
				nameArr3.add("MessageID");
				valArr3.add(MessageID);

				
			 	Set keys1 = rxJson.keySet();
			    Iterator a1 = keys1.iterator();
			    String rxNumber="";
			    while(a1.hasNext()) {
			    	
			    	String key = (String)a1.next();
			    	
			    	if(key.equalsIgnoreCase("RxNumber"))
			    	{
			    		rxNumber=(String) rxJson.get(key);
			    		
			    		nameArr2.add(key);
			    		valArr2.add(rxNumber);
			    		nameArr3.add(key);
			    		valArr3.add(rxNumber);
			    		nameArr4.add(key);
						valArr4.add(rxNumber);
			    	}
			    	
			    	if(key.equalsIgnoreCase("MedicationPrescribed") && rxJson.get(key) instanceof JSONObject){
			    		// loop to get the dynamic key
				    	JSONObject json1=(JSONObject)rxJson.get(key);
				    	
				    	Set mkeys = json1.keySet();
					    Iterator ma = mkeys.iterator();
					 
					    while(ma.hasNext()) {
					    	String mkey = (String)ma.next();
					    	String mvalue =(String)json1.get(mkey);
					    	if(mvalue==null)
					    		mvalue="";
					    	////System.out.println("key : "+mkey+" === value :"+mvalue);
					        nameArr2.add(mkey);
						    valArr2.add(mvalue);
					    }
				        
			    		
			    	}else if(key.equalsIgnoreCase("MedicationDispensed") && rxJson.get(key) instanceof JSONObject){
			    		// loop to get the dynamic key
				    	JSONObject json1=(JSONObject)rxJson.get(key);
				    	
				    	Set mkeys = json1.keySet();
					    Iterator ma = mkeys.iterator();
					 
					    while(ma.hasNext()) {
					    	String mkey = (String)ma.next();
					    	String mvalue =(String)json1.get(mkey);
					    	if(mvalue==null)
					    		mvalue="";
					    	////System.out.println("key : "+mkey+" === value :"+mvalue);
					        nameArr3.add(mkey);
						    valArr3.add(mvalue);
					    }
				        
			    		
			    	}else{
			    		
			    		if(key.equalsIgnoreCase("Comments") && rxJson.get(key) instanceof JSONObject)
			    		{
			    			JSONObject xx =(JSONObject) rxJson.get(key);
			    			
			    			JSONObject yy =(JSONObject) xx.get("RxComments");
			    			
			    			
					    	Set mkeys1 = yy.keySet();
						    Iterator ma1 = mkeys1.iterator();
						 
						    while(ma1.hasNext()) {
						    	String mkey = (String)ma1.next();
						    	String mvalue =(String)yy.get(mkey);
						    	if(mvalue==null)
						    		mvalue="";
						    	////System.out.println("key : RxComments"+mkey+" === value :"+mvalue);
						        nameArr1.add("RxComments"+mkey);
							    valArr1.add(mvalue);
						    }
						    
			    		
			    			JSONObject zz =(JSONObject) xx.get("FillComments");
			    		

					    	Set mkeys2 = yy.keySet();
						    Iterator ma2 = mkeys2.iterator();
						 
						    while(ma2.hasNext()) {
						    	String mkey = (String)ma2.next();
						    	String mvalue =(String)zz.get(mkey);
						    	if(mvalue==null)
						    		mvalue="";
						    	////System.out.println("key : FillComments"+mkey+" === value :"+mvalue);
						        nameArr1.add("FillComments"+mkey);
							    valArr1.add(mvalue);
						    }
						    
			    			
			    		}else
			    		{
			    			if(!(rxJson.get(key) instanceof JSONObject)){
				    			System.out.println("11111 key : "+key+" === value :"+rxJson.get(key));
				    			// loop to get the dynamic key
						    	String value =(String) rxJson.get(key);
						    	if(value==null)
						    		value="";
						        
						        nameArr1.add(key);
							    valArr1.add(value);
			    			}else
			    			{
			    				System.out.println("22222 key : "+key+" === value :"+rxJson.get(key));
			    			}
			    			
			    		}
				       
			    	}
				    
			    }
			  
			   
			    pushPioneerEventData(nameArr1, valArr1,"pioneerevent.rx");
			    pushPioneerEventData(nameArr2, valArr2,"pioneerevent.medicationprescribed");
			    pushPioneerEventData(nameArr3, valArr3,"pioneerevent.medicationdispensed");
			}
			  
		  }catch(Exception e)
		  {
			  
		  }
		  
	  }
	  /**
	   * pushPioneerEventClaimJSONData
	   * @param claimsJson
	   * @param MessageID
	   */
	  public static void pushPioneerEventClaimJSONData(JSONObject claimsJson,String MessageID)
	   {
		  try{
			  
			   JSONArray nameArr4 = new JSONArray();
			   JSONArray valArr4 =  new JSONArray();
			    
			    if(claimsJson!=null){
			    
				   
					nameArr4.add("MessageID");
					valArr4.add(MessageID);
				    
				 	Set keys4 = claimsJson.keySet();
				    Iterator a4 = keys4.iterator();
				    while(a4.hasNext()) {
				    	String key = (String)a4.next();
				    	
				    	if(key.equalsIgnoreCase("Claim") && claimsJson.get(key) instanceof JSONObject){
				    		// loop to get the dynamic key
					    	JSONObject json1=(JSONObject)claimsJson.get(key);
					    	
					    	Set mkeys = json1.keySet();
						    Iterator ma = mkeys.iterator();
						 
						    while(ma.hasNext()) {
						    	String mkey = (String)ma.next();
						    	String mvalue =(String)json1.get(mkey);
						    	if(mvalue==null)
						    		mvalue="";
						    	////System.out.println("key : "+mkey+" === value :"+mvalue);
						        nameArr4.add(mkey);
							    valArr4.add(mvalue);
						    }
					        
				    		
				    	}
				    
				    }
				    
				    pushPioneerEventData(nameArr4, valArr4,"pioneerevent.claim");
			    }
			  
		  }catch(Exception e)
		  {
			  
		  }
		  
	  }
	  /**
	   * pushPioneerEventPrescriberJSONData 
	   * @param prescribersJson
	   * @param MessageID
	   */
	  public static void pushPioneerEventPrescriberJSONData(JSONObject prescribersJson,String MessageID)
	   {
		  try{
			  
			  if(prescribersJson!=null){ 
				    String PrescriberPioneerRxID="";
				    JSONArray nameArr5 = new JSONArray();
				    JSONArray valArr5 =  new JSONArray();
					nameArr5.add("MessageID");
					valArr5.add(MessageID);
					
					JSONArray nameArr6 = new JSONArray();
				    JSONArray valArr6 =  new JSONArray();
					nameArr6.add("MessageID");
					valArr6.add(MessageID);
					
					JSONArray nameArr7 = new JSONArray();
				    JSONArray valArr7 =  new JSONArray();
					nameArr7.add("MessageID");
					valArr7.add(MessageID);
				    
				 	Set keys5 = prescribersJson.keySet();
				    Iterator a5 = keys5.iterator();
				    while(a5.hasNext()) {
				    	String key = (String)a5.next();
				    	
				    	if(key.equalsIgnoreCase("Prescriber") && prescribersJson.get(key) instanceof JSONObject){
				    		// loop to get the dynamic key
					    	JSONObject json1=(JSONObject)prescribersJson.get(key);
					    	
					    	Set mkeys1 = json1.keySet();
						    Iterator ma1 = mkeys1.iterator();
						 
						    while(ma1.hasNext()) {
						    	String mkey = (String)ma1.next();
						    	
						    	if(mkey.equalsIgnoreCase("Identification") && json1.get(mkey) instanceof JSONObject){
						    		
						    		

						    		// loop to get the dynamic key
							    	JSONObject json2=(JSONObject)json1.get(mkey);
							    	
							    	if(json2.get("PrescriberPioneerRxID")!=null)
							    	{
							    		PrescriberPioneerRxID=json2.get("PrescriberPioneerRxID").toString();
							    		nameArr6.add("PrescriberPioneerRxID");
							    		valArr6.add(PrescriberPioneerRxID);
							    		nameArr7.add("PrescriberPioneerRxID");
							    		valArr7.add(PrescriberPioneerRxID);
							    		
							    	}
							    	
							    	Set mkeys = json2.keySet();
								    Iterator ma = mkeys.iterator();
								 
								    while(ma.hasNext()) {
								    	String mkey1 = (String)ma.next();
								    	String mvalue =(String)json2.get(mkey1);
								    	if(mvalue==null)
								    		mvalue="";
								    	////System.out.println("key : "+mkey+" === value :"+mvalue);
								        nameArr5.add(mkey1);
									    valArr5.add(mvalue);
								    }
							        
						    		
						    	}else if(mkey.equalsIgnoreCase("Name") && json1.get(mkey) instanceof JSONObject){
						    		
						    		

						    		// loop to get the dynamic key
							    	JSONObject json2=(JSONObject)json1.get(mkey);
							    	
							    	Set mkeys = json2.keySet();
								    Iterator ma = mkeys.iterator();
								 
								    while(ma.hasNext()) {
								    	String mkey1 = (String)ma.next();
								    	String mvalue =(String)json2.get(mkey1);
								    	if(mvalue==null)
								    		mvalue="";
								    	////System.out.println("key : "+mkey+" === value :"+mvalue);
								        nameArr5.add(mkey1);
									    valArr5.add(mvalue);
								    }
							        
						    		
						    	}else if(mkey.equalsIgnoreCase("Addresses") && json1.get(mkey) instanceof JSONObject){
						    		
						    		/*//System.out.println(json1.get(mkey));
						    		//System.out.println(json1.get("PhoneNumber"));
						    		//System.out.println(json1.get(mkey)  instanceof JSONObject);
					    			//System.out.println(json1.get(mkey) instanceof JSONArray);*/
					    		
					    			JSONObject eJson=(JSONObject)json1.get(mkey);
					    			/*//System.out.println(eJson.get("PhoneNumber"));
					    			//System.out.println(eJson.get("PhoneNumber")  instanceof JSONObject);
					    			//System.out.println(eJson.get("PhoneNumber") instanceof JSONArray);*/
					    			
					    			if(eJson.get("Address") instanceof JSONObject)
					    			{
					    				JSONObject	jsonObj = (JSONObject) eJson.get("Address");
						    			////System.out.println("JSONObject ======"+jsonObj);
							            
						    			Set mkeys = jsonObj.keySet();
									    Iterator ma = mkeys.iterator();
									 
									    while(ma.hasNext()) {
									    	String mkey1 = (String)ma.next();
									    	String mvalue =(String)jsonObj.get(mkey1);
									    	if(mvalue==null)
									    		mvalue="";
									    	////System.out.println("key : "+mkey+" === value :"+mvalue);
									        nameArr6.add(mkey1);
										    valArr6.add(mvalue);
									    }
									    pushPioneerEventData(nameArr6, valArr6,"pioneerevent.prescriberaddress");
						    			
					    				
					    			}else if(eJson.get("Address") instanceof JSONArray)
					    			{
					    				JSONArray	jsonArray = (JSONArray) eJson.get("Address");
						    			////System.out.println("jsonArray ======"+jsonArray);
							            
							            for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
							            	JSONObject newJson=new JSONObject();
							            	newJson = (JSONObject) iterator.next();
							            	
							            	 nameArr6 = new JSONArray();
										     valArr6 =  new JSONArray();
											nameArr6.add("MessageID");
											valArr6.add(MessageID);
							        	    
							        	    ////System.out.println("newJson =========="+newJson);
										 	Set nkeys = newJson.keySet();
										    Iterator na = nkeys.iterator();
										    while(na.hasNext()) {
										    	String nkey = (String)na.next();
										        // loop to get the dynamic key
										        Object value = newJson.get(nkey);
										       
										        ////System.out.print("key : "+nkey+" value :"+value);
										        
										        nameArr6.add(nkey);
											    valArr6.add(value);
										    }
										    pushPioneerEventData(nameArr6, valArr6,"pioneerevent.prescriberaddress");

							            }
						    		
					    				
					    			}
					    			
						    	}else if(mkey.equalsIgnoreCase("PhoneNumbers") && json1.get(mkey) instanceof JSONObject){
						    		
						    		/*//System.out.println(json1.get(mkey));
						    		//System.out.println(json1.get("PhoneNumber"));
						    		//System.out.println(json1.get(mkey)  instanceof JSONObject);
					    			//System.out.println(json1.get(mkey) instanceof JSONArray);*/
					    		
					    			JSONObject eJson=(JSONObject)json1.get(mkey);
					    			/*//System.out.println(eJson.get("PhoneNumber"));
					    			//System.out.println(eJson.get("PhoneNumber")  instanceof JSONObject);
					    			//System.out.println(eJson.get("PhoneNumber") instanceof JSONArray);*/
					    			
					    			if(eJson.get("PhoneNumber") instanceof JSONObject)
					    			{
					    				JSONObject	jsonObj = (JSONObject) eJson.get("PhoneNumber");
						    			////System.out.println("JSONObject ======"+jsonObj);
							            
						    			Set mkeys = jsonObj.keySet();
									    Iterator ma = mkeys.iterator();
									 
									    while(ma.hasNext()) {
									    	String mkey1 = (String)ma.next();
									    	String mvalue =(String)jsonObj.get(mkey1);
									    	if(mvalue==null)
									    		mvalue="";
									    	////System.out.println("key : "+mkey+" === value :"+mvalue);
									        nameArr7.add(mkey1);
										    valArr7.add(mvalue);
									    }
								        
									    pushPioneerEventData(nameArr7, valArr7,"pioneerevent.prescriberphonenumber");
					    				
					    			}else if(eJson.get("PhoneNumber") instanceof JSONArray)
					    			{
					    				
					    				
					    				
					    				JSONArray	jsonArray = (JSONArray) eJson.get("PhoneNumber");
						    			//System.out.println("jsonArray ======"+jsonArray);
							            
							            for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
							            	
							            	 nameArr7 = new JSONArray();
						    			     valArr7 =  new JSONArray();
						    				nameArr7.add("MessageID");
						    				valArr7.add(MessageID);
						    				
							            	JSONObject newJson=new JSONObject();
							            	newJson = (JSONObject) iterator.next();
							        	    
							        	    ////System.out.println("newJson =========="+newJson);
										 	Set nkeys = newJson.keySet();
										    Iterator na = nkeys.iterator();
										    while(na.hasNext()) {
										    	String nkey = (String)na.next();
										        // loop to get the dynamic key
										        Object value = newJson.get(nkey);
										        nameArr7.add(nkey);
											    valArr7.add(value);
										        ////System.out.print("key : "+nkey+" value :"+value);
										    }
										    
										    pushPioneerEventData(nameArr7, valArr7,"pioneerevent.prescriberphonenumber");
							            }
							           
					    				
					    			}
					    			
						    	}else if(mkey.equalsIgnoreCase("Comments") && json1.get(mkey) instanceof JSONObject)
					    		{
					    			JSONObject xx =(JSONObject) json1.get(mkey);
					    			
					    			
							    	Set mkeys2 = xx.keySet();
								    Iterator ma2 = mkeys2.iterator();
								 
								    while(ma2.hasNext()) {
								    	String mkey2 = (String)ma2.next();
								    	String mvalue2 =(String)xx.get(mkey2);
								    	if(mvalue2==null)
								    		mvalue2="";
								    	////System.out.println("key : RxComments"+mkey2+" === value :"+mvalue2);
								    	nameArr5.add("Comments"+mkey2);
								    	valArr5.add(mvalue2);
								    }
								
					    			
					    		}else
					    		{
						    		////System.out.println("mkey ========"+mkey);
						    		// loop to get the dynamic key
							    	String value =(String) json1.get(mkey);
							    	if(value==null)
							    		value="";
							        ////System.out.println("key : "+key+" === value :"+value);
							        nameArr5.add(mkey);
								    valArr5.add(value);
								
					    			
					    		}   
								    
						    }
					        
				    		
				    	}
				    
				    }
				    
				    pushPioneerEventData(nameArr5, valArr5,"pioneerevent.prescriber");
				 
				    
				    //System.out.println(nameArr5);
				    //System.out.println(valArr5);
				    
				    //System.out.println(nameArr6);
				    //System.out.println(valArr6);
				    
				    
				    //System.out.println(nameArr7);
				    //System.out.println(valArr7);
				 }
			  
		  }catch(Exception e)
		  {
			  
		  }
		  
	  }
	  /**
	   * pushPioneerEventPatientJSONData 
	   * @param patientJson
	   * @param MessageID
	   */
	  public static void pushPioneerEventPatientJSONData(JSONObject patientJson,String MessageID)
	   {
		  try{
			  
			  if(patientJson!=null){  
				    String PatientPioneerRxID="";
				    JSONArray namePatArr5 = new JSONArray();
				    JSONArray valPatArr5 =  new JSONArray();
					namePatArr5.add("MessageID");
					valPatArr5.add(MessageID);
					
					JSONArray namePatArr6 = new JSONArray();
				    JSONArray valPatArr6 =  new JSONArray();
					namePatArr6.add("MessageID");
					valPatArr6.add(MessageID);
					
					JSONArray namePatArr7 = new JSONArray();
				    JSONArray valPatArr7 =  new JSONArray();
					namePatArr7.add("MessageID");
					valPatArr7.add(MessageID);
				    
				 	Set keysPat5 = patientJson.keySet();
				    Iterator patA5 = keysPat5.iterator();
				    
				    while(patA5.hasNext()) {
				    	
				    	String mkey = (String)patA5.next();
				    	
				    	if(mkey.equalsIgnoreCase("Identification") && patientJson.get(mkey) instanceof JSONObject){
						    		
						    		

						    		// loop to get the dynamic key
							    	JSONObject json2=(JSONObject)patientJson.get(mkey);
							    	
							    	if(json2.get("PatientPioneerRxID")!=null)
							    	{
							    		PatientPioneerRxID=json2.get("PatientPioneerRxID").toString();
							    		namePatArr6.add("PatientPioneerRxID");
							    		valPatArr6.add(PatientPioneerRxID);
							    		namePatArr7.add("PatientPioneerRxID");
							    		valPatArr7.add(PatientPioneerRxID);
							    		
							    	}
							    	
							    	Set mkeys = json2.keySet();
								    Iterator ma = mkeys.iterator();
								 
								    while(ma.hasNext()) {
								    	String mkey1 = (String)ma.next();
								    	String mvalue =(String)json2.get(mkey1);
								    	if(mvalue==null)
								    		mvalue="";
								    	////System.out.println("key : "+mkey+" === value :"+mvalue);
								        namePatArr5.add(mkey1);
									    valPatArr5.add(mvalue);
								    }
							        
						    		
						    	}else if(mkey.equalsIgnoreCase("Name") && patientJson.get(mkey) instanceof JSONObject){
						    		
						    		

						    		// loop to get the dynamic key
							    	JSONObject json2=(JSONObject)patientJson.get(mkey);
							    	
							    	Set mkeys = json2.keySet();
								    Iterator ma = mkeys.iterator();
								 
								    while(ma.hasNext()) {
								    	String mkey1 = (String)ma.next();
								    	String mvalue =(String)json2.get(mkey1);
								    	if(mvalue==null)
								    		mvalue="";
								    	////System.out.println("key : "+mkey+" === value :"+mvalue);
								        namePatArr5.add(mkey1);
									    valPatArr5.add(mvalue);
								    }
							        
						    		
						    	}else if(mkey.equalsIgnoreCase("Addresses") && patientJson.get(mkey) instanceof JSONObject){
						    		
						    		/*//System.out.println(patientJson.get(mkey));
						    		//System.out.println(patientJson.get("PhoneNumber"));
						    		//System.out.println(patientJson.get(mkey)  instanceof JSONObject);
					    			//System.out.println(patientJson.get(mkey) instanceof JSONArray);*/
					    		
					    			JSONObject eJson=(JSONObject)patientJson.get(mkey);
					    			/*//System.out.println(eJson.get("PhoneNumber"));
					    			//System.out.println(eJson.get("PhoneNumber")  instanceof JSONObject);
					    			//System.out.println(eJson.get("PhoneNumber") instanceof JSONArray);*/
					    			
					    			if(eJson.get("Address") instanceof JSONObject)
					    			{
					    				JSONObject	jsonObj = (JSONObject) eJson.get("Address");
						    			////System.out.println("JSONObject ======"+jsonObj);
							            
						    			Set mkeys = jsonObj.keySet();
									    Iterator ma = mkeys.iterator();
									 
									    while(ma.hasNext()) {
									    	String mkey1 = (String)ma.next();
									    	String mvalue =(String)jsonObj.get(mkey1);
									    	if(mvalue==null)
									    		mvalue="";
									    	////System.out.println("key : "+mkey+" === value :"+mvalue);
									        namePatArr6.add(mkey1);
										    valPatArr6.add(mvalue);
									    }
									    pushPioneerEventData(namePatArr6, valPatArr6,"pioneerevent.patientaddress");
						    			
					    				
					    			}else if(eJson.get("Address") instanceof JSONArray)
					    			{
					    				JSONArray	jsonArray = (JSONArray) eJson.get("Address");
						    			////System.out.println("jsonArray ======"+jsonArray);
							            
							            for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
							            	JSONObject newJson=new JSONObject();
							            	newJson = (JSONObject) iterator.next();
							        	    
							            	namePatArr6 = new JSONArray();
							            	valPatArr6 =  new JSONArray();
							            	namePatArr6.add("MessageID");
							            	valPatArr6.add(MessageID);
										
							        	    ////System.out.println("newJson =========="+newJson);
										 	Set nkeys = newJson.keySet();
										    Iterator na = nkeys.iterator();
										    while(na.hasNext()) {
										    	String nkey = (String)na.next();
										        // loop to get the dynamic key
										        Object value = newJson.get(nkey);
										       
										        ////System.out.print("key : "+nkey+" value :"+value);
										        
										        namePatArr6.add(nkey);
											    valPatArr6.add(value);
										    }
										    pushPioneerEventData(namePatArr6, valPatArr6,"pioneerevent.patientaddress");

							            }
						    		
					    				
					    			}
					    			
						    	}else if(mkey.equalsIgnoreCase("PhoneNumbers") && patientJson.get(mkey) instanceof JSONObject){
						    		
						    		/*//System.out.println(patientJson.get(mkey));
						    		//System.out.println(patientJson.get("PhoneNumber"));
						    		//System.out.println(patientJson.get(mkey)  instanceof JSONObject);
					    			//System.out.println(patientJson.get(mkey) instanceof JSONArray);*/
					    		
					    			JSONObject eJson=(JSONObject)patientJson.get(mkey);
					    			/*//System.out.println(eJson.get("PhoneNumber"));
					    			//System.out.println(eJson.get("PhoneNumber")  instanceof JSONObject);
					    			//System.out.println(eJson.get("PhoneNumber") instanceof JSONArray);*/
					    			
					    			if(eJson.get("PhoneNumber") instanceof JSONObject)
					    			{
					    				JSONObject	jsonObj = (JSONObject) eJson.get("PhoneNumber");
						    			////System.out.println("JSONObject ======"+jsonObj);
							            
						    			Set mkeys = jsonObj.keySet();
									    Iterator ma = mkeys.iterator();
									 
									    while(ma.hasNext()) {
									    	String mkey1 = (String)ma.next();
									    	String mvalue =(String)jsonObj.get(mkey1);
									    	if(mvalue==null)
									    		mvalue="";
									    	////System.out.println("key : "+mkey+" === value :"+mvalue);
									        namePatArr7.add(mkey1);
										    valPatArr7.add(mvalue);
									    }
								        
									    pushPioneerEventData(namePatArr7, valPatArr7,"pioneerevent.patientphonenumber");
					    				
					    			}else if(eJson.get("PhoneNumber") instanceof JSONArray)
					    			{
					    				JSONArray	jsonArray = (JSONArray) eJson.get("PhoneNumber");
						    			////System.out.println("jsonArray ======"+jsonArray);
							            
							            for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
							            	JSONObject newJson=new JSONObject();
							            	newJson = (JSONObject) iterator.next();
							        	    
							            	namePatArr7 = new JSONArray();
							            	valPatArr7 =  new JSONArray();
							            	namePatArr7.add("MessageID");
							            	valPatArr7.add(MessageID);
							            	
							        	    ////System.out.println("newJson =========="+newJson);
										 	Set nkeys = newJson.keySet();
										    Iterator na = nkeys.iterator();
										    while(na.hasNext()) {
										    	
										    	
										    	String nkey = (String)na.next();
										        // loop to get the dynamic key
										        Object value = newJson.get(nkey);
										        namePatArr7.add(nkey);
											    valPatArr7.add(value);
										        ////System.out.print("key : "+nkey+" value :"+value);
										    }
										    
										    pushPioneerEventData(namePatArr7, valPatArr7,"pioneerevent.patientphonenumber");
							            }
							           
					    				
					    			}
					    			
						    	}else if(mkey.equalsIgnoreCase("Comments") && patientJson.get(mkey) instanceof JSONObject)
					    		{
					    			JSONObject xx =(JSONObject) patientJson.get(mkey);
					    			
					    			
							    	Set mkeys2 = xx.keySet();
								    Iterator ma2 = mkeys2.iterator();
								 
								    while(ma2.hasNext()) {
								    	String mkey2 = (String)ma2.next();
								    	String mvalue2 =(String)xx.get(mkey2);
								    	if(mvalue2==null)
								    		mvalue2="";
								    	////System.out.println("key : RxComments"+mkey2+" === value :"+mvalue2);
								    	namePatArr5.add("Comments"+mkey2);
								    	valPatArr5.add(mvalue2);
								    }
								
					    			
					    		}else
					    		{
						    		////System.out.println("mkey ========"+mkey);
						    		// loop to get the dynamic key
							    	String value =(String) patientJson.get(mkey);
							    	if(value==null)
							    		value="";
							        ////System.out.println("key : "+key+" === value :"+value);
							        namePatArr5.add(mkey);
								    valPatArr5.add(value);
								
					    			
					    		}   
								    
						
				    
				    }
				    
				    pushPioneerEventData(namePatArr5, valPatArr5,"pioneerevent.patient");
		
				    //System.out.println(namePatArr5);
				    //System.out.println(valPatArr5);
				    
				    //System.out.println(namePatArr6);
				    //System.out.println(valPatArr6);
				    
				    
				    //System.out.println(namePatArr7);
				    //System.out.println(valPatArr7);
			
				}
			  
		  }catch(Exception e)
		  {
			  
		  }
		  
	  }
	    
	/**
	 * invokePushPioneerRXDatatoCRE8Portal
	 * @param messageContent
	 * @param messageHeaderJson
	 * @param bodyContentJson
	 * @param pharmacyJson
	 * @param employeesJson
	 * @param facilityJson
	 * @param prescribersJson
	 * @param patientJson
	 * @param rxJson
	 * @param claimsJson
	 */
	public void invokePushPioneerRXDatatoCRE8Portal(String messageContent,JSONObject messageHeaderJson,JSONObject bodyContentJson,JSONObject pharmacyJson,JSONObject employeesJson,JSONObject facilityJson,JSONObject prescribersJson,JSONObject patientJson,JSONObject rxJson,JSONObject claimsJson)
	{
		
		Connection conn = getDestinationDBConnection();
		try{
		
				//System.out.println("rxJson =========="+rxJson);
				//System.out.println("patientJson =========="+patientJson);
				//System.out.println("prescribersJson =========="+prescribersJson);
				
				String PatientPioneerRxID="", PrescriberPioneerRxID="";
				int patientid=0,physicianid=0,group_id=0,physicianClinicId=0,physicianGroupId=0;
				String physicianName="",patientName="";
				String prescribedItemName="";
				Date dateWritten=null;
				String shipping_address="",shipping_city="",shipping_state="",shipping_zip_code="";
				String phy_address="",phy_city="",phy_state="",phy_zip_code="",phy_country="",phy_phone="",phy_dea=""  ,phy_npi ="" ,phy_upin ="" ,
						phy_state_license ="" ,phy_medicaid ="" , patient_mobile  =""  ,patient_ssn  =""  ,allergies =""   ,diagnosis ="" ;
				Object patient_date_of_birth ="";
				String phy_firstname="", phy_lastname="",phy_middlename="",phy_fullname="";
		     	
		    	if(patientJson.get("Identification") instanceof JSONObject){
		
			    		// loop to get the dynamic key
				    	JSONObject json2=(JSONObject)patientJson.get("Identification");
				    	
				    	if(json2.get("PatientPioneerRxID")!=null)
				    	{
				    		PatientPioneerRxID=json2.get("PatientPioneerRxID").toString();
				    		
				    	}
			    	}
		    	
		    	if(patientJson.get("Addresses") instanceof JSONObject){
		    		
		    	
	    			JSONObject eJson=(JSONObject)patientJson.get("Addresses");
	    			
	    			if(eJson.get("Address") instanceof JSONObject)
	    			{
	    				JSONObject	jsonObj = (JSONObject) eJson.get("Address");
		    			////System.out.println("JSONObject ======"+jsonObj);
			            
	    				
	    				shipping_address=jsonObj.get("AddressLine").toString();;
	    				shipping_city=jsonObj.get("City").toString();;
	    				shipping_state=jsonObj.get("StateCode").toString();;
	    				shipping_zip_code=jsonObj.get("ZipCode").toString();;
	    				
	    			}else if(eJson.get("Address") instanceof JSONArray)
	    			{
	    				JSONArray	jsonArray = (JSONArray) eJson.get("Address");
		    			////System.out.println("jsonArray ======"+jsonArray);
			            
			            for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
			            	JSONObject newJson=new JSONObject();
			            	newJson = (JSONObject) iterator.next();
			            	
			            	if(newJson.get("Type").toString().equalsIgnoreCase("Primary"))
			            	{
				        		shipping_address=newJson.get("AddressLine").toString();;
			    				shipping_city=newJson.get("City").toString();;
			    				shipping_state=newJson.get("StateCode").toString();;
			    				shipping_zip_code=newJson.get("ZipCode").toString();;
			            	}
		    				
			    	    }
		    		
	    			}
	    			
		    	}
			
		    	
		    	
		    	System.out.println("prescribersJson 111==================="+(prescribersJson.get("Prescriber") instanceof JSONObject));
	    		System.out.println("prescribersJson 222==================="+(prescribersJson.get("Prescriber") instanceof JSONArray));
		    
		
		    	JSONObject phyjson1=new JSONObject();
		    	if(prescribersJson.get("Prescriber") instanceof JSONObject){
		    		// loop to get the dynamic key
		    		phyjson1=(JSONObject)prescribersJson.get("Prescriber");
		    	
				    	if(phyjson1.get("Identification") instanceof JSONObject){
				    	
				    		// loop to get the dynamic key
					    	JSONObject json2=(JSONObject)phyjson1.get("Identification");
					    	
					    	if(json2.get("PrescriberPioneerRxID")!=null)
					    	{
					    		PrescriberPioneerRxID=json2.get("PrescriberPioneerRxID").toString();
					    		
					    		if(json2.get("DEA")!=null)
					    			phy_dea=json2.get("DEA").toString();
					    		if(json2.get("NPI")!=null)
					    			phy_npi=json2.get("NPI").toString();
					    		if(json2.get("StateLicense")!=null)
					    			phy_state_license=json2.get("StateLicense").toString();
					    		
					    		
					    				
					    	}
					
				    	}
				    	
				    	
				    	if(phyjson1.get("Name") instanceof JSONObject){
					    	
				    		// loop to get the dynamic key
					    	JSONObject json2=(JSONObject)phyjson1.get("Name");
					    	
					    		
					    		if(json2.get("FirstName")!=null)
					    			phy_firstname=json2.get("FirstName").toString();
					    		if(json2.get("LastName")!=null)
					    			phy_lastname=json2.get("LastName").toString();
					    		if(json2.get("MiddleName")!=null)
					    			phy_middlename=json2.get("MiddleName").toString();
					    		
					    		if(phy_firstname!=null && phy_firstname.length()>0)
					    			phy_fullname=phy_firstname+" ";
					    		if(phy_middlename!=null && phy_middlename.length()>0)
					    			phy_fullname+=phy_middlename+" ";
					    		if(phy_lastname!=null && phy_lastname.length()>0)
					    			phy_fullname+=phy_lastname;
					    		
					    	
					
				    	}
				    	
				    	if(phyjson1.get("Addresses") instanceof JSONObject){
				    		
				    		/*//System.out.println(json1.get(mkey));
				    		//System.out.println(json1.get("PhoneNumber"));
				    		//System.out.println(json1.get(mkey)  instanceof JSONObject);
			    			//System.out.println(json1.get(mkey) instanceof JSONArray);*/
			    		
			    			JSONObject eJson=(JSONObject)phyjson1.get("Addresses");
			    			/*//System.out.println(eJson.get("PhoneNumber"));
			    			//System.out.println(eJson.get("PhoneNumber")  instanceof JSONObject);
			    			//System.out.println(eJson.get("PhoneNumber") instanceof JSONArray);*/
			    			
			    			if(eJson.get("Address") instanceof JSONObject)
			    			{
			    				JSONObject	jsonObj = (JSONObject) eJson.get("Address");
				    			////System.out.println("JSONObject ======"+jsonObj);
					            
			    				
			    				phy_address=jsonObj.get("AddressLine").toString();;
			    				phy_city=jsonObj.get("City").toString();;
			    				phy_state=jsonObj.get("StateCode").toString();;
			    				phy_zip_code=jsonObj.get("ZipCode").toString();;
			    				
				    			
			    				
			    			}else if(eJson.get("Address") instanceof JSONArray)
			    			{
			    				JSONArray	jsonArray = (JSONArray) eJson.get("Address");
				    			////System.out.println("jsonArray ======"+jsonArray);
					            
			    				
			    			    for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
					            	JSONObject newJson=new JSONObject();
					            	newJson = (JSONObject) iterator.next();
					            	
					            	if(newJson.get("Type").toString().equalsIgnoreCase("Primary"))
					            	{
						        		phy_address=newJson.get("AddressLine").toString();;
					    				phy_city=newJson.get("City").toString();;
					    				phy_state=newJson.get("StateCode").toString();;
					    				phy_zip_code=newJson.get("ZipCode").toString();;
					            	}
				    				
					    	    }
			    			}
			    			
				    	}
				    	
				    	if(phyjson1.get("PhoneNumbers") instanceof JSONObject){

				    		/*//System.out.println(json1.get(mkey));
				    		//System.out.println(json1.get("PhoneNumber"));
				    		//System.out.println(json1.get(mkey)  instanceof JSONObject);
				    		//System.out.println(json1.get(mkey) instanceof JSONArray);*/

				    		JSONObject eJson=(JSONObject)phyjson1.get("PhoneNumbers");
				    		/*//System.out.println(eJson.get("PhoneNumber"));
				    		//System.out.println(eJson.get("PhoneNumber")  instanceof JSONObject);
				    		//System.out.println(eJson.get("PhoneNumber") instanceof JSONArray);*/

				    		if(eJson.get("PhoneNumber") instanceof JSONObject)
				    		{
				    			JSONObject	jsonObj = (JSONObject) eJson.get("PhoneNumber");
				    			////System.out.println("JSONObject ======"+jsonObj);

				    			phy_phone=jsonObj.get("Number").toString();;
				    		
				    		}else if(eJson.get("PhoneNumber") instanceof JSONArray)
				    		{


				    			JSONArray	jsonArray = (JSONArray) eJson.get("PhoneNumber");
				    			////System.out.println("jsonArray ======"+jsonArray);
					            
			    				
			    			    for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
					            	JSONObject newJson=new JSONObject();
					            	newJson = (JSONObject) iterator.next();
					            	
					            	if(newJson.get("Type").toString().equalsIgnoreCase("Primary"))
					            	{
					            		phy_phone=newJson.get("Number").toString();;
					    			}
				    				
					    	    }
			    			  

				    		}

				    	}
				    	
		    	}else if(prescribersJson.get("Prescriber") instanceof JSONArray){
		    		
		    		JSONArray	jsonArray2 = (JSONArray) prescribersJson.get("Prescriber");
	    			////System.out.println("jsonArray ======"+jsonArray);
		            
    				
    			    for (Iterator iterator2 = jsonArray2.iterator(); iterator2.hasNext();) {
    			    
    			    	// loop to get the dynamic key
    		    		phyjson1=(JSONObject)iterator2.next();
    		    	
    				    	if(phyjson1.get("Identification") instanceof JSONObject){
    				    	
    				    		// loop to get the dynamic key
    					    	JSONObject json2=(JSONObject)phyjson1.get("Identification");
    					    	
    					    	if(json2.get("PrescriberPioneerRxID")!=null)
    					    	{
    					    		PrescriberPioneerRxID=json2.get("PrescriberPioneerRxID").toString();
    					    		
    					    		if(json2.get("DEA")!=null)
    					    			phy_dea=json2.get("DEA").toString();
    					    		if(json2.get("NPI")!=null)
    					    			phy_npi=json2.get("NPI").toString();
    					    		if(json2.get("StateLicense")!=null)
    					    			phy_state_license=json2.get("StateLicense").toString();
    					    		
    					    		
    					    				
    					    	}
    					
    				    	}
    				    	
    				    	
    				    	if(phyjson1.get("Name") instanceof JSONObject){
    					    	
    				    		// loop to get the dynamic key
    					    	JSONObject json2=(JSONObject)phyjson1.get("Name");
    					    	
    					    		
    					    		if(json2.get("FirstName")!=null)
    					    			phy_firstname=json2.get("FirstName").toString();
    					    		if(json2.get("LastName")!=null)
    					    			phy_lastname=json2.get("LastName").toString();
    					    		if(json2.get("MiddleName")!=null)
    					    			phy_middlename=json2.get("MiddleName").toString();
    					    		
    					    		if(phy_firstname!=null && phy_firstname.length()>0)
    					    			phy_fullname=phy_firstname+" ";
    					    		if(phy_middlename!=null && phy_middlename.length()>0)
    					    			phy_fullname+=phy_middlename+" ";
    					    		if(phy_lastname!=null && phy_lastname.length()>0)
    					    			phy_fullname+=phy_lastname;
    					    		
    					    	
    					
    				    	}
    				    	
    				    	if(phyjson1.get("Addresses") instanceof JSONObject){
    				    		
    				    		/*//System.out.println(json1.get(mkey));
    				    		//System.out.println(json1.get("PhoneNumber"));
    				    		//System.out.println(json1.get(mkey)  instanceof JSONObject);
    			    			//System.out.println(json1.get(mkey) instanceof JSONArray);*/
    			    		
    			    			JSONObject eJson=(JSONObject)phyjson1.get("Addresses");
    			    			/*//System.out.println(eJson.get("PhoneNumber"));
    			    			//System.out.println(eJson.get("PhoneNumber")  instanceof JSONObject);
    			    			//System.out.println(eJson.get("PhoneNumber") instanceof JSONArray);*/
    			    			
    			    			if(eJson.get("Address") instanceof JSONObject)
    			    			{
    			    				JSONObject	jsonObj = (JSONObject) eJson.get("Address");
    				    			////System.out.println("JSONObject ======"+jsonObj);
    					            
    			    				
    			    				phy_address=jsonObj.get("AddressLine").toString();;
    			    				phy_city=jsonObj.get("City").toString();;
    			    				phy_state=jsonObj.get("StateCode").toString();;
    			    				phy_zip_code=jsonObj.get("ZipCode").toString();;
    			    				
    				    			
    			    				
    			    			}else if(eJson.get("Address") instanceof JSONArray)
    			    			{
    			    				JSONArray	jsonArray = (JSONArray) eJson.get("Address");
    				    			////System.out.println("jsonArray ======"+jsonArray);
    					            
    			    				
    			    			    for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
    					            	JSONObject newJson=new JSONObject();
    					            	newJson = (JSONObject) iterator.next();
    					            	
    					            	if(newJson.get("Type").toString().equalsIgnoreCase("Primary"))
    					            	{
    						        		phy_address=newJson.get("AddressLine").toString();;
    					    				phy_city=newJson.get("City").toString();;
    					    				phy_state=newJson.get("StateCode").toString();;
    					    				phy_zip_code=newJson.get("ZipCode").toString();;
    					            	}
    				    				
    					    	    }
    			    			}
    			    			
    				    	}
    				    	
    				    	if(phyjson1.get("PhoneNumbers") instanceof JSONObject){

    				    		/*//System.out.println(json1.get(mkey));
    				    		//System.out.println(json1.get("PhoneNumber"));
    				    		//System.out.println(json1.get(mkey)  instanceof JSONObject);
    				    		//System.out.println(json1.get(mkey) instanceof JSONArray);*/

    				    		JSONObject eJson=(JSONObject)phyjson1.get("PhoneNumbers");
    				    		/*//System.out.println(eJson.get("PhoneNumber"));
    				    		//System.out.println(eJson.get("PhoneNumber")  instanceof JSONObject);
    				    		//System.out.println(eJson.get("PhoneNumber") instanceof JSONArray);*/

    				    		if(eJson.get("PhoneNumber") instanceof JSONObject)
    				    		{
    				    			JSONObject	jsonObj = (JSONObject) eJson.get("PhoneNumber");
    				    			////System.out.println("JSONObject ======"+jsonObj);

    				    			phy_phone=jsonObj.get("Number").toString();;
    				    		
    				    		}else if(eJson.get("PhoneNumber") instanceof JSONArray)
    				    		{


    				    			JSONArray	jsonArray = (JSONArray) eJson.get("PhoneNumber");
    				    			////System.out.println("jsonArray ======"+jsonArray);
    					            
    			    				
    			    			    for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
    					            	JSONObject newJson=new JSONObject();
    					            	newJson = (JSONObject) iterator.next();
    					            	
    					            	if(newJson.get("Type").toString().equalsIgnoreCase("Primary"))
    					            	{
    					            		phy_phone=newJson.get("Number").toString();;
    					    			}
    				    				
    					    	    }
    			    			  

    				    		}

    				    	}
    				    	
    		    	
    			    }
		    		
		    	}
		
		    	
		    	
			 
			    //System.out.println("rxJson =========="+rxJson);
		    	String rxNumber="",CurrentRxStatusText="",RefillsRemaining="",NumberOfRefillsFilled="",LastFillDateTime="",CurrentRxTransactionStatusText="",
		    			QuantityRemaining="",NumberOfRefillsAllowed="",CompletedDate="",TrackingNumber="",RxPioneerRxID="",RxFillTransactionPioneerRxID="",
		    					PreviousRxNumber="",PrescriberOrderNumber="",FutureFill="",PriorityTypeText="",ScriptType="",RefillNumber="";
		    	String prescribedNdc="",prescribedUpc="",pioneerRxItemID="",prescribedWrittenName="",prescribedGenericName="",prescribedDrugStrength="",prescribedQuantity="",prescribedUnitText="";
		    	String dispensedDrugName="",dispensedDrugStrength="",dispensedQuantity="",dispensedUnitText="",dispensedDaysSupply="",dispensedLotNumber="",dispensedLotExpirationDate="";
		    	String RxCommentsCritical="",FillCommentsCritical="",OriginTypeID="",dispensedUnitID="",dispensedSigCode="",dispensedSigCodeID="",deaschedule="";
		    	String TransmissionDate="",GrossAmountSubmitted="0",PatientPayAmountPaid="0",PercentageSalesTaxAmountPaid="0",PercentageSalesTaxAmountSubmitted="0",
		    			PercentageSalesTaxRatePaid="0",PercentageSalesTaxRateSubmitted="0",FlatSalesTaxAmountPaid="0",FlatSalesTaxAmountSubmitted="0",dispensedDrugPioneerItemId="";
		    	
		    	String DateFilledUTC="",ExpirationDateUTC="",DateWrittenUTC="";
		    	
		    	
		    	RxPioneerRxID=(String) rxJson.get("RxPioneerRxID");
		    	RxFillTransactionPioneerRxID=(String) rxJson.get("RxFillTransactionPioneerRxID");
		    	rxNumber=(String) rxJson.get("RxNumber");
		    	CurrentRxStatusText=(String) rxJson.get("CurrentRxStatusText");
		    	//'2019-02-02T05:00:00Z'
		    	if(rxJson.get("DateFilledUTC")!=null && rxJson.get("DateFilledUTC").toString().length()>0)
		    	{
		    		DateFilledUTC=(String)rxJson.get("DateFilledUTC");
		    		DateFilledUTC=DateFilledUTC.substring(0, DateFilledUTC.indexOf("T"))+"T00:00:00";
		    	}
		    	if(rxJson.get("DateWritten")!=null && rxJson.get("DateWritten").toString().length()>0)
		    	{
		    		DateWrittenUTC=(String)rxJson.get("DateWritten");
		    		DateWrittenUTC=DateWrittenUTC.substring(0, DateWrittenUTC.indexOf("T"))+"T00:00:00";
		    	}
		    	if(rxJson.get("ExpirationDateUTC")!=null && rxJson.get("ExpirationDateUTC").toString().length()>0)
		    	{
		    		ExpirationDateUTC=(String)rxJson.get("ExpirationDateUTC");
		    		ExpirationDateUTC=ExpirationDateUTC.substring(0, ExpirationDateUTC.indexOf("T"))+"T00:00:00";
		    	}
		    	System.out.println(DateFilledUTC+"===="+DateWrittenUTC+"===="+ExpirationDateUTC);
		    	
		    	/*DateWritten=(String) rxJson.get("DateWritten");
		    	ExpirationDateUTC=(String)rxJson.get("ExpirationDateUTC");*/
		    	RefillsRemaining=(String) rxJson.get("RefillsRemaining");
		    	NumberOfRefillsFilled=(String) rxJson.get("NumberOfRefillsFilled");
		    	OriginTypeID=(String) rxJson.get("OriginTypeID");
		    	
		    	CurrentRxTransactionStatusText=(String) rxJson.get("CurrentRxTransactionStatusText");
		    	QuantityRemaining=(String) rxJson.get("QuantityRemaining");
		    	NumberOfRefillsAllowed=(String) rxJson.get("NumberOfRefillsAllowed");
		    	CompletedDate=(String) rxJson.get("CompletedDate");
		    	TrackingNumber=(String) rxJson.get("TrackingNumber");
		    	PreviousRxNumber=(String) rxJson.get("PreviousRxNumber");
		    	PrescriberOrderNumber=(String) rxJson.get("PrescriberOrderNumber");
		    	FutureFill=(String) rxJson.get("FutureFill");
		    	PriorityTypeText=(String) rxJson.get("PriorityTypeText");
		    	ScriptType=(String) rxJson.get("ScriptType");
		    	RefillNumber=(String) rxJson.get("RefillNumber");
		    	dispensedSigCode=(String) rxJson.get("SigCode");
		    	
		    	if(rxJson.get("MedicationPrescribed") instanceof JSONObject){
		    		// loop to get the dynamic key
			    	JSONObject json1=(JSONObject)rxJson.get("MedicationPrescribed");
			    	prescribedNdc=(String)json1.get("NDC");
			    	prescribedUpc=(String)json1.get("UPC");
			    	pioneerRxItemID=(String)json1.get("PioneerRxItemID");
			    	prescribedWrittenName=(String)json1.get("WrittenName");
			    	prescribedGenericName=(String)json1.get("GenericName");
			    	prescribedDrugStrength=(String)json1.get("DrugStrength");
			    	prescribedQuantity=(String)json1.get("Quantity");
			    	prescribedUnitText=(String)json1.get("UnitText");
			  	}
		    	
		    	if(rxJson.get("MedicationDispensed") instanceof JSONObject){
		    		// loop to get the dynamic key
			    	JSONObject json1=(JSONObject)rxJson.get("MedicationDispensed");
			    	
			    	dispensedDrugPioneerItemId=(String)json1.get("PioneerRxItemID");
			    	dispensedDrugName=(String)json1.get("DrugName");
			    	dispensedDrugStrength=(String)json1.get("DrugStrength");
			    	dispensedQuantity=(String)json1.get("Quantity");
			    	dispensedUnitText=(String)json1.get("UnitText");
			    	
			    	dispensedDaysSupply=(String)json1.get("DaysSupply");
			    	dispensedLotNumber=(String)json1.get("LotNumber");
			    	dispensedLotExpirationDate=(String)json1.get("LotExpirationDate");
			    	LastFillDateTime=(String) json1.get("LastFillDateTime");
			    	if(LastFillDateTime!=null && LastFillDateTime.length()>0)
			    	{
			    		LastFillDateTime=(String)rxJson.get("LastFillDateTime");
			    		LastFillDateTime=LastFillDateTime.substring(0, LastFillDateTime.indexOf("T"))+"T00:00:00";
			    	}
			    	
			  	}
		    	if(rxJson.get("Comments") instanceof JSONObject)
	    		{
	    			JSONObject xx =(JSONObject) rxJson.get("Comments");
	    			
	    			JSONObject yy =(JSONObject) xx.get("RxComments");
	    			
	    			RxCommentsCritical=(String)yy.get("Critical");
	    		
	    			JSONObject zz =(JSONObject) xx.get("FillComments");
	    		
	    			FillCommentsCritical=(String)zz.get("Critical");
	    			
	    		}
		    	
		   
		    	if(claimsJson!=null && claimsJson.get("Claim")!=null){
		    	
			    	if(claimsJson.get("Claim") instanceof JSONObject){
			    		// loop to get the dynamic key
				    	JSONObject jsonObj=(JSONObject)claimsJson.get("Claim");
				   	
				    	if(TransmissionDate!=null && TransmissionDate.length()>0)
				    	{
				    		TransmissionDate=(String)rxJson.get("TransmissionDate");
				    		TransmissionDate=TransmissionDate.substring(0, TransmissionDate.indexOf("T"))+"T00:00:00";
				    	}
				    	
				    	//TransmissionDate=getFilteredText(jsonObj.get("TransmissionDate"));
				    	GrossAmountSubmitted=getFilteredText(jsonObj.get("GrossAmountSubmitted"));
				    	PatientPayAmountPaid=getFilteredText(jsonObj.get("PatientPayAmountPaid"));
				    	PercentageSalesTaxAmountPaid=getFilteredText(jsonObj.get("PercentageSalesTaxAmountPaid"));
				    	PercentageSalesTaxAmountSubmitted=getFilteredText(jsonObj.get("PercentageSalesTaxAmountSubmitted"));
				    	PercentageSalesTaxRatePaid=getFilteredText(jsonObj.get("PercentageSalesTaxRatePaid"));
				    	PercentageSalesTaxRateSubmitted=getFilteredText(jsonObj.get("PercentageSalesTaxRateSubmitted"));
				    	FlatSalesTaxAmountPaid=getFilteredText(jsonObj.get("FlatSalesTaxAmountPaid"));
				    	FlatSalesTaxAmountSubmitted=getFilteredText(jsonObj.get("FlatSalesTaxAmountSubmitted"));
						
				   
					    	
			    	}else if(claimsJson.get("Claim") instanceof JSONArray)
	    			{
	    				JSONArray	jsonArray = (JSONArray) claimsJson.get("Claim");
		    			////System.out.println("jsonArray ======"+jsonArray);
			            
			            for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
			            	JSONObject jsonObj=new JSONObject();
			            	jsonObj = (JSONObject) iterator.next();
			           	
			            	if(TransmissionDate!=null && TransmissionDate.length()>0)
					    	{
					    		TransmissionDate=(String)rxJson.get("TransmissionDate");
					    		TransmissionDate=TransmissionDate.substring(0, TransmissionDate.indexOf("T"))+"T00:00:00";
					    	}
			            	
			            	//TransmissionDate=getFilteredText(jsonObj.get("TransmissionDate"));;
					    	GrossAmountSubmitted=getFilteredText(jsonObj.get("GrossAmountSubmitted"));;
					    	PatientPayAmountPaid=getFilteredText(jsonObj.get("PatientPayAmountPaid"));;
					    	PercentageSalesTaxAmountPaid=getFilteredText(jsonObj.get("PercentageSalesTaxAmountPaid"));
					    	PercentageSalesTaxAmountSubmitted=getFilteredText(jsonObj.get("PercentageSalesTaxAmountSubmitted"));
					    	PercentageSalesTaxRatePaid=getFilteredText(jsonObj.get("PercentageSalesTaxRatePaid"));
					    	PercentageSalesTaxRateSubmitted=getFilteredText(jsonObj.get("PercentageSalesTaxRateSubmitted"));
					    	FlatSalesTaxAmountPaid=getFilteredText(jsonObj.get("FlatSalesTaxAmountPaid"));
					    	FlatSalesTaxAmountSubmitted=getFilteredText(jsonObj.get("FlatSalesTaxAmountSubmitted"));
					    
			    	    }
		    		
	    			}
		    	}
			    //###################################################################################################################################//
			    
		    	String selectSQL = "SELECT * FROM phy_info WHERE pioneer_uid=?";
		    	PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
		    	preparedStatement.setString(1, PrescriberPioneerRxID);
		    	ResultSet rs = preparedStatement.executeQuery();
		    	if(rs!=null){
			    	while (rs.next()) {
			    		physicianid = rs.getInt("physician_id");
			    	}
		    	}
		    	
		    	
		    	selectSQL = "SELECT * FROM patient_profile WHERE pioneer_uid = ?";
		    	preparedStatement = conn.prepareStatement(selectSQL);
		    	preparedStatement.setString(1, PatientPioneerRxID);
		    	rs = preparedStatement.executeQuery();
		    	if(rs!=null){
			    	while (rs.next()) {
			    		patientid = rs.getInt("patient_id");
			     	}
			    	rs.close();
		    	}
		    	
		    	
		    	
		    	
		    	int prescription_id=0,prescription_tran_id=0,order_id=0,invoice_id=0,prescription_number=0,invoice_number=0;
		    	String rx_number="",item_name="",cre8_prescription_no="",HiddenComment="",order_number="";
		    	String[] cre8_prescription_noArr=null;
		    	
		    	System.out.println("[Physician ID :: "+PrescriberPioneerRxID+"]"+physicianid+"==========[Patient ID :: "+PatientPioneerRxID+"]"+patientid);
		    	
		    	if(physicianid==0 || patientid==0)
		    	{
		    		
		    		if(physicianid==0)
		    		{
		    			//Create New Physician
		    			physicianid=PioneerChangedInfoUpdater.fetchPhysicianAndPatientInfo(false,true,"",PrescriberPioneerRxID,0);
		    		}
		    		
		    		if(patientid==0)
		    		{
		    			//Create New Patient
		    			patientid=PioneerChangedInfoUpdater.fetchPhysicianAndPatientInfo(true,false,PatientPioneerRxID,"",physicianid);
		    		}
		    		
		    	}
		    	
		    	
		    	if(physicianid>0 && patientid>0)
		    	{
		    		
		    		
				    		selectSQL = "SELECT * FROM phy_info WHERE pioneer_uid=?";
					    	preparedStatement = conn.prepareStatement(selectSQL);
					    	preparedStatement.setString(1, PrescriberPioneerRxID);
					    	rs = preparedStatement.executeQuery();
					    	if(rs!=null){
						    	while (rs.next()) {
						    		//physicianid = rs.getInt("physician_id");
						    		physicianName= rs.getString("physician_name");
						    		physicianClinicId=rs.getInt("clinic_id");
						    		
						    		
						    	
						    		
						    	}
						    	rs.close();
					    	}
					    	
					    	selectSQL = "SELECT * FROM phy_group WHERE physician_id=? and status='Active'";
					    	preparedStatement = conn.prepareStatement(selectSQL);
					    	preparedStatement.setInt(1, physicianid);
					    	rs = preparedStatement.executeQuery();
					    	if(rs!=null){
						    	while (rs.next()) {
						    			physicianGroupId=rs.getInt("group_id");
						    	}
						    	rs.close();
					    	}
						    	
					    	
		
					    	selectSQL = "SELECT * FROM phy_profile WHERE physician_id=?";
					    	preparedStatement = conn.prepareStatement(selectSQL);
					    	preparedStatement.setInt(1, physicianid);
					    	rs = preparedStatement.executeQuery();
					    	if(rs!=null){
						    	while (rs.next()) {
						    		phy_upin =rs.getString("upin");
						    		phy_medicaid =rs.getString("medicaid");
						    	}
						    	rs.close();
					    	}
					    	
					    	selectSQL = "SELECT * FROM patient_profile WHERE pioneer_uid = ?";
					    	preparedStatement = conn.prepareStatement(selectSQL);
					    	preparedStatement.setString(1, PatientPioneerRxID);
					    	rs = preparedStatement.executeQuery();
					    	if(rs!=null){
						    	while (rs.next()) {
						    		//patientid = rs.getInt("patient_id");
						    		patientName=rs.getString("patient_name");
						    		
						    		patient_mobile =rs.getString("mobile");
						    		patient_ssn  =rs.getString("SSN");
						    		patient_date_of_birth =rs.getObject("date_of_birth");
							     	
									
						     	}
						    	rs.close();
					    	}
			    	
			    	
		    				prescription_number=0;order_number="";invoice_number=0;
		    				cre8_prescription_no="";
				    		boolean rxExists=false;
				    		boolean orderExists=false;
				    		boolean invoiceExists=false;
			    			//String selectSQL2 = "SELECT * FROM prescription_transaction WHERE prescription_id = ? and rx_number =?";
				    		Connection dbConnection = null;
				    		PreparedStatement sta=null; 
				    		
				   		try {
				    		
				    			dbConnection = getSourceDBConnection1();
				    			
					    		if(dbConnection!=null){
					    			String Sql = "SELECT * FROM prescription.rx WHERE  RxNumber =?";
					    			
					    			sta = dbConnection.prepareStatement(Sql);
					    			sta.setString(1, rxNumber);
							    	
							    	ResultSet rs2 = sta.executeQuery();
							    	
							    	if(rs2!=null){
								    	while (rs2.next()) {
								    		HiddenComment=rs2.getString("HiddenComment");
								    		
								    		if(HiddenComment!=null && HiddenComment.length()>0){
								    		System.out.println("HiddenComment before 11111111===="+HiddenComment);
								    		HiddenComment=HiddenComment.replaceAll("\r\n","");
								    		HiddenComment=HiddenComment.replaceAll("\n","");
								    		System.out.println("HiddenComment after 11111111===="+HiddenComment);
								    		HiddenComment=HiddenComment.replaceAll("\\{","");
								    		HiddenComment=HiddenComment.replaceAll("\\}","");
									    		if(HiddenComment.length()>=116){
										    		System.out.println("HiddenComment afttttttttttter 11111111===="+HiddenComment);
										     		HiddenComment=HiddenComment.substring(116, HiddenComment.length()-4);
										    		System.out.println("HiddenComment 2222222222222222===="+HiddenComment);
										    		HiddenComment=HiddenComment.trim();
										    		System.out.println("HiddenComment 22222222===="+HiddenComment);
										    		cre8_prescription_no=HiddenComment.replaceAll("CRE8-", "");
										    		System.out.println("cre8_prescription_no ===="+cre8_prescription_no);
										    		cre8_prescription_noArr=cre8_prescription_no.split("-");
									    			}
								    			}
								    		}
							    		}
					    			}
				    		} catch (Exception e) {

				    			System.out.println(e.getMessage());
				    			e.printStackTrace();

				    		} finally {

				    			if (sta != null) {
				    				sta.close();
				    			}

				    			if (dbConnection != null) {
				    				dbConnection.close();
				    			}

				    		}
				   		
				   			//If CRE8-1030-1 exists in Hidden Comment
				   			//1030==> Prescription No
				   			//1==> No. of medicine item (RX) prescribed in that Prescription (1030)
					   		if(cre8_prescription_noArr!=null && cre8_prescription_noArr.length>=2){
						   		String selectSQL2 = "SELECT * FROM prescription_master pmst,prescription_transaction ptran WHERE "
					    				+ "pmst.prescription_id=ptran.prescription_id and pmst.prescription_number = ? and ptran.prescription_no =?";
		
				    			PreparedStatement preparedStatement2 = conn.prepareStatement(selectSQL2);
				    			preparedStatement2.setObject(1, cre8_prescription_noArr[0]);
						    	preparedStatement2.setObject(2, cre8_prescription_noArr[1]);
						    	
						    	ResultSet rs2 = preparedStatement2.executeQuery();
						    	
						    	if(rs2!=null){
							    	while (rs2.next()) {
							    		rxExists=true;
							    		rx_number = rs2.getString("rx_number");
							    		prescription_id = rs2.getInt("prescription_id");
							    		prescription_tran_id=rs2.getInt("prescription_tran_id");
							    		group_id=rs2.getInt("group_id");
							    	}
							    	rs2.close();
						    	}
					   		}else if (cre8_prescription_noArr!=null && cre8_prescription_noArr.length>=1)
					   		{
					   			//If CRE8-1030 exists in Hidden Comment
					   			//1030==> Prescription No
					   			//1==> No. of medicine item (RX) prescribed in that Prescription (1030) by default
					   			String selectSQL2 = "SELECT * FROM prescription_master pmst,prescription_transaction ptran WHERE "
					    				+ "pmst.prescription_id=ptran.prescription_id and pmst.prescription_number = ? and ptran.prescription_no =?";
		
				    			PreparedStatement preparedStatement2 = conn.prepareStatement(selectSQL2);
				    			preparedStatement2.setObject(1, cre8_prescription_noArr[0]);
						    	preparedStatement2.setObject(2, 1);
						    	
						    	ResultSet rs2 = preparedStatement2.executeQuery();
						    	
						    	if(rs2!=null){
							    	while (rs2.next()) {
							    		rxExists=true;
							    		rx_number = rs2.getString("rx_number");
							    		prescription_id = rs2.getInt("prescription_id");
							    		prescription_tran_id=rs2.getInt("prescription_tran_id");
							    		group_id=rs2.getInt("group_id");
							    	}
							    	rs2.close();
						    	}
					   			
					   		}else if (cre8_prescription_noArr==null && cre8_prescription_no.length()==0)
					   		{
					   			//No Hidden Comment found
					   			//Check for RX Number
					   			String selectSQL2 = "SELECT * FROM prescription_master pmst,prescription_transaction ptran WHERE "
					    				+ "pmst.prescription_id=ptran.prescription_id and ptran.rx_number = ?";
		
				    			PreparedStatement preparedStatement2 = conn.prepareStatement(selectSQL2);
				    			preparedStatement2.setObject(1, rxNumber);
						    	
						    	ResultSet rs2 = preparedStatement2.executeQuery();
						    	
						    	if(rs2!=null){
							    	while (rs2.next()) {
							    		rxExists=true;
							    		rx_number = rs2.getString("rx_number");
							    		prescription_id = rs2.getInt("prescription_id");
							    		prescription_tran_id=rs2.getInt("prescription_tran_id");
							    		group_id=rs2.getInt("group_id");
							    	}
							    	rs2.close();
						    	}
					   			
					   		}
					    
					   			System.out.println("rxExists ========="+rxExists);
					    		//If RX Number Already Exists just update the status and other info from rx
						    	if(rxExists)
					    		{
						    		String pupdateTableSQL3 = "UPDATE prescription_master SET "
							    			+ "last_updated_by=?,last_updated_user=?,last_updated_date=?,status_id=? "
							    					+ "WHERE prescription_id = ?";
							    			PreparedStatement pupreparedStatement3 = conn.prepareStatement(pupdateTableSQL3);
							    			
									    	pupreparedStatement3.setObject(1, "0");
									    	pupreparedStatement3.setObject(2, "RX");
									    	pupreparedStatement3.setObject(3, getFilteredText(getCurrentTimeStamp()));
									    	pupreparedStatement3.setInt(4, 6);//6==>Pioneer Rx Generated
									    	pupreparedStatement3.setInt(5, prescription_id);
									    
									    	
									    	// execute update SQL stetement
									    	pupreparedStatement3 .executeUpdate();
									    	
						    		
						 	    	
					    			String updateTableSQL = "UPDATE prescription_transaction SET "
					    					+ "rx_status = ?,"
					    					+ "refills_remaining=?,"
					    					+ "refills_filled=?,"
					    					+ "last_filled_date=?,"
					    					+ "previous_rx_number=?,"
					    					+ "prescriber_order_number=?,"
					    					+ "tracking_number=?,"
					    					+ "future_fill=?,"
					    					+ "priorty_type=?,"
					    					+ "script_type=?, "
					    					+ "dispensed_itemid=?, "
					    					+ "dispensed_itemname=?, "
					    					+ "rx_number=? "
					    					+ "WHERE prescription_tran_id = ?";
					    			PreparedStatement upreparedStatement = conn.prepareStatement(updateTableSQL);
					    			upreparedStatement.setObject(1, CurrentRxStatusText);
					    			upreparedStatement.setObject(2, RefillsRemaining);
					    			upreparedStatement.setObject(3, NumberOfRefillsFilled);
					    			upreparedStatement.setObject(4, LastFillDateTime);
					    			upreparedStatement.setObject(5, PreviousRxNumber);
					    			upreparedStatement.setObject(6, PrescriberOrderNumber);
					    			upreparedStatement.setObject(7, TrackingNumber);
					    			upreparedStatement.setObject(8, FutureFill);
					    			upreparedStatement.setObject(9, PriorityTypeText);
					    			upreparedStatement.setObject(10, ScriptType);
					    			upreparedStatement.setObject(11, dispensedDrugPioneerItemId);
					    			upreparedStatement.setObject(12, dispensedDrugName);
					    			upreparedStatement.setObject(13, rxNumber);
					    			
					    			upreparedStatement.setInt(14, prescription_tran_id);
					    			// execute update SQL stetement
					    			upreparedStatement .executeUpdate();
					    			
					    			
					    			//Checks whether the Script of Order is Original/Refill with the Refill Number,if so updates the order info else creates new order
					    			String selectSQL21 = "select * from order_transaction where prescription_tran_id = ? and scriptType=? and refillNumber=?";

					    			PreparedStatement preparedStatement21 = conn.prepareStatement(selectSQL21);
					    			preparedStatement21.setObject(1, prescription_tran_id);
							    	preparedStatement21.setObject(2, ScriptType);
							    	preparedStatement21.setObject(3, RefillNumber);
								    
							    	
							    	ResultSet rs21 = preparedStatement21.executeQuery();
							    	
							    	if(rs21!=null){
								    	while (rs21.next()) {
								    		orderExists=true;
								    		order_id=rs21.getInt("order_id");
								    	}
								    	rs21.close();
							    	}
							    	
					    			//Checks whether the Script of Order is Original/Refill with the Refill Number,if so updates the invoice info else creates new invoice
							    	String selectSQL31 = "select * from invoice_transaction where prescription_tran_id = ? and scriptType=? and refillNumber=?";

					    			PreparedStatement preparedStatement31 = conn.prepareStatement(selectSQL31);
					    			preparedStatement31.setObject(1, prescription_tran_id);
							    	preparedStatement31.setObject(2, ScriptType);
							    	preparedStatement31.setObject(3, RefillNumber);
								    System.out.println("selectSQL31 ==="+selectSQL31);
								    System.out.println(prescription_tran_id+"========"+ScriptType+"======="+RefillNumber);
							    	
							    	ResultSet rs31 = preparedStatement31.executeQuery();
							    	
							    	if(rs31!=null){
								    	while (rs31.next()) {
								    		invoiceExists=true;
								    		invoice_id=rs31.getInt("invoice_id");
								    	}
								    	rs31.close();
							    	}
							    	//get max order number
					    			/*String selectSQL212 = "select  max(order_number) from order_master";
					    			PreparedStatement preparedStatement212 = conn.prepareStatement(selectSQL212);
					    		  	ResultSet rs212 = preparedStatement212.executeQuery();
							    	
							    	if(rs212!=null){
								    	while (rs212.next()) {
								    		order_number=Integer.parseInt(rs212.getString(1));
								    	}
								    	rs212.close();
								    	order_number=order_number+1;
							    	}*/
							    	
							    	order_number=HiddenComment+"-"+RefillNumber;
							    	
							    	//get max invoice number
					    			String selectSQL312 = "select  max(invoice_number) from invoice_master";
					    			PreparedStatement preparedStatement312 = conn.prepareStatement(selectSQL312);
					    		  	ResultSet rs312 = preparedStatement312.executeQuery();
							    	
							    	if(rs312!=null){
								    	while (rs312.next()) {
								    		invoice_number=Integer.parseInt(rs312.getString(1));
								    	}
								    	rs312.close();
								    	invoice_number=invoice_number+1;
							    	}
							    	
							    	
							    	if(orderExists){
							    		
							    		
							    		//Update Already Existing Order
							    		
						    			String updateTableSQL3 = "UPDATE order_master SET "
						    			+ "shipping_address=?,shipping_city=?,shipping_state=?,shipping_zip_code=?,"
						    			+ "last_updated_by=?,last_updated_user=?,last_updated_date=? "
						    					+ "WHERE order_id=?";
						    			PreparedStatement upreparedStatement3 = conn.prepareStatement(updateTableSQL3);
						    			upreparedStatement3.setObject(1, getFilteredText(shipping_address));
								    	upreparedStatement3.setObject(2, getFilteredText(shipping_city));;
								    	upreparedStatement3.setObject(3, getFilteredText(shipping_state));
								    	upreparedStatement3.setObject(4, getFilteredText(shipping_zip_code));
								    	
								    	upreparedStatement3.setObject(5, "0");
								    	upreparedStatement3.setObject(6, "RX");
								    	upreparedStatement3.setObject(7, getFilteredText(getCurrentTimeStamp()));
								    	upreparedStatement3.setInt(8, order_id);
								    	// execute update SQL stetement
								    	upreparedStatement3 .executeUpdate();
						    			
						    			String updateTableSQL2 = "UPDATE order_transaction SET "
						    					+ "rx_status=?,prescribed_name=?,prescribed_drug=?,"
						    			+ "prescribed_quantity=?,prescribed_unit=?,dispensed_name=?,dispensed_drug=?,dispensed_quantity=?,"
						    			+ "dispensed_unit=?,days_supply=?,quantity_remaining=?,refills_allowed=?,refills_filled=?,"
						    			+ "refills_remaining=?,last_filled_date=?,tracking_number=?,"
						    			+ "priority_type=?,lot_number=?,lot_expiration_date=?,rx_comments=?,completed_date=? "
						    					+ "WHERE order_id=?";
						    			PreparedStatement upreparedStatement2 = conn.prepareStatement(updateTableSQL2);
						    			upreparedStatement2.setObject(1, getFilteredText(CurrentRxTransactionStatusText));
								    	upreparedStatement2.setObject(2, getFilteredText(prescribedWrittenName));;
								    	upreparedStatement2.setObject(3, getFilteredText(prescribedDrugStrength));
								    	upreparedStatement2.setObject(4, getFilteredText(prescribedQuantity));
								    	upreparedStatement2.setObject(5, getFilteredText(prescribedUnitText));
								    	upreparedStatement2.setObject(6, getFilteredText(dispensedDrugName));
								    	upreparedStatement2.setObject(7, getFilteredText(dispensedDrugStrength));
								    	upreparedStatement2.setObject(8, getFilteredText(dispensedQuantity));
								    	upreparedStatement2.setObject(9, getFilteredText(dispensedUnitText));
								    	upreparedStatement2.setObject(10, getFilteredText(dispensedDaysSupply));
								    	upreparedStatement2.setObject(11, getFilteredText(QuantityRemaining));
								    	upreparedStatement2.setObject(12, getFilteredText(NumberOfRefillsAllowed));
								    	upreparedStatement2.setObject(13, getFilteredText(NumberOfRefillsFilled));
								    	upreparedStatement2.setObject(14, getFilteredText(RefillsRemaining));
								    	upreparedStatement2.setObject(15, LastFillDateTime);
								    	upreparedStatement2.setObject(16, getFilteredText(TrackingNumber));
								    	upreparedStatement2.setObject(17, getFilteredText(PriorityTypeText));
								    	upreparedStatement2.setObject(18, getFilteredText(dispensedLotNumber));
								    	upreparedStatement2.setObject(19, dispensedLotExpirationDate);
								    	upreparedStatement2.setObject(20, getFilteredText(RxCommentsCritical));
								    	upreparedStatement2.setObject(21, CompletedDate);
								    	upreparedStatement2.setInt(22, order_id);
						    			// execute update SQL stetement
								    	upreparedStatement2 .executeUpdate();
								    	
								    	
							    	}else
							    	{
							    		//Create New Order
								    	
								    	String insertTableSQL = "INSERT INTO order_master"
								    			+ "(order_number, order_date, group_id, physician_id,patient_id,"
								    			+ "shipping_address,shipping_city,shipping_state,shipping_zip_code,"
								    			+ "created_by,created_user,created_date,last_updated_by,last_updated_user,last_updated_date) VALUES"
								    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
								    	PreparedStatement ipreparedStatement = conn.prepareStatement(insertTableSQL, Statement.RETURN_GENERATED_KEYS);
								    	ipreparedStatement.setObject(1, prescription_number_format+getFilteredText(prescription_number)+"-"+1+order_number);
								    	ipreparedStatement.setObject(2, getFilteredText(getCurrentTimeStamp()));
								    	ipreparedStatement.setObject(3, getFilteredText(physicianGroupId));
								    	ipreparedStatement.setObject(4, getFilteredText(physicianid));
								    	ipreparedStatement.setObject(5, getFilteredText(patientid));
								    	ipreparedStatement.setObject(6, getFilteredText(shipping_address));
								    	ipreparedStatement.setObject(7, getFilteredText(shipping_city));
								    	ipreparedStatement.setObject(8, getFilteredText(shipping_state));
								    	ipreparedStatement.setObject(9, getFilteredText(shipping_zip_code));
								    	
								    	ipreparedStatement.setObject(10, "0");
								    	ipreparedStatement.setObject(11, "RX");
								    	ipreparedStatement.setObject(12, getFilteredText(getCurrentTimeStamp()));
								    	
								    	ipreparedStatement.setObject(13, "0");
								    	ipreparedStatement.setObject(14, "RX");
								    	ipreparedStatement.setObject(15, getFilteredText(getCurrentTimeStamp()));
								   	
								    	// execute insert SQL stetement
								    	int orderid=0;
								    	ipreparedStatement .executeUpdate();
								    	
								    	   ResultSet generatedKeys = ipreparedStatement.getGeneratedKeys();
								           if (generatedKeys.next()) {
								                System.out.println("id is"+generatedKeys.getLong(1));
								                orderid= generatedKeys.getInt(1);
								           } else {
								               throw new SQLException("Creating order failed, no generated key obtained.");
								           }
								           
								    	
								    	String insertTableSQL2 = "INSERT INTO order_transaction "
								    			+ "(order_id,rx_number,rx_status,prescribed_name,prescribed_drug,"
								    			+ "prescribed_quantity,prescribed_unit,dispensed_name,dispensed_drug,dispensed_quantity,"
								    			+ "dispensed_unit,days_supply,quantity_remaining,refills_allowed,refills_filled,"
								    			+ "refills_remaining,last_filled_date,tracking_number,prescription_tran_id,"
								    			+ "priority_type,lot_number,lot_expiration_date,rx_comments,completed_date,scriptType,refillNumber) VALUES "
								    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
								    	PreparedStatement ipreparedStatement2 = conn.prepareStatement(insertTableSQL2);
								    	ipreparedStatement2.setObject(1, orderid);
								    	ipreparedStatement2.setObject(2, getFilteredText(rxNumber));
								    	ipreparedStatement2.setObject(3, getFilteredText(CurrentRxTransactionStatusText));
								    	ipreparedStatement2.setObject(4, getFilteredText(prescribedWrittenName));;
								    	ipreparedStatement2.setObject(5, getFilteredText(prescribedDrugStrength));
								    	ipreparedStatement2.setObject(6, getFilteredText(prescribedQuantity));
								    	ipreparedStatement2.setObject(7, getFilteredText(prescribedUnitText));
								    	ipreparedStatement2.setObject(8, getFilteredText(dispensedDrugName));
								    	ipreparedStatement2.setObject(9, getFilteredText(dispensedDrugStrength));
								    	ipreparedStatement2.setObject(10, getFilteredText(dispensedQuantity));
								    	ipreparedStatement2.setObject(11, getFilteredText(dispensedUnitText));
								    	ipreparedStatement2.setObject(12, getFilteredText(dispensedDaysSupply));
								    	ipreparedStatement2.setObject(13, getFilteredText(QuantityRemaining));
								    	ipreparedStatement2.setObject(14, getFilteredText(NumberOfRefillsAllowed));
								    	ipreparedStatement2.setObject(15, getFilteredText(NumberOfRefillsFilled));
								    	ipreparedStatement2.setObject(16, getFilteredText(RefillsRemaining));
								    	ipreparedStatement2.setObject(17, LastFillDateTime);
								    	ipreparedStatement2.setObject(18, getFilteredText(TrackingNumber));
								    	ipreparedStatement2.setObject(19, getFilteredText(prescription_tran_id));
								    	ipreparedStatement2.setObject(20, getFilteredText(PriorityTypeText));
								    	ipreparedStatement2.setObject(21, getFilteredText(dispensedLotNumber));
								    	ipreparedStatement2.setObject(22, dispensedLotExpirationDate);
								    	ipreparedStatement2.setObject(23, getFilteredText(RxCommentsCritical));
								    	ipreparedStatement2.setObject(24, CompletedDate);
								      	ipreparedStatement2.setObject(25,ScriptType);
								    	ipreparedStatement2.setObject(26,RefillNumber);
								   
								    	
								    	// execute insert SQL stetement
								    	int ordertranid=ipreparedStatement2 .executeUpdate();
							    		
							    		
							    	}
							    	
							    	
							    	
							    	if(invoiceExists){
							    		
								    	//Update Already Existing Invoice
								    	
								    	String updateTableSQL4 = "UPDATE invoice_master SET "
								    			//+ "invoice_number=?,invoice_date=?,rx_number=?,written_date=?,patient_id=?,physician_id=?,"
								    			+ "billing_name=?,billing_address=?,billing_city=?,billing_state=?,billing_zipcode=?,"
								    			+ "shipping_name=?,shipping_address=?,shipping_city=?,shipping_state=?,shipping_zipcode=?,"
								    			+ "subtotal=?,tax=?,total=?,last_updated_by=?,last_updated_user=?,last_updated_date=?,"
								    			+ "percentageSalesTaxAmountPaid=?,percentageSalesTaxAmountSubmitted=?,percentageSalesTaxRatePaid=?,"
								    			+ "percentageSalesTaxRateSubmitted=?,flatSalesTaxAmountPaid=?,flatSalesTaxAmountSubmitted=?"
								    			+ " WHERE invoice_id= ?";
								    	
								    	PreparedStatement upreparedStatement4 = conn.prepareStatement(updateTableSQL4);
								   	
								    	upreparedStatement4.setObject(1, getFilteredText(patientName));
								    	upreparedStatement4.setObject(2, getFilteredText(shipping_address));
								    	upreparedStatement4.setObject(3, getFilteredText(shipping_city));
								    	upreparedStatement4.setObject(4, getFilteredText(shipping_state));
								    	upreparedStatement4.setObject(5, getFilteredText(shipping_zip_code));
								    	
								    	upreparedStatement4.setObject(6, getFilteredText(patientName));
								    	upreparedStatement4.setObject(7, getFilteredText(shipping_address));
								    	upreparedStatement4.setObject(8, getFilteredText(shipping_city));
								    	upreparedStatement4.setObject(9, getFilteredText(shipping_state));
								    	upreparedStatement4.setObject(10, getFilteredText(shipping_zip_code));
								    	
								    	upreparedStatement4.setObject(11, getFilteredText(GrossAmountSubmitted));
								    	upreparedStatement4.setObject(12, new Double(PercentageSalesTaxAmountSubmitted)+new Double(FlatSalesTaxAmountSubmitted));
								    	upreparedStatement4.setObject(13, getFilteredText(PatientPayAmountPaid));
								    	
								    	upreparedStatement4.setObject(14, "0");
								    	upreparedStatement4.setObject(15, "RX");
								    	upreparedStatement4.setObject(16, getFilteredText(getCurrentTimeStamp()));
								    	
								    	upreparedStatement4.setObject(17, getFilteredText(PercentageSalesTaxAmountPaid));
								    	upreparedStatement4.setObject(18, getFilteredText(PercentageSalesTaxAmountSubmitted));
								    	upreparedStatement4.setObject(19, getFilteredText(PercentageSalesTaxRatePaid));
								    	
								    	upreparedStatement4.setObject(20, getFilteredText(PercentageSalesTaxRateSubmitted));
								    	upreparedStatement4.setObject(21, getFilteredText(FlatSalesTaxAmountPaid));
								    	upreparedStatement4.setObject(22, getFilteredText(FlatSalesTaxAmountSubmitted));
								    	
								    	upreparedStatement4.setInt(23, invoice_id);
								    	
								    	// execute update SQL stetement
								    	upreparedStatement4 .executeUpdate();
								    	
								    	String updateTableSQL5 = "UPDATE invoice_transaction SET "
								    			+ "item=?,quantity=?,total=?,lot_number=?,expiration_date=?,rx_number=?,days_supply=?,numberOfRefillsFilled=? "
								    			+ " WHERE invoice_id = ?";
								    	PreparedStatement upreparedStatement5 = conn.prepareStatement(updateTableSQL5);
								    	upreparedStatement5.setObject(1, getFilteredText(dispensedDrugName));
								    	upreparedStatement5.setObject(2, getFilteredText(dispensedQuantity));
								    	upreparedStatement5.setObject(3, getFilteredText(GrossAmountSubmitted));
								    	upreparedStatement5.setObject(4, getFilteredText(dispensedLotNumber));
								    	upreparedStatement5.setObject(5, getFilteredText(dispensedLotExpirationDate));
								    	upreparedStatement5.setObject(6, getFilteredText(rxNumber));
								    	upreparedStatement5.setObject(7, getFilteredText(dispensedDaysSupply));
								    	upreparedStatement5.setObject(8, getFilteredText(NumberOfRefillsFilled));
								    	upreparedStatement5.setInt(9, invoice_id);
								    	
								    	// execute update SQL stetement
								    	upreparedStatement5.executeUpdate();
							    	
							    	}else
							    	{
							    		//Create New Invoice
								    	
								    	String insertInvTableSQL = "INSERT INTO invoice_master"
								    			+ "(invoice_number,invoice_date,rx_number,written_date,patient_id,physician_id,"
								    			+ "billing_name,billing_address,billing_city,billing_state,billing_zipcode,"
								    			+ "shipping_name,shipping_address,shipping_city,shipping_state,shipping_zipcode,"
								    			+ "subtotal,tax,total,created_by,created_user,created_date,last_updated_by,last_updated_user,last_updated_date,"
								    			+ "percentageSalesTaxAmountPaid,percentageSalesTaxAmountSubmitted,percentageSalesTaxRatePaid,"
								    			+ "percentageSalesTaxRateSubmitted,flatSalesTaxAmountPaid,flatSalesTaxAmountSubmitted) VALUES"
								    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
								    	PreparedStatement invpreparedStatement = conn.prepareStatement(insertInvTableSQL, Statement.RETURN_GENERATED_KEYS);
								    	invpreparedStatement.setObject(1, invoice_number);
								    	invpreparedStatement.setObject(2, getFilteredText(getCurrentTimeStamp()));
								    	invpreparedStatement.setObject(3, rxNumber);
								    	if(TransmissionDate!=null && TransmissionDate.length()>0)
								    		invpreparedStatement.setObject(4, TransmissionDate);
								    	else
								    		invpreparedStatement.setObject(4,getFilteredText(getCurrentTimeStamp()) );
								    	invpreparedStatement.setObject(5, getFilteredText(patientid));
								    	invpreparedStatement.setObject(6, getFilteredText(physicianid));
								    	
								    	invpreparedStatement.setObject(7, getFilteredText(patientName));
								    	invpreparedStatement.setObject(8, getFilteredText(shipping_address));
								    	invpreparedStatement.setObject(9, getFilteredText(shipping_city));
								    	invpreparedStatement.setObject(10, getFilteredText(shipping_state));
								    	invpreparedStatement.setObject(11, getFilteredText(shipping_zip_code));
								    	
								    	invpreparedStatement.setObject(12, getFilteredText(patientName));
								    	invpreparedStatement.setObject(13, getFilteredText(shipping_address));
								    	invpreparedStatement.setObject(14, getFilteredText(shipping_city));
								    	invpreparedStatement.setObject(15, getFilteredText(shipping_state));
								    	invpreparedStatement.setObject(16, getFilteredText(shipping_zip_code));
								    	
								    	invpreparedStatement.setObject(17, getFilteredText(GrossAmountSubmitted));
								    	invpreparedStatement.setObject(18, new Double(PercentageSalesTaxAmountSubmitted)+new Double(FlatSalesTaxAmountSubmitted));
								    	invpreparedStatement.setObject(19, getFilteredText(PatientPayAmountPaid));
								    	
								    	invpreparedStatement.setObject(20, "0");
								    	invpreparedStatement.setObject(21, "RX");
								    	invpreparedStatement.setObject(22, getFilteredText(getCurrentTimeStamp()));
								    	
								    	invpreparedStatement.setObject(23, "0");
								    	invpreparedStatement.setObject(24, "RX");
								    	invpreparedStatement.setObject(25, getFilteredText(getCurrentTimeStamp()));
								    	
								    	invpreparedStatement.setObject(26, getFilteredText(PercentageSalesTaxAmountPaid));
								    	invpreparedStatement.setObject(27, getFilteredText(PercentageSalesTaxAmountSubmitted));
								    	invpreparedStatement.setObject(28, getFilteredText(PercentageSalesTaxRatePaid));
								    	
								    	invpreparedStatement.setObject(29, getFilteredText(PercentageSalesTaxRateSubmitted));
								    	invpreparedStatement.setObject(30, getFilteredText(FlatSalesTaxAmountPaid));
								    	invpreparedStatement.setObject(31, getFilteredText(FlatSalesTaxAmountSubmitted));
								    	
								    	
								    	// execute insert SQL stetement
								    	int invoiceid=0;
								    			invpreparedStatement .executeUpdate();
								    	
								    	
								    	  ResultSet generatedKeys2 = invpreparedStatement.getGeneratedKeys();
								           if (generatedKeys2.next()) {
								                System.out.println("id is"+generatedKeys2.getLong(1));
								                invoiceid= generatedKeys2.getInt(1);
								           } else {
								               throw new SQLException("Creating invoice failed, no generated key obtained.");
								           }
								           
								           
								    	String insertInvTableSQL2 = "INSERT INTO invoice_transaction "
								    			+ "(invoice_id,item,quantity,total,lot_number,expiration_date,prescription_tran_id,rx_number,days_supply,numberOfRefillsFilled,paymentstatus,scriptType,refillNumber) VALUES "
								    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
								    	PreparedStatement invpreparedStatement2 = conn.prepareStatement(insertInvTableSQL2);
								    	invpreparedStatement2.setObject(1, invoiceid);
								    	invpreparedStatement2.setObject(2, getFilteredText(dispensedDrugName));
								    	invpreparedStatement2.setObject(3, getFilteredText(dispensedQuantity));
								    	invpreparedStatement2.setObject(4, getFilteredText(GrossAmountSubmitted));;
								    	invpreparedStatement2.setObject(5, getFilteredText(dispensedLotNumber));
								    	invpreparedStatement2.setObject(6, getFilteredText(dispensedLotExpirationDate));
								    	invpreparedStatement2.setObject(7, getFilteredText(prescription_tran_id));
								    	invpreparedStatement2.setObject(8, getFilteredText(rxNumber));
								    	invpreparedStatement2.setObject(9, getFilteredText(dispensedDaysSupply));
								    	invpreparedStatement2.setObject(10, getFilteredText(NumberOfRefillsFilled));
								    	invpreparedStatement2.setObject(11, getFilteredText("Not Paid"));
								    	invpreparedStatement2.setObject(12,ScriptType);
								    	invpreparedStatement2.setObject(13,RefillNumber);
								    	// execute insert SQL stetement
								    	int invoicetranid=invpreparedStatement2 .executeUpdate();
								    }
						    			
					    		}else//if(!rxExists)
						    	{
					    				boolean newPioneerRX=true;
					    				
					    				order_number=HiddenComment+"-"+RefillNumber;
					    				
								    	//get max invoice number
						    			try {
											String selectSQL312 = "select  max(invoice_number) from invoice_master";
											PreparedStatement preparedStatement312 = conn.prepareStatement(selectSQL312);
											ResultSet rs312 = preparedStatement312.executeQuery();
											
											if(rs312!=null){
												while (rs312.next()) {
													invoice_number=Integer.parseInt(rs312.getString(1));
												}
												rs312.close();
												invoice_number=invoice_number+1;
											}
										} catch (Exception e) {
											invoice_number=Integer.parseInt(defaultPrescriptionNumber);
										}
								    	
								    	if(HiddenComment!=null && HiddenComment.length()>0)
								    		prescription_number=Integer.parseInt(cre8_prescription_noArr[0]);
								    	else
								    	{
								    		try {
												//get max prescription_number
												String selectSQL412 = "select  max(prescription_number) from prescription_master";
												PreparedStatement preparedStatement412 = conn.prepareStatement(selectSQL412);
												ResultSet rs412 = preparedStatement412.executeQuery();
												
												if(rs412!=null){
													while (rs412.next()) {
														prescription_number=Integer.parseInt(rs412.getString(1));
													}
													rs412.close();
													prescription_number=prescription_number+1;
												}
											} catch (Exception e) {
												prescription_number=Integer.parseInt(defaultPrescriptionNumber);
											}
								    		
								    		
								    	}
								    	
								    	if(newPioneerRX)
									   	{
								    			//If the prescription for the RX does not exists in the CRE8 Portal system, create new prescription, order and invoice for the corresponding RX Event
									    		int newphysicianid=0,newpatientid=0,newphysiciangroupid=0,newphysicianclinicid=0,medicationid=0,
									    				unitid=0,clinicalformulationid=0,itemtypeid=0;
									    		String newphysicianName="",newpatientName="",newphysicianClinicName="";
									    		String clinicAddress="",clinicCity="",clinicState="",clinicZipcode="",itemid="",itemname="";
									    				
									    		String selectSQL1 = "SELECT * FROM phy_info WHERE pioneer_uid=?";
										    	PreparedStatement preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setString(1, PrescriberPioneerRxID);
										    	ResultSet rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		newphysicianid = rs1.getInt("physician_id");
											    		newphysicianName= rs1.getString("physician_name");
											    		//newphysicianclinicid= rs1.getInt("clinic_id");
											    		
											    	}
											    	rs1.close();
										    	}
										    	
										    	selectSQL1 = "SELECT * FROM phy_group WHERE physician_id=?";
										    	preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setInt(1, newphysicianid);
										    	rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		newphysiciangroupid = rs1.getInt("group_id");
											    	}
											    	rs1.close();
										    	}
										    	
										    	selectSQL1 = "SELECT * FROM phy_clinic WHERE physician_id=?";
										    	preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setInt(1, newphysicianid);
										    	rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		newphysicianclinicid = rs1.getInt("clinic_id");
											    	}
											    	rs1.close();
										    	}
										    	
										    	selectSQL1 = "SELECT * FROM clinic WHERE clinic_id=?";
										    	preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setInt(1, newphysicianclinicid);
										    	rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		newphysicianClinicName = rs1.getString("clinic_name");
											    		clinicAddress=rs1.getString("address");
											    		clinicCity=rs1.getString("city");
											    		clinicState=rs1.getString("state");
											    		clinicZipcode=rs1.getString("zip_code");
											    	}
											    	rs1.close();
										    	}
										    	
										    	selectSQL1 = "SELECT * FROM patient_profile WHERE pioneer_uid = ?";
										    	preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setString(1, PatientPioneerRxID);
										    	rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		newpatientid = rs1.getInt("patient_id");
											    		newpatientName=rs1.getString("patient_name");
											    	}
											    	rs1.close();
										    	}
										    	
												
										    	selectSQL1 = "SELECT * FROM `pioneer.fdb.medmedication` WHERE medicationdescription = ?";
										    	preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setString(1, prescribedWrittenName);
										    	rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		medicationid = rs1.getInt("medicationid");
											    		clinicalformulationid= rs1.getInt("clinicalformulationid");
											    		itemtypeid=2;
											    	}
											    	rs1.close();
										    	}
										    	
										    	if(medicationid==0){
											    	selectSQL1 = "SELECT * FROM `pioneer.fdb.medmedication` WHERE medicationdescription = ?";
											    	preparedStatement1 = conn.prepareStatement(selectSQL1);
											    	preparedStatement1.setString(1,prescribedGenericName );
											    	rs1 = preparedStatement1.executeQuery();
											    	if(rs1!=null){
												    	while (rs1.next()) {
												    		medicationid = rs1.getInt("medicationid");
												    		clinicalformulationid= rs1.getInt("clinicalformulationid");
												    		itemtypeid=2;
												    	}
												    	rs1.close();
											    	}
											    	prescribedWrittenName=prescribedGenericName;
										    	}
										    	
										    	if(medicationid==0){
										    		
										    		selectSQL1 = "SELECT * FROM `pioneer.item.item` WHERE itemid = ?";
											    	preparedStatement1 = conn.prepareStatement(selectSQL1);
											    	preparedStatement1.setString(1, pioneerRxItemID);
											    	rs1 = preparedStatement1.executeQuery();
											    	if(rs1!=null){
												    	while (rs1.next()) {
												    		itemid = rs1.getString("itemid");
												    		itemtypeid=1;
												    	}
												    	rs1.close();
											    	}
											    	
											    	if(itemid==null || itemid.length()==0){
												    	selectSQL1 = "SELECT * FROM `pioneer.item.item` WHERE (itemname = ? or itemname = ?)";
												    	preparedStatement1 = conn.prepareStatement(selectSQL1);
												    	preparedStatement1.setString(1,prescribedWrittenName );
												    	preparedStatement1.setString(2,prescribedGenericName );
												    	rs1 = preparedStatement1.executeQuery();
												    	if(rs1!=null){
													    	while (rs1.next()) {
													    		itemid = rs1.getString("itemid");
													    		itemtypeid=1;
													    	}
													    	rs1.close();
												    	}
												    	
											    	}
										    		
										    		
										    		
										    	}
										    	
										    	
										    	
										    	selectSQL1 = "SELECT * FROM `pioneer.item.dispensingunit` WHERE dispensingunittext = ?";
										    	preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setString(1, prescribedUnitText);
										    	rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		unitid = rs1.getInt("dispensingunitid");
											    	}
											    	rs1.close();
										    	}
										    	
										    	selectSQL1 = "SELECT * FROM `pioneer.prescription.sig` WHERE englishtext in (?)";
										    	preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setString(1, dispensedSigCode);
										    	rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		dispensedSigCodeID = rs1.getString("sigid");
											    	}
											    	rs1.close();
										    	}
										    	
										    	selectSQL1 = "SELECT * FROM `pioneer.item.deaoverride` WHERE gcn=? and statecode=?";
										    	preparedStatement1 = conn.prepareStatement(selectSQL1);
										    	preparedStatement1.setString(1, clinicalformulationid+"");
										    	preparedStatement1.setString(2, phy_state);
										    	rs1 = preparedStatement1.executeQuery();
										    	if(rs1!=null){
											    	while (rs1.next()) {
											    		deaschedule = rs1.getString("deaschedule");
											    	}
											    	rs1.close();
										    	}
										    	if(deaschedule==null || !deaschedule.equalsIgnoreCase(""))
										    	{
										    		selectSQL1 = "SELECT * FROM `pioneer.item.deaoverride` WHERE gcn=? or statecode=?";
											    	preparedStatement1 = conn.prepareStatement(selectSQL1);
											    	preparedStatement1.setString(1, clinicalformulationid+"");
											    	preparedStatement1.setString(2, phy_state);
											    	rs1 = preparedStatement1.executeQuery();
											    	if(rs1!=null){
												    	while (rs1.next()) {
												    		deaschedule = rs1.getString("deaschedule");
												    	}
												    	rs1.close();
											    	}
										    	}
										    	//Create New Prescription
										    	String insertTableSQL = "INSERT INTO prescription_master (physician_id  ,patient_id  ,group_id  ,clinic_id  ,clinic_name  ,"
										    			+ "written_by  ,written_by_name  ,prescription_date  ,phy_address  ,phy_city  ,phy_state  ,phy_zip_code  ,phy_country  ,"
										    			+ "phy_phone  ,phy_dea  ,phy_npi  ,phy_upin  ,phy_state_license  ,phy_medicaid  ,"
										    			+ "patient_name  ,patient_address  ,patient_city  ,patient_state  ,patient_zip_code  ,patient_country  ,patient_date_of_birth  ,"
										    			+ "patient_mobile  ,patient_ssn  ,allergies  ,diagnosis  ,patient_bill_to_id  ,clinic_po  ,payment_type  ,billing_address_id  ,"
										    			+ "billing_name  ,billing_address  ,billing_city  ,billing_state  ,billing_zip_code  ,billing_country  ,patient_ship_to_id ,"
										    			+ "shipping_address_id,shipping_name  ,shipping_address  ,shipping_city  ,shipping_state  ,shipping_zip_code  ,shipping_country  ,"
										    			+ "created_by  ,created_user  ,created_date  ,last_updated_by  ,last_updated_user  ,last_updated_date  ,prescription_number,cre8_prescription_no,status_id)"
										    			+" VALUES "
										    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										    	PreparedStatement ipreparedStatement = conn.prepareStatement(insertTableSQL, Statement.RETURN_GENERATED_KEYS);
										    	
										    	ipreparedStatement.setObject(	1	, newphysicianid);
										    	ipreparedStatement.setObject(	2	, newpatientid);
										    	ipreparedStatement.setObject(	3	, newphysiciangroupid);
										    	ipreparedStatement.setObject(	4	, newphysicianclinicid  );
										    	ipreparedStatement.setObject(	5	, newphysicianClinicName  );
										    	ipreparedStatement.setObject(	6	, newphysicianid		  );
										    	ipreparedStatement.setObject(	7	, newphysicianName	  );
										    	ipreparedStatement.setObject(	8	, getFilteredText(getCurrentTimeStamp()));
										    	ipreparedStatement.setObject(	9	, phy_address  );
										    	ipreparedStatement.setObject(	10	, phy_city );
										    	ipreparedStatement.setObject(	11	, phy_state );
										    	ipreparedStatement.setObject(	12	, phy_zip_code  );
										    	ipreparedStatement.setObject(	13	, "USA"		  );
										    	ipreparedStatement.setObject(	14	, phy_phone  );
										    	ipreparedStatement.setObject(	15	, 	phy_dea	  );
										    	ipreparedStatement.setObject(	16	, 	phy_npi	  );
										    	ipreparedStatement.setObject(	17	, 	phy_upin	  );
										    	ipreparedStatement.setObject(	18	, 	phy_state_license	  );
										    	ipreparedStatement.setObject(	19	, 	phy_medicaid	  );
										    	ipreparedStatement.setObject(	20	, 	newpatientName	  );
										    	ipreparedStatement.setObject(	21	, 	shipping_address	  );
										    	ipreparedStatement.setObject(	22	, 	shipping_city	  );
										    	ipreparedStatement.setObject(	23	, 	shipping_state  );
										    	ipreparedStatement.setObject(	24	, 	shipping_zip_code  );
										    	ipreparedStatement.setObject(	25	, 	"USA");
										    	ipreparedStatement.setObject(	26	, 	patient_date_of_birth	  );
										    	ipreparedStatement.setObject(	27	, 	patient_mobile	  );
										    	ipreparedStatement.setObject(	28	, 	patient_ssn	  );
										    	ipreparedStatement.setObject(	29	, 	allergies	  );
										    	ipreparedStatement.setObject(	30	, 	diagnosis	  );
										    	ipreparedStatement.setObject(	31	, 	"1"	  );//Bill Clinic
										    	ipreparedStatement.setObject(	32	, 	"Test PO"	  );//Clinic's PO
										    	ipreparedStatement.setObject(	33	, 	"creditcard"	  );//default creditcard
										    	ipreparedStatement.setObject(	34	, 	"4"	  );//Same as Clinic Location
										    	ipreparedStatement.setObject(	35	, 	newphysicianClinicName  );
										    	ipreparedStatement.setObject(	36	, 	clinicAddress  );
										    	ipreparedStatement.setObject(	37	, 	clinicCity	  );
										    	ipreparedStatement.setObject(	38	, 	clinicState	  );
										    	ipreparedStatement.setObject(	39	, 	clinicZipcode	  );
										    	ipreparedStatement.setObject(	40	, 	"USA"  );
										    	ipreparedStatement.setObject(	41	, 	"2"	  );
										    	ipreparedStatement.setObject(	42	, 	"3"  );
										    	ipreparedStatement.setObject(	43	, 	newpatientName	  );
										    	ipreparedStatement.setObject(	44	, 	shipping_address	  );
										    	ipreparedStatement.setObject(	45	, 	shipping_city	  );
										    	ipreparedStatement.setObject(	46	, 	shipping_state	  );
										    	ipreparedStatement.setObject(	47	, 	shipping_zip_code	  );
										    	ipreparedStatement.setObject(	48	, 	"USA");
										    	ipreparedStatement.setObject(	49	, 		"0"  );
										    	ipreparedStatement.setObject(	50	, 	"RX"	  );
										    	ipreparedStatement.setObject(	51	, 	getFilteredText(getCurrentTimeStamp())	  );
										    	ipreparedStatement.setObject(	52	, 	"0"	  );
										    	ipreparedStatement.setObject(	53	, 	"RX"	  );
										    	ipreparedStatement.setObject(	54	, 	getFilteredText(getCurrentTimeStamp())	  );
										    	ipreparedStatement.setObject(	55	, 	getFilteredText(prescription_number)	  );
										    	ipreparedStatement.setObject(	56	, 	prescription_number_format+getFilteredText(prescription_number));
										    	ipreparedStatement.setObject(	57	, 	6);//6==>Pioneer Rx Generated
										    	
										    	// execute insert SQL stetement
										    	int prescriptionid=0;
										    	
										    	//System.out.println("ipreparedStatement ==========="+ipreparedStatement.);
										    	ipreparedStatement.executeUpdate();
										    	
										    	   ResultSet generatedKeys = ipreparedStatement.getGeneratedKeys();
										           if (generatedKeys.next()) {
										                System.out.println("id is"+generatedKeys.getLong(1));
										                prescriptionid= generatedKeys.getInt(1);
										           } else {
										               throw new SQLException("Creating prescription failed, no generated key obtained.");
										           }
										    	
										    	
										       	String insertTableSQL2 = "INSERT INTO prescription_transaction (prescription_id ,rx_number ,rx_status ,written_date ,"
										       			+ "expire_date ,type ,origin ,medicationid ,medicationdescription ,quantity ,unit_name ,refills ,refills_remaining ,"
										       			+ "refills_filled ,last_filled_date ,dwa ,auto ,prn  ,icd10  ,sig_codes  ,tracking_number  ,delFlag  ,"
										       			+ "days_supply,future_fill,priorty_type,script_type, previous_rx_number,prescriber_order_number, control_substance,"
										       			+ "dispensed_itemid,dispensed_itemname,prescription_no,cre8_prescription_no,itemid,itemname)"
										       			+ " VALUES "
										    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										    	PreparedStatement ipreparedStatement2 = conn.prepareStatement(insertTableSQL2, Statement.RETURN_GENERATED_KEYS);
										    	ipreparedStatement2.setObject(1, prescriptionid);
										    	ipreparedStatement2.setObject(2, getFilteredText(rxNumber));
										    	ipreparedStatement2.setObject(3, getFilteredText(CurrentRxStatusText));
										    	if(DateWrittenUTC!=null && DateWrittenUTC.length()>0)
										    		ipreparedStatement2.setObject(4, getFilteredText(DateWrittenUTC));
										    	else
										    		ipreparedStatement2.setObject(4, getFilteredText(DateFilledUTC));
										    	ipreparedStatement2.setObject(5, getFilteredText(ExpirationDateUTC));
										    	ipreparedStatement2.setObject(6, getFilteredText(itemtypeid));//Retail or Compound
										    	ipreparedStatement2.setObject(7, getFilteredText(OriginTypeID));
										    	ipreparedStatement2.setObject(8, getFilteredText(medicationid));
										    	ipreparedStatement2.setObject(9, getFilteredText(prescribedWrittenName));
										    	ipreparedStatement2.setObject(10, getFilteredText(dispensedQuantity));
										    	ipreparedStatement2.setObject(11, getFilteredText(unitid));
										    	ipreparedStatement2.setObject(12, getFilteredText(NumberOfRefillsAllowed));
										    	ipreparedStatement2.setObject(13, getFilteredText(RefillsRemaining));
										    	ipreparedStatement2.setObject(14, getFilteredText(NumberOfRefillsFilled));
										    	ipreparedStatement2.setObject(15, LastFillDateTime);
										    	ipreparedStatement2.setObject(16, "");
										    	ipreparedStatement2.setObject(17, "");
										    	ipreparedStatement2.setObject(18, "");
										    	ipreparedStatement2.setObject(19, "");
										    	ipreparedStatement2.setObject(20, dispensedSigCodeID);
										    	ipreparedStatement2.setObject(21, getFilteredText(TrackingNumber));
										    	ipreparedStatement2.setObject(22, "N");
										    	ipreparedStatement2.setObject(23,dispensedDaysSupply);
										    	ipreparedStatement2.setObject(24,FutureFill);
										    	ipreparedStatement2.setObject(25,PriorityTypeText);
										    	ipreparedStatement2.setObject(26,ScriptType);
										    	ipreparedStatement2.setObject(27,PreviousRxNumber);
										    	ipreparedStatement2.setObject(28,PrescriberOrderNumber);
										    	if(deaschedule!=null && deaschedule.length()>0)
										    		ipreparedStatement2.setObject(29,"Y");
										    	else
										    		ipreparedStatement2.setObject(29,"N");
										    	ipreparedStatement2.setObject(30,dispensedDrugPioneerItemId);
										    	ipreparedStatement2.setObject(31,dispensedDrugName);
										    	ipreparedStatement2.setObject(32,1);
										    	ipreparedStatement2.setObject(33,prescription_number_format+getFilteredText(prescription_number)+"-"+1);
										    	ipreparedStatement2.setObject(34,itemid);
										    	ipreparedStatement2.setObject(35,itemname);
										    	//System.out.println("ipreparedStatement2 ==========="+ipreparedStatement2.);
										    	ipreparedStatement2.executeUpdate();
										    	
										       // execute insert SQL stetement
									    	   ResultSet generatedKeys2 = ipreparedStatement2.getGeneratedKeys();
									           if (generatedKeys2.next()) {
									                System.out.println("id is"+generatedKeys2.getLong(1));
									                prescription_tran_id= generatedKeys2.getInt(1);
									           } else {
									               throw new SQLException("Creating prescription tran failed, no generated key obtained.");
									           }
										    	
										           
										    	
									   	}else{
								    	
									    	if(rxNumber!=null && rxNumber.length()>0 && prescription_tran_id>0)
								    		{
								    			
									    		String pupdateTableSQL3 = "UPDATE prescription_master SET "
										    			+ "last_updated_by=?,last_updated_user=?,last_updated_date=?,status_id=? "
										    					+ "WHERE prescription_id = ?";
										    			PreparedStatement pupreparedStatement3 = conn.prepareStatement(pupdateTableSQL3);
										    			
												    	pupreparedStatement3.setObject(1, "0");
												    	pupreparedStatement3.setObject(2, "RX");
												    	pupreparedStatement3.setObject(3, getFilteredText(getCurrentTimeStamp()));
												    	pupreparedStatement3.setInt(4, 6);//6==>Pioneer Rx Generated
												    	pupreparedStatement3.setInt(5, prescription_id);
												    	// execute update SQL stetement
												    	pupreparedStatement3 .executeUpdate();
								    			
								    			String updateTableSQL = "UPDATE prescription_transaction SET "
								    					+ "rx_number = ?,"
								    					+ "rx_status = ?,"
								    					+ "refills_remaining=?,"
								    					+ "refills_filled=?,"
								    					+ "last_filled_date=?,"
								    					+ "previous_rx_number=?,"
								    					+ "prescriber_order_number=?,"
								    					+ "tracking_number=?,"
								    					+ "future_fill=?,"
								    					+ "priorty_type=?,"
								    					+ "script_type=? "
								    					+ "dispensed_itemid=?, "
								    					+ "dispensed_itemname=? "
								    					+ "WHERE prescription_tran_id = ?";
								    			PreparedStatement upreparedStatement = conn.prepareStatement(updateTableSQL);
								    			upreparedStatement.setObject(1, getFilteredText(rxNumber));
								    			upreparedStatement.setObject(2, getFilteredText(CurrentRxStatusText));
								    			upreparedStatement.setObject(3, getFilteredText(RefillsRemaining));
								    			upreparedStatement.setObject(4, getFilteredText(NumberOfRefillsFilled));
								    			upreparedStatement.setObject(5, LastFillDateTime);
								    			upreparedStatement.setObject(6, getFilteredText(PreviousRxNumber));
								    			upreparedStatement.setObject(7, getFilteredText(PrescriberOrderNumber));
								    			upreparedStatement.setObject(8, getFilteredText(TrackingNumber));
								    			upreparedStatement.setObject(9, getFilteredText(FutureFill));
								    			upreparedStatement.setObject(10, getFilteredText(PriorityTypeText));
								    			upreparedStatement.setObject(11, getFilteredText(ScriptType));
								    			upreparedStatement.setObject(11, dispensedDrugPioneerItemId);
								    			upreparedStatement.setObject(12, dispensedDrugName);
								    			
								    			upreparedStatement.setInt(13,prescription_tran_id);
								    			// execute update SQL stetement
								    			upreparedStatement .executeUpdate();
								    			
								    		}//if(rxNumber!=null && rxNumber.length()>0 && prescription_tran_id>0)
								    	
								    	}
								    	
								    	
								    	if(rxNumber!=null && rxNumber.length()>0 && prescription_tran_id>0)
								    	{
									    	//Create New Order
									    	
									    	String insertTableSQL = "INSERT INTO order_master"
									    			+ "(order_number, order_date, group_id, physician_id,patient_id,"
									    			+ "shipping_address,shipping_city,shipping_state,shipping_zip_code,"
									    			+ "created_by,created_user,created_date,last_updated_by,last_updated_user,last_updated_date) VALUES"
									    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
									    	PreparedStatement ipreparedStatement = conn.prepareStatement(insertTableSQL, Statement.RETURN_GENERATED_KEYS);
									    	ipreparedStatement.setObject(1, prescription_number_format+getFilteredText(prescription_number)+"-"+1+order_number);
									    	ipreparedStatement.setObject(2, getFilteredText(getCurrentTimeStamp()));
									    	ipreparedStatement.setObject(3, getFilteredText(physicianGroupId));
									    	ipreparedStatement.setObject(4, getFilteredText(physicianid));
									    	ipreparedStatement.setObject(5, getFilteredText(patientid));
									    	ipreparedStatement.setObject(6, getFilteredText(shipping_address));
									    	ipreparedStatement.setObject(7, getFilteredText(shipping_city));
									    	ipreparedStatement.setObject(8, getFilteredText(shipping_state));
									    	ipreparedStatement.setObject(9, getFilteredText(shipping_zip_code));
									    	
									    	ipreparedStatement.setObject(10, "0");
									    	ipreparedStatement.setObject(11, "RX");
									    	ipreparedStatement.setObject(12, getFilteredText(getCurrentTimeStamp()));
									    	
									    	ipreparedStatement.setObject(13, "0");
									    	ipreparedStatement.setObject(14, "RX");
									    	ipreparedStatement.setObject(15, getFilteredText(getCurrentTimeStamp()));
									    	
									    	// execute insert SQL stetement
									    	int orderid=0;
									    	ipreparedStatement .executeUpdate();
									    	
									    	   ResultSet generatedKeys = ipreparedStatement.getGeneratedKeys();
									           if (generatedKeys.next()) {
									                System.out.println("id is"+generatedKeys.getLong(1));
									                orderid= generatedKeys.getInt(1);
									           } else {
									               throw new SQLException("Creating order failed, no generated key obtained.");
									           }
									           
									    	
									    	String insertTableSQL2 = "INSERT INTO order_transaction "
									    			+ "(order_id,rx_number,rx_status,prescribed_name,prescribed_drug,"
									    			+ "prescribed_quantity,prescribed_unit,dispensed_name,dispensed_drug,dispensed_quantity,"
									    			+ "dispensed_unit,days_supply,quantity_remaining,refills_allowed,refills_filled,"
									    			+ "refills_remaining,last_filled_date,tracking_number,prescription_tran_id,"
									    			+ "priority_type,lot_number,lot_expiration_date,rx_comments,completed_date,scriptType,refillNumber) VALUES "
									    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
									    	PreparedStatement ipreparedStatement2 = conn.prepareStatement(insertTableSQL2);
									    	ipreparedStatement2.setObject(1, orderid);
									    	ipreparedStatement2.setObject(2, getFilteredText(rxNumber));
									    	ipreparedStatement2.setObject(3, getFilteredText(CurrentRxTransactionStatusText));
									    	ipreparedStatement2.setObject(4, getFilteredText(prescribedWrittenName));;
									    	ipreparedStatement2.setObject(5, getFilteredText(prescribedDrugStrength));
									    	ipreparedStatement2.setObject(6, getFilteredText(prescribedQuantity));
									    	ipreparedStatement2.setObject(7, getFilteredText(prescribedUnitText));
									    	ipreparedStatement2.setObject(8, getFilteredText(dispensedDrugName));
									    	ipreparedStatement2.setObject(9, getFilteredText(dispensedDrugStrength));
									    	ipreparedStatement2.setObject(10, getFilteredText(dispensedQuantity));
									    	ipreparedStatement2.setObject(11, getFilteredText(dispensedUnitText));
									    	ipreparedStatement2.setObject(12, getFilteredText(dispensedDaysSupply));
									    	ipreparedStatement2.setObject(13, getFilteredText(QuantityRemaining));
									    	ipreparedStatement2.setObject(14, getFilteredText(NumberOfRefillsAllowed));
									    	ipreparedStatement2.setObject(15, getFilteredText(NumberOfRefillsFilled));
									    	ipreparedStatement2.setObject(16, getFilteredText(RefillsRemaining));
									    	ipreparedStatement2.setObject(17, LastFillDateTime);
									    	ipreparedStatement2.setObject(18, getFilteredText(TrackingNumber));
									    	ipreparedStatement2.setObject(19, getFilteredText(prescription_tran_id));
									    	ipreparedStatement2.setObject(20, getFilteredText(PriorityTypeText));
									    	ipreparedStatement2.setObject(21, getFilteredText(dispensedLotNumber));
									    	ipreparedStatement2.setObject(22, dispensedLotExpirationDate);
									    	ipreparedStatement2.setObject(23, getFilteredText(RxCommentsCritical));
									    	ipreparedStatement2.setObject(24, CompletedDate);
									    	ipreparedStatement2.setObject(25,ScriptType);
									    	ipreparedStatement2.setObject(26,RefillNumber);
									    	
									    	// execute insert SQL stetement
									    	int ordertranid=ipreparedStatement2 .executeUpdate();
									   	
									    	//Create New Invoice
									    	
									    	String insertInvTableSQL = "INSERT INTO invoice_master"
									    			+ "(invoice_number,invoice_date,rx_number,written_date,patient_id,physician_id,"
									    			+ "billing_name,billing_address,billing_city,billing_state,billing_zipcode,"
									    			+ "shipping_name,shipping_address,shipping_city,shipping_state,shipping_zipcode,"
									    			+ "subtotal,tax,total,created_by,created_user,created_date,last_updated_by,last_updated_user,last_updated_date,"
									    			+ "percentageSalesTaxAmountPaid,percentageSalesTaxAmountSubmitted,percentageSalesTaxRatePaid,"
									    			+ "percentageSalesTaxRateSubmitted,flatSalesTaxAmountPaid,flatSalesTaxAmountSubmitted) VALUES"
									    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
									    	PreparedStatement invpreparedStatement = conn.prepareStatement(insertInvTableSQL, Statement.RETURN_GENERATED_KEYS);
									    	invpreparedStatement.setObject(1, invoice_number);
									    	invpreparedStatement.setObject(2, getFilteredText(getCurrentTimeStamp()));
									    	invpreparedStatement.setObject(3, rxNumber);
									    	if(TransmissionDate!=null && TransmissionDate.length()>0)
									    		invpreparedStatement.setObject(4, TransmissionDate);
									    	else
									    		invpreparedStatement.setObject(4,getFilteredText(getCurrentTimeStamp()) );
									    	invpreparedStatement.setObject(5, getFilteredText(patientid));
									    	invpreparedStatement.setObject(6, getFilteredText(physicianid));
									    	
									    	invpreparedStatement.setObject(7, getFilteredText(patientName));
									    	invpreparedStatement.setObject(8, getFilteredText(shipping_address));
									    	invpreparedStatement.setObject(9, getFilteredText(shipping_city));
									    	invpreparedStatement.setObject(10, getFilteredText(shipping_state));
									    	invpreparedStatement.setObject(11, getFilteredText(shipping_zip_code));
									    	
									    	invpreparedStatement.setObject(12, getFilteredText(patientName));
									    	invpreparedStatement.setObject(13, getFilteredText(shipping_address));
									    	invpreparedStatement.setObject(14, getFilteredText(shipping_city));
									    	invpreparedStatement.setObject(15, getFilteredText(shipping_state));
									    	invpreparedStatement.setObject(16, getFilteredText(shipping_zip_code));
									    	
									    	invpreparedStatement.setObject(17, getFilteredText(GrossAmountSubmitted));
									    	invpreparedStatement.setObject(18, new Double(PercentageSalesTaxAmountSubmitted)+new Double(FlatSalesTaxAmountSubmitted));
									    	invpreparedStatement.setObject(19, getFilteredText(PatientPayAmountPaid));
									    	
									    	invpreparedStatement.setObject(20, "0");
									    	invpreparedStatement.setObject(21, "RX");
									    	invpreparedStatement.setObject(22, getFilteredText(getCurrentTimeStamp()));
									    	
									    	invpreparedStatement.setObject(23, "0");
									    	invpreparedStatement.setObject(24, "RX");
									    	invpreparedStatement.setObject(25, getFilteredText(getCurrentTimeStamp()));
									    	
									    	invpreparedStatement.setObject(26, getFilteredText(PercentageSalesTaxAmountPaid));
									    	invpreparedStatement.setObject(27, getFilteredText(PercentageSalesTaxAmountSubmitted));
									    	invpreparedStatement.setObject(28, getFilteredText(PercentageSalesTaxRatePaid));
									    	
									    	invpreparedStatement.setObject(29, getFilteredText(PercentageSalesTaxRateSubmitted));
									    	invpreparedStatement.setObject(30, getFilteredText(FlatSalesTaxAmountPaid));
									    	invpreparedStatement.setObject(31, getFilteredText(FlatSalesTaxAmountSubmitted));
									    	
									    	
									    	// execute insert SQL stetement
									    	int invoiceid=0;
									    			invpreparedStatement .executeUpdate();
									    	
									    	
									    	  ResultSet generatedKeys2 = invpreparedStatement.getGeneratedKeys();
									           if (generatedKeys2.next()) {
									                System.out.println("id is"+generatedKeys2.getLong(1));
									                invoiceid= generatedKeys2.getInt(1);
									           } else {
									               throw new SQLException("Creating invoice failed, no generated key obtained.");
									           }
									           
									           
									    	String insertInvTableSQL2 = "INSERT INTO invoice_transaction "
									    			+ "(invoice_id,item,quantity,total,lot_number,expiration_date,prescription_tran_id,rx_number,days_supply,numberOfRefillsFilled,paymentstatus,scriptType,refillNumber) VALUES "
									    			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
									    	PreparedStatement invpreparedStatement2 = conn.prepareStatement(insertInvTableSQL2);
									    	invpreparedStatement2.setObject(1, invoiceid);
									    	invpreparedStatement2.setObject(2, getFilteredText(dispensedDrugName));
									    	invpreparedStatement2.setObject(3, getFilteredText(dispensedQuantity));
									    	invpreparedStatement2.setObject(4, getFilteredText(GrossAmountSubmitted));;
									    	invpreparedStatement2.setObject(5, getFilteredText(dispensedLotNumber));
									    	invpreparedStatement2.setObject(6, getFilteredText(dispensedLotExpirationDate));
									    	invpreparedStatement2.setObject(7, getFilteredText(prescription_tran_id));
									    	invpreparedStatement2.setObject(8, getFilteredText(rxNumber));
									    	invpreparedStatement2.setObject(9, getFilteredText(dispensedDaysSupply));
									    	invpreparedStatement2.setObject(10, getFilteredText(NumberOfRefillsFilled));
									    	invpreparedStatement2.setObject(11, getFilteredText("Not Paid"));
									    	invpreparedStatement2.setObject(12,ScriptType);
									    	invpreparedStatement2.setObject(13,RefillNumber);
									    	// execute insert SQL stetement
									    	int invoicetranid=invpreparedStatement2 .executeUpdate();
								    
								    	}//if(rxNumber!=null && rxNumber.length()>0 && prescription_tran_id>0)
								    	
						    	}//if(!rxExists)
					    	
					   
					    	
				   
		    		
		    	}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally {

		
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}
	
	
	public static String getFilteredText(Object text)
	{
		String retText="",text1="";
		if(text!=null )
		{
			text1=text+"";
			if(text1.length()>0 && !text1.equals("null"))
				retText=text1;
			retText=retText.trim();
		}
		return retText;
	}
	
	private static java.sql.Timestamp getCurrentTimeStamp() {

		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());

	}
	 private static void pushPioneerEventData(JSONArray nameArray, JSONArray valArray,String tableName) {
		  try {
			Connection conn = getDestinationDBConnection();
			  StringBuffer sb = new StringBuffer("insert into `" + tableName + "`(");
			  int size = nameArray.size();
			  int count = 0;
			  Iterator<Object> iterator = nameArray.iterator();
			  
			  while (iterator.hasNext()) {
			   if (count < (size - 1))
			    sb.append("`"+iterator.next() + "`,");
			   else
			    sb.append("`"+iterator.next() + "`)");
			   count++;
			  }
			  sb.append(" values(");
			 
			  for (int i = 0; i < size; i++) {  
			   if (i < (size - 1))
			    sb.append("?,");
			   else
			    sb.append("?)");  
			  }
			  System.out.println(sb.toString());
			  PreparedStatement pstmt=null;
			  try {
			   pstmt = conn.prepareStatement(sb.toString());
			   bindVariables(valArray, pstmt);
			   pstmt.executeUpdate();
			  } catch (SQLException e) {
				  System.out.println("SQL Exception 11====="+e.getMessage());
				  System.out.println("SQL Exception 22====="+ e.getErrorCode());
				  //System.out.println("SQL Exception 22====="+ e.getSQLState());
				 // System.out.println("SQL Exception 22====="+ e.getLocalizedMessage());
				  
				  //If the exception is of new Unknown column in the field list, add the new column to the existing table and repush the data
				  //e.g.,Unknown column 'CommentsLatestMTMAction' in 'field list'
				  if(e.getErrorCode()==1054)
				  {
					  String errMessage=e.getMessage();
					  errMessage=errMessage.replaceAll("Unknown column '", "");
					  errMessage=errMessage.replaceAll("' in 'field list'", "");
					  
					  
					// Adding a new column to an existing table
				      Statement sta = conn.createStatement(); 
				      int ncount = sta.executeUpdate(
				        "ALTER TABLE `" + tableName + "` ADD `"+errMessage+"` varchar(100) NOT NULL DEFAULT ''");
				      System.out.println("A new column added -->"+errMessage+" added in -->"+tableName);
				      sta.close();
					  
				      pushPioneerEventData(nameArray, valArray, tableName);
				  }
				 
			   e.printStackTrace();
			  }finally {

					if (pstmt != null) {
						pstmt.close();
					}

					if (conn != null) {
						conn.close();
					}

				}
		} catch (Exception e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 }
		 private static void bindVariables(JSONArray valArray,
		   PreparedStatement pstmt) throws SQLException {
		  Iterator<Object> iterator = valArray.iterator();
		  int cnt = 0;
		  while (iterator.hasNext()) {
		   Object obj = iterator.next();
		   if (obj instanceof String) {
		    pstmt.setString(++cnt, (String) obj);
		   } else if (obj instanceof Integer) {
		    pstmt.setLong(++cnt, (Integer) obj);
		   } else if (obj instanceof Long) {
		    pstmt.setLong(++cnt, (Long) obj);
		   } else if (obj instanceof Double) {
		    pstmt.setDouble(++cnt, (Double) obj);
		   }
		  }
		 }
	
	
		 private static Connection getSourceDBConnection1() {

				Connection dbConnection = null;

				try {

					Class.forName(DB_DRIVER);

				} catch (ClassNotFoundException e) {

					System.out.println(e.getMessage());

				}

				try {

					dbConnection = DriverManager.getConnection(
						DB_CONNECTION1, DB_USER,DB_PASSWORD);
					return dbConnection;

				} catch (SQLException e) {

					System.out.println(e.getMessage());

				}

				return dbConnection;

			}
	private static Connection getDestinationDBConnection() {

		Connection dbConnection = null;

		try {

			Class.forName(MYSQL_DB_DRIVER);

		} catch (ClassNotFoundException e) {

			System.out.println(e.getMessage());

		}

		try {

			dbConnection = DriverManager.getConnection(
					MYSQL_DB_CONNECTION, MYSQL_DB_USER,MYSQL_DB_PASSWORD);
			return dbConnection;

		} catch (SQLException e) {

			System.out.println(e.getMessage());

		}

		return dbConnection;

	}
	
}