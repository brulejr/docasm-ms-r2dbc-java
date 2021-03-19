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
package io.jrb.labs.docasm.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.github.fge.jsonpatch.JsonPatch;
import io.jrb.labs.docasm.model.Projection;
import io.jrb.labs.docasm.resource.DocumentResource;
import io.jrb.labs.docasm.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/document")
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(final DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DocumentResource> createDocument(@RequestBody final DocumentResource document) {
        return documentService.createDocument(document);
    }

    @DeleteMapping("/{documentGuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDocument(@PathVariable final UUID documentGuid) {
        return documentService.deleteDocument(documentGuid);
    }

    @GetMapping("/{documentGuid}")
    public Mono<MappingJacksonValue> getDocumentById(
            @PathVariable final UUID documentGuid,
            @RequestParam(name = "projection", defaultValue = "DETAILS") final Projection projection
    ) {
        return documentService.findDocumentByGuid(documentGuid, projection)
                .map(resource -> wrapWithView(resource, projection));
    }

    @GetMapping
    @JsonView(Projection.Summary.class)
    public Flux<DocumentResource> listDocuments() {
        return documentService.listAllDocuments();
    }

    @PatchMapping("/{documentGuid}")
    public Mono<DocumentResource> updateDocument(
            @PathVariable final UUID documentGuid,
            @RequestBody final JsonPatch documentPatch
    ) {
        return documentService.updateDocument(documentGuid, documentPatch);
    }

    private <R> MappingJacksonValue wrapWithView(final R resource, final Projection projection) {
        final MappingJacksonValue result = new MappingJacksonValue(resource);
        result.setSerializationView(projection.getView());
        return result;
    }

}
