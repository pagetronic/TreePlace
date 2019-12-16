/*
 * Copyright 2019 Laurent PAGE, Apache Licence 2.0
 */
package com.agroneo.treeplace.api;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A cool toolkit for Json manipulation
 */
public class Json implements Map<String, Object>, Serializable {

	//The real store
	private final LinkedHashMap<String, Object> datas;

	/**
	 * Init empty
	 */
	public Json() {
		datas = new LinkedHashMap<>();
	}

	/**
	 * Init this a Json string, parse and make
	 */
	public Json(String json_string) {
		datas = new LinkedHashMap<>();
		if (json_string == null || json_string.equals("")) {
			return;
		}
		datas.putAll(new Gson().fromJson(json_string, Map.class));

	}

	/**
	 * Init and put Map object entries
	 *
	 * @param map for initialisation
	 */
	public Json(Map<String, Object> map) {

		datas = new LinkedHashMap<>();
		datas.putAll(map);
	}

	/**
	 * Init and put anything have entries
	 *
	 * @param obj for initialisation
	 */
	public Json(Object obj) {
		datas = new LinkedHashMap<>();
		if (obj == null) {
			return;
		}
		putAll((Map<String, Object>) obj);
	}

	/**
	 * Init with one entry
	 *
	 * @param key   to put where
	 * @param value to put at key
	 */
	public Json(String key, Object value) {

		datas = new LinkedHashMap<>();
		datas.put(key, value);
	}


	private <T> T get(String key, Class<T> clazz) {
		try {
			if (get(key) == null) {
				return null;
			}
			if (clazz.equals(Integer.class)) {
				return clazz.cast(((Number) datas.get(key)).intValue());
			}
			return clazz.cast(datas.get(key));
		} catch (Exception e) {

			return null;
		}
	}

	/**
	 * Get the ID as _id or id
	 *
	 * @return the id as String
	 */
	public String getId() {
		if (containsKey("_id")) {
			return getString("_id");
		} else {
			return getString("id");
		}
	}

	/**
	 * Get anything from key
	 *
	 * @param key where find value
	 * @return value at key
	 */
	public Object get(String key) {
		return datas.get(key);
	}

	/**
	 * Get anything from key with a failure value
	 *
	 * @param key where find the value
	 * @param def returned if there is no entry at key
	 * @return value at key
	 */
	public Object get(String key, Object def) {

		if (get(key) == null) {
			return def;
		}
		return get(key);
	}


	/**
	 * Get a string with a length limitation
	 *
	 * @param key where find the value
	 * @return null or a truncated string
	 */
	public String getString(String key) {
		if (key == null) {
			return null;
		}
		return get(key, String.class);
	}

	/**
	 * Get a string with a length limitation with a failure value
	 *
	 * @param key where find the value
	 * @param def returned if there is no entry at key
	 * @return null or a truncated string
	 */
	public String getString(String key, String def) {
		if (containsKey(key) && get(key) != null) {
			return getString(key);
		}
		return def;
	}

	/**
	 * Get a string with a limitation to possibles values
	 *
	 * @param key       where find the value
	 * @param possibles values
	 * @return null or a value in possibles parameter
	 */
	public String getChoice(String key, String... possibles) {
		for (String possible : possibles) {
			if (possible.equals(get(key))) {
				return getString(key);
			}
		}
		return null;
	}


	/**
	 * Get a string
	 *
	 * @param key where find the value
	 * @return null or a string
	 */
	public String getText(String key) {
		return get(key, String.class);
	}


	/**
	 * Get a string with a failure value
	 *
	 * @param key where find the value
	 * @param def returned if there is no entry at key
	 * @return null or a string
	 */
	public String getText(String key, String def) {
		if (containsKey(key)) {
			String text = getText(key);
			if (text == null) {
				return def;
			}
			return text;
		}
		return def;
	}


	/**
	 * Get a boolean with a failure value
	 *
	 * @param key where find the value
	 * @param def returned if there is no entry at key
	 * @return true or false
	 */
	public boolean getBoolean(String key, boolean def) {
		try {
			if (containsKey(key) && get(key) != null) {
				return get(key, Boolean.class);
			}
			return def;
		} catch (Exception e) {
			if (getString(key, "").equals("true")) {
				return true;
			}
			if (getString(key, "").equals("false")) {
				return false;
			}
			return def;
		}
	}

	/**
	 * Get a Date object at key
	 *
	 * @param key where find the value
	 * @return null or a Date object
	 */
	public Date getDate(String key) {
		return get(key, Date.class);
	}


	/**
	 * Get a Date object at key with a failure value
	 *
	 * @param key where find the value
	 * @param def returned if there is no entry at key
	 * @return null or a Date object
	 */
	public Date getDate(String key, Date def) {
		if (get(key) == null) {
			return def;
		}
		return get(key, Date.class);
	}

