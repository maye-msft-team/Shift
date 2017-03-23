# Shift
The Shift library project manages work schedules.  A work schedule consists of one or more teams who rotate through a sequence of shift and off-shift periods of time.  The Shift project allows breaks during shifts to be defined as well as non-working periods of time (e.g. holidays and scheduled maintenance periods) that are applicable to the entire work schedule.

## Concepts

The diagram below illustrates Business Management Systems' DNO (Day, Night, Off) work schedule with three teams and two 12-hour shifts with a 3-day rotation.  This schedule is explained in http://community.bmscentral.com/learnss/ZC/3T/c3tr12-1.

![Work Schedule Diagram](https://github.com/point85/shift/blob/master/doc/DNO.png)

*Shift*

A shift is defined with a name, description, starting time of day and duration.  An off-shift period is associated with a shift.  A rotation is a sequence of shifts and off-shift days, for example a "9-to-5" shift with a 5-on, 2-off rotation is a regular work week of 7 days.  An instance of a shift has a starting date and time of day and has an associated shift definition.

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

```
To obtain the working time over three days starting at 07:00, the following methods are called:

```java
LocalDateTime from = LocalDateTime.of(referenceDate, LocalTime.of(7, 0, 0));
Duration duration = schedule.calculateWorkingTime(from, from.plusDays(3));
```

To obtain the shift instances for a date, the following method is called:

```java
List<ShiftInstance> instances = schedule.getShiftInstancesForDay(LocalDate.of(2017, 3, 1)); 
```

To print a work schedule, call the toString() method.  For example:

```java
Schedule: DNO Plan (This is a fast rotation plan that uses 3 teams and two 12-hr shifts to provide 24/7 coverage. Each team rotates through the following sequence every three days: 1 day shift, 1 night shift, and 1 day off.)
Rotation duration: PT216H, Scheduled working time: PT72H
Shifts: 
   (1) Day (Day shift), Start : 07:00 (PT12H), End : 19:00
   (2) Night (Night shift), Start : 19:00 (PT12H), End : 07:00
Teams: 
   (1) Team 1 (First team), Rotation start: 2016-10-31, Rotation periods: [Day (on) , Night (on) , Night (off) ], Rotation duration: PT72H, Days in rotation: 3, Scheduled working time: PT24H, Percentage worked: 33.33%, Average hours worked per week: PT56H
   (2) Team 2 (Second team), Rotation start: 2016-10-30, Rotation periods: [Day (on) , Night (on) , Night (off) ], Rotation duration: PT72H, Days in rotation: 3, Scheduled working time: PT24H, Percentage worked: 33.33%, Average hours worked per week: PT56H
   (3) Team 3 (Third team), Rotation start: 2016-10-29, Rotation periods: [Day (on) , Night (on) , Night (off) ], Rotation duration: PT72H, Days in rotation: 3, Scheduled working time: PT24H, Percentage worked: 33.33%, Average hours worked per week: PT56H
Total team coverage: 100%
```

To print shift instances between two dates, the following method is called:

 ```java
schedule.printShiftInstances(LocalDate.of(2016, 10, 31), LocalDate.of(2016, 11, 3)));
```
with output:

```java
Working shifts
[1] Day: 2016-10-31
   (1) Team: Team 1, Shift: Day, Start : 2016-10-31T07:00, End : 2016-10-31T19:00
   (2) Team: Team 2, Shift: Night, Start : 2016-10-31T19:00, End : 2016-11-01T07:00
[2] Day: 2016-11-01
   (1) Team: Team 3, Shift: Day, Start : 2016-11-01T07:00, End : 2016-11-01T19:00
   (2) Team: Team 1, Shift: Night, Start : 2016-11-01T19:00, End : 2016-11-02T07:00
[3] Day: 2016-11-02
   (1) Team: Team 2, Shift: Day, Start : 2016-11-02T07:00, End : 2016-11-02T19:00
   (2) Team: Team 3, Shift: Night, Start : 2016-11-02T19:00, End : 2016-11-03T07:00
