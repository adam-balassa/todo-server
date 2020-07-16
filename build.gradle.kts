import org.gradle.api.tasks.testing.logging.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.1.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
	id("idea")
}

group = "hu.badam"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

idea {
	module {
		sourceDirs.add(file("generated/"))
		generatedSourceDirs.add(file("generated/"))
	}
}


val queryDslVersion = "4.2.1"
val lombokVersion = "1.18.12"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")


	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly ("mysql:mysql-connector-java")

	implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.13.3")
	implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.13.3")

	implementation("org.hibernate", "hibernate-core", "5.3.2.Final")

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-test")
	implementation("io.jsonwebtoken:jjwt-api:0.10.7")
	implementation("io.jsonwebtoken:jjwt-impl:0.10.7")
	implementation("io.jsonwebtoken:jjwt-jackson:0.10.7")

	implementation ("org.projectlombok:lombok:$lombokVersion")
	annotationProcessor ("org.projectlombok:lombok:$lombokVersion")

	implementation(group= "org.slf4j", name= "slf4j-api", version= "1.7.2")
	implementation(group= "org.apache.logging.log4j", name= "log4j-api", version= "2.11.1")
	implementation(group= "org.apache.logging.log4j", name= "log4j-core", version= "2.11.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
//	{
//		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
//	}
	testRuntimeOnly("com.h2database:h2")
//	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}
tasks.test {
	useJUnitPlatform()

	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
