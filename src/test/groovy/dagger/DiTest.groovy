package dagger

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Behavior Driven Development
 */
class DiTest extends Specification {
    @SuppressWarnings("GrDeprecatedAPIUsage")
    @Unroll
    def 'testValidBuilderClass #component'() {
        when:
        def builderOut = Di.find(component)
        def instanceOut = Di.create(component)

        then:
        Di.isCreatable(component)
        builder == builderOut
        component.isAssignableFrom instanceOut.class

        where:
        component                       || builder
        StubComponent                   || DaggerStubComponent
        StubInterface.NestedInterface   || DaggerStubInterface_NestedInterface
    }

    @SuppressWarnings(["GrEqualsBetweenInconvertibleTypes", "GroovyAssignabilityCheck", "GrDeprecatedAPIUsage"])
    @Unroll
    def 'testInvalidBuilderClass #component'() {
        when:
        Di.find(component)
        Di.create(component)

        then:
        Di.isCreatable(component) == creatable
        def exceptionOut = thrown(exception)

        if(message) {
            exceptionOut.message ==~ message

            (exceptionOut.message =~ message)[0].with {
                "${it[1]}.${it[2]}"
            } == component.name
        }

        where:
        component                       || creatable    | exception                    | message
        StubClass                       || false        | IllegalArgumentException     | /^([\w\.]+)\.([\w$]+) .*interface.*$/
        StubInterface                   || false        | IllegalArgumentException     | /^([\w\.]+)\.([\w$]+) .*Component.*$/
        UncompiledComponent             || false        | IllegalStateException        | /^([\w\.]+)\.([\w$]+) .*Dagger(?s).*$/
        HasDeps                         || false        | IllegalArgumentException     | /^([\w\.]+)\.Dagger([\w$]+)\.create\(\).*(dep|conf)(?s).*$/
        Private                         || false        | IllegalArgumentException     | /^([\w\.]+)\.Dagger([\w$]+)\.create\(\).*(dep|conf)(?s).*$/
        BadReturn                       || false        | IllegalStateException        | /^([\w\.]+)\.Dagger([\w$]+) .*\1\.\2.*$/
        Throws                          || true         | IllegalStateException        | /^([\w\.]+)\.Dagger([\w$]+)\.create\(\).*(thr|exception).*$/
    }

}

class StubClass {
}

interface StubInterface {
    @Component
    interface NestedInterface {
    }
}

@Component
class DaggerStubInterface_NestedInterface {
    public static StubInterface.NestedInterface create() {
        return new StubInterface.NestedInterface() {};
    }
}

@Component
interface UncompiledComponent {
}

@Component
interface HasDeps {
}

class DaggerHasDeps {
    // missing create method
}

@Component
interface BadReturn {
}

class DaggerBadReturn {
    public static Object create() {
        return null;
    }
}

@Component
interface StubComponent {
}

class DaggerStubComponent {
    public static StubComponent create() {
        return new StubComponent() {};
    }
}

@Component
interface Throws {
}

class DaggerThrows {
    public static Throws create() {
        throw new RuntimeException("derp");
    }
}

@Component
interface Private {
}

class DaggerPrivate {
    protected static Private create() {
        return null;
    }
}