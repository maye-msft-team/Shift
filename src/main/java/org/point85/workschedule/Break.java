package org.point85.workschedule;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Class Break is a defined working period of time during a shift, e.g. lunch.
 * @author Kent Randall
 *
 */
public class Break extends TimePeriod {

	/**
	 * Construct a period of time for a break
	 * 
	 * @param name
	 *            Name of break
	 * @param description
	 *            Description of break
	 * @param start
	 *            Starting time of day
	 * @param duration
	 *            Duration of break
	 * @throws Exception 
	 */
	public Break(String name, String description, LocalTime start, Duration duration) throws Exception {
		super(name, description, start, duration);
	}

	// breaks are considered to be in the shift's working period
	@Override
	boolean isWorkingPeriod() {
		return true;
	}
}
