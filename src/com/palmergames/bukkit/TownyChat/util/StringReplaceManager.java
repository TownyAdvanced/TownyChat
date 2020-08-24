package com.palmergames.bukkit.TownyChat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Register keyword regex's to be dynamically replaced.
 * 
 * Credits to:
 * - http://stackoverflow.com/questions/1326682/java-replacing-multiple-different-substring-in-a-string-at-once-or-in-the-most-e/1326962#1326962
 * 
 * @author Chris H (Shade)
 * @version 1.0
 */
public class StringReplaceManager<E> {
	private List<Pattern> replacementPatterns = new ArrayList<Pattern>();
	private List<Object> replacements = new ArrayList<Object>();
	
	public boolean registerFormatReplacement(String regexKey) {
		return registerFormatReplacement(regexKey, null);
	}
	
	public boolean registerFormatReplacement(String regexKey, Object replacement) {
		Pattern pattern = Pattern.compile(regexKey);
		replacementPatterns.add(pattern);
		replacements.add(replacement);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public String replaceAll(String format, E e) {
		String out = format;
		for (int i = 0; i < replacementPatterns.size(); i++) {
			Matcher matcher = replacementPatterns.get(i).matcher(out);
			Object replacer = replacements.get(i);
			StringBuffer sb = new StringBuffer();
			
			while (matcher.find()) {
				String match = matcher.group();
				if (replacer == null) {
					matcher.appendReplacement(sb, e.toString());
				} else if (replacer instanceof String) {
					matcher.appendReplacement(sb, replacer.toString());
				} else {
					try {
						String replacement = Matcher.quoteReplacement(((ReplacerCallable<E>)replacer).call(match, e));
						matcher.appendReplacement(sb, replacement);
					} catch (Exception exception) {
						continue;
					}
				}
				
			}
			matcher.appendTail(sb);
			out = sb.toString();
		}
		return out;
	}
	
	public static void main(String[] args) {
		String regex = "\\+{1}[a-zA-Z]+";
		String s = "+meh asdf+lo";
		/*
		 * TestChatEvent is a simple string holder. TestReplacerCallable will return the string held in TestChatEvent.
		 * 
		{
			StringReplaceManager<TestChatEvent> replacer = new StringReplaceManager<TestChatEvent>();
			replacer.registerFormatReplacement(regex, new TestReplacerCallable());
			System.out.println(replacer.replaceAll(s, new TestChatEvent("!")));
		}
		*/
		
		{
			StringReplaceManager<String> replacer = new StringReplaceManager<String>();
			replacer.registerFormatReplacement(regex, "1");
			System.out.println(replacer.replaceAll(s, "2"));
		}
		
		{
			StringReplaceManager<String> replacer = new StringReplaceManager<String>();
			replacer.registerFormatReplacement(regex);
			System.out.println(replacer.replaceAll(s, "2"));
		}
		
		int times = 1000;
		{
			long t = System.currentTimeMillis();
			for (int i = 0; i < times; i++) {
				s.replaceAll(regex, "2");
			}
			System.out.println("Runtime: " + ((System.currentTimeMillis() - t) / ((double)times)));
		}
		
		{
			long t = System.currentTimeMillis();
			StringReplaceManager<String> replacer = new StringReplaceManager<String>();
			replacer.registerFormatReplacement(regex);
			for (int i = 0; i < times; i++) {
				replacer.replaceAll(s, "2");
			}
			System.out.println("Precompiled: " + ((System.currentTimeMillis() - t) / ((double)times)));
		}
	}
}