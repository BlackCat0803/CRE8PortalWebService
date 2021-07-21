package com.pharma.core.webservice.model;

import com.opencsv.CSVWriter;
import com.pharma.core.util.PropertyConfigurer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.Date;

/**
 * The class <<PharmacyPOSScheduler>> is a Scheduler class for Tracking Shipment information for latest shipment of an rx/rf from Pioneer. 
 * For each Rx-Refill, Shipment information is tracked from point of sale ticket where the tracking information on that ticket for the corresponding rx/refill is captured.
 * The information for the Updating the POS Details is stopped, if the RX reached the Shipment Status ==> SHIPMENT_VOIDED/DELIVERED/MANUAL_SHIPMENT.
 * The POS details is updated to CRE8Portal tables - order_master, order_transaction, invoice_master, invoice_transaction
 * Invoice Payment Status is set to "Paid" when there is a POS entry in the Pioneer System

 */
public class PharmacyPOSScheduler {

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
	  * Shipment Tracking information in PioneerRx-Dec 5, 2017
		Tables you are looking for are:
		PointOfSale.Shipment
		PointOfSale.ShipperStatusType
		PointOfSale.SaleTransactionDeliveryAddressView
		PointOfSale.SaleTransactionShippingAddressView
		 
		The script below, shows you how to tie Rx-Refill to a point of sale ticket and see the tracking information on that ticket that the rx/refill is on.
		 
		Mitch
		 
		 
		--select *
		--    from PointOfSale.Shipment a (nolock)
		--select *
		--    from PointOfSale.SaleTransactionDeliveryAddressView a (nolock)
		--select *
		--    from PointOfSale.SaleTransactionShippingAddressView a (nolock)
		     
		     
		declare @RxNumber int = 145733, @RefillNumber int = 1
		     
