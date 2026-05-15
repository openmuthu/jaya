package org.jaya.annotation;

import org.apache.commons.lang3.StringUtils;
import org.jaya.search.LuceneUnicodeSearcher;
import org.jaya.search.ResultDocument;
import org.jaya.util.Constatants;
import org.jaya.util.TimestampUtils;
import org.jaya.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class AnnotationManager {

	private WeakReference<LuceneUnicodeSearcher> mSearcher = new WeakReference<LuceneUnicodeSearcher>(null);
	private String mAnnotationsFilePath;
	private TreeMap<String, Annotation> mDocIdToAnnotationMap = new TreeMap<>();
	private TreeMap<String, Annotation> mTimestampToAnnotationMap = new TreeMap<>();
	/** Groups stored in insertion order (LinkedHashMap preserves order). */
	private LinkedHashMap<String, BookmarkGroup> mGroupMap = new LinkedHashMap<>();
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
			JSONObject root = (JSONObject) parser.parse(new FileReader(new File(annotationsFilePath)));
			String version = (String) root.get("version");
			if ("1.0".equals(version)) {
				loadV1Items((JSONArray) root.get("items"));
			} else if ("2.0".equals(version)) {
				loadV2Groups((JSONArray) root.get("groups"));
				loadV2Items((JSONArray) root.get("items"));
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private void loadV1Items(JSONArray items) {
		if (items == null) return;
		for (int i = 0; i < items.size(); i++) {
			JSONObject obj = (JSONObject) items.get(i);
			String docPath    = (String) obj.get("docPath");
			String docLocalId = (String) obj.get("docLocalId");
			String updated    = (String) obj.get("updated");
			String name       = (String) obj.get("name");
			String notes      = (String) obj.get("notes");
			Date date = TimestampUtils.getDateFromISO8601String(updated);
			Annotation a = new Annotation(docPath, docLocalId, name, date);
			if (notes != null) a.setNotes(notes);
			// No fingerprint in v1 — will trigger fallback if docLocalId is stale
			if (StringUtils.isNotBlank(docPath) && StringUtils.isNotBlank(docLocalId)) {
				mDocIdToAnnotationMap.put(a.getKey(), a);
				mTimestampToAnnotationMap.put(updated, a);
			}
		}
	}

	private void loadV2Groups(JSONArray groups) {
		if (groups == null) return;
		for (int i = 0; i < groups.size(); i++) {
			JSONObject obj  = (JSONObject) groups.get(i);
			String id       = (String) obj.get("id");
			String name     = (String) obj.get("name");
			String created  = (String) obj.get("createdDate");
			if (id != null && name != null) {
				Date createdDate = TimestampUtils.getDateFromISO8601String(created);
				mGroupMap.put(id, new BookmarkGroup(id, name, createdDate));
			}
		}
	}

	private void loadV2Items(JSONArray items) {
		if (items == null) return;
		for (int i = 0; i < items.size(); i++) {
			JSONObject obj  = (JSONObject) items.get(i);
			String docPath    = (String) obj.get("docPath");
			String docLocalId = (String) obj.get("docLocalId");
			String updated    = (String) obj.get("updated");
			String name       = (String) obj.get("name");
			String notes      = (String) obj.get("notes");
			String fingerprint = (String) obj.get("fingerprint");
			Date date = TimestampUtils.getDateFromISO8601String(updated);
			Annotation a = new Annotation(docPath, docLocalId, name, date);
			if (notes != null) a.setNotes(notes);
			if (fingerprint != null) a.setContentFingerprint(fingerprint);
			JSONArray groupIds = (JSONArray) obj.get("groups");
			if (groupIds != null) {
				for (int j = 0; j < groupIds.size(); j++) {
					a.addGroupId((String) groupIds.get(j));
				}
			}
			if (StringUtils.isNotBlank(docPath) && StringUtils.isNotBlank(docLocalId)) {
				mDocIdToAnnotationMap.put(a.getKey(), a);
				mTimestampToAnnotationMap.put(updated, a);
			}
		}
	}

	public synchronized void saveIfDirty(){
		if(!mbIsDirty)
			return;
		FileWriter fw = null;
		try{
			fw = new FileWriter(new File(mAnnotationsFilePath));
			JSONObject root = new JSONObject();
			root.put("version", "2.0");

			// --- groups ---
			JSONArray groupsArray = new JSONArray();
			for (BookmarkGroup g : mGroupMap.values()) {
				JSONObject go = new JSONObject();
				go.put("id", g.getId());
				go.put("name", g.getName());
				go.put("createdDate", TimestampUtils.getISO8601StringForDate(g.getCreatedDate()));
				groupsArray.add(go);
			}
			root.put("groups", groupsArray);

			// --- items ---
			JSONArray items = new JSONArray();
			Set<String> keys = mTimestampToAnnotationMap.descendingKeySet();
			for (String ts : keys) {
				Annotation a = mTimestampToAnnotationMap.get(ts);
				String docPath    = a.getDocPath();
				String docLocalId = a.getDocLocalId();
				if (StringUtils.isNotBlank(docPath) && StringUtils.isNotBlank(docLocalId)) {
					JSONObject obj = new JSONObject();
					obj.put("docPath",    docPath);
					obj.put("docLocalId", docLocalId);
					obj.put("updated",    TimestampUtils.getISO8601StringForDate(a.getUpdatedDate()));
					obj.put("name",       a.getName());
					obj.put("notes",       a.getNotes());
					obj.put("fingerprint", a.getContentFingerprint());
					JSONArray gids = new JSONArray();
					for (String gid : a.getGroupIds()) {
						gids.add(gid);
					}
					obj.put("groups", gids);
					items.add(obj);
				}
			}
			root.put("items", items);
			fw.write(root.toJSONString());
			mbIsDirty = false;
		} catch(Exception ex){
			ex.printStackTrace();
		} finally {
			Utils.closeSilently(fw);
		}
	}

	public synchronized Annotation addAnnotation(ResultDocument resDoc, String name){
		if( resDoc == null || name == null || name.isEmpty() )
			return null;
		String docPath    = resDoc.getDoc().get(Constatants.FIELD_PATH);
		String docLocalId = resDoc.getDoc().get(Constatants.FIELD_DOC_LOCAL_ID);
		String fingerprint = Annotation.buildFingerprint(
				resDoc.getDoc().get(Constatants.FIELD_CONTENTS));
		String key = docPath + docLocalId;
		Annotation annotation;
		if( mDocIdToAnnotationMap.containsKey(key) ){
			annotation = mDocIdToAnnotationMap.get(key);
			String oldTimeStamp = TimestampUtils.getISO8601StringForDate(annotation.getUpdatedDate());
			annotation.setUpdatedDate(new Date());
			annotation.setName(name);
			annotation.setContentFingerprint(fingerprint);
			mTimestampToAnnotationMap.remove(oldTimeStamp);
		}
		else{
			annotation = new Annotation(docPath, docLocalId, name, new Date());
			annotation.setContentFingerprint(fingerprint);
			mDocIdToAnnotationMap.put(key, annotation);
		}

		String timeStamp = uniqueTimestamp(annotation);
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

	/**
	 * Add an annotation directly from its components (for tests that cannot easily
	 * construct a ResultDocument).  Package-private intentionally.
	 */
	synchronized Annotation addAnnotationDirect(String docPath, String docLocalId, String name) {
		String key = docPath + docLocalId;
		Annotation annotation;
		if (mDocIdToAnnotationMap.containsKey(key)) {
			annotation = mDocIdToAnnotationMap.get(key);
			mTimestampToAnnotationMap.remove(
					TimestampUtils.getISO8601StringForDate(annotation.getUpdatedDate()));
			annotation.setUpdatedDate(new Date());
			annotation.setName(name);
		} else {
			annotation = new Annotation(docPath, docLocalId, name, new Date());
			mDocIdToAnnotationMap.put(key, annotation);
		}
		String ts = uniqueTimestamp(annotation);
		mTimestampToAnnotationMap.put(ts, annotation);
		mbIsDirty = true;
		return annotation;
	}

	/**
	 * Returns a timestamp string that is unique within mTimestampToAnnotationMap.
	 * If the natural timestamp collides with an existing key (possible when two
	 * annotations are created within the same millisecond), the annotation's date
	 * is bumped by 1 ms until the key is free.
	 */
	private String uniqueTimestamp(Annotation annotation) {
		String ts = TimestampUtils.getISO8601StringForDate(annotation.getUpdatedDate());
		while (mTimestampToAnnotationMap.containsKey(ts)) {
			annotation.setUpdatedDate(new Date(annotation.getUpdatedDate().getTime() + 1));
			ts = TimestampUtils.getISO8601StringForDate(annotation.getUpdatedDate());
		}
		return ts;
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

	/** Returns the stored annotation for {@code doc}, or {@code null} if none. */
	public Annotation getAnnotation(ResultDocument doc) {
		if (doc == null) return null;
		return mDocIdToAnnotationMap.get(new Annotation(doc).getKey());
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

	/** Returns annotations that belong to the given group (newest-first). */
	public List<Annotation> getAnnotationsForGroup(String groupId) {
		List<Annotation> retVal = new ArrayList<>();
		Set<String> keys = mTimestampToAnnotationMap.descendingKeySet();
		for (String ts : keys) {
			Annotation a = mTimestampToAnnotationMap.get(ts);
			if (a.isInGroup(groupId)) {
				retVal.add(a);
			}
		}
		return retVal;
	}

	/**
	 * Called by the presentation layer after a successful fingerprint-based
	 * recovery.  Updates the annotation's {@code docLocalId} to the new value
	 * so subsequent lookups use the fast path.
	 *
	 * The annotation is re-keyed in {@code mDocIdToAnnotationMap} because the
	 * key embeds the old docLocalId.
	 *
	 * @param oldKey      the old key (docPath + old docLocalId)
	 * @param newLocalId  the docLocalId found in the current index
	 */
	public synchronized void healAnnotationLocalId(String oldKey, String newLocalId) {
		Annotation a = mDocIdToAnnotationMap.get(oldKey);
		if (a == null || newLocalId == null) return;
		if (newLocalId.equals(a.getDocLocalId())) return;  // nothing to heal
		mDocIdToAnnotationMap.remove(oldKey);
		a.setDocLocalId(newLocalId);
		mDocIdToAnnotationMap.put(a.getKey(), a);
		mbIsDirty = true;
	}

	/** Mark the store as dirty so the next saveIfDirty() call persists the changes. */
	public synchronized void markDirty() {
		mbIsDirty = true;
	}

	/**
	 * Returns true if any annotation is missing a content fingerprint.
	 * Pure in-memory check — no I/O.  Used to avoid spawning a worker thread
	 * on app launch when all fingerprints are already populated.
	 */
	public boolean needsFingerprintBackfill() {
		for (Annotation a : mDocIdToAnnotationMap.values()) {
			if (a.getContentFingerprint().isEmpty()) return true;
		}
		return false;
	}

	/**
	 * Backfill content fingerprints for any annotations that don't have one yet
	 * (e.g. bookmarks created before fingerprinting was introduced).
	 *
	 * <p>Must be called from a background thread — it performs Lucene I/O for
	 * each un-fingerprinted annotation.  Returns the number of annotations that
	 * were updated.
	 */
	public synchronized int backfillFingerprints(LuceneUnicodeSearcher searcher) {
		if (searcher == null) return 0;
		int count = 0;
		for (Annotation a : mDocIdToAnnotationMap.values()) {
			if (!a.getContentFingerprint().isEmpty()) continue;
			try {
				ResultDocument rd = searcher.getDoc(a.getDocPath(), a.getDocLocalId());
				if (rd == null || rd.getDoc() == null) continue;
				String raw = rd.getDoc().get(Constatants.FIELD_CONTENTS);
				String fp  = Annotation.buildFingerprint(raw);
				if (!fp.isEmpty()) {
					a.setContentFingerprint(fp);
					mbIsDirty = true;
					count++;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return count;
	}

	/** Rename a bookmark by its key. Returns false if not found. */
	public synchronized boolean renameAnnotation(String key, String newName) {
		Annotation a = mDocIdToAnnotationMap.get(key);
		if (a == null) return false;
		a.setName(newName);
		mbIsDirty = true;
		return true;
	}

	// ------------------------------------------------------------------ groups

	/** Returns all groups in creation order. */
	public List<BookmarkGroup> getGroups() {
		return new ArrayList<>(mGroupMap.values());
	}

	/**
	 * Add a new group.  The auto-name ("Group 1", "Group 2", …) is assigned by
	 * the caller; the manager just stores it.
	 */
	public synchronized BookmarkGroup addGroup(String name) {
		BookmarkGroup g = BookmarkGroup.createNew(name);
		mGroupMap.put(g.getId(), g);
		mbIsDirty = true;
		return g;
	}

	/** Generate the next default group name ("Group 1", "Group 2", …). */
	public String generateGroupName() {
		int n = mGroupMap.size() + 1;
		// Keep incrementing until we find a name not in use.
		while (true) {
			String candidate = "Group " + n;
			boolean taken = false;
			for (BookmarkGroup g : mGroupMap.values()) {
				if (candidate.equals(g.getName())) { taken = true; break; }
			}
			if (!taken) return candidate;
			n++;
		}
	}

	/** Rename a group.  Returns false if not found. */
	public synchronized boolean renameGroup(String groupId, String newName) {
		BookmarkGroup g = mGroupMap.get(groupId);
		if (g == null) return false;
		g.setName(newName);
		mbIsDirty = true;
		return true;
	}

	/**
	 * Delete a group and remove all its id references from annotations.
	 * Returns false if the group did not exist.
	 */
	public synchronized boolean removeGroup(String groupId) {
		if (!mGroupMap.containsKey(groupId)) return false;
		mGroupMap.remove(groupId);
		for (Annotation a : mDocIdToAnnotationMap.values()) {
			a.removeGroupId(groupId);
		}
		mbIsDirty = true;
		return true;
	}

	/** Assign an annotation to a group.  Returns false if either is not found. */
	public synchronized boolean assignAnnotationToGroup(String annotationKey, String groupId) {
		if (!mGroupMap.containsKey(groupId)) return false;
		Annotation a = mDocIdToAnnotationMap.get(annotationKey);
		if (a == null) return false;
		a.addGroupId(groupId);
		mbIsDirty = true;
		return true;
	}

	/** Remove an annotation from a group.  Returns false if annotation is not found. */
	public synchronized boolean unassignAnnotationFromGroup(String annotationKey, String groupId) {
		Annotation a = mDocIdToAnnotationMap.get(annotationKey);
		if (a == null) return false;
		a.removeGroupId(groupId);
		mbIsDirty = true;
		return true;
	}

	/**
	 * Replace the complete group membership of an annotation.
	 * Each id in newGroupIds must already exist in mGroupMap; unknown ids are silently ignored.
	 */
	public synchronized void setAnnotationGroups(String annotationKey, List<String> newGroupIds) {
		Annotation a = mDocIdToAnnotationMap.get(annotationKey);
		if (a == null) return;
		List<String> validated = new ArrayList<>();
		for (String gid : newGroupIds) {
			if (mGroupMap.containsKey(gid)) validated.add(gid);
		}
		a.setGroupIds(validated);
		mbIsDirty = true;
	}

	/** Convenience: comma-separated group names for a given annotation (for display). */
	public String getGroupNamesString(Annotation annotation) {
		if (annotation == null) return "";
		List<String> ids = annotation.getGroupIds();
		if (ids.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		for (String id : ids) {
			BookmarkGroup g = mGroupMap.get(id);
			if (g != null) {
				if (sb.length() > 0) sb.append(", ");
				sb.append(g.getName());
			}
		}
		return sb.toString();
	}
}
