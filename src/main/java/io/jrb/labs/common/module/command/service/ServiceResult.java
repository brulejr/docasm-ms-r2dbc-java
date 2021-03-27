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

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides a wrapper for a command service response.
 *
 * @param <T> the content type
 */
@Value
@Builder
public class ServiceResult<T> {

    T data;

    Throwable exception;

    Instant startTime;

    Instant endTime;

    public long getElapsedTime() {
        final long startTimeEpochSecs = Optional.ofNullable(startTime).map(Instant::getEpochSecond).orElse(0L);
        final long endTimeEpochSecs = Optional.ofNullable(endTime).map(Instant::getEpochSecond).orElse(0L);
        return endTimeEpochSecs - startTimeEpochSecs;
    }

    public boolean hasException() { return exception != null; }

}
