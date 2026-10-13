plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common-contracts"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
}
