package examplemod.inject;

import dagger.Module;
import dagger.Provides;
import examplemod.impl.ThingOne;
import examplemod.impl.ThingTwo;

/**
 */
@Module
public class ExampleModule {
	@Provides
	public ThingOne provideThingOne() {
		return new ThingOne();
	}

	@Provides
	public ThingTwo provideThingTwo(ThingOne dependency) {
		return new ThingTwo();
	}

	@Module
	public static class PreInit {
		@Provides
		public ThingOne provideThingOne() {
			return new ThingOne();
		}

		@Module
		public static class Prep {
			@Provides
			public ThingOne provideThingOne() {
				return new ThingOne();
			}
		}
	}
}
