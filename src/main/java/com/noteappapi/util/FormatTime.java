package com.noteappapi.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatTime {
	public static String randomResponseTime() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return simpleDateFormat.format(new Date());
	}
}
