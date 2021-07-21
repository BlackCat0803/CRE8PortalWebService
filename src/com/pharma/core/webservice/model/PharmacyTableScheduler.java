package com.pharma.core.webservice.model;

import com.opencsv.CSVWriter;
import com.pharma.core.util.PropertyConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;

/**
 * 
 * The class <<PharmacyTableScheduler>> is a Scheduler class for mapping the Pioneer Tables into CRE8Portal for putting Prescriptions
 * Frequently updated Tables - pioneer.item.deaoverride, pioneer.item.item, pioneer.diagnosis.icd10, pioneer.prescription.sig, 
 * pioneer.fdb.medmedication
 *
 */
public class PharmacyTableScheduler {

	 private static final String DB_DRIVER = PropertyConfigurer.getPropertyKeyValue("DB_DRIVER");
	 private static final String DB_CONNECTION1 = PropertyConfigurer.getPropertyKeyValue("DB_CONNECTION1");
	 private static final String DB_CONNECTION2 = PropertyConfigurer.getPropertyKeyValue("DB_CONNECTION2");
	 private static final String DB_USER = PropertyConfigurer.getPropertyKeyValue("DB_USER");
	 private static final String DB_PASSWORD = PropertyConfigurer.getPropertyKeyValue("DB_PASSWORD");
	 
	 
	 private static final String MYSQL_DB_DRIVER = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_DRIVER");
	 private static final String MYSQL_DB_CONNECTION = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_CONNECTION");
	 private static final String MYSQL_DB_USER = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_USER");
	 private static final String MYSQL_DB_PASSWORD = PropertyConfigurer.getPropertyKeyValue("MYSQL_DB_PASSWORD");
	 
	 private static final String pioneer_data_folderpath = PropertyConfigurer.getPropertyKeyValue("pioneer_data_folderpath");
	 
	 
 	
	/**
	 * pushPioneerPharmaCSVData
	 * @param filePath
	 * @param tablename
	 * @throws SQLException
	 */
	public static void pushPioneerPharmaCSVData(String filePath,String tablename) throws SQLException {

		Connection destinationDBConnection = null;
		Statement stmt =null;
		Statement tstmt =null; 

		try {
			
				destinationDBConnection = getDestinationDBConnection();
				
				tstmt= destinationDBConnection.createStatement();
				// Use TRUNCATE
			    String sql = "TRUNCATE `"+tablename+"`";
			    // Execute deletion
			    tstmt.executeUpdate(sql);
			    if(tstmt!=null)
			    	tstmt.close();
				    
			 	stmt=destinationDBConnection.createStatement();
			 	String quoteString="\"";
			 	String lineterminatedString="\\n";
				String hql="LOAD DATA local INFILE '"+filePath+"' INTO TABLE `" + tablename + "` CHARACTER SET latin1 FIELDS TERMINATED BY ',' ENCLOSED BY '"+quoteString+"' LINES TERMINATED BY '"+lineterminatedString+"'" ;
				System.out.println(hql);
				stmt.executeUpdate(hql);	
				
				if(tablename.equalsIgnoreCase("pioneer.fdb.medmedication")){
					String updateTableSQL3 = "UPDATE `" + tablename + "` SET medtype=1 where medtype=?";
	    			PreparedStatement upreparedStatement3 = destinationDBConnection.prepareStatement(updateTableSQL3);
	    			upreparedStatement3.setObject(1, 0);
			    	// execute update SQL stetement
			    	upreparedStatement3 .executeUpdate();
				}
				    	

		} catch (Exception e) {

			System.out.println(e.getMessage());
			e.printStackTrace();

		}
		
	}
	/**
	 * pushPioneerPrescriberTypeSelectedCSVData
	 * @param filePath
	 * @param tablename
	 * @throws SQLException
	 */
	public static void pushPioneerPrescriberTypeSelectedCSVData(String filePath,String tablename) throws SQLException {

		Connection destinationDBConnection = null;
		Statement stmt =null;
		Statement tstmt =null; 

		try {
			
				destinationDBConnection = getDestinationDBConnection();
				
				tstmt= destinationDBConnection.createStatement();
				// Use TRUNCATE
			    String sql = "TRUNCATE `"+tablename+"`";
			    // Execute deletion
			    tstmt.executeUpdate(sql);
			    if(tstmt!=null)
			    	tstmt.close();
				    
			 	stmt=destinationDBConnection.createStatement();
			 	String quoteString="\"";
			 	String lineterminatedString="\\n";
				String hql="LOAD DATA local INFILE '"+filePath+"' INTO TABLE `" + tablename + "` FIELDS TERMINATED BY ',' ENCLOSED BY '"+quoteString+"' LINES TERMINATED BY '"+lineterminatedString+"' (@value1,@value2,@value3,@value4,@value5) set prescriber_type=@value2 ,pioneer_prescriber_type_id=@value1;" ;
				System.out.println(hql);
				stmt.executeUpdate(hql);			

		} catch (Exception e) {

			System.out.println(e.getMessage());
			e.printStackTrace();

		}
		
	}

