package examplemod;

import dagger.Di;
import examplemod.impl.ThingOne;
import examplemod.impl.ThingTwo;
import examplemod.inject.ExampleComponent;

import javax.inject.Inject;

public class ExampleMod {
	ExampleComponent di;

	@Inject
	ThingOne one;

	public void preInit(Object fmlEvent) {
		// TODO make top-level component
	}

	public void init(Object fmlEvent) {
		// TODO make init phase component
	}

	public void postInit(Object fmlEvent) {
		// TODO make postinit phase component

		di = Di.create(ExampleComponent.class);

		di.inject(this);
	}
}
