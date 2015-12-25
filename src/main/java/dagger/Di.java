package dagger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Convenience methods to access dagger components.
 */
@SuppressWarnings("unused")
public class Di {
	public static final String DAGGER_COMPILE =
			"\n=============================================================\n" +
				"If this is your dagger component, ensure that\n" +
				"com.google.dagger:dagger-compiler:2.0.2 is available via your\n" +
				"build script's compiler classpath. If you are using gradle we\n" +
				"recommend the following:\n\n" +
				"plugins {\n" +
				"    id 'com.ewerk.gradle.plugins.dagger' version '1.0.0'\n" +
				"}\n\n" +
				"dagger {\n" +
				"    daggerSourcesDir = 'src/main/java'\n" +
				"}\n" +
			"\n=============================================================\n";

	public static final String DAGGER_DOCS =
			"\n=============================================================\n" +
					"See http://google.github.io/dagger/ for detailed API docs" +
			"\n=============================================================\n";

	protected enum Invalid {
		CONCRETE,
		UNANNOTATED,
		UNCOMPILED,
		UNCONFIGURED,
		BAD_RETURN,
		NOT_INVALID,
		;
	}

	/**
	 * DRY
	 */
	private static Invalid validateFind(Class<?> component) {
		if(! component.isInterface()) return Invalid.CONCRETE;
		if(component.getAnnotation(Component.class) == null) return Invalid.UNANNOTATED;

		return Invalid.NOT_INVALID;
	}

	/**
	 * DRY
	 */
	@SuppressWarnings("unchecked") // Dagger2 convention, enforced by validateMethod
	private static <T> Class<? extends T> doFind(Class<T> component) {
		String name = component.getName();
		int nameStart = name.lastIndexOf(".")+1;

		try {
			return (Class<? extends T>)Class.forName(name.substring(0,nameStart) + "Dagger" + name.substring(nameStart).replaceAll("\\$","_"),
					false, Di.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(component.getName() + " was not built with Dagger2" + DAGGER_COMPILE);
		}
	}

	/**
	 * DRY
	 */
	private static <T> Invalid validateMethod(Class<T> component, Method create) {
		if(! component.isAssignableFrom(create.getReturnType())) return Invalid.BAD_RETURN;

		return Invalid.NOT_INVALID;
	}

	/**
	 * Determines if the target component is immediately available on this classpath.
	 *
	 * @param component the component defining interface
	 * @return false if the component is not found or requires configuration
	 * @deprecated beta
	 */
	@Deprecated
	public static boolean isCreatable(Class<?> component) {
		switch (validateFind(component)) {
			default:
				return false;

			case NOT_INVALID:
				// continue
		}

		Class<?> clazz;
		try {
			clazz = doFind(component);
		} catch(IllegalStateException e) {
			// reason is Invalid.UNCOMPILED
			return false;
		}

		Method method;
		try {
			method = clazz.getMethod("create");
		} catch (NoSuchMethodException e) {
			// reason is Invalid.UNCONFIGURED
			return false;
		}

		switch (validateMethod(component,method)) {
			default:
				return false;

			case NOT_INVALID:
				// continue
		}

		return true;
	}

	/**
	 * Searches the classloader for a conventional Dagger2 implementation class.
	 *
	 * @throws IllegalArgumentException if the provided class does not have the shape of a Dagger2 component interface
	 * @throws IllegalStateException if the classpath does not have a compiled Dagger2 component corresponding to this interface
	 * @param component the component defining interface
	 * @param <T> the component interface type
	 * @return the Dagger2 component class
	 */
	public static <T> Class<? extends T> find(Class<T> component) throws IllegalArgumentException, IllegalStateException {
		Invalid validation = validateFind(component);
		switch(validation) {
			case CONCRETE:
				throw new IllegalArgumentException(component.getName() + " is not an interface");

			case UNANNOTATED:
				throw new IllegalArgumentException(component.getName() + " is not @dagger.Component");

			default:
				throw new IllegalStateException(validation.toString());

			case NOT_INVALID:
				// ok!
		}

		return doFind(component);
	}

	/**
	 * Creates a conventional Dagger2 component.
	 *
	 * @throws IllegalArgumentException if find(1) fails, or the component requires configuration
	 * @throws IllegalStateException if find(1) fails, or the implementation is invalid/tampered/unstable
	 * @param component the component defining interface
	 * @param <T> the component interface type
	 * @return the Dagger2 component instance
	 */
	@SuppressWarnings("unchecked") // Dagger2 convention, enforced by validateMethod
	public static <T> T create(Class<T> component) throws IllegalArgumentException, IllegalStateException {
		Class<? extends T> clazz = find(component);
		Method create;

		try {
			create = clazz.getMethod("create");
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(clazz.getName() + ".create() is missing, this component requires configuration." + DAGGER_DOCS);
		}

		Invalid validation = validateMethod(component, create);
		switch(validation) {
			case BAD_RETURN: throw new IllegalStateException(clazz.getName() + " does not return a " + component.getName());

			default:
				throw new IllegalStateException(validation.toString());

			case NOT_INVALID:
				// ok!
		}

		try {
			return (T) create.invoke(null);
		} catch(IllegalAccessException e) {
			throw new RuntimeException("Not possible in test scenarios, please report this bug.");
		} catch(InvocationTargetException e) {
			Throwable t = e.getCause();
			throw new IllegalStateException(clazz.getName() + ".create() threw an exception: " + t.toString(),t);
		}
	}
}
