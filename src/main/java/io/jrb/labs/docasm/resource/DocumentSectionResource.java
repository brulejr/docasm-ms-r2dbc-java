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
package io.jrb.labs.docasm.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.jrb.labs.docasm.model.DocumentSection;
import io.jrb.labs.docasm.model.Projection;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = DocumentSectionResource.DocumentSectionResourceBuilder.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentSectionResource {

    @JsonView(Projection.Summary.class)
    UUID guid;

    @JsonView(Projection.Summary.class)
    String name;

    @JsonView(Projection.Detail.class)
    String createdBy;

    @JsonView(Projection.Detail.class)
    Instant createdOn;

    @JsonView(Projection.Detail.class)
    String modifiedBy;

    @JsonView(Projection.Detail.class)
    Instant modifiedOn;

    public static DocumentSectionResource.DocumentSectionResourceBuilder fromEntity(
            final DocumentSection documentSection
    ) {
        return DocumentSectionResource.builder()
                .guid(documentSection.getGuid())
                .name(documentSection.getName())
                .createdBy(documentSection.getCreatedBy())
                .createdOn(documentSection.getCreatedOn())
                .modifiedBy(documentSection.getModifiedBy())
                .modifiedOn(documentSection.getModifiedOn());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class DocumentSectionResourceBuilder {
    }

}
