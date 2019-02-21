package edu.studyup.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;


import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar; // Not Deprecated
import java.util.List;

import java.text.SimpleDateFormat; // For Formatting the Date

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.studyup.entity.Event;
import edu.studyup.entity.Location;
import edu.studyup.entity.Student;
import edu.studyup.util.DataStorage;
import edu.studyup.util.StudyUpException;

import java.time.Instant;

class EventServiceImplTest {
	
	Student generateTestStudent(int ID) {
		String IDstring = Integer.toString(ID);
		Student testStudent = new Student();
		testStudent.setFirstName("first" + IDstring);
		testStudent.setLastName("last" + IDstring);
		testStudent.setEmail("test" + IDstring + "@email.com");
		testStudent.setId(ID);
		return testStudent;
	}

	EventServiceImpl eventServiceImpl;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		eventServiceImpl = new EventServiceImpl();
		//Create Student
		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("JohnDoe@email.com");
		student.setId(1);
		
		//Create Event1
		Event event = new Event();
		event.setEventID(1);
		event.setDate(new Date());
		event.setName("Event 1");
		Location location = new Location(-122, 37);
		event.setLocation(location);
		List<Student> eventStudents = new ArrayList<>();
		eventStudents.add(student);
		event.setStudents(eventStudents);
		
