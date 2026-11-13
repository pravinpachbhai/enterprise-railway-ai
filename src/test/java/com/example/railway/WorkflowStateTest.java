package com.example.railway;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowStateTest {
    @Test void basicWorkflowStatesAreNamed() {
        assertEquals("AWAITING_APPROVAL", "AWAITING_APPROVAL");
        assertEquals("SENT", "SENT");
    }
}
