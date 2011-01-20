package com.nextmilestone.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class PListParser {

	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public List<Map<String, Object>> parse(final String input) throws XmlPullParserException, IOException {
		final XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
		final XmlPullParser parser = xppf.newPullParser();
		parser.setInput(new ByteArrayInputStream(input.getBytes()), "utf-8");

		final Stack<List<Map<String, Object>>> arrayStack = new Stack<List<Map<String, Object>>>();
		final Stack<Map<String, Object>> dictStack = new Stack<Map<String, Object>>();
		final Stack<String> keyStack = new Stack<String>();

		int eventType = parser.getEventType();
		boolean done = false;
		while (!done) {
			final String name = parser.getName();
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if ("array".equalsIgnoreCase(name)) {
					final List<Map<String, Object>> array = new ArrayList<Map<String,Object>>();
					arrayStack.push(array);
				} else if ("dict".equalsIgnoreCase(name)) {
					final Map<String, Object> dict = new HashMap<String, Object>();
					dictStack.push(dict);
				} else if ("key".equalsIgnoreCase(name)){
					keyStack.push(parser.nextText()); // assign current key
				} else if ("string".equalsIgnoreCase(name)) {
					final Map<String, Object> dict = dictStack.peek();
					final String string = parser.nextText();
					final String key = keyStack.pop();
					dict.put(key, string);
				} else if ("integer".equalsIgnoreCase(name)) {
					final Map<String, Object> dict = dictStack.peek();
					final String integerStr = parser.nextText();
					final Integer integer = new Integer(integerStr);
					final String key = keyStack.pop();
					dict.put(key, integer);
				} else if ("date".equalsIgnoreCase(name)) {
					final Map<String, Object> dict = dictStack.peek();
					final String dateStr = parser.nextText();
					Date date = null;
					try {
						date = dateFormat.parse(dateStr);
					} catch (final ParseException e) {
						e.printStackTrace();
					}
					final String key = keyStack.pop();
					dict.put(key, date);
				} else if ("false".equalsIgnoreCase(name)) {
					final Map<String, Object> dict = dictStack.peek();
					final Boolean booleanValue = new Boolean(false);
					final String key = keyStack.pop();
					dict.put(key, booleanValue);
				} else if ("true".equalsIgnoreCase(name)) {
					final Map<String, Object> dict = dictStack.peek();
					final Boolean booleanValue = new Boolean(true);
					final String key = keyStack.pop();
					dict.put(key, booleanValue);
				}

				break;
			case XmlPullParser.END_TAG:
				if ("array".equalsIgnoreCase(name)) {
					final List<Map<String, Object>> array = arrayStack.pop();
					if (arrayStack.isEmpty()) {
						return array;
					}
					// If not end of array, means it's an array within a dict
					final String key = keyStack.pop();
					dictStack.peek().put(key, array);
				} else if ("dict".equalsIgnoreCase(name)) {
					final Map<String, Object> dict = dictStack.pop();
					arrayStack.peek().add(dict);
				}
				break;
			case XmlPullParser.END_DOCUMENT:
				done = true;
				break;
			}
			eventType = parser.next();
		}

		return null;
	}

}