[4] Day: 2016-11-03
   (1) Team: Team 1, Shift: Day, Start : 2016-11-03T07:00, End : 2016-11-03T19:00
   (2) Team: Team 2, Shift: Night, Start : 2016-11-03T19:00, End : 2016-11-04T07:00
```
 
For a second example, the 24/7 schedule below has two rotations for four teams in two shifts.  It is used by manufacturing companies.

```java
WorkSchedule schedule = new WorkSchedule("Manufacturing Company - four twelves",
	"Four 12 hour alternating day/night shifts");

// day shift, start at 07:00 for 12 hours
Shift day = schedule.createShift("Day", "Day shift", LocalTime.of(7, 0, 0), Duration.ofHours(12));

// night shift, start at 19:00 for 12 hours
Shift night = schedule.createShift("Night", "Night shift", LocalTime.of(19, 0, 0), Duration.ofHours(12));

// 7 days ON, 7 OFF
Rotation dayRotation = new Rotation();
dayRotation.on(7, day).off(7, day);

// 7 nights ON, 7 OFF
Rotation nightRotation = new Rotation();
nightRotation.on(7, night).off(7, night);

schedule.createTeam("A", "A day shift", dayRotation, LocalDate.of(2014, 1, 2));
schedule.createTeam("B", "B night shift", nightRotation, LocalDate.of(2014, 1, 2));
schedule.createTeam("C", "C day shift", dayRotation, LocalDate.of(2014, 1, 9));
schedule.createTeam("D", "D night shift", nightRotation, LocalDate.of(2014, 1, 9));
```

When printed out with the shift instances for a rotation, the output is:

```java
Schedule: Manufacturing Company - four twelves (Four 12 hour alternating day/night shifts)
Rotation duration: PT1344H, Scheduled working time: PT336H
Shifts: 
   (1) Day (Day shift), Start : 07:00 (PT12H), End : 19:00
   (2) Night (Night shift), Start : 19:00 (PT12H), End : 07:00
Teams: 
   (1) A (A day shift), Rotation start: 2014-01-02, Rotation periods: [Day (on) , Day (on) , Day (on) , Day (on) , Day (on) , Day (on) , Day (on) , Day (off) , Day (off) , Day (off) , Day (off) , Day (off) , Day (off) , Day (off) ], Rotation duration: PT336H, Days in rotation: 14, Scheduled working time: PT84H, Percentage worked: 25%, Average hours worked per week: PT42H
   (2) B (B night shift), Rotation start: 2014-01-02, Rotation periods: [Night (on) , Night (on) , Night (on) , Night (on) , Night (on) , Night (on) , Night (on) , Night (off) , Night (off) , Night (off) , Night (off) , Night (off) , Night (off) , Night (off) ], Rotation duration: PT336H, Days in rotation: 14, Scheduled working time: PT84H, Percentage worked: 25%, Average hours worked per week: PT42H
   (3) C (C day shift), Rotation start: 2014-01-09, Rotation periods: [Day (on) , Day (on) , Day (on) , Day (on) , Day (on) , Day (on) , Day (on) , Day (off) , Day (off) , Day (off) , Day (off) , Day (off) , Day (off) , Day (off) ], Rotation duration: PT336H, Days in rotation: 14, Scheduled working time: PT84H, Percentage worked: 25%, Average hours worked per week: PT42H
   (4) D (D night shift), Rotation start: 2014-01-09, Rotation periods: [Night (on) , Night (on) , Night (on) , Night (on) , Night (on) , Night (on) , Night (on) , Night (off) , Night (off) , Night (off) , Night (off) , Night (off) , Night (off) , Night (off) ], Rotation duration: PT336H, Days in rotation: 14, Scheduled working time: PT84H, Percentage worked: 25%, Average hours worked per week: PT42H
Total team coverage: 100%
Working shifts
[1] Day: 2014-01-09
   (1) Team: C, Shift: Day, Start : 2014-01-09T07:00, End : 2014-01-09T19:00
   (2) Team: D, Shift: Night, Start : 2014-01-09T19:00, End : 2014-01-10T07:00
[2] Day: 2014-01-10
   (1) Team: C, Shift: Day, Start : 2014-01-10T07:00, End : 2014-01-10T19:00
   (2) Team: D, Shift: Night, Start : 2014-01-10T19:00, End : 2014-01-11T07:00
