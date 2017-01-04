/*
MIT License

Copyright (c) 2016 Kent Randall

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package org.point85.workschedule;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class WorkSchedule extends Named {
	// name of resource bundle with translatable strings for exception messages
	static final String MESSAGES_BUNDLE_NAME = "Message";

	// resource bundle for exception messages
	private static ResourceBundle messages  = ResourceBundle.getBundle(MESSAGES_BUNDLE_NAME, Locale.getDefault());
	
	// list of teams
	private List<Team> teams = new ArrayList<>();

	// list of shifts
	private List<Shift> shifts = new ArrayList<>();

	// holidays and planned downtime
	private List<NonWorkingPeriod> nonWorkingPeriods = new ArrayList<>();

	public WorkSchedule(String name, String description) {
		super(name, description);
	}
	
	// get a particular message by its key
	static String getMessage(String key) {
		return messages.getString(key);
	}

	public void addTeam(Team team) {
		if (!this.teams.contains(team)) {
			this.teams.add(team);
		}
	}

	public void removeTeam(Team team) {
		if (this.teams.contains(team)) {
			this.teams.remove(team);
		}
	}

	public List<Team> getTeams() {
		return this.teams;
	}

	public void addNonWorkingPeriod(NonWorkingPeriod period) {
		if (!this.nonWorkingPeriods.contains(period)) {
			this.nonWorkingPeriods.add(period);
		}
	}

	public void removeNonWorkingPeriod(NonWorkingPeriod period) {
		if (this.nonWorkingPeriods.contains(period)) {
			this.nonWorkingPeriods.remove(period);
		}
	}

	public List<NonWorkingPeriod> getNonWorkingPeriods() {
		return this.nonWorkingPeriods;
	}

	public List<ShiftInstance> getShiftInstancesForDay(LocalDate day) throws Exception {
		List<ShiftInstance> workingShifts = new ArrayList<>();

		if (this.nonWorkingPeriods.contains(day)) {
			return workingShifts;
		}

		long dayTo = day.getLong(ChronoField.EPOCH_DAY);

		// for each team see if there is a working shift
		for (Team team : teams) {
			ShiftRotation shiftRotation = team.getShiftRotation();
			int dayInRotation = team.getDayInRotation(day);

			// shift or off shift
			TimePeriod period = shiftRotation.getPeriods().get(dayInRotation);

			if (period.isWorkingPeriod()) {
				LocalDateTime startDateTime = LocalDateTime.of(day, period.getStart());
				ShiftInstance instance = new ShiftInstance((Shift) period, startDateTime, team);
				workingShifts.add(instance);
			}
		}

		Collections.sort(workingShifts);

		return workingShifts;
	}

	private long getNumberOfRotations(LocalDate from, LocalDate to) {
		long dayFrom = from.getLong(ChronoField.EPOCH_DAY);
		long dayTo = to.getLong(ChronoField.EPOCH_DAY);
		long deltaDays = dayTo - dayFrom;

		return deltaDays / getRotationDays().toDays();
	}

	public Duration getRotationDays() {
		Duration duration = null;
		// each team has the same number of days in their rotation
		if (getTeams().size() > 0) {
			duration = getTeams().get(0).getShiftRotation().getDuration();
		}

		return duration;
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		String text = "Schedule: " + super.toString();

		try {
			text += "\nRotation duration: " + getRotationDuration() + ", scheduled working time " + getWorkingTime();

			// shifts
			text += "\nShifts: ";
			int count = 1;
			for (Shift shift : getShifts()) {
				text += "\n   (" + count + ") " + shift;
				count++;
			}

			// teams
			text += "\nTeams: ";
			count = 1;
			float teamPercent = 0.0f;
			for (Team team : this.getTeams()) {
				text += "\n   (" + count + ") " + team;
				teamPercent += team.getPercentageWorked();
				count++;
			}
			text += "\nTotal team coverage: " + df.format(teamPercent) + "%";

			// non-working periods
			if (getNonWorkingPeriods().size() > 0) {
				text += "\nNon-working periods:";

				Duration totalMinutes = Duration.ofMinutes(0);

				count = 1;
				for (NonWorkingPeriod period : getNonWorkingPeriods()) {
					totalMinutes = totalMinutes.plusMinutes(period.getDuration().toMinutes());
					text += "\n   (" + count + ") " + period;
					count++;
				}
				text += "\nTotal non-working time: " + totalMinutes;
			}

		} catch (Exception e) {
			text = e.getMessage();
		}
		return text;
	}

	public Duration calculateTeamWorkingTime(LocalDate from, LocalDate to) {
		return null;
	}

	/*
	 * public Map<Team, Duration> calculateTeamWorkingTime(LocalDate from,
	 * LocalDate to) {
	 * 
	 * Map<Team, Duration> workingTime = new HashMap<Team, Duration>();
	 * 
	 * LocalDate currentDate = from;
	 * 
	 * // iterate over each day while (currentDate.isBefore(to)) {
	 * List<TeamInstance> shifts = getTeamsForDay(currentDate);
	 * 
	 * for (TeamInstance shift : shifts) { Duration duration =
	 * shift.getTeam().calculateWorkingTime();
	 * 
	 * Duration sum = workingTime.get(shift.getTeam());
	 * 
	 * if (sum == null) { sum = duration; } else { sum = sum.plus(duration); }
	 * workingTime.put(shift.getTeam(), sum); }
	 * 
	 * currentDate = currentDate.plusDays(1); }
	 * 
	 * return workingTime; }
	 */
	public boolean isHoliday(LocalDate date) {
		return this.nonWorkingPeriods.contains(date);
	}

	public Team createTeam(String name, String description, ShiftRotation rotation, LocalDate rotationStart) {
		Team team = new Team(name, description, rotation, rotationStart);
		this.addTeam(team);
		return team;
	}

	public Shift createShift(String name, String description, LocalTime start, Duration duration) {
		Shift shift = new Shift(name, description, start, duration);
		shifts.add(shift);
		return shift;
	}

	public OffShift createOffShift(String name, String description, LocalTime start, Duration duration) {
		return new OffShift(name, description, start, duration);
	}

	public NonWorkingPeriod createNonWorkingPeriod(String name, String description, LocalDateTime startDateTime,
			Duration duration) {
		NonWorkingPeriod period = new NonWorkingPeriod(name, description, startDateTime, duration);
		this.addNonWorkingPeriod(period);
		return period;
	}

	public Duration getRotationDuration() throws Exception {
		Duration sum = null;
		for (Team team : teams) {
			if (sum == null) {
				sum = team.getRotationDuration();
			} else {
				sum = sum.plus(team.getRotationDuration());
			}
		}
		return sum;
	}

	public Duration getWorkingTime() {
		Duration sum = null;
		for (Team team : teams) {
			if (sum == null) {
				sum = team.getShiftRotation().getWorkingTime();
			} else {
				sum = sum.plus(team.getShiftRotation().getWorkingTime());
			}
		}
		return sum;
	}

	public List<Shift> getShifts() {
		return shifts;
	}

	public void setShifts(List<Shift> shifts) {
		this.shifts = shifts;
	}

	public void printShiftInstances(LocalDate start, LocalDate end) throws Exception {
		if (start.isAfter(end)) {
			throw new Exception("Start of " + start + " must be earlier than end of " + end);
		}

		long days = end.toEpochDay() - start.toEpochDay();

		LocalDate day = start;

		System.out.println("Working shifts:");
		for (long i = 0; i < days; i++) {
			System.out.println("[" + (i + 1) + "] Day: " + day);

			List<ShiftInstance> instances = getShiftInstancesForDay(day);

			if (instances.size() == 0) {
				System.out.println("   No working shifts");
			} else {
				int count = 1;
				for (ShiftInstance instance : instances) {
					System.out.println("   (" + count + ")" + instance);
					count++;
				}
			}
			day = day.plusDays(1);
		}
	}
}
