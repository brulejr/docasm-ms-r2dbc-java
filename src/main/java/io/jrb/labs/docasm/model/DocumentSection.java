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
package io.jrb.labs.docasm.model;


import io.jrb.labs.common.entity.Entity;
import io.jrb.labs.common.entity.EntityBuilder;
import io.jrb.labs.docasm.resource.DocumentSectionResource;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@Table(value = "t_document_section")
public class DocumentSection implements Entity {

    @Id
    @Column(value = "ds_id")
    Long id;

    @Column(value = "ds_guid")
    UUID guid;

    @Column(value = "ds_name")
    String name;

    @Column(value = "ds_do_id")
    Long documentId;

    @CreatedBy
    @Column(value = "ds_created_by")
    String createdBy;

    @CreatedDate
    @Column(value = "ds_created_on")
    Instant createdOn;

    @LastModifiedBy
    @Column(value = "ds_modified_by")
    String modifiedBy;

    @LastModifiedDate
    @Column(value = "ds_modified_on")
    Instant modifiedOn;

    public static DocumentSectionBuilder fromResource(final DocumentSectionResource documentSectionResource) {
        return DocumentSection.builder()
                .guid(documentSectionResource.getGuid())
                .name(documentSectionResource.getName());
    }

    public static class DocumentSectionBuilder implements EntityBuilder<DocumentSection, DocumentSectionBuilder> {
    }

}
