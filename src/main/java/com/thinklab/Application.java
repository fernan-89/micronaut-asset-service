package com.thinklab;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

/**
 * Main Entry Point: Bootstrap class for the Thinklab IT Asset Management Service.
 * This class orchestrates the application startup using the Micronaut framework,
 * ensuring high scalability, low memory footprint, and non-blocking execution
 * required for global-scale hardware inventory systems.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>AOT Ignition:</b> Configured for Ahead-of-Time compilation to support
 * ultra-fast startup and GraalVM native image compatibility.</li>
 * <li><b>Contract Anchor:</b> Serves as the authoritative source for OpenAPI 3.0
 * metadata, ensuring consistent API discovery.</li>
 * <li><b>Reactive Foundation:</b> Initializes the Netty event loop to handle
 * asynchronous IT Asset lifecycle operations with zero-blocking I/O.</li>
 * </ul>
 *
 * @author Thinklab Staff Engineering
 * @version 1.0.0
 */
@OpenAPIDefinition(
        info = @Info(
                title = "IT Asset Management API",
                version = "1.0.0",
                description = "Authoritative reactive service for hardware provisioning, lifecycle orchestration, and forensic auditing within the Thinklab ecosystem.",
                contact = @Contact(name = "Thinklab Staff Engineering", email = "staff@thinklab.com"),
                license = @License(name = "Apache 2.0", url = "https://thinklab.com/licenses/LICENSE-2.0")
        )
)
public class Application {

    /**
     * Main method to launch the Micronaut runtime.
     *
     * @param args Command line arguments passed during startup.
     */
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}