		select
		--    Top 1 -- only reuturn the latest shipment of an rx/rf
		  Rx.RxNumber
		 ,rxtrans.RefillNumber
		,ship.CreatedOn
		,ship.ShipperName
		,ship.TrackingNumber
		 ,ship.TrackingNumberSecondary
		,ship.ShipperStatusTypeID
		,shipstatus.ShipperStatusTypeText
		,ship.UpdatedOn
		,delview.*
		,shipview.*
		  FROM
		Prescription.Rx Rx (nolock)
		join Prescription.RxTransaction rxtrans (nolock) on rxtrans.RxID =  Rx.RxID
		join PointOfSale.SaleTransactionDetail saledetail (nolock) on saledetail.ReferenceID = rxtrans.RxTransactionID
		            AND saledetail.TransactionTypeID in (9) -- Only want sale detal if Rx is on it
		join PointOfSale.SaleTransaction saletrans (nolock) on saletrans.SaleTransactionID = saledetail.SaleTransactionID
		            AND saletrans.TransactionStatusID NOT in (34,35) --eliminate voided rx transaction
		join PointOfSale.Shipment ship (nolock) on ship.SaleReceiptString = CONVERT(varchar(20), saletrans.SaleReceiptNumber )
		join PointOfSale.ShipperStatusType shipstatus (nolock) on ship.ShipperStatusTypeID = shipstatus.ShipperStatusTypeID
		left join PointOfSale.SaleTransactionDeliveryAddressView delview (nolock) on saletrans.SaleTransactionID = delview.SaleTransactionID
		left join PointOfSale.SaleTransactionShippingAddressView shipview (nolock) on saletrans.SaleTransactionID = shipview.SaleTransactionID
		where 1=1
		--    and Rx.RxNumber = @RxNumber
		--    and rxtrans.RefillNumber = @RefillNumber
		order by ship.CreatedOn desc
	  * @throws SQLException
	  */
	 public static void fetchPOSShipmentTrackingInfo() throws Exception {
			

			

			System.out.println(new Timestamp(System.currentTimeMillis()));
			
			Connection conn = getDestinationDBConnection();
			Connection dbConnection = getSourceDBConnection1();
			Statement sta =null;
			
			
			JSONArray jsonArr=new JSONArray();
			String RxNumber="",RefillNumber="",ShipperName="",TrackingNumber="",ShipperStatusTypeText="",SaleTransactionID="",SaleReceiptNumber="",
					OrderNum="",Barcode="",delivery_PatientFullNameFirstThenLast="",delivery_PatientAddress="",delivery_PatientCityStateZip="",delivery_City="",
					delivery_StateCode="",delivery_ZipCode="",delivery_PatientPhone="",delivery_Email="",shipping_PatientFullNameFirstThenLast="",shipping_PatientAddress="",
					shipping_PatientCityStateZip="",shipping_City="",shipping_StateCode="",shipping_ZipCode="",shipping_PatientPhone="",shipping_Email="",
					TransactionAmount="",TaxAmount="",Quantity="",TransactionLineTotal="", PaymentTypeEnum="",PaymentTypeText="";
			Date paymentdate=null;
			try {
				
			  if(conn!=null && dbConnection!=null){
				sta = dbConnection.createStatement();
				String Sql =  "select  Rx.RxNumber ,rxtrans.RefillNumber,ship.CreatedOn,ship.ShipperName,ship.TrackingNumber ,ship.TrackingNumberSecondary,ship.ShipperStatusTypeID,shipstatus.ShipperStatusTypeText,ship.UpdatedOn,delview.*,shipview.* ,"
						+ " saletrans.TransactionAmount,saletrans.TaxAmount,saledetail.Quantity,saledetail.TransactionLineTotal FROM "
						+ "Prescription.Rx Rx (nolock) join Prescription.RxTransaction rxtrans (nolock) on rxtrans.RxID =  Rx.RxID join PointOfSale.SaleTransactionDetail saledetail (nolock) on saledetail.ReferenceID = rxtrans.RxTransactionID AND "
						+ "saledetail.TransactionTypeID in (9) join PointOfSale.SaleTransaction saletrans (nolock) on saletrans.SaleTransactionID = saledetail.SaleTransactionID AND saletrans.TransactionStatusID NOT in (34,35) join "
						+ "PointOfSale.Shipment ship (nolock) on ship.SaleReceiptString = CONVERT(varchar(20), saletrans.SaleReceiptNumber ) join PointOfSale.ShipperStatusType shipstatus (nolock) on ship.ShipperStatusTypeID = shipstatus.ShipperStatusTypeID "
						+ "left join PointOfSale.SaleTransactionDeliveryAddressView delview (nolock) on saletrans.SaleTransactionID = delview.SaleTransactionID left join PointOfSale.SaleTransactionShippingAddressView shipview (nolock) on "
						+ "saletrans.SaleTransactionID = shipview.SaleTransactionID order by ship.CreatedOn desc";

				ResultSet rs = sta.executeQuery(Sql);
				rs.setFetchSize(1000);
				
				while(rs.next())
				{
					
					RxNumber="";RefillNumber="";ShipperName="";TrackingNumber="";ShipperStatusTypeText="";SaleTransactionID="";SaleReceiptNumber="";
							OrderNum="";Barcode="";delivery_PatientFullNameFirstThenLast="";delivery_PatientAddress="";delivery_PatientCityStateZip="";delivery_City="";
							delivery_StateCode="";delivery_ZipCode="";delivery_PatientPhone="";delivery_Email="";shipping_PatientFullNameFirstThenLast="";shipping_PatientAddress="";
							shipping_PatientCityStateZip="";shipping_City="";shipping_StateCode="";shipping_ZipCode="";shipping_PatientPhone="";shipping_Email=""; PaymentTypeEnum="";PaymentTypeText="";
					
							
					ShipperStatusTypeText=rs.getString("ShipperStatusTypeText");
					
					RxNumber=rs.getString("RxNumber");
					System.out.println("RxNumber ======"+RxNumber+" ::  ShipperStatusTypeText  :: "+ShipperStatusTypeText);
					
					//Stop the Updation of POS Details if the RX reached the Shipment Status ==> SHIPMENT_VOIDED/DELIVERED/MANUAL_SHIPMENT
					if(ShipperStatusTypeText!=null && ShipperStatusTypeText.length()>0
							&& !ShipperStatusTypeText.equalsIgnoreCase("SHIPMENT_VOIDED")  && !ShipperStatusTypeText.equalsIgnoreCase("DELIVERED")  && !ShipperStatusTypeText.equalsIgnoreCase("MANUAL_SHIPMENT"))
					{
					
					RefillNumber=rs.getString("RefillNumber");
					ShipperName=rs.getString("ShipperName");
					TrackingNumber=rs.getString("TrackingNumber");
					
					SaleTransactionID=rs.getString("SaleTransactionID");
					SaleReceiptNumber=rs.getString("SaleReceiptNumber");
					OrderNum=rs.getString("OrderNum");
					Barcode=rs.getString("Barcode");
					delivery_PatientFullNameFirstThenLast=rs.getString("PatientFullNameFirstThenLast");
					delivery_PatientAddress=rs.getString("PatientAddress");
					delivery_PatientCityStateZip=rs.getString("PatientCityStateZip");
					delivery_City=rs.getString("City");
					delivery_StateCode=rs.getString("StateCode");;
					delivery_ZipCode=rs.getString("ZipCode");
					delivery_PatientPhone=rs.getString("PatientPhone");
					delivery_Email=rs.getString("Email");
					shipping_PatientFullNameFirstThenLast=rs.getString("PatientFullNameFirstThenLast");
					shipping_PatientAddress=rs.getString("PatientAddress");
					shipping_PatientCityStateZip=rs.getString("PatientCityStateZip");
					shipping_City=rs.getString("City");
					shipping_StateCode=rs.getString("StateCode");
					shipping_ZipCode=rs.getString("ZipCode");
					shipping_PatientPhone=rs.getString("PatientPhone");
					shipping_Email=rs.getString("Email");
					TransactionAmount=rs.getString("TransactionAmount");
					TaxAmount=rs.getString("TaxAmount");
					Quantity=rs.getString("Quantity");
					TransactionLineTotal=rs.getString("TransactionLineTotal");
					
						
					String updateTableSQL3 = "UPDATE order_master SET "
			    			+ "shipping_address=?,shipping_city=?,shipping_state=?,shipping_zip_code=?,"
			    			+ "last_updated_by=?,last_updated_user=?,last_updated_date=? "
			    					+ " WHERE order_id in (select order_id from order_transaction where rx_number = ?)";
			    			PreparedStatement upreparedStatement3 = conn.prepareStatement(updateTableSQL3);
			    			upreparedStatement3.setObject(1, getFilteredText(shipping_PatientAddress));
					    	upreparedStatement3.setObject(2, getFilteredText(shipping_City));;
					    	upreparedStatement3.setObject(3, getFilteredText(shipping_StateCode));
					    	upreparedStatement3.setObject(4, getFilteredText(shipping_ZipCode));
					    	upreparedStatement3.setObject(5, "0");
					    	upreparedStatement3.setObject(6, "RX");
					    	upreparedStatement3.setObject(7, getFilteredText(getCurrentTimeStamp()));
					    	upreparedStatement3.setObject(8, getFilteredText(RxNumber));
					    	
					    	// execute update SQL stetement
					    	upreparedStatement3 .executeUpdate();
			    			
			    			String updateTableSQL2 = "UPDATE order_transaction SET "
			    					+ "tracking_number=?,shipmentstatus=?,shippingcompany=?"
			    			+ " WHERE rx_number = ?";
			    			PreparedStatement upreparedStatement2 = conn.prepareStatement(updateTableSQL2);
			    			upreparedStatement2.setObject(1, getFilteredText(TrackingNumber));
					    	upreparedStatement2.setObject(2, getFilteredText(ShipperStatusTypeText));;
					    	upreparedStatement2.setObject(3, getFilteredText(ShipperName));
					    	upreparedStatement2.setObject(4, getFilteredText(RxNumber));
					    
			    			// execute update SQL stetement
					    	upreparedStatement2 .executeUpdate();
					    	
					    	
					    	//Update New Invoice
					    	
					    	String updateTableSQL4 = "UPDATE invoice_master SET "
					    			+ "shipping_name=?,shipping_address=?,shipping_city=?,shipping_state=?,shipping_zipcode=?,"
					    			+ "subtotal=?,tax=?,total=?,"
					    			+ "last_updated_by=?,last_updated_user=?,last_updated_date=? "
					    			+ " WHERE invoice_id in (select invoice_id from invoice_transaction where rx_number = ?)";
					    	
					    	PreparedStatement upreparedStatement4 = conn.prepareStatement(updateTableSQL4);
					    	upreparedStatement4.setObject(1, getFilteredText(shipping_PatientFullNameFirstThenLast));
					    	upreparedStatement4.setObject(2, getFilteredText(shipping_PatientAddress));
					    	upreparedStatement4.setObject(3, getFilteredText(shipping_City));;
					    	upreparedStatement4.setObject(4, getFilteredText(shipping_StateCode));
					    	upreparedStatement4.setObject(5, getFilteredText(shipping_ZipCode));
					    	
					    	upreparedStatement4.setObject(6, new Double(TransactionLineTotal));
					    	upreparedStatement4.setObject(7, new Double(TaxAmount));
					    	
					    	upreparedStatement4.setObject(8, new Double(TransactionAmount));
					    	
					    	upreparedStatement4.setObject(9, "0");
					    	upreparedStatement4.setObject(10, "RX");
					    	upreparedStatement4.setObject(11, getFilteredText(getCurrentTimeStamp()));
					    	
					    	upreparedStatement4.setObject(12, getFilteredText(RxNumber));
					    	
					    	// execute update SQL stetement
					    	upreparedStatement4 .executeUpdate();
					    	
					    	String updateTableSQL5 = "UPDATE invoice_transaction SET "
					    			+ "quantity=?,total=?,numberOfRefillsFilled=? "
					    			+ " WHERE rx_number = ?";
					    	PreparedStatement upreparedStatement5 = conn.prepareStatement(updateTableSQL5);
					    	upreparedStatement5.setObject(1, getFilteredText(Quantity));
					    	upreparedStatement5.setObject(2, getFilteredText(TransactionLineTotal));
					    	upreparedStatement5.setObject(3, getFilteredText(RefillNumber));
					    	upreparedStatement5.setObject(4, getFilteredText(RxNumber));
					    	
					    	// execute update SQL stetement
					    	upreparedStatement5.executeUpdate();
					    	
					    	

					    	
							String selectSQL =  "select paymentdetail.SaleTransactionID,paymentdetail.PaymentTypeEnum,paymenttype.PaymentTypeText,paymentdetail.postingdate "
									+ "from PointOfSale.Payment paymentdetail,PointOfSale.PaymentType paymenttype where "
									+ "paymenttype.PaymentTypeEnum=paymentdetail.PaymentTypeEnum  and paymentdetail.SaleTransactionID=?";
							PreparedStatement preparedStatement2 = dbConnection.prepareStatement(selectSQL);
							preparedStatement2.setString(1, SaleTransactionID);
							
							ResultSet rs2 = preparedStatement2.executeQuery();
							while(rs2.next())
							{
								PaymentTypeEnum=getFilteredText(rs2.getString("PaymentTypeEnum"));
								PaymentTypeText=getFilteredText(rs2.getString("PaymentTypeText"));
								paymentdate=rs2.getTimestamp("postingdate");
							}
							
							if(rs2!=null)
								rs2.close();
							
							
							if(PaymentTypeText!=null && PaymentTypeText.length()>0){
								String updateTableSQL6 = "UPDATE invoice_transaction SET "
						    			+ "paymentstatus=?,paymenttype=?,paymentdate=? "
						    			+ " WHERE rx_number = ? and (paymentstatus<>'Paid')";
						    	PreparedStatement upreparedStatement6 = conn.prepareStatement(updateTableSQL6);
						    	upreparedStatement6.setObject(1, getFilteredText("Paid"));
						    	upreparedStatement6.setObject(2, getFilteredText(PaymentTypeText));
						    	upreparedStatement6.setObject(3, paymentdate);
						    	upreparedStatement6.setObject(4, getFilteredText(RxNumber));
						    	
						    	// execute update SQL stetement
						    	int rowCnt=upreparedStatement6.executeUpdate();
						    	
						    	if(rowCnt>0)
						    	{
						    		
						    		//Update New Invoice
							    	
							    	String updateTableSQL7 = "UPDATE invoice_master SET "
							    			+ "last_updated_by=?,last_updated_user=?,last_updated_date=? "
							    			+ " WHERE invoice_id in (select invoice_id from invoice_transaction where rx_number = ?)";
							    	
							    	PreparedStatement upreparedStatement7 = conn.prepareStatement(updateTableSQL7);
							    	
							    	upreparedStatement7.setObject(1, "0");
							    	upreparedStatement7.setObject(2, "RX");
							    	upreparedStatement7.setObject(3, getFilteredText(getCurrentTimeStamp()));
							    	upreparedStatement7.setObject(4, getFilteredText(RxNumber));
							    	
							    	// execute update SQL stetement
							    	upreparedStatement7 .executeUpdate();
						    	}
							}
						}//ShipperStatusTypeText!=null && ShipperStatusTypeText.length()>0	
					}    	
				
				
				if(rs!=null)
					rs.close();
				
				
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
	     * Convert a result set into a JSON Array
	     * @param resultSet
	     * @return a JSONArray
	     * @throws Exception
	     */
	    public static JSONArray convertToJSON(ResultSet resultSet)
	            throws Exception {
	        JSONArray jsonArray = new JSONArray();
	        while (resultSet.next()) {
	            int total_rows = resultSet.getMetaData().getColumnCount();
	            for (int i = 0; i < total_rows; i++) {
	                JSONObject obj = new JSONObject();
	                obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
	                        .toLowerCase(), resultSet.getObject(i + 1));
	                jsonArray.put(obj);
	            }
	        }
	        return jsonArray;
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
	public static void main(String[] argv) {

		try {
			
			
			fetchPOSShipmentTrackingInfo();
			
			
		} catch (Exception e) {

			System.out.println(e.getMessage());

		}

	}
}
