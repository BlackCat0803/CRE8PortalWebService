package com.pharma.core.webservice.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.pharma.core.converters.JPACryptoConverter;
import com.pharma.core.util.PropertyConfigurer;

/**
 * 
 * The class <<PioneerChangedInfoUpdater>> is a Scheduler class for Tracking info such as Patient, Prescriber, PersonPhone, PersonAddress 
 * of Patient/Prescriber is changed in Pioneer on previous day and updates the changes in the CRE8Portal-phy_info,phy_profile, patient_profile,
 *
 */
public class PioneerChangedInfoUpdater {

	 private static final String DB_DRIVER = PropertyConfigurer.getPropertyKeyValue("DB_DRIVER");
	 private static final String DB_CONNECTION1 = PropertyConfigurer.getPropertyKeyValue("DB_CONNECTION1");
	 private static final String DB_CONNECTION2 = PropertyConfigurer.getPropertyKeyValue("DB_CONNECTION2");
	 private static final String DB_USER = PropertyConfigurer.getPropertyKeyValue("DB_USER");
	 private static final String DB_PASSWORD = PropertyConfigurer.getPropertyKeyValue("DB_PASSWORD");
	 
	 
	 private static final String MYSQL_DB_DRIVER = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_DRIVER");
	 private static final String MYSQL_DB_CONNECTION = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_CONNECTION");
	 private static final String MYSQL_DB_USER = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_USER");
	 private static final String MYSQL_DB_PASSWORD = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_PASSWORD");
	 
	
	 /**
	  * fetchPhysicianAndPatientInfo==>If a Patient changed, check for the record exists in the Pharmacy Portal, if so insert the new record otherwise update the existing record with the changes
	  * @param isRXPatientNewRecord==>Insert Patient New Record from RX, when no record exists in Pharmacy Portal otherewise update the existing record
	  * @param isRXPrescriberNewRecord==>Insert Prescriber New Record from RX, when no record exists in Pharmacy Portal otherewise update the existing record
	  * @param patientPioneerRXUID==>RX Event Patient PioneerRXUID
	  * @param prescriberPioneeRXUID==>RX Event Physician PioneerRXUID
	  * @param pharmacyRXPhysicianId==>RX Event Physician PioneerRXUID matched with the Physician
	  * @return record id for the RX Trigger Events
	  * Watching for Changes in PioneerRx-Dec 6, 2017
		 
		For you to check to see if a patient changed, you will need to look at
 
		These are the import tables for patient
		Person.Patient.ChangedOnUTC
		Person.Person.ChangedOnUTC
		Person.PersonPhone.ChangedOnUTC
		Person.PersonAddress.ChangedOnUTC
		Person.PatientAllergy.ChangedOnUTC
		Person.PatientCondition.ChangedOnUTC
		Person.PatientOtherMedication.ChangedOnUTC
		 
		 
		These are the import tables for prescriber
		Person.Prescriber.ChangedOnUTC
		Person.Person.ChangedOnUTC
		Person.PersonPhone.ChangedOnUTC
		Person.PersonAddress.ChangedOnUTC
		 
		Mitch
	  * @throws SQLException
	  */
	 public static int fetchPhysicianAndPatientInfo(boolean isRXPatientNewRecord,boolean isRXPrescriberNewRecord,
			 String patientPioneerRXUID,String prescriberPioneeRXUID,int pharmacyRXPhysicianId) throws Exception {
	
			System.out.println(new Timestamp(System.currentTimeMillis()));
			int returnId=0;
			Connection conn = getDestinationDBConnection();
			Connection dbConnection = null;
			Statement sta =null;
			Statement sta6 =null;
			
			
			try {
				
				dbConnection = getSourceDBConnection1();
				
				if(dbConnection!=null){
					sta = dbConnection.createStatement();
					sta6 = dbConnection.createStatement();
					
					String previousDate=getPreviousDateByDays(1);
					previousDate="2018-01-01";
							 
					String Sql ="";	 
						
					if(isRXPatientNewRecord)
					{
						if(patientPioneerRXUID!=null && patientPioneerRXUID.length()>0){
							//Insert Patient New Record from RX, when no record exists in Pharmacy Portal
							Sql =  "select prn.PersonID,prn.FirstName,prn.MiddleName,prn.LastName,prn.Gender,prn.DateOfBirth,prn.DriversLicenseNumber,prn.DriversLicenseStateCode,"
									+ "prn.EmailAddress,prn.DriversLicenseExpirationDate,prn.SerialNumberPerson,prn.SSN,prn.SSNLastFour,stp.StatusTypePatientText "
									+ "from Person.Patient as pt,Person.StatusTypePatient as stp,Person.Person as prn where pt.PersonID=prn.PersonID and pt.StatusTypePatientID=stp.StatusTypePatientID "
									+ "and prn.PersonID='"+patientPioneerRXUID+"'";
						}
						
					}else
					{
						//Checks for the changes made for the records in the Patient Info
						Sql =  "select prn.PersonID,prn.FirstName,prn.MiddleName,prn.LastName,prn.Gender,prn.DateOfBirth,prn.DriversLicenseNumber,prn.DriversLicenseStateCode,"
							+ "prn.EmailAddress,prn.DriversLicenseExpirationDate,prn.SerialNumberPerson,prn.SSN,prn.SSNLastFour,stp.StatusTypePatientText "
							+ "from Person.Patient as pt,Person.StatusTypePatient as stp,Person.Person as prn where pt.PersonID=prn.PersonID and pt.StatusTypePatientID=stp.StatusTypePatientID "
							+ "and (pt.ChangedOnUTC>='"+previousDate+"' or prn.ChangedOnUTC>='"+previousDate+"')";
					}
				
						if(Sql!=null && Sql.length()>0){
						System.out.println(Sql);
						ResultSet rs = sta.executeQuery(Sql);
						rs.setFetchSize(1000);
						
						if(rs!=null){
							
							//Insert / Update the Patient Info from Pioneer Softwaer to the Pharmacy Portal
							returnId=pushPatientInfofromPioneer(rs,conn,isRXPatientNewRecord,previousDate,dbConnection,pharmacyRXPhysicianId);
							
							if(rs!=null)
								rs.close();
						}
						
					}//sql
					
					Sql="";
					
					if(isRXPrescriberNewRecord)
					{
						if(prescriberPioneeRXUID!=null && prescriberPioneeRXUID.length()>0){
						//Insert Prescriber New Record from RX, when no record exists in Pharmacy Portal
						Sql =  "select prn.PersonID,prn.FirstName,prn.MiddleName,prn.LastName,prn.EmailAddress,prn.WebsiteAddress,pt.GroupCode,pt.DEA,pt.DPS,pt.NPI,pt.SubmitRenewalRequestDaysBeforeSupplyEnds,prn.SerialNumberPerson,stp.StatusTypePrescriberText,"
								+ "pt.StateLicense,pt.UPIN,pt.MarketerID,prtype.PrescriberTypeID,prn.Gender,prn.DateOfBirth,prn.DriversLicenseNumber,prn.DriversLicenseStateCode,prn.DriversLicenseExpirationDate,prn.SSN,prn.SSNLastFour,stp.StatusTypePrescriberText"
								+ "	from Person.Prescriber as pt,Person.StatusTypePrescriber as stp,Person.Person as prn,Person.PrescriberType as prtype where pt.PersonID=prn.PersonID and pt.StatusTypePrescriberID=stp.StatusTypePrescriberID "
								+ "and prtype.PrescriberTypeID = pt.PrescriberTypeID and prn.PersonID='"+prescriberPioneeRXUID+"'";
						}
						
					}else{
						//Checks for the changes made for the records in the Prescriber Info
						Sql =  "select prn.PersonID,prn.FirstName,prn.MiddleName,prn.LastName,prn.EmailAddress,prn.WebsiteAddress,pt.GroupCode,pt.DEA,pt.DPS,pt.NPI,pt.SubmitRenewalRequestDaysBeforeSupplyEnds,prn.SerialNumberPerson,stp.StatusTypePrescriberText,"
							+ "pt.StateLicense,pt.UPIN,pt.MarketerID,prtype.PrescriberTypeID,prn.Gender,prn.DateOfBirth,prn.DriversLicenseNumber,prn.DriversLicenseStateCode,prn.DriversLicenseExpirationDate,prn.SSN,prn.SSNLastFour,stp.StatusTypePrescriberText"
							+ "	from Person.Prescriber as pt,Person.StatusTypePrescriber as stp,Person.Person as prn,Person.PrescriberType as prtype where pt.PersonID=prn.PersonID and pt.StatusTypePrescriberID=stp.StatusTypePrescriberID "
							+ "and prtype.PrescriberTypeID = pt.PrescriberTypeID and (pt.ChangedOnUTC>='"+previousDate+"' or prn.ChangedOnUTC>='"+previousDate+"')";
					}
					
					
						if(Sql!=null && Sql.length()>0){
							ResultSet rs6 = sta6.executeQuery(Sql);
							rs6.setFetchSize(1000);
							if(rs6!=null){
								
								//Insert / Update the Patient Info from Pioneer Softwaer to the Pharmacy Portal
								returnId=pushPrescriberInfofromPioneer(rs6,conn,isRXPrescriberNewRecord,previousDate,dbConnection);
								  
								if(rs6!=null)
									rs6.close();
							}
					}//sql
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
		
			return returnId;
		}
	 
	 	/**
	 	 * Insert New or Updates Existing Patient Record
	 	 * @param rs
	 	 * @param conn
	 	 * @param isRXPatientNewRecord
	 	 * @param previousDate
	 	 * @param dbConnection
	 	 * @param pharmacyRXPhysicianId
	 	 * @return
	 	 */
	 	public static int pushPatientInfofromPioneer(ResultSet rs,Connection conn,boolean isRXPatientNewRecord,String previousDate,Connection dbConnection,int pharmacyRXPhysicianId)
	 	{
	 		int returnId=0;
	 		try{
	 			
	 			String PersonID="",FirstName="",MiddleName="",LastName="",Gender="",DateOfBirth="",DriversLicenseNumber="",DriversLicenseStateCode="",
						EmailAddress="",DriversLicenseExpirationDate="",SerialNumberPerson="",SSN="",SSNLastFour="",StatusTypePatientText="",Allergies="",
						OtherMedications="",MedicalConditions="";

				String WebsiteAddress="",GroupCode="",DEA="",DPS="",NPI="",SubmitRenewalRequestDaysBeforeSupplyEnds="",StateLicense="",UPIN="",MarketerID="",
						StatusTypePrescriberText="",PhoneTypeText="",PhoneNumber="",AddressTypeText="",Address="",City="",StateCode="",ZipCode="",Country="",PrescriberTypeID="",
						PatientPhone="",PatientMobile="",type_id="",prescriber_phone="",prescriber_mobile="",prescriber_home="",prescriber_fax="",prescriber_office="",SecAddress="",SecCity="",SecStateCode="",SecZipCode="",SecCountry="";
			
				int group_id=0, default_physicianid=0;
				String groupName="",Sql="";
				
				Statement sta1 =null;
				Statement sta2 =null;
				Statement sta3 =null;
				Statement sta4 =null;
				Statement sta5 =null;

				
				//Fetch the default Pharmacy Group
				String selectSQL = "SELECT * FROM group_master WHERE defaultgroup = ?";
				PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
		    	preparedStatement.setString(1, "Y");
		    	ResultSet rst = preparedStatement.executeQuery();
		    	if(rst!=null){
			    	while (rst.next()) {
			    		group_id = rst.getInt("group_id");
			    		groupName= rst.getString("group_name");
			     	}
			    	rst.close();
		    	}
		    	
		    	//Fetch the default Physician
				String selectSQL11= "SELECT * FROM phy_info WHERE defaultphysician = ?";
				PreparedStatement preparedStatement11 = conn.prepareStatement(selectSQL11);
		    	preparedStatement11.setString(1, "Y");
		    	ResultSet rst2 = preparedStatement11.executeQuery();
		    	if(rst2!=null){
			    	while (rst2.next()) {
			    		default_physicianid = rst2.getInt("physician_id");
			     	}
			    	rst2.close();
		    	}

				sta1 = dbConnection.createStatement();
				sta2 = dbConnection.createStatement();
				sta3 = dbConnection.createStatement();
				sta4 = dbConnection.createStatement();
				sta5 = dbConnection.createStatement();
				
	 			while(rs.next())
				{
					
					PersonID="";FirstName="";MiddleName="";LastName="";Gender="";DateOfBirth="";DriversLicenseNumber="";DriversLicenseStateCode="";
							EmailAddress="";DriversLicenseExpirationDate="";SerialNumberPerson="";SSN="";SSNLastFour="";StatusTypePatientText="";
					
					PersonID=rs.getString("PersonID");
					FirstName=rs.getString("FirstName");
					MiddleName=rs.getString("MiddleName");
					LastName=rs.getString("LastName");
					Gender=rs.getString("Gender");
					DateOfBirth=rs.getString("DateOfBirth");
					DriversLicenseNumber=rs.getString("DriversLicenseNumber");
					DriversLicenseStateCode=rs.getString("DriversLicenseStateCode");
					EmailAddress=rs.getString("EmailAddress");
					DriversLicenseExpirationDate=rs.getString("DriversLicenseExpirationDate");
					SerialNumberPerson=rs.getString("SerialNumberPerson");
					SSN=rs.getString("SSN");
					SSNLastFour=rs.getString("SSNLastFour");
					StatusTypePatientText=rs.getString("StatusTypePatientText");
					
					String Sql1 = "";
					
					
					
					//Fetches the allergies related to Patient
					//if(isRXPatientNewRecord)
					//{
						Sql1 =  "SELECT PersonID, STUFF((SELECT ', ' + A.AllergyComments FROM Person.PatientAllergy A "
								+ "Where A.PersonID=B.PersonID and A.PersonID='"+PersonID+"' FOR XML PATH('')),1,1,'') As Allergies "
								+ "From Person.PatientAllergy B	Group By PersonID";
					/*}else{
						Sql1 =  "SELECT PersonID, STUFF((SELECT ', ' + A.AllergyComments FROM Person.PatientAllergy A "
							+ "Where A.PersonID=B.PersonID and A.PersonID='"+PersonID+"' and A.ChangedOnUTC>='"+previousDate+"' FOR XML PATH('')),1,1,'') As Allergies "
							+ "From Person.PatientAllergy B	Group By PersonID";
					}*/
					
					System.out.println(Sql1);
			
					ResultSet rs1 = sta1.executeQuery(Sql1);
					rs1.setFetchSize(1000);
					
					while(rs1.next())
					{
						
						Allergies="";
						
						Allergies=rs1.getString(2);
					
					}    	
					
					
					if(rs1!=null)
						rs1.close();
					
					//Fetches the Other Medications related to Patient
					//if(isRXPatientNewRecord)
					//{
						Sql =  "SELECT PersonID, STUFF((SELECT ', ' + (A.MedicationName) FROM Person.PatientOtherMedication A "
								+ "Where A.PersonID=B.PersonID and A.PersonID='"+PersonID+"' FOR XML PATH('')),1,1,'') As OtherMedications	"
								+ "From Person.PatientOtherMedication B Group By PersonID";
					/*}else{
						
						Sql =  "SELECT PersonID, STUFF((SELECT ', ' + (A.MedicationName) FROM Person.PatientOtherMedication A "
							+ "Where A.PersonID=B.PersonID and A.PersonID='"+PersonID+"' and A.ChangedOnUTC>='"+previousDate+"' FOR XML PATH('')),1,1,'') As OtherMedications	"
							+ "From Person.PatientOtherMedication B Group By PersonID";
					}*/
					
					System.out.println(Sql);
					ResultSet rs2 = sta2.executeQuery(Sql);
					rs2.setFetchSize(1000);
					
					while(rs2.next())
					{
						
						OtherMedications="";
						
						OtherMedications=rs2.getString("OtherMedications");
					
					}    	
					
					
					if(rs2!=null)
						rs2.close();
					
					//Fetches the MedicalConditions related to Patient
					//if(isRXPatientNewRecord)
					//{
						Sql =  "SELECT PersonID, STUFF((SELECT ', ' + (A.ICD9Code+','+A.ICD10Code) FROM Person.PatientCondition A "
							+ "	Where A.PersonID=B.PersonID and A.PersonID='"+PersonID+"' FOR XML PATH('')),1,1,'') As MedicalConditions "
							+ "From Person.PatientCondition B Group By PersonID";
						
					/*}else
					{
						Sql =  "SELECT PersonID, STUFF((SELECT ', ' + (A.ICD9Code+','+A.ICD10Code) FROM Person.PatientCondition A "
								+ "	Where A.PersonID=B.PersonID and A.PersonID='"+PersonID+"' and A.ChangedOnUTC>='"+previousDate+"' FOR XML PATH('')),1,1,'') As MedicalConditions "
								+ "From Person.PatientCondition B Group By PersonID";
						
					}*/
					System.out.println(Sql);
					ResultSet rs3 = sta3.executeQuery(Sql);
					rs3.setFetchSize(1000);
					
					while(rs3.next())
					{
						
						MedicalConditions="";
						
						MedicalConditions=rs3.getString("MedicalConditions");
					
					}    	
					
					
					if(rs3!=null)
						rs3.close();
				
					PatientMobile="";

					//Fetches the Phone details related to Patient
					//if(isRXPatientNewRecord)
					//{
						Sql =  "select pp.PersonID,ptype.PhoneTypeText,pp.PhoneNumber from Person.PersonPhone as pp,Person.Patient as pt,Person.PhoneType as ptype where"
					 		+ " pt.PersonID=pp.PersonID and pt.PersonID='"+PersonID+"' and ptype.PhoneTypeID=pp.PhoneTypeID";
					/*}else
					{
						Sql =  "select pp.PersonID,ptype.PhoneTypeText,pp.PhoneNumber from Person.PersonPhone as pp,Person.Patient as pt,Person.PhoneType as ptype where"
						 		+ " pt.PersonID=pp.PersonID and pt.PersonID='"+PersonID+"' and ptype.PhoneTypeID=pp.PhoneTypeID and pp.ChangedOnUTC>='"+previousDate+"'";
					
					}*/
					
					System.out.println(Sql);
					ResultSet rs4 = sta4.executeQuery(Sql);
					rs4.setFetchSize(1000);
					
					
	 
					while(rs4.next())
						{
					
							PhoneTypeText=rs4.getString("PhoneTypeText");
							PhoneNumber=rs4.getString("PhoneNumber");
							
							
							if(PhoneTypeText.equalsIgnoreCase("Primary") || PhoneTypeText.equalsIgnoreCase("Home")  || PhoneTypeText.equalsIgnoreCase("Other")
									|| PhoneTypeText.equalsIgnoreCase("Work") || PhoneTypeText.equalsIgnoreCase("Cell")  || PhoneTypeText.equalsIgnoreCase("Business"))
							{
								if(PhoneTypeText.equalsIgnoreCase("Primary"))
									PatientPhone=PhoneNumber;
								
								if(prescriber_phone.length()==0)
								{
									if(PhoneTypeText.equalsIgnoreCase("Other"))
										PatientPhone=PhoneNumber;
									else if(PhoneTypeText.equalsIgnoreCase("Work"))
										PatientPhone=PhoneNumber;
									else if(PhoneTypeText.equalsIgnoreCase("Business"))
										PatientPhone=PhoneNumber;
									else if(PhoneTypeText.equalsIgnoreCase("Home"))
										PatientPhone=PhoneNumber;
									else if(PhoneTypeText.equalsIgnoreCase("Cell"))
										PatientPhone=PhoneNumber;
								}
								if(PhoneTypeText.equalsIgnoreCase("Cell"))
								{
									PatientMobile=PhoneNumber;
								}
						
							}
					}
					if(rs4!=null)
						rs4.close();
					
					
					//First check for the record exists for the pioneer_uid
					int patientid=0;
					String selectSQL2 = "SELECT * FROM patient_profile WHERE pioneer_uid = ?";
					PreparedStatement preparedStatement2 = conn.prepareStatement(selectSQL2);
					preparedStatement2.setString(1, PersonID);
					ResultSet patRs = preparedStatement2.executeQuery();
					if(patRs!=null){
						while (patRs.next()) {
							patientid = patRs.getInt("patient_id");
						}
						patRs.close();
					}
					
					
				
					//If not next check for the record with the matching first_name,last_name,middle_name,phone exists
					if(patientid==0)
					{
						String pat_Phone=PatientPhone;
						if(pat_Phone!=null && pat_Phone.length()>0 && pat_Phone.length()==10)
						{
							pat_Phone=pat_Phone.substring(0, 3)+"-"+pat_Phone.substring(3, 6)+"-"+pat_Phone.substring(6, pat_Phone.length());

						}

						String selectSQL3 = "SELECT * FROM patient_profile WHERE first_name=? and last_name=? and middle_name=? and date_of_birth=?";
						PreparedStatement preparedStatement3 = conn.prepareStatement(selectSQL3);
						preparedStatement3.setString(1, FirstName);
						preparedStatement3.setString(2, LastName);
						preparedStatement3.setString(3, MiddleName);
						preparedStatement3.setObject(4, DateOfBirth);

						ResultSet phyrs3 = preparedStatement3.executeQuery();
						if(phyrs3!=null){
							while (phyrs3.next()) {
								patientid = phyrs3.getInt("patient_id");
							}
							phyrs3.close();
						}

					}

					
					if(patientid==0)
						isRXPatientNewRecord=true;
					else
						isRXPatientNewRecord=false;
					
					
					AddressTypeText="";Address="";City="";StateCode="";ZipCode="";Country="";
					
					//Fetches the Address details related to Patient
					//if(isRXPatientNewRecord)
					//{
						Sql =  "select pp.PersonID,ptype.AddressTypeText,pp.Address,pp.City,pp.StateCode,pp.ZipCode,pp.Country from Person.PersonAddress as pp,Person.Patient as pt,Person.AddressType as ptype where"
							+ " pt.PersonID=pp.PersonID and pt.PersonID='"+PersonID+"' and ptype.AddressTypeID=pp.AddressTypeID";
					/*}else
					{
						
						Sql =  "select pp.PersonID,ptype.AddressTypeText,pp.Address,pp.City,pp.StateCode,pp.ZipCode,pp.Country from Person.PersonAddress as pp,Person.Patient as pt,Person.AddressType as ptype where"
								+ " pt.PersonID=pp.PersonID and pt.PersonID='"+PersonID+"' and ptype.AddressTypeID=pp.AddressTypeID and pp.ChangedOnUTC>='"+previousDate+"'";
					
					}*/
					System.out.println(Sql);
					ResultSet rs5 = sta5.executeQuery(Sql);
					rs5.setFetchSize(1000);
					
					while(rs5.next())
					{
						
					
						AddressTypeText=rs5.getString("AddressTypeText");
						
						
						if(AddressTypeText.equalsIgnoreCase("Primary"))
						{
							Address=rs5.getString("Address");
							City=rs5.getString("City");
							StateCode=rs5.getString("StateCode");
							ZipCode=rs5.getString("ZipCode");
							Country=rs5.getString("Country");


						}
					
					}    	
					
					
					if(rs5!=null)
						rs5.close();
				
					if(isRXPatientNewRecord){
						
						//Insert Patient New Record from RX, when no record exists in Pharmacy Portal
								
						String insertTableSQL = "INSERT INTO patient_profile ("
								+ " first_name  ,last_name  ,middle_name  ,patient_name  ,status"
								+ ",user_login_id  ,password  ,phone  ,email  ,mobile , date_of_birth"
								+ ",gender  ,allergies  ,other_medications  ,medical_conditions  , drivers_license"
								+ ",license_expiration_date  ,SSN  ,Date_registered  "
								+ ",address  ,city  ,state  ,zip_code  ,country  ,drivers_license_state  "
								+ ",pioneer_uid  ,pioneer_response  ,syncStatus "
								+ ",created_by  ,created_user  ,created_date  ,last_updated_by  ,last_updated_user  ,last_updated_date,approved_by  ,approved_user  ,approved_date,denied_by  ) VALUES "
								+ " (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						
				    	PreparedStatement ipreparedStatement = conn.prepareStatement(insertTableSQL, Statement.RETURN_GENERATED_KEYS);
				    	ipreparedStatement.setObject(1, getFilteredText(FirstName));
				    	ipreparedStatement.setObject(2,getFilteredText(LastName));
		    			ipreparedStatement.setObject(3, getFilteredText(MiddleName));
		    		
						if(MiddleName!=null && MiddleName.trim().length()>0)
							ipreparedStatement.setObject(4,FirstName.trim() + " " + MiddleName.trim() + " " + LastName.trim());
						else
							ipreparedStatement.setObject(4,FirstName.trim() + " " + LastName.trim());
						
						ipreparedStatement.setObject(5, "Approved");
		    			ipreparedStatement.setObject(6, getFilteredText(EmailAddress));//username
						ipreparedStatement.setObject(7, "Temp123456");
						
						ipreparedStatement.setObject(8, getFilteredText(PatientPhone));
						
						if(EmailAddress!=null && EmailAddress.length()>0)
				    		ipreparedStatement.setObject(9, EmailAddress);
				    	else
				    		ipreparedStatement.setObject(9, "cre8patient@gmail.com");
					
						ipreparedStatement.setObject(10, getFilteredText(PatientMobile));
						ipreparedStatement.setObject(11, DateOfBirth);
						ipreparedStatement.setObject(12, getFilteredText(Gender));
						ipreparedStatement.setObject(13, getFilteredText(Allergies));
		    			ipreparedStatement.setObject(14, getFilteredText(OtherMedications));
		    			ipreparedStatement.setObject(15, getFilteredText(MedicalConditions));
						
						ipreparedStatement.setObject(16, getFilteredText(DriversLicenseNumber));
						ipreparedStatement.setObject(17, DriversLicenseExpirationDate);
						ipreparedStatement.setObject(18, getFilteredText(SSNLastFour));
						ipreparedStatement.setObject(19, getFilteredText(getCurrentTimeStamp()));
						
						ipreparedStatement.setObject(20, getFilteredText(Address));
		    			ipreparedStatement.setObject(21, getFilteredText(City));
		    			ipreparedStatement.setObject(22, getFilteredText(StateCode));
		    			ipreparedStatement.setObject(23, getFilteredText(ZipCode));
		    			ipreparedStatement.setObject(24, getFilteredText(Country));
		    			ipreparedStatement.setObject(25, getFilteredText(DriversLicenseStateCode));
		    			
		    			ipreparedStatement.setObject(26, PersonID);
		    			ipreparedStatement.setObject(27, "1");
		    			ipreparedStatement.setObject(28, 0);
		    		
		    			ipreparedStatement.setObject(29, "0");
				    	ipreparedStatement.setObject(30, "RX");
				    	ipreparedStatement.setObject(31, getFilteredText(getCurrentTimeStamp()));
				    	
				    	ipreparedStatement.setObject(32, "0");
				    	ipreparedStatement.setObject(33, "RX");
				    	ipreparedStatement.setObject(34, getFilteredText(getCurrentTimeStamp()));
				    	
				    	ipreparedStatement.setObject(35, "0");
				    	ipreparedStatement.setObject(36, "RX");
				    	ipreparedStatement.setObject(37, getFilteredText(getCurrentTimeStamp()));
				    	
				    	ipreparedStatement.setObject(38, "0");
		    			
				    	// execute insert SQL stetement
				    	patientid=0;
				    	ipreparedStatement .executeUpdate();
				    	
			    	    ResultSet generatedKeys = ipreparedStatement.getGeneratedKeys();
			            if (generatedKeys.next()) {
			                System.out.println("id is"+generatedKeys.getLong(1));
			                patientid= generatedKeys.getInt(1);
			                returnId=patientid;
			            } else {
			               throw new SQLException("Creating patient id failed, no generated key obtained.");
			            }
				           
				       	      	
					      	
					      	String insertTableSQL2 = "INSERT INTO patient_physicians ("
				        		   	+ "patient_id  ,physician_id  ,delFlag  ,created_date  ,created_by  ,created_user_type  ,updated_date  ,updated_by  ,updated_user_type) VALUES "
					    			+ " (?,?,?,?,?,?,?,?,?)";
					    	PreparedStatement ipreparedStatement2 = conn.prepareStatement(insertTableSQL2, Statement.RETURN_GENERATED_KEYS);
					    	
					    	ipreparedStatement2.setObject(1, getFilteredText(patientid));
					    	if(pharmacyRXPhysicianId==0)
					    		ipreparedStatement2.setObject(2, getFilteredText(default_physicianid));
					    	else
					  			ipreparedStatement2.setObject(2, getFilteredText(pharmacyRXPhysicianId));
					    	ipreparedStatement2.setObject(3,getFilteredText("N"));
			    		  	
			    			ipreparedStatement2.setObject(4, getFilteredText(getCurrentTimeStamp()));
			    			ipreparedStatement2.setObject(5, "0");
			    			ipreparedStatement2.setObject(6, "RX");
			    			
			    			ipreparedStatement2.setObject(7, getFilteredText(getCurrentTimeStamp()));
			    			ipreparedStatement2.setObject(8, "0");
			    			ipreparedStatement2.setObject(9, "RX");
			    			
		    		    	// execute insert SQL stetement
					      	ipreparedStatement2.executeUpdate();
					    	
					    	String insertTableSQL3 = "INSERT INTO patient_group ("
				        		   	+ "patient_id  ,group_id  ,delFlag  ,created_date  ,created_by  ,created_user_type  ,updated_date  ,updated_by  ,updated_user_type) VALUES "
					    			+ " (?,?,?,?,?,?,?,?,?)";
					    	PreparedStatement ipreparedStatement3 = conn.prepareStatement(insertTableSQL3, Statement.RETURN_GENERATED_KEYS);
					    	
					    	ipreparedStatement3.setObject(1, getFilteredText(patientid));
					    	ipreparedStatement3.setObject(2, getFilteredText(group_id));
					    	ipreparedStatement3.setObject(3,getFilteredText("N"));
			    		  	
			    			ipreparedStatement3.setObject(4, getFilteredText(getCurrentTimeStamp()));
			    			ipreparedStatement3.setObject(5, "0");
			    			ipreparedStatement3.setObject(6, "RX");
			    			
			    			ipreparedStatement3.setObject(7, getFilteredText(getCurrentTimeStamp()));
			    			ipreparedStatement3.setObject(8, "0");
			    			ipreparedStatement3.setObject(9, "RX");
			    			
		    		    	// execute insert SQL stetement
					      	ipreparedStatement3.executeUpdate();
						
					}else{
						
						//Update the Changed Info to the Existing Patient Record
						String pupdateTableSQL3 = "UPDATE patient_profile SET   first_name = ?  ,middle_name = ?  ,last_name = ?  ,patient_name = ?  ,"
								+ "gender = ?  ,date_of_birth = ?  ,drivers_license = ?  ,drivers_license_state = ?  ,email = ?  ,license_expiration_date = ?  ,"
								+ "patient_code = ?  ,SSN = ?  ,status = ?  ,allergies = ?  ,other_medications = ?  ,medical_conditions = ?  , mobile = ? ,"
								+ "address = ?,city=?,state=?,zip_code=?,country=?, last_updated_by = ?  ,"
								+ "last_updated_user = ?, last_updated_date = ? WHERE pioneer_uid = ?";
				    			PreparedStatement pupreparedStatement3 = conn.prepareStatement(pupdateTableSQL3);
				    			pupreparedStatement3.setObject(1, getFilteredText(FirstName));
				    			pupreparedStatement3.setObject(2, getFilteredText(MiddleName));
				    			pupreparedStatement3.setObject(3,getFilteredText(LastName));
				    		
								if(MiddleName!=null && MiddleName.trim().length()>0)
									pupreparedStatement3.setObject(4,FirstName.trim() + " " + MiddleName.trim() + " " + LastName.trim());
								else
									pupreparedStatement3.setObject(4,FirstName.trim() + " " + LastName.trim());
								
				    			pupreparedStatement3.setObject(5, getFilteredText(Gender));
				    			pupreparedStatement3.setObject(6, DateOfBirth);
				    			pupreparedStatement3.setObject(7, getFilteredText(DriversLicenseNumber));
				    			pupreparedStatement3.setObject(8, getFilteredText(DriversLicenseStateCode));
				    			
				    			if(EmailAddress!=null && EmailAddress.length()>0)
				    				pupreparedStatement3.setObject(9, EmailAddress);
						    	else
						    		pupreparedStatement3.setObject(9, "cre8patient@gmail.com");
				    			
				    			pupreparedStatement3.setObject(10, DriversLicenseExpirationDate);
				    			pupreparedStatement3.setObject(11, getFilteredText(SerialNumberPerson));
				    			pupreparedStatement3.setObject(12, getFilteredText(SSNLastFour));
				    			if(StatusTypePatientText.equalsIgnoreCase("Active"))
				    				pupreparedStatement3.setObject(13, "Approved");
				    			else 
				    				pupreparedStatement3.setObject(13, "Denied");
				    			//pupreparedStatement3.setObject(13, StatusTypePatientText);
				    			pupreparedStatement3.setObject(14, getFilteredText(Allergies));
				    			pupreparedStatement3.setObject(15, getFilteredText(OtherMedications));
				    			pupreparedStatement3.setObject(16, getFilteredText(MedicalConditions));
				    			pupreparedStatement3.setObject(17, getFilteredText(PatientMobile));
				    			pupreparedStatement3.setObject(18, getFilteredText(Address));
				    			pupreparedStatement3.setObject(19, getFilteredText(City));
				    			pupreparedStatement3.setObject(20, getFilteredText(StateCode));
				    			pupreparedStatement3.setObject(21, getFilteredText(ZipCode));
				    			pupreparedStatement3.setObject(22, getFilteredText(Country));
						    	pupreparedStatement3.setObject(23, "0");
						    	pupreparedStatement3.setObject(24, "RX");
						    	pupreparedStatement3.setObject(25, getFilteredText(getCurrentTimeStamp()));
						    	pupreparedStatement3.setObject(26, PersonID);
						    	// execute update SQL stetement
						    	pupreparedStatement3 .executeUpdate();
						    	
						} 
				}
	 			
	 		}catch(Exception e)
	 		{
	 			e.printStackTrace();
	 		}
	 		return returnId;
	 	}
	 	/**
	 	 * Insert New or Updates Existing Prescriber Record
	 	 * @param rs6
	 	 * @param conn
	 	 * @param isRXPrescriberNewRecord
	 	 * @param previousDate
	 	 * @param dbConnection
	 	 * @return
	 	 */
	 	public static int pushPrescriberInfofromPioneer(ResultSet rs6,Connection conn,boolean isRXPrescriberNewRecord,String previousDate,Connection dbConnection)
	 	{
	 		int returnId=0;
	 		try{
	 			String PersonID="",FirstName="",MiddleName="",LastName="",Gender="",DateOfBirth="",DriversLicenseNumber="",DriversLicenseStateCode="",
						EmailAddress="",DriversLicenseExpirationDate="",SerialNumberPerson="",SSN="",SSNLastFour="",StatusTypePatientText="",Allergies="",
						OtherMedications="",MedicalConditions="";

				String WebsiteAddress="",GroupCode="",DEA="",DPS="",NPI="",SubmitRenewalRequestDaysBeforeSupplyEnds="",StateLicense="",UPIN="",MarketerID="",
						StatusTypePrescriberText="",PhoneTypeText="",PhoneNumber="",AddressTypeText="",Address="",City="",StateCode="",ZipCode="",Country="",PrescriberTypeID="",
						PatientPhone="",PatientMobile="",type_id="",prescriber_phone="",prescriber_mobile="",prescriber_home="",prescriber_fax="",prescriber_office="",SecAddress="",SecCity="",SecStateCode="",SecZipCode="",SecCountry="";
			
				int group_id=0;
				String groupName="",Sql="";
	 			
				Statement sta7 =null;
				Statement sta8 =null;

				
				//Fetch the default Pharmacy Group
				String selectSQL = "SELECT * FROM group_master WHERE defaultgroup = ?";
				PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
		    	preparedStatement.setString(1, "Y");
		    	ResultSet rst = preparedStatement.executeQuery();
		    	if(rst!=null){
			    	while (rst.next()) {
			    		group_id = rst.getInt("group_id");
			    		groupName= rst.getString("group_name");
			     	}
			    	rst.close();
		    	}
		    	

				sta7 = dbConnection.createStatement();
				sta8 = dbConnection.createStatement();

	 			while(rs6.next())
				{
					
					PersonID="";FirstName="";MiddleName="";LastName="";EmailAddress="";WebsiteAddress="";GroupCode="";DEA="";DPS="";NPI="";
					SubmitRenewalRequestDaysBeforeSupplyEnds="";SerialNumberPerson="";StatusTypePrescriberText="";StateLicense="";UPIN="";MarketerID="";
					PrescriberTypeID="";Gender="";DateOfBirth="";DriversLicenseNumber="";DriversLicenseStateCode="";DriversLicenseExpirationDate="";SSN="";SSNLastFour="";
					StatusTypePrescriberText="";type_id="";
					
					PersonID=rs6.getString("PersonID");
					FirstName=rs6.getString("FirstName");
					MiddleName=rs6.getString("MiddleName");
					LastName=rs6.getString("LastName");
					EmailAddress=rs6.getString("EmailAddress");
					WebsiteAddress=rs6.getString("WebsiteAddress");
					GroupCode=rs6.getString("GroupCode");
					DEA=rs6.getString("DEA");
					DPS=rs6.getString("DPS");
					NPI=rs6.getString("NPI");
					SubmitRenewalRequestDaysBeforeSupplyEnds=rs6.getString("SubmitRenewalRequestDaysBeforeSupplyEnds");
					SerialNumberPerson=rs6.getString("SerialNumberPerson");
					StatusTypePrescriberText=rs6.getString("StatusTypePrescriberText");
					StateLicense=rs6.getString("StateLicense");
					UPIN=rs6.getString("UPIN");
					MarketerID=rs6.getString("MarketerID");
					PrescriberTypeID=rs6.getString("PrescriberTypeID");
					Gender=rs6.getString("Gender");
					DateOfBirth=rs6.getString("DateOfBirth");
					DriversLicenseNumber=rs6.getString("DriversLicenseNumber");
					DriversLicenseStateCode=rs6.getString("DriversLicenseStateCode");
					DriversLicenseExpirationDate=rs6.getString("DriversLicenseExpirationDate");
					SSN=rs6.getString("SSN");
					SSNLastFour=rs6.getString("SSNLastFour");
					StatusTypePrescriberText=rs6.getString("StatusTypePrescriberText");
					
					
					
					//Fetches the Prescriber Type Info related to Prescriber
					String selectSQL21 = "SELECT type_id, prescriber_type FROM prescriber_type_master WHERE "
		    				+ " pioneer_prescriber_type_id =?";

	    			PreparedStatement preparedStatement21 = conn.prepareStatement(selectSQL21);
			    	preparedStatement21.setString(1, PrescriberTypeID);
			   	
			    	ResultSet rs21 = preparedStatement21.executeQuery();
			    	
			    	if(rs21!=null){
				    	while (rs21.next()) {
				    		
				    		type_id = rs21.getString("type_id");
				     		
				    	}//while (rs3.next()) {
				    	rs21.close();
			    	}
					
					
					    	
			    	prescriber_phone="";prescriber_mobile="";prescriber_home="";prescriber_fax="";prescriber_office="";
					   
			    	//Fetches the Prescriber Phone details related to Prescriber
			    	//if(isRXPrescriberNewRecord)
					//{
			    	   	Sql =  "select pp.PersonID,ptype.PhoneTypeText,pp.PhoneNumber from Person.PersonPhone as pp,Person.Prescriber as pt,Person.PhoneType as ptype where"
						 		+ " pt.PersonID=pp.PersonID and pt.PersonID='"+PersonID+"' and ptype.PhoneTypeID=pp.PhoneTypeID";
				
			    		
					/*}else
					{
					   	Sql =  "select pp.PersonID,ptype.PhoneTypeText,pp.PhoneNumber from Person.PersonPhone as pp,Person.Prescriber as pt,Person.PhoneType as ptype where"
						 		+ " pt.PersonID=pp.PersonID and pt.PersonID='"+PersonID+"' and ptype.PhoneTypeID=pp.PhoneTypeID and pp.ChangedOnUTC>='"+previousDate+"'";
				
						
					}*/
					 	
							ResultSet rs7 = sta7.executeQuery(Sql);
							rs7.setFetchSize(1000);
							
							while(rs7.next())
							{
								
								PhoneTypeText="";PhoneNumber="";
								
								
								PhoneTypeText=rs7.getString("PhoneTypeText");
								PhoneNumber=rs7.getString("PhoneNumber");
								
									
								if(PhoneTypeText.equalsIgnoreCase("Primary") || PhoneTypeText.equalsIgnoreCase("Home")  || PhoneTypeText.equalsIgnoreCase("Other")
										|| PhoneTypeText.equalsIgnoreCase("Work") || PhoneTypeText.equalsIgnoreCase("Cell")  || PhoneTypeText.equalsIgnoreCase("Business"))
								{
									if(PhoneTypeText.equalsIgnoreCase("Primary"))
										prescriber_phone=PhoneNumber;
									
									if(prescriber_phone.length()==0)
									{
										if(PhoneTypeText.equalsIgnoreCase("Other"))
											prescriber_phone=PhoneNumber;
										else if(PhoneTypeText.equalsIgnoreCase("Work"))
											prescriber_phone=PhoneNumber;
										else if(PhoneTypeText.equalsIgnoreCase("Business"))
											prescriber_phone=PhoneNumber;
										else if(PhoneTypeText.equalsIgnoreCase("Home"))
											prescriber_phone=PhoneNumber;
										else if(PhoneTypeText.equalsIgnoreCase("Cell"))
											prescriber_phone=PhoneNumber;
									}
									if(PhoneTypeText.equalsIgnoreCase("Cell"))
									{
										prescriber_mobile=PhoneNumber;
									}
							
								}else if(PhoneTypeText.equalsIgnoreCase("Primary Fax") || PhoneTypeText.equalsIgnoreCase("Fax")) 
								{
									prescriber_fax=PhoneNumber;
						
								}
								
							
							
							}    	
							
							
							if(rs7!=null)
								rs7.close();

							//First check for the record exists for the pioneer_uid
							int physicianid=0;
							String selectSQL2 = "SELECT * FROM phy_info WHERE pioneer_uid=?";
							PreparedStatement preparedStatement2 = conn.prepareStatement(selectSQL2);
							preparedStatement2.setString(1, PersonID);
							ResultSet phyrs = preparedStatement2.executeQuery();
							if(phyrs!=null){
								while (phyrs.next()) {
									physicianid = phyrs.getInt("physician_id");
								}
								phyrs.close();
							}
							//If not next check for the record with the matching first_name,last_name,middle_name,phone exists
							if(physicianid==0)
							{
								String phy_Phone=prescriber_phone;
								if(phy_Phone!=null && phy_Phone.length()>0 && phy_Phone.length()==10)
								{
									phy_Phone=phy_Phone.substring(0, 3)+"-"+phy_Phone.substring(3, 6)+"-"+phy_Phone.substring(6, phy_Phone.length());
									
								}
								
								String selectSQL3 = "SELECT * FROM phy_info WHERE first_name=? and last_name=? and middle_name=? and phone=?";
								PreparedStatement preparedStatement3 = conn.prepareStatement(selectSQL3);
								preparedStatement3.setString(1, FirstName);
								preparedStatement3.setString(2, LastName);
								preparedStatement3.setString(3, MiddleName);
								preparedStatement3.setString(4, phy_Phone);
								
								ResultSet phyrs3 = preparedStatement3.executeQuery();
								if(phyrs3!=null){
									while (phyrs3.next()) {
										physicianid = phyrs3.getInt("physician_id");
									}
									phyrs3.close();
								}
							
							}
							
							if(physicianid==0)
								isRXPrescriberNewRecord=true;
							else
								isRXPrescriberNewRecord=false;
							
							
							//Fetches the Prescriber Address details related to Prescriber
							//if(isRXPrescriberNewRecord)
							//{
								Sql =  "select pp.PersonID,ptype.AddressTypeText,pp.Address,pp.City,pp.StateCode,pp.ZipCode,pp.Country from Person.PersonAddress as pp,Person.Prescriber as pt,Person.AddressType as ptype where"
										+ " pt.PersonID=pp.PersonID and pt.PersonID='"+PersonID+"' and ptype.AddressTypeID=pp.AddressTypeID";
							
							/*}else
							{
								Sql =  "select pp.PersonID,ptype.AddressTypeText,pp.Address,pp.City,pp.StateCode,pp.ZipCode,pp.Country from Person.PersonAddress as pp,Person.Prescriber as pt,Person.AddressType as ptype where"
										+ " pt.PersonID=pp.PersonID and pt.PersonID='"+PersonID+"' and ptype.AddressTypeID=pp.AddressTypeID and pp.ChangedOnUTC>='"+previousDate+"'";
								
							}*/
							
							ResultSet rs8 = sta8.executeQuery(Sql);
							rs8.setFetchSize(1000);
							
							while(rs8.next())
							{
								
								AddressTypeText="";Address="";City="";StateCode="";ZipCode="";Country="";
								SecAddress="";SecCity="";SecStateCode="";SecZipCode="";SecCountry="";
								
								AddressTypeText=rs8.getString("AddressTypeText");
								if(AddressTypeText.equalsIgnoreCase("Primary")){
									Address=rs8.getString("Address");
									City=rs8.getString("City");
									StateCode=rs8.getString("StateCode");
									ZipCode=rs8.getString("ZipCode");
									Country=rs8.getString("Country");
								}else
								{
									SecAddress=rs8.getString("Address");
									SecCity=rs8.getString("City");
									SecStateCode=rs8.getString("StateCode");
									SecZipCode=rs8.getString("ZipCode");
									SecCountry=rs8.getString("Country");
								}
							}    	
							
							
							if(rs8!=null)
								rs8.close();
						  		
				
							if(isRXPrescriberNewRecord)
							{
								String fullName="";
								
								//Insert Prescriber New Record from RX, when no record exists in Pharmacy Portal
								String insertTableSQL = "INSERT INTO phy_info "
						    			+ "(first_name  ,last_name  ,middle_name  ,physician_name  ,status  ,password"
						    			+",phone  ,email  ,mobile  ,prescriber_type  ,fax , address1  ,city  ,state"
						    			+",zip_code  ,country  ,date_of_registration  ,secondary_address1"
						    			+",secondary_city  ,secondary_state  ,secondary_zip_code  ,secondary_country  ,pioneer_uid  ,pioneer_response ,"
						    			+ "created_by  ,created_user  ,created_date"
						    			+",last_updated_by  ,last_updated_user  ,last_updated_date  ,useGroupLogo  ,name_with_group_name,approved_by  ,approved_user  ,approved_date,denied_by) VALUES "
						    			+ " (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
								
						    	PreparedStatement ipreparedStatement = conn.prepareStatement(insertTableSQL, Statement.RETURN_GENERATED_KEYS);
						    	ipreparedStatement.setObject(1, getFilteredText(FirstName));
						    	ipreparedStatement.setObject(2, getFilteredText(LastName));
						    	ipreparedStatement.setObject(3, getFilteredText(MiddleName));
						    	
						    	if(MiddleName!=null && MiddleName.trim().length()>0)
						    		fullName=FirstName.trim() + " " + MiddleName.trim() + " " + LastName.trim();
								else
									fullName=FirstName.trim() + " " + LastName.trim();
						    	
						    	ipreparedStatement.setObject(4,fullName);
								ipreparedStatement.setObject(5, "Approved");
				    			
						    	ipreparedStatement.setObject(6, "Temp123456");
						    	ipreparedStatement.setObject(7, getFilteredText(prescriber_phone));
						    	
						    	if(EmailAddress!=null && EmailAddress.length()>0)
						    		ipreparedStatement.setObject(8, EmailAddress);
						    	else
						    		ipreparedStatement.setObject(8, "cre8physician@gmail.com");
						    	
						    	ipreparedStatement.setObject(9, getFilteredText(prescriber_mobile));
						    	ipreparedStatement.setObject(10, getFilteredText(type_id));
						    	ipreparedStatement.setObject(11, getFilteredText(prescriber_fax));
				    			ipreparedStatement.setObject(12, getFilteredText(Address));
				    			ipreparedStatement.setObject(13, getFilteredText(City));
				    			ipreparedStatement.setObject(14, getFilteredText(StateCode));
				    			ipreparedStatement.setObject(15, getFilteredText(ZipCode));
				    			ipreparedStatement.setObject(16, getFilteredText(Country));
				    			ipreparedStatement.setObject(17, getFilteredText(getCurrentTimeStamp()));
				    			ipreparedStatement.setObject(18, getFilteredText(SecAddress));
				    			ipreparedStatement.setObject(19, getFilteredText(SecCity));
				    			ipreparedStatement.setObject(20, getFilteredText(SecStateCode));
				    			ipreparedStatement.setObject(21, getFilteredText(SecZipCode));
				    			ipreparedStatement.setObject(22, getFilteredText(SecCountry));
				    			
						    	ipreparedStatement.setObject(23, getFilteredText(PersonID));
						    	ipreparedStatement.setObject(24, getFilteredText(1));
						    	
						    	ipreparedStatement.setObject(25, "0");
						    	ipreparedStatement.setObject(26, "RX");
						    	ipreparedStatement.setObject(27, getFilteredText(getCurrentTimeStamp()));
						    	
						    	ipreparedStatement.setObject(28, "0");
						    	ipreparedStatement.setObject(29, "RX");
						    	ipreparedStatement.setObject(30, getFilteredText(getCurrentTimeStamp()));
						    	
						    	ipreparedStatement.setObject(31, getFilteredText("Yes"));
						    	ipreparedStatement.setObject(32, getFilteredText(fullName+" - "+groupName));
						    	
						    	ipreparedStatement.setObject(33, "0");
						    	ipreparedStatement.setObject(34, "RX");
						    	ipreparedStatement.setObject(35, getFilteredText(getCurrentTimeStamp()));
						    	
						    	ipreparedStatement.setObject(36, "0");
						    	
						    	// execute insert SQL stetement
						    	physicianid=0;
						    	ipreparedStatement .executeUpdate();
						    	
						    	   ResultSet generatedKeys = ipreparedStatement.getGeneratedKeys();
						           if (generatedKeys.next()) {
						                System.out.println("id is"+generatedKeys.getLong(1));
						                physicianid= generatedKeys.getInt(1);
						                returnId=physicianid;
						           } else {
						               throw new SQLException("Creating prescriber id failed, no generated key obtained.");
						           }
						           
						           String insertTableSQL2 = "INSERT INTO phy_profile ("
						        		   	+ "physician_id  ,website  ,dea  ,npi  ,upin  ,state_license  ,dps, renewal_req_days_bf_supply_ends) VALUES "
							    			+ " (?,?,?,?,?,?,?,?)";
							    	PreparedStatement ipreparedStatement2 = conn.prepareStatement(insertTableSQL2, Statement.RETURN_GENERATED_KEYS);
							    	
							    	ipreparedStatement2.setObject(1, getFilteredText(physicianid));
							  		ipreparedStatement2.setObject(2, getFilteredText(WebsiteAddress));
					    			ipreparedStatement2.setObject(3,getFilteredText(DEA));
					    			ipreparedStatement2.setObject(4, NPI);
					    			ipreparedStatement2.setObject(5, UPIN);
					    			ipreparedStatement2.setObject(6, StateLicense);
					    			
				    				ipreparedStatement2.setObject(7, DPS);
				    				if(SubmitRenewalRequestDaysBeforeSupplyEnds==null || SubmitRenewalRequestDaysBeforeSupplyEnds.equalsIgnoreCase(""))
				    					ipreparedStatement2.setObject(8, 0);
				    				else
				    					ipreparedStatement2.setObject(8, SubmitRenewalRequestDaysBeforeSupplyEnds);
				    				
							    	// execute insert SQL stetement
							      	ipreparedStatement2.executeUpdate();
							      	
							      	
							      	String insertTableSQL3 = "INSERT INTO phy_group ("
						        		   	+ "physician_id  ,group_id  ,status  ,created_date  ,created_by  ,created_user_type  ,updated_date  ,updated_by  ,updated_user_type) VALUES "
							    			+ " (?,?,?,?,?,?,?,?,?)";
							    	PreparedStatement ipreparedStatement3 = conn.prepareStatement(insertTableSQL3, Statement.RETURN_GENERATED_KEYS);
							    	
							    	ipreparedStatement3.setObject(1, getFilteredText(physicianid));
							  		ipreparedStatement3.setObject(2, getFilteredText(group_id));
					    			ipreparedStatement3.setObject(3,getFilteredText("Active"));
					    		  	
					    			ipreparedStatement3.setObject(4, getFilteredText(getCurrentTimeStamp()));
					    			ipreparedStatement3.setObject(5, "0");
					    			ipreparedStatement3.setObject(6, "RX");
					    			
					    			ipreparedStatement3.setObject(7, getFilteredText(getCurrentTimeStamp()));
					    			ipreparedStatement3.setObject(8, "0");
					    			ipreparedStatement3.setObject(9, "RX");
					    			
				    		    	// execute insert SQL stetement
							      	ipreparedStatement3.executeUpdate();
							      	
							      	//Encrypted creditcard details pushed
							      	JPACryptoConverter jpaCrypt=new JPACryptoConverter();
							      	Date curDate=new Date();
							      	int curYear = LocalDate.now().getYear();
							      	String card_type="",card_number="",Card_expiry_month="",Card_expiry_year="",card_cvc_number="",card_holder_name="";
							      	card_type=jpaCrypt.convertToDatabaseColumn("4");
							      	card_number=jpaCrypt.convertToDatabaseColumn("4111111111111111");
							      	Card_expiry_month=jpaCrypt.convertToDatabaseColumn("12");
							      	Card_expiry_year=jpaCrypt.convertToDatabaseColumn((curYear+10)+"");
							      	card_cvc_number=jpaCrypt.convertToDatabaseColumn("123");
							      	card_holder_name=jpaCrypt.convertToDatabaseColumn(fullName);
							      	
							      	
							    
							      	
							      	
							      	//Create Physician's Clinic
							      	String insertTableSQL4 = "INSERT INTO clinic ("
						        		   	+ "clinic_name  ,address  ,city  ,state  ,zip_code  ,Phone  ,status ,email  ,created_by  ,created_user  ,created_date  ,"
						        		   	+ " last_updated_by  ,last_updated_user  ,last_updated_date  ,group_id  ,location  ,contact_name  ,fax) VALUES "
							    			+ " (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
							    	PreparedStatement ipreparedStatement4 = conn.prepareStatement(insertTableSQL4, Statement.RETURN_GENERATED_KEYS);
							    	//Clinic Name => <Doctor Name> - CLINIC
							    	ipreparedStatement4.setObject(1, getFilteredText(fullName+" - CLINIC"));
							    	//Clini Address details => all his Primary Address & other details
							  		ipreparedStatement4.setObject(2, getFilteredText(Address));
					    			ipreparedStatement4.setObject(3,getFilteredText(City));
					    			ipreparedStatement4.setObject(4, getFilteredText(StateCode));
					    			ipreparedStatement4.setObject(5,getFilteredText(ZipCode));
					    			ipreparedStatement4.setObject(6,getFilteredText(prescriber_phone));
					    			ipreparedStatement4.setObject(7,getFilteredText("Info Completed"));
					    		  	
					    			if(EmailAddress!=null && EmailAddress.length()>0)
							    		ipreparedStatement4.setObject(8, EmailAddress);
							    	else
							    		ipreparedStatement4.setObject(8, "cre8physician@gmail.com");
					    			
					    			ipreparedStatement4.setObject(9, "0");
					    			ipreparedStatement4.setObject(10, "RX");
					    			ipreparedStatement4.setObject(11, getFilteredText(getCurrentTimeStamp()));
					    			
					    			ipreparedStatement4.setObject(12, "0");
					    			ipreparedStatement4.setObject(13, "RX");
					    			ipreparedStatement4.setObject(14, getFilteredText(getCurrentTimeStamp()));
					    			//Default Group
					    			ipreparedStatement4.setObject(15, group_id);
					    			//Branch -> City
					    			ipreparedStatement4.setObject(16, City);
					    			//Contact Name -> Physician Name 
					    			ipreparedStatement4.setObject(17, fullName);
					    			ipreparedStatement4.setObject(18, prescriber_fax);
					    			
					    			// execute insert SQL stetement
							      	ipreparedStatement4.executeUpdate();
							      	int clinic_Id=0;
							      	ResultSet generatedKeys4 = ipreparedStatement4.getGeneratedKeys();
							           if (generatedKeys4.next()) {
							                System.out.println("id is"+generatedKeys4.getLong(1));
							                clinic_Id= generatedKeys4.getInt(1);
							           } else {
							               throw new SQLException("Creating prescriber clinic id failed, no generated key obtained.");
							           }
							           
							    	
							        //Display the clinic created in the list in the Physician Info
							      	String insertTableSQL5 = "INSERT INTO phy_clinic ("
						        		   	+ "physician_id  ,clinic_id  ,delFlag  ,created_date  ,created_by  ,created_user_type  ,updated_date  ,updated_by  ,updated_user_type) VALUES "
							    			+ " (?,?,?,?,?,?,?,?,?)";
							    	PreparedStatement ipreparedStatement5 = conn.prepareStatement(insertTableSQL5, Statement.RETURN_GENERATED_KEYS);
							    	
							    	ipreparedStatement5.setObject(1, getFilteredText(physicianid));
							  		ipreparedStatement5.setObject(2, getFilteredText(clinic_Id));
					    			ipreparedStatement5.setObject(3,getFilteredText("N"));
					    		  	
					    			ipreparedStatement5.setObject(4, getFilteredText(getCurrentTimeStamp()));
					    			ipreparedStatement5.setObject(5, "0");
					    			ipreparedStatement5.setObject(6, "RX");
					    			
					    			ipreparedStatement5.setObject(7, getFilteredText(getCurrentTimeStamp()));
					    			ipreparedStatement5.setObject(8, "0");
					    			ipreparedStatement5.setObject(9, "RX");
					    			
				    		    	// execute insert SQL stetement
							      	ipreparedStatement5.executeUpdate();
							      	//Default creditcard details pushed
							  		String insertTableSQL6 = "INSERT INTO phy_creditcard ("
						        		   	+ "physician_id  ,card_type  ,card_number  ,card_holder_name  ,Card_expiry_month  ,Card_expiry_year  ,card_cvc_number  ,billingZipCode) VALUES "
							    			+ " (?,?,?,?,?,?,?,?)";
							    	PreparedStatement ipreparedStatement6 = conn.prepareStatement(insertTableSQL6, Statement.RETURN_GENERATED_KEYS);
							    	
							    	ipreparedStatement6.setObject(1, getFilteredText(physicianid));
							  		ipreparedStatement6.setObject(2, getFilteredText(card_type));
					    			ipreparedStatement6.setObject(3,getFilteredText(card_number));
					    		  	
					    			ipreparedStatement6.setObject(4, getFilteredText(card_holder_name));
					    			ipreparedStatement6.setObject(5, getFilteredText(Card_expiry_month));
					    			ipreparedStatement6.setObject(6, getFilteredText(Card_expiry_year));
					    			
					    			ipreparedStatement6.setObject(7, getFilteredText(card_cvc_number));
					    			ipreparedStatement6.setObject(8, getFilteredText(ZipCode));
					    			
					    			
				    		    	// execute insert SQL stetement
							      	ipreparedStatement6.executeUpdate();
							      	
							}else{
								
 								 //Update the Changed Info to the Existing Prescriber Record
								 String pupdateTableSQL3 = "UPDATE phy_info SET   first_name = ?  ,middle_name = ?  ,last_name = ?  ,physician_name = ?  ,email = ?  ,"
								 		+ "prescriber_group = ? ,physician_code = ?  ,status = ?     ,prescriber_type = ? ,phone=?, mobile = ?,fax=?,phone2=?,phone3=?  ,"
								 		+ "address1 = ?,city=?,state=?,zip_code=?,country=?,secondary_address1=?,secondary_city=?,secondary_state=?,secondary_zip_code=?,"
								 		+ "secondary_country=?, last_updated_by = ?  ,last_updated_user = ?  ,last_updated_date = ? WHERE pioneer_uid = ?";
								 PreparedStatement pupreparedStatement3 = conn.prepareStatement(pupdateTableSQL3);
						    			pupreparedStatement3.setObject(1, getFilteredText(FirstName));
						    			pupreparedStatement3.setObject(2, getFilteredText(MiddleName));
						    			pupreparedStatement3.setObject(3,getFilteredText(LastName));
						    		
										if(MiddleName!=null && MiddleName.trim().length()>0)
											pupreparedStatement3.setObject(4,FirstName.trim() + " " + MiddleName.trim() + " " + LastName.trim());
										else
											pupreparedStatement3.setObject(4,FirstName.trim() + " " + LastName.trim());
										
						    			if(EmailAddress!=null && EmailAddress.length()>0)
						    				pupreparedStatement3.setObject(5, EmailAddress);
								    	else
								    		pupreparedStatement3.setObject(5, "cre8physician@gmail.com");
						    			
						    			pupreparedStatement3.setObject(6, GroupCode);
						    			pupreparedStatement3.setObject(7, SerialNumberPerson);
						    			
						    			if(StatusTypePrescriberText.equalsIgnoreCase("Active"))
						    				pupreparedStatement3.setObject(8, "Approved");
						    			else 
						    				pupreparedStatement3.setObject(8, "Denied");
						    			
						    				pupreparedStatement3.setObject(9, type_id);
						    				pupreparedStatement3.setObject(10, getFilteredText(prescriber_phone));
							    			pupreparedStatement3.setObject(11, getFilteredText(prescriber_mobile));
							    			pupreparedStatement3.setObject(12, getFilteredText(prescriber_fax));
							    			pupreparedStatement3.setObject(13, getFilteredText(prescriber_home));
							    			pupreparedStatement3.setObject(14, getFilteredText(prescriber_office));
							    			pupreparedStatement3.setObject(15, getFilteredText(Address));
							    			pupreparedStatement3.setObject(16, getFilteredText(City));
							    			pupreparedStatement3.setObject(17, getFilteredText(StateCode));
							    			pupreparedStatement3.setObject(18, getFilteredText(ZipCode));
							    			pupreparedStatement3.setObject(19, getFilteredText(Country));
							    			pupreparedStatement3.setObject(20, getFilteredText(SecAddress));
							    			pupreparedStatement3.setObject(21, getFilteredText(SecCity));
							    			pupreparedStatement3.setObject(22, getFilteredText(SecStateCode));
							    			pupreparedStatement3.setObject(23, getFilteredText(SecZipCode));
							    			pupreparedStatement3.setObject(24, getFilteredText(SecCountry));
							    			pupreparedStatement3.setObject(25, "0");
									    	pupreparedStatement3.setObject(26, "RX");
									    	pupreparedStatement3.setObject(27, getFilteredText(getCurrentTimeStamp()));
									    	pupreparedStatement3.setObject(28, PersonID);
									    	// execute update SQL stetement
									    	pupreparedStatement3 .executeUpdate();
									    	
									    	
									    	
								   //medicaid    	
									pupdateTableSQL3 = "UPDATE phy_profile SET  website = ?  ,dea = ?  ,npi = ?  ,upin = ?  ,state_license = ?  ,"
										+ "dps = ?  ,renewal_req_days_bf_supply_ends = ? WHERE physician_id in (select physician_id from phy_info WHERE pioneer_uid = ?)";
									//System.out.println("SubmitRenewalRequestDaysBeforeSupplyEnds ======="+SubmitRenewalRequestDaysBeforeSupplyEnds);
							    	pupreparedStatement3 = conn.prepareStatement(pupdateTableSQL3);
					    			pupreparedStatement3.setObject(1, getFilteredText(WebsiteAddress));
					    			//pupreparedStatement3.setObject(2, getFilteredText(MarketerID));
					    			pupreparedStatement3.setObject(2,getFilteredText(DEA));
					    			pupreparedStatement3.setObject(3, NPI);
					    			pupreparedStatement3.setObject(4, UPIN);
					    			pupreparedStatement3.setObject(5, StateLicense);
					    			
				    				pupreparedStatement3.setObject(6, DPS);
				    				if(SubmitRenewalRequestDaysBeforeSupplyEnds==null || SubmitRenewalRequestDaysBeforeSupplyEnds.equalsIgnoreCase(""))
				    					pupreparedStatement3.setObject(7, 0);
				    				else
				    					pupreparedStatement3.setObject(7, SubmitRenewalRequestDaysBeforeSupplyEnds);
					    			pupreparedStatement3.setObject(8, PersonID);
							    	// execute update SQL stetement
							    	pupreparedStatement3 .executeUpdate();
							}
					}
	 			
	 		}catch(Exception e)
	 		{
	 			e.printStackTrace();
	 		}
	 		return returnId;
	 	}
	 	
		private static java.sql.Timestamp getCurrentTimeStamp() {

			java.util.Date today = new java.util.Date();
			return new java.sql.Timestamp(today.getTime());

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
private static Connection getSourceDBConnection2() {

	Connection dbConnection = null;

	try {

		Class.forName(DB_DRIVER);

	} catch (ClassNotFoundException e) {

		System.out.println(e.getMessage());

	}

	try {

		dbConnection = DriverManager.getConnection(
			DB_CONNECTION2, DB_USER,DB_PASSWORD);
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

public static String getPreviousDateByDays(int days) {
	
	Date dateFrom=null;
	String previousDate="";
	try {
	
		
		Instant now = Instant.now();
		Instant nowMinusDays = now.minus(Duration.ofDays(days));
		dateFrom = Date.from(nowMinusDays);

		LocalDateTime localeDate = LocalDateTime.ofInstant(nowMinusDays, ZoneId.systemDefault());
		previousDate = localeDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		//System.out.println("1111111111111 ==="+previousDate);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
	return previousDate;
}
	public static void main(String[] argv) {

		try {
			
			//getPreviousDateByDays(1);
			//fetchPhysicianAndPatientInfo(false,"","",0);
			
			/*String phy_Phone="9546391574";
			if(phy_Phone!=null && phy_Phone.length()>0 && phy_Phone.length()==10)
			{
				phy_Phone=phy_Phone.substring(0, 3)+"-"+phy_Phone.substring(3, 6)+"-"+phy_Phone.substring(6, phy_Phone.length());
				
			}
			System.out.println("phy_Phone ==="+phy_Phone);*/
			
		} catch (Exception e) {

			System.out.println(e.getMessage());

		}

	}
}
