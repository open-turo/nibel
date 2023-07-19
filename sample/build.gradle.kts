subprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.openturo.nibel:nibel-runtime"))
                .using(project(":nibel-runtime"))

            substitute(module("com.openturo.nibel:nibel-compiler"))
                .using(project(":nibel-compiler"))
        }
    }
}
