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
import io.jrb.labs.docasm.model.EntityType;
import io.jrb.labs.docasm.model.LookupValue;
import io.jrb.labs.docasm.model.LookupValueType;
import io.jrb.labs.docasm.model.Projection;
import io.jrb.labs.docasm.repository.DocumentRepository;
import io.jrb.labs.docasm.repository.LookupValueRepository;
import io.jrb.labs.docasm.resource.DocumentResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DocumentServiceImpl extends CrudServiceSupport<Document, Document.DocumentBuilder>  implements DocumentService {

    private final DocumentRepository documentRepository;
    private final LookupValueRepository lookupValueRepository;

    public DocumentServiceImpl(
            final DocumentRepository documentRepository,
            final LookupValueRepository lookupValueRepository,
            final ObjectMapper objectMapper
    ) {
        super(Document.class, documentRepository, objectMapper);
        this.documentRepository = documentRepository;
        this.lookupValueRepository = lookupValueRepository;
    }

    @Override
    @Transactional
    public Mono<DocumentResource> createDocument(final DocumentResource document) {
        return createEntity(Document.fromResource(document))
                .zipWhen(documentEntity ->
                        createLookupValues(documentEntity.getId(), LookupValueType.TAG, document.getTags())
                )
                .map(tuple -> DocumentResource.fromEntity(tuple.getT1())
                        .tags(tuple.getT2())
                        .build());
    }

    @Override
    @Transactional
    public Mono<Void> deleteDocument(final UUID documentGuid) {
        return deleteEntity(documentGuid, documentEntity -> {
            final long documentId = documentEntity.getId();
            return lookupValueRepository.deleteByEntityTypeAndEntityId(EntityType.DOCUMENT, documentId)
                    .then(documentRepository.deleteById(documentId))
                    .then();
        });
    }

    @Override
    @Transactional
    public Mono<DocumentResource> findDocumentByGuid(final UUID documentGuid, Projection projection) {
        return findEntityByGuid(documentGuid)
                .zipWhen(task -> findValueList(task.getId(), projection))
                .map(tuple -> {
                    final DocumentResource.DocumentResourceBuilder builder = DocumentResource.fromEntity(tuple.getT1());
                    tuple.getT2().forEach(lookupValue -> {
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
                .map(entity -> DocumentResource.fromEntity(entity).build());    }

    @Override
    @Transactional
    public Mono<DocumentResource> updateDocument(final UUID guid, final JsonPatch patch) {
        return updateEntity(guid, entity -> {
            final DocumentResource resource = DocumentResource.fromEntity(entity).build();
            final DocumentResource updatedResource = applyPatch(guid, patch, resource, DocumentResource.class);
            return Document.fromResource(updatedResource);
        }).flatMap(taskEntity -> {
            final long taskId = taskEntity.getId();
            return findDocumentByGuid(guid, Projection.DETAILS);
        });
    }

    private Mono<List<String>> createLookupValues(
            final long taskId,
            final LookupValueType type,
            final List<String> values
    ) {
        return Flux.fromIterable(values)
                .map(value -> LookupValue.builder()
                        .entityType(EntityType.DOCUMENT)
                        .entityId(taskId)
                        .valueType(type)
                        .value(value)
                        .build())
                .flatMap(lookupValueRepository::save)
                .map(LookupValue::getValue)
                .collectList();
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
