package com.example.swapify;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DBObjectInstrumentedTest {
    private DBObject dbObject;

    @Before
    public void setup() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbObject = new DBObject(appContext);
        dbObject.deleteAll(); // Clear the database before each test
    }

    @After
    public void tearDown() {
        dbObject.deleteAll(); // Clear the database after each test
    }

    @Test
    public void testRegisterUser() {
        // Create a mock user for testing
        CustomerModel mockUser = new CustomerModel("John Doe", "john@example.com", "johndoe", "password123");

        // Register the mock user in the DBObject
        boolean registrationResult = dbObject.addOne(mockUser);

        // Assert that the registration was successful
        assertTrue(registrationResult);

        // Check if the registered user exists in the database
        boolean userExists = dbObject.emailExists(mockUser.getEmail());

        // Assert that the user exists in the database
        assertTrue(userExists);
    }
}
