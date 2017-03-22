# Shift
The Shift library project manages work schedules with teams who rotate through a sequence of shift and off-shift periods of time.  The Shift project allows breaks during shifts to be defined as well as non-working periods of time (e.g. holidays and scheduled maintenance periods) that are applicable to the entire work schedule.

## Concepts

The diagram below illustrates Business Management Systems' DNO (Day, Night, Off) work schedule with three teams and two 12-hour shifts as explained in http://community.bmscentral.com/learnss/ZC/3T/c3tr12-1.

![Work Schedule Diagram](https://github.com/point85/shift/blob/master/doc/DNO.png)

*Shift*
A shift is defined with a name, description, starting time of day and duration.  An off-shift period is associated with a shift.  A rotation is a sequence of shifts and off-shift days, for example a "9-to-5" shift with a 5-on, 2-off rotation is a regular work week fo 7 days.  An instance of a shift has a starting date and time of day and has an associated shift definition.

*Team*
A team is defined with a name and description.  It has a rotation with a starting date.  The first shift will have an instance with that date and starting time of day defined in the shift.  The same rotation can be shared between more than one team, but with different starting times.

*Work Schedule*
A work schedule is defined with a name and description.  It has one or more teams.  Zero or more non-working periods can be defined.  A non-working period has a defined starting date and time of day and duration.  For example, New Year's Day holiday starting at midnight or three consecutive days for preventive maintenance of equipment starting at the end of the night shift. 

After a work schedule is defined, the working time for all shifts can be computed for a defined time interval.  For example, this duration of time is the maximum available time as an input to the utilization of equipment in a metric known as the Overall Equipment Effectiveness (OEE).

## Code Examples
The schedule discussed above is defined as follows.

```java
String description = "This is a fast rotation plan that uses 3 teams and two 12-hr shifts to provide 24/7 coverage. "
	+ "Each team rotates through the following sequence every three days: 1 day shift, 1 night shift, and 1 day off.";

WorkSchedule schedule = new WorkSchedule("DNO Plan", description);

// Day shift, starts at 07:00 for 12 hours
Shift day = schedule.createShift("Day", "Day shift", LocalTime.of(7, 0, 0), Duration.ofHours(12));

// Night shift, starts at 19:00 for 12 hours
Shift night = schedule.createShift("Night", "Night shift", LocalTime.of(19, 0, 0), Duration.ofHours(12));

// rotation
Rotation rotation = new Rotation();
rotation.on(1, day).on(1, night).off(1, night);

// create the teams
// reference date for start of shift rotations
LocalDate referenceDate = LocalDate.of(2016, 10, 31);
	
schedule.createTeam("Team 1", "First team", rotation, referenceDate);
schedule.createTeam("Team 2", "Second team", rotation, referenceDate.minusDays(1));
schedule.createTeam("Team 3", "Third team", rotation, referenceDate.minusDays(2));

// get the working time over 3 days starting at 07:00.
LocalDateTime from = LocalDateTime.of(referenceDate, LocalTime.of(7, 0, 0));
Duration duration = schedule.calculateWorkingTime(from, from.plusDays(3));
```

## Project Structure
Shift depends upon Java 7+ due to use of the java time classes.  The unit tests depend on JUnit (http://junit.org/junit4/) and Hamcrest (http://hamcrest.org/).

Shift, when built with Gradle, has the following structure:
 * `/build/docs/javadoc` javadoc files
 * `/build/libs` compiled shift.jar 
 * `/doc` documentation
 * `/src/main/java` - java source files
 * `/src/main/resources` - localizable Message.properties file to define error messages.
 * `/src/test/java` - JUnit test java source files 
 
When Shift is built with Maven, the javadoc and jar files are in the 'target' folder.
