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
package io.jrb.labs.docasm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.jrb.labs.common.service.crud.CrudServiceSupport;
import io.jrb.labs.docasm.model.Document;
import io.jrb.labs.docasm.model.DocumentSection;
import io.jrb.labs.docasm.model.EntityType;
import io.jrb.labs.docasm.model.LookupValue;
import io.jrb.labs.docasm.model.LookupValueType;
import io.jrb.labs.docasm.model.Projection;
import io.jrb.labs.docasm.repository.DocumentRepository;
import io.jrb.labs.docasm.repository.DocumentSectionRepository;
import io.jrb.labs.docasm.repository.LookupValueRepository;
import io.jrb.labs.docasm.resource.DocumentResource;
import io.jrb.labs.docasm.resource.DocumentSectionResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DocumentServiceImpl extends CrudServiceSupport<Document, Document.DocumentBuilder>  implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentSectionRepository documentSectionRepository;
    private final LookupValueRepository lookupValueRepository;

    public DocumentServiceImpl(
            final DocumentRepository documentRepository,
            final DocumentSectionRepository documentSectionRepository,
            final LookupValueRepository lookupValueRepository,
            final ObjectMapper objectMapper
    ) {
        super(Document.class, documentRepository, objectMapper);
        this.documentRepository = documentRepository;
        this.documentSectionRepository = documentSectionRepository;
        this.lookupValueRepository = lookupValueRepository;
    }

    @Override
    @Transactional
    public Mono<DocumentResource> createDocument(final DocumentResource document) {
        return createEntity(Document.fromResource(document))
                .zipWhen(documentEntity -> Mono.zip(
                        createLookupValues(documentEntity.getId(), LookupValueType.TAG, document.getTags()),
                        createDocumentSections(documentEntity.getId(), document.getSections())
                ))
                .map(tuple -> DocumentResource.fromEntity(tuple.getT1())
                        .tags(tuple.getT2().getT1())
                        .sections(tuple.getT2().getT2())
                        .build());
    }

    @Override
    @Transactional
    public Mono<Void> deleteDocument(final UUID documentGuid) {
        return deleteEntity(documentGuid, documentEntity -> {
            final long documentId = documentEntity.getId();
            return documentSectionRepository.deleteByDocumentId(documentId)
                    .then(lookupValueRepository.deleteByEntityTypeAndEntityId(EntityType.DOCUMENT, documentId))
                    .then(documentRepository.deleteById(documentId))
                    .then();
        });
    }

    @Override
    @Transactional
    public Mono<DocumentResource> findDocumentByGuid(final UUID documentGuid, Projection projection) {
        return findEntityByGuid(documentGuid)
                .zipWhen(document -> Mono.zip(
                        findDocumentSectionList(document.getId(), projection),
                        findValueList(document.getId(), projection)
                ))
                .map(tuple -> {
                    final DocumentResource.DocumentResourceBuilder builder = DocumentResource.fromEntity(tuple.getT1());
                    tuple.getT2().getT1().forEach(documentSection -> {
                        final DocumentSectionResource section =
                                DocumentSectionResource.fromEntity(documentSection).build();
                        builder.section(section);
                    });
                    tuple.getT2().getT2().forEach(lookupValue -> {
                        final String value = lookupValue.getValue();
                        switch (lookupValue.getValueType()) {
                            case TAG:
                                builder.tag(value);
                                break;
                        }
                    });
                    return builder.build();
                });
    }

    @Override
    @Transactional
    public Flux<DocumentResource> listAllDocuments() {
        return retrieveEntities()
                .map(entity -> DocumentResource.fromEntity(entity).build());
    }

    @Override
    @Transactional
    public Mono<DocumentResource> updateDocument(final UUID guid, final JsonPatch patch) {
        return updateEntity(guid, entity -> {
            final DocumentResource resource = DocumentResource.fromEntity(entity).build();
            final DocumentResource updatedResource = applyPatch(guid, patch, resource, DocumentResource.class);
            return Document.fromResource(updatedResource);
        }).flatMap(documentEntity -> findDocumentByGuid(guid, Projection.DETAILS));
    }

    private Mono<List<DocumentSectionResource>> createDocumentSections(
            final long documentId,
            final List<DocumentSectionResource> sections
    ) {
        return Optional.ofNullable(sections)
                .map(sectionsList -> Flux.fromIterable(sections)
                        .map(section -> DocumentSection.fromResource(section)
                                .guid(UUID.randomUUID())
                                .documentId(documentId)
                                .build())
                        .flatMap(documentSectionRepository::save)
                        .map(ds -> DocumentSectionResource.fromEntity(ds).build())
                        .collectList())
                .orElse(Mono.just(Collections.emptyList()));
    }

    private Mono<List<String>> createLookupValues(
            final long documentId,
            final LookupValueType type,
            final List<String> values
    ) {
        return Flux.fromIterable(values)
                .map(value -> LookupValue.builder()
                        .entityType(EntityType.DOCUMENT)
                        .entityId(documentId)
                        .valueType(type)
                        .value(value)
                        .build())
                .flatMap(lookupValueRepository::save)
                .map(LookupValue::getValue)
                .collectList();
    }

    private Mono<List<DocumentSection>> findDocumentSectionList(final long entityId, final Projection projection) {
        if (projection == Projection.DEEP) {
            return documentSectionRepository.findAllByDocumentId(entityId)
                    .collectList();
        } else {
            return Mono.just(Collections.emptyList());
        }
    }

    private Mono<List<LookupValue>> findValueList(final long entityId, final Projection projection) {
        if (projection == Projection.DEEP) {
            return lookupValueRepository.findByEntityTypeAndEntityId(EntityType.DOCUMENT, entityId)
                    .collectList();
        } else {
            return Mono.just(Collections.emptyList());
        }
    }

}
