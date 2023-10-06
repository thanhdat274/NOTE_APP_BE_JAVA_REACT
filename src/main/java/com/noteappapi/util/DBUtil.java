package com.noteappapi.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBUtil {
		public static Integer countPayments(Object object) {
		Integer count = 0;
		try {
			count = object.getClass().getDeclaredFields().length;
		} catch (Exception e) {
			log.error("Error while counting attributes: ", e);
		}
		return count;
	}
}
