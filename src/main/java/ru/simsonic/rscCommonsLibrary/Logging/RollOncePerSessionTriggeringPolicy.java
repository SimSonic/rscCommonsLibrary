package ru.simsonic.rscCommonsLibrary.Logging;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RolloverFailure;

@NoAutoStart
public class RollOncePerSessionTriggeringPolicy<E> extends DefaultTimeBasedFileNamingAndTriggeringPolicy<E>
{
	@Override
	public void start()
	{
		super.start();
		super.nextCheck = 0L;
		isTriggeringEvent(null, null);
		try
		{
			super.tbrp.rollover();
		} catch(RolloverFailure e) {
			//Do nothing
		}
	}
}
