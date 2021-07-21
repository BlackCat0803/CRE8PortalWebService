package com.pharma.core.form;

import java.util.Date;

public class PharamaRXForm {

	int patientid=0,physicianid=0,group_id=0,physicianClinicId=0,physicianGroupId=0;
	String physicianName="",patientName="";
	String prescribedItemName="";
	Date dateWritten=null;
	String shipping_address="",shipping_city="",shipping_state="",shipping_zip_code="";
	String phy_address="",phy_city="",phy_state="",phy_zip_code="",phy_country="",phy_phone="",phy_dea=""  ,phy_npi ="" ,phy_upin ="" ,
			phy_state_license ="" ,phy_medicaid ="" , patient_mobile  =""  ,patient_ssn  =""  ,allergies =""   ,diagnosis ="" ;
	Object patient_date_of_birth ="";
	String phy_firstname="", phy_lastname="",phy_middlename="",phy_fullname="";
	
	String rxNumber="",CurrentRxStatusText="",RefillsRemaining="",NumberOfRefillsFilled="",LastFillDateTime="",CurrentRxTransactionStatusText="",
			QuantityRemaining="",NumberOfRefillsAllowed="",CompletedDate="",TrackingNumber="",RxPioneerRxID="",RxFillTransactionPioneerRxID="",
					PreviousRxNumber="",PrescriberOrderNumber="",FutureFill="",PriorityTypeText="",ScriptType="",RefillNumber="";
	String prescribedWrittenName="",prescribedGenericName="",prescribedDrugStrength="",prescribedQuantity="",prescribedUnitText="";
	String dispensedDrugName="",dispensedDrugStrength="",dispensedQuantity="",dispensedUnitText="",dispensedDaysSupply="",dispensedLotNumber="",dispensedLotExpirationDate="";
	String RxCommentsCritical="",FillCommentsCritical="",OriginTypeID="",dispensedUnitID="",dispensedSigCode="",dispensedSigCodeID="",deaschedule="";
	String TransmissionDate="",GrossAmountSubmitted="0",PatientPayAmountPaid="0",PercentageSalesTaxAmountPaid="0",PercentageSalesTaxAmountSubmitted="0",
			PercentageSalesTaxRatePaid="0",PercentageSalesTaxRateSubmitted="0",FlatSalesTaxAmountPaid="0",FlatSalesTaxAmountSubmitted="0",dispensedDrugPioneerItemId="";
	
	String DateFilledUTC="",ExpirationDateUTC="",DateWrittenUTC="";
	

	public int getPatientid() {
		return patientid;
	}

	public void setPatientid(int patientid) {
		this.patientid = patientid;
	}

	public int getPhysicianid() {
		return physicianid;
	}

	public void setPhysicianid(int physicianid) {
		this.physicianid = physicianid;
	}

