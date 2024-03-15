package anon.subevents.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private Date previousDate = null;

	public String printTimeDiff() {

		Date currentDate = new Date();

		if (this.previousDate == null) {
			this.previousDate = currentDate;
			return dateFormat.format(currentDate);
		} else {
			long ms = (currentDate.getTime() - this.previousDate.getTime());
			this.previousDate = currentDate;
			return (Long.toString(ms) + "ms");
		}

	}

	public static String printMemory() {
		return Long.toString(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	}
	
}
