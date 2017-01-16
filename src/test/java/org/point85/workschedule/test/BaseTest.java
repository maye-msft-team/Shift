package org.point85.workschedule.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.BeforeClass;
import org.point85.workschedule.NonWorkingPeriod;
import org.point85.workschedule.Shift;
import org.point85.workschedule.ShiftInstance;
import org.point85.workschedule.ShiftRotation;
import org.point85.workschedule.Team;
import org.point85.workschedule.WorkSchedule;

/**
 * Base class for testing shift plans from
 * //community.bmscentral.com/learnss/Tutorials/SchedulePlans/
 * 
 * @author Kent
 *
 */
public abstract class BaseTest {
	public static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	protected static final BigDecimal DELTA3 = new BigDecimal("0.001", MATH_CONTEXT);

	// reference date for start of shift rotations
	protected LocalDate referenceDate = LocalDate.of(2016, 10, 31);

	private static boolean testToString = false;

	@BeforeClass
	public static void setFlags() {
		testToString = true;
	}

	private void testShifts(WorkSchedule ws) throws Exception {
		assertTrue(ws.getShifts().size() > 0);

		for (Shift shift : ws.getShifts()) {
			Duration total = shift.getDuration();
			LocalTime start = shift.getStart();
			LocalTime end = shift.getEnd();

			assertTrue(shift.getName().length() > 0);
			assertTrue(shift.getDescription().length() > 0);

			assertTrue(total.toMinutes() > 0);
			assertTrue(shift.getBreaks().size() >= 0);
			assertTrue(start != null);
			assertTrue(end != null);

			Duration worked = shift.getWorkingTimeBetween(start, end);
			assertTrue(worked.equals(total));

			worked = shift.getWorkingTimeBetween(start, start);

			// 24 hour shift on midnight is a special case
			if (start.equals(LocalTime.MIDNIGHT) && end.equals(LocalTime.MIDNIGHT)) {
				assertTrue(worked.toHours() == 24);
			} else {
				assertTrue(worked.toMinutes() == 0);
			}

			worked = shift.getWorkingTimeBetween(end, end);

			if (start.equals(LocalTime.MIDNIGHT) && end.equals(LocalTime.MIDNIGHT)) {
				assertTrue(worked.toHours() == 24);
			} else {
				assertTrue(worked.toMinutes() == 0);
			}

			try {
				LocalTime t = start.minusMinutes(1);
				worked = shift.getWorkingTimeBetween(t, end);

				if (!start.equals(LocalTime.MIDNIGHT) && !end.equals(LocalTime.MIDNIGHT)) {
					fail("Bad working time");
				}
			} catch (Exception e) {
			}

			try {
				LocalTime t = end.plusMinutes(1);
				worked = shift.getWorkingTimeBetween(start, t);
				if (!start.equals(LocalTime.MIDNIGHT) && !end.equals(LocalTime.MIDNIGHT)) {
					fail("Bad working time");
				}
			} catch (Exception e) {
			}
		}
	}

	private void testTeams(WorkSchedule ws, Duration hoursPerRotation, Duration rotationDays) throws Exception {
		assertTrue(ws.getTeams().size() > 0);

		for (Team team : ws.getTeams()) {
			assertTrue(team.getName().length() > 0);
			assertTrue(team.getDescription().length() > 0);
			assertTrue(team.getDayInRotation(team.getRotationStart()) == 0);
			Duration hours = team.getShiftRotation().getWorkingTime();
			assertTrue(hours.equals(hoursPerRotation));
			assertTrue(team.getPercentageWorked() > 0.0f);
			assertTrue(team.getRotationDuration().equals(rotationDays));
			assertTrue(team.getRotationStart() != null);

			ShiftRotation rotation = team.getShiftRotation();
			assertTrue(rotation.getDuration().equals(rotationDays));
			assertTrue(rotation.getPeriods().size() > 0);
			assertTrue(rotation.getWorkingTime().getSeconds() <= rotation.getDuration().getSeconds());
		}

		assertTrue(ws.getNonWorkingPeriods().size() >= 0);
	}