	public int getGroup_id() {
		return group_id;
	}

	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}

	public int getPhysicianClinicId() {
		return physicianClinicId;
	}

	public void setPhysicianClinicId(int physicianClinicId) {
		this.physicianClinicId = physicianClinicId;
	}

	public int getPhysicianGroupId() {
		return physicianGroupId;
	}

	public void setPhysicianGroupId(int physicianGroupId) {
		this.physicianGroupId = physicianGroupId;
	}

	public String getPhysicianName() {
		return physicianName;
	}

	public void setPhysicianName(String physicianName) {
		this.physicianName = physicianName;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getPrescribedItemName() {
		return prescribedItemName;
	}

	public void setPrescribedItemName(String prescribedItemName) {
		this.prescribedItemName = prescribedItemName;
	}

	public Date getDateWritten() {
		return dateWritten;
	}

	public void setDateWritten(Date dateWritten) {
		this.dateWritten = dateWritten;
	}

	public String getShipping_address() {
		return shipping_address;
	}

	public void setShipping_address(String shipping_address) {
		this.shipping_address = shipping_address;
	}

	public String getShipping_city() {
		return shipping_city;
	}

	public void setShipping_city(String shipping_city) {
		this.shipping_city = shipping_city;
	}

	public String getShipping_state() {
		return shipping_state;
	}

	public void setShipping_state(String shipping_state) {
		this.shipping_state = shipping_state;
	}

	public String getShipping_zip_code() {
		return shipping_zip_code;
	}

	public void setShipping_zip_code(String shipping_zip_code) {
		this.shipping_zip_code = shipping_zip_code;
	}

	public String getPhy_address() {
		return phy_address;
	}

	public void setPhy_address(String phy_address) {
		this.phy_address = phy_address;
	}

	public String getPhy_city() {
		return phy_city;
	}

	public void setPhy_city(String phy_city) {
		this.phy_city = phy_city;
	}

	public String getPhy_state() {
		return phy_state;
	}

	public void setPhy_state(String phy_state) {
		this.phy_state = phy_state;
	}

	public String getPhy_zip_code() {
		return phy_zip_code;
	}

	public void setPhy_zip_code(String phy_zip_code) {
		this.phy_zip_code = phy_zip_code;
	}

	public String getPhy_country() {
		return phy_country;
	}

	public void setPhy_country(String phy_country) {
		this.phy_country = phy_country;
	}

	public String getPhy_phone() {
		return phy_phone;
	}

	public void setPhy_phone(String phy_phone) {
		this.phy_phone = phy_phone;
	}

	public String getPhy_dea() {
		return phy_dea;
	}

	public void setPhy_dea(String phy_dea) {
		this.phy_dea = phy_dea;
	}

	public String getPhy_npi() {
		return phy_npi;
	}

	public void setPhy_npi(String phy_npi) {
		this.phy_npi = phy_npi;
	}

	public String getPhy_upin() {
		return phy_upin;
	}

	public void setPhy_upin(String phy_upin) {
		this.phy_upin = phy_upin;
	}

	public String getPhy_state_license() {
		return phy_state_license;
	}

	public void setPhy_state_license(String phy_state_license) {
		this.phy_state_license = phy_state_license;
	}

	public String getPhy_medicaid() {
		return phy_medicaid;
	}

	public void setPhy_medicaid(String phy_medicaid) {
		this.phy_medicaid = phy_medicaid;
	}

	public String getPatient_mobile() {
		return patient_mobile;
	}

	public void setPatient_mobile(String patient_mobile) {
		this.patient_mobile = patient_mobile;
	}

	public String getPatient_ssn() {
		return patient_ssn;
	}

	public void setPatient_ssn(String patient_ssn) {
		this.patient_ssn = patient_ssn;
	}

	public String getAllergies() {
		return allergies;
	}

	public void setAllergies(String allergies) {
		this.allergies = allergies;
	}

	public String getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}

	public Object getPatient_date_of_birth() {
		return patient_date_of_birth;
	}

	public void setPatient_date_of_birth(Object patient_date_of_birth) {
		this.patient_date_of_birth = patient_date_of_birth;
	}

	public String getPhy_firstname() {
		return phy_firstname;
	}

	public void setPhy_firstname(String phy_firstname) {
		this.phy_firstname = phy_firstname;
	}

	public String getPhy_lastname() {
		return phy_lastname;
	}

	public void setPhy_lastname(String phy_lastname) {
		this.phy_lastname = phy_lastname;
	}

	public String getPhy_middlename() {
		return phy_middlename;
	}

	public void setPhy_middlename(String phy_middlename) {
		this.phy_middlename = phy_middlename;
	}

	public String getPhy_fullname() {
		return phy_fullname;
	}

	public void setPhy_fullname(String phy_fullname) {
		this.phy_fullname = phy_fullname;
	}

	public String getRxNumber() {
		return rxNumber;
	}

	public void setRxNumber(String rxNumber) {
		this.rxNumber = rxNumber;
	}

	public String getCurrentRxStatusText() {
		return CurrentRxStatusText;
	}

	public void setCurrentRxStatusText(String currentRxStatusText) {
		CurrentRxStatusText = currentRxStatusText;
	}

	public String getRefillsRemaining() {
		return RefillsRemaining;
	}

	public void setRefillsRemaining(String refillsRemaining) {
		RefillsRemaining = refillsRemaining;
	}

	public String getNumberOfRefillsFilled() {
		return NumberOfRefillsFilled;
	}

	public void setNumberOfRefillsFilled(String numberOfRefillsFilled) {
		NumberOfRefillsFilled = numberOfRefillsFilled;
	}

	public String getLastFillDateTime() {
		return LastFillDateTime;
	}

	public void setLastFillDateTime(String lastFillDateTime) {
		LastFillDateTime = lastFillDateTime;
	}

	public String getCurrentRxTransactionStatusText() {
		return CurrentRxTransactionStatusText;
	}

	public void setCurrentRxTransactionStatusText(
			String currentRxTransactionStatusText) {
		CurrentRxTransactionStatusText = currentRxTransactionStatusText;
	}

	public String getQuantityRemaining() {
		return QuantityRemaining;
	}

	public void setQuantityRemaining(String quantityRemaining) {
		QuantityRemaining = quantityRemaining;
	}

	public String getNumberOfRefillsAllowed() {
		return NumberOfRefillsAllowed;
	}

	public void setNumberOfRefillsAllowed(String numberOfRefillsAllowed) {
		NumberOfRefillsAllowed = numberOfRefillsAllowed;
	}

	public String getCompletedDate() {
		return CompletedDate;
	}

	public void setCompletedDate(String completedDate) {
		CompletedDate = completedDate;
	}

	public String getTrackingNumber() {
		return TrackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		TrackingNumber = trackingNumber;
	}

	public String getRxPioneerRxID() {
		return RxPioneerRxID;
	}

	public void setRxPioneerRxID(String rxPioneerRxID) {
		RxPioneerRxID = rxPioneerRxID;
	}

	public String getRxFillTransactionPioneerRxID() {
		return RxFillTransactionPioneerRxID;
	}

	public void setRxFillTransactionPioneerRxID(String rxFillTransactionPioneerRxID) {
		RxFillTransactionPioneerRxID = rxFillTransactionPioneerRxID;
	}

	public String getPreviousRxNumber() {
		return PreviousRxNumber;
	}

	public void setPreviousRxNumber(String previousRxNumber) {
		PreviousRxNumber = previousRxNumber;
	}

	public String getPrescriberOrderNumber() {
		return PrescriberOrderNumber;
	}

	public void setPrescriberOrderNumber(String prescriberOrderNumber) {
		PrescriberOrderNumber = prescriberOrderNumber;
	}

	public String getFutureFill() {
		return FutureFill;
	}

	public void setFutureFill(String futureFill) {
		FutureFill = futureFill;
	}

	public String getPriorityTypeText() {
		return PriorityTypeText;
	}

	public void setPriorityTypeText(String priorityTypeText) {
		PriorityTypeText = priorityTypeText;
	}

	public String getScriptType() {
		return ScriptType;
	}

	public void setScriptType(String scriptType) {
		ScriptType = scriptType;
	}

	public String getRefillNumber() {
		return RefillNumber;
	}

	public void setRefillNumber(String refillNumber) {
		RefillNumber = refillNumber;
	}

	public String getPrescribedWrittenName() {
		return prescribedWrittenName;
	}

	public void setPrescribedWrittenName(String prescribedWrittenName) {
		this.prescribedWrittenName = prescribedWrittenName;
	}

	public String getPrescribedGenericName() {
		return prescribedGenericName;
	}

	public void setPrescribedGenericName(String prescribedGenericName) {
		this.prescribedGenericName = prescribedGenericName;
	}

	public String getPrescribedDrugStrength() {
		return prescribedDrugStrength;
	}

	public void setPrescribedDrugStrength(String prescribedDrugStrength) {
		this.prescribedDrugStrength = prescribedDrugStrength;
	}

	public String getPrescribedQuantity() {
		return prescribedQuantity;
	}

	public void setPrescribedQuantity(String prescribedQuantity) {
		this.prescribedQuantity = prescribedQuantity;
	}

	public String getPrescribedUnitText() {
		return prescribedUnitText;
	}

	public void setPrescribedUnitText(String prescribedUnitText) {
		this.prescribedUnitText = prescribedUnitText;
	}

	public String getDispensedDrugName() {
		return dispensedDrugName;
	}

	public void setDispensedDrugName(String dispensedDrugName) {
		this.dispensedDrugName = dispensedDrugName;
	}

	public String getDispensedDrugStrength() {
		return dispensedDrugStrength;
	}

	public void setDispensedDrugStrength(String dispensedDrugStrength) {
		this.dispensedDrugStrength = dispensedDrugStrength;
	}

	public String getDispensedQuantity() {
		return dispensedQuantity;
	}

	public void setDispensedQuantity(String dispensedQuantity) {
		this.dispensedQuantity = dispensedQuantity;
	}

	public String getDispensedUnitText() {
		return dispensedUnitText;
	}

	public void setDispensedUnitText(String dispensedUnitText) {
		this.dispensedUnitText = dispensedUnitText;
	}

	public String getDispensedDaysSupply() {
		return dispensedDaysSupply;
	}

	public void setDispensedDaysSupply(String dispensedDaysSupply) {
		this.dispensedDaysSupply = dispensedDaysSupply;
	}

	public String getDispensedLotNumber() {
		return dispensedLotNumber;
	}

	public void setDispensedLotNumber(String dispensedLotNumber) {
		this.dispensedLotNumber = dispensedLotNumber;
	}

	public String getDispensedLotExpirationDate() {
		return dispensedLotExpirationDate;
	}

	public void setDispensedLotExpirationDate(String dispensedLotExpirationDate) {
		this.dispensedLotExpirationDate = dispensedLotExpirationDate;
	}

	public String getRxCommentsCritical() {
		return RxCommentsCritical;
	}

	public void setRxCommentsCritical(String rxCommentsCritical) {
		RxCommentsCritical = rxCommentsCritical;
	}

	public String getFillCommentsCritical() {
		return FillCommentsCritical;
	}

	public void setFillCommentsCritical(String fillCommentsCritical) {
		FillCommentsCritical = fillCommentsCritical;
	}

	public String getOriginTypeID() {
		return OriginTypeID;
	}

	public void setOriginTypeID(String originTypeID) {
		OriginTypeID = originTypeID;
	}

	public String getDispensedUnitID() {
		return dispensedUnitID;
	}

	public void setDispensedUnitID(String dispensedUnitID) {
		this.dispensedUnitID = dispensedUnitID;
	}

	public String getDispensedSigCode() {
		return dispensedSigCode;
	}

	public void setDispensedSigCode(String dispensedSigCode) {
		this.dispensedSigCode = dispensedSigCode;
	}

	public String getDispensedSigCodeID() {
		return dispensedSigCodeID;
	}

	public void setDispensedSigCodeID(String dispensedSigCodeID) {
		this.dispensedSigCodeID = dispensedSigCodeID;
	}

	public String getDeaschedule() {
		return deaschedule;
	}

	public void setDeaschedule(String deaschedule) {
		this.deaschedule = deaschedule;
	}

	public String getTransmissionDate() {
		return TransmissionDate;
	}

	public void setTransmissionDate(String transmissionDate) {
		TransmissionDate = transmissionDate;
	}

	public String getGrossAmountSubmitted() {
		return GrossAmountSubmitted;
	}

	public void setGrossAmountSubmitted(String grossAmountSubmitted) {
		GrossAmountSubmitted = grossAmountSubmitted;
	}

	public String getPatientPayAmountPaid() {
		return PatientPayAmountPaid;
	}

	public void setPatientPayAmountPaid(String patientPayAmountPaid) {
		PatientPayAmountPaid = patientPayAmountPaid;
	}

	public String getPercentageSalesTaxAmountPaid() {
		return PercentageSalesTaxAmountPaid;
	}

	public void setPercentageSalesTaxAmountPaid(String percentageSalesTaxAmountPaid) {
		PercentageSalesTaxAmountPaid = percentageSalesTaxAmountPaid;
	}

	public String getPercentageSalesTaxAmountSubmitted() {
		return PercentageSalesTaxAmountSubmitted;
	}

	public void setPercentageSalesTaxAmountSubmitted(
			String percentageSalesTaxAmountSubmitted) {
		PercentageSalesTaxAmountSubmitted = percentageSalesTaxAmountSubmitted;
	}

	public String getPercentageSalesTaxRatePaid() {
		return PercentageSalesTaxRatePaid;
	}

	public void setPercentageSalesTaxRatePaid(String percentageSalesTaxRatePaid) {
		PercentageSalesTaxRatePaid = percentageSalesTaxRatePaid;
	}

	public String getPercentageSalesTaxRateSubmitted() {
		return PercentageSalesTaxRateSubmitted;
	}

	public void setPercentageSalesTaxRateSubmitted(
			String percentageSalesTaxRateSubmitted) {
		PercentageSalesTaxRateSubmitted = percentageSalesTaxRateSubmitted;
	}

	public String getFlatSalesTaxAmountPaid() {
		return FlatSalesTaxAmountPaid;
	}

	public void setFlatSalesTaxAmountPaid(String flatSalesTaxAmountPaid) {
		FlatSalesTaxAmountPaid = flatSalesTaxAmountPaid;
	}

	public String getFlatSalesTaxAmountSubmitted() {
		return FlatSalesTaxAmountSubmitted;
	}

	public void setFlatSalesTaxAmountSubmitted(String flatSalesTaxAmountSubmitted) {
		FlatSalesTaxAmountSubmitted = flatSalesTaxAmountSubmitted;
	}

	public String getDispensedDrugPioneerItemId() {
		return dispensedDrugPioneerItemId;
	}

	public void setDispensedDrugPioneerItemId(String dispensedDrugPioneerItemId) {
		this.dispensedDrugPioneerItemId = dispensedDrugPioneerItemId;
	}

	public String getDateFilledUTC() {
		return DateFilledUTC;
	}

	public void setDateFilledUTC(String dateFilledUTC) {
		DateFilledUTC = dateFilledUTC;
	}

	public String getExpirationDateUTC() {
		return ExpirationDateUTC;
	}

	public void setExpirationDateUTC(String expirationDateUTC) {
		ExpirationDateUTC = expirationDateUTC;
	}

	public String getDateWrittenUTC() {
		return DateWrittenUTC;
	}

	public void setDateWrittenUTC(String dateWrittenUTC) {
		DateWrittenUTC = dateWrittenUTC;
	}

	
	
	
}
