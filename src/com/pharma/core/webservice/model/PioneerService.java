package com.pharma.core.webservice.model;
 
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.pharma.core.util.PropertyConfigurer;
 
@Path("/pioneer")
public class PioneerService {

    @Resource
    WebServiceContext wsContext;
	
	@POST
	@Path("/PatientSave")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response patientSave(InputStream incomingData) {
		StringBuilder strBuilder = new StringBuilder();
		JSONObject returnJson=new JSONObject();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				strBuilder.append(line);
			}
		
		System.out.println("Data Received: " + strBuilder.toString());
		
		JSONParser parser = new JSONParser(); 
		JSONObject json = (JSONObject) parser.parse(strBuilder.toString());
		
		System.out.println("id =========="+json.get("status"));
		returnJson=PharmacyStoredProcedure.callPatientStoredProcedure(json);
		} catch (Exception e) {
			System.out.println("Error Parsing: - "+e);
			e.printStackTrace();
		}
		// return HTTP response 200 in case of success
		return Response.status(200).entity(returnJson.toString()).build();
	}
	@POST
	@Path("/PrescriberSave")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response prescriberSave(InputStream incomingData) {
		StringBuilder strBuilder = new StringBuilder();
		JSONObject returnJson=new JSONObject();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				strBuilder.append(line);
			}
		
		System.out.println("Data Received: " + strBuilder.toString());
		
		JSONParser parser = new JSONParser(); 
		JSONObject json = (JSONObject) parser.parse(strBuilder.toString());
		
		System.out.println("id =========="+json.get("status"));
		returnJson=PharmacyStoredProcedure.callPrescriberStoredProcedure(json);
		} catch (Exception e) {
			System.out.println("Error Parsing: - "+e);
			e.printStackTrace();
		}
		// return HTTP response 200 in case of success
		return Response.status(200).entity(returnJson.toString()).build();
	}

	@POST
	@Path("/SchedulePioneerPharmacyTables")
	@Consumes(MediaType.APPLICATION_JSON)
    public Response SchedulePioneerPharmacyTables(InputStream incomingData) {
		JSONObject returnJson=new JSONObject();    
        String str = "PioneerPharmacyTablesPushSuccess";
        
        try {
        	returnJson.put("result", str);
			PharmacyTableScheduler.FetchPioneerPharmacyTables();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
        return Response.status(200).entity(returnJson.toString()).build();
 
    }
	

	@POST
	@Path("/SchedulePioneerChangedInfo")
	@Consumes(MediaType.APPLICATION_JSON) 
    public Response SchedulePioneerPhysicianPatientChangedInfo(InputStream incomingData) {
		JSONObject returnJson=new JSONObject();    
        String str = "PioneerPhysicianPatientChangedInfoSuccess";
        
        try {
        	returnJson.put("result", str);
        	PioneerChangedInfoUpdater.fetchPhysicianAndPatientInfo(false,false,"","",0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
        return Response.status(200).entity(returnJson.toString()).build();
 
    }
	
	@POST
	@Path("/SchedulePointOfSale")
	@Consumes(MediaType.APPLICATION_JSON)
    public Response SchedulePointOfSale(InputStream incomingData) {
		JSONObject returnJson=new JSONObject();    
        String str = "PointOfSaleSuccess";
        try {
        	returnJson.put("result", str);
        	PharmacyPOSScheduler.fetchPOSShipmentTrackingInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return Response.status(200).entity(returnJson.toString()).build();
 
    }
	
	@POST
	@Path("/SchedulePioneerRX")
	@Consumes(MediaType.APPLICATION_JSON)
    public Response SchedulePharmacyRX(InputStream incomingData) {
		JSONObject returnJson=new JSONObject();    
        String str = "PioneerRXSuccess";
        try {
        	returnJson.put("result", str);
        	try {
        		 
    			// Step1: Let's 1st read file from fileSystem
    			// Change CrunchifyJSON.txt path here
    			JSONObject jsonObject = new JSONObject();
    			jsonObject.put("FirstName", "John");
    			jsonObject.put("LastName", "Doe");
    			jsonObject.put("MiddleName", "David");
    			jsonObject.put("Suffix", "III");
    			jsonObject.put("Prefix", "Mr.");
    			System.out.println(jsonObject);
     
    			// Step2: Now pass JSON File Data to REST Service
    			try {
                    MessageContext mc = wsContext.getMessageContext();
                    HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST);
                    System.out.println("Client IP = " + req.getRemoteAddr());
                    URL url = new URL("http://"+ req.getRemoteAddr()+"/CRE8PortalWebService/pharmacy/");
    				System.out.println("calling SchedulePioneerRX :: http://"+ req.getRemoteAddr()+"/CRE8PortalWebService/pharmacy/");
    				URLConnection connection = url.openConnection();
    				connection.setDoOutput(true);
    				connection.setRequestProperty("Content-Type", "application/json");
    				/*connection.setConnectTimeout(5000);
    				connection.setReadTimeout(5000);*/
    				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
    				out.write(jsonObject.toString());
    				out.close();
    				System.out.println("jsonObject  input String ======="+jsonObject.toString());
    				
    				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    				String output = "";
    				while ((output=in.readLine()) != null) {
    					System.out.println("final output ========="+output);
    				}
    				in.close();
     
    			} catch (Exception e) {
    				e.printStackTrace();
    				System.out.println("\nError while calling SchedulePioneerRX REST Service");
    				System.out.println(e);
    			}
     
    		
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return Response.status(200).entity(returnJson.toString()).build();
 
    }
 

}