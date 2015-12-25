package examplemod.inject;

import dagger.Component;
import examplemod.ExampleMod;

@Component(modules = {
		ExampleModule.class
})
public interface ExampleComponent {
	void inject(ExampleMod mod);

	@Component(modules = {
			ExampleModule.PreInit.class
	})
	interface PreInit {
		void inject(ExampleMod mod);
	}

	@Component(modules = {
			ExampleModule.PreInit.class
	})
	interface Init {

		@Component(modules = {
				ExampleModule.PreInit.Prep.class
		})
		interface Prep {

		}
	}
}
