package org.jaya.annotation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jaya.search.LuceneUnicodeSearcher;
import org.jaya.search.ResultDocument;
import org.jaya.util.Constatants;
import org.jaya.util.TimestampUtils;
import org.jaya.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AnnotationManager {

	private WeakReference<LuceneUnicodeSearcher> mSearcher = new WeakReference<LuceneUnicodeSearcher>(null);
	private String mAnnotationsFilePath;
	private TreeMap<String, Annotation> mDocIdToAnnotationMap = new TreeMap<>();
	private TreeMap<String, Annotation> mTimestampToAnnotationMap = new TreeMap<>();
	private boolean mbIsDirty = false;
	private int mMaxItems = Integer.MAX_VALUE;
	
	public AnnotationManager(LuceneUnicodeSearcher searcher, String AnnotationsFilePath){
		init(searcher, AnnotationsFilePath, Integer.MAX_VALUE);
	}
	
	public AnnotationManager(LuceneUnicodeSearcher searcher, String AnnotationsFilePath, int maxItems){
		init(searcher, AnnotationsFilePath, maxItems);
	}	
	
	private void init(LuceneUnicodeSearcher searcher, String annotationsFilePath, int maxItems) {
		mSearcher = new WeakReference<LuceneUnicodeSearcher>(searcher);
		mAnnotationsFilePath = annotationsFilePath;
		mMaxItems = maxItems;
		JSONParser parser = new JSONParser();
		try{
			JSONObject annotationsJSONObject = (JSONObject) parser.parse(new FileReader(new File(annotationsFilePath)));
			String version = (String)annotationsJSONObject.get("version");
			if( version.equals("1.0")){
				JSONArray items = (JSONArray)annotationsJSONObject.get("items");
				for(int i=0;i<items.size();i++){
					JSONObject obj = (JSONObject)items.get(i);
					String docPath = (String) obj.get("docPath");
					String docLocalId = (String) obj.get("docLocalId");
					String updated = (String) obj.get("updated");
					String name = (String) obj.get("name");
					Date date = TimestampUtils.getDateFromISO8601String(updated);
					Annotation a = new Annotation(docPath, docLocalId, name, date);
					if (StringUtils.isNotBlank(docPath) && StringUtils.isNoneBlank(docLocalId)) {
						mDocIdToAnnotationMap.put(a.getKey(), a);
						mTimestampToAnnotationMap.put(updated, a);
					}
				}
			}
			
		}
		catch(Exception ex){
			ex.printStackTrace();
		}		
	}
	
	public synchronized void saveIfDirty(){
		if(!mbIsDirty)
			return;
		FileWriter fw = null;
		try{
			fw = new FileWriter(new File(mAnnotationsFilePath));
			JSONObject annotationsJSONObject = new JSONObject();
			JSONArray items = new JSONArray();
			annotationsJSONObject.put("version", "1.0");
			Set<String> keys = mTimestampToAnnotationMap.descendingKeySet();
			for(String ts:keys){
				JSONObject obj = new JSONObject();
				Annotation a = mTimestampToAnnotationMap.get(ts);
				String docPath = a.getDocPath();
				String docLocalId = a.getDocLocalId();
				if (StringUtils.isNotBlank(docPath) && StringUtils.isNoneBlank(docLocalId)) {
					obj.put("docPath", docPath);
					obj.put("docLocalId", docLocalId);
					obj.put("updated", TimestampUtils.getISO8601StringForDate(a.getUpdatedDate()));
					obj.put("name", a.getName());
					obj.put("notes", a.getNotes());
					items.add(obj);
				}
			}
			annotationsJSONObject.put("items", items);
			fw.write(annotationsJSONObject.toJSONString());
			mbIsDirty = false;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		finally {
			Utils.closeSilently(fw);
		}
	}
	
	public synchronized Annotation addAnnotation(ResultDocument resDoc, String name){
		if( resDoc == null || name == null || name.isEmpty() )
			return null;
		String docPath = resDoc.getDoc().get(Constatants.FIELD_PATH);
		String docLocalId = resDoc.getDoc().get(Constatants.FIELD_DOC_LOCAL_ID);
		String key = docPath + docLocalId;
		Annotation annotation;
		if( mDocIdToAnnotationMap.containsKey(key) ){
			annotation = mDocIdToAnnotationMap.get(key);
			String oldTimeStamp = TimestampUtils.getISO8601StringForDate(annotation.getUpdatedDate());			
			annotation.setUpdatedDate(new Date());
			annotation.setName(name);		
			mTimestampToAnnotationMap.remove(oldTimeStamp);
		}
		else{
			annotation = new Annotation(docPath, docLocalId, name, new Date());
			mDocIdToAnnotationMap.put(key, annotation);
		}
		
		String timeStamp = TimestampUtils.getISO8601StringForDate(annotation.getUpdatedDate());	
		mTimestampToAnnotationMap.put(timeStamp, annotation);	
		while( mTimestampToAnnotationMap.size() > mMaxItems ){
			String updated = mTimestampToAnnotationMap.firstKey();
			Annotation a = mTimestampToAnnotationMap.get(updated);
			mTimestampToAnnotationMap.remove(updated);
			mDocIdToAnnotationMap.remove(a.getKey());
		}
		mbIsDirty = true;
		return annotation;
	}
	
	public synchronized void removeAnnotation(ResultDocument doc){
		removeAnnotation(new Annotation(doc));
	}
	
	public synchronized void removeAnnotation(Annotation annotation){
		if( annotation == null || !mDocIdToAnnotationMap.containsKey(annotation.getKey()) )
			return;
		Annotation a = mDocIdToAnnotationMap.get(annotation.getKey());
		mDocIdToAnnotationMap.remove(a.getKey());
		mTimestampToAnnotationMap.remove(TimestampUtils.getISO8601StringForDate(a.getUpdatedDate()));
		mbIsDirty = true;
	}
	
	public boolean annotationExists(ResultDocument doc){
		return annotationExists(new Annotation(doc));
	}
	
	public boolean annotationExists(Annotation a){
		if( a == null || mDocIdToAnnotationMap == null || mDocIdToAnnotationMap.isEmpty() )
			return false;
		return mDocIdToAnnotationMap.containsKey(a.getKey());
	}
	
	public int getNumAnnotations(){
		return mDocIdToAnnotationMap.size();
	}
	
	public List<Annotation> getAnnotations(){
		List<Annotation> retVal = new ArrayList<>();
		Set<String> keys = mTimestampToAnnotationMap.descendingKeySet();
		for(String ts:keys){
			retVal.add(mTimestampToAnnotationMap.get(ts));
		}
		return retVal;
	}
}
