package io.jrb.labs.common.module.command.service.command;

import io.jrb.labs.common.module.command.service.ServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class PingCommandImplTest {

    private PingCommand command;

    @BeforeEach
    void setup() {
        command = new PingCommandImpl();
    }

    @Test
    void testPingCommand() {
        final ServiceRequest<?> request = ServiceRequest.builder().build();
        StepVerifier.create(command.execute(request))
                .expectNextMatches(result -> {
                    assertThat(result, is(notNullValue()));
                    return true;
                })
                .expectComplete()
                .verify();
    }

}