	private void testShiftInstances(WorkSchedule ws, LocalDate instanceReference) throws Exception {
		ShiftRotation rotation = ws.getTeams().get(0).getShiftRotation();

		// shift instances
		LocalDate startDate = instanceReference;
		LocalDate endDate = instanceReference.plusDays(rotation.getDuration().toDays());

		long days = endDate.toEpochDay() - instanceReference.toEpochDay() + 1;
		LocalDate day = startDate;

		for (long i = 0; i < days; i++) {
			List<ShiftInstance> instances = ws.getShiftInstancesForDay(day);

			for (ShiftInstance instance : instances) {
				assertTrue(instance.getStartTime().isBefore(instance.getEndTime()));
				assertTrue(instance.getShift() != null);
				assertTrue(instance.getTeam() != null);

				Shift shift = instance.getShift();
				LocalTime startTime = shift.getStart();
				LocalTime endTime = shift.getEnd();

				assertTrue(shift.isInShift(startTime));
				assertTrue(shift.isInShift(startTime.plusSeconds(1)));

				// midnight is special case
				if (!startTime.equals(LocalTime.MIDNIGHT) && !endTime.equals(LocalTime.MIDNIGHT)) {
					assertFalse(shift.isInShift(startTime.minusSeconds(1)));
				}

				assertTrue(shift.isInShift(endTime));
				assertTrue(shift.isInShift(endTime.minusSeconds(1)));

				if (!startTime.equals(LocalTime.MIDNIGHT) && !endTime.equals(LocalTime.MIDNIGHT)) {
					assertFalse(shift.isInShift(endTime.plusSeconds(1)));
				}

				LocalDateTime ldt = LocalDateTime.of(day, startTime);
				assertTrue(ws.getShiftInstancesForTime(ldt).size() > 0);

				ldt = LocalDateTime.of(day, startTime.plusSeconds(1));
				assertTrue(ws.getShiftInstancesForTime(ldt).size() > 0);

				ldt = LocalDateTime.of(day, startTime.minusSeconds(1));

				for (ShiftInstance si : ws.getShiftInstancesForTime(ldt)) {
					if (!startTime.equals(LocalTime.MIDNIGHT) && !endTime.equals(LocalTime.MIDNIGHT)) {
						assertFalse(shift.getName().equals(si.getShift().getName()));
					}
				}

				ldt = LocalDateTime.of(day, endTime);
				assertTrue(ws.getShiftInstancesForTime(ldt).size() > 0);

				ldt = LocalDateTime.of(day, endTime.minusSeconds(1));
				assertTrue(ws.getShiftInstancesForTime(ldt).size() > 0);

				ldt = LocalDateTime.of(day, endTime.plusSeconds(1));

				for (ShiftInstance si : ws.getShiftInstancesForTime(ldt)) {
					if (!startTime.equals(LocalTime.MIDNIGHT) && !endTime.equals(LocalTime.MIDNIGHT)) {
						assertFalse(shift.getName().equals(si.getShift().getName()));
					}
				}
			}

			day = day.plusDays(1);
		}

	}

	protected void runBaseTest(WorkSchedule ws, Duration hoursPerRotation, Duration rotationDays, LocalDate instanceReference) throws Exception {

		// toString
		if (testToString) {
			System.out.println(ws.toString());
			ws.printShiftInstances(instanceReference, instanceReference.plusDays(rotationDays.toDays()));
		}

		assertTrue(ws.getName().length() > 0);
		assertTrue(ws.getDescription().length() > 0);
		assertTrue(ws.getNonWorkingPeriods().size() >= 0);

		// shifts
		testShifts(ws);

		// teams
		testTeams(ws, hoursPerRotation, rotationDays);

		// shift instances
		testShiftInstances(ws, instanceReference);

		// team deletions
		Team[] teams = new Team[ws.getTeams().size()];
		ws.getTeams().toArray(teams);

		for (Team team : teams) {
			ws.deleteTeam(team);
		}
		assertTrue(ws.getTeams().size() == 0);

		// shift deletions
		Shift[] shifts = new Shift[ws.getShifts().size()];
		ws.getShifts().toArray(shifts);

		for (Shift shift : shifts) {
			ws.deleteShift(shift);
		}
		assertTrue(ws.getShifts().size() == 0);

		// non-working period deletions
		NonWorkingPeriod[] periods = new NonWorkingPeriod[ws.getNonWorkingPeriods().size()];
		ws.getNonWorkingPeriods().toArray(periods);

		for (NonWorkingPeriod period : periods) {
			ws.deleteNonWorkingPeriod(period);
		}
		assertTrue(ws.getNonWorkingPeriods().size() == 0);

	}

}