	/**
	 * Parse a string value as a Date object
	 * Needed to parse when Json is created from a string
	 *
	 * @param key where find the value
	 * @return null or a Date object
	 */
	public Date parseDate(String key) {
		if (getString(key) == null) {
			return null;
		}
		try {
			SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			fm.setTimeZone(TimeZone.getTimeZone("UTC"));
			return fm.parse(getString(key));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get a Integer at key
	 *
	 * @param key where find the value
	 * @return int value
	 */
	public int getInteger(String key) {
		if (get(key).getClass().equals(Long.class)) {
			return get(key, Long.class).intValue();
		}
		try {
			return get(key, Integer.class);
		} catch (Exception e) {
			return Integer.parseInt(getString(key));
		}
	}

	/**
	 * Get a Integer at key with a failure value
	 *
	 * @param key where find the value
	 * @param def returned if there is no entry at key
	 * @return int value
	 */
	public int getInteger(String key, int def) {

		try {
			if (datas.get(key) == null) {
				return def;
			}
			return (int) get(key);
		} catch (Exception e) {
			try {
				return Integer.parseInt(getString(key));
			} catch (Exception ex) {
				return def;
			}
		}
	}

	/**
	 * Get a Double at key
	 *
	 * @param key where find the value
	 * @return double value
	 */
	public double getDouble(String key) {

		try {
			return get(key, Double.class);
		} catch (Exception e) {
			return Double.parseDouble(getString(key));
		}
	}

	/**
	 * Get a Double at key with a failure value
	 *
	 * @param key where find the value
	 * @param def returned if there is no entry at key
	 * @return double value
	 */
	public double getDouble(String key, double def) {
		if (datas.get(key) == null) {
			return def;
		}
		try {
			return getDouble(key);
		} catch (Exception e) {
			try {
				return getInteger(key);
			} catch (Exception ex) {
				return def;
			}
		}
	}

	/**
	 * Get a List of string
	 *
	 * @param key where find the values
	 * @return List of string values
	 */
	public List<String> getList(String key) {
		return getList(key, String.class);
	}

	/**
	 * Get a List of object in a specific class
	 *
	 * @param key   where find the values
	 * @param clazz Class of the objects in the result
	 * @return List of objects values
	 */
	public <T> List<T> getList(String key, Class<T> clazz) {
		return (List<T>) datas.get(key);
	}

	/**
	 * Get a Json
	 *
	 * @param key where find the json
	 * @return Json value
	 */
	public Json getJson(String key) {
		if (get(key) == null) {
			return null;
		}
		try {
			if (get(key).getClass().equals(Json.class)) {
				return get(key, Json.class);
			} else {
				return new Json(get(key));
			}
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Get a List of Jsons
	 *
	 * @param key where find the jsons
	 * @return List of Jsons values
	 */
	public List<Json> getListJson(String key) {

		if (key == null || get(key) == null) {
			return null;
		}
		Iterator<?> list = get(key, List.class).iterator();
		List<Json> outlist = new ArrayList<>();
		while (list.hasNext()) {
			Object obj = list.next();
			if (Json.class.isAssignableFrom(obj.getClass())) {
				outlist.add((Json) obj);
			} else if (Map.class.isAssignableFrom(obj.getClass())) {
				outlist.add(new Json(obj));
			} else {
				return null;
			}
		}
		return outlist;
	}


	/**
	 * Count entries in this object
	 *
	 * @return number of entries
	 */
	@Override
	public int size() {
		return datas.size();
	}

	/**
	 * Is this object empty ?
	 *
	 * @return true if empty
	 */
	@Override
	public boolean isEmpty() {
		return datas.isEmpty();
	}

	/**
	 * Is this object contains entry at this key ?
	 *
	 * @param key where to find entry
	 * @return true if contains
	 */
	@Override
	public boolean containsKey(Object key) {
		return datas.containsKey(key);
	}

	/**
	 * Is this object contains this value ?
	 *
	 * @param value to find in this object
	 * @return true if contains
	 */
	@Override
	public boolean containsValue(Object value) {
		return datas.containsValue(value);
	}

	/**
	 * Get entry at a specific key
	 *
	 * @param key where find the value
	 * @return the entry value
	 */
	@Override
	public Object get(Object key) {
		return datas.get(key);
	}

	/**
	 * Put a value in this Json object at the end
	 *
	 * @param key   where to put an entry
	 * @param value to put in entry
	 * @return this object
	 */
	public Json put(String key, Object value) {
		datas.remove(key);
		datas.put(key, value);
		return this;
	}


	/**
	 * Put a value in this Json object at first
	 *
	 * @param key   where to put an entry
	 * @param value to put in entry
	 * @return this object
	 */
	public Json prepend(String key, Object value) {
		datas.remove(key);
		Json clone = new Json(this);
		datas.clear();
		datas.put(key, value);
		datas.putAll(clone);
		return this;
	}

	/**
	 * Put a value in this Json object at the same place if exists, or at the end
	 *
	 * @param key   where to put an entry
	 * @param value to put in entry
	 * @return this object
	 */
	public Json set(String key, Object value) {
		datas.put(key, value);
		return this;
	}


	/**
	 * Put all entry of a Map object
	 *
	 * @param map where entries have to be putted
	 */
	@Override
	public void putAll(Map<? extends String, ?> map) {
		datas.putAll(map);
	}

	/**
	 * Put all entry of a Json object
	 *
	 * @param map where entries have to be putted
	 */
	public Json putAll(Json map) {
		datas.putAll(map);
		return this;
	}

	/**
	 * Json contain entry at this key ?
	 *
	 * @param key where to find entry
	 * @return true if Json contain entry
	 */
	public boolean containsKey(String key) {
		return datas.containsKey(key);
	}

	/**
	 * Remove entry
	 *
	 * @param key where to find the entry
	 * @return this Json object
	 */
	public Json remove(String key) {
		datas.remove(key);
		return this;
	}

	/**
	 * Remove value
	 *
	 * @param object to remove
	 * @return value of this entry
	 */
	@Override
	public Object remove(Object object) {
		return datas.remove(object);
	}

	/**
	 * Create or add value to a List in this object
	 *
	 * @param key   where to find or create a List
	 * @param value to add to the List
	 * @return this Json object
	 */
	public Json add(String key, Object value) {
		List<Object> values = getList(key, Object.class);
		if (values == null) {
			values = new ArrayList<>();
		}
		values.add(value);
		return put(key, values);
	}

	/**
	 * Create or add value to a List in this object at a specific position
	 *
	 * @param key      where to find or create a List
	 * @param value    to add to the List
	 * @param position where to add value
	 * @return this Json object
	 */
	public Json add(String key, Object value, int position) {
		List<Object> values = getList(key, Object.class);
		if (values == null) {
			values = new ArrayList<>();
		}
		values.add(position, value);
		return put(key, values);
	}

	/**
	 * Get all entries in this object
	 *
	 * @return entries
	 */
	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return datas.entrySet();
	}

	/**
	 * Get all keys in this object
	 *
	 * @return keys
	 */
	@Override
	public Set<String> keySet() {
		return datas.keySet();
	}


	/**
	 * Get all keys in this object
	 *
	 * @return List of keys
	 */
	public List<String> keyList() {
		List<String> keys = new ArrayList<>();
		Collections.addAll(keys, datas.keySet().toArray(new String[0]));
		return keys;
	}

	/**
	 * Get all values in this object
	 *
	 * @return values
	 */
	@Override
	public Collection<Object> values() {
		return datas.values();
	}

	/**
	 * Clear/Empty this Json object
	 */
	@Override
	public void clear() {
		datas.clear();
	}

	/**
	 * Stringify this object as JavaScript standard
	 *
	 * @return a string usable in JavaScript
	 */
	@Override
	public String toString() {
		return toString(true);
	}


	/**
	 * Stringify this object as JavaScript standard in low weight/compressed
	 *
	 * @return a compressed string usable in JavaScript
	 */
	public String toString(boolean compressed) {
		if (compressed) {
			return new Gson().toJson(this);
		}
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	/**
	 * Simple clone function
	 *
	 * @return a clone of this object
	 */
	@Override
	public Json clone() {
		try {
			return (Json) super.clone();
		} catch (CloneNotSupportedException e) {
			return new Json(this);
		}
	}

	/**
	 * Get a List representation of this Object
	 *
	 * @return a List of the entries
	 */
	public List<Json> toList() {
		List<Json> arr = new ArrayList<>();
		for (Entry<String, Object> entry : datas.entrySet()) {
			arr.add(new Json("key", entry.getKey()).put("value", entry.getValue()));
		}
		return arr;
	}

	/**
	 * Get the key of a value
	 *
	 * @return the first key where the value can be found
	 */
	public String findKey(Object value) {
		if (value == null) {
			return null;
		}
		for (Entry<String, Object> set : entrySet()) {
			if (set.getValue().equals(value)) {
				return set.getKey();
			}
		}
		return null;

	}

	/**
	 * Sort entries by key
	 *
	 * @return Json where entries are alphabetical ordered
	 */
	public Json sort() {
		List<String> keys = Arrays.asList(keySet().toArray(new String[0]));
		Collections.sort(keys);
		Collections.reverse(keys);
		for (String key : keys) {
			prepend(key, get(key));
		}
		return this;
	}
}
