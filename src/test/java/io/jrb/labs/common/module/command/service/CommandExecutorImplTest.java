package io.jrb.labs.common.module.command.service;

import io.jrb.labs.common.module.command.service.command.PingCommand;
import io.jrb.labs.common.module.command.service.command.PingCommandImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringJUnitConfig(CommandExecutorImplTest.Config.class)
class CommandExecutorImplTest {

    @Configuration
    static class Config {

        @Bean
        Validator validator() {
            final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            return factory.getValidator();
        }

        @Bean
        CommandExecutor commandExecutor(
                final ApplicationContext applicationContext,
                final Validator validator
        ) {
            return new CommandExecutorImpl(applicationContext, validator);
        }

        @Bean
        PingCommand pingCommand() {
            return new PingCommandImpl();
        }

    }

    @Autowired
    private CommandExecutor commandExecutor;

    @Test
    void testCommandExecutor() {
        final ServiceRequest<?> request = ServiceRequest.builder().build();
        StepVerifier.create(commandExecutor.execute(PingCommand.class, request))
                .expectNextMatches(result -> {
                    assertThat(result, is(notNullValue()));
                    return true;
                })
                .expectComplete()
                .verify();
    }

}