	/**
	 * writeDatatoCSVFile
	 * @param rs
	 * @param csvFile
	 */
	public static void writeDatatoCSVFile(ResultSet rs,String csvFile)
	{
		String lineString="";
		
		try {
			if(rs!=null)
			{
				 int total_column = rs.getMetaData().getColumnCount();
				 System.out.println(new Timestamp(System.currentTimeMillis()));
				 
			      
					 String filePath = csvFile;
					 File f = new File(filePath);
					if(f.exists()){
	
						 f.delete();
					 }
							
				   CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
		            writer.writeAll(rs, false); //And the second argument is boolean which represents whether you want to write header columns (table column names) to file or not.
		            writer.close();
		      
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	/**
	 * fetchPioneerICD10CodeData
	 * @param csvFile
	 * @param tableName
	 * @param fetchDataFlg
	 * @throws SQLException
	 */
	public static void fetchPioneerICD10CodeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {

		System.out.println(new Timestamp(System.currentTimeMillis()));
		if(fetchDataFlg){
			Connection dbConnection = null;
			Statement sta =null;
			
			
			try {
				
				dbConnection = getSourceDBConnection2();
				if(dbConnection!=null){
					sta = dbConnection.createStatement();
					String Sql = "select * from diagnosis.icd10  order by icd10code asc";
					ResultSet rs = sta.executeQuery(Sql);
					rs.setFetchSize(1000);
					
					if(rs!=null)
					{
						writeDatatoCSVFile(rs,csvFile);
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
		}
		
		try {
			
			pushPioneerPharmaCSVData(csvFile,tableName);
			System.out.println(new Timestamp(System.currentTimeMillis()));
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * fetchPrescriptionSigData
	 * @param csvFile
	 * @param tableName
	 * @param fetchDataFlg
	 * @throws SQLException
	 */
	public static void fetchPrescriptionSigData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {

		System.out.println(new Timestamp(System.currentTimeMillis()));
		if(fetchDataFlg){
		Connection dbConnection = null;
		Statement sta =null;
		
		
		try {
			
			dbConnection = getSourceDBConnection1();
			if(dbConnection!=null){
			sta = dbConnection.createStatement();
			String Sql = "select * from prescription.sig order by sigid asc";
			ResultSet rs = sta.executeQuery(Sql);
			rs.setFetchSize(1000);
			
			if(rs!=null)
			{
				writeDatatoCSVFile(rs,csvFile);
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
		}
		
		try {
			
			pushPioneerPharmaCSVData(csvFile,tableName);
			System.out.println(new Timestamp(System.currentTimeMillis()));
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * fetchPioneerFDBMedicationCodeData
	 * @param csvFile
	 * @param tableName
	 * @param fetchDataFlg
	 * @throws SQLException
	 */
	public static void fetchPioneerFDBMedicationCodeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {

		System.out.println(new Timestamp(System.currentTimeMillis()));
		if(fetchDataFlg){
		Connection dbConnection = null;
		Statement sta =null;
		
		
		try {
			
			dbConnection = getSourceDBConnection2();
			if(dbConnection!=null){
			sta = dbConnection.createStatement();
			String Sql = "select * from fdb.medmedication order by medicationid asc";
			/*String Sql = "select med.*,ndc.DEACode,ndc.NationalDrugCode,ndc.LabelName60 from Fdb.medmedication as med,"
					+ "Fdb.NDC as ndc where med.ClinicalFormulationID=ndc.ClinicalFormulationID order by med.medicationid asc";*/
			ResultSet rs = sta.executeQuery(Sql);
			rs.setFetchSize(1000);
			
			if(rs!=null)
			{
				writeDatatoCSVFile(rs,csvFile);
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
		}
		
		try {
			
			pushPioneerPharmaCSVData(csvFile,tableName);
			System.out.println(new Timestamp(System.currentTimeMillis()));
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * fetchPioneerDispensingUnitData
	 * @param csvFile
	 * @param tableName
	 * @param fetchDataFlg
	 * @throws SQLException
	 */
	public static void fetchPioneerDispensingUnitData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {

		System.out.println(new Timestamp(System.currentTimeMillis()));
		if(fetchDataFlg){
		Connection dbConnection = null;
		Statement sta =null;
		
		
		try {
			
			dbConnection = getSourceDBConnection1();
			if(dbConnection!=null){
			sta = dbConnection.createStatement();
			String Sql = "select * from item.dispensingunit order by dispensingunitid asc";
			ResultSet rs = sta.executeQuery(Sql);
			rs.setFetchSize(1000);
			
			if(rs!=null)
			{
				writeDatatoCSVFile(rs,csvFile);
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
		}
		
		try {
			
			pushPioneerPharmaCSVData(csvFile,tableName);
			System.out.println(new Timestamp(System.currentTimeMillis()));
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * fetchPioneerItemData
	 * @param csvFile
	 * @param tableName
	 * @param fetchDataFlg
	 * @throws SQLException
	 */
	public static void fetchPioneerItemData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
		

		System.out.println(new Timestamp(System.currentTimeMillis()));
		if(fetchDataFlg){
		Connection dbConnection = null;
		Statement sta =null;
		
		
		try {
			
			dbConnection = getSourceDBConnection1();
			if(dbConnection!=null){
			sta = dbConnection.createStatement();
			String Sql =  "select * from Item.Item order by itemid asc";
			ResultSet rs = sta.executeQuery(Sql);
			rs.setFetchSize(1000);
			
			if(rs!=null)
			{
				writeDatatoCSVFile(rs,csvFile);
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
		}
		
		try {
			
			pushPioneerPharmaCSVData(csvFile,tableName);
			System.out.println(new Timestamp(System.currentTimeMillis()));
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * fetchPioneerOriginTypeData
	 * @param csvFile
	 * @param tableName
	 * @param fetchDataFlg
	 * @throws SQLException
	 */
	public static void fetchPioneerOriginTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
		

		

		System.out.println(new Timestamp(System.currentTimeMillis()));
		if(fetchDataFlg){
		Connection dbConnection = null;
		Statement sta =null;
		
		
		try {
			
			dbConnection = getSourceDBConnection1();
			if(dbConnection!=null){
			sta = dbConnection.createStatement();
			String Sql =  "select * from prescription.origintype order by origintypeid asc";
			ResultSet rs = sta.executeQuery(Sql);
			rs.setFetchSize(1000);
			
			if(rs!=null)
			{
				writeDatatoCSVFile(rs,csvFile);
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
		}
		
		try {
			
			pushPioneerPharmaCSVData(csvFile,tableName);
			System.out.println(new Timestamp(System.currentTimeMillis()));
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	/**
	 * fetchPioneerPrescriptionItemTypeData
	 * @param csvFile
	 * @param tableName
	 * @param fetchDataFlg
	 * @throws SQLException
	 */
	public static void fetchPioneerPrescriptionItemTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
		

		

		System.out.println(new Timestamp(System.currentTimeMillis()));
		if(fetchDataFlg){
		Connection dbConnection = null;
		Statement sta =null;
		
		
		try {
			
			dbConnection = getSourceDBConnection1();
			if(dbConnection!=null){
			sta = dbConnection.createStatement();
			String Sql =  "select * from prescription.prescribeditemtype order by prescribeditemtypeid asc";
			ResultSet rs = sta.executeQuery(Sql);
			rs.setFetchSize(1000);
			
			if(rs!=null)
			{
				writeDatatoCSVFile(rs,csvFile);
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
		}
		
		try {
			
			pushPioneerPharmaCSVData(csvFile,tableName);
			System.out.println(new Timestamp(System.currentTimeMillis()));
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
/**
 * fetchPioneerPatientSyncStatusTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPioneerPatientSyncStatusTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
		

		

		System.out.println(new Timestamp(System.currentTimeMillis()));
		if(fetchDataFlg){
		Connection dbConnection = null;
		Statement sta =null;
		
		
		try {
			
			dbConnection = getSourceDBConnection1();
			if(dbConnection!=null){
			sta = dbConnection.createStatement();
			String Sql =  "select * from person.patientsyncstatustype order by SyncStatusTypeID asc";
			ResultSet rs = sta.executeQuery(Sql);
			rs.setFetchSize(1000);
			
			if(rs!=null)
			{
				writeDatatoCSVFile(rs,csvFile);
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
		}
		
		try {
			
			pushPioneerPharmaCSVData(csvFile,tableName);
			System.out.println(new Timestamp(System.currentTimeMillis()));
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
/**
 * fetchPioneerPatientRxNotifyTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPioneerPatientRxNotifyTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from person.PatientRxNotifyType order by RxNotifyTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPioneerPrescriberTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPioneerPrescriberTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	System.out.println(new Timestamp(System.currentTimeMillis()));

	if(fetchDataFlg){

	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from person.PrescriberType order by PrescriberTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPrescriberTypeSelectedCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPioneerPatientRXNotifyProviderTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPioneerPatientRXNotifyProviderTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql = "select * from Person.PersonSmsCarrierType order by smscarrierid asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPatientRXNotifyProviderTypeCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
/**
 * pushPioneerPatientRXNotifyProviderTypeCSVData
 * @param filePath
 * @param tablename
 * @throws SQLException
 */
public static void pushPioneerPatientRXNotifyProviderTypeCSVData(String filePath,String tablename) throws SQLException {

	Connection destinationDBConnection = null;
	Statement stmt =null;
	Statement tstmt =null; 

	try {
		
			destinationDBConnection = getDestinationDBConnection();
			
			tstmt= destinationDBConnection.createStatement();
			// Use TRUNCATE
		    String sql = "TRUNCATE `"+tablename+"`";
		    // Execute deletion
		    tstmt.executeUpdate(sql);
		    if(tstmt!=null)
		    	tstmt.close();
			    
		 	stmt=destinationDBConnection.createStatement();
		 	String quoteString="\"";
		 	String lineterminatedString="\\n";
			String hql="LOAD DATA local INFILE '"+filePath+"' INTO TABLE `" + tablename + "` FIELDS TERMINATED BY ',' ENCLOSED BY '"+quoteString+"' LINES TERMINATED BY '"+lineterminatedString+"' (@value1,@value2,@value3) set rxProviderNotifyTypeID=@value1 ,rxProviderNotifyTypeText=@value2;" ;
			System.out.println(hql);
			stmt.executeUpdate(hql);			

	} catch (Exception e) {

		System.out.println(e.getMessage());
		e.printStackTrace();

	}
	
}
/**
 * fetchPrescriptionRXStatusData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPrescriptionRXStatusData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from prescription.rxstatustype order by RxStatusTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPrescriptionRXTransactionStatusData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPrescriptionRXTransactionStatusData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from prescription.RxTransactionStatusType order by RxTransactionStatusTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPrescriptionPriorityTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPrescriptionPriorityTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from prescription.PriorityType order by PriorityTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPrescriptionDEAScheduleData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPrescriptionDEAScheduleData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from item.DEAScheduleType order by DEAScheduleTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPioneerDeliveryMethodData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPioneerDeliveryMethodData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from delivery.deliverymethod order by DeliveryMethodID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPersonPhoneTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPersonPhoneTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from person.phonetype order by PhoneTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchItemDeaOverrideData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchItemDeaOverrideData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from item.deaoverride order by deaoverrideID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaItemDeaOverrideCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * pushPioneerPharmaItemDeaOverrideCSVData
 * @param filePath
 * @param tablename
 * @throws SQLException
 */
public static void pushPioneerPharmaItemDeaOverrideCSVData(String filePath,String tablename) throws SQLException {

	Connection destinationDBConnection = null;
	Statement stmt =null;
	Statement tstmt =null; 

	try {
		
			destinationDBConnection = getDestinationDBConnection();
			
			tstmt= destinationDBConnection.createStatement();
			// Use TRUNCATE
		    String sql = "TRUNCATE `"+tablename+"`";
		    // Execute deletion
		    tstmt.executeUpdate(sql);
		    if(tstmt!=null)
		    	tstmt.close();
			    
		 	stmt=destinationDBConnection.createStatement();
		 	String quoteString="\"";
		 	String lineterminatedString="\\n";
			String hql="LOAD DATA local INFILE '"+filePath+"' INTO TABLE `" + tablename + "` FIELDS TERMINATED BY ',' ENCLOSED BY '"+quoteString+"' LINES TERMINATED BY '"+lineterminatedString+"' "
					+ "(deaoverrideid,gcn,statecode,locationid,deaschedule,drugofconcern,@changedon,changedby,changedat,itemid,@changedonutc,@centralchangedonutc,applytoexpirationdate,applytorefills,applytopartialfills,applytotransfers)" 
					+ "set changedon=STR_TO_DATE(@changedon,'%d-%M-%Y'),changedonutc=STR_TO_DATE(@changedonutc, '%d-%M-%Y'),centralchangedonutc=STR_TO_DATE(@centralchangedonutc, '%d-%M-%Y')" ;
			/**
			 * LOAD DATA local INFILE 'f:\\pioneerdata\\pioneer.item.deaoverride.csv' INTO TABLE `pioneer.item.deaoverride` 
FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' 
(deaoverrideid,gcn,statecode,locationid,deaschedule,drugofconcern,@changedon,changedby,changedat,itemid,@changedonutc,@centralchangedonutc,applytoexpirationdate,applytorefills,applytopartialfills,applytotransfers) 
set changedon=STR_TO_DATE(@changedon,'%d-%M-%Y'),
changedonutc=STR_TO_DATE(@changedonutc, '%d-%M-%Y'),
centralchangedonutc=STR_TO_DATE(@centralchangedonutc, '%d-%M-%Y');
			 */
			System.out.println(hql);
			stmt.executeUpdate(hql);			

	} catch (Exception e) {

		System.out.println(e.getMessage());
		e.printStackTrace();

	}
	
}
/**
 * fetchPOSDeliveryStatusData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPOSDeliveryStatusData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from PointOfSale.DeliveryStatusType order by DeliveryStatusTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPOSPaymentTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPOSPaymentTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from PointOfSale.PaymentType order by PaymentTypeEnum asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPOSShipperStatusTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPOSShipperStatusTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from PointOfSale.ShipperStatusType order by ShipperStatusTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
/**
 * fetchPOSShipperTypeData
 * @param csvFile
 * @param tableName
 * @param fetchDataFlg
 * @throws SQLException
 */
public static void fetchPOSShipperTypeData(String csvFile,String tableName,boolean fetchDataFlg) throws SQLException {
	

	

	System.out.println(new Timestamp(System.currentTimeMillis()));
	if(fetchDataFlg){
	Connection dbConnection = null;
	Statement sta =null;
	
	
	try {
		
		dbConnection = getSourceDBConnection1();
		if(dbConnection!=null){
		sta = dbConnection.createStatement();
		String Sql =  "select * from PointOfSale.ShipperType order by ShipperTypeID asc";
		ResultSet rs = sta.executeQuery(Sql);
		rs.setFetchSize(1000);
		
		if(rs!=null)
		{
			writeDatatoCSVFile(rs,csvFile);
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
	}
	
	try {
		
		pushPioneerPharmaCSVData(csvFile,tableName);
		System.out.println(new Timestamp(System.currentTimeMillis()));
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
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

	public static void FetchPioneerPharmacyTables()
	{


		try {
			String drivePath=pioneer_data_folderpath;
			File nFile=new File(pioneer_data_folderpath);
			if(!nFile.exists())
				nFile.mkdirs();
			
			
			//[PioneerPharmacySystem_Test].[Person].[PrescriberType]
			//fetchPioneerPrescriberTypeData(drivePath+"prescriber_type_master.csv","prescriber_type_master",false);
			
			//[PioneerPharmacySystem_Test].[Person].[PatientRxNotifyType]
			//fetchPioneerPatientRxNotifyTypeData(drivePath+"pioneer.person.patientrxnotifytype.csv","pioneer.person.patientrxnotifytype",false);
			
			//[PioneerPharmacySystem_Test].[Person].[PatientSyncStatusType]
//			fetchPioneerPatientSyncStatusTypeData(drivePath+"pioneer.person.patientsyncstatustype.csv","pioneer.person.patientsyncstatustype",false);		
			
			
			//[PioneerPharmacySystem_Test].[Prescription].[PrescribedItemType]
//			fetchPioneerPrescriptionItemTypeData(drivePath+"pioneer.prescription.prescribeditemtype.csv","pioneer.prescription.prescribeditemtype",false);
			
			//[PioneerPharmacySystem_Test].[Prescription].[OriginType]
//			fetchPioneerOriginTypeData(drivePath+"pioneer.prescription.origintype.csv","pioneer.prescription.origintype",false);
			
			//[PioneerPharmacySystem_Test].[Item].[DispensingUnit]
//			fetchPioneerDispensingUnitData(drivePath+"pioneer.item.dispensingunit.csv","pioneer.item.dispensingunit",false);
			
			
			//[PioneerPharmacySystem_Test].[Person].[PersonSmsCarrierType]
//			fetchPioneerPatientRXNotifyProviderTypeData(drivePath+"pioneer.person.patientrxnotifyprovidertype.csv","pioneer.person.patientrxnotifyprovidertype",false);
			

			//[PioneerPharmacySystem_Test].[Prescription].[PriorityType]
//			fetchPrescriptionPriorityTypeData(drivePath+"pioneer.prescription.prioritytype.csv","pioneer.prescription.prioritytype",false);
			
			//[PioneerPharmacySystem_Test].[Item].[DEAScheduleType]
//			fetchPrescriptionDEAScheduleData(drivePath+"pioneer.item.deascheduletype.csv","pioneer.item.deascheduletype",false);
			
			//[PioneerPharmacySystem_Test].[delivery].[deliverymethod]
			//fetchPioneerDeliveryMethodData(drivePath+"pioneer.delivery.deliverymethod.csv","pioneer.delivery.deliverymethod",false);
			
			//[PioneerPharmacySystem_Test].[person].[PhoneType]
//			fetchPersonPhoneTypeData(drivePath+"pioneer.person.phonetype.csv","pioneer.person.phonetype",false);
			
/****************************************************************Frequently Pushing the Following Tables*********************************************************************/
			
			//[PioneerPharmacySystem_Test].[Prescription].[RxStatusType]
			//fetchPrescriptionRXStatusData(drivePath+"pioneer.prescription.rxstatustype.csv","pioneer.prescription.rxstatustype",false);

			//[PioneerPharmacySystem_Test].[Prescription].[RxTransactionStatusType]
			//fetchPrescriptionRXTransactionStatusData(drivePath+"pioneer.prescription.rxtransactionstatustype.csv","pioneer.prescription.rxtransactionstatustype",false);
			
			//[PioneerPharmacySystem_Test].[PointOfSale].[DeliveryStatusType]
			//fetchPOSDeliveryStatusData(drivePath+"pioneer.pointofsale.deliverystatustype.csv","pioneer.pointofsale.deliverystatustype",false);
			
			//[PioneerPharmacySystem_Test].[PointOfSale].[PaymentType]
			//fetchPOSPaymentTypeData(drivePath+"pioneer.pointofsale.paymenttype.csv","pioneer.pointofsale.paymenttype",false);
			
			//[PioneerPharmacySystem_Test].[PointOfSale].[ShipperStatusType]
			//fetchPOSShipperStatusTypeData(drivePath+"pioneer.pointofsale.shipperstatustype.csv","pioneer.pointofsale.shipperstatustype",false);
			
			//[PioneerPharmacySystem_Test].[PointOfSale].[ShipperType]
			//fetchPOSShipperTypeData(drivePath+"pioneer.pointofsale.shippertype.csv","pioneer.pointofsale.shippertype",false);
			
			/*********************************************************************************************************************/
			
			//[PioneerPharmacySystem_Test].[Item].[DeaOverride]
			fetchItemDeaOverrideData(drivePath+"pioneer.item.deaoverride.csv","pioneer.item.deaoverride",true);
			
			//[PioneerPharmacySystem_Test].[Item].[Item]
			fetchPioneerItemData(drivePath+"pioneer.item.item.csv","pioneer.item.item",true);
			
			//[PioneerRxCatalog].[Diagnosis].[ICD10]
			fetchPioneerICD10CodeData(drivePath+"pioneer.diagnosis.icd10.csv","pioneer.diagnosis.icd10",true);

			//[PioneerPharmacySystem_Test].[Prescription].[Sig]
			fetchPrescriptionSigData(drivePath+"pioneer.prescription.sig.csv","pioneer.prescription.sig",true);
			
			//[PioneerRxCatalog].[Fdb].[MEDMedication]
			fetchPioneerFDBMedicationCodeData(drivePath+"pioneer.fdb.medmedication.csv","pioneer.fdb.medmedication",true);
			

			
		} catch (Exception e) {

			System.out.println(e.getMessage());

		}

	
		
	}

	public static void main(String[] argv) {

		try {
			/*String drivePath=pioneer_data_folderpath;
			File nFile=new File(pioneer_data_folderpath);
			if(!nFile.exists())
				nFile.mkdirs();*/
			
			
			//[PioneerPharmacySystem_Test].[Person].[PrescriberType]
			//fetchPioneerPrescriberTypeData(drivePath+"prescriber_type_master.csv","prescriber_type_master",false);
			
			//[PioneerPharmacySystem_Test].[Person].[PatientRxNotifyType]
			//fetchPioneerPatientRxNotifyTypeData(drivePath+"pioneer.person.patientrxnotifytype.csv","pioneer.person.patientrxnotifytype",false);
			
			//[PioneerPharmacySystem_Test].[Person].[PatientSyncStatusType]
//			fetchPioneerPatientSyncStatusTypeData(drivePath+"pioneer.person.patientsyncstatustype.csv","pioneer.person.patientsyncstatustype",false);		
			
			
			//[PioneerPharmacySystem_Test].[Prescription].[PrescribedItemType]
//			fetchPioneerPrescriptionItemTypeData(drivePath+"pioneer.prescription.prescribeditemtype.csv","pioneer.prescription.prescribeditemtype",false);
			
			//[PioneerPharmacySystem_Test].[Prescription].[OriginType]
//			fetchPioneerOriginTypeData(drivePath+"pioneer.prescription.origintype.csv","pioneer.prescription.origintype",false);
			
			//[PioneerPharmacySystem_Test].[Item].[DispensingUnit]
//			fetchPioneerDispensingUnitData(drivePath+"pioneer.item.dispensingunit.csv","pioneer.item.dispensingunit",false);
			
			
			//[PioneerPharmacySystem_Test].[Person].[PersonSmsCarrierType]
//			fetchPioneerPatientRXNotifyProviderTypeData(drivePath+"pioneer.person.patientrxnotifyprovidertype.csv","pioneer.person.patientrxnotifyprovidertype",false);
			

			//[PioneerPharmacySystem_Test].[Prescription].[PriorityType]
//			fetchPrescriptionPriorityTypeData(drivePath+"pioneer.prescription.prioritytype.csv","pioneer.prescription.prioritytype",false);
			
			//[PioneerPharmacySystem_Test].[Item].[DEAScheduleType]
//			fetchPrescriptionDEAScheduleData(drivePath+"pioneer.item.deascheduletype.csv","pioneer.item.deascheduletype",false);
			
			//[PioneerPharmacySystem_Test].[delivery].[deliverymethod]
			//fetchPioneerDeliveryMethodData(drivePath+"pioneer.delivery.deliverymethod.csv","pioneer.delivery.deliverymethod",false);
			
			//[PioneerPharmacySystem_Test].[person].[PhoneType]
//			fetchPersonPhoneTypeData(drivePath+"pioneer.person.phonetype.csv","pioneer.person.phonetype",false);
			
/****************************************************************Frequently Pushing the Following Tables*********************************************************************/
			
			//[PioneerPharmacySystem_Test].[Item].[DeaOverride]
			//fetchItemDeaOverrideData(drivePath+"pioneer.item.deaoverride.csv","pioneer.item.deaoverride",false);
			
			//[PioneerPharmacySystem_Test].[Prescription].[RxStatusType]
			//fetchPrescriptionRXStatusData(drivePath+"pioneer.prescription.rxstatustype.csv","pioneer.prescription.rxstatustype",false);

			//[PioneerPharmacySystem_Test].[Prescription].[RxTransactionStatusType]
			//fetchPrescriptionRXTransactionStatusData(drivePath+"pioneer.prescription.rxtransactionstatustype.csv","pioneer.prescription.rxtransactionstatustype",false);
			
			//[PioneerPharmacySystem_Test].[PointOfSale].[DeliveryStatusType]
			//fetchPOSDeliveryStatusData(drivePath+"pioneer.pointofsale.deliverystatustype.csv","pioneer.pointofsale.deliverystatustype",false);
			
			//[PioneerPharmacySystem_Test].[PointOfSale].[PaymentType]
			//fetchPOSPaymentTypeData(drivePath+"pioneer.pointofsale.paymenttype.csv","pioneer.pointofsale.paymenttype",false);
			
			//[PioneerPharmacySystem_Test].[PointOfSale].[ShipperStatusType]
			//fetchPOSShipperStatusTypeData(drivePath+"pioneer.pointofsale.shipperstatustype.csv","pioneer.pointofsale.shipperstatustype",false);
			
			//[PioneerPharmacySystem_Test].[PointOfSale].[ShipperType]
			//fetchPOSShipperTypeData(drivePath+"pioneer.pointofsale.shippertype.csv","pioneer.pointofsale.shippertype",false);
			
			//[PioneerPharmacySystem_Test].[Item].[Item]
			//fetchPioneerItemData(drivePath+"pioneer.item.item.csv","pioneer.item.item",true);
			
			//[PioneerRxCatalog].[Diagnosis].[ICD10]
			//fetchPioneerICD10CodeData(drivePath+"pioneer.diagnosis.icd10.csv","pioneer.diagnosis.icd10",true);

			//[PioneerPharmacySystem_Test].[Prescription].[Sig]
			//fetchPrescriptionSigData(drivePath+"pioneer.prescription.sig.csv","pioneer.prescription.sig",true);
			
			//[PioneerRxCatalog].[Fdb].[MEDMedication]
			//fetchPioneerFDBMedicationCodeData(drivePath+"pioneer.fdb.medmedication.csv","pioneer.fdb.medmedication",true);
			

			
		} catch (Exception e) {

			System.out.println(e.getMessage());

		}

	}
}
