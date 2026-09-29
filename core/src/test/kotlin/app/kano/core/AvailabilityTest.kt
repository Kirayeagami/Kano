package app.kano.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailabilityTest {
    @Test fun unavailableFeaturesExplainTheirGate() {
        assertTrue(FeatureRequirements(false, 26).availability(35, emptySet()) is Availability.Unavailable)
        assertEquals(Availability.Unavailable("Requires Android API 35 or later."), FeatureRequirements(true, 35).availability(26, emptySet()))
        assertTrue(FeatureRequirements(true, 26, "usage").availability(35, emptySet()) is Availability.Unavailable)
        assertEquals(Availability.Available, FeatureRequirements(true, 26, "usage").availability(35, setOf("usage")))
    }
}
