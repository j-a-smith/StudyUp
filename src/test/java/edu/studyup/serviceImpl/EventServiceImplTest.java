package edu.studyup.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

class EventServiceImplTest {

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

	@Test // This assertion is thrown, but it shouldn't because it should allow 20 characters, hence the test case passses (BUG)
	void testUpdateEvent_GoodInput_goodCase() throws StudyUpException {
		int eventID = 1;
		String emptyStr = "12345678912345678912"; //20 Characters Exactly but program throws error when it shouldn't
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.updateEventName(eventID, emptyStr);
		});
	}

	@Test // Get the Active Events and there should be only one! Pass #3
	void testGetActiveEvents_Good() {
		int eventID = 1;
		List<Event> activeEvents = eventServiceImpl.getActiveEvents();
		assertEquals(1, activeEvents.size()); //
	}

	@Test // Create a new student and a new event with old date. This passes when it should fail because I put the new event way in the past.
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
		Date d1 = new Date();
		int d2 = (d1.getDate() - 1000); // Creates an old date, but the getActiveEvents doesn't work as intended.
		Date d3 = new Date(d2);
		event.setEventID(2);
		event.setDate(d3);
		event.setName("Event 2");

		Location location = new Location(-100, 100);
		event.setLocation(location);

		List<Student> eventStudents = new ArrayList<>();
		eventStudents.add(student);
		event.setStudents(eventStudents);

		DataStorage.eventData.put(event.getEventID(), event); // Add another event

		List<Event> currentEvents = eventServiceImpl.getActiveEvents();
		assertEquals(2, currentEvents.size());
	}

	@Test // This test case should pass, creates an old event and checks for it in the past events. #5
	void testGetPastEvents_Good(){
		int eventID = 1;
		Date d1 = new Date();
		int d2 = (d1.getDate() - 1000);  // These series of steps creates an old date.
		Date d3 = new Date(d2);

		DataStorage.eventData.get(eventID).setDate(d3);
		List<Event> pastEvents = eventServiceImpl.getPastEvents();
		assertEquals(1, pastEvents.size());
	}
}