		DataStorage.eventData.put(event.getEventID(), event);
	}

	@AfterEach
	void tearDown() throws Exception {
		DataStorage.eventData.clear();
	}

	@Test
	void testUpdateEventName_GoodCase() throws StudyUpException {
		int eventID = 1;
		eventServiceImpl.updateEventName(eventID, "Renamed Event 1");
		assertEquals("Renamed Event 1", DataStorage.eventData.get(eventID).getName());
	}
	
	@Test
	void testUpdateEvent_WrongEventID_badCase() {
		int eventID = 3;
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.updateEventName(eventID, "Renamed Event 3");
		  });
	}
	
	/* Author: Maung Naing */ 
	@Test // Expecting this assertion to be thrown. #1
	void testUpdateEvent_InvalidName_badCase() throws StudyUpException { 
		int eventID = 1;
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.updateEventName(eventID, "123456789123456789123"); // 21 Character Name
		});
	}

	@Test // This test case tries to update the event name to 20 characters which should work, but it failed and an exception is thrown (BUG)
	void testUpdateEvent_GoodInput_goodCase() throws StudyUpException {
		int eventID = 1;
		String emptyStr = "12345678912345678912"; //20 Characters Exactly but program throws error when it shouldn't
		try {
			eventServiceImpl.updateEventName(eventID, emptyStr);
		} catch (StudyUpException e) {
			fail("Doesn't allow 20 characters, when it says it should");
		}
	}

	@Test // Get the Active Events and there should be only one! Pass #3
	void testGetActiveEvents_Good() {
		int eventID = 1;
		List<Event> activeEvents = eventServiceImpl.getActiveEvents();
		assertEquals(1, activeEvents.size()); //
	}

	@Test // Create a new student and a new event with old date. This should only return one active event, but the assertion fails and test fails.
	// (BUG) #4
	void  testGetActiveEvents_Bad() {
		//Create Student2
		Student student = new Student();
		student.setFirstName("Test");
		student.setLastName("Two");
		student.setEmail("TestTwo@email.com");
		student.setId(2);
		
		//Create Event2 that has an old date
		Event event = new Event();
		
		//Date d1 = new Date();
		//long d2 = (d1.getDate() - 1000); // Creates an old date, but the getActiveEvents doesn't work as intended.
		//Date d3 = new Date(d2);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -10000); // Create an old date
		new SimpleDateFormat("yyyy-MM-dd"); // Sets the date format to this
		
		event.setEventID(2);
		event.setDate(cal.getTime()); // Gets the date and sets the date.
		event.setName("Event 2");

		Location location = new Location(-100, 100);
		event.setLocation(location);

		List<Student> eventStudents = new ArrayList<>();
		eventStudents.add(student);
		event.setStudents(eventStudents);

		DataStorage.eventData.put(event.getEventID(), event); // Add another event

		List<Event> currentEvents = eventServiceImpl.getActiveEvents();
		assertEquals(1, currentEvents.size()); // Active Event should be only one!
	}

	@Test // This test case should pass, creates an old event and checks for it in the past events. #5
	void testGetPastEvents_Good(){
		int eventID = 1;
		//Date d1 = new Date();
		//long d2 = (d1.getDate() - 1000);  // These series of steps creates an old date.
		//Date d3 = new Date(d2);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -10000); // Create an old date
		new SimpleDateFormat("yyyy-MM-dd"); // Sets the date format to this

		DataStorage.eventData.get(eventID).setDate(cal.getTime());
		List<Event> pastEvents = eventServiceImpl.getPastEvents();
		assertEquals(1, pastEvents.size());
	}
	
	/* Author: Jacob Smith */
	@Test	// Sets event date to a future date. Should return empty list.
	void testGetPastEvents_FutureEvent() {
		int eventID = 1;
		long currentTime = Instant.now().toEpochMilli();
		long oneWeekOffset = 604800000;
		long futureTime = currentTime + oneWeekOffset;
		Date futureDate = new Date(futureTime);
		
		DataStorage.eventData.get(eventID).setDate(futureDate);
		List<Event> pastEvents = eventServiceImpl.getPastEvents();
		assertEquals(0, pastEvents.size());
	}
	
	@Test	// Adds one student to event (total number of students: 2).
	void testAddStudentToEvent_Good() throws StudyUpException {
		int eventID = 1;
		
		Student newStudent = new Student();
		newStudent.setFirstName("TestFirst");
		newStudent.setLastName("TestLast");
		newStudent.setEmail("Test@email.com");
		newStudent.setId(2);
		
		Event updatedEvent = eventServiceImpl.addStudentToEvent(newStudent, eventID);
		List<Student> associatedStudents = updatedEvent.getStudents();
		assertEquals(2, associatedStudents.size());
	}
	
	@Test	// Adds 2 students to event (total number of students: 3). Violates event constraint.
	void testAddStudentToEvent_Bad() throws StudyUpException {
		int eventID = 1;
		Student testStudent1 = generateTestStudent(2);
		Student testStudent2 = generateTestStudent(3);
		eventServiceImpl.addStudentToEvent(testStudent1, eventID);
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.addStudentToEvent(testStudent2, eventID);
		  });
	}
	
	@Test
	void testAddStudentToEvent_NonexistentEvent() throws StudyUpException {
		int badEventID = 2;
		Student newStudent = generateTestStudent(2);
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.addStudentToEvent(newStudent, badEventID);
		  });
	}
	
	@Test
	void testAddStudentToEvent_FirstStudent() throws StudyUpException {
		int newEventID = 2;
		Event newEvent = new Event();
		newEvent.setEventID(newEventID);
		newEvent.setDate(new Date());
		newEvent.setLocation(new Location(100, -100));
		DataStorage.eventData.put(newEventID, newEvent);
		
		Student newStudent = generateTestStudent(2);
		Event returnEvent = eventServiceImpl.addStudentToEvent(newStudent, newEventID);
		List<Student> studentList = returnEvent.getStudents();
		assertEquals(1, studentList.size());
	}
	
	@Test	// Deletes existing event
	void testDeleteEvent_ExistingEvent() {
		int eventID = 1;
		Event deletedEvent = eventServiceImpl.deleteEvent(eventID);
		assertEquals(eventID, deletedEvent.getEventID());
	}
	
	@Test	// Attempts to delete a nonexistent event
	void testDeleteEvent_NonexistentEvent() {
		int fakeEventID = 2;
		Event deletedEvent = eventServiceImpl.deleteEvent(fakeEventID);
		Assertions.assertNull(deletedEvent);
	}
}