[3] Day: 2014-01-11
   (1) Team: C, Shift: Day, Start : 2014-01-11T07:00, End : 2014-01-11T19:00
   (2) Team: D, Shift: Night, Start : 2014-01-11T19:00, End : 2014-01-12T07:00
[4] Day: 2014-01-12
   (1) Team: C, Shift: Day, Start : 2014-01-12T07:00, End : 2014-01-12T19:00
   (2) Team: D, Shift: Night, Start : 2014-01-12T19:00, End : 2014-01-13T07:00
[5] Day: 2014-01-13
   (1) Team: C, Shift: Day, Start : 2014-01-13T07:00, End : 2014-01-13T19:00
   (2) Team: D, Shift: Night, Start : 2014-01-13T19:00, End : 2014-01-14T07:00
[6] Day: 2014-01-14
   (1) Team: C, Shift: Day, Start : 2014-01-14T07:00, End : 2014-01-14T19:00
   (2) Team: D, Shift: Night, Start : 2014-01-14T19:00, End : 2014-01-15T07:00
[7] Day: 2014-01-15
   (1) Team: C, Shift: Day, Start : 2014-01-15T07:00, End : 2014-01-15T19:00
   (2) Team: D, Shift: Night, Start : 2014-01-15T19:00, End : 2014-01-16T07:00
[8] Day: 2014-01-16
   (1) Team: A, Shift: Day, Start : 2014-01-16T07:00, End : 2014-01-16T19:00
   (2) Team: B, Shift: Night, Start : 2014-01-16T19:00, End : 2014-01-17T07:00
[9] Day: 2014-01-17
   (1) Team: A, Shift: Day, Start : 2014-01-17T07:00, End : 2014-01-17T19:00
   (2) Team: B, Shift: Night, Start : 2014-01-17T19:00, End : 2014-01-18T07:00
[10] Day: 2014-01-18
   (1) Team: A, Shift: Day, Start : 2014-01-18T07:00, End : 2014-01-18T19:00
   (2) Team: B, Shift: Night, Start : 2014-01-18T19:00, End : 2014-01-19T07:00
[11] Day: 2014-01-19
   (1) Team: A, Shift: Day, Start : 2014-01-19T07:00, End : 2014-01-19T19:00
   (2) Team: B, Shift: Night, Start : 2014-01-19T19:00, End : 2014-01-20T07:00
[12] Day: 2014-01-20
   (1) Team: A, Shift: Day, Start : 2014-01-20T07:00, End : 2014-01-20T19:00
   (2) Team: B, Shift: Night, Start : 2014-01-20T19:00, End : 2014-01-21T07:00
[13] Day: 2014-01-21
   (1) Team: A, Shift: Day, Start : 2014-01-21T07:00, End : 2014-01-21T19:00
   (2) Team: B, Shift: Night, Start : 2014-01-21T19:00, End : 2014-01-22T07:00
[14] Day: 2014-01-22
   (1) Team: A, Shift: Day, Start : 2014-01-22T07:00, End : 2014-01-22T19:00
   (2) Team: B, Shift: Night, Start : 2014-01-22T19:00, End : 2014-01-23T07:00
[15] Day: 2014-01-23
   (1) Team: C, Shift: Day, Start : 2014-01-23T07:00, End : 2014-01-23T19:00
   (2) Team: D, Shift: Night, Start : 2014-01-23T19:00, End : 2014-01-24T07:00
```

## Project Structure
Shift depends upon Java 8+ due to use of the java time classes.  The unit tests depend on JUnit (http://junit.org/junit4/) and Hamcrest (http://hamcrest.org/).

Shift, when built with Gradle, has the following structure:
 * `/build/docs/javadoc` javadoc files
 * `/build/libs` compiled shift.jar 
 * `/doc` documentation
 * `/src/main/java` - java source files
 * `/src/main/resources` - localizable Message.properties file to define error messages.
 * `/src/test/java` - JUnit test java source files 
 
When Shift is built with Maven, the javadoc and jar files are in the 'target' folder.
