package com.thetonyk.Proxy.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {
	
	public static String toText(long time, boolean withSeconds) {
		
		int duration = (int) (time / 1000l);
		int seconds = duration % 60;
		int minutes = (duration % 3600) / 60;
		int hours = (duration % 86400) / 3600;
		int days = (duration % 2678400) / 86400;
		int months = duration / 2678400;
		
		String text = (months > 0 ? months + " months " : "") + (days > 0 ? days + " days " : "") + (hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "min " : "") + (seconds > 0 && withSeconds ? seconds + "s " : "");
		
		return text.substring(0, text.length() - 1);
		
	}
	
	public static String toShortText(long time, boolean withSeconds) {
		
		int duration = (int) (time / 1000l);
		int seconds = duration % 60;
		int minutes = (duration % 3600) / 60;
		int hours = (duration % 86400) / 3600;
		int days = (duration % 2678400) / 86400;
		int months = duration / 2678400;
		
		String text = (months > 0 ? months + " mo " : "") + (days > 0 ? days + "d " : "") + (hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + (seconds > 0 && withSeconds ? seconds + "s " : "");
		
		return text.substring(0, text.length() - 1);
		
	}
	
	/* By Essentials, modified by LeonTG77 & D4mnX */
	private static final Pattern TIME_PATTERN = Pattern.compile(
			
            "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" +     // Captured group 1 = Years     (y)
            "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" +    // Captured group 2 = Months    (mo)
            "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" +     // Captured group 3 = Weeks     (w)
            "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +     // Captured group 4 = Days      (d)
            "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" +     // Captured group 5 = Hours     (h)
            "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +     // Captured group 6 = Minutes   (m)
            "(?:([0-9]+)\\s*(?:s[a-z]*)?)?",        // Captured group 2 = Seconds   (s)
            Pattern.CASE_INSENSITIVE
            
    );
	
	public static long parseDateDiff (String time) {
		
    	final Matcher matcher = TIME_PATTERN.matcher(time);
 
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
 
        boolean found = false;
 
        while (matcher.find()) {
        	
            if (matcher.group() == null || matcher.group().isEmpty()) continue;
 
            for (int i = 0; i < matcher.groupCount(); i++) {
            	
                if (matcher.group(i) == null || matcher.group(i).isEmpty()) continue;
                	
                found = true;
                break;
                
            }
 
            if (found) {
            	
                years = parseGroup(matcher, 1);
                months = parseGroup(matcher, 2);
                weeks = parseGroup(matcher, 3);
                days = parseGroup(matcher, 4);
                hours = parseGroup(matcher, 5);
                minutes = parseGroup(matcher, 6);
                seconds = parseGroup(matcher, 7);
                break;
                
            }
            
        }
 
        if (!found) return -1;
 
        long duration = 0;
        
        duration += (years * 31536000000l);
        duration += (months * 2678400000l);
        duration += (weeks * 604800000l);
        duration += (days * 86400000l);
        duration += (hours * 3600000l);
        duration += (minutes * 60000l);
        duration += (seconds * 1000l);
 
        return duration;
        
    }
	
	private static int parseGroup (Matcher matcher, int groupNumber) {
		
    	String group = matcher.group(groupNumber);
 
        if (group == null || group.isEmpty()) return 0;
 
        return Integer.parseInt(group);
        
    }

}
