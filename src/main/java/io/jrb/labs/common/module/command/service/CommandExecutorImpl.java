/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jrb.labs.common.module.command.service;

import io.jrb.labs.common.module.command.service.exception.CommandValidationException;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.Instant;
import java.util.Set;

/**
 * Provides a command executor that validates a request before executing the commmand with the validated data.
 */
public class CommandExecutorImpl implements CommandExecutor {

    private final ApplicationContext applicationContext;
    private final Validator validator;

    public CommandExecutorImpl(
            final ApplicationContext applicationContext,
            final Validator validator
    ) {
        this.applicationContext = applicationContext;
        this.validator = validator;
    }

    @Override
    public <R extends CommandRequest, T extends CommandResponse> Mono<CommandResponseWrapper<T>> execute(
            final Class<? extends Command<R, T>> commandClass,
            final R request
    ) {
        final Command<R, T> command = applicationContext.getBean(commandClass);
        final Set<ConstraintViolation<R>> constraintViolations = validator.validate(request);
        if (constraintViolations.isEmpty()) {
            return Mono.just(Instant.now())
                    .zipWith(command.execute(request))
                    .map(tuple -> {
                        final Instant startTimestamp = tuple.getT1();
                        final T content = tuple.getT2();
                        return CommandResponseWrapper.<T>builder()
                                .startTimestamp(startTimestamp)
                                .content(content)
                                .endTimestamp(Instant.now())
                                .build();
                    });
        } else {
            return Mono.error(new CommandValidationException(constraintViolations));
        }
    }

}
