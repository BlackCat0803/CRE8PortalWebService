package com.pharma.core.webservice.model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.pharma.core.util.PropertyConfigurer;
import com.pharma.core.util.Util;

public class PharmacyStoredProcedure {

	 private static final String DB_DRIVER = PropertyConfigurer.getPropertyKeyValue("DB_DRIVER");
	 private static final String DB_CONNECTION = PropertyConfigurer.getPropertyKeyValue("DB_CONNECTION1");
	 private static final String DB_USER = PropertyConfigurer.getPropertyKeyValue("DB_USER");
	 private static final String DB_PASSWORD = PropertyConfigurer.getPropertyKeyValue("DB_PASSWORD");
	 

	 /**
	  * Create Procedure callPrescriberStoredProcedure
	  * Create Web Service Client Call in Pharmacy  Webservice Portal
		Invoke Webservice Client Call from Pharmacy  Web Portal on Save/Update
		Pass the Physician Info as Parameter to  Webservice Client Call
		On Calling Webservice Client Call generate NEWID() as Unique Identifier for new Record and Store this ID in the Web Portal as Reference Key
		Set the Prescriber Type ID (Unique Identifier) from PrescriberType table
		Update the Webservice Call Flag Value from the  Webservice Client Call 
		
		Stored Procedure Web Service Client Call from CRE8 Portal to Pioneer System when Approved Prescriber in created and returns the Pioneer UID
		If Return Value>0 record is inserted/updated successfully , Store this NEWID() in the Web Portal as Reference Key
		If Return Value other than 1, record updation is failure


	  * @param json
	  * @return Prescriber Pioneer UID
	  * @throws SQLException
	  */
	public static JSONObject callPrescriberStoredProcedure(JSONObject json) throws SQLException {

		Connection dbConnection = null;
		CallableStatement callableStatement = null;
		String newID="";
		
		//23 Parameters
		String storedProdcedureSql = "{call API.PrescriberSave(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
		int result= -1;
		try {
			
			if(json!=null){
				if(json.get("pioneerUid")!=null && json.get("pioneerUid").toString().length()>0)
				{
					newID=json.get("pioneerUid").toString();
				}else
					newID=UUID.randomUUID().toString();
					//newID=generateNEWID(dbConnection);
				//	newID="69E65837-2541-460F-8BC8-B7E063AB123C";	
				dbConnection = getDBConnection();
				
				if(dbConnection!=null){
					System.out.println("********** inside calling callPrescriberStoredProcedure ***************");
					callableStatement = dbConnection.prepareCall(storedProdcedureSql);
					callableStatement.setObject("PersonID",newID);
					
					callableStatement.setObject("EmailAddress", json.get("email"));
					callableStatement.setObject("FirstName",json.get("firstName"));
					callableStatement.setObject("MiddleName",json.get("middleName"));
					callableStatement.setObject("LastName",json.get("lastName"));
					callableStatement.setObject("Suffix","");
					
					callableStatement.setObject("DEA",json.get("dea"));
					callableStatement.setObject("NPI",json.get("npi"));
					callableStatement.setObject("StateLicense",json.get("stateLicense"));
					callableStatement.setObject("MedcaidLicenseNumber",json.get("medicaid"));
					
					callableStatement.setObject("Address",json.get("address1"));
					callableStatement.setObject("City",json.get("city"));
					callableStatement.setObject("ZipCode",json.get("zipCode"));
					callableStatement.setObject("StateCode",json.get("state"));
					callableStatement.setObject("PrimaryPhoneNumber",json.get("phone"));
					callableStatement.setObject("PrimaryFaxNumber",json.get("fax"));
					callableStatement.setObject("Version",2);
					
					callableStatement.setObject("ExternalID","");
					callableStatement.setObject("AlternateID","");
					callableStatement.setObject("UPIN",json.get("upin"));
					callableStatement.setObject("PrescriberTypeID",json.get("pioneerPrescriberTypeId"));//M.D.
					//callableStatement.setObject("PrescriberTypeID","D139410D-1DF9-4F25-AFFF-F6F35F4E4C30");//M.D.
					callableStatement.setObject("GroupCode",json.get("prescriberGroup"));
					callableStatement.setObject("MarketerID",null);
					
					
			
					// execute storedProdcedureSql store procedure
					callableStatement.executeUpdate();
					
					if (callableStatement != null) {
						if(!callableStatement.isClosed())
							callableStatement.close();
					}
					
					//Result=0==>Inserted Successfully
					//Result=1==>Updated Successfully
					//Result=-1==>Record not affected
					
					try {
						Statement sta =dbConnection.createStatement();
						
						String Sql =  "select * from Person.Person as prn where prn.PersonID='"+newID+"'";
						System.out.println("SQL Physician ========"+Sql);
						ResultSet rs = sta.executeQuery(Sql);
	
						while(rs.next())
						{
							result=0;
						}
						
						if (rs != null) {
							rs.close();
						}
						
						
						if (sta != null) {
							sta.close();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("result ========="+result+":: newID ::"+newID);
				
				//Result=0==>Inserted Successfully
				//Result=1==>Updated Successfully
				//Result=-1==>Record not affected
				json.put("webServiceNewID", newID);
				json.put("webServiceCallResult", result);
				json.put("webServiceTableName", "Physician");
			
			}
			

		} catch (Exception e) {

			System.out.println(e.getMessage());
			e.printStackTrace();

		} finally {

			if (callableStatement != null) {
				if(!callableStatement.isClosed())
					callableStatement.close();
			}

			if (dbConnection != null) {
				dbConnection.close();
			}

		}
		return json;
	}

	
	/**
	 * Create Procedure callPatientStoredProcedure
	 * Create Web Service Client Call in Pharmacy  Webservice Portal
	   Invoke Webservice Client Call from Pharmacy  Web Portal on Save/Update
	   Pass the Patient Info as Parameter to  Webservice Client Call
	   On Calling Webservice Client Call generate NEWID() as Unique Identifier for new Record and Store this ID in the Web Portal as Reference Key
	   Update the Webservice Call Flag Value from the  Webservice Client Call 
	   
	   Stored Procedure Web Service Client Call from CRE8 Portal to Pioneer System when Active Patient in created and returns the Pioneer UID
	   If Return Value>0 record is inserted/updated successfully , Store this NEWID() in the Web Portal as Reference Key
	   If Return Value other than 1, record updation is failure
		
	 * @param json
	 * @return Patient Pioneer UID
	 * @throws SQLException
	 */
	public static JSONObject callPatientStoredProcedure(JSONObject json) throws SQLException {

		Connection dbConnection = null;
		CallableStatement callableStatement = null;
		String newID="";
		
		//29 Parameters
		//String storedProdcedureSql = "{call API.PatientSave(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
		//String storedProdcedureSql = "{call API.PatientSave(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
		String storedProdcedureSql = "{call API.PatientSave(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
		int result= -1;
		try {
			if(json!=null){
				if(json.get("pioneerUid")!=null && json.get("pioneerUid").toString().length()>0)
				{
					newID=json.get("pioneerUid").toString();
				}else
					newID=UUID.randomUUID().toString();
					//newID=generateNEWID(dbConnection);
				//newID="63874563-359D-4CC7-BE58-3CF876D7D492";
				
				dbConnection = getDBConnection();
				if(dbConnection!=null){
					System.out.println("********** inside calling callPatientStoredProcedure ***************");
					callableStatement = dbConnection.prepareCall(storedProdcedureSql);
					
					callableStatement.setObject("PersonID",newID);
					callableStatement.setObject("EmailAddress", json.get("email"));
					callableStatement.setObject("FirstName",json.get("firstName"));
					callableStatement.setObject("MiddleName",json.get("middleName"));
					callableStatement.setObject("LastName",json.get("lastName"));
					callableStatement.setObject("Suffix","");
					callableStatement.setObject("Gender",json.get("gender"));
					if(json.get("dateOfBirth")!=null)
						callableStatement.setObject("DateOfBirth",Util.getSqlTimeStampFromString2(json.get("dateOfBirth").toString()));
					else
						callableStatement.setObject("DateOfBirth",null);
					callableStatement.setObject("UnencryptedSSN",json.get("ssn"));
					callableStatement.setObject("Address",json.get("address"));
					callableStatement.setObject("City",json.get("city"));	
					callableStatement.setObject("ZipCode",json.get("zipCode"));
					callableStatement.setObject("StateCode",json.get("state"));
					callableStatement.setObject("PrimaryPhoneNumber",json.get("phone"));//Phone
					callableStatement.setObject("PrimaryFaxNumber",json.get("phone"));
					callableStatement.setObject("Version",2);
					
					/*callableStatement.setString("ExternalID","");
					callableStatement.setObject("RaceTypeID",0);
					callableStatement.setObject("AnimalOwnerID","");//UUID
					callableStatement.setObject("DriversLicenseNumber",json.get("driversLicense").toString());
					callableStatement.setObject("DriversLicenseStateCode",json.get("driversLicenseState").toString());
					callableStatement.setObject("DriversLicenseExpirationDate",Util.getSqlTimeStampFromString2(json.get("licenseExpDate").toString()));
					
					
					callableStatement.setObject("AlternateID",0);
					callableStatement.setObject("AlternateIDTypeID","");
					callableStatement.setObject("IdentificationExpirationDate",null);
					callableStatement.setObject("LanguageTypeID",0);
					callableStatement.setObject("HeightInches",0);
					callableStatement.setObject("WeightOzs",0);
					callableStatement.setObject("EasyOpen",0);*/
					
					
		
					// execute storedProdcedureSql store procedure
					callableStatement.executeUpdate();
					
					if (callableStatement != null) {
						if(!callableStatement.isClosed())
							callableStatement.close();
					}
					
					//Result=0==>Inserted Successfully
					//Result=1==>Updated Successfully
					//Result=-1==>Record not affected
					
					try {
						Statement sta =dbConnection.createStatement();
						
						String Sql =  "select * from Person.Person as prn where prn.PersonID='"+newID+"'";
						System.out.println("SQl Patient  ======"+Sql);
						ResultSet rs = sta.executeQuery(Sql);
	
						while(rs.next())
						{
							result=0;
						}
						
						if (rs != null) {
							rs.close();
						}
						
						
						if (sta != null) {
							sta.close();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("result ========="+result+":: newID ::"+newID);
				json.put("webServiceNewID", newID);
				json.put("webServiceCallResult", result);
				json.put("webServiceTableName", "Patient");
	
			
			}
		} catch (Exception e) {

			System.out.println(e.getMessage());
			e.printStackTrace();

		} finally {

			if (callableStatement != null) {
				if(!callableStatement.isClosed())
					callableStatement.close();
			}

			if (dbConnection != null) {
				dbConnection.close();
			}

		}
		return json;
	}

	private static Connection getDBConnection() {

		Connection dbConnection = null;

		try {

			Class.forName(DB_DRIVER);

		} catch (ClassNotFoundException e) {

			System.out.println(e.getMessage());

		}

		try {

			dbConnection = DriverManager.getConnection(
				DB_CONNECTION, DB_USER,DB_PASSWORD);
			return dbConnection;

		} catch (SQLException e) {

			System.out.println(e.getMessage());

		}

		return dbConnection;

	}
	
	/**
	 * generate GUID
	 * @throws SQLException
	 */
	public static String generateNEWID(Connection dbConnection) throws SQLException {

		Statement statement = null;
		ResultSet rst=null;
		String newID="";
		
		String sql = "SELECT NEWID()";

		try {
			
			
			dbConnection = getDBConnection();
			statement=dbConnection.createStatement();
			rst=statement.executeQuery(sql);
			
			if(rst!=null)
			{
				while(rst.next())
				{
					
					newID=rst.getString(1);
				}
				
			}
			
			System.out.println("newID in webservice calll ======"+newID);

		} catch (SQLException e) {

			System.out.println(e.getMessage());
			e.printStackTrace();

		} finally {

			if(rst!=null)
			{
				rst.close();
			}
			
			if (statement != null) {
				statement.close();
			}

			

		}
		return newID;
	}


	public static void fetchPioneerPharmaData() throws SQLException {

		Connection dbConnection = null;
		Statement sta =null;
		String newID="";
		
		
		try {
			dbConnection = getDBConnection();
			sta = dbConnection.createStatement();
			String Sql = "select * from Prescription.PrescribedItemType";
			ResultSet rs = sta.executeQuery(Sql);
			while (rs.next()) {
				System.out.println(rs.getString("PrescribedItemTypeText"));
			}
			
			
		} catch (SQLException e) {

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
		
	}
	public static void main(String[] argv) {

		try {
			//fetchPioneerPharmaData();
			//callPatientStoredProcedure();
			//callPhysicianStoredProcedure();

		} catch (Exception e) {

			System.out.println(e.getMessage());

		}

	}
}
