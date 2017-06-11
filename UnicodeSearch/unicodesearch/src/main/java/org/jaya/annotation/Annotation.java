package org.jaya.annotation;

import java.util.Date;

import org.jaya.search.ResultDocument;
import org.jaya.util.Constatants;
import org.jaya.util.TimestampUtils;

public class Annotation {
	
	private String mDocPath = "";
	private String mDocLocalId = "";
	private String mName = "";
	private String mNotes = "";
	private Date mUpdatedDate;
	
	public Annotation(ResultDocument resDoc){
		init(resDoc, TimestampUtils.nowAsString(), new Date());
	}
	
	public Annotation(ResultDocument resDoc, String name, Date d){
		init(resDoc, name, d);
	}

	public void init(ResultDocument resDoc, String name, Date d){
		if( resDoc == null || resDoc.getDoc() == null )
			return;
		mDocLocalId = resDoc.getDoc().get(Constatants.FIELD_DOC_LOCAL_ID);
		mDocPath = resDoc.getDoc().get(Constatants.FIELD_PATH);
		mName = name;
		mUpdatedDate = d;
	}
	
	public Annotation(String docPath, String docLocalId, String name, Date d){
		mDocPath = docPath;
		mDocLocalId = docLocalId;
		mName = name;
		mUpdatedDate = d;
	}
	
	public String getDocPath(){
		return mDocPath;
	}
	
	public String getDocLocalId(){
		return mDocLocalId;
	}
	
	public String getName(){
		return mName;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public String getKey(){
		return mDocPath+mDocLocalId;
	}
	
	public void setUpdatedDate(Date d){
		mUpdatedDate = d;
	}
	
	public Date getUpdatedDate(){
		return mUpdatedDate;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if( !(arg0 instanceof Annotation) )
			return false;
		Annotation a = (Annotation) arg0;
		return a.mDocLocalId == mDocLocalId && a.mDocPath == mDocPath;
	}

	public void setNotes(String notes){
		mNotes = notes;
	}
	
	public String getNotes(){
		return mNotes;
	}
}
