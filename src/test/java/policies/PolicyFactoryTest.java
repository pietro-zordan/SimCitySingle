package policies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolicyFactoryTest {

    @Test
    void testCreateStandardPolicy() {
        Policy policy = PolicyFactory.create(PolicyType.STANDARD);
        assertNotNull(policy);
        assertTrue(policy instanceof StandardPolicy);
        assertEquals(PolicyType.STANDARD, policy.getType());
    }

    @Test
    void testCreateEnvironmentalPolicy() {
        Policy policy = PolicyFactory.create(PolicyType.ENVIRONMENTAL);
        assertNotNull(policy);
        assertTrue(policy instanceof EnvironmentalPolicy);
        assertEquals(PolicyType.ENVIRONMENTAL, policy.getType());
    }

    @Test
    void testCreateIndustrialPolicy() {
        Policy policy = PolicyFactory.create(PolicyType.INDUSTRIAL);
        assertNotNull(policy);
        assertTrue(policy instanceof IndustrialPolicy);
        assertEquals(PolicyType.INDUSTRIAL, policy.getType());
    }

    @Test
    void testCreateWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PolicyFactory.create(null));
    }
}