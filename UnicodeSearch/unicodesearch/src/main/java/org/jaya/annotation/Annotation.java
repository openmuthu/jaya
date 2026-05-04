package org.jaya.annotation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jaya.search.ResultDocument;
import org.jaya.util.Constatants;
import org.jaya.util.TimestampUtils;

public class Annotation {

	private String mDocPath = "";
	private String mDocLocalId = "";
	private String mName = "";
	private String mNotes = "";
	private Date mUpdatedDate;
	private List<String> mGroupIds = new ArrayList<>();

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

	public void setDocLocalId(String docLocalId){
		mDocLocalId = docLocalId;
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

	/** Returns a copy of the group ids list. */
	public List<String> getGroupIds() {
		return new ArrayList<>(mGroupIds);
	}

	public void setGroupIds(List<String> ids) {
		mGroupIds = new ArrayList<>(ids);
	}

	public void addGroupId(String groupId) {
		if (groupId != null && !mGroupIds.contains(groupId)) {
			mGroupIds.add(groupId);
		}
	}

	public void removeGroupId(String groupId) {
		mGroupIds.remove(groupId);
	}

	public boolean isInGroup(String groupId) {
		return mGroupIds.contains(groupId);
	}

	/**
	 * Content fingerprint: the first {@link #FINGERPRINT_LENGTH} characters of
	 * the bookmarked chunk's text, with whitespace normalised.
	 *
	 * Used to recover the correct Lucene document after an index rebuild where
	 * the chunk's {@code docLocalId} has shifted.  Empty string means no
	 * fingerprint has been recorded (legacy annotation).
	 */
	public static final int FINGERPRINT_LENGTH = 120;

	private String mContentFingerprint = "";

	public String getContentFingerprint() {
		return mContentFingerprint;
	}

	public void setContentFingerprint(String fingerprint) {
		mContentFingerprint = (fingerprint == null) ? "" : fingerprint;
	}

	/**
	 * Build a fingerprint from raw content: trim, collapse internal whitespace,
	 * and take the first {@link #FINGERPRINT_LENGTH} chars.
	 */
	public static String buildFingerprint(String rawContent) {
		if (rawContent == null) return "";
		String normalised = rawContent.trim().replaceAll("\\s+", " ");
		return normalised.substring(0, Math.min(FINGERPRINT_LENGTH, normalised.length()));
	}
}
