package task.scheduler;

import org.junit.Before;
import org.junit.Test;
import task.scheduler.schedule.IScheduler;
import task.scheduler.schedule.SchedulerFactory;
import task.scheduler.schedule.valid.ValidScheduler;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class TestSchedulerFactory {

    private SchedulerFactory schedulerFactory;

    @Before
    public void setUp() {
        this.schedulerFactory = new SchedulerFactory();
    }

    @Test
    public void testSchedulerValidScheduler() {

        //Act
        IScheduler scheduler = schedulerFactory.createScheduler(SchedulerFactory.SchedulerType.VALID);

        //Assert
        assertThat(scheduler, instanceOf(ValidScheduler.class));
    }

    @Test
    public void testNull() {

        //Act
        try {
            schedulerFactory.createScheduler(null);
            fail();
        } catch (IllegalArgumentException e) {
            //Assert
            assertEquals("Null is not a valid argument", e.getMessage());
        }

    }

}
