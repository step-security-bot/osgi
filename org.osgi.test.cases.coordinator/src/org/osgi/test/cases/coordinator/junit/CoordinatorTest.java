package org.osgi.test.cases.coordinator.junit;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.coordinator.Coordination;
import org.osgi.service.coordinator.CoordinationException;
import org.osgi.service.coordinator.Coordinator;
import org.osgi.test.support.OSGiTestCase;

/**
 *	Contains utilities used by other tests.
 */
public abstract class CoordinatorTest extends OSGiTestCase {
	/**
	 * The Coordinator service.
	 */
	protected Coordinator coordinator;
	/**
	 * The Coordinator service reference.
	 */
	protected ServiceReference coordinatorReference;
	
	/**
	 * Ensures the provided Coordination is at the top of the current thread's
	 * local stack.
	 * @param c The Coordination whose stack will be used.
	 */
	protected void assertAtTopOfStack(Coordination c) {
		assertEquals("The coordination must be at the top of the thread local stack.", c, coordinator.peek());
	}
	
	/**
	 * Ensures a CoordinationException occurred, is of the expected type, and
	 * has the expected cause.
	 * @param e The CoordinationException, if any, that occurred.
	 * @param expectedType The expected type of CoordinationException.
	 * @param expectedCause The expected cause of the CoordinationException.
	 */
	protected void assertCoordinationException(CoordinationException e, int expectedType, Throwable expectedCause) {
		assertNotNull("A CoordinationException did not occur", e);
		assertEquals("The CoordinationException type was incorrect.", expectedType, e.getType());
		assertEquals("The CoordinationException cause was incorrect", expectedCause, e.getCause());
	}
	
	/**
	 * Ensures the current thread's local stack is empty.
	 */
	protected void assertEmptyStack() {
		assertNull("The thread local stack must be empty.", coordinator.peek());
	}
	
	/**
	 * Ensures the provided Coordination does not end successfully and that the
	 * resulting CoordinationException is of the expected type and has the
	 * expected cause.
	 * @param c The Coordination to end.
	 * @param expectedType The ecpected type of the CoordinationException.
	 * @param expectedCause The expected cause of the CoordinationException.
	 */
	protected void assertEndFailed(Coordination c, int expectedType, Throwable expectedCause) {
		try {
			c.end();
			fail("Ending the coordination must result in a CoordinationException.");
		}
		catch (CoordinationException e) {
			assertCoordinationException(e, expectedType, expectedCause);
		}
	}
	
	/**
	 * Ensures the provided Coordination has a failure cause that is of the
	 * expected type.
	 * @param c The Coordination whose failure will be inspected.
	 * @param expectedFailure The expected failure.
	 */
	protected void assertFailure(Coordination c, Throwable expectedFailure) {
		assertEquals("The failure cause was incorrect.", expectedFailure, c.getFailure());
	}
	
	/**
	 * Ensures the provided Coordination is terminated.
	 * @param c The Coordination that should be terminated.
	 */
	protected void assertTerminated(Coordination c) {
		assertTrue("The coordination must be terminated.", c.isTerminated());
	}
	
	protected void setUp() throws Exception {
		// Clean up any coordinations that may be lingering on the thread local
		// stack from previous tests.
		Bundle bundle = getContext().getServiceReference(Coordinator.class.getName()).getBundle();
		bundle.stop();
		bundle.start();
		coordinatorReference = getContext().getServiceReference(Coordinator.class.getName());
		coordinator = (Coordinator)getContext().getService(coordinatorReference);
	}
	
	protected void tearDown() throws Exception {
		getContext().ungetService(coordinatorReference);
	}
}